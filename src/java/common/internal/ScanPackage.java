package common.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanPackage {
	private static Logger logger = LoggerFactory.getLogger(ScanPackage.class);

	/**
	 * 遍历包 并遍历子包
	 */
	public static void scan(String packageName, List<Class<?>> list) {
		String path = null;
		try {
			path = getSrcPath() + packageToDir(packageName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (path == null) {
			logger.error("进行包扫描时出错，找不到源路径");
			return;
		}

		File dir = new File(path);
		File[] files = dir.listFiles();
		Class<?> cla = null;
		for (File f : files) {
			if (f.isDirectory()) {
				String childName = packageName + "." + f.getName();
				scan(childName, list);

			} else {
				try {
					cla = Class.forName(packageName + "." + f.getName().split("\\.")[0]);
				} catch (ClassNotFoundException e) {
					logger.error("进行包扫描时出错，找不到类， class name:" + packageName + "." + f.getName().split("\\.")[0]);
				}
				list.add(cla);
			}
		}
	}

	/**
	 * 获取源码根路径
	 * @throws IOException 
	 */
	private static String getSrcPath() throws IOException {
		String path = ScanPackage.class.getResource("ScanPackage.class").getPath().substring(1);
		path = path.substring(0, path.indexOf("common"));
		path = path.substring(0, path.length() - 1);

		// File file = new File("");
		// String path = file.getCanonicalPath() + File.separator + "src" +
		// File.separator + "java";
		return path;
	}

	/**
	 * package转换为路径格式
	 */
	private static String packageToDir(String packageName) {
		String[] array = packageName.split("\\.");
		StringBuffer sb = new StringBuffer();
		for (String str : array) {
			sb.append(File.separator).append(str);
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		List<Class<?>> list = new ArrayList<Class<?>>();
		scan("net", list);
		System.out.println(list.size());
		for (Class<?> cla : list) {
			System.out.println(cla.getName());
		}
	}
}
