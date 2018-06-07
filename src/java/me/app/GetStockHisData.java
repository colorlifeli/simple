package me.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.SimpleException;
import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.Util;
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
	 * 使用 getHistData速度较快，但获取的 startDate 不一定准确。
	 * 20180426: 由于sina会返回拒绝访问，只能多次执行。调用getHisData2，逐个code获取。
	 * getHisData2 如果是周末，为了可以忽略那些已完全获取完的code，不需要再次访问sina, 填周五日期。
	 * 执行日志：
	 * 20130101-20161021 数据执行无异常。
	 * 20100101-20121231 数据执行无异常.
	 * 获取到 20180425，数据正常
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

		//getHisData(impl);
		getHisData2(impl, stockSourceDao);
		//getHisDataByCode(impl, "002351");

		long end = System.currentTimeMillis();
		System.out.println("use time:" + (end - start));
		
		H2Helper.close(SqlRunner.me().getConn());
		System.exit(0);
	}
	
	public static void getHisData(StockSourceImpl2 impl) {
		impl.getHistoryAll("20161130", null);
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
				//endDate 由于今天的数据还没出，所以一般填昨天的日期。
				//如果是周末，为了可以忽略那些已完全获取完的code，不需要再次访问新浪。可填周五日期。
				impl.getHistory(tmp, null, "20180425"); 
				
			}
		} catch(SimpleException se) {
			Util.logger.error(se.getMessage(), se);
		} catch (SQLException e) {
			Util.logger.error("", e);
		}
	}

	/**
	 * 获取单个历史数据，主要用于调试程序
	 * @param impl
	 * @param code
	 */
	public static void getHisDataByCode(StockSourceImpl2 impl, String code) {
		List<String> codes = new ArrayList<String>();
		codes.add(code);
		impl.getHistory(codes, "20161130", null);
	}
}
