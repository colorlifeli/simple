package net.model;

import java.lang.reflect.Field;

public class RealTime {

	public String code;
	public String yClose;
	public String tOpen;
	public String now;
	public String high;
	public String low;
	public String deals;
	public String dealsum;
	public String time;
	public String source;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		Class clazz = this.getClass();
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			try {
				sb.append(field.getName()).append(":").append(field.get(this)).append(",");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}