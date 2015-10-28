package net;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.model.RealTime;
import net.model.StockDay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.annotation.IocAnno.Ioc;
import common.util.TypeUtil;

/**
 * 本实现类，
 * 1.
 * @author James
 *
 */
public class StockSourceImpl1 implements StockSource {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc(name = "sinaSourceService")
	private SinaSourceService sina;
	@Ioc(name = "yahooSourceService")
	private YahooSourceService yahoo;
	@Ioc
	private StockService stockService;

	private String historyStartDate = "20140101";

	@Override
	public void getRealTime(List<String> codes) {
		List<String> sina_codes = null;
		try {
			sina_codes = stockService.getCodes(codes, "r");
		} catch (SQLException e) {
			logger.error("将code转为 sina code失败");
			e.printStackTrace();
			return;
		}
		List<RealTime> list = sina.getRealTime(sina_codes);
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
	@Override
	public void getRealTimeAll() {
		List<String> sina_codes;
		List<String> part = new ArrayList<String>();
		List<RealTime> list = null;

		try {
			sina_codes = stockService.getAllAvailableCodes(0, "r");
			int size = sina_codes.size();
			int each = 200;
			int start = 0;

			logger.info("get and save realtime data all. size:" + size);
			while (true) {
				if (size <= start + each) {
					for (int i = start; i < size; i++) {
						part.add(sina_codes.get(i));
					}
					list = sina.getRealTime(part);
					stockService.saveRealTimeData(list);

					break;
				} else {
					for (int i = start; i < start + each; i++) {
						part.add(sina_codes.get(i));
					}

					list = sina.getRealTime(part);
					stockService.saveRealTimeData(list);

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
								this.dayFinalDo();

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
			List<String> hcodes = stockService.getCodes(codes, "h");
			Map<String, String> urls = yahoo.getHistory(hcodes, startDate, endDate);

			logger.info("getHistory, number of urls:" + urls.size() + ", " + new Date());
			stockService.saveCsvFromUrl(urls);
			logger.info("getHistoryAll, end " + new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

			List<String> codes = stockService.getCodes(0, "h");
			Map<String, String> urls = yahoo.getHistory(codes, startDate, endDate);

			int i = 5; // 最大重做次数
			while (i-- > 0) {
				// 因为网络可能超时，或服务器一时没有反应，所以尝试多次重做
				logger.info("getHistoryAll, number of urls:" + urls.size() + ", " + new Date());
				urls = stockService.saveCsvFromUrl(urls);
				logger.info("getHistoryAll, end " + new Date());

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
		List<RealTime> datas = sina.getRealTime(list);
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

		List<String> sina_codes;
		List<String> part = new ArrayList<String>();

		try {
			stockService.freshAllcode();

			sina_codes = stockService.getAllAvailableCodes(0, "r");
			int size = sina_codes.size();
			int each = 200;
			int start = 0;

			logger.info("checkStocks and set flag. size:" + size);

			while (true) {
				if (size <= start + each) {
					for (int i = start; i < size; i++) {
						part.add(sina_codes.get(i));
					}
					this.dealAbnormal(sina.findAbnormal(part));

					break;
				} else {
					for (int i = start; i < start + each; i++) {
						part.add(sina_codes.get(i));
					}

					this.dealAbnormal(sina.findAbnormal(part));

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
	 */
	public void dayFinalDo() {
		// TODO 有时并不会整日去获取实时数据，实时数据就会很少，这时存在一个表也会有浪费，以后考虑怎么处理
		List<String> sina_codes;
		List<String> part = new ArrayList<String>();
		List<StockDay> list = null;

		try {
			sina_codes = stockService.getAllAvailableCodes(0, "r");
			int size = sina_codes.size();
			int each = 200;
			int start = 0;

			logger.info("get realtime to stock day data all. size:" + size);

			// 1. 将实时数据转为 day
			while (true) {
				if (size <= start + each) {
					for (int i = start; i < size; i++) {
						part.add(sina_codes.get(i));
					}
					list = stockService.realtimeToDay(sina.getRealTime(part));
					stockService.saveDayData(list);

					break;
				} else {
					for (int i = start; i < start + each; i++) {
						part.add(sina_codes.get(i));
					}

					list = stockService.realtimeToDay(sina.getRealTime(part));
					stockService.saveDayData(list);

					start += each;
					part.clear();
				}
			}

			// 2. 对实时表改名，以今日日期为标识
			if (!stockService.isRealtimeDayTableExists())
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
			stockService.setCodeFlag(TypeUtil.oneToTwo(result[0]), TypeUtil.StockCodeFlag.STOP.toString());
			logger.info("dealAbnormal, stop size:" + result[0].length);
		}
		if (result[1].length > 0) {
			// 表示异常的
			stockService.setCodeFlag(TypeUtil.oneToTwo(result[1]), TypeUtil.StockCodeFlag.ERROR.toString());
			logger.info("dealAbnormal, error size:" + result[1].length);
		}
	}

	public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
		stockService.setImpl("impl1");
	}

	public SinaSourceService getSina() {
		return sina;
	}

	public void setSina(SinaSourceService sina) {
		this.sina = sina;
	}

	public YahooSourceService getYahoo() {
		return yahoo;
	}

	public void setYahoo(YahooSourceService yahoo) {
		this.yahoo = yahoo;
	}

}
