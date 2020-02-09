package me.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.SimpleException;
import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.Util;
import me.net.StockSourceImpl2;
import me.net.dao.StockSourceDao;

public class GetStockHisData {

	/**
	 * 
	 * 使用 getHistData速度较快，但获取的 startDate 不一定准确。
	 * 如果调用getHistData，每次执行应查看是否有异常。有异常使用 getHisData2 重新执行。直到没有异常为止。
	 * 如果怀疑某段日期开始的数据可能有问题，则可填上 startDate，则会从这个日期开始的数据都会下载，插入时有主键冲突插入不了，没有主键冲突的会正常插入。(已多次确认)
	 * 
	 * 20200206: 对getHisData2进一步优化。不再需要传入 startDate和endDate
	 * 如果早上执行程序时，只能保存到上一天的数据，因此为了一致，如果endDate不填，默认只保存到上一天的数据；如需要下载今天的数据，则endDate填上今天日期即可。
	 * 为防止访问拒绝，进行了sleep。一个code所需时间约1.4秒。所有2800多个code要1个多小时。
	 * 
	 * 20180426: 由于sina会返回拒绝访问，只能多次执行。调用getHisData2，逐个code获取。
	 * getHisData2 如果是周末，为了可以忽略那些已完全获取完的code，不需要再次访问sina, 填周五日期。
	 * 
	 * 执行日志：
	 * 20130101-20161021 数据执行无异常。
	 * 20100101-20121231 数据执行无异常.
	 * 获取到 20180425，数据正常
	 * 
	 * @param args
	 */

	private static Logger logger = LoggerFactory.getLogger(GetStockHisData.class);
	
	public static void main(String[] args) {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = BeanContext.me();
		//StockSourceImpl1 impl = (StockSourceImpl1) bc.getBean("stockSourceImpl1");
		StockSourceImpl2 impl = (StockSourceImpl2) bc.getBean("stockSourceImpl2");
		StockSourceDao stockSourceDao = (StockSourceDao) bc.getBean("stockSourceDao");

		long start = System.currentTimeMillis();

		getHisData2(impl, stockSourceDao);

		long end = System.currentTimeMillis();
		logger.info("use time:" + (end - start));
		
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
				//if("000001".equals(code))
				impl.getHistory(tmp, null, null); 
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
