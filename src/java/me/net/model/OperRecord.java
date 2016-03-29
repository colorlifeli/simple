package me.net.model;

import java.math.BigDecimal;

import me.common.util.Util;
import me.net.NetType.eStockOper;

public class OperRecord {
	private String code;
	private eStockOper oper; //操作
	private int num; //数量
	private BigDecimal price; //单价
	private BigDecimal sum; //总价
	private int total; //当前拥有数量
	private BigDecimal remain; //余额，为了方便知道当前余额
	private String flag;

	public OperRecord() {
	};

	public OperRecord(eStockOper oper, int num, BigDecimal price, BigDecimal sum, int total, BigDecimal remain) {
		this.oper = oper;
		this.num = num;
		this.total = total;
		this.price = price;
		this.sum = sum;
		this.remain = remain;
	}

	@Override
	public String toString() {
		return Util.printFields(this.getClass(), this);
	}

	public eStockOper getOper() {
		return oper;
	}

	public void setOper(eStockOper oper) {
		this.oper = oper;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getSum() {
		return sum;
	}

	public void setSum(BigDecimal sum) {
		this.sum = sum;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public BigDecimal getRemain() {
		return remain;
	}

	public void setRemain(BigDecimal remain) {
		this.remain = remain;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

}
