package me.net;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.common.annotation.IocAnno.Ioc;
import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.NetType.eStockOper;
import me.net.NetType.eStockSource;
import me.net.NetType.eStrategy;
import me.net.dao.StockAnalysisDao;
import me.net.dao.StockSourceDao;
import me.net.dayHandler.Simulator;
import me.net.model.OperRecord;
import me.net.model.StockDay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class ComputeSimulation {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private StockAnalysisDao stockAnalysisDao;
	@Ioc
	Simulator simulator;

	/****  用于统计   *****/
	int g_totalRecords = 0;
	int g_win = 0;
	int g_lose = 0;
	BigDecimal g_allRecordsSum = BigDecimal.ZERO;
	BigDecimal g_investment = BigDecimal.ZERO;

	private final eStrategy strategy = eStrategy.One;
	private final int one = 1;

	public void compute(String hcode) throws SQLException {

		List<StockDay> all = null;
		List<OperRecord> operList = new ArrayList<OperRecord>();

		//all = stockAnalysisDao.getDay(hcode, Constant.simulate.startDate, null);
		all = stockAnalysisDao.getDay(hcode, "2014-01-01", null);

		if (all.size() < 50) {
			logger.error("数据太少，只有：" + all.size());
			return;
		}

		this.addSequence(all);

		BigDecimal price = BigDecimal.ZERO;
		BigDecimal sum = BigDecimal.ZERO;
		int num = 0;
		int total = 0;
		BigDecimal remain = BigDecimal.ZERO;
		int symbol = 1; //表示正负
		Date date = null;
		int sn = 0;

		simulator.reset();

		for (int i = 0; i < all.size() - 1; i++) {
			StockDay someDay = all.get(i);
			StockDay nextDay = all.get(i + 1);

			eStockOper result = simulator.handle(someDay);

			if (result == eStockOper.None)
				continue;

			if (operList.size() == 0 && result != eStockOper.Buy) {
				//第一次必须是买
				continue;
			}

			sn++;

			//如果能在第二天以中间价处理，结果会理想很多
			if (result == eStockOper.Buy) {
				price = new BigDecimal(nextDay.high);
				//price = (Double.parseDouble(nextDay.high) + Double.parseDouble(nextDay.low)) / 2;
				date = nextDay.date_;
				symbol = 1;
			} else if (result == eStockOper.Sell) {
				price = new BigDecimal(nextDay.low);
				//price = (Double.parseDouble(nextDay.high) + Double.parseDouble(nextDay.low)) / 2;
				date = nextDay.date_;
				symbol = -1;
			}

			switch (strategy) {
			case One:
				//每次按结果操作一单位
				num = one;
				break;
			case OneBuyOneSell:
				//严格执行一买一卖，不然放弃
				if (operList.size() != 0 && result.toString() == operList.get(operList.size() - 1).getOper())
					continue;
				num = one;
			case Double:
				//第二次出现相同操作（如连续第二次买），则执行2倍的量
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
		}

		for (OperRecord record : operList) {
			System.out.println(record);
		}
		//		for (OperRecord record : operList) {
		//			//只需要卖光后的情况
		//			if (record.total == 0)
		//				logger.info(record.toString());
		//			System.out.println(record);
		//		}
		for (int i = operList.size() - 1; i >= 0; i--) {
			//最后一次卖光时的情况
			OperRecord record = operList.get(i);
			if (record.getTotal() == 0) {
				logger.info("code:" + hcode + "," + record.toString());
				g_totalRecords++;
				if (record.getRemain().doubleValue() > 0)
					g_win++;
				else
					g_lose++;

				if (record.getRemain().doubleValue() > 200) {
					System.out.println("too large");
					break; //去除结果过好数据
				}
				if (record.getRemain().doubleValue() < -200) {
					System.out.println("too low");
					break;
				}

				g_allRecordsSum = g_allRecordsSum.add(record.getRemain()); //g_allRecordsSum += record.remain;

				break;
			}
		}

		BigDecimal minRemain = BigDecimal.ZERO; //余额最小时，即最大的投资额度
		int buys = 0, sells = 0, times = 0;
		for (int i = operList.size() - 1; i >= 0; i--) {
			OperRecord record = operList.get(i);
			if (record.getRemain().compareTo(minRemain) == -1) //record.remain < minRemain
				minRemain = record.getRemain();
			if (record.getTotal() == 0)
				times++;
			if (record.getOper() == eStockOper.Buy.toString())
				buys++;
			if (record.getOper() == eStockOper.Sell.toString())
				sells++;
		}
		g_investment = g_investment.subtract(minRemain); //g_investment -= minRemain;

		logger.info("*********code:{},   buys:{}, sells:{}, total=0 times:{}", hcode, buys, sells, times);

	}

	private void addSequence(List<StockDay> list) {

		for (int i = 0; i < list.size(); i++) {
			list.get(i).sn = i;
		}
	}

	private void runOne() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = BeanContext.me();
		ComputeSimulation simulation = (ComputeSimulation) bc.getBean("computeSimulation");

		try {
			//String code = "002061.sz";
			//String code = "600836.ss"; //有代表性
			//String code = "000895.sz";
			String code = "000001.ss"; //上证指数

			simulation.compute(code);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void runAll() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = BeanContext.me();
		ComputeSimulation simulation = (ComputeSimulation) bc.getBean("computeSimulation");

		try {
			List<String> codes = new StockSourceDao().getAllAvailableCodes(0, eStockSource.YAHOO);
			System.out.println(codes.size());
			for (String code : codes) {
				simulation.compute(code);
			}
			System.out.println("\n\n************************************************************\n\n");
			System.out.println(String.format("total:%s, win:%s, lose:%s, remain:%s, investment:%s",
					simulation.g_totalRecords, simulation.g_win, simulation.g_lose, simulation.g_allRecordsSum,
					simulation.g_investment));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		new ComputeSimulation().runAll();

	}

	public StockAnalysisDao getStockAnalysisDao() {
		return stockAnalysisDao;
	}

	public void setStockAnalysisDao(StockAnalysisDao stockAnalysisDao) {
		this.stockAnalysisDao = stockAnalysisDao;
	}

}
