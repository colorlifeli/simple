package net;

import java.util.List;

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
	 * 获取指定 codes 的某个时间段的日线
	 * @param codes
	 * @param startDate
	 * @param endDate
	 */
	public void getHistory(List<String> codes, String startDate, String endDate);

	public void getHistoryAll(String startDate, String endDate);

}
