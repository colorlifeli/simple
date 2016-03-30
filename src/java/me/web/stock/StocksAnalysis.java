package me.web.stock;

import me.common.annotation.ActionAnno.Action;
import me.common.annotation.ActionAnno.Pack;
import me.common.annotation.ActionAnno.Result;
import me.common.annotation.IocAnno.Ioc;
import me.net.dao.StockAnalysisDao;
import me.net.dayHandler.Analyzer;

@Pack(path = "stock")
public class StocksAnalysis {

	@Ioc
	private StockAnalysisDao stockAnalysisDao;
	@Ioc
	private Analyzer analyzer;

	@Action(path = "enter", targets = { @Result(name = "success", value = "stock/stock.jsp") })
	public String enter() {

		return "success";
	}
}
