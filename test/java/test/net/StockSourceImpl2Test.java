package test.net;

import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.NetUtil;
import me.net.StockSourceImpl2;
import me.net.dao.StockSourceDao;
import me.net.supplier.SinaHisSupplier;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StockSourceImpl2Test {

	private StockSourceImpl2 impl = new StockSourceImpl2();
	private SinaHisSupplier sina = new SinaHisSupplier();
	private StockSourceDao sourceDao = new StockSourceDao();

	@Before
	public void init() {
		impl.setStockSourceDao(sourceDao);
		impl.setHistory_supplier(sina);
	}

	@BeforeClass
	public static void before() throws UnknownHostException {
		SqlRunner.me().setConn(H2Helper.connEmbededDbTest());
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
