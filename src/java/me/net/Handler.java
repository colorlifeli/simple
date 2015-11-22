package me.net;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.annotation.IocAnno.Ioc;
import me.common.jdbcutil.SqlRunner;
import me.net.model.StockDay;

public class Handler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	SqlRunner sqlrunner = SqlRunner.me();

	@Ioc
	private StockDataService stockDataService;

	private String code = "002061";

	/**
	 *  包含处理
	 */
	public List<StockDay> include() {
		int step = 1;
		List<StockDay> days2 = new ArrayList<StockDay>();

		try {
			List<StockDay> days = stockDataService.getDay(code, null, null);

			logger.info("origin size: " + days.size());

			int i = 0;
			int j = 0;
			days2.add(days.get(0));
			while (i++ < days.size() - 1) {

				StockDay day2 = new StockDay();
				day2.code = code;

				StockDay day_pre = days2.get(j);
				StockDay day = days.get(i);
				StockDay day_next = days.get(i + 1);

				//4种情况
				if (isInclude(day, day_next)) {
					day2.date_ = day.date_;
					if (Integer.parseInt(day.high) >= Integer.parseInt(day_pre.high)) {
						//1.前包含后，且向上处理
						day2.high = day.high;
						day2.low = day_next.low;
					} else {
						//2.前包含后，且向下处理
						day2.high = day_next.high;
						day2.low = day.low;
					}

					//包含关系，多跳一步
					i++;
					logger.debug("include find，front include back");
					logger.debug("day:" + day);
					logger.debug("day next:" + day_next);
					logger.debug("new day:" + days2);
				} else if (isInclude(day_next, day)) {
					day2.date_ = day.date_;
					if (Integer.parseInt(day.high) >= Integer.parseInt(day_pre.high)) {
						//3.后包含前，且向上处理
						day2.high = day_next.high;
						day2.low = day.low;
					} else {
						//4.后包含前，且向下处理
						day2.high = day.high;
						day2.low = day_next.low;
					}

					//包含关系，多跳一步
					i++;
					logger.debug("include find，back include front0y ");
					logger.debug("day:" + day);
					logger.debug("day next:" + day_next);
					logger.debug("new day:" + days2);
				} else {
					day2.high = day.high;
					day2.low = day.low;

				}

				days2.add(day2);
				j++;

			}

			logger.info("after include handle, size: " + days2.size());

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return days2;
	}

	/**
	 * day1 include day2
	 * @param day1
	 * @param day2
	 * @return
	 */
	private boolean isInclude(StockDay day1, StockDay day2) {
		if (Integer.parseInt(day1.high) >= Integer.parseInt(day2.high)
				&& Integer.parseInt(day1.low) <= Integer.parseInt(day2.low))
			return true;

		return false;
	}

	public static void main(String[] args) {

	}

}
