package me.common.json;

import java.math.BigDecimal;

import me.common.SimpleException;

public class Primary implements Value {
    private String value;

    public Primary(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public <T> Object value(Class<T> clazz) {
    	if (clazz.getSimpleName().equals("String"))
			return (T)value;
		else {
			//number
			if (!clazz.getSimpleName().equals("BigDecimal")){
				throw new SimpleException("数字类型必须是 BigDecimal");
			}
			BigDecimal d = new BigDecimal(value);
			return (T)d;
		}
    }
    
    public String toString(int level) {
    	return "\"" + value + "\"";
    }
    
}