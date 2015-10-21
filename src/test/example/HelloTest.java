package example;

import org.junit.Test;

public class HelloTest {

	@Test
	public void hellotest() {
		System.out.println("cccc");
	}

	@Test
	public void hellotest2() throws Exception {
		System.out.println("aaaaa");
		throw new Exception("aaa");
	}

	@Test
	public void tello() {
		System.out.println("bbbb");
	}

}
