package me.net;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.TypeUtil;
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
	private Analyzer handler;

	public void compute() throws SQLException {
		String code = "002061";
		List<String> codes = new ArrayList<String>();
		codes.add(code);
		List<String> hCodes = stockService.getCodes(codes, TypeUtil.StockSource.YAHOO);
		if (hCodes == null || hCodes.size() == 0) {
			logger.error("cannot find the code:" + code);
		}

		String hcode = hCodes.get(0);

		List<StockDay> all = null;
		List<StockDay> his = null;

		all = stockDataService.getDay(hcode, null, null);
		int has = 0;

		int start = 50; // 有部分历史数据才开始计算

		Simulator simulator = new Simulator();

		for (int i = start; i < all.size(); i++) {
			StockDay someDay = all.get(i);

			int result = simulator.handle(someDay, his, has);

			if (result != 0) {
				has = has + result;
				// record();
			}

		}

	}

	public static void main(String[] args) {

	}
}
