package common.jdbcutil;

import java.sql.Connection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import common.jdbcutil.h2.H2Helper;
import common.testutil.Mock;

public class H2Test {

	@Before
	public void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		// create table
	}

	@After
	public void after() {
		H2Helper.close(SqlRunner.me().getConn());
	}

	@Test
	public void closeConnection() throws Exception {
		Connection mockCon = Mock.createMock(Connection.class);
		H2Helper.close(mockCon);
		Mock.verify(mockCon).close();
	}

	@Test
	public void testDateOperation() {

	}

}
