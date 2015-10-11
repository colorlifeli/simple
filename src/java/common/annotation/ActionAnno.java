package common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.ActionIf;

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
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Action {
		String path() default "";

		Result[] targets();
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
			classPath = p.path();
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
			Object ret = method.invoke(action);

			if (ret != null && ret.getClass().equals(String.class)) {
				return results.get(ret);
			}

			return null;

		}

		@Override
		public String toString() {

			String str = "";
			for (String key : results.keySet()) {
				str = str + key + ":" + results.get(key) + " ";
			}

			return String
					.format("action info:%s, %s, %s, %s", path, action.getClass().getName(), method.getName(), str);
		}

	}
}
