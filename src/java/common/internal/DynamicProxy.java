package common.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicProxy implements InvocationHandler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Object target; // 代理目标
	private Object proxy; // 代理对象

	private static HashMap<Class<?>, DynamicProxy> invoHandlers = new HashMap<Class<?>, DynamicProxy>();

	private DynamicProxy() {
	}

	/**
	 * 通过Class来生成动态代理对象Proxy
	 * 
	 * @param clazz
	 * @return
	 */
	public synchronized static <T> T getProxyInstance(Class<T> clazz) {
		DynamicProxy invoHandler = invoHandlers.get(clazz);

		if (null == invoHandler) {
			invoHandler = new DynamicProxy();
			try {
				T tar = clazz.newInstance();
				invoHandler.setTarget(tar);
				invoHandler.setProxy(Proxy.newProxyInstance(tar.getClass().getClassLoader(), tar.getClass()
						.getInterfaces(), invoHandler));
			} catch (Exception e) {
				e.printStackTrace();
			}
			invoHandlers.put(clazz, invoHandler);

		}

		return (T) invoHandler.getProxy();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		Object result = method.invoke(target, args); // 执行业务处理

		after(method, args, result);
		return result;
	}

	protected void before() {

	}

	protected void after(Method method, Object[] args, Object result) {
		// 打印日志
		logger.debug("____invoke method: " + method.getName() + "; args: "
				+ (null == args ? "null" : Arrays.asList(args).toString()) + "; return: " + result);
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public Object getProxy() {
		return proxy;
	}

	public void setProxy(Object proxy) {
		this.proxy = proxy;
	}

	public static void main(String[] args) {

	}

}
