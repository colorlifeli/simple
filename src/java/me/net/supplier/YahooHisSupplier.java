package me.net.supplier;

import java.util.ArrayList;
import java.util.List;

import me.net.model.Item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YahooHisSupplier implements IStockSupplier {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// http://ichart.finance.yahoo.com/table.csv?s=300072.sz&d=7&e=23&f=2010&a=5&b=11&c=2010
	private String url = "http://ichart.finance.yahoo.com/table.csv?s=%s&d=%s&e=%s&f=%s&a=%s&b=%s&c=%s";

	/**
	 * 获取每个 stock 的url
	 * 因为url直接下载 csv 文件，所以采取直接使用数据库读取 csv 的功能来写入数据库
	 * 
	 * @param codes  yahoo code
	 * @param start  format:yyyymmdd
	 * @param end    format:yyyymmdd
	 */
	@Override
	public List<?> getData(List<String> codes, Object... obj) {

		if (obj.length != 2)
			return null;
		String start = (String) obj[0];
		String end = (String) obj[1];
		List<Item> urls = new ArrayList<Item>();
		for (String code : codes) {

			int sYear = Integer.parseInt(start.substring(0, 4));
			int sMonth = Integer.parseInt(start.substring(4, 6));
			int sDay = Integer.parseInt(start.substring(6, 8));

			int eYear = Integer.parseInt(end.substring(0, 4));
			int eMonth = Integer.parseInt(end.substring(4, 6));
			int eDay = Integer.parseInt(end.substring(6, 8));

			// 特殊处理，月要减1
			sMonth -= 1;
			eMonth -= 1;

			String urlStr = String.format(url, code, eMonth, eDay, eYear, sMonth, sDay, sYear);
			urls.add(new Item(code, urlStr));
			logger.debug(urlStr);
		}

		return urls;
	}

	@Override
	public Object[][] findAbnormal(List<String> codes) {
		// 不需实现
		return null;
	}
}
