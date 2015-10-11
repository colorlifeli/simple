package common.internal;

import org.junit.Assert;
import org.junit.Test;

import common.testutil.Mock;

public class MockTest {

	@Test
	public void testMock() {

		Calculator tested = new Calculator();
		Adder adder = Mock.createMock(Adder.class);
		tested.setAdder(adder);

		// 记录参数及其预期返回值
		Mock.expect(adder.add(1, 2)).andReturn(3);

		// 如果参数一致，则调用预期结果
		Assert.assertEquals(3, tested.add(1, 2));

	}

	class Calculator {

		private Adder adder;

		public void setAdder(Adder adder) {

			this.adder = adder;
		}

		public int add(int x, int y) {
			return adder.add(x, y);
		}

	}

	interface Adder {

		public int add(int x, int y);
	}
}
