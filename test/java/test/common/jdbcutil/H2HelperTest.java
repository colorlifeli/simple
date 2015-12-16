package test.common.jdbcutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class H2HelperTest {

	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDbTest());
	}

	@AfterClass
	public static void after() throws SQLException {
		H2Helper.close(SqlRunner.me().getConn());
	}

	@Test
	public void getCount() {

		SqlRunner sqlrunner = SqlRunner.me();
		String sql = "";
		Object[] params;

		// 创建表
		sql = "create table test(id varchar(10) primary key, name varchar(10), age int)";
		try {
			SqlRunner.me().execute(sql);

			sql = "insert into test values(?,?,?)";
			params = new Object[] { "1", "xiaoming", 10 };
			// 不关心 Insert 返回的resulteSet（auto-increase key)，因此直接使用 execute 函数
			sqlrunner.execute(sql, params);
			params = new Object[] { "2", "小明", 20 };
			sqlrunner.execute(sql, params);

			int count = H2Helper.getCount("test");
			assertEquals(2, count);

			count = H2Helper.getCount("test", "name", "xiaoming");
			assertEquals(1, count);

			count = H2Helper.getCount("test", "name", "xiaoming", "age", 10);
			assertEquals(1, count);

			count = H2Helper.getCount("test", "name", "小明", "age", 10);
			assertEquals(0, count);

			sql = "truncate table test";
			sqlrunner.execute(sql);
			sql = "drop table test";
			sqlrunner.execute(sql);

		} catch (SQLException e) {
			fail();
		}
	}

}
