package me.common.jdbcutil.h2;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.h2.tools.Csv;

public class CsvUtil {

	// public static void readFromCsv(String filePath) {
	// ResultSet rs = new Csv().read("data/test.csv", null, null);
	// ResultSetMetaData meta = rs.getMetaData();
	// while (rs.next()) {
	// for (int i = 0; i < meta.getColumnCount(); i++) {
	// System.out.println(meta.getColumnLabel(i + 1) + ": " + rs.getString(i +
	// 1));
	// }
	// System.out.println();
	// }
	// rs.close();
	// }

	public static void main(String[] args) throws Exception {
		ResultSet rs = new Csv().read("d:/result.csv", null, "GBK");
		ResultSetMetaData meta = rs.getMetaData();
		while (rs.next()) {
			for (int i = 0; i < meta.getColumnCount(); i++) {
				System.out.println(meta.getColumnLabel(i + 1) + ": " + rs.getString(i + 1));
			}
			System.out.println();
		}
		rs.close();
	}

}
