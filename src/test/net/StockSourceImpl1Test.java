package net;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import common.jdbcutil.SqlRunner;
import common.jdbcutil.h2.H2Helper;
import common.util.NetUtil;

public class StockSourceImpl1Test {

	private StockSourceImpl1 impl = new StockSourceImpl1();
	private SinaSourceService sina = new SinaSourceService();
	private StockService stockService = new StockService();

	@Before
	public void init() {
		impl.setSina(sina);
		impl.setStockService(stockService);
	}

	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());
		NetUtil.me().setProxy();
	}

	@AfterClass
	public static void after() throws SQLException {
		H2Helper.close(SqlRunner.me().getConn());
	}

	@Test
	public void getRealTime() {
		NetUtil.me().setProxy();

		List<String> codes = new ArrayList<String>();
		codes.add("600151");
		codes.add("000830");

		impl.getRealTime(codes);
	}

	// @Ignore
	@Test
	public void getRealTimeAll() {
		NetUtil.me().setProxy();

		long start = System.currentTimeMillis();

		impl.getRealTimeAll();

		long end = System.currentTimeMillis();
		System.out.println("time:" + (end - start));
	}

	// @Ignore
	@Test
	public void checkStocks() {
		NetUtil.me().setProxy();

		try {

			long start = System.currentTimeMillis();

			impl.checkStocks();

			long end = System.currentTimeMillis();
			System.out.println("time:" + (end - start));

			List<String> list = stockService.getCodes(0);
			Assert.assertTrue(list.size() < 2600); // 必然有一些停牌
		} catch (SQLException e) {
			Assert.fail();
			e.printStackTrace();
		}

	}

	@Test
	public void getRealTimeAllByInterval() {

		try {

			impl.getRealTimeAll(60);

		} catch (Exception e) {
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test
	public void isSameAsPrevious() {
		try {
			boolean result = impl.isSameAsPrevious();
			if (stockService.isStockTime()) {
				Assert.assertFalse(result);
			} else {
				Assert.assertTrue(result);
			}
		} catch (SQLException e) {
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test
	public void dayFinalDo() {
		impl.dayFinalDo();
	}

	@Test
	public void getHistory() {
		long start = System.currentTimeMillis();

		impl.getHistory(null, null, null);

		long end = System.currentTimeMillis();
		System.out.println("time:" + (end - start));
	}

}
