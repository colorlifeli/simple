package me.common.util;

import java.lang.reflect.Field;

/**
 * 其它公用方法
 * 
 * @author James
 *
 */
public class Util {

	// 按照 field1:value1,field2:value3,...的格式打印对象的值
	public static String printFields(Class<?> clazz, Object object) {
		StringBuffer sb = new StringBuffer("");

		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if (field.getName().contains("this"))
				continue;
			try {
				sb.append(field.getName()).append(":").append(field.get(object)).append(",");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
