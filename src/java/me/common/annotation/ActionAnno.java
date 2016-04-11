package me.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.ActionIf;
import me.common.util.Constant;
import me.common.util.TypeUtil;

/**
 * action 注解，并且包含了注解处理方法
 * 
 * action 的path是 restful的，不带后缀
 * 由三个注解构成：
 * 		Pack: class 层面，可选，用于标记 action path的一部分
 * 		Action: 方法层面，标记每个 action 方法，必须有 path, target信息
 * 		Result: action 的 target 一般有多个结果，指向不同的url
 * 
 * 由三个层次组成：/ns/pack/action
 * 		Pack.ns: namespace
 * 		pack:package. 一般每个 action class 都放在同一个pack 下
 * 		action: 对应action class 里的某个方法
 * 
 * 注解处理方法：
 * 		将传入的每个 action 的信息包装为 ActionInfo 对象，并存储在一个静态的 map 当中，key 为 action path。
 * 		使用 ActionAnno.actions.get(pathName); 获得 pathName对应的处理 action
 * 
 * ns 和 path 均不需要以 '/' 开头
 * 
 * ！！！注意，当前action是单例的，如果要多例，actions map 应保存 action的class，在execute是再实例化，并调用 ioc processor 来注入 service。
 * ！！！ 由于是单例，但在action实例化时并没有实例化它的field对象（除ioc外），因此 action 的field如果不是基本类型，则需在声明时实例化。
 * 
 * @author James
 *
 */
public class ActionAnno {

	private static Logger logger = LoggerFactory.getLogger(ActionAnno.class);
	public static final Map<String, ActionInfo> actions = new HashMap<String, ActionInfo>();

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Result {
		String name();

		String value() default "";
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Pack {
		String path() default "";

		String ns() default "";
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Action {
		String path(); // 不能为空

		Result[]targets(); // 不能为空
	}

	/**
	 * 假设传进来对象的类的有action注解
	 * 
	 * @param obj
	 */
	public static void processor(ActionIf obj) {

		String classPath = "";
		if (obj.getClass().isAnnotationPresent(Pack.class)) {
			Pack p = obj.getClass().getAnnotation(Pack.class);
			if (!"".equals(p.path().trim()))
				classPath = "/" + p.path();
			if (!"".equals(p.ns().trim()))
				classPath = "/" + p.ns() + classPath;
		}
		for (Method m : obj.getClass().getMethods()) {
			if (m.isAnnotationPresent(Action.class)) {
				Action tag = m.getAnnotation(Action.class);

				String path = classPath + "/" + tag.path();
				ActionInfo a = new ActionInfo(path, obj, m);
				Map<String, String> resultsMap = new HashMap<String, String>();
				Result[] results = tag.targets();
				for (Result result : results) {
					resultsMap.put(result.name(), result.value());
				}
				a.setResults(resultsMap);

				actions.put(path, a);
			}
		}

	}

	/**
	 * 打印所有 action 的信息
	 */
	public static void printAllActionInfo() {
		for (ActionInfo info : actions.values()) {
			logger.info(info.toString());
		}
	}

	public static class ActionInfo {
		private String path;
		private ActionIf action;
		private Method method;
		private Map<String, String> results = new HashMap<String, String>();

		public ActionInfo(String path, ActionIf action, Method method) {
			this.path = path;
			this.action = action;
			this.method = method;
		}

		public Map<String, String> getResults() {
			return results;
		}

		public void setResults(Map<String, String> results) {
			this.results = results;
		}

		public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
			action.setRequest(request);
			action.setResponse(response);
			setParameter(request);

			Object ret = method.invoke(action);

			if (ret != null && ret.getClass().equals(String.class)) {
				//json
				if ("json".equals(results.get("json"))) {
					//返回的是 json 字符串
					//response.setContentType("text/json");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write((String) ret);
					return null;
				}

				//jsp
				String url = results.get(ret);
				if (url.endsWith(".jsp")) {
					return Constant.web.jspPrefix + url;
				}
			}

			return null;

		}

		// 将页面参数 set 到 action属性
		// 如果参数名是 a.b ，则 b是a的属性，先查找 action 里是否有此属性
		private void setParameter(HttpServletRequest request) {

			Map<String, String[]> params = request.getParameterMap();

			Object obj = action;
			String actionName = action.getClass().getName();

			for (String key : params.keySet()) {

				//参数名字是否包含 .
				String[] namePart = key.split(".");
				if (namePart.length > 1) {
					Field field = null;
					try {
						field = action.getClass().getDeclaredField(namePart[0]);
						field.setAccessible(true);

						Object fieldObj = field.get(action);
						TypeUtil.setField(fieldObj, namePart[1], fieldObj);

					} catch (Exception e) {
						logger.error(String.format("赋值失败. field %s, action:%s ", namePart[0], actionName));
						continue;
					}

				}

				String[] values = params.get(key);

				Field field = action.getClass().getDeclaredField(key);
				field.setAccessible(true);
				String classname = field.getType().getName();

				if (values == null || values.length == 0 || values[0] == null) {
					//这里可能有问题，因为 value为数据的话，第一个可以是 null的。
					logger.info("value is null, parameter name:" + key);
				}
				switch (classname) {
				case "int":
					field.set(action, value);

				}
			}

			//			Field[] fields = action.getClass().getDeclaredFields();
			//			for (Field field : fields) {
			//					String fieldName = field.getName();
			//
			//					// 直接赋值来进行注入
			//					try {
			//						field.setAccessible(true);
			//						field.set(obj, bean);
			//
			//						//logger.debug("注入成功，fieldname：" + fieldName + ",class:" + clazz);
			//					} catch (IllegalArgumentException | IllegalAccessException e1) {
			//						logger.error("注入失败，fieldname：" + fieldName + ",class:" + clazz);
			//					}
		}

		@Override
		public String toString() {

			String str = "";
			for (String key : results.keySet()) {
				str = str + key + ":" + results.get(key) + " ";
			}

			return String.format("action info:%s, %s, %s, %s", path, action.getClass().getName(), method.getName(),
					str);
		}

	}
}
