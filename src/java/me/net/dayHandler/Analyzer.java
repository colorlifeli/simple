package me.net.dayHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.annotation.IocAnno.Ioc;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.NetType.eStockDayFlag;
import me.net.NetType.eStockSource;
import me.net.StockDataService;
import me.net.StockService;
import me.net.model.StockDay;

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

	//private String code = "002061";

	/**
	 *  包含处理
	 *  只需一次包含处理即可，处理后不会存在包含关系的了
	 */
	public List<StockDay> include(List<StockDay> days) {
		// int step = 1;
		List<StockDay> days2 = new ArrayList<StockDay>();

		// List<StockDay> days = stockDataService.getDay(code, null, null);

		logger.info("origin size: " + days.size());

		int i = 0;
		while (i < days.size()) {
			if (!includeOne(days2, days.get(i))) {
				days2.add(days.get(i));
			}
			i++;
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

		logger.info("origin size: " + days.size());

		List<StockDay> days2 = new ArrayList<StockDay>();
		int i = 0;
		while (i < days.size()) {
			this.recognizeTypeOne(days2, days.get(i), needK);
			days2.add(days.get(i));
			i++;
		}

		/*
		int i = 0;
		while (i + 2 < days.size()) {
		
			StockDay day1 = days.get(i);
			StockDay day2 = days.get(i + 1);
			StockDay day3 = days.get(i + 2);
		
			if (Double.parseDouble(day2.high) > Double.parseDouble(day1.high)
					&& Double.parseDouble(day2.high) > Double.parseDouble(day3.high)) {
				day2.flag = eStockDayFlag.TOP.toString();
				i += 4;
				if (!needK) {
					i += 3;
				}
			} else if (Double.parseDouble(day2.low) < Double.parseDouble(day1.low)
					&& Double.parseDouble(day2.low) < Double.parseDouble(day3.low)) {
				day2.flag = eStockDayFlag.BOTTOM.toString();
				i += 4;
				if (!needK) {
					i += 3;
				}
			} else {
				i++;
			}
		}*/

		for (StockDay day : days2) {
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
		if (Double.parseDouble(day1.high) >= Double.parseDouble(day2.high)
				&& Double.parseDouble(day1.low) <= Double.parseDouble(day2.low))
			return true;

		return false;
	}

	/**
	 * 对下一天进行包含处理
	 * 
	 * @param includedList：已经过包含处理的列表
	 * @param day_next：要处理的数据
	 * @return 返回是否包含。true：做了包含处理 
	 */
	public boolean includeOne(List<StockDay> includedList, StockDay day_next) {
		if (includedList.size() < 2) {
			return false;
		}
		StockDay day = includedList.get(includedList.size() - 1); // 最后一个
		StockDay day_pre = includedList.get(includedList.size() - 2);// 倒数第二个
		if (isInclude(day, day_next)) {
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

		} else if (isInclude(day_next, day)) {

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

		} else {
			return false;
		}

		return true;
	}

	/**
	 * 新的一条数据是否对历史构成分型形成分型
	 * @param his
	 * @param day
	 * @param isNeedK:2个分型之间是否需要独立k线
	 * @return eStockDayFlag.toString 或者 null
	 */
	public String recognizeTypeOne(List<StockDay> his, StockDay day, boolean isNeedK) {

		if (his.size() < 2)
			return null;

		StockDay last1 = his.get(his.size() - 1);
		StockDay last2 = his.get(his.size() - 2);

		String top = eStockDayFlag.TOP.toString();
		String bottom = eStockDayFlag.BOTTOM.toString();

		// 要超过一定间隔才能再次设置分型
		if ((last2 != null && (top.equals(last2.flag) || bottom.equals(last2.flag)))) {
			return null;
		}
		if (his.size() > 2) {
			StockDay last3 = his.get(his.size() - 3);
			if (last3 != null && (top.equals(last3.flag) || bottom.equals(last3.flag)))
				return null;
		}
		if (isNeedK && his.size() > 3) {
			StockDay last4 = his.get(his.size() - 4);
			if (top.equals(last4.flag) || bottom.equals(last4.flag))
				return null;
		}

		// 判断是分型
		if (Double.parseDouble(last1.high) > Double.parseDouble(last2.high)
				&& Double.parseDouble(last1.high) > Double.parseDouble(day.high)) {
			last1.flag = top;
			return top;
		} else if (Double.parseDouble(last1.low) < Double.parseDouble(last2.low)
				&& Double.parseDouble(last1.low) < Double.parseDouble(day.low)) {
			last1.flag = bottom;
			return bottom;
		}

		return null;
	}

	public static void main(String[] args) throws SQLException {

		SqlRunner.me().setConn(H2Helper.connEmbededDb());
		Analyzer anlyzer = new Analyzer();
		String hcode = new StockService().getCode("002061", eStockSource.YAHOO);
		List<StockDay> list = new StockDataService().getDay(hcode, null, null);

		list = anlyzer.include(list);

		System.out.println("\n************************************************\n");

		list = anlyzer.recognizeType(list);

		//		List<StockDay> his = new ArrayList<StockDay>();
		//		for (StockDay day : list) {
		//			//没做包含处理才需判断分型
		//			if (!anlyzer.includeOne(his, day)) {
		//				anlyzer.recognizeTypeOne(his, day, true);
		//				his.add(day);
		//			}
		//		}
		//		for (StockDay day : his) {
		//			System.out.println(day);
		//		}
	}

}
