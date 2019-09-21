package me.common.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.common.Config;

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
		this.setProxy(Config.net.proxy_ip, Config.net.proxy_port);
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

	/**
	 * 从url下载文件
	 * @param urlStr
	 * @param fileName 完整路径文件名
	 * @return
	 */
	public boolean saveUrlAs(String urlStr, String fileName) {
		// 此方法只能用户HTTP协议
		try {

			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			DataInputStream in = new DataInputStream(connection.getInputStream());
			DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
			byte[] buffer = new byte[4096];

			int count = 0;
			while ((count = in.read(buffer)) > 0) {
				out.write(buffer, 0, count);
			}

			out.close();
			in.close();

			return true;
		} catch (Exception e) {
			return false;
		}

	}
}