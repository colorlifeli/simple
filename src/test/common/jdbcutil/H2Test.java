package common.jdbcutil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import common.jdbcutil.h2.H2Helper;
import common.testutil.Mock;

public class H2Test {

	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		// create table
	}

	@AfterClass
	public static void after() throws SQLException {
		String sql = "truncate table test";
		SqlRunner.me().execute(sql);
		sql = "drop table test";
		SqlRunner.me().execute(sql);

		H2Helper.close(SqlRunner.me().getConn());
	}

	@Test
	public void closeConnection() throws Exception {
		Connection mockCon = Mock.createMock(Connection.class);
		H2Helper.close(mockCon);
		Mock.verify(mockCon).close();
	}

	@Test
	public void testDateOperation() throws SQLException {

		String sql = "";
		Object[] params;
		SqlRunner sqlrunner = SqlRunner.me();

		// 创建表
		sql = "create table test(id varchar(10) primary key, name varchar(10), age int)";
		SqlRunner.me().execute(sql);

		sql = "insert into test values(?,?,?)";
		params = new Object[] { "1", "xiaoming", 10 };
		// 不关心 Insert 返回的resulteSet（auto-increase key)，因此直接使用 execute 函数
		SqlRunner.me().execute(sql, params);
		params = new Object[] { "2", "小明", 20 };
		SqlRunner.me().execute(sql, params);

		sql = "select * from test";
		Object[] result = SqlRunner.me().query(sql, new ArrayHandler());
		Assert.assertEquals("xiaoming", result[1]);

		List<Object[]> results = sqlrunner.query(sql, new ArrayListHandler());
		Assert.assertEquals(2, results.size());
		System.out.println(results.get(1)[1]);
		Assert.assertEquals("小明", results.get(1)[1]);

		TestBean resultB = sqlrunner.query(sql, new BeanHandler<TestBean>(TestBean.class));
		Assert.assertEquals(10, resultB.getAge());

		List<TestBean> resultBs = sqlrunner.query(sql, new BeanListHandler<TestBean>(TestBean.class));
		Assert.assertEquals(2, resultBs.size());
		System.out.println(resultBs.get(1));
		Assert.assertEquals(resultBs.get(1).getName(), "小明");

		// 测试表是否存在
		Assert.assertTrue(sqlrunner.isTableExists("test"));

	}

	public static class TestBean {
		private String id;
		private String name;
		private int age;

		public TestBean() {
		}

		@Override
		public String toString() {
			return "(" + id + "," + name + "," + age + ")";
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

	}
}
