package me.common.internal;

import java.util.HashMap;
import java.util.Map;


/**
 * 这个 cache 的 annotation 主要用于缓存类的处理方法，当请求过来时，从缓存中取得处理方法，从而不用 new
 * 还需要实现 aop 才能自动拦截请求并处理
 * 
 * 暂不使用
 * @author lijiancan
 *
 */
@Deprecated
public class CacheContext {
	private static Map<String, Cache> cacheMap = new HashMap<String, Cache>();

	/**
	 * 刷新本地线程，如果值为true,则不从缓存中取数据，数据取得之后会将新的数据写入缓存 初始值false
	 */
	public static ThreadLocal<Boolean> refreshFlag = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};

	/**
	 * 只读本地线程，如果值为true,则新的值不会写进缓存 初始值false
	 */
	public static ThreadLocal<Boolean> onlyReadCache = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};

	/**
	 * 获取具体的缓存
	 * 
	 * @param key
	 * @return
	 */
	public static Cache getCache(String key) {
		return cacheMap.get(key);
	}

	/**
	 * 注册新的缓存
	 * 
	 * @param key
	 * @param cache
	 */
	public static void register(String key, Cache cache) {
		cacheMap.put(key, cache);
	}

}
