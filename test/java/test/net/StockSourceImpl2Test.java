package test.net;

import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import me.common.jdbcutil.ArrayListHandler;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.NetUtil;
import me.net.StockSourceImpl2;
import me.net.dao.StockAnalysisDao;
import me.net.dao.StockSourceDao;
import me.net.supplier.SinaHisSupplier;
import test.MyTest;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StockSourceImpl2Test extends MyTest {

	private StockSourceImpl2 impl = new StockSourceImpl2();
	private SinaHisSupplier sina = new SinaHisSupplier();
	private StockSourceDao sourceDao = new StockSourceDao();
	private StockAnalysisDao stockAnalysisDao = new StockAnalysisDao();

	@Before
	public void init() {
		impl.setStockSourceDao(sourceDao);
		impl.setStockAnalysisDao(stockAnalysisDao);
		impl.setHistory_supplier(sina);
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
	public void getHistory() {

		String sql = "truncate table sto_day_tmp2";
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
	
	@Test
	public void getHistory_increment() { //增量
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.WEEK_OF_MONTH, -1); //上星期.假设最近一个星期应该有交易
		String strdate = format.format(c.getTime());
		System.out.println(strdate);

		String code = "300489";
		
		String sql = "delete from sto_day_tmp2 where code='" + code + "' and date_>'" + strdate + "'";
		try {
			SqlRunner.me().execute(sql);
		} catch (SQLException e) {
			fail();
		}

		List<String> codes = new ArrayList<String>();
		codes.add(code);

		long start = System.currentTimeMillis();

		impl.getHistory(codes, "20161022", null);
		
		sql = "select * from sto_day_tmp2 where code='" + code + "' and date_>'" + strdate + "'";
		try {
			List<Object[]> result = SqlRunner.me().query(sql, new ArrayListHandler(), (Object [])null);

			Assert.assertFalse(result == null || result.size() == 0);
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
		

		long end = System.currentTimeMillis();
		System.out.println("use time:" + (end - start));
	}

	@Test
	public void getHistoryAll() {
		
		String sql = "truncate table sto_day_tmp2";
		try {
			SqlRunner.me().execute(sql);
		} catch (SQLException e) {
			fail();
		}
		
		long start = System.currentTimeMillis();

		impl.getHistoryAll(null, null);

		long end = System.currentTimeMillis();
		System.out.println("use time:" + (end - start));
	}

}
