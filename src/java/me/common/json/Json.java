package me.common.json;

/**
 * 代表 json 对象。可以是 JObject，也可以是 JArray（json可以直接是一个数组）
 * 
 * json可以嵌套，即一个json对象可以是另一个json的一个属性的值而已。因为 json继承 value
 * @author opq
 *
 */
public interface Json extends Value {
	public <T> Object toObject(Class<T> clazz);
}
