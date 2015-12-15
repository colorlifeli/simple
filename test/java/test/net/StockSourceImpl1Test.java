package test.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.NetUtil;
import me.net.NetType.eStockSource;
import me.net.SinaSourceService;
import me.net.StockService;
import me.net.StockSourceImpl1;
import me.net.YahooSourceService;
import me.net.model.RealTime;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StockSourceImpl1Test {

	private StockSourceImpl1 impl = new StockSourceImpl1();
	private SinaSourceService sina = new SinaSourceService();
	private YahooSourceService yahoo = new YahooSourceService();
	private StockService service = new StockService();

	@Before
	public void init() {
		impl.setStockService(service);
		impl.setRealtime_supplier(sina);
		impl.setHistory_supplier(yahoo);
	}

	@BeforeClass
	public static void before() throws UnknownHostException {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());
		String ip = InetAddress.getLocalHost().getHostAddress();
		System.out.println(ip);
		if ("10.132.8.78".equals(ip)) {
			NetUtil.me().setProxy();
		}
	}

	@AfterClass
	public static void after() throws SQLException {
		H2Helper.close(SqlRunner.me().getConn());
	}

	@Test
	public void getRealTime() {

		List<String> codes = new ArrayList<String>();
		codes.add("300489");
		codes.add("603997");

		try {
			impl.getRealTime(codes);

			List<RealTime> strs;
			strs = service.findRealtimeLast(codes);

			assertEquals(2, strs.size());

			// date没有记录，默认都是今天
			// Date date = new Date();
			// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			// String now = format.format(date);
			// for (RealTime str : strs) {
			// assertEquals(now, str.date);
			// }
		} catch (SQLException e) {
			fail();
		}
	}

	// @Ignore
	@Test
	public void getRealTimeAll() {

		long start = System.currentTimeMillis();

		impl.getRealTimeAll();

		long end = System.currentTimeMillis();
		System.out.println("time:" + (end - start));
	}

	// 无法测试
	@Ignore
	@Test
	public void checkStocks() {

		try {

			long start = System.currentTimeMillis();

			impl.checkStocks();

			long end = System.currentTimeMillis();
			System.out.println("time:" + (end - start));

			List<String> list = service.getAllAvailableCodes(0, eStockSource.SINA);
			Assert.assertTrue(list.size() < 2600); // 必然有一些停牌
		} catch (SQLException e) {
			Assert.fail();
			e.printStackTrace();
		}

	}

	@Test(timeout = 65 * 1000)
	public void getRealTimeAllByInterval() {

		try {
			impl.getRealTimeAll(60);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	// this test must be before getRealTimeAll,加上一个前缀让其先执行
	@Test
	public void a_isSameAsPrevious() {
		try {
			boolean result = impl.isSameAsPrevious();
			if (service.isStockTime()) {
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
		try {
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String now = format.format(date);
			String sql = "delete from sto_day where date_='" + now + "'";
			SqlRunner.me().execute(sql);

			impl.dayFinalDo();

			int count = H2Helper.getCount("sto_day", "date_", now);
			assertEquals(10, count);

		} catch (SQLException e) {
			fail();
		}

	}

	@Test
	public void getHistory() {

		List<String> codes = new ArrayList<String>();
		codes.add("603997");

		long start = System.currentTimeMillis();

		impl.getHistory(codes, null, null);

		long end = System.currentTimeMillis();
		System.out.println("use time:" + (end - start));
	}

	@Test
	public void getHistoryAll() {
		long start = System.currentTimeMillis();

		impl.getHistoryAll(null, null);

		long end = System.currentTimeMillis();
		System.out.println("use time:" + (end - start));
	}

}
