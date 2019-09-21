package me.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.Config;

/**
 * ioc 依赖注入注解。用于标记某个 field自动注入
 * 		name: 注入的对象名称，默认是 field 的名字。对象名称是 类名，首字母小写
 * 
 * 20180412:
 * 		增加对父类属性的注入
 * @author james
 *
 */
public class IocAnno {
	private static Logger logger = LoggerFactory.getLogger(IocAnno.class);

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Ioc {
		String name() default "";
	}

	public static <T> void processor(Object obj, Class<T> clazz, Map<String, Object> beans) {
		Field[] fields = clazz.getDeclaredFields();
		List<Field> fieldList = new ArrayList<Field>(Arrays.asList(fields));
		//最多只能继承一个父类, 循环直到父类是Object类
		for (Class<?> superClass = clazz.getSuperclass(); superClass != Object.class; 
				superClass = superClass.getSuperclass()) {
			//父类是本地父类，才考虑要注入
			for (String packageName : Config.web.packages) {
				if(superClass.getName().startsWith(packageName)) {
					Field[] fields2 = superClass.getDeclaredFields();
					fieldList.addAll(Arrays.asList(fields2));
				}
			}
		}
		for (Field field : fieldList) {
			if (field.isAnnotationPresent(Ioc.class)) {
				Ioc ioc = field.getAnnotation(Ioc.class);
				String name = ioc.name();
				String fieldName = field.getName();
				if (name == null || "".equals(name.trim())) {
					name = fieldName;
				}
				Object bean = beans.get(name);
				if (bean == null) {
					logger.error("注入失败，找不到bean:" + name + ",class:" + clazz);
					return;
				}

				// 直接赋值来进行注入
				try {
					field.setAccessible(true);
					field.set(obj, bean);

					//logger.debug("注入成功，fieldname：" + fieldName + ",class:" + clazz);
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					logger.error("注入失败，fieldname：" + fieldName + ",class:" + clazz);
				}

				/*
				 * 使用setter方法进行注入 String methodName = "set" +
				 * fieldName.replaceFirst(fieldName.substring(0, 1),
				 * fieldName.substring(0, 1).toUpperCase()); try { Method set =
				 * clazz.getDeclaredMethod(methodName, bean.getClass());
				 * set.invoke(obj, bean);
				 * 
				 * logger.info( "注入成功。class:" + clazz + ", field:" + fieldName +
				 * ", ioc bean's class:" + bean.getClass()); } catch (Exception
				 * e) { logger.error("注入失败，无法调用setter：" + methodName + ",class:"
				 * + clazz); }
				 */

			}
		}
	}
}
