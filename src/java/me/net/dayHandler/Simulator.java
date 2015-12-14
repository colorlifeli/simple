package me.net.dayHandler;

import java.util.List;

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

	public int handle(StockDay day, List<StockDay> his, int has) {

		return has;
	}

}
