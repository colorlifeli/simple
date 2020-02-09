package me.net.supplier;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.Config;
import me.common.SimpleException;
import me.common.annotation.IocAnno.Ioc;
import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.JsonUtil;
import me.common.util.Util;
import me.net.NetType.eStockSource;
import me.net.dao.StockSourceDao;
import me.net.model.StockDay;
import me.net.model.StockDaySina;

/**
 * 20200207 原新浪接口失效，SinaHisSupplier不能使用
 * 
 * 现使用新浪另一接口，不过此接口估计也不会长久存在。
 * 
 * 包含2个接口：
 * 1.获取每日数据。
 * http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sz002095&scale=240&ma=no&datalen=1023
 * 此数据是未复权数据。symbol是代码；scale是间隔，单位分钟，如240是4小时，一天开盘时间4小时，也就是每日k线数据了；
 * ma表示均线数据，去掉ma=no，就能显示均线数据；datalen表示要获取的数据量
 * 2.复权：
 * http://finance.sina.com.cn/realstock/company/sz002095/<houfuquan/qianfuquan>.js[?d=2020-02-06]
 * 需要的是后复权数据。结果只包含日期和收盘价。
 * 需要计算后复权因子 factor，从而再推送出开盘价、最高价、最低价的后复权数据。
 * 
 * @author ljc
 *
 */
public class SinaHisSupplier2 implements IStockSupplier {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final String url = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=%s&scale=240&ma=no&datalen=%s";
	private final String url_houfuquan = "http://finance.sina.com.cn/realstock/company/%s/houfuquan.js";
	private final static String charset = "UTF-8";

	@Ioc
	private StockSourceDao stockSourceDao;
	
