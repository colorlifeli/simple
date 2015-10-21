package net;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import common.jdbcutil.SqlRunner;
import common.jdbcutil.h2.H2Helper;
import common.util.NetUtil;

public class StockSourceImpl1Test {

	private StockSourceImpl1 impl = new StockSourceImpl1();
	private SinaSourceService sina = new SinaSourceService();
	private StockService stockService = new StockService();

	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());
	}

	@AfterClass
	public static void after() throws SQLException {
		H2Helper.close(SqlRunner.me().getConn());
	}

	@Ignore
	@Test
	public void getRealTime() {
		NetUtil.me().setProxy();

		List<String> codes = new ArrayList<String>();
		codes.add("600151");
		codes.add("000830");

		impl.setSina(sina);
		impl.setStockService(stockService);
		impl.getRealTime(codes);
	}

	@Test
	public void getRealTimeAll() {
		NetUtil.me().setProxy();

		impl.setSina(sina);
		impl.setStockService(stockService);
		long start = System.currentTimeMillis();

		impl.getRealTimeAll();

		long end = System.currentTimeMillis();
		System.out.println("time:" + (end - start));
	}

}
