package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.h2.tools.Server;
import org.h2.util.StringUtils;

public class H2StarterListener implements ServletContextListener {

	private Connection conn;
	private Server server;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO 在配置文件中配置

		// 在 web.xml 中配置 servlet context init parameters，例子如下：
		/**
		 * 	<context-param>
				<param-name>db.tcpServer</param-name>
				<param-value>-tcpAllowOthers</param-value>
			</context-param>
		 */
		// 默认情况下使用嵌入方式，性能比server模式好很多
		// 没有使用连接池，在访问不是十分频繁的情况下，不需要连接池
		// 使用 connection：
		// Connection conn = getServletContext().getAttribute("connection");
		try {
			org.h2.Driver.load();

			ServletContext servletContext = arg0.getServletContext();
			String url = getParameter(servletContext, "db.url", "jdbc:h2:~/simple");
			String user = getParameter(servletContext, "db.user", "sa");
			String password = getParameter(servletContext, "db.password", "sa");

			// Start the server if configured to do so
			String serverParams = getParameter(servletContext, "db.tcpServer", null);
			if (serverParams != null) {
				String[] params = StringUtils.arraySplit(serverParams, ' ', true);
				server = Server.createTcpServer(params);
				server.start();
			}

			conn = DriverManager.getConnection(url, user, password);
			servletContext.setAttribute("connection", conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			Statement stat = conn.createStatement();
			stat.execute("SHUTDOWN");
			stat.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	private static String getParameter(ServletContext servletContext, String key, String defaultValue) {
		String value = servletContext.getInitParameter(key);
		return value == null ? defaultValue : value;
	}

}
