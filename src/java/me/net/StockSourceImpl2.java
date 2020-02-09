package me.net;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.TypeUtil;
import me.common.util.Util;
import me.net.dao.StockAnalysisDao;
import me.net.dao.StockSourceDao;
import me.net.model.StockDay;
import me.net.supplier.IStockSupplier;

/**
 * 20200208 由于原因的新浪历史数据接口不能用了，启用了新的实现 sinaHisSupplier2
 * 			如果早上执行程序时，只能保存到上一天的数据，因此为了一致，如果endDate不填，默认只保存到上一天的数据
 * 经测试，每个code 1秒间隔是可行的，加上执行时间0.4秒，一个code所需时间约1.4秒。所有2800多个code要1个多小时。
 * @author opq
 *
 */
public class StockSourceImpl2 implements StockSource {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc(name = "sinaHisSupplier2")
	private IStockSupplier history_supplier;

	@Ioc
	private StockSourceDao stockSourceDao;
	@Ioc
	private StockAnalysisDao stockAnalysisDao;

	private final String historyStartDate = "20130101";

	private final SimpleDateFormat format_db = new SimpleDateFormat("yyyy-MM-dd"); //数据库日期的表示
	private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

	@Override
	/**
	 * 为了保证supplier每次获取数据中间不会因为网络异常等原因而漏数，有任何异常应该马上往外抛，
	 * 中断执行过程，保证后面的数据也不被插入。
	 * 
	 * 20200206 
	 * 
	 * startDate: yyyyMMdd
	 * endDate: yyyyMMdd
	 */
	public void getHistory(List<String> codes, String startDate, String endDate) {
		if (startDate == null) {
			// 多个codes时不指定 startDate，则无法确定 startDate是什么。如果赋一个常量值，可能会带来大量不必要的数据下载。
			// 因此直接返回。
			if (codes.size() > 1) {
				logger.error(" ******  多个 code 时必须指定 startDate。******* ");
				return;
			}
			try {
				//原来 startDate 是根据已有数据的最后一天，再加一天来确定，
				//现在改为用表记录到底插入到哪一天。主要是因为有些会停牌，现有数据的最后
				//一天并不是查询过的最后一天。避免每次执行都要重新get
				String lastDate = null;
				lastDate = stockAnalysisDao.getLastDate(codes.get(0));
				if (!TypeUtil.isEmpty(lastDate)) {
					Calendar c = Calendar.getInstance();
					c.setTime(format_db.parse(lastDate));
					c.add(Calendar.DAY_OF_YEAR, 1);
					startDate = format.format(c.getTime());
				} else {
					List<String> allDates = stockAnalysisDao.getAllDate(codes.get(0));
					if (allDates != null && allDates.size() > 0) {
						Calendar c = Calendar.getInstance();
						c.setTime(format_db.parse(allDates.get(allDates.size() - 1)));
						c.add(Calendar.DAY_OF_YEAR, 1);
						startDate = format.format(c.getTime());
					} else {
						logger.error("no start date");
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.debug("获取日期时出错。");
				return;
			}
		}
		if (endDate == null) {
			//如果早上执行程序时，只能保存到上一天的数据，为了一致，只保存到上一天。
			Date date = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.DAY_OF_YEAR, -1);
			endDate = format.format(c.getTime());
		}
		if(startDate.compareTo(endDate) > 0) {
			logger.error("start is after end!! start:" + startDate + ", end:" + endDate);
			return;
		}

		logger.debug(Util.getString(codes) + " will be got. start:" + startDate + ", end:" + endDate);
		@SuppressWarnings("unchecked")
		List<StockDay> days = (List<StockDay>) history_supplier.getData(codes, startDate, endDate);
		
		stockSourceDao.saveDayData2(days);
		try {
			stockAnalysisDao.saveHisProgress(codes, format.parse(endDate));
		
			//新浪加强了管制，用了防爬虫的措施，访问太快会被禁
			//2020208新接口也会被拒绝！！！
			//if(days != null && days.size() > 0)
				Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getHistoryAll(String startDate, String endDate) {
		if (startDate == null) {
			try {
				List<String> allDates = stockAnalysisDao.getAllDate();
				if (allDates != null && allDates.size() > 0) {
					Calendar c = Calendar.getInstance();
					c.setTime(format_db.parse(allDates.get(allDates.size() - 1)));
					c.add(Calendar.DAY_OF_YEAR, 1);
					startDate = format.format(c.getTime());
					logger.debug("start date: " + startDate);
				} else
					startDate = historyStartDate;
			} catch (Exception e) {
				e.printStackTrace();
				logger.debug("获取日期时出错。");
				return;
			}
		}
		if (endDate == null) {
			Date date = new Date();
			endDate = format.format(date);
		}
		
		try {
			List<String> codes = stockSourceDao.getAllAvailableCodes(0, null);

			//分批获取与保存
			List<String> part = new ArrayList<String>();
			List<?> list = null;
			int size = codes.size();
			int each = 20; //20个code有过万条记录 
			int start = 0;
			
			while (true) {
				if (size <= start + each) {
					part = codes.subList(start, size);
					list = history_supplier.getData(part, startDate, endDate);
					stockSourceDao.saveDayData2((List<StockDay>) list);

					break;
				} else {
					part = codes.subList(start, start + each);

					list = history_supplier.getData(part, startDate, endDate);
					stockSourceDao.saveDayData2((List<StockDay>) list);

					start += each;
				}
				logger.debug("process: " + start);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	
	
	@Override
	public void getRealTime(List<String> codes) {

	}

	@Override
	public void getRealTime(List<String> codes, int interval) {

	}

	@Override
	public void getRealTimeAll() {

	}

	@Override
	public void getRealTimeAll(int interval) {

	}

	public IStockSupplier getHistory_supplier() {
		return history_supplier;
	}

	public void setHistory_supplier(IStockSupplier history_supplier) {
		this.history_supplier = history_supplier;
	}

	public StockSourceDao getStockSourceDao() {
		return stockSourceDao;
	}

	public void setStockSourceDao(StockSourceDao stockSourceDao) {
		this.stockSourceDao = stockSourceDao;
	}

	public void setStockAnalysisDao(StockAnalysisDao stockAnalysisDao) {
		this.stockAnalysisDao = stockAnalysisDao;
	}

}
