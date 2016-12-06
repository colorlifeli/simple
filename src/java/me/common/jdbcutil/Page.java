package me.common.jdbcutil;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Page {
	
	private static Logger logger = LoggerFactory.getLogger(Page.class);
	
	public List<?> list = Collections.EMPTY_LIST;
	public int total = 0;
	
	public static enum dbType {
		sql, oracle;
	}
	
	public static String makePageSql(String sql, dbType type, int page, int pageSize) {
		
		if(page < 1 || pageSize < 0 ) {
			logger.warn("page or pageSize is illegal, page:{}, pag size:{}", page, pageSize);
			return sql;
		}
		
		switch(type) {
		case sql:
			sql = standard(sql, page, pageSize);
			break;
		case oracle:
			sql = oracle(sql, page, pageSize);
		}
		return sql;
	}
	
	private static String standard(String sql, int page, int pageSize) {
		sql = sql + String.format(" LIMIT %d OFFSET %d", pageSize, (page - 1) * pageSize);
		
		return sql;
	}
	
	private static String oracle(String sql, int page, int pageSize) {
		String pre = "SELECT * FROM (SELECT T.*, ROWNUM RN FROM (";
        String last = String.format(") T WHERE ROWNUM <= %d) WHERE RN > %d",
                                    page * pageSize,
                                    (page - 1) * pageSize);
        sql = pre + sql + last;
		
		return sql;
	}
}
