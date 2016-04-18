package me.net.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.common.SimpleException;
import me.common.jdbcutil.ArrayHandler;
import me.common.jdbcutil.BeanListHandler;
import me.common.jdbcutil.QueryRule;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.model.OperRecord;
import me.net.model.StockDay;
import me.net.model.StockOperSum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockAnalysisDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	SqlRunner sqlrunner = SqlRunner.me();

	/**
	 * 获取指定 code 的 day 数据
	 * @param code
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	public List<StockDay> getDay(String code, String startDate, String endDate) throws SQLException {
		String sql = "SELECT * FROM STO_DAY_TMP where code=?";
		List<Object> params = new ArrayList<Object>();
		params.add(code);

		if (startDate != null) {
			sql += " and date_>?";
			params.add(startDate);
		}
		if (endDate != null) {
			sql += "and date_<?";
			params.add(endDate);
		}

		sql += " order by date_";

		return sqlrunner.query(sql, new BeanListHandler<StockDay>(StockDay.class), params.toArray());
	}

	/**
	 * 获取指定 code 的名字
	 * @param code
	 * @return
	 * @throws SQLException
	 */
	public String getName(String code) throws SQLException {
		String sql = "select name from sto_code where code=?";
		Object[] result = sqlrunner.query(sql, new ArrayHandler(), code);
		if (result == null || result.length == 0) {
			logger.error("cannot find the code:" + code);
			return null;
		}

		return (String) result[0];
	}

	/**
	 * 保存操作数据
	 * @param list
	 */
	public void saveOperList(List<OperRecord> list) {
		if (list == null || list.size() == 0) {
			logger.info("saveOperList: list is empty");
			return;
		}
		String sql = "insert into sto_operation (sn,code,oper,num,price,total,sum,remain,flag,date_) values (?,?,?,?,?,?,?,?,?,?)";
		Object[][] params = new Object[list.size()][];
		for (int i = 0; i < list.size(); i++) {
			params[i] = list.get(i).toObjectArray();
		}
		try {
			sqlrunner.insertBatch(sql, new ArrayHandler(), params);
		} catch (SQLException e) {
			logger.error("批量插入操作数据失败.");
			e.printStackTrace();
			throw new SimpleException("批量保存操作数据失败");
		}
	}

	/**
	 * 保存操作单个code汇总数据
	 * @param list
	 */
	public void saveOperSum(StockOperSum sum) {
		if (sum == null) {
			logger.info("StockOperSum is null");
			return;
		}
		String sql = "insert into sto_oper_sum (code,name,buys,sells,times,winTimes,loseTimes,lastRemain,minRemain,falg) values (?,?,?,?,?,?,?,?,?,?)";
		Object[][] params = new Object[1][];

		params[0] = sum.toObjectArray();

		try {
			sqlrunner.insertBatch(sql, new ArrayHandler(), params);
		} catch (SQLException e) {
			logger.error("保存单个code操作汇总数据失败.");
			e.printStackTrace();
		}
	}

	/**
	 * 保存操作多个code汇总数据
	 * @param list
	 */
	public void saveOperSums(List<StockOperSum> list) {
		if (list == null || list.size() == 0) {
			logger.info("saveOperSums: list is empty");
			return;
		}
		String sql = "insert into sto_oper_sum (code,name,buys,sells,times,winTimes,loseTimes,lastRemain,minRemain,flag) values (?,?,?,?,?,?,?,?,?,?)";
		Object[][] params = new Object[list.size()][];

		for (int i = 0; i < list.size(); i++) {
			params[i] = list.get(i).toObjectArray();
		}

		try {
			sqlrunner.insertBatch(sql, new ArrayHandler(), params);
		} catch (SQLException e) {
			logger.error("保存多个code操作汇总数据失败.");
			e.printStackTrace();
			throw new SimpleException("保存操作汇总数据失败");
		}
	}

	/**
	 * 查找所有code的操作汇总数据
	 * @param isIncludeAbnormal  是否包含异常数据，true:包含
	 * @return
	 * @throws Exception 
	 */
	public List<StockOperSum> getAllCodeSum(boolean isIncludeAbnormal, Map<String, String> voMap) throws Exception {

		String sql = "SELECT * FROM sto_oper_sum where flag='00'";

		if (isIncludeAbnormal)
			sql = "SELECT * FROM sto_oper_sum";

		Object[] params = null;
		if (voMap != null) {
			QueryRule qr = new QueryRule();
			H2Helper.genCondition(StockOperSum.class, voMap, "", qr);
			sql += qr.toString();
			params = qr.getParams().toArray();
		}

		return sqlrunner.query(sql, new BeanListHandler<StockOperSum>(StockOperSum.class), params);
	}

	/**
	 * 查找code的操作数据
	 * @param hcode
	 * @return
	 * @throws SQLException
	 */
	public List<OperRecord> getOperList(String hcode) throws SQLException {
		String sql = "SELECT * FROM sto_operation where code=?";
		Object[] params = { hcode };

		return sqlrunner.query(sql, new BeanListHandler<OperRecord>(OperRecord.class), params);
	}

	public void clearOperation() {
		String sql = "truncate table sto_operation";

		try {
			sqlrunner.execute(sql);
		} catch (SQLException e) {
			logger.error("truncate table sto_operation fail!");
			e.printStackTrace();
		}
	}

	public void clearOperSum() {
		String sql = "truncate table sto_oper_sum";

		try {
			sqlrunner.execute(sql);
		} catch (SQLException e) {
			logger.error("truncate table sto_oper_sum fail!");
			e.printStackTrace();
		}
	}

}
