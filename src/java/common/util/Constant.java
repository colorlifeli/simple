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
}
