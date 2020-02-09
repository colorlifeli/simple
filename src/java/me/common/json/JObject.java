package me.common.json;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JObject implements Json {

	private Map<String, Value> map = new HashMap<>();

	public JObject(Map<String, Value> map) {
		this.map = map;
	}

	public String toString() {
		return toString(1);
//		StringBuilder sb = new StringBuilder();
//		sb.append("{ ").append('\n');
//		int size = map.size();
//		for (String key : map.keySet()) {
//			sb.append('\t');
//			sb.append(key + " : " + map.get(key).toString());
//			if (--size != 0) {
//				sb.append(", ").append('\n');
//			}
//		}
//		sb.append(" }");
//		return sb.toString();
	}
	
	public String toString(int i) {
		StringBuilder sb = new StringBuilder();

		printNTab(sb, i-1);
		sb.append("{ ").append('\n');
		int size = map.size();
		for (String key : map.keySet()) {
			printNTab(sb, i);
			sb.append("\"").append(key).append("\"").append(" : " + map.get(key).toString(i + 1));
			if (--size != 0) {
				sb.append(", ").append('\n');
			}
		}
		sb.append('\n');
		printNTab(sb, i-1);
		sb.append(" }");
		return sb.toString();
	}
	
	private void printNTab(StringBuilder sb, int n) {
		for(int i=0; i<n; i++)
			sb.append('\t');
	}
	
	public <T> Object toObject(Class<T> clazz) {
		return value(clazz);
	}

	public <T> Object value(Class<T> clazz) {
		T obj = null;
		try {
			obj = clazz.newInstance();
			for(String name : map.keySet()) {
				Field field = obj.getClass().getField(name);
				field.setAccessible(true);
				field.set(obj, map.get(name).value(field.getType()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return obj;
	}

}
