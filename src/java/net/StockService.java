package net;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.model.RealTime;

import common.jdbcutil.ArrayListHandler;
import common.jdbcutil.SqlRunner;

public class StockService {

	SqlRunner sqlrunner = SqlRunner.me();

	public void initCode(String csvFilePath) throws SQLException {
		String sql = "truncate table sto_code";
		SqlRunner.me().execute(sql);
		sql = "insert into sto_code(market,code,name) select * from CSVREAD('" + csvFilePath + "',null,'charset=GBK')";
		SqlRunner.me().execute(sql);
	}

	/**
	 * 保存实时数据到数据库
	 * @param list
	 */
	public void saveRealTimeData(List<RealTime> list) {
		for (RealTime rt : list) {
			System.out.println(rt);
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
		String sql = null;
		Object[] params = new Object[] { null };
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
			codestr += code + ",";
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

}
