package me.common.json;

import java.util.ArrayList;
import java.util.List;

public class JArray implements Json, Value {
	private List<Value> list = new ArrayList<>();  //modify 20200208

	public JArray(List<Value> list) {
		this.list = list;
	}

	public int length() {
		return list.size();
	}

	public void add(Value element) {
		list.add(element);
	}

	public Value get(int i) {
		return list.get(i);
	}

	public String toString() {
		return toString(1);
		
//		StringBuilder sb = new StringBuilder();
//		sb.append('\n').append("[ ").append('\n');
//		for (int i = 0; i < list.size(); i++) {
//			sb.append(list.get(i).toString());
//			if (i != list.size() - 1) {
//				sb.append(", ");
//			}
//		}
//		sb.append('\n').append(" ]").append('\n');
//		return sb.toString();
	}
	
	public String toString(int level) {
		StringBuilder sb = new StringBuilder();
		sb.append('\n');
		printNTab(sb, level-1);
		sb.append("[ ").append('\n');
		for (int i = 0; i < list.size(); i++) {
			if(list.get(i).getClass().getSimpleName().equals("Primary"))
				printNTab(sb, level);
			sb.append(list.get(i).toString(level));
			if (i != list.size() - 1) {
				sb.append(", ").append('\n');
			}
		}
		sb.append('\n');
		printNTab(sb, level-1);
		sb.append(" ]");
		return sb.toString();
	}
	
	private void printNTab(StringBuilder sb, int n) {
		for(int i=0; i<n; i++)
			sb.append('\t');
	}

	public Object value() {
		return this;
	}
	
	public <T> Object toObject(Class<T> clazz) {
		return value(clazz);
	}
	
	public <T> Object value(Class<T> clazz) {
		List<T> results = new ArrayList<T>();
		if (list.size() == 0)
			return results;

		for (int i = 0; i < list.size(); i++) {
			Value item = list.get(i);
			results.add((T)item.value(clazz));
		}
		
		return results;
	}
}