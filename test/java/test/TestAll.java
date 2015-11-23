package test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.common.TestCommon;
import test.net.TestNet;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestCommon.class, TestNet.class })
public class TestAll {

}
