package net;

import java.sql.SQLException;
import java.util.List;

import common.jdbcutil.SqlRunner;

public class StockService {

	public void initCode(String csvFilePath) throws SQLException {
		String sql = "truncate table sto_code";
		SqlRunner.me().execute(sql);
		sql = "insert into sto_code(market,code,name) select * from CSVREAD('" + csvFilePath + "',null,'charset=GBK')";
		SqlRunner.me().execute(sql);
	}

	public void saveRealTimeData(List<Object[]> list) {
		for (Object[] objs : list) {
			for (Object obj : objs) {
				System.out.print(obj + ",");
			}
			System.out.println();
		}
	}

}
