package me.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.util.TypeUtil;

public class DbToolService {
	//private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private List<String> tableNamesAll = new ArrayList<String>();
	private boolean isUseOracle = true;
	
	public DbToolService() {
		if(isUseOracle) {
			try {
				Class.forName("oracle.jdbc.driver.OracleDriver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Connection connectOracle(String url, String user, String pwd, String schema) throws SQLException {
		//default value
		if (TypeUtil.isEmpty(url))
			url = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = 10.133.35.19)(PORT = 1521))(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = cskhyx)))";
		if (TypeUtil.isEmpty(user)) user = "khyx_ecif_dev";
		if (TypeUtil.isEmpty(pwd)) pwd = "khyx";
		if (TypeUtil.isEmpty(schema)) schema = "KHYX_ECIF_DEV";
		return DriverManager.getConnection(url, user, pwd);
	}
	
	public List<String> getTableNamesAll() {
		return tableNamesAll;
	}

	public void setTableNamesAll(List<String> tableNamesAll) {
		this.tableNamesAll = tableNamesAll;
	}
}
