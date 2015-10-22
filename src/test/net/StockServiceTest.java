package net;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import common.jdbcutil.SqlRunner;
import common.jdbcutil.h2.H2Helper;
import net.model.RealTime;

public class StockServiceTest {

	private StockService service = new StockService();

	public StockServiceTest() {
		service.setImpl("impl1");
	}

	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());
	}

	@AfterClass
	public static void after() throws SQLException {
		H2Helper.close(SqlRunner.me().getConn());
	}

	//@Ignore
	@Test
	public void initCode() throws SQLException {
		System.out.println("aaa");
		service.initCode("d:/codes.csv");
	}

	@Ignore
	@Test
	public void getCodes_forsina() {
		try {
			List<String> strs = service.getCodes(10);
			for (String str : strs) {
				System.out.println(str);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void getCodes_forsina2() {
		try {
			List<String> codes = new ArrayList<String>();
			codes.add("600151");
			codes.add("000830");
			List<String> strs = service.getCodes(codes);
			Assert.assertTrue(strs.contains("sh600151"));
			Assert.assertTrue(strs.contains("sz000830"));
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void findRealtime() {
		try {
			List<String> codes = new ArrayList<String>();
			codes.add("600151");
			codes.add("000830");
			List<RealTime> strs = service.findRealtime(codes);
			for (RealTime str : strs) {
				System.out.println(str);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void findRealtimeLast() {
		try {
			List<String> codes = new ArrayList<String>();
			codes.add("600151");
			codes.add("000830");
			List<RealTime> strs = service.findRealtimeLast(codes);
			for (RealTime str : strs) {
				System.out.println(str);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void findRealtimeAllLast() {
		try {
			List<RealTime> strs = service.findRealtimeAllLast();
			for (RealTime str : strs) {
				// System.out.println(str);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}
