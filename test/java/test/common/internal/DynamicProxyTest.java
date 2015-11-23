package test.common.internal;

import me.common.internal.DynamicProxy;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicProxyTest {
	// private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void invokeTest() {
		BusinessObjectIf obj2 = DynamicProxy.getProxyInstance(BusinessObject.class);
		obj2.businessFunc();
	}

}

interface BusinessObjectIf {
	public String businessFunc();
}

class BusinessObject implements BusinessObjectIf {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public String businessFunc() {

		logger.debug("enter business function");
		return "return value";
	}
}
