package me.common.jdbcutil.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import me.common.jdbcutil.ArrayHandler;
import me.common.jdbcutil.SqlRunner;
import me.common.util.Constant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2Helper {

	private static Logger logger = LoggerFactory.getLogger(H2Helper.class);

	public static Connection connEmbededDb() {
		try {
			org.h2.Driver.load();

			Connection conn = DriverManager.getConnection(Constant.db.url_test_embeded, Constant.db.user,
					Constant.db.password);

			System.out.println("connect to h2 db in embeded mode...");
			return conn;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
				System.out.println("close connection.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取某个表所有行的数据目
	 * @param table_name
	 * @param obj 条件。 条件字段1，值1，条件字段2，值2....
	 * @return
	 * @throws SQLException
	 */
	public static int getCount(String table_name, Object... obj) throws SQLException {
		String sql = "select count(*) from " + table_name;

		Object[] params = new Object[obj.length / 2];
		if (obj.length > 0) {
			if (obj.length % 2 != 0) {
				logger.error("参数必须是双数");
				return 0;
			}
			sql += " where 1=1 ";
			int max = obj.length / 2;
			for (int i = 0; i < max; i++) {
				sql += String.format(" and %s=? ", obj[i * 2]);
				params[i] = obj[i * 2 + 1];
			}
		}

		return ((Long) SqlRunner.me().query(sql, new ArrayHandler(), params)[0]).intValue();
	}
}
