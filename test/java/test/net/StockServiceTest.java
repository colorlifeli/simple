package test.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.TypeUtil;
import me.net.StockService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * 为了按顺序执行 test case，方法名前加了前缀
 * @author James
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StockServiceTest {

	private StockService service = new StockService();

	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());
	}

	@AfterClass
	public static void after() throws SQLException {
		H2Helper.close(SqlRunner.me().getConn());
	}

	@Test
	public void a1_initCode() throws SQLException {
		System.out.println("aaa");
		String file = this.getClass().getClassLoader().getResource("codes_test.csv").getPath().substring(1);
		// 或
		// this.getClass().getResource("/codes_test.csv");
		// file = "d:/codes_test.csv";
		service.initCode(file);

		List<String> codes = service.getCodes(0, null);
		assertEquals(10, codes.size());

		assertTrue(codes.contains("000001"));
		assertTrue(codes.contains("399001"));
		assertTrue(codes.contains("300489"));
		assertTrue(codes.contains("300488"));
		assertTrue(codes.contains("000002"));
		assertTrue(codes.contains("000001"));
		assertTrue(codes.contains("603998"));
		assertTrue(codes.contains("603997"));
		assertTrue(codes.contains("600004"));
		assertTrue(codes.contains("600000"));

		// all code:
		// 000001
		// 399001
		// 300489
		// 300488
		// 000002
		// 000001
		// 603998
		// 603997
		// 600004
		// 600000
	}

	@Test
	public void getCodes_forsina() {
		try {
			List<String> codes = service.getAllAvailableCodes(10, TypeUtil.StockSource.SINA);

			assertEquals(10, codes.size());

			assertTrue(codes.contains("s_sh000001"));
			assertTrue(codes.contains("s_sz399001"));
			assertTrue(codes.contains("sz300489"));
			assertTrue(codes.contains("sz300488"));
			assertTrue(codes.contains("sz000002"));
			assertTrue(codes.contains("sz000001"));
			assertTrue(codes.contains("sh603998"));
			assertTrue(codes.contains("sh603997"));
			assertTrue(codes.contains("sh600004"));
			assertTrue(codes.contains("sh600000"));

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void getCodes_foryahoo() {
		try {
			List<String> codes = new ArrayList<String>();
			codes.add("300489");
			codes.add("600004");
			List<String> strs = service.getCodes(codes, TypeUtil.StockSource.YAHOO);
			Assert.assertTrue(strs.contains("300489.sz"));
			Assert.assertTrue(strs.contains("600004.ss"));
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	// 由 StockSourceImpl1Test.getRealTime 进行测试
	// @Test
	// public void findRealtime() {
	// try {
	// List<String> codes = new ArrayList<String>();
	// codes.add("300489");
	// codes.add("600004");
	// List<RealTime> strs = service.findRealtime(codes);
	//
	// assertEquals(2, strs.size());
	//
	// Date date = new Date();
	// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	// String now = format.format(date);
	// for (RealTime str : strs) {
	// assertEquals(now, str.date);
	// }
	//
	// } catch (SQLException e) {
	// e.printStackTrace();
	// Assert.fail();
	// }
	// }

	// 由 StockSourceImpl1Test.getRealTime 进行测试
	// @Test
	// public void findRealtimeLast() {
	// try {
	// List<String> codes = new ArrayList<String>();
	// codes.add("300489");
	// codes.add("600004");
	// List<RealTime> strs = service.findRealtimeLast(codes);
	//
	// assertEquals(2, strs.size());
	//
	// Date date = new Date();
	// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	// String now = format.format(date);
	// for (RealTime str : strs) {
	// assertEquals(now, str.date);
	// }
	//
	// } catch (SQLException e) {
	// e.printStackTrace();
	// Assert.fail();
	// }
	// }

	// @Test
	// public void findRealtimeAllLast() {
	// try {
	// List<RealTime> strs = service.findRealtimeAllLast();
	// assertEquals(10, strs.size());
	//
	// } catch (SQLException e) {
	// e.printStackTrace();
	// Assert.fail();
	// }
	// }

	@Test
	public void isStockTime() {
		Assert.assertTrue(service.isStockTime());
	}

}
