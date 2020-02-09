package me.common.jdbcutil.h2;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.Config;
import me.common.jdbcutil.ArrayHandler;
import me.common.jdbcutil.QueryRule;
import me.common.jdbcutil.SqlRunner;
import me.common.util.TypeUtil;

public class H2Helper {

	private static Logger logger = LoggerFactory.getLogger(H2Helper.class);


	public static Connection connEmbededDb() {
		try {
			org.h2.Driver.load();

			Connection conn = DriverManager.getConnection(Config.db.url_embeded, Config.db.user,
					Config.db.password);

			logger.info("connect to h2 db in embeded mode... url:" + Config.db.url_embeded);
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
				logger.info("close connection.");
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

	public static void genCondition(Class<?> clazz, Map<String, String> voMap, String prefix, QueryRule queryRule)
			throws Exception {
		Field[] ff = clazz.getDeclaredFields();
		//对数据模型的每个字段，增加查询条件
		//对于不同类型的字段，判断条件、查询规则都可能不同，分开处理
		//对于复合主键，主键本身也是对象
		for (Field f : ff) {
			f.setAccessible(true);
			String type = f.getType().getName().substring(f.getType().getName().lastIndexOf(".") + 1);
			String value = voMap.get(f.getName());
			if (TypeUtil.isEmpty(value))
				continue;
			String propertyName = prefix + f.getName();
			logger.debug("name:" + f.getName() + ", type:" + f.getType().getName() + "(" + type + "), value:" + value);

			switch (Type.toType(type.toUpperCase())) {
			case STRING:
				value.replace("*", "%");
				queryRule.andLike(propertyName, value);
				break;

			case INT:
				value = value.trim();
				if (value.startsWith("<")) {// example: <10
					value = value.split("<")[1].trim();
					logger.debug("int value:" + value);
					queryRule.andLessThan(propertyName, Integer.parseInt(value));
				} else {
					if (value.startsWith(">")) { // >10
						value = value.split(">")[1].trim();
						logger.debug("int value:" + value);
						queryRule.andGreaterThan(propertyName, Integer.parseInt(value));
					} else {
						if (value.startsWith("[") && value.endsWith("]")) { // [1,10]
							value = value.substring(1, value.length() - 1);
							logger.debug("int value:" + value);
							String start = value.split(",")[0].trim();
							String end = value.split(",")[1].trim();
							queryRule.andBetween(propertyName, Integer.parseInt(start), Integer.parseInt(end));
						} else {//just a number
							queryRule.andEqual(propertyName, Integer.parseInt(value));
						}
					}
				}

				break;
			case BIGDECIMAL:
				value = value.trim();
				if (value.startsWith("<")) {// example: <10
					value = value.split("<")[1].trim();
					logger.debug("int value:" + value);
					queryRule.andLessThan(propertyName, new BigDecimal(value));
				} else {
					if (value.startsWith(">")) { // >10
						value = value.split(">")[1].trim();
						logger.debug("int value:" + value);
						queryRule.andGreaterThan(propertyName, new BigDecimal(value));
					} else {
						if (value.startsWith("[") && value.endsWith("]")) { // [1,10]
							value = value.substring(1, value.length() - 1);
							logger.debug("int value:" + value);
							String start = value.split(",")[0].trim();
							String end = value.split(",")[1].trim();
							queryRule.andBetween(propertyName, new BigDecimal(start), new BigDecimal(end));
						} else {//just a number
							queryRule.andEqual(propertyName, new BigDecimal(value));
						}
					}
				}
				break;

			case NOVALUE:
				//复合主键
				if (f.getName().equals("id")) {
					genCondition(f.getType(), voMap, "id.", queryRule);
				}
				break;
			}

		}
	}
	
	public static void genCondition2(Map<String, String> map, String prefix, QueryRule queryRule)
			throws Exception {
		for (Entry<String, String> entry : map.entrySet()) {
			String tmp = entry.getValue();
			String name = entry.getKey();
			String value = tmp.split(",")[0];
			String type = tmp.split(",")[1];
			if(TypeUtil.isEmpty(value))
				continue;
			if (TypeUtil.isEmpty(value))
				continue;
			String propertyName = prefix + name;
			logger.debug("name:" + name + ", type:" + type + ", value:" + value);

			switch (Type.toType(type.toUpperCase())) {
			case STRING:
				value = value.replace("*", "%");
				queryRule.andLike(propertyName, value);
				break;

			case INT:
				value = value.trim();
				if (value.startsWith("<")) {// example: <10
					value = value.split("<")[1].trim();
					logger.debug("int value:" + value);
					queryRule.andLessThan(propertyName, Integer.parseInt(value));
				} else {
					if (value.startsWith(">")) { // >10
						value = value.split(">")[1].trim();
						logger.debug("int value:" + value);
						queryRule.andGreaterThan(propertyName, Integer.parseInt(value));
					} else {
						if (value.startsWith("[") && value.endsWith("]")) { // [1,10]
							value = value.substring(1, value.length() - 1);
							logger.debug("int value:" + value);
							String start = value.split(",")[0].trim();
							String end = value.split(",")[1].trim();
							queryRule.andBetween(propertyName, Integer.parseInt(start), Integer.parseInt(end));
						} else {//just a number
							queryRule.andEqual(propertyName, Integer.parseInt(value));
						}
					}
				}

				break;
			case BIGDECIMAL:
				value = value.trim();
				if (value.startsWith("<")) {// example: <10
					value = value.split("<")[1].trim();
					logger.debug("int value:" + value);
					queryRule.andLessThan(propertyName, new BigDecimal(value));
				} else {
					if (value.startsWith(">")) { // >10
						value = value.split(">")[1].trim();
						logger.debug("int value:" + value);
						queryRule.andGreaterThan(propertyName, new BigDecimal(value));
					} else {
						if (value.startsWith("[") && value.endsWith("]")) { // [1,10]
							value = value.substring(1, value.length() - 1);
							logger.debug("int value:" + value);
							String start = value.split(",")[0].trim();
							String end = value.split(",")[1].trim();
							queryRule.andBetween(propertyName, new BigDecimal(start), new BigDecimal(end));
						} else {//just a number
							queryRule.andEqual(propertyName, new BigDecimal(value));
						}
					}
				}
				break;

			case NOVALUE:
				break;
			}
			
		}
		
	}

	public enum Type {
		STRING, INT, BIGDECIMAL, NOVALUE;

		public static Type toType(String str) {
			try {
				return valueOf(str);
			} catch (Exception e) {
				return NOVALUE;
			}
		}
	}
}
