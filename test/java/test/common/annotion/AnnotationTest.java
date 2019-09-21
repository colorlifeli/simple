package test.common.annotion;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import me.common.annotation.ActionAnno.Action;
import me.common.annotation.ActionAnno.Pack;
import me.common.internal.BeanContext;

public class AnnotationTest {
	

	@Test
	public void testAction() throws ClassNotFoundException {
		Class<?> targetClass = Class.forName("test.common.annotion.HelloAction");
		if(targetClass.isAnnotationPresent(Pack.class)) {
			Pack hello = targetClass.getAnnotation(Pack.class);
			System.out.println("pack: " + hello.path());
			Assert.assertEquals("hello", hello.path());
		}

		for (Method m : targetClass.getMethods()) {
			if (m.isAnnotationPresent(Action.class)) {
				Action sayHello = m.getAnnotation(Action.class);
				System.out.println("path: " + sayHello.path());
				Assert.assertEquals("sayHello", sayHello.path());
			}
		}
		
	}
	
	@Test 
	public void testIoc() {
		BeanContext bc = BeanContext.me();
		Object hello = bc.getBean("helloAction");

		Assert.assertEquals("hello", ((HelloAction) hello).getHello());
	}

}
