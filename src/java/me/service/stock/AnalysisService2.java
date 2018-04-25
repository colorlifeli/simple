package me.service.stock;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.TypeUtil;
import me.net.NetType.eStockOper;
import me.net.NetType.eStrategy;
import me.net.dao.StockAnalysisDao;
import me.net.dao.StockSourceDao;
import me.net.dayHandler.Simulator2;
import me.net.model.Central;
import me.net.model.OperRecord;
import me.net.model.StockDay;
import me.net.model.StockOperSum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisService2 {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private StockAnalysisDao stockAnalysisDao;
	@Ioc
	Simulator2 simulator2;
	@Ioc
	private StockSourceDao stockSourceDao;

	/**   配置参数   *****/
	private eStrategy strategy = eStrategy.OneBuyOneSell; //策略
	private double abnormal = 20000; //绝对值超过这个值视为异常值
	public int c_priceStrategy = 1; //以什么策略来交易：1:第二天最差价格，2：今天最差价格 3：第二天中间价格 4:按中枢价格
	public String c_startDate = "2015-04-01"; //2013-01-01  2015-06-01 2014-04-01
	public String c_endDate = null;  //startdate 与 enddate都是不包含

	public String c_sellAllDate = null; //在这一天全部卖出
	private boolean printOperLog = false;
	private boolean isPractice = false;  //如果是实际操作，则最后一天不卖，倒数第二天和最后一天要计算买

	private final int one = 10;
	//不以量来买，以总价来买，更贴合实际
	private final int oneAmount = 10000000;

	private Map<String, List<OperRecord>> g_operListMap = new HashMap<String, List<OperRecord>>();
	private List<StockOperSum> g_operSumList = new ArrayList<StockOperSum>();

	/**
	 * 生成此 code 的所有操作数据
	 * @param hcode
	 * @throws SQLException
	 */
	public void compute(String hcode) throws SQLException {
		List<StockDay> all = null;
		List<OperRecord> operList = new ArrayList<OperRecord>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		// ******  在这一天全部卖出。此功能仅在这一处进行了修改

		if(c_sellAllDate == null) {
			all = stockAnalysisDao.getDay(hcode, c_startDate, c_endDate);
		}
		else {
			List<StockDay> cache = stockAnalysisDao.getDayCache(hcode, c_startDate, c_endDate);
			int date_index = 0;
			for(int i=0; i<cache.size(); i++) {
				try {
					if(cache.get(i).date_.after(format.parse(c_sellAllDate)))
						break;
				} catch (ParseException e) {
					e.printStackTrace();
					return;
				}
				date_index = i;
			}
			all = cache.subList(0, date_index + 1);
		}
		
		// ********************

		//logger.debug(hcode);

		if (all.size() < 50) {
			//logger.error("数据太少，只有：" + all.size());
			return;
		}

		this.addSequence(all);

		BigDecimal price = BigDecimal.ZERO;
		BigDecimal sum = BigDecimal.ZERO;
		int num = 0;
		int total = 0;
		BigDecimal remain = BigDecimal.ZERO;
		int symbol = 1; //表示正负
		int sn = 0;
		Date date = new Date();
		//BigDecimal gain = BigDecimal.ZERO;//在买入后到今天这个时间段内的最大赢利
		BigDecimal cost = BigDecimal.ZERO; //成本

		simulator2.reset();

		//中枢买卖辅助
		int central_size = 0;
		int last_sn = 0;
		
		for (int i = 0; i <= all.size() - 1; i++) {
			StockDay someDay = all.get(i);
			StockDay nextDay = new StockDay();
			if(i != all.size() - 1)
				nextDay = all.get(i + 1);

			eStockOper result = simulator2.handle(someDay, nextDay);
			//eStockOper result = simulator.handle_old(someDay);
			
			//根据中枢来决定买卖
			if(simulator2.info.centrals.size() > 0 ) {
				if(central_size != simulator2.info.centrals.size()) {
					central_size = simulator2.info.centrals.size();
					last_sn = someDay.sn;
				}
				Central c = simulator2.info.centrals.get(simulator2.info.centrals.size() -1);
				if(someDay.sn - last_sn > 100) {
					//表明中枢比较稳定, 一段时间没有变化
					if(Double.parseDouble(someDay.high) < Double
							.parseDouble(c.low)) {
						//result = eStockOper.Buy;
					}
				}
			}

			if (result == eStockOper.None || (!isPractice && i == all.size() - 1) || result == eStockOper.Sell) { //最后一天是特殊的，必须全卖

				//卖时机是通过资金管理手段来控制
				if (operList.size() == 0)
					continue;
				OperRecord last = operList.get(operList.size() - 1);
				if (last.getTotal() == 0)
					continue;
				if (last.getDate_() != null && last.getDate_().equals(someDay.date_)) //昨天决定今天买，今天不能卖
					continue;

				BigDecimal price_l = new BigDecimal(someDay.low);
				int total_l = last.getTotal();
				cost = last.getRemain().negate();
				BigDecimal gain_l = price_l.multiply(new BigDecimal(total_l));
				date = someDay.date_;
				int num_l = 0;
				BigDecimal sum_l = BigDecimal.ZERO;

				// *********** 最简单的资金管理，赚10%即卖
				if (i == all.size() - 1 && !isPractice) {//最后一天，全卖
					num_l = total_l;
					sn = 998;
				} else if(result == eStockOper.Sell) {
					num_l = total_l;
					logger.debug("oper is sell : {},{},{}", hcode, date, num_l);
				} else if (gain_l.doubleValue() > cost.doubleValue() * 1.05) {
					num_l = total_l;
				} else if (gain_l.doubleValue() < cost.doubleValue() * 0.9) {
					//logger.debug("0.9 do nothing : {},{},{}", hcode, date, num_l);
					//num_l = total_l;
//					num_l = (int) Math.ceil((double)total_l / 2); //进位取整
				} else if (gain_l.doubleValue() < cost.doubleValue() * 0.8) {
					//num_l = (int) Math.ceil((double)total_l / 2); 
					num_l = total_l;
					logger.debug("0.8 sell part : {},{},{}", hcode, date, num_l);
				} else if (gain_l.doubleValue() < cost.doubleValue() * 0.5) {
					//num_l = total_l;
					logger.debug("0.5 sell all : {},{},{}", hcode, date, num_l);
				}

				if (num_l > 0) {
					sum_l = price_l.multiply(new BigDecimal(num_l));
					remain = remain.add(sum_l);//remain += -sum; //买是付钱，用负表示
					//20180412 这里 cost计算有问题，成本直接用 remain 好了
					cost = cost.multiply(new BigDecimal((total_l - num_l) / total_l));
					//输出前后2天，共5天的k线
					operList.add(new OperRecord(++sn, hcode, eStockOper.Sell.toString(), num_l, price_l, sum_l,
							total_l - num_l, remain, date));
					if(printOperLog)
					System.out.println(operList.get(operList.size() - 1).toString());
				}

				continue;
			}
			
			int stop = all.size()/2>200?all.size()-200:all.size()/2; //为了测试大部分都卖出的情况，在最后100天或最后2分之一的时间不再买入
			if(i > stop) {
				//continue;
			}

			//最后一天卖，所以前一天计算出来也不买。（这里最后一天是指倒数第二天，因为买是用第二的价格）
			if (i == all.size() - 2 && !isPractice) {
				//logger.debug(hcode + " last 2 " + someDay.date_);
				continue;
			}
			if (i == all.size() - 1) {
				if(isPractice)
					logger.debug("****** tomorrow buy!! {} {}  *********", someDay.date_, hcode);
				continue;
			}
			
			//执行一买一卖
			if(operList.size() != 0 && operList.get(operList.size() - 1).getTotal() != 0)
				continue;

			//只取买入时机
			symbol = 1;

			//输出前后2天，共5天的k线


			//第二天最差价格买
			price = new BigDecimal(nextDay.high);
			date = nextDay.date_;

			//每次买1个单位
//			num = one;
			// 以固定总价来买, 取一个整的数量，然后以这个数量来买。卖还是以量来卖 
			num = oneAmount/price.toBigInteger().intValue();

			if(operList.size() > 0) {
				total = operList.get(operList.size() -1 ).getTotal() + symbol * num;
			} else
				total = symbol * num;
			sum = price.multiply(new BigDecimal(symbol * num));//symbol * price * num;

			if (total < 0) {
				total = 0;
				continue;
			}
			
			remain = remain.subtract(sum);//remain += -sum; //买是付钱，用负表示
			cost = cost.add(sum);
			operList.add(new OperRecord(++sn, hcode, result.toString(), num, price, sum, total, remain, date));

			//if(date.equals(new Date()))
			if (isPractice) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.WEEK_OF_MONTH, -4); //打印最近1个星期可买的
				if (date.after(c.getTime())) {
					String factor = stockAnalysisDao.getFactor(hcode, date);
					BigDecimal aprice = price.divide(new BigDecimal(factor), 2);
					System.out.println(operList.get(operList.size() - 1).toString() + String.format("actual price:%.2f", aprice));
				}
			}

			if(printOperLog)
			System.out.println(operList.get(operList.size() - 1).toString());

		} //end for
		
		if(simulator2.info.centrals.size() < 4 ) {
//			System.out.println(hcode);
//			simulator2.info.printCentrals();
		}
		g_operListMap.put(hcode, operList);

		this.computeOperSum(hcode, operList);
	}

	/**
	 * 对操作数据的分析与汇总
	 * @param hcode
	 * @param operList
	 * @throws SQLException 
	 */
	private void computeOperSum(String hcode, List<OperRecord> operList) throws SQLException {

		BigDecimal lastRemain = BigDecimal.ZERO;
		String lastFlag = "00";

		//最后一次卖光时的情况
		for (int i = operList.size() - 1; i >= 0; i--) {
			OperRecord record = operList.get(i);
			if (record.getTotal() == 0) {
				lastRemain = record.getRemain();

				//去除结果过好过坏数据
				if (Math.abs(record.getRemain().doubleValue()) > abnormal) {
					if(lastRemain.doubleValue() > 0)
						lastFlag = "01";
					if(lastRemain.doubleValue() < 0)
						lastFlag = "02";
				}

				break;
			}
		}

		BigDecimal minRemain = BigDecimal.ZERO; //余额最小时，即最大的投资额度
		int buys = 0, sells = 0, times = 0, winTimes = 0, loseTimes = 0;
		for (int i = operList.size() - 1; i >= 0; i--) {
			OperRecord record = operList.get(i);
			if (record.getRemain().compareTo(minRemain) == -1) //record.remain < minRemain
				minRemain = record.getRemain();
			if (record.getTotal() == 0) {
				times++;
				if (record.getRemain().doubleValue() > 0)
					winTimes++;
				else
					loseTimes++;
			}
			if (record.getOper().equals(eStockOper.Buy.toString()))
				buys++;
			if (record.getOper().equals(eStockOper.Sell.toString()))
				sells++;
		}
		
		if(operList.size() > 0 && operList.get(operList.size()-1).getSn() == 999) {
			//logger.debug("" + operList.get(operList.size()-1).getTotal());
			lastFlag = "11"; //not sell out
		}

		StockOperSum operSum = new StockOperSum(buys, sells, times, winTimes, loseTimes, lastRemain, minRemain,
				lastFlag);
		operSum.setCode(hcode);
		//operSum.setName(stockAnalysisDao.getName(hcode.substring(0, hcode.length() - 3)));
		operSum.setName(stockAnalysisDao.getName(hcode));

		g_operSumList.add(operSum);

		if(lastRemain.doubleValue() < 0) {
//			logger.info(operSum.toString());
//			for(OperRecord record : operList) {
//				logger.debug(record.toString());
//			}
		}
	}

	/**
	 * 对所有 code 的汇总数据再次汇总
	 * @return
	 * @throws Exception 
	 */
	public String summary(boolean isDb) throws Exception {

		int win = 0;
		int lose = 0;
		BigDecimal allRecordsSum = BigDecimal.ZERO;
		BigDecimal investment = BigDecimal.ZERO;
		int buys = 0;
		int sells = 0;
		int wins = 0; //所有总计赢利次数
		int loses = 0;//所有总计亏本次数
		int lastDaySell = 0; //最后一日逼不得已才卖出

		List<StockOperSum> operSumList;
		if (isDb)
			operSumList = stockAnalysisDao.getAllCodeSum(true, null);
		else
			operSumList = g_operSumList;

		for (StockOperSum record : operSumList) {
			
			buys += record.getBuys();
			sells += record.getSells();
			wins += record.getWinTimes();
			loses += record.getLoseTimes();
			
			if("01".equals(record.getFlag())) {
				//logger.debug("too good data, abandon. " + record);
				//continue;
			}
			if("02".equals(record.getFlag())) {
				//logger.debug("too bad data, abandon. " + record);
				//continue;
			}
			if (record.getLastRemain().doubleValue() > 0)
				win++;
			else if (record.getLastRemain().doubleValue() < 0)
				lose++;

			allRecordsSum = allRecordsSum.add(record.getLastRemain()); //allRecordsSum += record.remain;
			//investment = investment.subtract(record.getMinRemain()); //investment -= minRemain;
			
			if("11".equals(record.getFlag()))
				lastDaySell++;

		}
		
		//计算每天的投资额，得出最大投资
		String minDate = "";
		BigDecimal min = BigDecimal.ZERO;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		Date start = format.parse(c_startDate);
		Date end = new Date();
		if(!TypeUtil.isEmpty(c_endDate))
			end = format.parse(c_endDate);
		
		c.setTime(start);
		while(start.before(end)) {
			BigDecimal allRemain = BigDecimal.ZERO;
			for (String code : g_operListMap.keySet()) {
				List<OperRecord> operList = g_operListMap.get(code);
				int i =0;
				if(operList == null || operList.size() == 0)
					continue;
				for(; i<operList.size(); i++) {
					if(start.before(operList.get(i).getDate_())) {// 操作记录的日期 > 当前日期 时取前一条操作记录  也就是最接近当前日期的操作记录
						if(i>0) {
							allRemain = allRemain.add(operList.get(i-1).getRemain()); 
						}
						break;
					}
					if(i == operList.size() - 1) { //所有记录都在当前日期之前，则取最后一条记录
						allRemain = allRemain.add(operList.get(i).getRemain()); 
					}
				}
			}
			
			if(allRemain.compareTo(min) < 0) {
				min = allRemain;
				minDate = format.format(start);
			}
			
			c.add(Calendar.DAY_OF_YEAR, 1);
			start = c.getTime();
		}
		investment = min.abs();

		//数值太大没什么意义，主要还是看比例
		BigDecimal factor = new BigDecimal(1000000);
		return String.format("total:%s, win stocks:%s, lose:%s, last day sell stocks:%s, remain:%.0f, investment:%.0f|%s, buys:%s, sells:%s, win times:%s, lose times:%s", 
				operSumList.size(), win, lose, lastDaySell, allRecordsSum.divide(factor), investment.divide(factor), minDate, buys, sells, wins, loses);
	}

	/**
	 * 计算所有 code
	 */
	public void computeAll() {

		try {
			g_operSumList.clear();
			for (String code : g_operListMap.keySet())
				g_operListMap.get(code).clear();
			g_operListMap.clear();

			List<String> codes = stockSourceDao.getAllAvailableCodes(0, null);
			for (String code : codes) {
				this.compute(code);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void sellSomeday2() {

		List<String> allDates;
		try {
			allDates = stockAnalysisDao.getAllDate();

			int index = 0;
			for (int i = 0; i < allDates.size(); i++) {
				if (c_startDate != null && c_startDate.compareTo(allDates.get(i)) < 0) {
					index = i;
					break;
				}
			}

			List<String> subDates = allDates.subList(index, allDates.size());
			//要有一定天数后才有意义
			int beginSellDay = 100;
			if(beginSellDay > subDates.size())
				return;
			//for (int i = beginSellDay; i < subDates.size(); i++) {
			for (int i = subDates.size() - 1; i >= beginSellDay ; i--) {
				String date = subDates.get(i);
				c_sellAllDate = date;
				computeAll();
				System.out.println(date + "," + this.summary(false));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//改变的是开始时间
	public void sellSomeday3() {

		List<String> allDates;
		try {
			allDates = stockAnalysisDao.getAllDate();

			int index = 0;
			for (int i = 0; i < allDates.size(); i++) {
				if (c_startDate != null && c_startDate.compareTo(allDates.get(i)) < 0) {
					index = i;
					break;
				}
			}

			List<String> subDates = allDates.subList(index, allDates.size());
			
			c_endDate = "2016-10-12";
			//要有一定天数后才有意义
			int endSellDay = 100;
			if(endSellDay > subDates.size())
				return;
			//for (int i = beginSellDay; i < subDates.size(); i++) {
			for (int i = 0; i <subDates.size() - endSellDay; i++) {
				c_startDate = subDates.get(i);
				computeAll();
				System.out.println(c_startDate + "," + this.summary(false));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sellSomeday() {

		try {

			c_sellAllDate = "2016-05-19";
			computeAll();
			System.out.println(c_sellAllDate + "," + this.summary(false));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addSequence(List<StockDay> list) {

		for (int i = 0; i < list.size(); i++) {
			list.get(i).sn = i;
		}
	}

}
