package me.app;

import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.StockSourceImpl1;

public class GetStockHisData {

	public static void main(String[] args) {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = new BeanContext();
		StockSourceImpl1 impl = (StockSourceImpl1) bc.getBean("stockSourceImpl1");

		long start = System.currentTimeMillis();

		//impl.getHistoryAll(null, null);
		impl.getHistoryRemain(null, null);

		long end = System.currentTimeMillis();
		System.out.println("use time:" + (end - start));
	}

}
