package me.net.dayHandler;

import java.util.List;

import me.common.annotation.IocAnno.Ioc;
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

		analyzer.includeOne(his, day);
		analyzer.recognizeType(his);
		return eStockOper.Buy;
	}

}
