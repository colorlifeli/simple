package me.net;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.annotation.IocAnno.Ioc;
import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.NetType.eStockOper;
import me.net.NetType.eStockSource;
import me.net.dayHandler.Simulator;
import me.net.model.StockDay;

public class ComputeSimulation {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private StockDataService stockDataService;
	@Ioc
	private StockService stockService;
	@Ioc
	Simulator simulator;

	/****  用于统计   *****/
	int totalRecords = 0;
	int win = 0;
	int lose = 0;
	double allRecordsSum = 0;

	private final eStrategy strategy = eStrategy.One;
	private final int one = 1;

	public void compute(String code) throws SQLException {

		String hcode = stockService.getCode(code, eStockSource.YAHOO);

		List<StockDay> all = null;
		List<StockDay> his = new ArrayList<StockDay>();// 已进行分析过的历史数据
		List<OperRecord> operList = new ArrayList<OperRecord>();

		all = stockDataService.getDay(hcode, null, null);

		if (all.size() < 50) {
			logger.error("数据太少，只有：" + all.size());
			return;
		}

		double price = 0;
		double sum = 0;
		int num = 0;
		int total = 0;
		double remain = 0;
		int symbol = 1; //表示正负

		for (int i = 0; i < all.size() - 1; i++) {
			StockDay someDay = all.get(i);
			StockDay nextDay = all.get(i + 1);

			eStockOper result = simulator.handle(someDay, his);

			if (result == eStockOper.None)
				continue;

			if (operList.size() == 0 && result != eStockOper.Buy) {
				//第一次必须是买
				continue;
			}

			if (result == eStockOper.Buy) {
				price = Double.parseDouble(nextDay.high);
				symbol = 1;
			} else if (result == eStockOper.Sell) {
				price = Double.parseDouble(nextDay.low);
				symbol = -1;
			}

			switch (strategy) {
			case One:
				//每次按结果操作一单位
				num = one;
				break;
			case OneBuyOneSell:
				//严格执行一买一卖，不然放弃
				break;
			case Double:
				//第二次出现相同操作（如连续第二次买），则执行2倍的量
				break;
			}

			sum = symbol * price * num;
			total += symbol * num;
			if (total < 0) {
				total = 0;
				continue;
			}
			remain += -sum; //买是付钱，用负表示
			operList.add(new OperRecord(result, one, price, sum, total, remain));
		}

		//		for (OperRecord record : operList) {
		//			System.out.println(record);
		//		}
		//		for (OperRecord record : operList) {
		//			//只需要卖光后的情况
		//			if (record.total == 0)
		//				logger.info(record.toString());
		//			System.out.println(record);
		//		}
		for (int i = operList.size() - 1; i >= 0; i--) {
			//最后一次卖光时的情况
			OperRecord record = operList.get(i);
			if (record.total == 0) {
				logger.info("code:" + code + "," + record.toString());
				totalRecords++;
				if (record.remain > 0)
					win++;
				else
					lose++;
				allRecordsSum += record.remain;

				break;
			}
		}

	}

	public static void main(String[] args) {

		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = new BeanContext();
		ComputeSimulation simulation = (ComputeSimulation) bc.getBean("computeSimulation");

		try {
			//String code = "002061";
			List<String> codes = new StockService().getAllAvailableCodes(0, null);
			for (String code : codes) {
				simulation.compute(code);
			}
			System.out.println("\n\n************************************************************\n\n");
			System.out.println(String.format("total:%s, win:%s, lose:%s, remain:%s", simulation.totalRecords,
					simulation.win, simulation.lose, simulation.allRecordsSum));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	enum eStrategy {
		One, // 每次按推荐操作一单位
		OneBuyOneSell, // 严格按照：买一单位后必然卖一单位
		Double;// 符合某些条件则买入（或卖出）更多
	}

	class OperRecord {
		protected eStockOper oper; //操作
		protected int num; //数量
		protected double price; //单价
		protected double sum; //总价
		protected int total; //当前拥有数量
		protected double remain; //余额，为了方便知道当前余额

		public OperRecord(eStockOper oper, int num, double price, double sum, int total, double remain) {
			this.oper = oper;
			this.num = num;
			this.total = total;
			this.price = price;
			this.sum = sum;
			this.remain = remain;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer("");
			Class<?> clazz = this.getClass();
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				if (field.getName().contains("this"))
					continue;
				try {
					sb.append(field.getName()).append(":").append(field.get(this)).append(",");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return sb.toString();
		}
	}

	public StockDataService getStockDataService() {
		return stockDataService;
	}

	public void setStockDataService(StockDataService stockDataService) {
		this.stockDataService = stockDataService;
	}

	public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
}
