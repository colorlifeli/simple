package common.jdbcutil;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHandler<T> {

	/**
	 * 处理结果集
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	T handle(ResultSet rs) throws SQLException;
}
