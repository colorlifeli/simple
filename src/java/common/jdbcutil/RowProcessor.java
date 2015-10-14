package common.jdbcutil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 数据结果的处理函数
 * 
 * 使用单例模式
 * 
 * @author James
 *
 */
public class RowProcessor {

	private static final BeanProcessor beanProcessor = new BeanProcessor();

	public static class holder {
		private static final RowProcessor instance = new RowProcessor();
	}

	private RowProcessor() {
	};

	public static final RowProcessor me() {
		return holder.instance;
	}

	public Object[] toArray(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		Object[] result = new Object[cols];

		for (int i = 0; i < cols; i++) {
			result[i] = rs.getObject(i + 1);
		}

		return result;
	}

	public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
		return beanProcessor.toBean(rs, type);
	}

	public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
		return beanProcessor.toBeanList(rs, type);
	}

	public Map<String, Object> toMap(ResultSet rs) throws SQLException {
		Map<String, Object> result = new CaseInsensitiveHashMap();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();

		for (int i = 1; i <= cols; i++) {
			String columnName = rsmd.getColumnLabel(i);
			if (null == columnName || 0 == columnName.length()) {
				columnName = rsmd.getColumnName(i);
			}
			result.put(columnName, rs.getObject(i));
		}

		return result;
	}

	private static class CaseInsensitiveHashMap extends LinkedHashMap<String, Object> {

		private final Map<String, String> lowerCaseMap = new HashMap<String, String>();

		private static final long serialVersionUID = -2848100435296897392L;

		@Override
		public boolean containsKey(Object key) {
			Object realKey = lowerCaseMap.get(key.toString().toLowerCase(Locale.ENGLISH));
			return super.containsKey(realKey);
			// Possible optimisation here:
			// Since the lowerCaseMap contains a mapping for all the keys,
			// we could just do this:
			// return lowerCaseMap.containsKey(key.toString().toLowerCase());
		}

		@Override
		public Object get(Object key) {
			Object realKey = lowerCaseMap.get(key.toString().toLowerCase(Locale.ENGLISH));
			return super.get(realKey);
		}

		@Override
		public Object put(String key, Object value) {
			/*
			 * In order to keep the map and lowerCaseMap synchronized, we have
			 * to remove the old mapping before putting the new one. Indeed,
			 * oldKey and key are not necessaliry equals. (That's why we call
			 * super.remove(oldKey) and not just super.put(key, value))
			 */
			Object oldKey = lowerCaseMap.put(key.toLowerCase(Locale.ENGLISH), key);
			Object oldValue = super.remove(oldKey);
			super.put(key, value);
			return oldValue;
		}

		@Override
		public void putAll(Map<? extends String, ?> m) {
			for (Map.Entry<? extends String, ?> entry : m.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				this.put(key, value);
			}
		}

		@Override
		public Object remove(Object key) {
			Object realKey = lowerCaseMap.remove(key.toString().toLowerCase(Locale.ENGLISH));
			return super.remove(realKey);
		}
	}

}
