package me.net;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.annotation.IocAnno.Ioc;
import me.net.NetType.eStockOper;
import me.net.NetType.eStockSource;
import me.net.dayHandler.Analyzer;
import me.net.dayHandler.Simulator;
import me.net.model.StockDay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComputeSimulation {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private StockDataService stockDataService;
	@Ioc
	private StockService stockService;
	@Ioc
	private Analyzer analyzer;

	private final eStrategy strategy = eStrategy.One;
	private final int one = 1;

	public void compute() throws SQLException {
		String code = "002061";

		String hcode = stockService.getCode(code, eStockSource.YAHOO);

		List<StockDay> all = null;
		List<StockDay> his = new ArrayList<StockDay>();// 已进行分析过的历史数据
		List<OperRecord> operList = new ArrayList<OperRecord>();

		all = stockDataService.getDay(hcode, null, null);

		int has = 0;

		// int start = 50; // 有部分历史数据才开始计算

		Simulator simulator = new Simulator();

		for (int i = 0; i < all.size(); i++) {
			StockDay someDay = all.get(i);

			eStockOper result = simulator.handle(someDay, his);

			if (result == eStockOper.None)
				continue;

			if (operList.size() == 0) {
				has = one;
				operList.add(new OperRecord(result, one, has));
			}

			switch (strategy) {
			case One:
				break;
			case OneBuyOneSell:
				break;
			case Double:
				break;
			}

		}

	}

	public static void main(String[] args) {

	}

	enum eStrategy {
		One, // 每次按推荐操作一单位
		OneBuyOneSell, // 严格按照：买一单位后必然卖一单位
		Double;// 符合某些条件则买入（或卖出）更多
	}

	class OperRecord {
		protected eStockOper oper;
		protected int num;
		protected int total;

		public OperRecord(eStockOper oper, int num, int total) {
			this.oper = oper;
			this.num = num;
			this.total = total;
		}
	}
}
