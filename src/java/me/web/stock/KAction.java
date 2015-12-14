package me.web.stock;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.ActionIf;
import me.common.annotation.ActionAnno.Action;
import me.common.annotation.ActionAnno.Pack;
import me.common.annotation.ActionAnno.Result;
import me.common.annotation.IocAnno.Ioc;
import me.common.util.TypeUtil;
import me.net.StockDataService;
import me.net.StockService;
import me.net.dayHandler.Analyzer;
import me.net.model.StockDay;

@Pack(path = "stock")
public class KAction extends ActionIf {

	@Ioc
	private StockDataService stockDataService;
	@Ioc
	private StockService stockService;
	@Ioc
	private Analyzer handler;

	@Action(path = "k", targets = { @Result(name = "success", value = "k.jsp") })
	public String show() {

		// test code is 603997 002061
		String code = request.getParameter("code");
		String step = request.getParameter("step");
		try {
			if (code == null)
				code = "002061"; // 默认
			if (step == null)
				step = "0";

			List<String> codes = new ArrayList<String>();
			codes.add(code);
			List<String> hCodes = stockService.getCodes(codes, TypeUtil.StockSource.YAHOO);
			if (hCodes == null || hCodes.size() == 0) {
				logger.error("cannot find the code:" + code);
				return null;
			}
			String hcode = hCodes.get(0);

			List<StockDay> list = null;

			switch (step) {
			case "1":
				list = stockDataService.getDay(hcode, null, null);
				list = handler.include(list);

				list = handler.recognizeType(list);
				break;

			default:
				list = stockDataService.getDay(hcode, null, null);
			}

			showK(list, code);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return "success";
	}

	private void showK(List<StockDay> list, String code) {
		String stockName = "";
		try {
			stockName = stockDataService.getName(code);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		StringBuffer sb_date = new StringBuffer(" ");
		StringBuffer sb_price = new StringBuffer(" ");

		for (StockDay day : list) {
			sb_date.append("\"" + day.date_ + "\"").append(",");

			String price = String.format("[%s,%s,%s,%s],", day.open_, day.close_, day.low, day.high);
			sb_price.append(price);
		}

		request.setAttribute("dates", sb_date.toString().substring(0, sb_date.length() - 1));
		request.setAttribute("prices", sb_price.toString().substring(0, sb_price.length() - 1));
		request.setAttribute("name", stockName);

	}
}
