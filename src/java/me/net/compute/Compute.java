package me.net.compute;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.TypeUtil;
import me.net.NetType.eStockOper;
import me.net.NetType.eStrategy;
import me.net.dao.StockAnalysisDao;
import me.net.dao.StockSourceDao;
import me.net.model.OperRecord;
import me.net.model.StockDay;
import me.net.model.StockOperSum;

/**
 * 本类提供计算买入、卖出操作的业务逻辑
 * 
 * 概念解释：
 * 20200205: remain含义是手里的钱剩多少，开始时是0。作用是：负数表示曾经最大的投入额；正数表示盈利；
 * 				最后一刻全卖了，可以看到是赚了还是亏了。
 * 			balance 含义是某一天以当天最低价格把持有的股票都卖了，余额是多少。作用是为了看看在这种买卖策略下，随时卖掉是否会
 * 				亏损很大，也就是想看看持仓是不是一直亏损;如某一天，看到自己的持仓已经亏了很多，可能会觉得风险太大，
 * 				这时并不确定以后能升回来。持仓一直亏损考验人的耐心，也对资金的随时取出使用带来困难。
 * remain = 产出 - 投入。当产出为0时，remain的绝对值=投入
 * 		remain 也可看作是负债，一开始时借钱买，如果有盈利，就会还钱，盈利多的时候，直接使用盈利去买
 * 		不用借了；如果是亏的，则钱越借越多了
 * @author James
 *
 */
public abstract class Compute {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private StockAnalysisDao stockAnalysisDao;
	@Ioc
	private StockSourceDao stockSourceDao;
	
	// --------------- 可配置参数  -------------
	protected eStrategy strategy = eStrategy.One;
	protected String startDate = null;
	protected String endDate = null;
	protected boolean printOperLog = false;
	protected boolean isPractice = false;
	//不以量来买，以总价来买，更贴合实际
	protected final int oneAmount = 10000;
	protected double abnormal = 20000; //绝对值超过这个值视为异常值
	// --------------- 可配置参数 end  -------------
	
	//操作记录列表
	private Map<String, List<OperRecord>> g_operListMap = new HashMap<String, List<OperRecord>>();
	private List<StockOperSum> g_operSumList = new ArrayList<StockOperSum>();
	private Map<String, List<EveryDayGain>> g_gainListMap = new HashMap<String, List<EveryDayGain>>();

	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 每个code 计算前做的准备工作
	 */
	abstract public void codeStart();
	/**
	 * code 计算后执行
	 * @param operList：操作记录列表
	 */
	abstract public void codeEnd(List<OperRecord> operList);
	abstract public void buyOrSell(String hcode, StockDay someDay, 
			StockDay nextDay, List<OperRecord> operList);
	//abstract public List<EveryDayGain> computeGain(List<OperRecord> operList, List<StockDay> all);
	
	/**
	 * 计算某个 code 的操作
	 * @param hcode
	 * @throws Exception 
	 */
	public void compute(String hcode) throws Exception {
		List<StockDay> all = null;
		List<OperRecord> operList = new ArrayList<OperRecord>();
		
		all = stockAnalysisDao.getDay(hcode, startDate, endDate);
		this.addSequence(all);

		codeStart();
		
		for (int i = 0; i <= all.size() - 1; i++) {
			StockDay someDay = all.get(i);
			StockDay nextDay = null;
			if(i != all.size() - 1)
				nextDay = all.get(i + 1);
			buyOrSell(hcode, someDay, nextDay, operList);
		}
		
		codeEnd(operList);
		
		g_operListMap.put(hcode, operList);

		computeOperSum(hcode, operList);
		
		List<EveryDayGain> list = computeGain(operList, all);
		if(list != null && list.size() > 0)
			g_gainListMap.put(hcode, list);
	}
	
