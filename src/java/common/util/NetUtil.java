package common.util;

/**
 * 本实例提供网络相关的公共操作
 * 单例模式的一种，使用内部静态类。利用了classloader线程安全的特性来解决单例的并发问题
 * 
 * @author James
 *
 */
public class NetUtil {
	public static class holder {
		private static final NetUtil instance = new NetUtil();
	}

	private NetUtil() {
	};

	public static final NetUtil me() {
		return holder.instance;
	}

	/**
	 * 为系统访问网络增加代理服务器,采用系统默认值
	 */
	public void setProxy() {
		this.setProxy(Constant.net.proxy_ip, Constant.net.proxy_port);
	}

	/**
	 * 为系统访问网络增加代理服务器
	 * 
	 * @param ip
	 * @param port
	 */
	public void setProxy(String ip, String port) {
		System.getProperties().setProperty("proxySet", "true");
		System.getProperties().setProperty("http.proxyHost", ip);
		System.getProperties().setProperty("http.proxyPort", port);
	}
}