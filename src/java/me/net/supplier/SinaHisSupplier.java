package me.net.supplier;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.Config;
import me.common.SimpleException;
import me.common.util.Util;
import me.net.NetType.eStockSource;
import me.net.model.StockDay;

public class SinaHisSupplier implements IStockSupplier {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final String url = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_FuQuanMarketHistory/stockid/%s.phtml?year=%s&jidu=%s";
	//指数的url不一样
	//private final String url_index = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/%s/type/S.phtml?year=%s&jidu=%s";
	private final static String charset = "UTF-8";
	
	@Override
	public List<?> getData(List<String> codes, Object... obj) {

		/**
		 * start 和 end 日期都被包括
		 */
		
		String start = (String) obj[0];
		String end = (String) obj[1];
		if(start.compareTo(end) > 0)
			return null;
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
		
        try {
    		
			int startSeason = Util.getSeason(format.parse(start));
			int endSeason = Util.getSeason(format.parse(end));
			
			int sYear = Integer.parseInt(start.substring(0, 4));
			int eYear = Integer.parseInt(end.substring(0, 4));
			
			List<int[]> seasons = this.getAllSeason(sYear, startSeason, eYear, endSeason);
				
			//return this.sigleThread(httpclient, codes, seasons);
			List<StockDay> days = new ArrayList<StockDay>();
			//days = this.multiThread(httpclient, codes, seasons);
			days = sigleThread(httpclient, codes, seasons);
			
			//筛选，取start与end之间
			Date startDate = format.parse(start);
			Date endDate = format.parse(end);
			List<StockDay> result = new ArrayList<StockDay>();
			for(StockDay day : days) {
				if(!day.date_.before(startDate) && !day.date_.after(endDate)) //startDate <= date <= endDate
					result.add(day);
			}
			
			return result;
			
		} catch(SimpleException se) {
			throw se;
		} catch (Exception e) { 
			//发生exception，则返回空表。要保持原子性，不成功就全部时间不成功。
			//不然只是中间部分不成功的话，程序没办法检查出来重新执行。因为没有数据有可能是停牌
			throw new SimpleException(e);
			//return null;
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public Object[][] findAbnormal(List<String> codes) {
		return null;
	}
	
	@SuppressWarnings("unused")
	private List<StockDay> sigleThread(CloseableHttpClient httpclient, List<String> codes, List<int[]> seasons) throws Exception, IOException {

		List<StockDay> days = new ArrayList<StockDay>();
		
		for (String code : codes) {
			for(int i=0; i<seasons.size(); i++) {
				int[] s = seasons.get(i);
				String urlStr = String.format(url, code, s[0], s[1]);
	            
				//Document doc = Jsoup.connect(urlStr).get(); //默认超时时间是3秒
				HttpGet httpget = new HttpGet(urlStr);
				
				days.addAll(parseToDay(code, getData(httpclient, httpget)));
			}
			
		}
		return days;
	}
	
	/**
	 * 多线程去获取数据。
	 * 性能有所提升，但并没有预期中好。
	 * 如果改为全局对象，而不是阻塞的形式去获取结果，性能应该可以更好一些。但因为系统假设各个服务都是 ioc 注入，即都是单例，使用全局对象会影响单例并发执行，所以暂不考虑。
	 * @param httpclient
	 * @param codes
	 * @param seasons
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ExecutionException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<StockDay> multiThread(CloseableHttpClient httpclient, List<String> codes, List<int[]> seasons) throws IOException, InterruptedException, ExecutionException {

        List<StockDay> days = new ArrayList<StockDay>();
        int multiSize = 5; //10线程

		ExecutorService pool = Executors.newFixedThreadPool(multiSize);
		try {
            int j = 0;
            Future f[] = new Future[multiSize];
			
			for (String code : codes) {
				for(int i=0; i<seasons.size(); i++) {
					int[] s = seasons.get(i);
					String urlStr = String.format(url, code, s[0], s[1]);
		            
					HttpGet httpget = new HttpGet(urlStr);
					
					//启动线程
					f[j] = pool.submit(new GetThread(httpclient, httpget, code));
					
					if(++j == multiSize) {
						//线程都满了，就去获取结果，这时会阻塞
						for(Future ff : f) {
							days.addAll((List<StockDay>)ff.get()); //不能catch exception。要保持原子性，不成功就全部时间不成功。不然只是中间部分不成功的话，程序没办法检查出来重新执行。因为没有数据有可能是停牌
						}
						j = 0;
					}
				}
			}
			
			//剩余未获取结果的线程
			for(int i=0; i<j; i++) {
				days.addAll((List<StockDay>)f[i].get());
			}

			pool.shutdown(); //马上关闭
			try {
			     pool.awaitTermination(30, TimeUnit.SECONDS);//最长等待时间，也即此程序最长执行时间
			} catch (InterruptedException e) {
			     e.printStackTrace();
			}
        } finally {
            httpclient.close();
        }
		
		return days;
	}
	
	
	/**
	 * 获取2个时间点之间的所有季度
	 * @param startYear
	 * @param startSeason
	 * @param endYear
	 * @param endSeason
	 * @return 返回一个数组的list，数组第一位是年，第二位是季度
	 */
	private List<int[]> getAllSeason(int startYear, int startSeason, int endYear, int endSeason) {
		List<int[]> seasons = new ArrayList<int[]>();
		int year = startYear;
		int season = startSeason;
		while (year < endYear) {
			while (season <= 4) {
				seasons.add(new int[] { year, season });
				season++;
			}
			year++;
			season = 1;
		}
		while(season <= endSeason) {
			seasons.add(new int[] { year, season });
			season++;
		}
		
		return seasons;
	}
	
	/**
	 * 使用 httpclient 获取网页
	 * @param httpclient
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private String getData(CloseableHttpClient httpclient, HttpGet httpget) throws IOException {
		
		//设置代理
        String ip = InetAddress.getLocalHost().getHostAddress();
		if ("10.132.8.78".equals(ip)) {
			HttpHost proxy = new HttpHost(Config.net.proxy_ip, Integer.parseInt(Config.net.proxy_port), "http");

	        RequestConfig config = RequestConfig.custom()
	                .setProxy(proxy)
	                .build();
	        httpget.setConfig(config);
		}
		
        CloseableHttpResponse response = httpclient.execute(httpget);
        try {
            // 获取响应实体
            HttpEntity entity = response.getEntity();
            // 打印响应状态
            if (entity != null) {
            	//toString 函数本身也会尝试在 header 中查找 charset，没找到才用 default的。
            	//如果没有default，则 text/html content type的charset默认是 ISO_8859_1
            	//很多时候 header 中也没有，但html 页面倒是有说明
                return EntityUtils.toString(entity, charset);
            }
        } finally {
            response.close();
        }
        
        return null;
	}
	
	private List<StockDay> parseToDay(String code, String html) throws Exception {

		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		List<StockDay> days = new ArrayList<StockDay>();
		if(html.contains("拒绝访问")) {
			logger.error("sina access denied");
			throw new SimpleException("sina access denied");
		}
		try {
			Document doc = Jsoup.parse(html);
			Elements trs = doc.select("#FundHoldSharesTable").select("tr");
			
			for(int j = 2;j<trs.size();j++){ //跳过表头
	            Elements tds = trs.get(j).select("td");
	            if(tds.size() == 8) {
	            	StockDay day = new StockDay();
	            	day.setCode(code);
	            	day.setSource(eStockSource.SINA.toString());
	            	
	            	//'date', 'open', 'high', 'close', 'low', 'volume', 'amount', 'factor']
	            	day.setDate_(format1.parse(tds.get(0).text()));
	            	day.setOpen_(tds.get(1).text());
	            	day.setHigh(tds.get(2).text());
	            	day.setClose_(tds.get(3).text());
	            	day.setLow(tds.get(4).text());
	            	day.setVolume(tds.get(5).text());
	            	day.setFactor(tds.get(7).text());
	            	
	            	//logger.debug(day.toString());
	            	days.add(day);
	            }
	        }
		} catch(Exception e) {
			//重新抛出异常，不返回任何数据，以保持原子性
			throw new SimpleException(e); 
		}
		return days;
	}

	@SuppressWarnings("rawtypes")
	class GetThread implements Callable {

		private final CloseableHttpClient httpClient;
		//private final HttpContext context;
		private final HttpGet httpget;
		private final String code;

		public GetThread(CloseableHttpClient httpClient, HttpGet httpget, String code) {
			this.httpClient = httpClient;
			//this.context = new BasicHttpContext();
			this.httpget = httpget;
			this.code = code;
		}

		@Override
		public Object call() throws Exception, IOException {
			return parseToDay(code, getData(httpClient, httpget));
		}

	}

}
