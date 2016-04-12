package me.web.stock;

import java.util.List;

import me.common.ActionIf;
import me.common.annotation.ActionAnno.Action;
import me.common.annotation.ActionAnno.Pack;
import me.common.annotation.ActionAnno.Result;
import me.common.annotation.IocAnno.Ioc;
import me.common.util.JsonUtil;
import me.net.dao.StockAnalysisDao;
import me.net.model.StockOperSum;
import me.service.stock.AnalysisService;

@Pack(path = "stock")
public class StocksAnalysis extends ActionIf {

	@Ioc
	private StockAnalysisDao stockAnalysisDao;
	@Ioc
	private AnalysisService analysisService;

	private int page;
	private int rows;

	@Action(path = "enter", targets = { @Result(name = "success", value = "stock/stock.jsp") })
	public String enter() {

		return "success";
	}

	@Action(path = "computeAll", targets = { @Result(name = "json", value = "json") })
	public String computeAll() {

		analysisService.computeAll();
		//		try {
		//			Thread.sleep(3000);
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		JsonUtil util = new JsonUtil();
		util.put("msg", "操作成功");

		return util.toString();
	}

	@Action(path = "getOperSumAll", targets = { @Result(name = "json", value = "json") })
	public String getOperSumAll() {

		JsonUtil util = new JsonUtil();
		List<StockOperSum> list = analysisService.getOperSumList(page, rows);

		System.out.println(page + "  " + rows);

		util.put("total", list.size());
		util.put("rows", list);

		return util.toString();
	}
}
