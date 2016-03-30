package test.net;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 说明：
 * 执行顺序：
 * 1. StockSourceDaoTest.class 要先执行，因为由它初始化 codes
 * 2. StockSourceImpl1Test 获取实时与历史数据
 * @author James
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ StockSourceDaoTest.class, StockSourceImpl1Test.class, StockAnalysisDaoTest.class })
public class TestNet {

}
