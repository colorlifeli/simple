package net;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YahooSourceService {

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
	public Map<String, String> getHistory(List<String> codes, String start, String end) {

		Map<String, String> result = new HashMap<String, String>();
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
			result.put(code, urlStr);
			logger.debug(urlStr);
		}

		return result;
	}
}
