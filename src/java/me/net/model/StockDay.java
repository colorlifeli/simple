package me.net.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;

public class StockDay implements Serializable {

	private static final long serialVersionUID = 1L;

	public String code;
	public Date date_;
	public String open_;
	public String high;
	public String low;
	public String close_;
	public String volume;
	public String source;
	public String flag;
	public int sn;

	public void setFlag(String flag) {
		this.flag = flag;
	}

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
		return new Object[] { this.code, this.date_, this.open_, this.high, this.low, this.close_, this.volume,
				this.source };
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setDate_(Date date_) {
		this.date_ = date_;
	}

	public void setOpen_(String open_) {
		this.open_ = open_;
	}

	public void setHigh(String high) {
		this.high = high;
	}

	public void setLow(String low) {
		this.low = low;
	}

	public void setClose_(String close_) {
		this.close_ = close_;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
