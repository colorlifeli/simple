package me.net;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.TypeUtil;
import me.net.NetType.eStockCodeFlag;
import me.net.NetType.eStockSource;
import me.net.model.Item;
import me.net.model.RealTime;
import me.net.model.StockDay;

/**
 * 本实现类，
 * 1.
 * @author James
 *
 */
public class StockSourceImpl1 implements StockSource {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc(name = "sinaSourceService")
	private StockSupplier realtime_supplier;
	@Ioc(name = "yahooSourceService")
	private StockSupplier history_supplier;
	@Ioc
	private StockService stockService;

	private final String historyStartDate = "20140101";

	private eStockSource realtime = eStockSource.SINA;
	private eStockSource history = eStockSource.YAHOO;

	@Override
	public void getRealTime(List<String> codes) {
		List<String> realtime_codes = null;
		try {
			realtime_codes = stockService.getCodes(codes, realtime);
		} catch (SQLException e) {
			logger.error("将code转为 realtime code失败, source:" + realtime);
			e.printStackTrace();
			return;
		}
		@SuppressWarnings("unchecked")
		List<RealTime> list = (List<RealTime>) realtime_supplier.getData(realtime_codes);
		stockService.saveRealTimeData(list);
	}

	@Override
	public void getRealTime(List<String> codes, int interval) {
		getRealTime(codes);
		try {
			Thread.sleep(interval * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 总数约2780，分批进行获取和保存。
	 * 每批数量暂定 200
	 * 经测试，如果一次获取达到1000个code，sina服务器也会拒绝访问
	 * 获取完所有约需5－6秒
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void getRealTimeAll() {
		List<String> realtime_codes;
		List<String> part = new ArrayList<String>();
		List<?> list = null;

		try {
			realtime_codes = stockService.getAllAvailableCodes(0, realtime);
			int size = realtime_codes.size();
			int each = 200;
			int start = 0;

			logger.info("get and save realtime data all. size:" + size);
			while (true) {
				if (size <= start + each) {
					for (int i = start; i < size; i++) {
						part.add(realtime_codes.get(i));
					}
					list = realtime_supplier.getData(part);
					stockService.saveRealTimeData((List<RealTime>) list);

					break;
				} else {
					for (int i = start; i < start + each; i++) {
						part.add(realtime_codes.get(i));
					}

					list = realtime_supplier.getData(part);
					stockService.saveRealTimeData((List<RealTime>) list);

					start += each;
					part.clear();

				}
			}
		} catch (Exception e) {
			logger.error("getRealTimeAll failed");
			e.printStackTrace();
		}

	}

	/**
	 * 由于一次获取需5－10秒，实际的时间间隔将是 interval + 10秒
	 */
	@Override
	public void getRealTimeAll(int interval) {
		// 标记是否开市
		boolean isOpen = false;
		// 由于是循环获取，需要进行限制
		while (true) {
			try {
				if (stockService.isStockTime()) {
					if (isOpen)
						getRealTimeAll();
					else {
						if (this.checkOpen()) {
							isOpen = true;
							this.checkStocks();
						}
					}
				} else {
					if (stockService.isAfterStockTime()) {
						// 防止程序是否多次重启进入
						if (!stockService.isRealtimeDayTableExists()) {

							// 当获取的时间和上次一样时，证明上次已是最后一次
							if (this.isSameAsPrevious()) {
								// 保存到历史表,并重命名今日的实时数据表
								this.dayFinalDo(true);

								// 收市了
								isOpen = false;

								break;
							} else {
								// 数据一般会有延迟，并不一定在严格的开盘时间范围内。所以仍需继续获取数据
								getRealTimeAll();
							}
						} else {
							// TODO 如果作为 web运行，24小时不间断，则不要break
							break;
						}
					}
				}
			} catch (Exception e) {
				logger.error("getRealTimeAll, 获取和保存数据时间失败");
				e.printStackTrace();
			}
			try {
				Thread.sleep(interval * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void getHistory(List<String> codes, String startDate, String endDate) {
		try {
			if (startDate == null)
				startDate = this.historyStartDate;
			if (endDate == null) {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				Date date = new Date();
				endDate = format.format(date);
			}
			List<String> hcodes = stockService.getCodes(codes, history);
			@SuppressWarnings("unchecked")
			List<Item> urls = (List<Item>) history_supplier.getData(hcodes, startDate, endDate);

			logger.info("getHistory, number of urls:" + urls.size() + ", " + new Date());
			stockService.saveCsvFromUrl(urls);
			logger.info("getHistory, END. " + new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getHistoryAll(String startDate, String endDate) {
		try {
			if (startDate == null)
				startDate = this.historyStartDate;
			if (endDate == null) {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				Date date = new Date();
				endDate = format.format(date);
			}

			List<String> codes = stockService.getCodes(0, history);
			List<Item> urls = (List<Item>) history_supplier.getData(codes, startDate, endDate);

			int i = 5; // 最大重做次数
			while (i-- > 0) {
				// 因为网络可能超时，或服务器一时没有反应，所以尝试多次重做
				logger.info("getHistoryAll, number of urls:" + urls.size() + ", " + new Date());
				urls = stockService.saveCsvFromUrl(urls);
				logger.info("getHistoryAll, 第 " + (5 - i) + " 次 END. " + new Date());

				// 如果没有 error，就完成了
				if (urls.size() == 0)
					break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 还没下载历史数据的
	 * @param startDate
	 * @param endDate
	 */
	public void getHistoryRemain(String startDate, String endDate) {
		try {
			if (startDate == null)
				startDate = this.historyStartDate;
			if (endDate == null) {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				Date date = new Date();
				endDate = format.format(date);
			}

			List<String> codes = stockService.getNoHisCode(history);
			@SuppressWarnings("unchecked")
			List<Item> urls = (List<Item>) history_supplier.getData(codes, startDate, endDate);

			int i = 5; // 最大重做次数
			while (i-- > 0) {
				// 因为网络可能超时，或服务器一时没有反应，所以尝试多次重做
				logger.info("getHistoryRemain, number of urls:" + urls.size() + ", " + new Date());
				urls = stockService.saveCsvFromUrl(urls);
				logger.info("getHistoryRemain, 第 " + (5 - i) + " 次 END. " + new Date());

				// 如果没有 error，就完成了
				if (urls.size() == 0)
					break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 检查本次获取数据是否最后一次
	 * @return
	 * @throws SQLException 
	 */
	public boolean isSameAsPrevious() throws SQLException {

		String code = stockService.getAvailableCode();
		List<String> list = new ArrayList<String>();
		list.add(code);
		@SuppressWarnings("unchecked")
		List<RealTime> datas = (List<RealTime>) realtime_supplier.getData(list);
		if (datas == null || datas.get(0) == null) {
			logger.error("isSameAsPrevious, cannot get realtime,code:" + code);
			return false;
		}

		boolean result = stockService.checkSameTime(datas.get(0).code, datas.get(0));

		logger.info("isSameAsPrevious, code:" + code + ", same:" + result);
		return result;

	}

	/**
	 * 对所有stock进行检查，看是否正常，不正常的设置 flag
	 * 
	 * 注意：对于sina, 9:00-9:30 时间，不能进行 checkStock，此时所有stock 都是类似 stop 的状态
	 */
	public void checkStocks() {

		List<String> realtime_codes;
		List<String> part = new ArrayList<String>();

		try {
			stockService.freshAllcode();

			realtime_codes = stockService.getAllAvailableCodes(0, realtime);
			int size = realtime_codes.size();
			int each = 200;
			int start = 0;

			logger.info("checkStocks and set flag. size:" + size);

			while (true) {
				if (size <= start + each) {
					for (int i = start; i < size; i++) {
						part.add(realtime_codes.get(i));
					}
					this.dealAbnormal(realtime_supplier.findAbnormal(part));

					break;
				} else {
					for (int i = start; i < start + each; i++) {
						part.add(realtime_codes.get(i));
					}

					this.dealAbnormal(realtime_supplier.findAbnormal(part));

					start += each;
					part.clear();
				}
			}

		} catch (Exception e) {
			logger.error("getRealTimeAll failed");
			e.printStackTrace();
		}
	}

	/**
	 * 每日最后事情
	 * 
	 * 1. 实时数据转为 day 数据
	 * 2. 由于分时数据只对当日有意义，没必要一个表存储所有分时数据，也担心性能问题，所以每日一个实时表
	 * 	
	 * @param 	isCreateTable 是否需要创建一个新的实时表
	 */
	@SuppressWarnings("unchecked")
	public void dayFinalDo(boolean isCreateTable) {
		// TODO 有时并不会整日去获取实时数据，实时数据就会很少，这时存在一个表也会有浪费，以后考虑怎么处理
		List<String> realtime_codes;
		List<String> part = new ArrayList<String>();
		List<StockDay> list = null;

		try {
			realtime_codes = stockService.getAllAvailableCodes(0, realtime);
			int size = realtime_codes.size();
			int each = 200;
			int start = 0;

			logger.info("get realtime to stock day data all. size:" + size);

			// 1. 将实时数据转为 day
			while (true) {
				if (size <= start + each) {
					for (int i = start; i < size; i++) {
						part.add(realtime_codes.get(i));
					}
					list = stockService.realtimeToDay((List<RealTime>) realtime_supplier.getData(part));
					stockService.saveDayData(list);

					break;
				} else {
					for (int i = start; i < start + each; i++) {
						part.add(realtime_codes.get(i));
					}

					list = stockService.realtimeToDay((List<RealTime>) realtime_supplier.getData(part));
					stockService.saveDayData(list);

					start += each;
					part.clear();
				}
			}

			// 2. 对实时表改名，以今日日期为标识
			if (isCreateTable && !stockService.isRealtimeDayTableExists())
				stockService.dealRealTimeTable();

		} catch (Exception e) {
			logger.error("everydayFinalDealing failed");
			e.printStackTrace();
		}
	}

	/**
	 * 判断是否开市
	 */
	public boolean checkOpen() {
		// TODO 补充判断开市逻辑
		logger.info("checkOpen. time:" + new Date() + ", open:" + true);
		return true;
	}

	private void dealAbnormal(Object[][] result) throws SQLException {
		if (result[0].length > 0) {
			// 表示停牌的
			stockService.setCodeFlag(TypeUtil.oneToTwo(result[0]), eStockCodeFlag.STOP.toString());
			logger.info("dealAbnormal, stop size:" + result[0].length);
		}
		if (result[1].length > 0) {
			// 表示异常的
			stockService.setCodeFlag(TypeUtil.oneToTwo(result[1]), eStockCodeFlag.ERROR.toString());
			logger.info("dealAbnormal, error size:" + result[1].length);
		}
	}

	public StockSupplier getRealtime_supplier() {
		return realtime_supplier;
	}

	public void setRealtime_supplier(StockSupplier realtime_supplier) {
		this.realtime_supplier = realtime_supplier;
	}

	public StockSupplier getHistory_supplier() {
		return history_supplier;
	}

	public void setHistory_supplier(StockSupplier history_supplier) {
		this.history_supplier = history_supplier;
	}

	public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

}
