package common.jdbcutil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 所有行数据放在一个 list中，每一行数据放在map中，map的key是列名
 * @author James
 *
 */
public class MapListHandler implements ResultSetHandler<List<Map<String, Object>>> {

	@Override
	public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		while (rs.next()) {
			rows.add(RowProcessor.me().toMap(rs));
		}
		return rows;
	}

}
