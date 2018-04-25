package me.app;

import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.compute.Compute1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuyAndSell {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) {

		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = BeanContext.me();
		//StockSourceImpl1 impl = (StockSourceImpl1) bc.getBean("stockSourceImpl1");
		Compute1 compute = (Compute1) bc.getBean("compute1");
		
		compute.setStartDate("2015-04-01");
		try {
			//compute.compute("603116");
			//compute.compute("002570");
			
			compute.computeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		H2Helper.close(SqlRunner.me().getConn());
	}
}
