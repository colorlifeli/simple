package test.common.internal;

import me.common.internal.BeanContext;
import me.web.example.HelloAction;

import org.junit.Assert;
import org.junit.Test;

public class BeanContextTest {

	@Test
	public void testReadBean() {
		BeanContext bc = BeanContext.me();
		Object hello = bc.getBean("helloAction");

		Assert.assertEquals(HelloAction.class.getName(), hello.getClass().getName());
		Assert.assertEquals("hello", ((HelloAction) hello).getHello());
	}

}
