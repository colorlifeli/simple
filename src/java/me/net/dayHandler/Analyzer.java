package me.net.dayHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.NetType.eStockDayFlag;
import me.net.NetType.eStockSource;
import me.net.dao.StockAnalysisDao;
import me.net.dao.StockSourceDao;
import me.net.model.Central;
import me.net.model.CentralInfo;
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

	//private String code = "002061";

	/**
	 *  包含处理
	 *  只需一次包含处理即可，处理后不会存在包含关系的了
	 */
	public List<StockDay> include(List<StockDay> days) {
		// int step = 1;
		List<StockDay> days2 = new ArrayList<StockDay>();

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

		String result = null;

		// 要超过一定间隔才能再次设置分型
		//		if ((last2 != null && (top.equals(last2.flag) || bottom.equals(last2.flag)))) {
		//			return null;
		//		}
		//		if (his.size() > 2) {
		//			StockDay last3 = his.get(his.size() - 3);
		//			if (last3 != null && (top.equals(last3.flag) || bottom.equals(last3.flag)))
		//				return null;
		//		}
		//		if (isNeedK && his.size() > 3) {
		//			StockDay last4 = his.get(his.size() - 4);
		//			if (top.equals(last4.flag) || bottom.equals(last4.flag))
		//				return null;
		//		}

		//如果加入上面间隔的判断语句的话，就会出现一个顶分型的下一个分型仍然是顶分型的情况。
		//去除下面的间隔约束之后，则必定是顶底分型交叉出现，顶分型的下一个必是底分型，反之成立。

		// 判断是分型
		if (Double.parseDouble(last1.high) > Double.parseDouble(last2.high)
				&& Double.parseDouble(last1.high) > Double.parseDouble(day.high)) {
			//last1.flag = top;
			result = top;
		} else if (Double.parseDouble(last1.low) < Double.parseDouble(last2.low)
				&& Double.parseDouble(last1.low) < Double.parseDouble(day.low)) {
			//last1.flag = bottom;
			result = bottom;
		}

		if (result != null) {

			//间隔的判断改为：如果最新分型与上一个分弄间隔太小，则同时去除最新分型与上一个分型。这样就可以保证顶底是交叉的。
			if ((last2 != null && (top.equals(last2.flag) || bottom.equals(last2.flag)))) {
				last2.flag = null;
				return "deleteLast";
			} else if (his.size() > 2) {
				StockDay last3 = his.get(his.size() - 3);
				if (last3 != null && (top.equals(last3.flag) || bottom.equals(last3.flag))) {
					last3.flag = null;
					return "deleteLast";
				}
			} else if (isNeedK && his.size() > 3) {
				StockDay last4 = his.get(his.size() - 4);
				if (top.equals(last4.flag) || bottom.equals(last4.flag)) {
					last4.flag = null;
					return "deleteLast";
				}
			}

			last1.flag = result;
		}

		return result;
	}

	/**
	 * 
	 * @param his
	 * @param point ：分型后的顶点。顶分型是day的high，底分型是day的low
	 * @return 返回是否形成中枢，true:是 false:否
	 */
	public boolean makeCentral(CentralInfo info, String point) {

		info.points.add(point);

		if (info.points.size() < 4) {
			//4个点，构成3笔，才能计算中枢
			return false;
		}

		// ****** 开始计算中枢

		//判断第一笔是向上笔还是向下笔
		String point1 = info.points.get(0);
		String point2 = info.points.get(1);
		String point3 = info.points.get(2);
		String point4 = info.points.get(3);

		logger.debug("{},{},{},{}", point1, point2, point3, point4);

		Central c = new Central();
		if (Double.parseDouble(point1) < Double.parseDouble(point2)) {
			//向上笔
			c.low = max(point1, point2);
			c.high = min(point2, point4);
		} else {
			c.low = max(point2, point4);
			c.high = min(point1, point3);
		}

		if (Double.parseDouble(c.low) < Double.parseDouble(c.high)) {
			//有公共区域，才有中枢

			logger.debug("中枢：({},{})", c.low, c.high);

			//这是第一个中枢
			boolean result = false;
			if (info.centrals.size() == 0) {
				c.position = 0;
				result = true;
			} else {
				//如果前面已经有中枢，则要与最后一个中枢进行比较
				Central c_pre = info.centrals.get(info.centrals.size() - 1);
				if (Double.parseDouble(c.low) > Double.parseDouble(c_pre.high)) {
					//趋势向上。记录第几次向上
					c.position = c_pre.position > 0 ? c_pre.position + 1 : 1;
					result = true;
				} else if (Double.parseDouble(c.high) > Double.parseDouble(c_pre.low)) {
					//趋势向下
					c.position = c_pre.position < 0 ? c_pre.position - 1 : -1;
					result = true;
				}
			}
			if (result) {
				info.centrals.add(c);

				//保留最后一个中枢的4点
				info.pointsHis.clear();
				info.pointsHis.addAll(info.points);

				info.points.clear();
				//以最后一点作为下一个中枢的起点
				info.points.add(point4);
				return true;
			}
		} else {
			//删除第一个点，和后面加入的点再形成中枢
			info.points.remove(0);
		}

		return false;

	}

	/**
	 * 取较大值。针对String 类型的数字
	 * @param num1
	 * @param num2
	 * @return
	 */
	private String max(String num1, String num2) {
		if (Double.parseDouble(num1) > Double.parseDouble(num2))
			return num1;
		else
			return num2;
	}

	/**
	 * 取较小值。针对String 类型的数字
	 * @param num1
	 * @param num2
	 * @return
	 */
	private String min(String num1, String num2) {
		if (Double.parseDouble(num1) < Double.parseDouble(num2))
			return num1;
		else
			return num2;
	}

	public static void main(String[] args) throws SQLException {

		SqlRunner.me().setConn(H2Helper.connEmbededDb());
		Analyzer anlyzer = new Analyzer();
		String hcode = new StockSourceDao().getCode("002061", eStockSource.YAHOO);
		List<StockDay> list = new StockAnalysisDao().getDay(hcode, "2015-06-01", null);

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
