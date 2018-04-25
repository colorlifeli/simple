package test.net;

import java.sql.SQLException;

import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.ComputeSimulation;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestComputeSimulation {

	private static ComputeSimulation simulation;

	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDbTest());
		BeanContext bc = BeanContext.me();
		simulation = (ComputeSimulation) bc.getBean("computeSimulation");
	}

	@AfterClass
	public static void after() throws SQLException {
		H2Helper.close(SqlRunner.me().getConn());
	}

	@Test
	public void testCompute() throws SQLException {
		String code = "002061";
		simulation.compute(code);
	}

}
