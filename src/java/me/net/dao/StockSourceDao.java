package me.net.dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.Config;
import me.common.jdbcutil.ArrayHandler;
import me.common.jdbcutil.ArrayListHandler;
import me.common.jdbcutil.BeanListHandler;
import me.common.jdbcutil.SqlRunner;
import me.net.NetType.eStockSource;
import me.net.model.Item;
import me.net.model.RealTime;
import me.net.model.StockDay;

public class StockSourceDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	SqlRunner sqlrunner = SqlRunner.me();

	// 根据不同的实现进行设置
	// private String impl;
	// private SourceVar sourceVar = new SourceVar();

	public void initCode(String csvFilePath) throws SQLException {
		String sql = "truncate table sto_code";
		sqlrunner.execute(sql);
		sql = "insert into sto_code(market,code,name) select * from CSVREAD('" + csvFilePath + "',null,'charset=GBK')";
		sqlrunner.execute(sql);

		// 对sina code 作特殊处理,对指数作特殊处理
		sql = "update sto_code t set code_sina=(select market||code from sto_code where code=t.code and market=t.market)";
		sqlrunner.execute(sql);
		sql = "update sto_code set type_='1',code_sina='s_sh000001' where code='000001' and market='sh'";
		sqlrunner.execute(sql);
		sql = "update sto_code set type_='1',code_sina='s_sz399001' where code='399001' and market='sz'";
		sqlrunner.execute(sql);

		// yahoo code 处理
		sql = "update sto_code t set code_yahoo=(select code||'.ss' "
				+ "from sto_code where code=t.code and market=t.market) where t.market='sh'";
		sqlrunner.execute(sql);
		sql = "update sto_code t set code_yahoo=(select code||'.sz' "
				+ "from sto_code where code=t.code and market=t.market) where t.market='sz'";
		sqlrunner.execute(sql);
	}

	/**
	 * 保存实时数据到数据库
	 * @param list
	 */
	public void saveRealTimeData(List<RealTime> list) {
		if (list == null || list.size() == 0) {
			logger.info("saveRealTimeData: list is empty");
			return;
		}
		String sql = "insert into sto_realtime (code,yclose,topen,now,high,low,deals,dealsum,time_,source) values (?,?,?,?,?,?,?,?,?,?)";
		Object[][] params = new Object[list.size()][];
		for (int i = 0; i < list.size(); i++) {
			params[i] = list.get(i).toObjectArray();
		}
		try {
			sqlrunner.insertBatch(sql, new ArrayHandler(), params);
		} catch (SQLException e) {
			logger.error("批量插入实时数据失败.");
			e.printStackTrace();
		}
	}

	/**
	 * 查询所有或部分code出来，都是非 stop 的 stock
	 * 
	 * @param num 查询出来的数量 0:all 
	 * @param source 不同的source，id的格式可能不同，在数据库中通过不同的字段来保存
	 * @return
	 * @throws SQLException
	 */
	public List<String> getAllAvailableCodes(int num, eStockSource source) throws SQLException {
		String fieldName = "code";
		if (num < 0)
			return null;
		if (source != null) {
			fieldName = source.fieldName();
		}
		String sql = null;
		Object[] params = null;

		if (num == 0) {
			sql = String.format("select %s from sto_code where flag is null and type_ is null", fieldName);
		} else {
			sql = String.format("select %s from sto_code where flag is null and type_ is null and rownum<=?",
					fieldName);
			params = new Object[] { num };
		}
		List<Object[]> result = sqlrunner.query(sql, new ArrayListHandler(), params);

		List<String> strs = new ArrayList<String>();
		for (Object[] objs : result) {
			strs.add((String) objs[0]);
		}

		return strs;
	}

	/**
	 * 查询所有或部分code出来
	 * 
	 * @param num 查询出来的数量 0:all 
	 * @param source 不同的source，id的格式可能不同，在数据库中通过不同的字段来保存
	 * @return
	 * @throws SQLException
	 */
	public List<String> getCodes(int num, eStockSource source) throws SQLException {
		String fieldName = "code";
		if (num < 0)
			return null;
		if (source != null) {
			fieldName = source.fieldName();
		}
		String sql = null;
		Object[] params = null;

		if (num == 0) {
			sql = String.format("select %s from sto_code", fieldName);
		} else {
			sql = String.format("select %s from sto_code where rownum<=?", fieldName);
			params = new Object[] { num };
		}
		List<Object[]> result = sqlrunner.query(sql, new ArrayListHandler(), params);

		List<String> strs = new ArrayList<String>();
		for (Object[] objs : result) {
			strs.add((String) objs[0]);
		}

		return strs;
	}

	/**
	 * 将code转为 sina code
	 * @param codes
	 * @param source 不同的source，id的格式可能不同，在数据库中通过不同的字段来保存
	 * @return
	 * @throws SQLException
	 */
	public List<String> getCodes(List<String> codes, eStockSource source) throws SQLException {
		if (codes == null || codes.size() == 0) {
			return null;
		}
		String fieldName = "code";
		if (source != null) {
			fieldName = source.fieldName();
		}

		String codestr = "(";
		for (String code : codes) {
			codestr += "'" + code + "',";
		}
		codestr = codestr.substring(0, codestr.length() - 1) + ")";
		String sql = null;

		sql = String.format("select %s from sto_code where code in %s", fieldName, codestr);

		List<Object[]> result = sqlrunner.query(sql, new ArrayListHandler());

		List<String> strs = new ArrayList<String>();
		for (Object[] objs : result) {
			strs.add((String) objs[0]);
		}

		return strs;
	}

	public String getCode(String code, eStockSource source) throws SQLException {
		List<String> codes = new ArrayList<String>();
		codes.add(code);
		List<String> result = this.getCodes(codes, source);
		if (result == null || result.size() == 0) {
			logger.error("cannot find the code:" + code);
			return null;
		} else {
			return result.get(0);
		}
	}

	/**
	 * 根据code查出所有实时数据
	 * @param codes
	 * @return
	 * @throws SQLException
	 */
	public List<RealTime> findRealtime(List<String> codes) throws SQLException {

		if (codes == null || codes.size() == 0) {
			return null;
		}
		String codestr = "(";
		for (String code : codes) {
			codestr += "'" + code + "',";
		}
		codestr = codestr.substring(0, codestr.length() - 1) + ")";
		String sql = null;

		sql = "select * from sto_realtime where code in %s";
		sql = String.format(sql, codestr);

		return sqlrunner.query(sql, new BeanListHandler<RealTime>(RealTime.class));
	}

	/**
	 * 根据code查出最新实时数据
	 * @param codes
	 * @return
	 * @throws SQLException
	 */
	public List<RealTime> findRealtimeLast(List<String> codes) throws SQLException {

		if (codes == null || codes.size() == 0) {
			return null;
		}
		String codestr = "(";
		for (String code : codes) {
			codestr += "'" + code + "',";
		}
		codestr = codestr.substring(0, codestr.length() - 1) + ")";
		String sql = null;

		sql = "select * from sto_realtime a where code in " + codestr;
		sql += " and not exists (select 1 from sto_realtime b where a.code=b.code and b.time_>a.time_)";

		return sqlrunner.query(sql, new BeanListHandler<RealTime>(RealTime.class));
	}

	/**
	 * 查出所有code的最新数据
	 * @return
	 * @throws SQLException
	 */
	public List<RealTime> findRealtimeAllLast() throws SQLException {
		String sql = "select  * from sto_realtime a where not exists (select 1 from sto_realtime b where a.code=b.code and b.time_>a.time_)";

		return sqlrunner.query(sql, new BeanListHandler<RealTime>(RealTime.class));
	}

	/**
	 * 对某个code，检查当前获得的数据是否和数据库的时间一样
	 * @param code
	 * @param data
	 * @return
	 * @throws SQLException 
	 */
	public boolean checkSameTime(String code, RealTime data) throws SQLException {
		if (code == null || "".equals(code) || data == null)
			return false;
		String sql = "select top 1 * from sto_realtime where code=? order by time_ desc";
		List<RealTime> his = sqlrunner.query(sql, new BeanListHandler<RealTime>(RealTime.class), new Object[] { code });

		if (his != null && his.get(0) != null && his.get(0).time_.equals(data.time_))
			return true;

		return false;
	}

	/**
	 * 查出一个可用的code
	 * @return
	 * @throws SQLException
	 */
	public String getAvailableCode() throws SQLException {
		// 约定没有flag的是正常code
		String sql = "select top 1 %s from sto_code where flag is null and type_ is null";
		sql = String.format(sql, eStockSource.SINA.fieldName());
		return (String) sqlrunner.query(sql, new ArrayHandler())[0];
	}

	/**
	 * 将所有 code 设为正常
	 * @throws SQLException 
	 */
	public void freshAllcode() throws SQLException {
		String sql = "update sto_code set flag = null";
		sqlrunner.execute(sql);
	}

	public void setCodeFlag(Object[][] params, String flag) throws SQLException {
		String sql = "update sto_code set flag = %s where code = ?";
		sql = String.format(sql, "'" + flag + "'");

		int[] rows = sqlrunner.batch(sql, params);
		int sum = 0;
		for (int i : rows)
			sum += i;
		logger.info("setCodeFlag, 共更新记录数" + sum);
	}

	/**
	 * 判断当前时间是否open time
	 * @return
	 */
	public boolean isStockTime() {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		Date date = new Date();
		String now = format.format(date);

		if ((now.compareTo(Config.stock.morningStart) > 0 && now.compareTo(Config.stock.morningEnd) <= 0)
				|| (now.compareTo(Config.stock.afternoonStart) > 0
						&& now.compareTo(Config.stock.afternoonEnd) <= 0)) {
			return true;
		}

		return false;
	}

	/**
	 * 判断时间是否在close之后
	 * 用处：1.数据产生的时间可能有延迟  2.即使一整天都没有获取数据，但收盘数据还是要的，用于日线
	 * @return
	 */
	public boolean isAfterStockTime() {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		Date date = new Date();
		String now = format.format(date);

		if (now.compareTo(Config.stock.afternoonEnd) >= 0) {
			return true;
		}

		return false;
	}

	public void saveDayData(List<StockDay> list) {
		if (list == null || list.size() == 0) {
			logger.info("saveDayData: list is empty");
			return;
		}
		String sql = "insert into sto_day (code,date_,open_,high,low,close_,volume,source) values (?,?,?,?,?,?,?,?)";
		Object[][] params = new Object[list.size()][];
		for (int i = 0; i < list.size(); i++) {
			params[i] = list.get(i).toObjectArray();
		}
		try {
			sqlrunner.insertBatch(sql, new ArrayHandler(), params);
		} catch (SQLException e) {
			logger.error("批量插入每日数据失败.");
			e.printStackTrace();
		}
	}
	
	public void saveDayData2(List<StockDay> list) {
		if (list == null || list.size() == 0) {
			logger.info("saveDayData2: list is empty");
			return;
		}
		String sql = "insert into sto_day_tmp2 (code,date_,open_,high,low,close_,volume,factor,source) values (?,?,?,?,?,?,?,?,?)";
		Object[][] params = new Object[list.size()][];
		for (int i = 0; i < list.size(); i++) {
			params[i] = list.get(i).toObjectArray2();
			//logger.debug(list.get(i).toString());
		}
		try {
			sqlrunner.insertBatch(sql, new ArrayHandler(), params);
		} catch (SQLException e) {
			logger.error("批量插入每日数据失败.");
			e.printStackTrace();
		}
	}

	/**
	 * 将实时数据转为日数据
	 * @param realtimes
	 * @return
	 */
	public List<StockDay> realtimeToDay(List<RealTime> realtimes) {
		List<StockDay> list = new ArrayList<StockDay>();
		for (RealTime realtime : realtimes) {
			StockDay day = new StockDay();
			day.code = realtime.code;
			day.open_ = realtime.tOpen;
			day.high = realtime.high;
			day.low = realtime.low;
			day.close_ = realtime.now;
			day.volume = realtime.deals;
			day.source = realtime.source;
			day.date_ = new Date();
			list.add(day);
		}
		return list;
	}

	public void dealRealTimeTable() throws SQLException {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		String day = format.format(date);

		String sql = "create table if not exists sto_realtime_tmp as select * from sto_realtime where 1=2 ";
		sqlrunner.execute(sql);

		sql = "alter table sto_realtime rename to sto_realtime" + day;
		sqlrunner.execute(sql);

		sql = "alter table sto_realtime_tmp rename to sto_realtime";
		sqlrunner.execute(sql);

		logger.info("rename realtime table success. table name:sto_realtime" + day);
	}

	/**
	 * 实时数据表是否改名了
	 * @return
	 * @throws SQLException 
	 */
	public boolean isRealtimeDayTableExists() throws SQLException {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		String day = format.format(date);

		String table = "sto_realtime" + day;
		return sqlrunner.isTableExists(table);
	}

	/**
	 * 直接使用数据库 api访问网络 csv 文件来获得数据
	 * 
	 * 如果失败，则重试一次，仍然失败则记录并返回
	 * @param items
	 * @return  失败列表
	 */
	public List<Item> saveCsvFromUrl(List<Item> items) {
		List<Item> errors = new ArrayList<Item>();

		String sql = "insert into sto_day_tmp(code,date_,open_,high,low,close_,volume,source) "
				+ "SELECT '%s',date,open,high,low,close,volume,'%s' FROM CSVREAD('%s')";
		String source = eStockSource.YAHOO.toString();
		int i = 0;
		for (Item item : items) {
			String url = item.getValue();
			String code = item.getKey();
			try {
				sqlrunner.execute(String.format(sql, code, source, url));
			} catch (SQLException e) {
				logger.error("saveCsvFromUrl, execute failed! code:{}, url:{}, {}. exception msg:{}", code, url,
						new Date(), e.getMessage());
				// e.printStackTrace();

				logger.info("try again...");
				try {
					sqlrunner.execute(String.format(sql, code, source, url));
				} catch (SQLException e1) {
					// e1.printStackTrace();
					logger.error("saveCsvFromUrl, execute failed again! code:{}, url:{}, {}. exception msg:{}", code,
							url, new Date(), e.getMessage());
					logger.info("record and go next...");
					errors.add(item);
				}

				continue;
			}

			logger.debug("saveCsvFromUrl, code:{}, url:{}, {}", code, url, new Date());
			if (++i % 200 == 0)
				try {
					Thread.sleep(30 * 1000); //每连接200次进行休眠，免得过度频繁
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

		return errors;
	}

	/**
	 * 查找还没有下载历史数据的 code
	 * @return
	 * @throws SQLException 
	 */
	public List<String> getNoHisCode(eStockSource source) throws SQLException {
		String fieldName = "code";
		if (source != null) {
			fieldName = source.fieldName();
		}
		String sql = String.format(
				"select %s from sto_code where CODE_yahoo not in (select distinct code from STO_DAY_TMP ) and flag is null and type_ is null",
				fieldName);

		List<Object[]> result = sqlrunner.query(sql, new ArrayListHandler());

		List<String> strs = new ArrayList<String>();
		for (Object[] objs : result) {
			strs.add((String) objs[0]);
		}

		return strs;
	}
	

	// class SourceVar {
	// TypeUtil.StockSource realSource;
	// TypeUtil.StockSource historySource;
	// // 查询实时数据时的code字段名称
	// String rCodeName;
	// String hCodeName;
	//
	// public void setVar(String impl) {
	// if (impl == null) {
	// logger.error("SourceVar->setVar, impl is null");
	// return;
	// }
	// switch (impl) {
	// case "impl1":
	// realSource = TypeUtil.StockSource.SINA;
	// rCodeName = "code_sina";
	// hCodeName = "code_yahoo";
	// break;
	// default:
	// logger.error("not support impl:" + impl);
	// }
	// }
	// }

}
