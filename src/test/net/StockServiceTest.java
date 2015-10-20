package net;

import java.sql.SQLException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import common.jdbcutil.SqlRunner;
import common.jdbcutil.h2.H2Helper;

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

	@Ignore
	@Test
	public void initCode() throws SQLException {
		System.out.println("aaa");
		service.initCode("d:/codes.csv");
	}

	@Test
	public void getCodes_forsina() {
		try {
			List<String> strs = service.getCodes_forsina(10);
			for (String str : strs) {
				System.out.println(str);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
