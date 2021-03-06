package me.net.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.util.Util;

/**
 * 中枢相关信息。
 * 
 * 这里的中枢是指连续3笔的公共区域。数字上即连续的 4点 top/bottom的交叉范围：{max(bottom),min(top)}，如果没有交叉（max(bottom)>min(top)）时，则不构成中枢，继续取后面的点。
 * 
 * @author James
 * 
 * ****** 与CentralInfo区别：CentralInfo用3条线段的公共区域作为中枢；CentralInfo2用最高点和最低点作为中枢。
 * 2020725 增加记录中枢的公共区域
 *
 */
public class CentralInfo2 {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	//中枢列表
	public List<Central> centrals;

	//最新的点，由这些点形成最新的中枢
	//public List<String> points;
	public List<Point> points;

	//标记最后一次 MakeCentral 是否成功
	private boolean isLastSuccess = false;

	//public List<String> pointsHis;

	public CentralInfo2() {
		centrals = new ArrayList<Central>();
		points = new ArrayList<Point>();
		//pointsHis = new ArrayList<String>();
	}

	/**
	 * 由最新的点判断是否能够形成中枢。
	 * 
	 * 由于下一个点可能是最后一个点的延伸，此时只是一个临时的中枢，只有下一点确定不是延伸时，才能产生最终的中枢。如最后一点是 high，下一点是更高的high，这时中枢就要重构。
	 * @see reMakeCentral()
	 * @return
	 */
	public boolean makeNewCentral() {

		if (points.size() < 4) {
			//4个点，构成3笔，才能计算中枢
			return false;
		}

		String point1 = points.get(0).value;
		String point2 = points.get(1).value;
		String point3 = points.get(2).value;
		String point4 = points.get(3).value;

		//logger.debug("{},{},{},{}", point1, point2, point3, point4);

		Central c = new Central();
		String high = "";
		String low = "";
		if (Double.parseDouble(point1) < Double.parseDouble(point2)) {
			//向上笔
			c.low = Util.max(point1, point3);
			c.high = Util.min(point2, point4);
			high = Util.max(point2, point4);
			low = Util.min(point1, point3);
		} else {
			c.low = Util.max(point2, point4);
			c.high = Util.min(point1, point3);
			high = Util.max(point1, point3);
			low = Util.min(point2, point4);
		}

		boolean result = false;

		if (Double.parseDouble(c.low) < Double.parseDouble(c.high)) {
			//有公共区域，才有中枢
			//****20200725 先记录公共区域
			c.share_high = c.high;
			c.share_low = c.low;
			//****20200205  重设中枢，不是公共区域，而是最高点与最低之间的区域
			c.high = high;
			c.low = low;

			//这是第一个中枢
			if (centrals.size() == 0) {
				c.position = 0;
				result = true;
			} else {
				//如果前面已经有中枢，则要与最后一个中枢进行比较
				Central c_pre = centrals.get(centrals.size() - 1);
				if (Double.parseDouble(c.low) > Double.parseDouble(c_pre.high)) {
					//趋势向上。记录第几次向上
					c.position = c_pre.position > 0 ? c_pre.position + 1 : 1;
					result = true;
				} else if (Double.parseDouble(c.high) < Double.parseDouble(c_pre.low)) {
					//趋势向下
					c.position = c_pre.position < 0 ? c_pre.position - 1 : -1;
					result = true;
				}
			}
			if (result) {
				c.startDate = points.get(0).date;
				c.endDate = points.get(3).date;
				centrals.add(c);

//				logger.debug("中枢：({},{}), position:{}", c.low, c.high, c.position);
			}

		}

		isLastSuccess = result;

		return result;
	}

	/**
	 * 对最后一个点重新赋值
	 * @param value
	 */
	public void reassignPoint(String value, String date) {

		points.remove(points.size() - 1);
		points.add(new Point(value, date));

		if (points.size() == 4) {
			//等于4，即上一个点执行过 makeCentral
			if (isLastSuccess) //如果上一个点的 makeCentral成功的话，要将最后一个中枢删除，重新构建 
			{
				//Central c_pre = centrals.get(centrals.size() - 1);
				centrals.remove(centrals.size() - 1);
			}
		}
	}

	/**
	 * 增加一个点
	 * @param value
	 */
	public void addPoint(String value, String date) {

		if (points.size() == 4) {
			//等于4，即上一个点执行过 makeCentral
			if (isLastSuccess) {
				//上一个点的 makeCentral成功,以上一中枢最后一点作为下一个中枢的起点
				//String last = points.get(points.size() - 1);
				Point last = points.get(points.size() - 1);
				points.clear();
				points.add(last);

			} else {
				//上一个点的 makeCentral失败，则删除上一中枢的第一个点，剩余的3个点和后面加入的点继续尝试创建中枢
				points.remove(0);
			}
		}

		points.add(new Point(value, date));
	}
	
	public void printCentrals() {
		for(Central c : centrals) {
			logger.debug("中枢：({},{}), position:{}, date:({}, {})", c.low, c.high, c.position, c.startDate, c.endDate);
		}
	}
	
	class Point {
		public Point(String value, String date) {
			this.value = value;
			this.date = date;
		}
		String value;
		String date;
	}

}
