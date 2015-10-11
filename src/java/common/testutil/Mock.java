package common.testutil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.util.TypeUtil;

/**
 * 模拟指定接口或类的实例，用于单元测试
 * 其中：
 * createMock: 根据接口或类产生一个代理对象
 * except: 后面紧跟 andReturn 方法，表示对此方法的指定参数赋予一个预期的返回值
 * andReturn： 和 except 配合使用，如上
 * 
 * 说明：参考了 easyMock，进行了简化
 * @author James
 *
 */
public class Mock implements InvocationHandler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Object target; // 代理目标
	private Object proxy; // 代理对象

	private static HashMap<Class<?>, Mock> invoHandlers = new HashMap<Class<?>, Mock>();

	private static List<InvoInfo> mockInvoInfos = new ArrayList<InvoInfo>(); // 模拟的调用信息

	private Mock() {
	}

	/**
	 * 通过Class来生成动态代理对象Proxy
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public synchronized static <T> T createMock(Class<T> clazz) {
		Mock invoHandler = invoHandlers.get(clazz);

		if (null == invoHandler) {
			invoHandler = new Mock();
			try {
				if (clazz.isInterface()) {
					invoHandler.setProxy(Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz },
							invoHandler));
				} else {
					invoHandler.setProxy(Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(),
							invoHandler));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			invoHandlers.put(clazz, invoHandler);

		}

		return (T) invoHandler.getProxy();
	}

	/**
	 * 如果在模拟调用的列表中没有查到此调用，则认为此调用是模拟调用
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		InvoInfo invo = new InvoInfo(this, method, args);
		for (int i = 0; i < mockInvoInfos.size(); i++) {
			if (mockInvoInfos.get(i).matches(invo)) {
				return mockInvoInfos.get(i).returnValue;
			}
		}
		// 新增模拟调用
		mockInvoInfos.add(new InvoInfo(this, method, args));
		return TypeUtil.emptyValuesFor(method.getReturnType());
	}

	/**
	 * 选择最后一个模拟调用
	 * 
	 * @param value
	 * @return
	 */
	public static Mock expect(Object value) {
		if (mockInvoInfos.size() == 0) {
			return null;
		}
		return mockInvoInfos.get(mockInvoInfos.size() - 1).getMockObject();
	}

	/**
	 * 选择最后一个模拟调用来设置其返回值
	 * 
	 * 没有考虑并行
	 * 
	 * @param value
	 */
	public void andReturn(Object value) {
		mockInvoInfos.get(mockInvoInfos.size() - 1).setReturnValue(value);
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

	/**
	 * 内部类，保存调用信息，包括：
	 * mockObject：代理对象
	 * method：函数
	 * args：函数参数
	 * returnValue：返回值
	 * 
	 * @author James
	 *
	 */
	class InvoInfo {
		private final Mock mockObject;
		private final Method method;
		private final Object[] args;
		private Object returnValue;

		public InvoInfo(Mock mockObject, Method method, Object[] args) {
			this.mockObject = mockObject;
			this.method = method;
			this.args = args;
		}

		public boolean matches(InvoInfo invo) {
			// 判断是否完全相等，均使用 equals 方法
			if (this.mockObject.equals(invo.getMockObject()) && this.method.equals(invo.getMethod())) {
				// 判断所有参数是否相等
				boolean allEqual = true;
				if (args.length == invo.getArgs().length) {
					for (int i = 0; i < args.length; i++) {
						if (!args[i].equals(invo.getArgs()[i])) {
							allEqual = false;
						}
					}
				} else {
					allEqual = false;
				}
				return allEqual;
			}
			return false;
		}

		public Method getMethod() {
			return method;
		}

		public Object[] getArgs() {
			return args;
		}

		public Object getReturnValue() {
			return returnValue;
		}

		public void setReturnValue(Object returnValue) {
			this.returnValue = returnValue;
		}

		public Mock getMockObject() {
			return mockObject;
		}
	}

}
