package me.net.dayHandler;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.Constant;
import me.net.NetType.eStockDayFlag;
import me.net.NetType.eStockOper;
import me.net.model.Central;
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
public class Simulator {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private Analyzer analyzer;

	//记录所有符合笔要求的点
	public Stack<Point> points = new Stack<Point>();
	// 已进行分析过的历史数据
	List<StockDay> his = new ArrayList<StockDay>();
	//中枢信息
	CentralInfo info = new CentralInfo();
	
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

	public eStockOper handle_old(StockDay day) {

		if (!analyzer.includeOne(his, day)) {
			//不需要独立k线，似乎结果更好
			String type = analyzer.recognizeTypeOne(his, day, Constant.simulate.isNeedK);
			his.add(day);

			if (type == null)
				return eStockOper.None;
			if (type.equals(eStockDayFlag.TOP.toString()))
				return eStockOper.Sell;
			if (type.equals(eStockDayFlag.BOTTOM.toString()))
				return eStockOper.Buy;

		}
		return eStockOper.None;
	}

	public eStockOper handle2(StockDay day) {
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

			//因为判断包含关系时会修改数据，为了不修改原来的数据，复制一份出来
			his.add(day.duplicate()); 
			
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
				if (pos < -2
				&& type.equals(eStockDayFlag.BOTTOM.toString())
						//&& Double.parseDouble(day.low) < Double
						//		.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
						) {
					//operation = eStockOper.Buy;

				}
				
				//正负交替，比前一个负更低时
				if (pos == -1 && type.equals(eStockDayFlag.BOTTOM.toString())
				//&& Double.parseDouble(day.low) < Double
				//		.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
				) {
					if (info.centrals.size() > 2) {
						for (int i = info.centrals.size() - 2; i > 0; i--) {
							int tmp_pos = info.centrals.get(i).position;
							if (tmp_pos < 0)
								if (Double.parseDouble(info.centrals.get(info.centrals.size()-1).low) < Double.parseDouble(info.centrals.get(i).low)) {

									//operation = eStockOper.Buy;
								} else
									break;
						}
					}
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
							if (info.centrals.size() > 2) {
								for (int i = info.centrals.size() - 2; i > 0; i--) {
									int tmp_pos = info.centrals.get(i).position;
									if (tmp_pos > 0)
										if (Double.parseDouble(info.centrals.get(info.centrals.size()-1).high) > Double.parseDouble(info.centrals.get(i).high)) {

											//operation = eStockOper.Sell;
										} else
											break;
								}
							}
						}

				
			} else if(type != null && info.centrals.size() > 0){ //非产生中枢时
				//没有产生中枢，但如果是底且比中枢的底还低，则买入
				int pos = info.centrals.get(info.centrals.size() - 1).position;
				if (pos < -1
					&& type.equals(eStockDayFlag.BOTTOM.toString())
							&& Double.parseDouble(day.high) < Double
									.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
							) {
					//operation = eStockOper.Buy;
				}
				

				//正负交替，比前一个负更低时
				if (pos == -1 
						&& type.equals(eStockDayFlag.BOTTOM.toString()) 
						&& Double.parseDouble(day.high) < Double
						.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
				//&& Double.parseDouble(day.low) < Double
				//		.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
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

		}
		return operation;
	}

