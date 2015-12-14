package me.net.dayHandler;

import java.util.ArrayList;
import java.util.List;

import me.common.annotation.IocAnno.Ioc;
import me.common.jdbcutil.SqlRunner;
import me.common.util.TypeUtil;
import me.net.StockDataService;
import me.net.model.StockDay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于分析历史数据，对历史数据进行处理
 * 
 * @author James
 *
 */

public class Analyzer {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	SqlRunner sqlrunner = SqlRunner.me();

	@Ioc
	private StockDataService stockDataService;

	private String code = "002061";

	/**
	 *  包含处理
	 *  只需一次包含处理即可，处理后不会存在包含关系的了
	 */
	public List<StockDay> include(List<StockDay> days) {
		// int step = 1;
		List<StockDay> days2 = new ArrayList<StockDay>();

		// List<StockDay> days = stockDataService.getDay(code, null, null);

		logger.info("origin size: " + days.size());

		int i = 1;
		int j = 1;
		days2.add(days.get(0));
		days2.add(days.get(1));
		while (i++ < days.size() - 1) {

			StockDay day_pre = days2.get(j - 1);
			StockDay day = days2.get(j);
			StockDay day_next = days.get(i);

			// 4种情况
			// 包含关系时，days2是不动的，因为不断地包含了另一个
			if (isInclude(day, day_next)) {

				logger.debug("include find，front include back");
				logger.debug("day:" + day);
				logger.debug("day next:" + day_next);

				day.date_ = day.date_;
				if (Double.parseDouble(day.high) >= Double.parseDouble(day_pre.high)) {
					// 1.前包含后，且向上处理
					day.high = day.high;
					day.low = day_next.low;
				} else {
					// 2.前包含后，且向下处理
					day.high = day_next.high;
					day.low = day.low;
				}

				logger.debug("new day:" + day);
			} else if (isInclude(day_next, day)) {

				logger.debug("include find，back include front ");
				logger.debug("day:" + day);
				logger.debug("day next:" + day_next);

				day.date_ = day_next.date_;
				if (Double.parseDouble(day.high) >= Double.parseDouble(day_pre.high)) {
					// 3.后包含前，且向上处理
					day.high = day_next.high;
					day.low = day.low;
				} else {
					// 4.后包含前，且向下处理
					day.high = day.high;
					day.low = day_next.low;
				}

				logger.debug("new day:" + day);
			} else {
				// 非包含时，复制一个到新的数组 days2
				StockDay day2 = new StockDay();
				day2.code = code;
				day2.date_ = day_next.date_;
				day2.high = day_next.high;
				day2.low = day_next.low;
				days2.add(day2);
				j++;

			}
		}

		logger.info("after include handle, size: " + days2.size());

		days2 = this.adjust(days2);

		return days2;
	}

	/**
	 * 识别分型
	 * 顶分型：连续3根，中间最高
	 * 底分型：连续3根，中间最低
	 * 
	 * 分型之间有独立k线
	 * @param days
	 * @return
	 */
	public List<StockDay> recognizeType(List<StockDay> days) {

		// 是否需要独立k线
		boolean needK = true;

		// List<StockDay> days2 = new ArrayList<StockDay>();

		logger.info("origin size: " + days.size());

		int i = 0;
		while (i + 2 < days.size()) {

			StockDay day1 = days.get(i);
			StockDay day2 = days.get(i + 1);
			StockDay day3 = days.get(i + 2);

			if (Double.parseDouble(day2.high) > Double.parseDouble(day1.high)
					&& Double.parseDouble(day2.high) > Double.parseDouble(day3.high)) {
				day2.flag = TypeUtil.StockDayFlag.TOP.toString();
				i += 4;
				if (!needK) {
					i += 3;
				}
			} else if (Double.parseDouble(day2.low) < Double.parseDouble(day1.low)
					&& Double.parseDouble(day2.low) < Double.parseDouble(day3.low)) {
				day2.flag = TypeUtil.StockDayFlag.BOTTOM.toString();
				i += 4;
				if (!needK) {
					i += 3;
				}
			} else {
				i++;
			}

		}

		for (StockDay day : days) {
			System.out.println(day);
		}

		return days;
	}

	/**
	 * 只使用 high和 low 数据，为了能在图上显示，因此补充 open/close数据
	 * @param days
	 * @return
	 */
	private List<StockDay> adjust(List<StockDay> days) {

		for (StockDay day : days) {
			day.open_ = day.low;
			day.close_ = day.high;
		}
		return days;
	}

	/**
	 * day1 include day2
	 * @param day1
	 * @param day2
	 * @return
	 */
	private boolean isInclude(StockDay day1, StockDay day2) {
		// logger.debug("day1:{}, day2:{}", day1, day2);
		if (Double.parseDouble(day1.high) >= Double.parseDouble(day2.high)
				&& Double.parseDouble(day1.low) <= Double.parseDouble(day2.low))
			return true;

		return false;
	}

	public static void main(String[] args) {

	}

}
