package me.service.stock;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.common.annotation.IocAnno.Ioc;
import me.common.jdbcutil.Page;
import me.common.util.Util;
import me.net.NetType.eStockOper;
import me.net.NetType.eStockSource;
import me.net.NetType.eStrategy;
import me.net.dao.StockAnalysisDao;
import me.net.dao.StockSourceDao;
import me.net.dayHandler.Simulator;
import me.net.model.OperRecord;
import me.net.model.StockDay;
import me.net.model.StockOperSum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private StockAnalysisDao stockAnalysisDao;
	@Ioc
	Simulator simulator;
	@Ioc
	private StockSourceDao stockSourceDao;

	/**   配置参数   *****/
	private eStrategy strategy = eStrategy.OneBuyOneSell; //策略
	private double abnormal = 20000; //绝对值超过这个值视为异常值
	public int c_priceStrategy = 1; //以什么策略来交易：1:第二天最差价格，2：今天最差价格 3：第二天中间价格 4:按中枢价格
	public String c_startDate = "2015-06-01";
	public String c_endDate = null;

	public String c_sellAllDate = null; //在这一天全部卖出
	//private boolean isPersistent = false;

	private final int one = 10;

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

		all = stockAnalysisDao.getDay(hcode, c_startDate, c_endDate);

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

		simulator.reset();

		for (int i = 0; i <= all.size() - 1; i++) {
			StockDay someDay = all.get(i);
			StockDay nextDay = new StockDay();
			if(i != all.size() - 1)
				nextDay = all.get(i + 1);

			eStockOper result = simulator.handle2(someDay);
			//eStockOper result = simulator.handle_old(someDay);

			if (result == eStockOper.None) {

				//卖时机是通过资金管理手段来控制
				if (operList.size() == 0)
					continue;
				OperRecord last = operList.get(operList.size() - 1);
				if (last.getTotal() == 0)
					continue;
				if (last.getDate_() != null && last.getDate_().equals(someDay.date_)) //昨天决定今天买，今天不能卖
					continue;

				BigDecimal price_l = new BigDecimal(someDay.high);
				int total_l = last.getTotal();
				BigDecimal gain_l = price_l.multiply(new BigDecimal(total_l));
				date = someDay.date_;
				int num_l = 0;
				BigDecimal sum_l = BigDecimal.ZERO;

				// *********** 最简单的资金管理，赚10%即卖
				if (i == all.size() - 2) {//最后一天，全卖
					num_l = total_l;
				} else if (gain_l.doubleValue() > cost.doubleValue() * 1) {
					num_l = total_l;
				} else if (gain_l.doubleValue() < cost.doubleValue() * 0.9) {
//					num_l = (int) Math.ceil((double)total_l / 2); //进位取整
				} else if (gain_l.doubleValue() < cost.doubleValue() * 0.8) {
//					num_l = total_l; 
				}

				if (num_l > 0) {
					sum_l = price_l.multiply(new BigDecimal(num_l));
					remain = remain.add(sum_l);//remain += -sum; //买是付钱，用负表示
					cost = cost.multiply(new BigDecimal(total_l - num_l / total_l));
					//输出前后2天，共5天的k线
					operList.add(new OperRecord(++sn, hcode, eStockOper.Sell.toString(), num_l, price_l, sum_l,
							total_l - num_l, remain, date));
//					logger.info(String.format("%s:(%s, %s),%s:(%s, %s),%s:(%s, %s),%s:(%s, %s)",
//					all.get(i - 2).date_, all.get(i - 2).low, all.get(i - 2).high, all.get(i - 1).date_,
//					all.get(i - 1).low, all.get(i - 1).high, all.get(i).date_, all.get(i).low, all.get(i).high,
//					i==all.size()-1?"":all.get(i + 1).date_, i==all.size()-1?"":all.get(i + 1).low, i==all.size()-1?"":all.get(i + 1).high));
//					logger.debug(operList.get(operList.size() - 1).toString());
				}

				//				double average = last.getRemain().divide(new BigDecimal(last.getTotal())).abs().doubleValue();
				//				double low = Double.parseDouble(someDay.low);
				//				int total_l = last.getTotal();
				//				BigDecimal gain_l = BigDecimal.ZERO;
				//				
				//				//今天价格大于最后一次买的价格，采取赢利判断。（虽然今天价格大于最后价格，但可能小于平均价格）
				//				if(low > last.getPrice().doubleValue()) {
				//					gain_l = new BigDecimal(someDay.low).multiply(new BigDecimal(total_l)).subtract(last.getRemain().abs());
				//					if(gain_l.doubleValue() > gain.doubleValue()) 
				//						gain = gain_l;
				//					//else if(gain > 0){
				//						
				//					//}
				//				}
				//				
				//				//情况1，买后就下跌
				//				if(low <= average * 0.8) {
				//					num = total_l;
				//				} else if(low <= average * 0.85) {
				//					num = (int)Math.floor(total_l/2); //向上取整
				//				} else if(low <= average * 0.9) {
				//					num = (int)Math.floor(total_l/2); //向上取整
				//				} 

				continue;
			}

			//最后一天卖，所以前一天计算出来也不买。（这里最后一天是指倒数第二天）
			if (i == all.size() - 2) {
				//logger.debug(hcode + " last 2 " + someDay.date_);
				continue;
			}
			if (i == all.size() - 1) {
				//logger.debug(hcode + " last 1 " + someDay.date_);
				continue;
			}

			//只取买入时机
			symbol = 1;

			//输出前后2天，共5天的k线


			//第二天最差价格买
			price = new BigDecimal(nextDay.high);
			date = nextDay.date_;

			//每次买1个单位
			num = one;

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
//			logger.info(String.format("%s:(%s, %s),%s:(%s, %s),%s:(%s, %s),%s:(%s, %s),%s:(%s, %s)",
//					all.get(i - 2).date_, all.get(i - 2).low, all.get(i - 2).high, all.get(i - 1).date_,
//					all.get(i - 1).low, all.get(i - 1).high, all.get(i).date_, all.get(i).low, all.get(i).high,
//					all.get(i + 1).date_, all.get(i + 1).low, all.get(i + 1).high, all.get(i + 2).date_,
//					all.get(i + 2).low, all.get(i + 2).high));
//			logger.debug(operList.get(operList.size() - 1).toString());

		} //end for
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
					lastFlag = "01";
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
			if (record.getOper() == eStockOper.Buy.toString())
				buys++;
			if (record.getOper() == eStockOper.Sell.toString())
				sells++;
		}

		StockOperSum operSum = new StockOperSum(buys, sells, times, winTimes, loseTimes, lastRemain, minRemain,
				lastFlag);
		operSum.setCode(hcode);
		operSum.setName(stockAnalysisDao.getName(hcode.substring(0, hcode.length() - 3)));

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

		List<StockOperSum> operSumList;
		if (isDb)
			operSumList = stockAnalysisDao.getAllCodeSum(true, null);
		else
			operSumList = g_operSumList;

		for (StockOperSum record : operSumList) {
			if (record.getLastRemain().doubleValue() > 0)
				win++;
			else if (record.getLastRemain().doubleValue() < 0)
				lose++;

			allRecordsSum = allRecordsSum.add(record.getLastRemain()); //allRecordsSum += record.remain;
			investment = investment.subtract(record.getMinRemain()); //investment -= minRemain;

		}

		return String.format("total:%s, win:%s, lose:%s, remain:%s, investment:%s", operSumList.size(), win, lose,
				allRecordsSum, investment);
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

			List<String> codes = stockSourceDao.getAllAvailableCodes(0, eStockSource.YAHOO);
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
				if (c_startDate != null && c_startDate.equals(allDates.get(i))) {
					index = i;
					break;
				}
			}

			List<String> subDates = allDates.subList(index, allDates.size());
			for (int i = 0; i < subDates.size(); i++) {
				String date = subDates.get(i);
				c_sellAllDate = date;
				computeAll();
				System.out.println(date + "," + this.summary(false));
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

	/**
	 * 返回所有code的汇总数据
	 * @return
	 */
	public List<StockOperSum> getOperSumList() {

		return g_operSumList;
	}

	/**
	 * 分页返回汇总数据
	 * @return
	 */
	public Page getOperSumList(int page, int rows, String sort, String order) {

		Page p = new Page();

		if (sort != null) {
			Util.sort(g_operSumList, StockOperSum.class, sort, order);
		}

		int size = g_operSumList.size();
		if ((page - 1) * rows < size && size < page * rows)
			p.list = g_operSumList.subList((page - 1) * rows, size);
		else if (size <= (page - 1) * rows)
			p.list = Collections.emptyList();
		else
			p.list = g_operSumList.subList((page - 1) * rows, page * rows);

		p.total = g_operSumList.size();

		return p;
	}

	/**
	 * 返回所有code的汇总数据，从数据库读取
	 * @return
	 * @throws Exception 
	 */
	public List<StockOperSum> getOperSumListDB(Map<String, String> voMap) throws Exception {

		g_operSumList = stockAnalysisDao.getAllCodeSum(false, voMap);
		return g_operSumList;
	}

	/**
	 * 获取某个 code 的详细操作
	 * @param code
	 * @return
	 */
	public List<OperRecord> getOperList(String code) {
		//假设 code 是 hcode 形式
		return g_operListMap.get(code);
	}

	/**
	 * 获取某个 code 的详细操作，从数据库读取
	 * @param code
	 * @return
	 * @throws SQLException 
	 */
	public List<OperRecord> getOperListDB(String code) throws SQLException {
		//假设 code 是 hcode 形式
		return stockAnalysisDao.getOperList(code);
	}

	/**
	 * 将计算结果数据保存到数据库
	 */
	public void saveToDb() {

		//保存前先清空
		stockAnalysisDao.clearOperation();
		stockAnalysisDao.clearOperSum();

		if (g_operListMap == null || g_operListMap.size() == 0 || g_operSumList == null || g_operSumList.size() == 0) {
			logger.info("没有数据，请先进行计算.");
			return;
		}

		//将所有code的操作放在一个list，一次性保存
		List<OperRecord> all = new ArrayList<OperRecord>();
		for (List<OperRecord> list : g_operListMap.values())
			all.addAll(list);

		stockAnalysisDao.saveOperList(all);
		stockAnalysisDao.saveOperSums(g_operSumList);
	}

	private void addSequence(List<StockDay> list) {

		for (int i = 0; i < list.size(); i++) {
			list.get(i).sn = i;
		}
	}

	/**
	 * 生成此 code 的所有操作数据
	 * @param hcode
	 * @throws SQLException
	 */
	public void compute_old(String hcode) throws SQLException {
		List<StockDay> all = null;
		List<OperRecord> operList = new ArrayList<OperRecord>();

		if (c_sellAllDate != null)
			all = stockAnalysisDao.getDayCache(hcode, c_startDate, c_endDate);
		else
			all = stockAnalysisDao.getDay(hcode, c_startDate, c_endDate);

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

		simulator.reset();

		for (int i = 0; i < all.size() - 1; i++) {
			StockDay someDay = all.get(i);
			StockDay nextDay = all.get(i + 1);

			if (c_sellAllDate != null && c_sellAllDate.compareTo(nextDay.date_.toString()) < 0)
				break;
			if (c_sellAllDate != null && c_sellAllDate.equals(nextDay.date_.toString())) {
				//如果明天要卖，取消今天的操作。因为今天的操作是以今天的最后结果来决定的。
				if (operList.size() > 0) {
					OperRecord rec = operList.get(operList.size() - 1);
					if (rec.getTotal() > 0) {
						num = rec.getTotal();
						total = 0;

						price = new BigDecimal(nextDay.low);
						symbol = -1;
						sum = price.multiply(new BigDecimal(symbol * num));
						remain = remain.subtract(sum);//remain += -sum; //买是付钱，用负表示
						date = nextDay.date_;
						operList.add(new OperRecord(sn, hcode, eStockOper.Sell.toString(), num, price, sum, total,
								remain, date));
						break;
					}
				}

			}

			StockDay tmp = new StockDay();
			eStockOper result = simulator.handle(someDay, nextDay, tmp);
			//eStockOper result = simulator.handle_old(someDay);

			if (result == eStockOper.None)
				continue;

			if (operList.size() == 0 && result != eStockOper.Buy) {
				//第一次必须是买
				continue;
			}

			sn++;

			//如果能在第二天以中间价处理，结果会理想很多
			if (result == eStockOper.Buy) {
				switch (c_priceStrategy) {
				case 1: //第二天最差价格交易
					price = new BigDecimal(nextDay.high);
					date = nextDay.date_;
					break;
				case 2://今天最差价格
					price = new BigDecimal(someDay.high);
					date = someDay.date_;
					break;
				case 3://第二天中间价格
					price = new BigDecimal(nextDay.high).add(new BigDecimal(nextDay.low)).divide(new BigDecimal(2));
					date = nextDay.date_;
					break;
				case 4:
					price = new BigDecimal(tmp.low);
					break;
				default:
				}
				//price = (Double.parseDouble(nextDay.high) + Double.parseDouble(nextDay.low)) / 2;
				symbol = 1;
			} else if (result == eStockOper.Sell) {
				switch (c_priceStrategy) {
				case 1: //第二天最差价格交易
					price = new BigDecimal(nextDay.low);
					date = nextDay.date_;
					break;
				case 2://今天最差价格
					price = new BigDecimal(someDay.low);
					date = someDay.date_;
					break;
				case 3://第二天中间价格
					price = new BigDecimal(nextDay.high).add(new BigDecimal(nextDay.low)).divide(new BigDecimal(2));
					date = nextDay.date_;
					break;
				case 4:
					price = new BigDecimal(tmp.high);
					break;
				default:
				}
				//price = (Double.parseDouble(nextDay.high) + Double.parseDouble(nextDay.low)) / 2;
				symbol = -1;
			}

			switch (strategy) {
			case One:
				//每次操作一单位
				num = one;
				break;
			case OneBuyOneSell:
				//严格执行一买一卖，不然放弃
				if (operList.size() != 0 && result.toString() == operList.get(operList.size() - 1).getOper())
					//if (operList.size() > 1 && result.toString() == operList.get(operList.size() - 1).getOper()
					//		&& result.toString() == operList.get(operList.size() - 2).getOper())
					continue;
				num = one;
			case Double:
				//第二次出现相同操作（如连续第二次买），则执行2倍的量
				num = one;
				if (operList.size() != 0 && result.toString() == operList.get(operList.size() - 1).getOper())
					num = one * 2;
				if (operList.size() > 1 && result.toString() == operList.get(operList.size() - 1).getOper()
						&& result.toString() == operList.get(operList.size() - 2).getOper())
					num = one * 4;
				if (operList.size() > 2 && result.toString() == operList.get(operList.size() - 1).getOper()
						&& result.toString() == operList.get(operList.size() - 2).getOper()
						&& result.toString() == operList.get(operList.size() - 3).getOper())
					continue;
				break;
			}

			total += symbol * num;
			sum = price.multiply(new BigDecimal(symbol * num));//symbol * price * num;

			if (total < 0) {
				total = 0;
				continue;
			}
			remain = remain.subtract(sum);//remain += -sum; //买是付钱，用负表示
			operList.add(new OperRecord(sn, hcode, result.toString(), one, price, sum, total, remain, date));

		} //end for
		g_operListMap.put(hcode, operList);

		this.computeOperSum(hcode, operList);
	}

}
