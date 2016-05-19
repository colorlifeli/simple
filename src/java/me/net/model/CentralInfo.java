package me.net.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 中枢相关信息。
 * 
 * 这里的中枢是指连续3笔的公共区域。数字上即连续的 4点 top/bottom的交叉范围：{max(bottom),min(top)}，如果没有交叉（max(bottom)>min(top)）时，则不构成中枢，继续取后面的点。
 * 
 * @author James
 *
 */
public class CentralInfo {

	public List<Central> centrals;
	public List<String> points;

	//public List<String> pointsHis;

	public CentralInfo() {
		centrals = new ArrayList<Central>();
		points = new ArrayList<String>();
		//pointsHis = new ArrayList<String>();
	}
}
