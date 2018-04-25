package me.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.StockSourceImpl2;
import me.net.dao.StockSourceDao;

public class GetStockHisData {

	/**
	 * 一般使用 getHisData 获取数据。会自动判断从上一次的日期开始获取。
	 * 
	 * 每次执行应执行是否有异常。有异常使用 getHisData2 重新执行。直到没有异常为止。
	 * 
	 * 如果怀疑某段日期开始的数据可能有问题，则可填上 startDate，则会从这个日期开始的数据都会下载，插入时有主键冲突插入不了，没有主键冲突的会正常插入。(已多次确认)
	 * 
	 * 执行日志：
	 * 20130101-20161021 数据执行无异常。
	 * 20100101-20121231 数据执行无异常.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = BeanContext.me();
		//StockSourceImpl1 impl = (StockSourceImpl1) bc.getBean("stockSourceImpl1");
		StockSourceImpl2 impl = (StockSourceImpl2) bc.getBean("stockSourceImpl2");
		StockSourceDao stockSourceDao = (StockSourceDao) bc.getBean("stockSourceDao");

		long start = System.currentTimeMillis();

		getHisData(impl);
		//getHisData2(impl, stockSourceDao);

		long end = System.currentTimeMillis();
		System.out.println("use time:" + (end - start));
		
		H2Helper.close(SqlRunner.me().getConn());
		System.exit(0);
	}
	
	public static void getHisData(StockSourceImpl2 impl) {
		impl.getHistoryAll("20161110", null);
		//impl.getHistoryAll(null, null);
	}
	
	//逐个code来获取数据。
	public static void getHisData2(StockSourceImpl2 impl, StockSourceDao stockSourceDao) {
		try {
			List<String> codes = stockSourceDao.getAllAvailableCodes(0, null);
			List<String> tmp = new ArrayList<String>();
			for(String code : codes) {
				tmp.clear();
				tmp.add(code);
				impl.getHistory(tmp, null, "20161130"); //endDate 由于今天的数据还没出，所以一般填昨天的日期。如果是周末，为了可以忽略已完全获取取的，可填周五日期。
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
