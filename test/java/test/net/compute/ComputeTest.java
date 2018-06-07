package test.net.compute;

import java.sql.SQLException;

import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.compute.Compute;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComputeTest {
	
	private static Logger logger = LoggerFactory.getLogger(ComputeTest.class);
	private static long startTime = 0;
	private static long endTime = 0;
	private static Compute compute;
	private static Compute compute2;
	
	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());
		startTime = System.currentTimeMillis();
		compute = (Compute) BeanContext.me().getBean("compute1");
		compute2 = (Compute) BeanContext.me().getBean("compute2");
	}

	@AfterClass
	public static void after() throws SQLException {
		H2Helper.close(SqlRunner.me().getConn());
		endTime = System.currentTimeMillis();
		logger.info("运行时间： "+(endTime-startTime)+" ms");
	}
	
	@Test
	public void computeAll1() {
		try {
			compute.setStartDate("2015-04-01");
			compute.setEndDate("2017-01-01");
			//compute.compute("603116");
			//compute.compute("002570");
			
			compute.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void computeSingle1() {
		try {
			compute.setStartDate("2015-04-01");
			compute.setEndDate("2017-01-01");
			//compute.compute("603116");
			//compute.compute("600881");
			compute.compute("600176");
			
			compute.summaryGain();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void computeAll2() {
		try {
			compute2.setStartDate("2015-04-01");
			compute2.setEndDate("2017-01-01");
			//compute.compute("603116");
			//compute.compute("002570");
			
			compute2.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void computeAll2_2() {
		try {
			compute2.setStartDate("2013-04-01");
			compute2.setEndDate("2015-04-01");
			//compute.compute("603116");
			//compute.compute("002570");
			
			compute2.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