	public eStockOper handle(StockDay day, StockDay nextDay, StockDay tmp) {

		eStockOper operation = eStockOper.None;
		if (!analyzer.includeOne(his, day)) {
			//不需要独立k线，似乎结果更好
			String type = analyzer.recognizeTypeOne(his, day, Constant.simulate.isNeedK);

			//对分型进行处理，必须是顶底交叉，从而可以形成笔
			if (type != null) {

				//尝试在上升时的下一个顶点再操作
				if (info.centrals.size() > 0) {
					Central c_pre = info.centrals.get(info.centrals.size() - 1);
					if (c_pre.position > 0 && type.equals(eStockDayFlag.TOP.toString())
							&& Double.parseDouble(day.high) > Double.parseDouble(c_pre.high)
							&& Double.parseDouble(nextDay.high) > Double.parseDouble(c_pre.high)) {
						operation = eStockOper.Sell;
						tmp.high = c_pre.high;
						if (Double.parseDouble(nextDay.low) > Double.parseDouble(c_pre.high))
							tmp.high = new BigDecimal(nextDay.high).add(new BigDecimal(nextDay.low))
									.divide(new BigDecimal(2)).toString();

					} else if (c_pre.position < 0 && type.equals(eStockDayFlag.BOTTOM.toString())
							&& Double.parseDouble(day.low) < Double.parseDouble(c_pre.low)
							&& Double.parseDouble(nextDay.low) < Double.parseDouble(c_pre.low)) {
						operation = eStockOper.Buy;
						tmp.low = c_pre.low;
						if (Double.parseDouble(nextDay.high) < Double.parseDouble(c_pre.low))
							tmp.low = new BigDecimal(nextDay.high).add(new BigDecimal(nextDay.low))
									.divide(new BigDecimal(2)).toString();
					}
				}

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

						// **** info.points.add(point.value);
					} else if (lastP.type.equals(type)
							&& Double.parseDouble(point.value) > Double.parseDouble(lastP.value)) {
						//新顶点更高
						points.pop();
						points.push(point);

						info.points.remove(info.points.size() - 1);
						// **** info.points.add(point.value);
					} else if (!lastP.type.equals(type)
							&& Double.parseDouble(point.value) > Double.parseDouble(lastP.value)) {
						//和上一个分型不一样，则要判断是否符合顶高于底
						points.push(point);

						// **** info.points.add(point.value);
					}

				} else if (eStockDayFlag.BOTTOM.toString().equals(type)) {
					point.value = his.get(his.size() - 1).low;
					if (lastP == null) {
						points.push(point);

						// **** info.points.add(point.value);
					} else if (lastP.type.equals(type)
							&& Double.parseDouble(point.value) < Double.parseDouble(lastP.value)) {
						//新底更低
						points.pop();
						points.push(point);

						info.points.remove(info.points.size() - 1);
						// **** info.points.add(point.value);
					} else if (!lastP.type.equals(type)
							&& Double.parseDouble(point.value) < Double.parseDouble(lastP.value)) {
						//和上一个分型不一样，则要判断是否符合顶高于底
						points.push(point);

						// **** info.points.add(point.value);
					}
				}

				if (lastP != null) {
					point.degree = Math.abs((Double.parseDouble(point.value) - Double.parseDouble(lastP.value))
							/ (point.sn - lastP.sn));
				}

				//logger.debug("point:{},{},{},{}", point.value, point.type, point.sn,
				//		String.format("%.2f", point.degree));
			}

			boolean result = false;
			if (eStockDayFlag.TOP.toString().equals(type)) {
				result = analyzer.makeCentral(info);
			} else if (eStockDayFlag.BOTTOM.toString().equals(type)) {
				result = analyzer.makeCentral(info);
			}
			his.add(day);

			//			//由于间隔太近，顶和底共享了k线，不能构成笔
			//			if ("noK".equals(type)) {
			//				if (info.points.size() == 1 && info.centrals.size() > 0) {
			//					//上一个点是中枢的构成点，且是中枢的最后一个构成点，则需要把中枢拆了
			//					info.centrals.remove(info.centrals.size() - 1);
			//					info.points.clear();
			//					info.points.addAll(info.pointsHis);
			//					info.points.remove(3);//删除最后一个点
			//				} else if (info.points.size() > 1)
			//					return eStockOper.None;
			//			}

			//如果本次形成了一个新中枢

			//不论是否形成中枢
			if (result && type != null && info.centrals.size() > 0) {
				int pos = info.centrals.get(info.centrals.size() - 1).position;
				//				//趋势向下，且当前的分型是顶分型，则看顶是否大于中枢，是则卖.
				//				//暂改为今天是否大于中枢，是则卖
				//				if (pos > 0
				//						&& type.equals(eStockDayFlag.TOP.toString())
				//						&& Double.parseDouble(day.low) > Double
				//								.parseDouble(info.centrals.get(info.centrals.size() - 1).high)) {
				//					return eStockOper.Sell;
				//
				//				}
				//				if (pos < 0
				//						&& type.equals(eStockDayFlag.BOTTOM.toString())
				//						&& Double.parseDouble(day.high) < Double
				//								.parseDouble(info.centrals.get(info.centrals.size() - 1).low)) {
				//					return eStockOper.Buy;
				//
				//				}

				//只要倾斜度趋缓，认为是背驰,就执行操作

				//第1种判断方法：
				//卖：找到最近的顶点，与前一顶点进行degree比较
				if (pos > 1 && type.equals(eStockDayFlag.TOP.toString())) {
					//					for (int i = points.size() - 1; i >= 0; i--) {
					//						if (eStockDayFlag.TOP.toString().equals(points.get(i).type)) {
					//							if (points.get(i).degree < points.get(i - 2).degree)
					//								return eStockOper.Sell;
					//						}
					//					}
					//return eStockOper.Sell;
				}
				//买：找到最近的底，与前一底进行degree比较
				if (pos < -1 && type.equals(eStockDayFlag.BOTTOM.toString())) {
					//					for (int i = points.size() - 1; i >= 0; i--) {
					//						if (eStockDayFlag.BOTTOM.toString().equals(points.get(i).type)) {
					//							if (points.get(i).degree < points.get(i - 2).degree)
					//								return eStockOper.Buy;
					//						}
					//					}
					//return eStockOper.Buy;
				}
			}

