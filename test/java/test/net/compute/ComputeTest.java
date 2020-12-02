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
import me.net.NetType.eStrategy;
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
			//set config
			compute3.setStartDate("2018-04-01").setStartBuyDate("2018-04-01");
			compute3.setEndDate("2020-10-06");
			compute3.setPrintOper(false).setStrategy(eStrategy.Ratio).setPractice(false);
			compute3.setBuyNumRatio(0.7).setSellRatio_win(1.5).setSellRatio_lose(0.6);
			compute3.setOperationFunction("operation5");
			//end --set config
			
			compute3.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void computeOne3() {
		try {
			compute3.setStartDate("2016-04-01").setStartBuyDate("2017-04-01");
			//compute3.setEndDate("2019-11-11");
			compute3.setPrintOper(true).setStrategy(eStrategy.One).setPractice(false);
			compute3.setOperationFunction("operation4");
			
			compute3.compute("000413");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//total:3864, win stocks:1032, lose:420, last day sell stocks:668, remain:4441166, investment:8293532|2020-06-22, rate:0.54, buys:2327, sells:1563, win times:1087, lose times:476
	//有个问题，要startDate也在 2018年才有这么好的结果。但正常来说，为了有延续性，startDate应该是一个较早一点的时间，如2016,但这时收益率只有0.33
	@Test
	public void computeAll3_1() {
		logger.info("enter computeAll3");
		try {
			//set config
			compute3.setStartDate("2018-04-01").setStartBuyDate("2018-04-01");
			compute3.setEndDate("2020-10-06");
			compute3.setPrintOper(false).setStrategy(eStrategy.Ratio).setPractice(false);
			compute3.setBuyNumRatio(0.7).setSellRatio_win(1.5).setSellRatio_lose(0.6);
			compute3.setOperationFunction("operation5");
			//end --set config
			
			compute3.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//波动很大，不同 startDate,startbuydate相差较多。均为2017-04-01时，有较好的结果，达到0.57收益率
	@Test
	public void computeAll3_2() {
		logger.info("enter computeAll3");
		try {
			//set config
			compute3.setStartDate("2017-04-01").setStartBuyDate("2017-04-01");
			compute3.setEndDate("2020-10-06");
			compute3.setPrintOper(false).setStrategy(eStrategy.Ratio).setPractice(false);
			compute3.setBuyNumRatio(0.7).setSellRatio_win(1.5).setSellRatio_lose(0.6);
			compute3.setOperationFunction("operation4");
			//end --set config
			
			compute3.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
