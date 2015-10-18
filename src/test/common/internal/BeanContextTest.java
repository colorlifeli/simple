package common.internal;

import org.junit.Assert;
import org.junit.Test;

import web.example.HelloAction;

public class BeanContextTest {

	@Test
	public void testReadBean() {
		BeanContext beanContext = new BeanContext();
		Object hello = beanContext.getBean("helloAction");

		Assert.assertEquals(HelloAction.class.getName(), hello.getClass().getName());
		Assert.assertEquals("hello", ((HelloAction) hello).getHello());
	}

}
