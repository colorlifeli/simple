package me.net.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.util.Util;
import me.net.NetType.eStockDayFlag;

/**
 * 中枢相关信息。
 * 
 * 这里的中枢是指连续3笔的公共区域。数字上即连续的 4点 top/bottom的交叉范围：{max(bottom),min(top)}，如果没有交叉（max(bottom)>min(top)）时，则不构成中枢，继续取后面的点。
 * 
 * @author James
 *
 */
public class CentralInfo {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	//中枢列表
	public List<Central> centrals;

	//最新的点，由这些点形成最新的中枢
	//public List<String> points;
	public List<Point> points;

	//标记最后一次 MakeCentral 是否成功
	private boolean isLastSuccess = false;
	
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	//public List<String> pointsHis;

	public CentralInfo() {
		centrals = new ArrayList<Central>();
		points = new ArrayList<Point>();
		//pointsHis = new ArrayList<String>();
	}
	
	public boolean makeNewCentral() {
		return this.makeNewCentral(null);
	}

	/**
	 * 由最新的点判断是否能够形成中枢。
	 * 
	 * 由于下一个点可能是最后一个点的延伸，此时只是一个临时的中枢，只有下一点确定不是延伸时，才能产生最终的中枢。如最后一点是 high，下一点是更高的high，这时中枢就要重构。
	 * 
	 * 20201031: 解决bug: 不能随便执行 makeNewCentral这个函数，否则会影响isLastSuccess的准确性，也就会影响addPoint与reassignPoint的逻辑
	 * 			因此，应只在 addPoint与reassignPoint 后才执行 makeNewCentral（即point变化了才需要重新计算中枢）
	 * @see reMakeCentral()
	 * @return
	 */
	public boolean makeNewCentral(String code) {

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
		if (Double.parseDouble(point1) < Double.parseDouble(point2)) {
			//向上笔
			c.low = Util.max(point1, point3);
			c.high = Util.min(point2, point4);
		} else {
			c.low = Util.max(point2, point4);
			c.high = Util.min(point1, point3);
		}

		boolean result = false;

		if (Double.parseDouble(c.low) < Double.parseDouble(c.high)) {
			//有公共区域，才有中枢

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
				c.startSn = points.get(0).sn;
				c.endSn = points.get(3).sn;
				c.code = code;
				
				//计算本中枢相对于上一个中枢的振幅：(最高-最低)/时间跨度 。因为并不是每一日都会开市，而且还会有停牌，所以通过sn来计算时间跨度
				//20201129  !!! 这个定义有问题，因为第一个中枢有可能长时间震荡，统计其整个横跨的时间长度并不能反映出第二个中枢相对于第一个中枢的变化速度
				// 一时间想不出如何定义才合理。暂时改为 c.endSn-c_pre.endSn
				if (centrals.size() > 0) {
					Central c_pre = centrals.get(centrals.size() - 1);
					if(c.position > c_pre.position)
						//c.degree = (Double.parseDouble(c.high) - Double.parseDouble(c_pre.low))/(c.endSn - c_pre.startSn);
						c.degree = (Double.parseDouble(c.high) - Double.parseDouble(c_pre.high))/(c.endSn - c_pre.endSn);
					else
						//c.degree = (Double.parseDouble(c_pre.high) - Double.parseDouble(c.low))/(c.endSn - c_pre.startSn);
						c.degree = (Double.parseDouble(c_pre.high) - Double.parseDouble(c.high))/(c.endSn - c_pre.endSn);
				}
				
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
	 * 对最后一个点重新赋值
	 */
	public boolean reassignPoint(Point point) {

		points.remove(points.size() - 1);
		points.add(point);

		if (points.size() == 4) {
			//等于4，即上一个点执行过 makeCentral
			if (isLastSuccess) //如果上一个点的 makeCentral成功的话，要将最后一个中枢删除，重新构建 
			{
				//Central c_pre = centrals.get(centrals.size() - 1);
				centrals.remove(centrals.size() - 1);
			}
		}
		
		return makeNewCentral(point.code);
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
	
	/**
	 * 增加一个点
	 */
	public boolean addPoint(Point point) {

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

		points.add(point);
		return makeNewCentral(point.code);
	}
	
	/**
	 * 20201030 所有中枢相关的代码移入此类中
	 * @param day
	 * @param type top 或者 bottom
	 * @return
	 */
	public boolean makeNewCentral(StockDay day, String type) {
		if (type == null)
			return false;

		String value = null;
		String date = format.format(day.date_);
		Point lastP = null;
		if (points.size() > 0)
			lastP = points.get(points.size() - 1);

		if (eStockDayFlag.TOP.toString().equals(type)) {
			value = day.high;
			Point point = new Point(value, date, type, day.sn, 0.0);
			point.code = day.code;
			if (lastP == null)
				return addPoint(point);
			else if (lastP.type.equals(type) && Double.parseDouble(value) > Double.parseDouble(lastP.value)) {
				// 新顶点更高
				return reassignPoint(point);
			} else if (!lastP.type.equals(type) && Double.parseDouble(value) > Double.parseDouble(lastP.value)) {
				// 和上一个分型不一样，则要判断是否符合顶高于底
				return addPoint(point);
			}
		} else if (eStockDayFlag.BOTTOM.toString().equals(type)) {
			value = day.low;
			Point point = new Point(value, date, type, day.sn, 0.0);
			point.code = day.code;
			if (lastP == null)
				return addPoint(point);
			else if (lastP.type.equals(type) && Double.parseDouble(value) < Double.parseDouble(lastP.value)) {
				// 新底更低
				return reassignPoint(point);
			} else if (!lastP.type.equals(type) && Double.parseDouble(value) < Double.parseDouble(lastP.value)) {
				return addPoint(point);
			}
		}

		return false;
	}
	
	public void printCentrals() {
//		for(Central c : centrals) {
		for(int i=0; i<centrals.size(); i++) {
			Central c = centrals.get(i);
			String degree = String.format("%.3f", c.degree);
			if(i == 0)
				logger.debug("中枢：{} ({},{}), position:{}, date:({}, {}), degree: {}", c.code, c.low, c.high, c.position, c.startDate, c.endDate, degree);
			else
				logger.debug("中枢：             ({},{}), position:{}, date:({}, {}), degree: {}", c.low, c.high, c.position, c.startDate, c.endDate, degree);
		}
	}
	
	class Point {
		public Point(String value, String date) {
			this.value = value;
			this.date = date;
		}
		public Point(String value, String date, String type, int sn, double degree) {
			this.value = value;
			this.date = date;
			this.type = type;
			this.sn = sn;
			this.degree = degree;
		}
		String value;
		String date;
		int sn = 0;
		double degree = 0.0;
		String type;
		String code;
	}

}
