package test.common.internal;

import org.junit.Assert;
import org.junit.Test;

import me.common.internal.BeanContext;
import test.MyTest;
import test.common.annotion.HelloAction;

public class BeanContextTest extends MyTest {

	@Test
	public void testReadBean() {
		BeanContext bc = BeanContext.me();
		Object hello = bc.getBean("helloAction");

		Assert.assertEquals(HelloAction.class.getName(), hello.getClass().getName());
		Assert.assertEquals("hello", ((HelloAction) hello).getHello());
	}

}
