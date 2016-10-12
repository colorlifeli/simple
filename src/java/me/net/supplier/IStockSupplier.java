package me.net.supplier;

import java.util.List;

/**
 * stock数据提供者接口
 * 
 * 即数据据的某个获取接口，一般数据是从某个网站分析得出的链接获取而来
 * @author James
 *
 */
public interface IStockSupplier {

	/**
	 * 根据stock代码获取数据
	 * @param codes
	 * @return
	 */
	public List<?> getData(List<String> codes, Object... obj);

	/**
	 * 对给定的代码进行检查，并返回不正常的数据
	 * 
	 * @param codes
	 * @return 返回不正常代码数组，其中
	 * object0].停牌
	 * object[1].该数据提供者未能正常提供数据的code
	 */
	public Object[][] findAbnormal(List<String> codes);

}