	@Override
	public List<?> getData(List<String> codes, Object... obj) {

		/**
		 * start 和 end 日期都被包括
		 */
		
		String start = (String) obj[0];
		String end = (String) obj[1];
		if(start.compareTo(end) > 0)
			return null;

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
		
        try {
        	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

			Date startDate = format.parse(start);
			Date endDate = format.parse(end);
 	
			List<String> codes_sina = stockSourceDao.getCodes(codes, eStockSource.SINA);
			//List<StockDay> days = this.multiThread(httpclient, codes_sina, startDate, endDate);
			List<StockDay> days = sigleThread(httpclient, codes_sina, startDate, endDate);
			
			return days;
			
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
	
	private List<StockDay> sigleThread(CloseableHttpClient httpclient, List<String> codes, Date startDate, Date endDate)
			throws Exception {

		List<StockDay> days = new ArrayList<StockDay>();

		//由于获取的是当前时间之前的数据，所以要计算当前时间与 start的天数
    	Date now = new Date();        	
    	int dayNum = Util.totalDays(startDate, now);
    	//周六日不开盘，减去一定时间(不好判断，先不实现）
		
		for (String code : codes) {
			String urlStr = String.format(url, code, dayNum);
			HttpGet httpget = new HttpGet(urlStr);
			String json = getData(httpclient, httpget);
			
			httpget = null;
			urlStr = String.format(url_houfuquan, code);
			httpget = new HttpGet(urlStr);
			String json_houfuquan = getData(httpclient, httpget);
			
			try {
				days.addAll(parseToDay(code, json, json_houfuquan, startDate, endDate));
			} catch (Exception e) {
				logger.error(json);
				logger.error(json_houfuquan);
				throw e;
			}
			
		}
		return days;
	}
	
	/**
	 * 多线程去获取数据。
	 * 性能有所提升，但并没有预期中好。
	 * 如果改为全局对象，而不是阻塞的形式去获取结果，性能应该可以更好一些。但因为系统假设各个服务都是 ioc 注入，即都是单例，
	 * 		使用全局对象会影响单例并发执行，所以暂不考虑。
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<StockDay> multiThread(CloseableHttpClient httpclient, List<String> codes, Date startDate, Date endDate)
			throws Exception {

        List<StockDay> days = new ArrayList<StockDay>();
        int multiSize = 5; //10线程
        Date now = new Date();        	
    	int dayNum = Util.totalDays(startDate, now);

		ExecutorService pool = Executors.newFixedThreadPool(multiSize);
		try {
            int j = 0;
            Future f[] = new Future[multiSize];
			
			for (String code : codes) {

				// 启动线程
				f[j] = pool.submit(new GetThread(httpclient, code, startDate, endDate, dayNum));

				if (++j == multiSize) {
					// 线程都满了，就去获取结果，这时会阻塞
					for (Future ff : f) {
						days.addAll((List<StockDay>) ff.get()); // 不能catch
																// exception。要保持原子性，不成功就全部时间不成功。不然只是中间部分不成功的话，程序没办法检查出来重新执行。因为没有数据有可能是停牌
					}
					j = 0;
				}

			}
			
			//剩余未获取结果的线程
			for(int i=0; i<j; i++) {
				days.addAll((List<StockDay>)f[i].get());
			}
			
			
        } finally {
			pool.shutdown(); //马上关闭
			try {
			     pool.awaitTermination(30, TimeUnit.SECONDS);//最长等待时间，也即此程序最长执行时间
			} catch (InterruptedException e) {
			     e.printStackTrace();
			}
            httpclient.close();
        }
		
		return days;
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
	
	private List<StockDay> parseToDay(String code, String json, String json_houfuquan, Date startDate, Date endDate)
			throws Exception {

//		logger.info(json);
		List<StockDay> days = new ArrayList<StockDay>();

		//houfuquanData不是规范json，只能特殊处理
		json_houfuquan = json_houfuquan.substring(json_houfuquan.indexOf('{', 2)+1, json_houfuquan.indexOf('}'));
		String[] hou_datas = json_houfuquan.split(",");
		Map<String, String> houfuquanMap = new HashMap<String, String>();
		for(String item : hou_datas) {
			String date = item.split(":")[0];
			String close = item.split(":")[1];
			//处理格式
			date = date.substring(1);
			date = date.replace('_', '-');
			
			close = close.replace("\"", "");
			houfuquanMap.put(date, close);
		}
		
//		for(String key: houfuquanMap.keySet()) {
//			System.out.println(key + ":" +houfuquanMap.get(key));
//		}
		
		List<StockDaySina> datas = JsonUtil.parseToObjectList(json, StockDaySina.class);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		for(StockDaySina data : datas) {
			Date date_ = format.parse(data.day);
			if(!date_.before(startDate) && !date_.after(endDate)) { //startDate <= date <= endDate
				BigDecimal close_hou = new BigDecimal(houfuquanMap.get(data.day));
				BigDecimal close = new BigDecimal(data.close);
				BigDecimal factor = close_hou.divide(close, 3, BigDecimal.ROUND_HALF_UP);
				
            	StockDay day = new StockDay();
            	day.setCode(code.substring(2));
            	day.setSource(eStockSource.SINA.toString());
            	
            	day.setDate_(date_);
            	//不使用 factor，重新计以减少误差
            	BigDecimal open_ = new BigDecimal(data.open).multiply(close_hou).divide(close, 3, BigDecimal.ROUND_HALF_UP);
            	day.setOpen_(open_.setScale(3, BigDecimal.ROUND_HALF_UP).toString());

            	BigDecimal high = new BigDecimal(data.high).multiply(close_hou).divide(close, 3, BigDecimal.ROUND_HALF_UP);
            	day.setHigh(high.setScale(3, BigDecimal.ROUND_HALF_UP).toString());
            	
            	day.setClose_(close_hou.setScale(3, BigDecimal.ROUND_HALF_UP).toString());

            	BigDecimal low = new BigDecimal(data.low).multiply(close_hou).divide(close, 3, BigDecimal.ROUND_HALF_UP);
            	day.setLow(low.setScale(3, BigDecimal.ROUND_HALF_UP).toString());
            	
            	day.setVolume(data.volume);
            	
            	day.setFactor(factor.setScale(3, BigDecimal.ROUND_HALF_UP).toString());
            	
            	//logger.debug(day.toString());
            	days.add(day);
			}
		}
		
		return days;
	}

	@SuppressWarnings("rawtypes")
	class GetThread implements Callable {

		private final CloseableHttpClient httpClient;
		private final String code;
		private final Date startDate;
		private final Date endDate;
		private final int dayNum;

		public GetThread(CloseableHttpClient httpClient, String code, Date startDate, Date endDate, int dayNum) {
			this.httpClient = httpClient;
			this.startDate = startDate;
			this.endDate = endDate;
			this.dayNum = dayNum;
			this.code = code;
		}

		@Override
		public Object call() throws Exception, IOException {

			String urlStr = String.format(url, code, dayNum);
			HttpGet httpget = new HttpGet(urlStr);
			String json = getData(httpClient, httpget);
			
			httpget = null;
			urlStr = String.format(url_houfuquan, code);
			httpget = new HttpGet(urlStr);
			String json_houfuquan = getData(httpClient, httpget);
			
			return parseToDay(code, json, json_houfuquan, startDate, endDate);
		}

	}
	
	
	public static void main(String[] args) {
		//SinaHisSupplier2 supplier = new SinaHisSupplier2();
		SqlRunner.me().setConn(H2Helper.connEmbededDb());
		BeanContext bc = BeanContext.me();
		SinaHisSupplier2 supplier = (SinaHisSupplier2) bc.getBean("sinaHisSupplier2");
		List<String> codes = new ArrayList<String>();
		codes.add("002095");

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		supplier.getData(codes, "20161101", "20161110");
	}

}
