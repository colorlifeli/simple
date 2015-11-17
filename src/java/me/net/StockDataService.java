package me.net;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.jdbcutil.ArrayHandler;
import me.common.jdbcutil.BeanListHandler;
import me.common.jdbcutil.SqlRunner;
import me.net.model.StockDay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockDataService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	SqlRunner sqlrunner = SqlRunner.me();

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

	public String getName(String code) throws SQLException {
		String sql = "select name from sto_code where code=?";
		Object[] result = sqlrunner.query(sql, new ArrayHandler(), code);
		if (result == null || result.length == 0) {
			logger.error("cannot find the code:" + code);
			return null;
		}

		return (String) result[0];
	}

}
