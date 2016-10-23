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
import me.net.dao.StockAnalysisDao;
import me.net.dao.StockSourceDao;
import me.net.model.StockDay;
import me.net.supplier.IStockSupplier;

public class StockSourceImpl2 implements StockSource {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc(name = "sinaHisSupplier")
	private IStockSupplier history_supplier;

	@Ioc
	private StockSourceDao stockSourceDao;
	@Ioc
	private StockAnalysisDao stockAnalysisDao;

	private final String historyStartDate = "20130101";

	private final SimpleDateFormat format_db = new SimpleDateFormat("yyyy-MM-dd"); //数据库日期的表示
	private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

	@Override
	public void getHistory(List<String> codes, String startDate, String endDate) {
		if (startDate == null) {
			try {
				List<String> allDates = stockAnalysisDao.getAllDate(codes.get(0));
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
		
		@SuppressWarnings("unchecked")
		List<StockDay> days = (List<StockDay>) history_supplier.getData(codes, startDate, endDate);
		
		stockSourceDao.saveDayData2(days);
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
