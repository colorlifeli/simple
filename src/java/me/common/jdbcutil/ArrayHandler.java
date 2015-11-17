package me.common.jdbcutil;

import java.sql.ResultSet;
import java.sql.SQLException;

import me.common.util.TypeUtil;

/**
 * 返回第一行数据，每列放到object中
 * @author James
 *
 */
public class ArrayHandler implements ResultSetHandler<Object[]> {

	@Override
	public Object[] handle(ResultSet rs) throws SQLException {
		return rs.next() ? RowProcessor.me().toArray(rs) : TypeUtil.EMPTY_ARRAY;
	}
}
