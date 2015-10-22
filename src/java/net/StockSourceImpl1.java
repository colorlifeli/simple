package net;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.model.RealTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.annotation.IocAnno.Ioc;

public class StockSourceImpl1 implements StockSource {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc(name = "sinaSourceService")
	private SinaSourceService sina;
	@Ioc
	private StockService stockService;

	@Override
	public void getRealTime(List<String> codes) {
		List<String> sina_codes = null;
		try {
			sina_codes = stockService.getCodes(codes);
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
			sina_codes = stockService.getCodes(0);
			int size = sina_codes.size();
			int each = 200;
			int start = 0;

			while (true) {
				if (size <= start + each) {
					for (int i = start; i < size; i++) {
						part.add(sina_codes.get(i));
					}
					list = sina.getRealTime(part);
					stockService.saveRealTimeData(list);

					break;
				} else {
					logger.info("get and save realtime data all. size:" + size + ",start:" + start);
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
	 * 由于一次获取需5－6秒，实际的时间间隔将是 interval + 6秒
	 */
	@Override
	public void getRealTimeAll(int interval) {
		// 由于是循环获取，需要进行限制
		while (true) {
			// 获取当前时间
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR);// 小时
			if (hour < 9 || (hour > 11))
				getRealTimeAll();
			try {
				Thread.sleep(interval * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void getHistory(List<String> codes, String startDate, String endDate) {
		// TODO Auto-generated method stub

	}

	/**
	 * 检查本次获取数据是否最后一次
	 * @return
	 * @throws SQLException 
	 */
	public boolean checkLast() throws SQLException {

		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR);// 小时
		// 当获取的时间和上次一样时，证明上次已是最后的时间了
		if (hour >= 15) {
			String code = stockService.getAvailableCode();
			List<String> list = new ArrayList<String>();
			list.add(code);
			RealTime data = sina.getRealTime(list).get(0);
			return stockService.checkSameTime(code, data);
		}
		return false;
	}

	/**
	 * 对所有stock进行检查，看是否正常，不正常的设置 flag
	 */
	public void checkStocks() {
		List<String> sina_codes;
		List<String> part = new ArrayList<String>();
		List<RealTime> list = null;

		try {
			sina_codes = stockService.getCodes(0);
			int size = sina_codes.size();
			int each = 200;
			int start = 0;

			while (true) {
				if (size <= start + each) {
					for (int i = start; i < size; i++) {
						part.add(sina_codes.get(i));
					}
					list.addAll(sina.getRealTime(part));
					// stockService.saveRealTimeData(list);

					break;
				} else {
					logger.info("get and save realtime data all. size:" + size + ",start:" + start);
					for (int i = start; i < start + each; i++) {
						part.add(sina_codes.get(i));
					}

					list.addAll(sina.getRealTime(part));
					// stockService.saveRealTimeData(list);

					start += each;
					part.clear();

				}
			}

		} catch (Exception e) {
			logger.error("getRealTimeAll failed");
			e.printStackTrace();
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

}
