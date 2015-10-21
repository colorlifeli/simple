package net;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.model.RealTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.jdbcutil.ArrayHandler;
import common.jdbcutil.ArrayListHandler;
import common.jdbcutil.BeanListHandler;
import common.jdbcutil.SqlRunner;

public class StockService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	SqlRunner sqlrunner = SqlRunner.me();

	public void initCode(String csvFilePath) throws SQLException {
		String sql = "truncate table sto_code";
		sqlrunner.execute(sql);
		sql = "insert into sto_code(market,code,name) select * from CSVREAD('" + csvFilePath + "',null,'charset=GBK')";
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
	 * 查询所有或部分code出来，结果是 sina code的形式
	 * sina的 code是 market + code
	 * @param num 查询出来的数量 0:all 
	 * @return
	 * @throws SQLException
	 */
	public List<String> getCodes_forsina(int num) throws SQLException {
		if (num < 0)
			return null;
		String sql = null;
		Object[] params = null;
		if (num == 0) {
			sql = "select market||code from sto_code";
		} else {
			sql = "select market||code from sto_code where rownum<=?";
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
	 * @return
	 * @throws SQLException
	 */
	public List<String> getCodes_forsina(List<String> codes) throws SQLException {
		if (codes == null || codes.size() == 0) {
			return null;
		}
		String codestr = "(";
		for (String code : codes) {
			codestr += "'" + code + "',";
		}
		codestr = codestr.substring(0, codestr.length() - 1) + ")";
		String sql = null;

		sql = "select market||code from sto_code where code in " + codestr;

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

		sql = "select * from sto_realtime where code in " + codestr;

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

}
