package me.net.model;

public class Central {
	public String low;
	public String high;
	public int position;
	
	public String startDate;
	public String endDate;
	
	//add 20200725 主要用于centralinfo2的，记录公共区域
	public String share_high;
	public String share_low;
	
	//add 20201030 用于计算振幅
	public int startSn;
	public int endSn;
	public double degree = 0.0;
	
	public int id;//顺序标记中枢
	public String code;
}