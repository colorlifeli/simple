package me.common;

public class Config {

	public static class db {

		// ***** db start parameters ******* //

		//public final static String url_server = "jdbc:h2:tcp://localhost/~/db/simple";
		public static String url_server = "jdbc:h2:tcp://localhost/d:/develop/db/simple";
		//public final static String url_embeded = "jdbc:h2:~/db/simple";
		public static String url_embeded = "jdbc:h2:d:/develop/db/simple";
		//public final static String url_test_embeded = "jdbc:h2:~/db/test";
		//public final static String url_test_embeded = "jdbc:h2:d:/develop/db/test";
		public static String user = "sa";
		public static String password = "sa";
		// 如使用 embeded模式，则置 为 null
		public final static String tcpServer = "-tcpAllowOthers";

		// ***** db start parameters end ******* //
	}

	public static class net {
		public final static String proxy_ip = "proxy.piccnet.com.cn";
		public final static String proxy_port = "3128";
	}

	// 和 web 页面相关的，如 action 等
	public static class web {
		public final static String jspPrefix = "/WEB-INF/jsp/";

		// bean 扫描目录
		public static String[] packages = { "me.web", "me.net", "me.service" };
	}

	// stock 相关
	public class stock {
		// 开盘时间
		public final static String morningStart = "09:30";
		public final static String morningEnd = "11:30";
		public final static String afternoonStart = "13:00";
		public final static String afternoonEnd = "15:00";
	}

	//控制模拟计算的各种参数
	public class simulate {
		public final static boolean isNeedK = false; //是否需要独立k
		public final static String startDate = "2015-06-01"; //选择一个比较差的时间段，希望能有一个好一点的结果
	}

}