			//			if (type != null && info.centrals.size() > 0) {
			//				int pos = info.centrals.get(info.centrals.size() - 1).position;
			//				//趋势向下，且当前的分型是顶分型，则看顶是否大于中枢，是则卖.
			//				//暂改为今天是否大于中枢，是则卖
			//
			//				//最小要大于中枢，即完全在中枢之外
			//				if (pos > 0
			//						&& type.equals(eStockDayFlag.TOP.toString())
			//						&& Double.parseDouble(day.low) > Double
			//								.parseDouble(info.centrals.get(info.centrals.size() - 1).high)) {
			//					return eStockOper.Sell;
			//				}
			//				if (pos < 0
			//						&& type.equals(eStockDayFlag.BOTTOM.toString())
			//						&& Double.parseDouble(day.high) < Double
			//								.parseDouble(info.centrals.get(info.centrals.size() - 1).low)) {
			//					return eStockOper.Buy;
			//				}
			//			}

			//			if (type == null)
			//				return eStockOper.None;
			//			if (type.equals(eStockDayFlag.TOP.toString()))
			//				return eStockOper.Sell;
			//			if (type.equals(eStockDayFlag.BOTTOM.toString()))
			//				return eStockOper.Buy;

		}
		return operation;
	}

	public eStockOper handle(StockDay day) {

		eStockOper operation = eStockOper.None;
		if (!analyzer.includeOne(his, day)) {
			//不需要独立k线，似乎结果更好
			String type = analyzer.recognizeTypeOne(his, day, Constant.simulate.isNeedK);

			//对分型进行处理，必须是顶底交叉，从而可以形成笔
			if (type != null) {

				//尝试在上升时的下一个顶点再操作
				if (info.centrals.size() > 0) {
					Central c_pre = info.centrals.get(info.centrals.size() - 1);
					if (c_pre.position > 0 && type.equals(eStockDayFlag.TOP.toString())
							&& Double.parseDouble(day.low) > Double.parseDouble(c_pre.high)) {
						operation = eStockOper.Sell;
					} else if (c_pre.position < 0 && type.equals(eStockDayFlag.BOTTOM.toString())
							&& Double.parseDouble(day.high) < Double.parseDouble(c_pre.low)) {
						operation = eStockOper.Buy;
					}
				}

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

						// **** info.points.add(point.value);
					} else if (lastP.type.equals(type)
							&& Double.parseDouble(point.value) > Double.parseDouble(lastP.value)) {
						//新顶点更高
						points.pop();
						points.push(point);

						info.points.remove(info.points.size() - 1);
						// **** info.points.add(point.value);
					} else if (!lastP.type.equals(type)
							&& Double.parseDouble(point.value) > Double.parseDouble(lastP.value)) {
						//和上一个分型不一样，则要判断是否符合顶高于底
						points.push(point);

						// **** info.points.add(point.value);
					}

				} else if (eStockDayFlag.BOTTOM.toString().equals(type)) {
					point.value = his.get(his.size() - 1).low;
					if (lastP == null) {
						points.push(point);

						// **** info.points.add(point.value);
					} else if (lastP.type.equals(type)
							&& Double.parseDouble(point.value) < Double.parseDouble(lastP.value)) {
						//新底更低
						points.pop();
						points.push(point);

						info.points.remove(info.points.size() - 1);
						// **** info.points.add(point.value);
					} else if (!lastP.type.equals(type)
							&& Double.parseDouble(point.value) < Double.parseDouble(lastP.value)) {
						//和上一个分型不一样，则要判断是否符合顶高于底
						points.push(point);

						// **** info.points.add(point.value);
					}
				}

				if (lastP != null) {
					point.degree = Math.abs((Double.parseDouble(point.value) - Double.parseDouble(lastP.value))
							/ (point.sn - lastP.sn));
				}

				//logger.debug("point:{},{},{},{}", point.value, point.type, point.sn,
				//		String.format("%.2f", point.degree));
			}

			boolean result = false;
			if (eStockDayFlag.TOP.toString().equals(type)) {
				result = analyzer.makeCentral(info);
			} else if (eStockDayFlag.BOTTOM.toString().equals(type)) {
				result = analyzer.makeCentral(info);
			}
			his.add(day);

			//			//由于间隔太近，顶和底共享了k线，不能构成笔
			//			if ("noK".equals(type)) {
			//				if (info.points.size() == 1 && info.centrals.size() > 0) {
			//					//上一个点是中枢的构成点，且是中枢的最后一个构成点，则需要把中枢拆了
			//					info.centrals.remove(info.centrals.size() - 1);
			//					info.points.clear();
			//					info.points.addAll(info.pointsHis);
			//					info.points.remove(3);//删除最后一个点
			//				} else if (info.points.size() > 1)
			//					return eStockOper.None;
			//			}

			//如果本次形成了一个新中枢

			//不论是否形成中枢
			if (result && type != null && info.centrals.size() > 0) {
				int pos = info.centrals.get(info.centrals.size() - 1).position;
				//				//趋势向下，且当前的分型是顶分型，则看顶是否大于中枢，是则卖.
				//				//暂改为今天是否大于中枢，是则卖
				//				if (pos > 0
				//						&& type.equals(eStockDayFlag.TOP.toString())
				//						&& Double.parseDouble(day.low) > Double
				//								.parseDouble(info.centrals.get(info.centrals.size() - 1).high)) {
				//					return eStockOper.Sell;
				//
				//				}
				//				if (pos < 0
				//						&& type.equals(eStockDayFlag.BOTTOM.toString())
				//						&& Double.parseDouble(day.high) < Double
				//								.parseDouble(info.centrals.get(info.centrals.size() - 1).low)) {
				//					return eStockOper.Buy;
				//
				//				}

				//只要倾斜度趋缓，认为是背驰,就执行操作

				//第1种判断方法：
				//卖：找到最近的顶点，与前一顶点进行degree比较
				if (pos > 1 && type.equals(eStockDayFlag.TOP.toString())) {
					//					for (int i = points.size() - 1; i >= 0; i--) {
					//						if (eStockDayFlag.TOP.toString().equals(points.get(i).type)) {
					//							if (points.get(i).degree < points.get(i - 2).degree)
					//								return eStockOper.Sell;
					//						}
					//					}
					//return eStockOper.Sell;
				}
				//买：找到最近的底，与前一底进行degree比较
				if (pos < -1 && type.equals(eStockDayFlag.BOTTOM.toString())) {
					//					for (int i = points.size() - 1; i >= 0; i--) {
					//						if (eStockDayFlag.BOTTOM.toString().equals(points.get(i).type)) {
					//							if (points.get(i).degree < points.get(i - 2).degree)
					//								return eStockOper.Buy;
					//						}
					//					}
					//return eStockOper.Buy;
				}
			}

			//			if (type != null && info.centrals.size() > 0) {
			//				int pos = info.centrals.get(info.centrals.size() - 1).position;
			//				//趋势向下，且当前的分型是顶分型，则看顶是否大于中枢，是则卖.
			//				//暂改为今天是否大于中枢，是则卖
			//
			//				//最小要大于中枢，即完全在中枢之外
			//				if (pos > 0
			//						&& type.equals(eStockDayFlag.TOP.toString())
			//						&& Double.parseDouble(day.low) > Double
			//								.parseDouble(info.centrals.get(info.centrals.size() - 1).high)) {
			//					return eStockOper.Sell;
			//				}
			//				if (pos < 0
			//						&& type.equals(eStockDayFlag.BOTTOM.toString())
			//						&& Double.parseDouble(day.high) < Double
			//								.parseDouble(info.centrals.get(info.centrals.size() - 1).low)) {
			//					return eStockOper.Buy;
			//				}
			//			}

			//			if (type == null)
			//				return eStockOper.None;
			//			if (type.equals(eStockDayFlag.TOP.toString()))
			//				return eStockOper.Sell;
			//			if (type.equals(eStockDayFlag.BOTTOM.toString()))
			//				return eStockOper.Buy;

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