	/**
	 * 计算所有 code
	 */
	public void computeAll() {

		long startTime = System.currentTimeMillis();
		try {
			g_operSumList.clear();
			for (String code : g_operListMap.keySet())
				g_operListMap.get(code).clear();
			g_operListMap.clear();

			List<String> codes = stockSourceDao.getAllAvailableCodes(0, null);
			for (String code : codes) {
				this.compute(code);
			}
			
			logger.info(summary(false));
			summaryGain();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTime=System.currentTimeMillis();
		logger.info("运行时间： "+(endTime-startTime)+" ms");
	}
	
	/**
	 * 为每个日期的信息增加序列号
	 * @param list
	 */
	private void addSequence(List<StockDay> list) {

		for (int i = 0; i < list.size(); i++) {
			list.get(i).sn = i;
		}
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

//		if(lastRemain.doubleValue() < 0) {
//			logger.info(operSum.toString());
//			for(OperRecord record : operList) {
//				logger.debug(record.toString());
//			}
//		}
		if(operList.size() > 0)
			logger.info(operSum.toString());
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
		Date start = format.parse(startDate);
		Date end = new Date();
		if(!TypeUtil.isEmpty(endDate))
			end = format.parse(endDate);
		
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
		//BigDecimal factor = new BigDecimal(1000000);
		BigDecimal factor = new BigDecimal(1);
		return String.format("total:%s, win stocks:%s, lose:%s, last day sell stocks:%s, remain:%.0f, investment:%.0f|%s, buys:%s, sells:%s, win times:%s, lose times:%s", 
				operSumList.size(), win, lose, lastDaySell, allRecordsSum.divide(factor), investment.divide(factor), minDate, buys, sells, wins, loses);
	}
	
	/**
	 * 计算每一天的收益合计
	 * 最后一天balance 不等于 remain原因：有可能在倒数第二决定最后一天买，这些买入就不会卖掉。因为卖的时候判断了，
	 * 									有买入就不能卖。
	 * @return
	 * @throws Exception
	 */
	public String summaryGain() throws Exception {
		Calendar c = Calendar.getInstance();
		Date start = format.parse(startDate);
		Date end = new Date();
		if(!TypeUtil.isEmpty(endDate))
			end = format.parse(endDate);

		c.setTime(start);
		while (start.before(end)) {
			BigDecimal balanceSum = BigDecimal.ZERO;
			BigDecimal balance = BigDecimal.ZERO;
			//计算 gain
			for(Map.Entry<String,List<EveryDayGain>> codeGainList : g_gainListMap.entrySet()) {
				List<EveryDayGain> gainList = codeGainList.getValue();
				//code = codeGainList.getKey();
				for (int i = 0; i < gainList.size(); i++) {
					EveryDayGain gain = gainList.get(i);
					if (start.before(gain.date_)) { //start < gain.date
						break;
					} 
					balance = gain.balance;
				}
				
				balanceSum = balanceSum.add(balance);
			}
			
			//
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
			
			if(balanceSum.doubleValue() != 0 || allRemain.doubleValue() != 0)
			//对比 gain 和 remain，可以看到在多大的负债情况下获得此收益。
				logger.info(String.format("%s %.0f %.0f", 
						format.format(start), balanceSum, allRemain));
			c.add(Calendar.DAY_OF_YEAR, 1);
			start = c.getTime();
		}
		
		return null;
	}
	
	public List<EveryDayGain> computeGain(List<OperRecord> operList, List<StockDay> all) {

		List<EveryDayGain> gainList = new ArrayList<EveryDayGain>();
		if(operList == null || operList.size() == 0) 
			return null;

		int j = 0;
		int total = 0; //开始时没有数量
		BigDecimal remain = BigDecimal.ZERO;
		OperRecord record = operList.get(j);
		for(int i = 0; i< all.size(); i++) {
			StockDay day = all.get(i);
			//第一次买之前 gain 都是0
			//前后两次操作之间的数量 等于 前面那次的数量
			Date operDate = record.getDate_();
			if(!day.date_.before(operDate)) { //20200205 resolve bug
				total = record.getTotal();
				remain = record.getRemain();
				if(j < operList.size()-1) 
					record = operList.get(++j); 
			}

			EveryDayGain gain = new EveryDayGain();
			gain.date_ = day.date_;
			//假设以最低价格卖
			BigDecimal price = new BigDecimal(day.low);
			gain.balance = price.multiply(new BigDecimal(total)).add(remain);
			gainList.add(gain);
			
		}//end for
		//输出每一天 gain
		
		//output(gainList);
		
		return gainList;
		
	}
	
	public void setStrategy(eStrategy strategy) {
		this.strategy = strategy;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public void setPrintOperLog(boolean printOperLog) {
		this.printOperLog = printOperLog;
	}
	public void setPractice(boolean isPractice) {
		this.isPractice = isPractice;
	}
	
	class EveryDayGain {
		public Date date_;
		public BigDecimal balance; //当前日期的结算
	}
}
