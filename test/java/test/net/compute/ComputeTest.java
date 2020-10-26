package test.net.compute;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.compute.Compute;

public class ComputeTest {
	
	private static Logger logger = LoggerFactory.getLogger(ComputeTest.class);
	private static long startTime = 0;
	private static long endTime = 0;
	private static Compute compute;
	private static Compute compute2;
	private static Compute compute3;
	
	@BeforeClass
	public static void before() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());
		startTime = System.currentTimeMillis();
		compute = (Compute) BeanContext.me().getBean("compute1");
		compute2 = (Compute) BeanContext.me().getBean("compute2");
		compute3 = (Compute) BeanContext.me().getBean("compute3");
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
		logger.info("enter computeAll2");
		try {
			compute2.setStartDate("2015-04-01");
			compute2.setEndDate("2016-11-11");
			//compute2.compute("603116");
			//compute.compute("002570");
			
			compute2.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void computeAll2_2() {
		try {
			compute2.setStartDate("2015-04-01");
			compute2.setEndDate("2016-11-11");
			compute2.compute("603729");
			//compute.compute("002570");
			
			//compute2.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void computeAll3() {
		logger.info("enter computeAll3");
		try {
			compute3.setStartDate("2015-04-01");
			compute3.setEndDate("2017-01-06");
			//compute3.compute("603116");
			//compute3.compute("002570");
			
			compute3.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void computeAll3_2() {
		try {
			compute3.setStartDate("2015-04-01");
			compute3.setEndDate("2016-11-11");
			compute3.compute("002415");
			//compute.compute("002570");
			
			//compute2.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
