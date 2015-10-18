package common.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.annotation.IocAnno;

/**
 * bean 管理器
 * 
 * packages：扫描哪些包下的类
 * 将类转化为 bean 信息，并对每个类进行实例化，存储到 map 之中。
 * 
 * 
 * 注意：在这里作了一个约定：bean id 是类名字的首字母改为小写，可见，被管理的bean 所对应的类不同同名！
 * 
 * @author James
 *
 */
public class BeanContext {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, Class<?>> beanClazzs = new HashMap<String, Class<?>>();
	private final Map<String, Object> beans = new HashMap<String, Object>();

	private final String[] packages = { "web.example" };

	public BeanContext() {
		readBeans();
		setProperty();
	}

	private void readBeans() {

		// 读取包下所有类
		List<Class<?>> classList = new ArrayList<Class<?>>();
		for (String packageName : packages) {
			ScanPackage.scan(packageName, classList);
		}

		// 将所有类放到 bean 列表中去
		for (Class<?> clazz : classList) {
			// 将 class的名字的首字母改为小写，以此作为 bean 的id
			if (clazz != null && clazz.getName() != null && !clazz.getName().isEmpty()) {
				String id = clazz.getSimpleName();
				id = id.replaceFirst(id.substring(0, 1), id.substring(0, 1).toLowerCase());
				try {
					beans.put(id, clazz.newInstance());
					beanClazzs.put(id, clazz);
				} catch (InstantiationException | IllegalAccessException e) {
					logger.error("创建类实例错误, id:" + id);
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

}
