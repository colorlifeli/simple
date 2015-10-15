package common.jdbcutil.h2;

import java.sql.Connection;
import java.sql.DriverManager;

import common.util.Constant;

public class H2Helper {

	private static Connection conn;

	public static Connection connEmbededDb() {
		try {
			org.h2.Driver.load();

			conn = DriverManager.getConnection(Constant.db.url, Constant.db.user, Constant.db.password);

			return conn;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void close() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
