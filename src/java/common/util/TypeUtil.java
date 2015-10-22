package common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 汇集类型相关的方法
 * 
 * @author James
 *
 */
public class TypeUtil {

	private static final Map<Class<?>, Object> emptyValues = new HashMap<Class<?>, Object>(9);

	static {
		emptyValues.put(Void.TYPE, null);
		emptyValues.put(Boolean.TYPE, Boolean.FALSE);
		emptyValues.put(Byte.TYPE, Byte.valueOf((byte) 0));
		emptyValues.put(Short.TYPE, Short.valueOf((short) 0));
		emptyValues.put(Character.TYPE, Character.valueOf((char) 0));
		emptyValues.put(Integer.TYPE, Integer.valueOf(0));
		emptyValues.put(Long.TYPE, Long.valueOf(0));
		emptyValues.put(Float.TYPE, Float.valueOf(0));
		emptyValues.put(Double.TYPE, Double.valueOf(0));
	}

	public static Object emptyValuesFor(Class<?> type) {
		// 是否为基本类型
		return type.isPrimitive() ? emptyValues.get(type) : null;
	}

	public enum StockDataType {
		realTime("R"), history("H");

		private StockDataType(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

		private String value;
	}

	public enum StockSource {
		SINA("sina"), YAHOO("yahoo");

		private StockSource(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

		private String value;
	}

	// 返回一个空对象，而不是 null
	public static final Object[] EMPTY_ARRAY = new Object[0];

	public static Object[][] oneToTwo(final Object[] var) {
		if (var.length == 0)
			return null;
		Object[][] result = new Object[var.length][1];
		for (int i = 0; i < var.length; i++) {
			result[i][0] = var[i];
		}
		return result;
	}

}
