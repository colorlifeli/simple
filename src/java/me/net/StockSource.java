package me.net;

import java.util.List;

/**
 * stock 数据源
 * 
 * 产生stock所有数据，包括实时和历史数据。这是一个虚拟的数据源，从一个或多个地方获取相应的数据，这些地方称为数据提供者。
 * @author James
 *
 */
public interface StockSource {

	/**
	 * 获取某些 code 的实时数据
	 * @param codes
	 */
	public void getRealTime(List<String> codes);

	/**
	 * 循环获取某些code 的实时数据
	 * @param codes
	 * @param interval ：两次获取数据之间的时间间隔，单位为秒
	 */
	public void getRealTime(List<String> codes, int interval);

	public void getRealTimeAll();

	public void getRealTimeAll(int interval);

	/**
	 * 获取指定 codes 的某个时间段的日线，包含 startDate和endDate
	 * @param codes
	 * @param startDate
	 * @param endDate
	 */
	public void getHistory(List<String> codes, String startDate, String endDate);

	public void getHistoryAll(String startDate, String endDate);

}
