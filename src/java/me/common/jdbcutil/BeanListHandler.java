package me.common.jdbcutil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BeanListHandler<T> implements ResultSetHandler<List<T>> {

	private final Class<T> type;

	public BeanListHandler(Class<T> type) {
		this.type = type;
	}

	@Override
	public List<T> handle(ResultSet rs) throws SQLException {
		return RowProcessor.me().toBeanList(rs, type);
	}

}
