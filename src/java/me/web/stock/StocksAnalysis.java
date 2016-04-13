package me.web.stock;

import java.util.Collections;
import java.util.List;

import me.common.ActionIf;
import me.common.SimpleException;
import me.common.annotation.ActionAnno.Action;
import me.common.annotation.ActionAnno.Pack;
import me.common.annotation.ActionAnno.Result;
import me.common.annotation.IocAnno.Ioc;
import me.common.util.JsonUtil;
import me.net.dao.StockAnalysisDao;
import me.net.model.OperRecord;
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
	private boolean isFromDB;
	private String code;

	@Action(path = "enter", targets = { @Result(name = "success", value = "stock/stock.jsp") })
	public String enter() {

		return "success";
	}

	@Action(path = "computeAll", targets = { @Result(name = "json", value = "json") })
	public String computeAll() {

		analysisService.computeAll();

		JsonUtil util = new JsonUtil();
		util.put("msg", "操作成功");

		return util.toString();
	}

	@Action(path = "writeToDB", targets = { @Result(name = "json", value = "json") })
	public String writeToDB() {

		String msg = "操作成功";
		try {
			analysisService.saveToDb();
		} catch (SimpleException se) {
			msg = "操作失败：" + se.getMsgDes();
		}

		JsonUtil util = new JsonUtil();
		util.put("msg", msg);

		return util.toString();
	}

	@Action(path = "getOperSumAll", targets = { @Result(name = "json", value = "json") })
	public String getOperSumAll() {

		JsonUtil util = new JsonUtil();
		List<StockOperSum> list;
		try {
			list = analysisService.getOperSumList(page, rows);

			if (isFromDB && list.size() == 0) {
				analysisService.getOperSumListDB();
				list = analysisService.getOperSumList(page, rows);
			}
		} catch (Exception e) {
			logger.error("getOperSumAll 获取操作汇总数据失败！");
			list = Collections.emptyList();
		}

		util.put("total", list.size());
		util.put("rows", list);

		return util.toString();
	}

	@Action(path = "getOperList", targets = { @Result(name = "json", value = "json") })
	public String getOperList() {

		JsonUtil util = new JsonUtil();
		List<OperRecord> list = null;
		try {
			if (isFromDB)
				list = analysisService.getOperListDB(code);
			else
				list = analysisService.getOperList(code);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("getOperList 获取操作数据失败！code:" + code);
		}

		if (list == null)
			list = Collections.emptyList();

		util.put("total", list.size());
		util.put("rows", list);

		return util.toString();
	}
}
