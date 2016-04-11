package me.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.annotation.ActionAnno;

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
	 * 设置对象某个字段值
	 * @param obj
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public static void setField(Object obj, String name, Object value) throws Exception {
		Field field = obj.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(obj, value);
	}

	/**
	 * 设置对象某个字段的值。这时值为 string 数组，需要转换
	 * @param obj
	 * @param name
	 * @param values
	 * @throws Exception
	 */
	public static void setField(Object obj, String name, String[] values) {
		Field field = obj.getClass().getDeclaredField(name);
		field.setAccessible(true);
		String classname = field.getType().getName();

		if (values == null || values.length == 0) {
			logger.info("value is null, parameter name:" + name);
			return;
		}
		if (field.getType().isPrimitive() && values[0] == null) {
			logger.info("value is null, parameter name:" + name);
			return;
		}

		try {
			switch (classname) {
			case "int":
			case "java.lang.Integer":
				field.set(obj, Integer.parseInt(values[0]));
				break;
			case "float":
			case "java.lang.Float":
				field.set(obj, Float.parseFloat(values[0]));
				break;
			case "double":
			case "java.lang.Double":
				field.set(obj, Double.parseDouble(values[0]));
				break;
			case "long":
			case "java.lang.Long":
				field.set(obj, Long.parseLong(values[0]));
				break;
			case "boolean":
			case "java.lang.Boolean":
				field.set(obj, Boolean.parseBoolean(values[0])); // 当值为 "true" (不分大小写）时，表示 true
				break;

			case "java.lang.String":
				field.set(obj, values[0]);
				break;

			case "java.util.List": //field 的类型只能是 list，不能是其字类，不然很难处理。实例化为 ArrayList
				ParameterizedType pt = (ParameterizedType) field.getGenericType();
				Class lll = (Class) pt.getActualTypeArguments()[0];
				List<lll> list = new ArrayList<lll>();

			}

			if (field.getType().isAssignableFrom(List.class)) { //判断是否为List  
				//由泛型转为参数化类型，再获取其参数的类型 

				System.out.println("\t\t" + lll.getName());
			}
		} catch (Exception e) {
			String str = null;
			for (int i = 0; i < values.length; i++) {
				String value = values[i];
				str += value + ",";
			}
			logger.error(String.format("赋值 时出错, object class: %s,  field name:%s, values:%s", obj.getClass().getName(),
					name, str));
		}

	}

}
