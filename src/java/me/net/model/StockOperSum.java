package me.net.model;

import java.math.BigDecimal;

import me.common.util.Util;

public class StockOperSum {

	private String code;
	private String name;
	private int buys;
	private int sells;
	private int times;
	private int winTimes;
	private int loseTimes;
	private BigDecimal lastRemain;
	private BigDecimal minRemain;
	private String flag;

	public StockOperSum(int buys, int sells, int times, int winTimes, int loseTimes, BigDecimal lastRemain,
			BigDecimal minRemain, String flag) {
		this.buys = buys;
		this.sells = sells;
		this.times = times;
		this.winTimes = winTimes;
		this.loseTimes = loseTimes;
		this.lastRemain = lastRemain;
		this.minRemain = minRemain;
		this.flag = flag;
	}

	@Override
	public String toString() {
		return Util.printFields(this.getClass(), this);
	}

	public Object[] toObjectArray() {
		return new Object[] { this.code, this.name, this.buys, this.sells, this.times, this.winTimes, this.loseTimes,
				this.lastRemain, this.minRemain, this.flag };
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getBuys() {
		return buys;
	}

	public void setBuys(int buys) {
		this.buys = buys;
	}

	public int getSells() {
		return sells;
	}

	public void setSells(int sells) {
		this.sells = sells;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public int getWinTimes() {
		return winTimes;
	}

	public void setWinTimes(int winTimes) {
		this.winTimes = winTimes;
	}

	public int getLoseTimes() {
		return loseTimes;
	}

	public void setLoseTimes(int loseTimes) {
		this.loseTimes = loseTimes;
	}

	public BigDecimal getLastRemain() {
		return lastRemain;
	}

	public void setLastRemain(BigDecimal lastRemain) {
		this.lastRemain = lastRemain;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public BigDecimal getMinRemain() {
		return minRemain;
	}

	public void setMinRemain(BigDecimal minRemain) {
		this.minRemain = minRemain;
	}
}
