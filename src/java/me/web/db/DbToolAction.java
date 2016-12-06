package me.web.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.common.ActionIf;
import me.common.annotation.ActionAnno.Action;
import me.common.annotation.ActionAnno.Pack;
import me.common.annotation.ActionAnno.Result;
import me.common.annotation.IocAnno.Ioc;
import me.common.jdbcutil.ArrayHandler;
import me.common.jdbcutil.MapListHandler;
import me.common.jdbcutil.Page;
import me.common.jdbcutil.QueryRule;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.JsonUtil;
import me.common.util.TypeUtil;
import me.service.DbToolService;

@Pack(path = "db")
public class DbToolAction extends ActionIf {
	
	@Ioc
	DbToolService dbToolService;

	// jquery easyui datagrid 相关
	private int page;
	private int rows;
//	private String sort;
//	private String order;
	
	private String tableName;
	private String schema;
	private String pattern;
	
	private Connection conn = null;
	private Page.dbType dbType = Page.dbType.sql;

	@Action(path = "enter", targets = { @Result(name = "success", value = "db/dbtool.jsp") })
	public String enter() {

		return "success";
	}
	
	@Action(path = "getTableNamesAll", targets = { @Result(name = "json", value = "json") })
	public String getTableNamesAll() {
		
		List<String> tableNamesAll = new ArrayList<String>();
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			tableNamesAll = SqlRunner.me().getAllTables(conn, schema, null, null);
			dbToolService.setTableNamesAll(tableNamesAll);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for(String name : tableNamesAll) {
			Map<String, String> tmp = new HashMap<String, String>();
			tmp.put("tableName", name);
			result.add(tmp);
		}
		JsonUtil util = new JsonUtil();
		util.put("total", result.size());
		util.put("rows", result);
		
		return util.toString();
	}
	
	@Action(path = "getTableNames", targets = { @Result(name = "json", value = "json") })
	public String getTableNames() {

		//使用的是 java 的正则表达式匹配
		//* 表示 匹配前面的子表达式任意次。 . 表示匹配除“\r\n”之外的任何单个字符。因此 * 前面要加 . 
		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		List<String> tableNamesAll = dbToolService.getTableNamesAll();
		if(tableNamesAll.size() > 0) {
			for(String name : tableNamesAll) {
				if(!TypeUtil.isEmpty(pattern)) {
					Pattern p = Pattern.compile(pattern.toUpperCase());
					Matcher m = p.matcher(name);
					if (!m.find()) { //不符合则下一条
						continue;
					}
				}

				Map<String, String> tmp = new HashMap<String, String>();
				tmp.put("tableName", name);
				result.add(tmp);
			}
		}
	
		JsonUtil util = new JsonUtil();
		util.put("total", result.size());
		util.put("rows", result);
		
		return util.toString();
	}
	
	@Action(path = "getData", targets = { @Result(name = "json", value = "json") })
	public String getData() {
		if(page  == 0) page = 1;
		if(rows == 0) rows = 20;

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, String> types = new HashMap<String, String>();
		List<Object> params = new ArrayList<Object>();
		String sql = "select * from " + tableName;
		
		try {
			sql = this.getCondition(sql, params);
			result = SqlRunner.me().queryForPage(conn, sql, new MapListHandler(), page, rows, dbType, params.toArray());
			types = SqlRunner.me().getColumns(conn, tableName, dbType);
		} catch (Exception e) {
			e.printStackTrace();
		}

		JsonUtil util = new JsonUtil();
		util.put("total", result.size());
		util.put("rows", result);
		util.put("types", types);
		
		return util.toString();
	}
	
	@Action(path = "getCount", targets = { @Result(name = "json", value = "json") })
	public String getCount() {

		String sqlCount = "select count(1) from " + tableName;
		List<Object> params = new ArrayList<Object>();
		Object[] count = {};
		try {
			sqlCount = this.getCondition(sqlCount, params);
			if(conn == null)
				count = SqlRunner.me().query(sqlCount, new ArrayHandler(), params.toArray());
			else
				count = SqlRunner.me().query(conn, sqlCount, new ArrayHandler(), params.toArray());
			if(count == null || count.length == 0) 
				count = new Object[]{0};
		} catch (Exception e) {
			e.printStackTrace();
		}

		JsonUtil util = new JsonUtil();
		util.put("total", count[0]);
		
		return util.toString();
	}
	
	private String getCondition(String sql, List<Object> params) throws Exception {
		Map<String, String> c = new HashMap<String, String>();
		for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			if("tableName".equals(entry.getKey()))
					continue;
			c.put(entry.getKey(), entry.getValue()[0]);
		}

		QueryRule qr = new QueryRule();
		H2Helper.genCondition2(c, "", qr);
		params.addAll(qr.getParams());
		sql += " where 1=1 " + qr.toString();
		
		return sql;
	}
	
}
