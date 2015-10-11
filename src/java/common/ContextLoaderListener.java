package common;

import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.annotation.ActionAnno;
import common.internal.BeanContext;

public class ContextLoaderListener implements ServletContextListener {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {

		logger.info("start up...");

		BeanContext bc = new BeanContext();
		Map<String, Object> beans = bc.getAllBeans();

		for (Object bean : beans.values()) {
			// 对 action 进行注解处理
			if (bean instanceof ActionIf) {
				ActionAnno.processor((ActionIf) bean);
			}
		}

		ActionAnno.printAllActionInfo();

		logger.info("context init end");
	}

}
