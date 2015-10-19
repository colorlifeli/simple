package net;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import common.util.NetUtil;

public class StockSourceImpl1Test {

	private StockSourceImpl1 impl = new StockSourceImpl1();
	private SinaSourceService sina = new SinaSourceService();
	private StockService stockService = new StockService();

	// @BeforeClass
	// public static void before() {
	// SqlRunner.me().setConn(H2Helper.connEmbededDb());
	// }
	//
	// @AfterClass
	// public static void after() throws SQLException {
	// H2Helper.close(SqlRunner.me().getConn());
	// }

	@Test
	public void getRealTime() {
		NetUtil.me().setProxy();

		String str = "sh600151,sz000830";
		List<String> list = new ArrayList<String>();
		String[] codes = str.split(",");
		for (String code : codes) {
			// code = code.substring(2);
			list.add(code);
		}

		impl.setSina(sina);
		impl.setStockService(stockService);
		impl.getRealTime(list);
	}

}
