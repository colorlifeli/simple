package me.common.json;

public interface Value {

	public String toString(int i);
	public <T> Object value(Class<T> clazz);
}
