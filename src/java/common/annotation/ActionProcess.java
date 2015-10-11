package common.annotation;

import java.util.ArrayList;
import java.util.List;

import common.internal.ScanPackage;

public class ActionProcess {
	
	//TODO scanPath可配置
	/**
	 * 根据注解取得 action 的信息，并放入 map
	 * @param scanPath
	 * @throws Exception
	 */
	public static void getActionInfo(String scanPath) throws Exception {
		List<Class<?>> list = new ArrayList<Class<?>>();
		ScanPackage.scan("com.Models", list);
		
		for (Class<?> cla : list) {
			System.out.println(cla.getName());
		}
	}
}
