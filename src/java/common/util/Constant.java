package common.util;

public class Constant {

	public class db {

		// ***** db start parameters ******* //

		public final static String url = "jdbc:h2:~/db/simple";
		public final static String user = "sa";
		public final static String password = "sa";
		// 如使用 embeded模式，则置 为 null
		public final static String tcpServer = "-tcpAllowOthers";

		// ***** db start parameters end ******* //
	}

	public class net {
		public final static String proxy_ip = "proxy.piccnet.com.cn";
		public final static String proxy_port = "3128";
	}

	// 和 web 页面相关的，如 action 等
	public class web {
		public final static String jspPrefix = "/WEB-INF/jsp/";
	}

	// stock 相关
	public class stock {
		// 开盘时间
		public final static String morningStart = "09:30";
		public final static String morningEnd = "11:30";
		public final static String afternoonStart = "13:00";
		public final static String afternoonEnd = "15:00";
	}
}
