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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import jdk.nashorn.internal.ir.annotations.Ignore;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.NetUtil;
import me.net.NetType.eStockSource;
import me.net.StockSourceImpl1;
import me.net.dao.StockSourceDao;
import me.net.model.RealTime;
import me.net.supplier.SinaRealSupplier;
import me.net.supplier.YahooHisSupplier;
import test.MyTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StockSourceImpl1Test extends MyTest {

	private StockSourceImpl1 impl = new StockSourceImpl1();
	private SinaRealSupplier sina = new SinaRealSupplier();
	private YahooHisSupplier yahoo = new YahooHisSupplier();
	private StockSourceDao sourceDao = new StockSourceDao();

	@Before
	public void init() {
		impl.setStockSourceDao(sourceDao);
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
		codes.add("300488");
		codes.add("000002");
		codes.add("000001");
		codes.add("603998");
		codes.add("603997");
		codes.add("600004");
		codes.add("600000");

		try {
			impl.getRealTime(codes);

			List<RealTime> strs;
			strs = sourceDao.findRealtimeLast(codes);

			assertEquals(strs.size()%8 , 0);

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

	@Test
	public void checkStocks() {

		try {

			long start = System.currentTimeMillis();
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("hh");
			int hour = Integer.parseInt(format.format(date));
			if(hour == 9 || hour == 10)
			{//新浪开盘时 9:00-9:30 状态不对
				System.out.println("current hour is " + hour + ", cannot check the stock status");
				return;
			}

			impl.checkStocks();

			long end = System.currentTimeMillis();
			System.out.println("time:" + (end - start));

			List<String> list = sourceDao.getAllAvailableCodes(0, eStockSource.SINA);
	
			Assert.assertFalse(list == null || list.isEmpty());
			Assert.assertTrue(list.size() < 2600); // 必然有一些停牌
		} catch (SQLException e) {
			Assert.fail();
			e.printStackTrace();
		}

	}

	//时间较长
	@Ignore
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
			if (sourceDao.isStockTime()) {
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

			impl.dayFinalDo(true);

			int count = H2Helper.getCount("sto_day", "date_", now);
			assertEquals(8, count);

		} catch (SQLException e) {
			fail();
		}

	}

	@Test
	public void getHistory() {

		String sql = "truncate table sto_day_tmp";
		try {
			SqlRunner.me().execute(sql);
		} catch (SQLException e) {
			fail();
		}

		List<String> codes = new ArrayList<String>();
		codes.add("300489");
		codes.add("300488");
		codes.add("000002");
		codes.add("000001");
		codes.add("603998");
		codes.add("603997");
		codes.add("600004");
		codes.add("600000");

		long start = System.currentTimeMillis();

		impl.getHistory(codes, null, null);

		long end = System.currentTimeMillis();
		System.out.println("use time:" + (end - start));
	}

	//时间较长
	//@Ignore
	@Test
	public void getHistoryAll() {
		long start = System.currentTimeMillis();

		impl.getHistoryAll(null, null);

		long end = System.currentTimeMillis();
		System.out.println("use time:" + (end - start));
	}

}
