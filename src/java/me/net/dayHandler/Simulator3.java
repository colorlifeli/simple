package me.net.dayHandler;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.Config;
import me.common.annotation.IocAnno.Ioc;
import me.net.NetType.eStockDayFlag;
import me.net.NetType.eStockOper;
import me.net.model.CentralInfo;
import me.net.model.StockDay;

/**
 * 模拟处理器
 * 
 * 不更新数据库
 * 
 * @author James
 * 
 *         ******* 使用 CentralInfo2
 *
 */
public class Simulator3 {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private Analyzer analyzer;

	// 已进行分析过的历史数据
	List<StockDay> his = new ArrayList<StockDay>();
	// 中枢信息
	// ****** 尝试使用不同的中枢定义方法：效果不好
	//CentralInfo2 info = new CentralInfo2();
	
	public CentralInfo info = new CentralInfo();

	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	List<Integer> operlist = new ArrayList<Integer>();
	

	/**
	 * 在使用不同的code之前应该执行此方法进行重置
	 */
	public void reset() {
		his.clear();
		info.centrals.clear();
		info.points.clear();
	}

	public eStockOper handle(StockDay day, String operationFunction) {
		if (analyzer.includeOne(his, day)) // 包含关系
			return eStockOper.None;
		// 不需要独立k线，似乎结果更好
		String type = analyzer.recognizeTypeOne(his, day, Config.simulate.isNeedK);
		
		boolean isMakeCentral = false;
		if(type != null)
			isMakeCentral = info.makeNewCentral(his.get(his.size() - 1), type);
		
		// 因为判断包含关系时会修改数据，为了不修改原来的数据，复制一份出来。在recognizeTypeOne中，修改的是his里的最后一个元素
		// 20201026 bug，要在判断完中枢点后才执行，否则会取了顶或底的下一个点的值
		his.add(day.duplicate());

		try {
			Method method = Simulator3.class.getDeclaredMethod(operationFunction, StockDay.class, String.class, boolean.class);
			return (eStockOper)method.invoke(this, day, type, isMakeCentral);
		} catch (Exception e) {
			e.printStackTrace();
			return eStockOper.None;
		} 
//		return this.operation4(day, type, isMakeCentral);
	}
	
	//这个策略在较稳定的时期效果不错，如 2018-4到2020-10，可以有50%的收益率。
	//但在2017年就不好，因为有不少股票下跌很多，过早买入反而容易亏损
	private eStockOper operation5(StockDay day, String type, boolean isMakeCentral) {
		if (!isMakeCentral)
			return eStockOper.None;
		if(isMakeCentral && eStockDayFlag.BOTTOM.toString().equals(type) && info.centrals.get(info.centrals.size() - 1).position < -1) {
			if (info.centrals.size() < 3)
				return eStockOper.None;
			if(info.centrals.get(info.centrals.size() - 2).degree > info.centrals.get(info.centrals.size() - 1).degree)
				return eStockOper.Buy;
		}

//		if(isMakeCentral && eStockDayFlag.TOP.toString().equals(type) && info.centrals.get(info.centrals.size() - 1).position > 0) {
//			return eStockOper.Sell;
//		}
		return eStockOper.None;
	}

	//在2017年效果较好，因为有不少股票下跌很多，在较低点入手会有不错收益。
	//有点奇怪，2017大盘并没有大的变化，2019年1月反而跌得比较低。
	private eStockOper operation4(StockDay day, String type, boolean isMakeCentral) {
		if(isMakeCentral && eStockDayFlag.BOTTOM.toString().equals(type) && info.centrals.get(info.centrals.size() - 1).position == -6) {
			return eStockOper.Buy;
		}

//		if(isMakeCentral && eStockDayFlag.TOP.toString().equals(type) && info.centrals.get(info.centrals.size() - 1).position > 0) {
//			return eStockOper.Sell;
//		}
		return eStockOper.None;
	}

	// 策略：低于中枢最低点则买，高于中枢的最高点则卖。思想：认为中枢的力量较大，底能拉起来
	// 问题：1.中枢会在下一个点，被重建时，这时原最后的中枢就会删除。如果新中枢与上一中枢有重合，又会被删除，这时买入卖出相对的就不是同一个中枢了。
	// 2.如果是下跌趋势，则买入后就很难卖出了.卖也是在下一个比较低的中枢卖，会亏很多钱（买卖也是基于不同的中枢）
	private eStockOper operation(StockDay day, String type, boolean isMakeCentral) {

		eStockOper operation = eStockOper.None;
		if (info.centrals.size() == 0)
			return eStockOper.None;

		if (Double.parseDouble(day.high) < Double.parseDouble(info.centrals.get(info.centrals.size() - 1).low)) {
			operation = eStockOper.Buy;
		} else if (Double.parseDouble(day.low) > Double.parseDouble(info.centrals.get(info.centrals.size() - 1).high)) {
			operation = eStockOper.Sell;
		}

		// if(operation != eStockOper.None)
		// logger.debug(operation.name());
		return operation;
	}
	
	// 策略：在中枢区间内，高于中枢最低点但低于中枢区间则买，低于中枢的最高点但高于中枢区间则卖。思想：认为中枢的力量较大，底能拉起来
	// 失败，结果不好
	private eStockOper operation3(StockDay day, String type, boolean isMakeCentral) {

		eStockOper operation = eStockOper.None;
		if (info.centrals.size() == 0)
			return eStockOper.None;

		if (Double.parseDouble(day.high) > Double.parseDouble(info.centrals.get(info.centrals.size() - 1).low)
				&& Double.parseDouble(day.high) < Double.parseDouble(info.centrals.get(info.centrals.size() - 1).share_low)) {
			operation = eStockOper.Buy;
		} else if (Double.parseDouble(day.low) < Double.parseDouble(info.centrals.get(info.centrals.size() - 1).high)
				&& Double.parseDouble(day.low) > Double.parseDouble(info.centrals.get(info.centrals.size() - 1).share_high)) {
			operation = eStockOper.Sell;
		}

		// if(operation != eStockOper.None)
		// logger.debug(operation.name());
		return operation;
	}

	// ****** Simulator的handler2策略。
	// ****** 因为使用了新的中枢定义方法，这个策略不能使用了
	@Deprecated
	private eStockOper operation2(StockDay day, String type, boolean isMakeCentral) {
		eStockOper operation = eStockOper.None;

		if (!isMakeCentral && info.centrals.size() > 0) { // 非产生中枢时

			int pos = info.centrals.get(info.centrals.size() - 1).position;

			// 正负交替，比前一个负更低时
			if (pos == -1 && type.equals(eStockDayFlag.BOTTOM.toString()) && Double.parseDouble(day.high) < Double
					.parseDouble(info.centrals.get(info.centrals.size() - 1).low)) {
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

}
