package me.net.dayHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.Constant;
import me.net.NetType.eStockDayFlag;
import me.net.NetType.eStockOper;
import me.net.model.CentralInfo;
import me.net.model.StockDay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模拟处理器
 * 
 * 不更新数据库
 * 
 * @author James
 *
 */
public class Simulator2 {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private Analyzer analyzer;

	//记录所有符合笔要求的点
	public Stack<Point> points = new Stack<Point>();
	// 已进行分析过的历史数据
	List<StockDay> his = new ArrayList<StockDay>();
	//中枢信息
	public CentralInfo info = new CentralInfo();
	
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

	public eStockOper handle2(StockDay day) {
		return handle(day, null);
	}
	public eStockOper handle(StockDay day, StockDay nextDay) {
		eStockOper operation = eStockOper.None;
		if (!analyzer.includeOne(his, day)) {
			//不需要独立k线，似乎结果更好
			String type = analyzer.recognizeTypeOne(his, day, Constant.simulate.isNeedK);

			//对分型进行处理，必须是顶底交叉，从而可以形成笔
			if (type != null) {

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
						
						//logger.debug("has new low point");
					} else if (!lastP.type.equals(type)
							&& Double.parseDouble(point.value) < Double.parseDouble(lastP.value)) {
						//和上一个分型不一样，则要判断是否符合顶高于底
						points.push(point);

						info.addPoint(point.value, format.format(day.date_));
					}
				}

//				logger.debug("point:{}, {}, {}, {}, {}", point.value, point.type, point.sn,
//						String.format("%.2f", point.degree), day.date_);
			}

			boolean result = false;
			if (eStockDayFlag.TOP.toString().equals(type) || eStockDayFlag.BOTTOM.toString().equals(type)) {
				result = info.makeNewCentral();
			}

			his.add(day);

			if (result && type != null && info.centrals.size() > 0) {
				int pos = info.centrals.get(info.centrals.size() - 1).position;
				//趋势向下，且当前的分型是顶分型，则看顶是否大于中枢，是则卖.
				//暂改为今天是否大于中枢，是则以中枢顶点价格卖
				//				if (pos > 0
				//						//&& type.equals(eStockDayFlag.TOP.toString())
				//						&& Double.parseDouble(day.high) > Double
				//								.parseDouble(info.centrals.get(info.centrals.size() - 1).high)) {
				//					operation = eStockOper.Sell;
				//
				//				}
				if (pos < -1
				&& type.equals(eStockDayFlag.BOTTOM.toString())
//						&& Double.parseDouble(nextDay.low) < Double
//								.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
						) {
//					operation = eStockOper.Buy;
//					nextDay.flag = info.centrals.get(info.centrals.size() - 1).low;

				}
				
				//正负交替，比前一个负更低时
				if (pos == -1 && type.equals(eStockDayFlag.BOTTOM.toString())
				//&& Double.parseDouble(day.low) < Double
				//		.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
				) {
//					if (info.centrals.size() > 2) {
//						for (int i = info.centrals.size() - 2; i > 0; i--) {
//							int tmp_pos = info.centrals.get(i).position;
//							if (tmp_pos < 0)
//								if (Double.parseDouble(info.centrals.get(info.centrals.size()-1).low) < Double.parseDouble(info.centrals.get(i).low)) {
//
//									//operation = eStockOper.Buy;
//								} else
//									break;
//						}
//					}
				}
				
				//卖
				if (pos > 1
						&& type.equals(eStockDayFlag.TOP.toString())
								//&& Double.parseDouble(day.low) < Double
								//		.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
								) {
							//operation = eStockOper.Sell;

						}
				if (pos == 1 && type.equals(eStockDayFlag.TOP.toString())
						//&& Double.parseDouble(day.low) < Double
						//		.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
						) {
//							if (info.centrals.size() > 2) {
//								for (int i = info.centrals.size() - 2; i > 0; i--) {
//									int tmp_pos = info.centrals.get(i).position;
//									if (tmp_pos > 0)
//										if (Double.parseDouble(info.centrals.get(info.centrals.size()-1).high) > Double.parseDouble(info.centrals.get(i).high)) {
//
//											operation = eStockOper.Sell;
//										} else
//											break;
//								}
//							}
						}

				
			} else if(type != null && info.centrals.size() > 0){ //非产生中枢时
				//没有产生中枢，但如果是底且比中枢的底还低，则买入
				int pos = info.centrals.get(info.centrals.size() - 1).position;
				if (pos < -1
					&& type.equals(eStockDayFlag.BOTTOM.toString())
							&& Double.parseDouble(day.high) < Double
									.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
							) {
					operation = eStockOper.Buy;
				}
				

				//正负交替，比前一个负更低时
				if (pos == -1 
						&& type.equals(eStockDayFlag.BOTTOM.toString()) 
						&& Double.parseDouble(day.high) < Double
						.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
				//&& Double.parseDouble(day.low) < Double
				//		.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
				) {
//					if (info.centrals.size() > 2) {
//						for (int i = info.centrals.size() - 2; i > 0; i--) {
//							int tmp_pos = info.centrals.get(i).position;
//							if (tmp_pos < 0)
//								if (Double.parseDouble(info.centrals.get(info.centrals.size() - 1).low) < Double
//										.parseDouble(info.centrals.get(i).low)) {
//
//									operation = eStockOper.Buy;
//								} else
//									break;
//						}
//					}
				}
			}

		}
		return operation;
	}


	class Point {
		String value = "0";
		String type = ""; //是顶还是底
		int sn = 0;
		double degree = 0.0; //角度，变化值/间隔
	}

}
