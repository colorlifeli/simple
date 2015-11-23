package test.example;

import java.lang.reflect.Method;

import me.common.annotation.ActionAnno.Action;

import org.junit.Assert;
import org.junit.Test;

public class HelloActionTest {

	@Test
	public void testExecute() throws ClassNotFoundException {
		Class<?> targetClass = Class.forName("web.example.HelloAction");

		for (Method m : targetClass.getMethods()) {
			if (m.isAnnotationPresent(Action.class)) {
				Action hello = m.getAnnotation(Action.class);
				Assert.assertEquals("hello", hello.path());
			}
		}

	}

}
