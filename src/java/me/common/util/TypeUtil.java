package me.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.common.annotation.ActionAnno;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 汇集类型相关的方法
 * 
 * @author James
 *
 */
public class TypeUtil {

	private static Logger logger = LoggerFactory.getLogger(ActionAnno.class);
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

	// 返回一个空对象，而不是 null
	public static final Object[] EMPTY_ARRAY = new Object[0];

	/**
	 * 一维对象变二维
	 * @param var
	 * @return
	 */
	public static Object[][] oneToTwo(final Object[] var) {
		if (var.length == 0)
			return null;
		Object[][] result = new Object[var.length][1];
		for (int i = 0; i < var.length; i++) {
			result[i][0] = var[i];
		}
		return result;
	}

	public static boolean isEmpty(String value) {
		return (value == null || "".equals(value.trim()));
	}

	/**
	 * 根据class，将 string 值转换为对应类型的值。支持基本类型及相应的对象类型
	 * @param value
	 * @param classname
	 * @return
	 */
	public static Object stringValueToObject(String value, Class<?> clazz) {
		String classname = clazz.getName();
		Object obj = null;
		try {
			switch (classname) {
			case "int":
			case "java.lang.Integer":
				obj = Integer.parseInt(value);
				break;
			case "float":
			case "java.lang.Float":
				obj = Float.parseFloat(value);
				break;
			case "double":
			case "java.lang.Double":
				obj = Double.parseDouble(value);
				break;
			case "long":
			case "java.lang.Long":
				obj = Long.parseLong(value);
				break;
			case "boolean":
			case "java.lang.Boolean":
				obj = Boolean.parseBoolean(value); // 当值为 "true" (不分大小写）时，表示 true
				break;

			case "java.lang.String":
				obj = value;
				break;

			default:
				obj = null;
			}
		} catch (Exception e) {
			logger.error(String.format("由string转换值出错, value: %s, class name:%s", value, classname));
		}
		return obj;
	}

	/**
	 * 设置对象某个字段的值。这时值为 string 数组，需要转换
	 * 字段的类型为基本类型 或 list类型
	 * @param obj
	 * @param name:字段名称
	 * @param values
	 * @throws Exception
	 */
	public static void setField(Object obj, String name, String[] values) throws Exception {
		Field field = obj.getClass().getDeclaredField(name);
		field.setAccessible(true);
		Class<?> clazz = field.getType();

		if (values == null || values.length == 0) {
			logger.info("value is null, parameter name:" + name);
			return;
		}
		if (field.getType().isPrimitive() && values[0] == null) {
			logger.info("value is null, parameter name:" + name);
			return;
		}

		if (List.class.equals(clazz)) {
			//list类型
			//field 的类型只能是 list，不能是其字类，不然很难处理。实例化为 ArrayList
			//由泛型转为参数化类型，再获取其参数的类型
			ParameterizedType pt = (ParameterizedType) field.getGenericType();
			Class<?> parameterClass = (Class<?>) pt.getActualTypeArguments()[0];
			List<Object> list = new ArrayList<Object>();
			for (String value : values) {
				list.add(stringValueToObject(value, parameterClass));
			}
			field.set(obj, list);
		} else {
			//基本类型
			field.set(obj, stringValueToObject(values[0], clazz));
		}

	}

}
