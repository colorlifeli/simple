package me.common.internal;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.common.annotation.IocAnno;
import me.common.util.Constant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * bean 管理器
 * 
 * packages：扫描哪些包下的类
 * 将类转化为 bean 信息，并对每个类进行实例化，存储到 map 之中。
 * 
 * 注意：在这里作了一个约定：bean id 是类名字的首字母改为小写，可见，被管理的bean 所对应的类不同同名！
 * 
 * 2016.04.12 
 * 增加 newInstance 方法，某些类需要建多个实例以便每个实例拥有自己的数据。提供自动注入
 * BeanContext改为单例，方便随时可以拿到 beanContext 进行 newInstance
 * @author James
 *
 */
public class BeanContext {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, Class<?>> beanClazzs = new HashMap<String, Class<?>>();
	private final Map<String, Object> beans = new HashMap<String, Object>();

	public static class holder {
		private static final BeanContext instance = new BeanContext();
	}
	
	private BeanContext() {
		readBeans();
		setProperty();
	}
	
	public static final BeanContext me() {
		return holder.instance;
	}

	private void readBeans() {

		// 读取包下所有类
		List<Class<?>> classList = new ArrayList<Class<?>>();
		for (String packageName : Constant.web.packages) {
			ScanPackage.scan(packageName, classList);
		}

		// 将所有类放到 bean 列表中去
		for (Class<?> clazz : classList) {
			// 将 class的名字的首字母改为小写，以此作为 bean 的id
			if (clazz != null && clazz.getName() != null && !clazz.getName().isEmpty()) {
				if (clazz.getName().contains("$")) {
					// 内部类由外部类实例化，不在这里实例化
					continue;
				}
				if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
					// 接口和抽象类无法实例化
					continue;
				}
				String id = clazz.getSimpleName();
				id = id.replaceFirst(id.substring(0, 1), id.substring(0, 1).toLowerCase());
				try {
					beans.put(id, clazz.newInstance());
					beanClazzs.put(id, clazz);
				} catch (InstantiationException | IllegalAccessException e) {
					logger.error("创建类实例错误, class:" + clazz);
				}
			}
		}
	}

	/**
	 * 对bean依赖的对象进行注入
	 * 有 Ioc 注解的进行注入
	 */
	private void setProperty() {
		for (Entry<String, Object> entry : beans.entrySet()) {
			IocAnno.processor(entry.getValue(), beanClazzs.get(entry.getKey()), beans);
		}
	}

	/**
	 * 根据 id 获取 bean 实例
	 * @param id
	 * @return
	 */
	public Object getBean(String id) {
		return this.beans.get(id);
	}

	public Map<String, Object> getAllBeans() {
		return beans;
	}

	/**
	 * 为类创建一个实例，并自动注入
	 * @param clazz
	 * @return
	 */
	public <T> T newInstance(Class<T> clazz) {
		try {
			T t = clazz.newInstance();
			IocAnno.processor(t, clazz, beans);
			return t;
		} catch(Exception e) {
			logger.error("创建类实例错误, class:" + clazz + ", msg:" + e.getMessage());
		}
		
		return null;
	}
}
