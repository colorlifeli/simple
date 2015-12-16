package test.net;

import java.sql.SQLException;
import java.util.List;

import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.StockDataService;
import me.net.model.StockDay;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class StockDataServiceTest {

	private StockDataService service = new StockDataService();

	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDbTest());
	}

	@AfterClass
	public static void after() throws SQLException {
		H2Helper.close(SqlRunner.me().getConn());
	}

	@Test
	public void getDay() {
		String code = "300072.sz";
		List<StockDay> list;
		try {
			list = service.getDay(code, null, null);

			Assert.assertEquals(948, list.size());
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}

	}

	@Test
	public void getName() {
		String code = "300072";
		String name;
		try {
			name = service.getName(code);
			Assert.assertEquals("三聚环保", name);

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}
