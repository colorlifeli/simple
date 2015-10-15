package common.jdbcutil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 所有行数据放在一个对象数组的 list中，每一个对象数据是一行
 * @author James
 *
 */
public class ArrayListHandler implements ResultSetHandler<List<Object[]>> {

	@Override
	public List<Object[]> handle(ResultSet rs) throws SQLException {
		List<Object[]> rows = new ArrayList<Object[]>();
		while (rs.next()) {
			rows.add(RowProcessor.me().toArray(rs));
		}
		return rows;
	}

}
