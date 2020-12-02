package me.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.SimpleException;

/**
 * 其它公用方法
 * 
 * @author James
 *
 */
public class Util {
	

	public static Logger logger = LoggerFactory.getLogger("common.logger");

	// 按照 field1:value1,field2:value3,...的格式打印对象的值
	public static String printFields(Class<?> clazz, Object object) {
		StringBuffer sb = new StringBuffer("");

		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if (field.getName().contains("this"))
				continue;
			try {
				if(field.getType().getName().equals("java.math.BigDecimal")){
					sb.append(String.format("%s:%.2f,", field.getName(), field.get(object)));
				} else
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

	/**
	 * list 深拷贝
	 * @param src
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static <T> List<T> deepCopy(List<T> src) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(byteOut);
			out.writeObject(src);

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in = new ObjectInputStream(byteIn);
			@SuppressWarnings("unchecked")
			List<T> dest = (List<T>) in.readObject();
			return dest;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}

	}

	/**
	 * 取较大值。针对String 类型的数字
	 * @param num1
	 * @param num2
	 * @return
	 */
	public static String max(String num1, String num2) {
		if (Double.parseDouble(num1) > Double.parseDouble(num2))
			return num1;
		else
			return num2;
	}

	/**
	 * 取较小值。针对String 类型的数字
	 * @param num1
	 * @param num2
	 * @return
	 */
	public static String min(String num1, String num2) {
		if (Double.parseDouble(num1) < Double.parseDouble(num2))
			return num1;
		else
			return num2;
	}
	
	/** 
	 *  
	 * 1 第一季度 2 第二季度 3 第三季度 4 第四季度 
	 *  
	 * @param date 
	 * @return 
	 */
	public static int getSeason(Date date) {

		int season = 0;

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int month = c.get(Calendar.MONTH);
		switch (month) {
		case Calendar.JANUARY:
		case Calendar.FEBRUARY:
		case Calendar.MARCH:
			season = 1;
			break;
		case Calendar.APRIL:
		case Calendar.MAY:
		case Calendar.JUNE:
			season = 2;
			break;
		case Calendar.JULY:
		case Calendar.AUGUST:
		case Calendar.SEPTEMBER:
			season = 3;
			break;
		case Calendar.OCTOBER:
		case Calendar.NOVEMBER:
		case Calendar.DECEMBER:
			season = 4;
			break;
		default:
			break;
		}
		return season;
	}
	
	/**
	 * 返回对象的 <field_name, field_value>键值对
	 * @param obj
	 * @return
	 * @throws  
	 */
	public static Map<String, String> getMapFromObject(Object obj) {

		Map<String, String> map = new HashMap<String, String>();
		if(obj == null)
			return map;
		Field[] ff = obj.getClass().getDeclaredFields();
		for (Field f : ff) {
			f.setAccessible(true);
			try {
				if(f.get(obj) != null && !TypeUtil.isEmpty(String.valueOf(f.get(obj)))) {
					map.put(f.getName(), f.get(obj).toString());
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		return map;
	}
	
	/**
	 * 将list合并为 str1,str2,str3...
	 * @param strs
	 * @return
	 */
	public static String getString(List<String> strs) {
		StringBuilder sb = new StringBuilder();
		for(String str : strs) {
			sb.append(str).append(",");
		}
		return sb.substring(0, sb.length()-1).toString();
	}
	
	/**
	 * 计算2个日期之间天数
	 * @param startDate：yyyy-MM-dd
	 * @param endDate：yyyy-MM-dd
	 * @return
	 */
	public static int totalDays(String startDate, String endDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date = formatter.parse(startDate);
			long start = date.getTime();
			Date date1 = formatter.parse(endDate);
			long end = date1.getTime();

			Long l = (end - start) / (1000 * 60 * 60 * 24);

			return l.intValue();
			
		} catch (Exception e) {
			throw new SimpleException("totalDays error", e);
		}
		
	}

	/**
	 * 计算2个日期之间天数
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static int totalDays(Date start, Date end) {
		try {

			Long l = (end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24);

			return l.intValue();
			
		} catch (Exception e) {
			throw new SimpleException("totalDays error", e);
		}
		
	}
	public static void main(String[] args) {
		int i = totalDays("2019-12-01", "2020-01-01");
		logger.info(i+"");
	}
}
