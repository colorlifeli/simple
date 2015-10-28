package net;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.model.RealTime;
import net.model.StockDay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.jdbcutil.ArrayHandler;
import common.jdbcutil.ArrayListHandler;
import common.jdbcutil.BeanListHandler;
import common.jdbcutil.SqlRunner;
import common.util.Constant;
import common.util.TypeUtil;

public class StockService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	SqlRunner sqlrunner = SqlRunner.me();

	// 根据不同的实现进行设置
	private String impl;
	private SourceVar sourceVar = new SourceVar();

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
	 * @param code_type o:origin 正式code， r: 用于实时获取的code  h:用于历史获取的code
	 * @return
	 * @throws SQLException
	 */
	public List<String> getAllAvailableCodes(int num, String code_type) throws SQLException {
		if (num < 0)
			return null;
		String codeName = "code";
		String sql = null;
		Object[] params = null;

		if (code_type != null && "r".equals(code_type))
			codeName = sourceVar.rCodeName;
		if (code_type != null && "h".equals(code_type))
			codeName = sourceVar.hCodeName;

		if (num == 0) {
			sql = "select %s from sto_code where flag is null";
			sql = String.format(sql, codeName);
		} else {
			sql = "select %s from sto_code where flag is null and rownum<=?";
			sql = String.format(sql, codeName);
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
	 * @param code_type o:origin 正式code， r: 用于实时获取的code  h:用于历史获取的code
	 * @return
	 * @throws SQLException
	 */
	public List<String> getCodes(int num, String code_type) throws SQLException {
		if (num < 0)
			return null;
		String codeName = "code";
		String sql = null;
		Object[] params = null;

		if (code_type != null && "r".equals(code_type))
			codeName = sourceVar.rCodeName;
		if (code_type != null && "h".equals(code_type))
			codeName = sourceVar.hCodeName;

		if (num == 0) {
			sql = "select %s from sto_code";
			sql = String.format(sql, codeName);
		} else {
			sql = "select %s from sto_code where rownum<=?";
			sql = String.format(sql, codeName);
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
	 * @param code_type  r: 用于实时获取的code  h:用于历史获取的code
	 * @return
	 * @throws SQLException
	 */
	public List<String> getCodes(List<String> codes, String code_type) throws SQLException {
		if (codes == null || codes.size() == 0) {
			return null;
		}
		String codeName = "code";

		if (code_type != null && "r".equals(code_type))
			codeName = sourceVar.rCodeName;
		if (code_type != null && "h".equals(code_type))
			codeName = sourceVar.hCodeName;

		String codestr = "(";
		for (String code : codes) {
			codestr += "'" + code + "',";
		}
		codestr = codestr.substring(0, codestr.length() - 1) + ")";
		String sql = null;

		sql = "select %s from sto_code where code in %s";
		sql = String.format(sql, codeName, codestr);

		List<Object[]> result = sqlrunner.query(sql, new ArrayListHandler());

		List<String> strs = new ArrayList<String>();
		for (Object[] objs : result) {
			strs.add((String) objs[0]);
		}

		return strs;
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
		sql = String.format(sql, sourceVar.rCodeName);
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

		if ((now.compareTo(Constant.stock.morningStart) > 0 && now.compareTo(Constant.stock.morningEnd) <= 0)
				|| (now.compareTo(Constant.stock.afternoonStart) > 0 && now.compareTo(Constant.stock.afternoonEnd) <= 0)) {
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

		if (now.compareTo(Constant.stock.afternoonEnd) >= 0) {
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
	 * @param map
	 * @return  失败列表
	 */
	public Map<String, String> saveCsvFromUrl(Map<String, String> map) {
		Map<String, String> errors = new HashMap<String, String>();

		String sql = "insert into sto_day_tmp(code,date_,open_,high,low,close_,volume,source) "
				+ "SELECT '%s',date,open,high,low,close,volume,'%s' FROM CSVREAD('%s')";
		String source = "yahoo";
		for (Entry<String, String> item : map.entrySet()) {
			String url = item.getValue();
			String code = item.getKey();
			try {
				sqlrunner.execute(String.format(sql, code, source, url));
			} catch (SQLException e) {
				logger.error("saveCsvFromUrl, execute failed! code:{}, url:{}, {}", code, url, new Date());
				e.printStackTrace();

				logger.info("try again...");
				try {
					sqlrunner.execute(String.format(sql, code, source, url));
				} catch (SQLException e1) {
					logger.error("saveCsvFromUrl, execute failed again! code:{}, url:{}, {}", code, url, new Date());
					logger.info("record and go next...");
					errors.put(code, url);
					e1.printStackTrace();
				}

				continue;
			}

			logger.debug("saveCsvFromUrl, code:{}, url:{}, {}", code, url, new Date());
		}

		return errors;
	}

	public String getImpl() {
		return impl;
	}

	public void setImpl(String impl) {
		this.impl = impl;
		sourceVar.setVar(impl);
	}

	class SourceVar {
		TypeUtil.StockSource realSource;
		TypeUtil.StockSource historySource;
		// 查询实时数据时的code字段名称
		String rCodeName;
		String hCodeName;

		public void setVar(String impl) {
			if (impl == null) {
				logger.error("SourceVar->setVar, impl is null");
				return;
			}
			switch (impl) {
			case "impl1":
				realSource = TypeUtil.StockSource.SINA;
				rCodeName = "code_sina";
				hCodeName = "code_yahoo";
				break;
			default:
				logger.error("not support impl:" + impl);
			}
		}
	}

}
