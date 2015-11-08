package web.stock;

import java.sql.SQLException;
import java.util.List;

import common.ActionIf;
import common.annotation.ActionAnno.Action;
import common.annotation.ActionAnno.Pack;
import common.annotation.ActionAnno.Result;
import common.annotation.IocAnno.Ioc;
import net.StockDataService;
import net.model.StockDay;

@Pack(path = "hello")
public class KAction extends ActionIf {

	@Ioc
	private StockDataService stockDataService;

	@Action(path = "show", targets = { @Result(name = "success", value = "k.jsp") })
	public String show() {
		String code = (String) request.getAttribute("code");
		try {
			List<StockDay> list = stockDataService.getDay(code);
			String stockName = stockDataService.getName(code);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return "success";
	}

}
