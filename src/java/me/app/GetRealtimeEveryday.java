package me.app;

import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.StockSourceImpl1;

public class GetRealtimeEveryday {
	public static void main(String[] args) {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = BeanContext.me();
		StockSourceImpl1 impl = (StockSourceImpl1) bc.getBean("stockSourceImpl1");

		long start = System.currentTimeMillis();

		impl.dayFinalDo(false);

		long end = System.currentTimeMillis();
		System.out.println("use time:" + (end - start));
	}
}
