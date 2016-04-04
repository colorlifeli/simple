package me.web.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.common.ActionIf;
import me.common.annotation.ActionAnno.Action;
import me.common.annotation.ActionAnno.Pack;
import me.common.annotation.ActionAnno.Result;
import me.common.annotation.IocAnno.Ioc;
import me.common.util.JsonUtil;
import me.net.dao.StockAnalysisDao;
import me.net.dayHandler.Analyzer;

@Pack(path = "stock")
public class StocksAnalysis extends ActionIf {

	@Ioc
	private StockAnalysisDao stockAnalysisDao;
	@Ioc
	private Analyzer analyzer;

	@Action(path = "enter", targets = { @Result(name = "success", value = "stock/stock.jsp") })
	public String enter() {

		return "success";
	}

	@Action(path = "computeAll", targets = { @Result(name = "json", value = "json") })
	public String computeAll() {

		JsonUtil util = new JsonUtil();
		int total = 1;
		List<Object> list = new ArrayList<Object>();

		util.put("total", total);
		util.put("rows", list);

		try {
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(util.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "json";
	}
}
