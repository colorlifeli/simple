package me.web.stock;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.common.ActionIf;
import me.common.SimpleException;
import me.common.annotation.ActionAnno.Action;
import me.common.annotation.ActionAnno.Pack;
import me.common.annotation.ActionAnno.Result;
import me.common.annotation.IocAnno.Ioc;
import me.common.jdbcutil.Page;
import me.common.util.JsonUtil;
import me.common.util.TypeUtil;
import me.net.dao.StockAnalysisDao;
import me.net.model.OperRecord;
import me.net.model.StockOperSum;
import me.service.stock.AnalysisService;

@Pack(path = "stock")
public class StocksAnalysisAction extends ActionIf {

	@Ioc
	private StockAnalysisDao stockAnalysisDao;
	@Ioc
	private AnalysisService analysisService;

	// jquery easyui datagrid 相关
	private int page;
	private int rows;
	private String sort;
	private String order;

	private boolean isFromDB;
	private String code;
	private String priceStrategy;
	private String startDate;
	
	private StockOperSum operSum;

	@Action(path = "enter", targets = { @Result(name = "success", value = "stock/stock.jsp") })
	public String enter() {

		return "success";
	}

	@Action(path = "computeAll", targets = { @Result(name = "json", value = "json") })
	public String computeAll() {

		if (!TypeUtil.isEmpty(priceStrategy))
			analysisService.c_priceStrategy = Integer.parseInt(priceStrategy);
		if (!TypeUtil.isEmpty(startDate))
			analysisService.c_startDate = startDate;

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

		Map<String, String> voMap = new HashMap<String, String>();
		this.getVoMapFromJsp(StockOperSum.class, voMap);
		
		//由于 int 等数字类型，在不赋值的情况下是 0，这会导致生成错误的条件，所以这么用也不好
		//Map<String, String> voMap = Util.getMapFromObject(operSum);

		Page p = new Page();
		try {
			if (isFromDB) {
				analysisService.getOperSumListDB(voMap);
				p = analysisService.getOperSumList(page, rows, sort, order);
			} else {
				p = analysisService.getOperSumList(page, rows, sort, order);
			}
		} catch (Exception e) {
			logger.error("getOperSumAll 获取操作汇总数据失败！");
			e.printStackTrace();
			p.list = Collections.emptyList();
		}

		util.put("total", p.total);
		util.put("rows", p.list);

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

	@Action(path = "summary", targets = { @Result(name = "json", value = "json") })
	public String summary() {

		String msg = "";
		try {
			msg = analysisService.summary(isFromDB);
		} catch (Exception e) {
			e.printStackTrace();
			msg = "操作失败";
		}

		JsonUtil util = new JsonUtil();
		util.put("msg", msg);

		return util.toString();
	}

	@Action(path = "sellAll", targets = { @Result(name = "json", value = "json") })
	public String sellAll() {

		String msg = "完成";
		try {
			analysisService.sellSomeday();
		} catch (Exception e) {
			e.printStackTrace();
			msg = "操作失败";
		}

		JsonUtil util = new JsonUtil();
		util.put("msg", msg);

		return util.toString();
	}
}
