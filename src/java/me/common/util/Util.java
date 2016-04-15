package me.common.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

	public static <T> void sort(List<T> list, Class<T> clazz, final String sort, final String order) {
		Collections.sort(list, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				Field field;
				try {
					field = clazz.getDeclaredField(sort);
					field.setAccessible(true);
					String classname = field.getType().getName();

					Object value1 = field.get(o1);
					Object value2 = field.get(o2);

					switch (classname) {
					case "int":
						int i1 = (int) value1;
						int i2 = (int) value2;
						if ("desc".equals(order.toLowerCase())) //降序
						{
							if (i1 > i2)
								return -1;
							else if (i1 == i2)
								return 0;
							else if (i1 < i2)
								return 1;
						} else {
							if (i1 > i2)
								return 1;
							else if (i1 == i2)
								return 0;
							else if (i1 < i2)
								return -1;
						}
						//break;
					case "java.lang.Integer":
						Integer ii1 = (Integer) value1;
						Integer ii2 = (Integer) value2;
						if ("desc".equals(order.toLowerCase())) //降序
							return ii2.compareTo(ii1);
						else
							return ii1.compareTo(ii2);
						//break;
					case "float":
					case "java.lang.Float":
					case "double":
					case "java.lang.Double":
					case "long":
					case "java.lang.Long":
					case "java.math.BigDecimal":
						BigDecimal bg1 = (BigDecimal) value1;
						BigDecimal bg2 = (BigDecimal) value2;
						if ("desc".equals(order.toLowerCase())) //降序
							return bg2.compareTo(bg1);
						else
							return bg1.compareTo(bg2);
					case "java.lang.String":
						if ("desc".equals(order.toLowerCase())) //降序
							return value2.toString().compareTo(value1.toString());
						else
							return value1.toString().compareTo(value2.toString());

					default:
						//不支持的类型
						return 0;
					}

				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}

				return 0;
			}
		});
	}
}
