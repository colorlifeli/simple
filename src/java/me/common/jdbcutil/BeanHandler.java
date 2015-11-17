package me.common.jdbcutil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BeanHandler<T> implements ResultSetHandler<T> {

	private final Class<T> type;

	public BeanHandler(Class<T> type) {
		this.type = type;
	}

	@Override
	public T handle(ResultSet rs) throws SQLException {
		return rs.next() ? RowProcessor.me().toBean(rs, this.type) : null;
	}

}
