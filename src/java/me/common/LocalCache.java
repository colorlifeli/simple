package me.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import me.common.internal.Cache;
import me.common.internal.CacheContext;

@SuppressWarnings("deprecation")
public class LocalCache extends Cache {

	// 定义缓存类型
	public static final String CACHE_KEY = "local";
	// 设置初始化容量
	// private static final int CAPACITY = 512;
	// 设置最大缓存个数
	// private static final int WEIGHT = 10000;
	// 存放byte[]类型主要是为了防止缓存数据遭到污染
	private static Map<String, byte[]> map = new ConcurrentHashMap<>();
	private static LocalCache localCache = new LocalCache();

	private LocalCache() {
	}

	@Override
	public void init() {
		CacheContext.register(CACHE_KEY, localCache);

	}

	@Override
	public void put(String key, Object object) {

	}

	@Override
	public Object get(String key) {
		byte[] cacheValue = map.get(key);
		return disassembleValue(key, cacheValue);

	}

	@Override
	public void remove(String key) {
		map.remove(key);

	}

	@Override
	public int getTotalNum() {
		return map.keySet().size();

	}

	@Override
	public List<String> getMatchKey(String key) {
		List<String> list = new ArrayList<String>();
		Set<String> set = map.keySet();
		for (String s : set) {
			if (s.contains(key)) {
				list.add(s);
			}
		}
		return list;

	}

	@Override
	public void put(String key, Object object, long expire) {

		byte[] cacheValue = assembleValue(object, expire);
		map.put(key, cacheValue);
	}

	@Override
	public void clear() {
		map.clear();

	}

}
