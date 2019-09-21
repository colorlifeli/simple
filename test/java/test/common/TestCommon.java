package test.common;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.common.internal.BeanContextTest;
import test.common.internal.MockTest;
import test.common.jdbcutil.H2HelperTest;
import test.common.jdbcutil.H2Test;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	BeanContextTest.class,MockTest.class,H2HelperTest.class,H2Test.class
})
public class TestCommon {

}
