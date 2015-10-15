package common.jdbcutil.h2;

import java.sql.Connection;
import java.sql.DriverManager;

import common.util.Constant;

public class H2Helper {

	public static Connection connEmbededDb() {
		try {
			org.h2.Driver.load();

			Connection conn = DriverManager.getConnection(Constant.db.url, Constant.db.user, Constant.db.password);

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
}
