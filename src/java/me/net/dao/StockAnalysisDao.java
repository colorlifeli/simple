package me.net.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.common.SimpleException;
import me.common.jdbcutil.ArrayHandler;
import me.common.jdbcutil.ArrayListHandler;
import me.common.jdbcutil.BeanListHandler;
import me.common.jdbcutil.QueryRule;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.TypeUtil;
import me.common.util.Util;
import me.net.model.OperRecord;
import me.net.model.StockDay;
import me.net.model.StockOperSum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockAnalysisDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	SqlRunner sqlrunner = SqlRunner.me();

	Map<String, List<StockDay>> all = new HashMap<String, List<StockDay>>();

	/**
	 * 获取指定 code 的 day 数据
	 * @param code
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public List<StockDay> getDay(String code, String startDate, String endDate) throws SQLException {

		String sql = "SELECT * FROM sto_day_tmp where code=?";
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
	 * 获取指定 code 的 day 数据,并缓存，有利于重复计算
	 * @param code
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public List<StockDay> getDayCache(String code, String startDate, String endDate) throws SQLException {

		if (all.size() > 3000) {
			//缓存太大
			for (Entry<String, List<StockDay>> entry : all.entrySet()) {
				entry.getValue().clear();
			}
			all.clear();
		}

		//缓存
		if (all.get(code + startDate + endDate) != null) {
			return Util.deepCopy(all.get(code + startDate + endDate));
		}

		String sql = "SELECT * FROM sto_day_tmp where code=?";
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

		List<StockDay> result = sqlrunner.query(sql, new BeanListHandler<StockDay>(StockDay.class), params.toArray());
		all.put(code + startDate + endDate, result);

		return Util.deepCopy(result);
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

		String sql = "SELECT * FROM sto_oper_sum where flag not in('01','02') ";

		if (isIncludeAbnormal)
			sql = "SELECT * FROM sto_oper_sum where 1=1 ";

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

	/**
	 * 查出所有有历史数据的日期
	 * @return
	 * @throws SQLException
	 */
	public List<String> getAllDate() throws SQLException {
		String sql = "select to_char(date_,'yyyy-mm-dd') from sto_day_tmp "
				+ "where code =(select top 1 code from (select code,count(1)  num from sto_day_tmp group by code) order by num  desc) order by date_";
		Object[] params = null;

		List<Object[]> result = sqlrunner.query(sql, new ArrayListHandler(), params);

		List<String> strs = new ArrayList<String>();
		for (Object[] objs : result) {
			strs.add((String) objs[0]);
		}

		return strs;
	}
	
	public List<String> getAllDate(String code) throws SQLException {
		if(TypeUtil.isEmpty(code)) 
			return null;
		String sql = "select to_char(date_,'yyyy-mm-dd') from sto_day_tmp "
				+ "where code = ? order by date_";
		Object[] params = {code};

		List<Object[]> result = sqlrunner.query(sql, new ArrayListHandler(), params);

		List<String> strs = new ArrayList<String>();
		for (Object[] objs : result) {
			strs.add((String) objs[0]);
		}

		return strs;
	}
	
	public String getFactor(String code, Date date) throws SQLException {
		String sql = "select factor from sto_day_tmp "
				+ "where code = ? and date_= ? ";
		
		Object[] result = sqlrunner.query(sql, new ArrayHandler(), code, date);
		if (result == null || result.length == 0) {
			logger.error("cannot find the code:{}, date:{}", code, date);
			return null;
		}

		return (String) result[0];
	}

	/**
	 * 查出code的最后一次获取历史数据的endDate
	 * @param code
	 * @return
	 * @throws SQLException
	 */
	public String getLastDate(String code) throws SQLException {
		if(TypeUtil.isEmpty(code)) 
			return null;
		String sql = "select to_char(lastDate,'yyyy-mm-dd') from his_data_progress "
				+ "where code = ? ";
		Object[] params = {code};

		List<Object[]> result = sqlrunner.query(sql, new ArrayListHandler(), params);

		if(result == null || result.size() == 0)
			return null;
		return (String)result.get(0)[0];		
	}
	
	/**
	 * 更新历史数据下载进度
	 * @param code
	 * @param endDate
	 */
	public void saveHisProgress(List<String> codes, Date endDate) {
		String sql = "update his_data_progress set lastDate=? where code = ?";
		//Object[] params = {endDate, code};
		Object[][] params = new Object[codes.size()][];

		for (int i = 0; i < codes.size(); i++) {
			params[i] = new Object[]{endDate, codes.get(i)};
		}
		try {
			sqlrunner.insertBatch(sql, new ArrayHandler(), params);
		} catch (SQLException e) {
			logger.error("更新历史数据下载进度失败.");
			e.printStackTrace();
			throw new SimpleException("更新历史数据下载进度失败");
		}
	}
}
