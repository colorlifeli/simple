package test;

import org.junit.BeforeClass;

import me.common.Config;

/**
 * 对全局配置类进行配置
 * @author opq
 *
 */
public abstract class MyTest {

	@BeforeClass
	public static void beforeAll() {
		Config.db.url_embeded = "jdbc:h2:d:/develop/db/test";
		Config.web.packages = new String[]{ "test.common.annotion", "me.net", "me.service" };
	}
}
