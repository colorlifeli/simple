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
	public String time_;
	public String source;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		Class<?> clazz = this.getClass();
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

	/**
	 * 得到一个按顺序的对象数组
	 * @return
	 */
	public Object[] toObjectArray() {
		return new Object[] { this.code, this.tOpen, this.yClose, this.now, this.high, this.low, this.deals,
				this.dealsum, this.time_, this.source };
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setyClose(String yClose) {
		this.yClose = yClose;
	}

	public void settOpen(String tOpen) {
		this.tOpen = tOpen;
	}

	public void setNow(String now) {
		this.now = now;
	}

	public void setHigh(String high) {
		this.high = high;
	}

	public void setLow(String low) {
		this.low = low;
	}

	public void setDeals(String deals) {
		this.deals = deals;
	}

	public void setDealsum(String dealsum) {
		this.dealsum = dealsum;
	}

	public void setTime_(String time_) {
		this.time_ = time_;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
