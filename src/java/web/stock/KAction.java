package web.stock;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.ActionIf;
import common.annotation.ActionAnno.Action;
import common.annotation.ActionAnno.Pack;
import common.annotation.ActionAnno.Result;
import common.annotation.IocAnno.Ioc;
import net.StockDataService;
import net.StockService;
import net.model.StockDay;

@Pack(path = "stock")
public class KAction extends ActionIf {

	@Ioc
	private StockDataService stockDataService;
	@Ioc
	private StockService stockService;

	@Action(path = "k", targets = { @Result(name = "success", value = "k.jsp") })
	public String show() {
		String code = (String) request.getParameter("code");
		try {
			List<String> codes = new ArrayList<String>();
			codes.add(code);
			List<String> hCodes = stockService.getCodes(codes, "h");
			if (hCodes == null || hCodes.size() == 0) {
				logger.error("cannot find the code:" + code);
				return null;
			}
			String hcode = hCodes.get(0);
			List<StockDay> list = stockDataService.getDay(hcode, null, null);
			String stockName = stockDataService.getName(hcode);

			StringBuffer sb_date = new StringBuffer("");
			StringBuffer sb_price = new StringBuffer("");

			for (StockDay day : list) {
				sb_date.append(day.date_).append(",");

				String price = String.format("[%s,%s,%s,%s],", day.open_, day.close_, day.low, day.high);
				sb_price.append(price);
			}

			request.setAttribute("dates", sb_date.toString().substring(0, sb_date.length() - 1));
			request.setAttribute("prices", sb_price.toString().substring(0, sb_price.length() - 1));
			request.setAttribute("name", stockName);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return "success";
	}
}
