package me.net.dayHandler;

import java.util.List;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.Constant;
import me.net.NetType.eStockDayFlag;
import me.net.NetType.eStockOper;
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

}
