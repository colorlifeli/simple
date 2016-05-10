package me.net.dayHandler;

import java.util.List;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.Constant;
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
 */
public class Simulator {

	@Ioc
	private Analyzer analyzer;

	public eStockOper handle(StockDay day, List<StockDay> his) {

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

	public eStockOper handle(StockDay day, List<StockDay> his, CentralInfo info) {

		if (!analyzer.includeOne(his, day)) {
			//不需要独立k线，似乎结果更好
			String type = analyzer.recognizeTypeOne(his, day, Constant.simulate.isNeedK);

			boolean result = false;
			if (eStockDayFlag.TOP.toString().equals(type)) {
				result = analyzer.makeCentral(info, his.get(his.size() - 1).high);
			} else if (eStockDayFlag.BOTTOM.toString().equals(type)) {
				result = analyzer.makeCentral(info, his.get(his.size() - 1).low);
			}
			his.add(day);

			//由于间隔太近，将本次与上次的分型都删除
			if ("deleteLast".equals(type)) {
				if (info.points.size() == 1 && info.centrals.size() > 0) {
					//上一个点是中枢的构成点，且是中枢的最后一个构成点，则需要把中枢拆了
					info.centrals.remove(info.centrals.size() - 1);
					info.points.clear();
					info.points.addAll(info.pointsHis);
					info.points.remove(3);//删除最后一个点
				} else if (info.points.size() > 1)
					return eStockOper.None;
			}

			//如果本次形成了一个新中枢

			//不论是否形成中枢
			if (type != null && info.centrals.size() > 0) {
				int pos = info.centrals.get(info.centrals.size() - 1).position;
				//趋势向下，且当前的分型是顶分型，则看顶是否大于中枢，是则卖.
				//暂改为今天是否大于中枢，是则卖
				if (pos > 0
						&& type.equals(eStockDayFlag.TOP.toString())
						&& Double.parseDouble(day.high) > Double
								.parseDouble(info.centrals.get(info.centrals.size() - 1).high)) {
					return eStockOper.Sell;
				}
				if (pos < 0
						&& type.equals(eStockDayFlag.BOTTOM.toString())
						&& Double.parseDouble(day.low) < Double
								.parseDouble(info.centrals.get(info.centrals.size() - 1).low)) {
					return eStockOper.Buy;
				}
			}

			//			if (type == null)
			//				return eStockOper.None;
			//			if (type.equals(eStockDayFlag.TOP.toString()))
			//				return eStockOper.Sell;
			//			if (type.equals(eStockDayFlag.BOTTOM.toString()))
			//				return eStockOper.Buy;

		}
		return eStockOper.None;
	}

}
