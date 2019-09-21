package test.service.stock;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.service.stock.AnalysisService;
import test.MyTest;

public class AnalysisServiceTest extends MyTest {

	AnalysisService service = new AnalysisService();

	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());
	}

	@AfterClass
	public static void after() throws SQLException {
		H2Helper.close(SqlRunner.me().getConn());
	}

	@Test
	public void compute() {

	}
}
