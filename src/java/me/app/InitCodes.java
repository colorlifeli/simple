package me.app;

import java.sql.SQLException;

import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.net.dao.StockSourceDao;

public class InitCodes {
	public static void main(String[] args) throws SQLException {

		SqlRunner.me().setConn(H2Helper.connEmbededDb());
		StockSourceDao service = new StockSourceDao();
		String file = "d:/codes.csv";
		service.initCode(file);
	}
}
