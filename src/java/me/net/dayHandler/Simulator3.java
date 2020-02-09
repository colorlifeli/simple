package me.net.dayHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.Config;
import me.common.annotation.IocAnno.Ioc;
import me.net.NetType.eStockDayFlag;
import me.net.NetType.eStockOper;
import me.net.model.CentralInfo2;
import me.net.model.StockDay;

/**
 * 模拟处理器
 * 
 * 不更新数据库
 * 
 * @author James
 * 
 * ******* 使用 CentralInfo2
 *
 */
public class Simulator3 {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private Analyzer analyzer;

	//记录所有符合笔要求的点
	public Stack<Point> points = new Stack<Point>();
	// 已进行分析过的历史数据
	List<StockDay> his = new ArrayList<StockDay>();
	//中枢信息
	CentralInfo2 info = new CentralInfo2();
	
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 在使用不同的code之前应该执行此方法进行重置
	 */
	public void reset() {
		points.clear();
		his.clear();
		info.centrals.clear();
		info.points.clear();
	}

	public eStockOper handle(StockDay day) {
		if (analyzer.includeOne(his, day)) //包含关系 
			return eStockOper.None;
		// 不需要独立k线，似乎结果更好
		String type = analyzer.recognizeTypeOne(his, day, Config.simulate.isNeedK);
		// 因为判断包含关系时会修改数据，为了不修改原来的数据，复制一份出来
		his.add(day.duplicate());

		if (type == null)
			return eStockOper.None; // ******** 只在顶底点进行买卖

		return this.operation2(day, type, this.central(day, type));
	}
	
	//策略：在中枢区间内，底则买，顶则卖。思想：认为中枢的力量较大，底能拉起来
	private eStockOper operation(StockDay day, String type, boolean isMakeCentral) {

		eStockOper operation = eStockOper.None;
		if (isMakeCentral == false && eStockDayFlag.BOTTOM.toString().equals(type) && info.centrals.size() > 0) {
			// 中枢后面的底，认为会有上升的机会。
			if (Double.parseDouble(day.high) < Double.parseDouble(info.centrals.get(info.centrals.size() - 1).high)) {
				// 还没达到中枢高点
				operation = eStockOper.Buy;
			}
		} else if (isMakeCentral == false && eStockDayFlag.TOP.toString().equals(type) && info.centrals.size() > 0) {
			// 中枢后面的底，认为会有上升的机会。
			if (Double.parseDouble(day.low) > Double.parseDouble(info.centrals.get(info.centrals.size() - 1).low)) {
				// 还没达到中枢高点
				operation = eStockOper.Sell;
			}
		}

		// if(operation != eStockOper.None)
		// logger.debug(operation.name());
		return operation;
	}
	
	// ****** Simulator的handler2策略。
	private eStockOper operation2(StockDay day, String type, boolean isMakeCentral) {
		eStockOper operation = eStockOper.None;
		
		if (!isMakeCentral && info.centrals.size() > 0) { // 非产生中枢时
			
			int pos = info.centrals.get(info.centrals.size() - 1).position;

			// 正负交替，比前一个负更低时
			if (pos == -1 && type.equals(eStockDayFlag.BOTTOM.toString()) && Double.parseDouble(day.high) < Double
					.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
			) {
				if (info.centrals.size() > 2) {
					for (int i = info.centrals.size() - 2; i > 0; i--) {
						int tmp_pos = info.centrals.get(i).position;
						if (tmp_pos < 0)
							if (Double.parseDouble(info.centrals.get(info.centrals.size() - 1).low) < Double
									.parseDouble(info.centrals.get(i).low)) {

								operation = eStockOper.Buy;
							} else
								break;
					}
				}
			}
		}
		
		return operation;
	}

	//计算中枢, 返回是否成功生成中枢
	private boolean central(StockDay day, String type) {
		Point lastP = null;
		if (points.size() > 0)
			lastP = points.peek();

		Point point = new Point();
		point.type = type;
		point.sn = his.get(his.size() - 1).sn;

		if (eStockDayFlag.TOP.toString().equals(type)) {
			//同为顶点，选取高的那个，去除另一个
			point.value = his.get(his.size() - 1).high;
			if (lastP == null) {
				points.push(point);

				info.addPoint(point.value, format.format(day.date_));
			} else if (lastP.type.equals(type)
					&& Double.parseDouble(point.value) > Double.parseDouble(lastP.value)) {
				//新顶点更高
				points.pop();
				points.push(point);

				info.reassignPoint(point.value, format.format(day.date_));
			} else if (!lastP.type.equals(type)
					&& Double.parseDouble(point.value) > Double.parseDouble(lastP.value)) {
				//和上一个分型不一样，则要判断是否符合顶高于底
				points.push(point);

				info.addPoint(point.value, format.format(day.date_));
			}

		} else if (eStockDayFlag.BOTTOM.toString().equals(type)) {
			point.value = his.get(his.size() - 1).low;
			if (lastP == null) {
				points.push(point);

				info.addPoint(point.value, format.format(day.date_));
			} else if (lastP.type.equals(type)
					&& Double.parseDouble(point.value) < Double.parseDouble(lastP.value)) {
				//新底更低
				points.pop();
				points.push(point);

				info.reassignPoint(point.value, format.format(day.date_));
			} else if (!lastP.type.equals(type)
					&& Double.parseDouble(point.value) < Double.parseDouble(lastP.value)) {
				//和上一个分型不一样，则要判断是否符合顶高于底
				points.push(point);

				info.addPoint(point.value, format.format(day.date_));
			}
		}

//		logger.debug("point:{}, {}, {}, {}, {}", point.value, point.type, point.sn,
//				String.format("%.2f", point.degree), day.date_);

		return info.makeNewCentral();

	}

	class Point {
		String value = "0";
		String type = ""; //是顶还是底
		int sn = 0;
		double degree = 0.0; //角度，变化值/间隔
	}

}
