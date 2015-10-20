package net;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.model.RealTime;

public class SinaSourceService {

	private String realTimeUrl = "http://hq.sinajs.cn/list=";

	public List<RealTime> getRealTime(List<String> codes) {
		String url = realTimeUrl;
		for (String i_code : codes) {
			url = url + i_code + ",";
		}
		url.subSequence(0, url.length() - 1);
		List<RealTime> dataList = new ArrayList<RealTime>();
		BufferedReader br;
		String stock = null;

		try {
			URL u = new URL(url);
			br = new BufferedReader(new InputStreamReader(u.openStream(), "GBK"));
			try {
				while ((stock = br.readLine()) != null) {

					if (stock.length() < 10)
						continue;
					// "var hq_str_sz000830="鲁西化工,6.86,6.86,6.78,6.90,6.74,6.78,6.79,33275590,227193182.00,69000,6.78,19400,6.77,58900,6.76,122800,6.75,123900,6.74,112161,6.79,64000,6.80,49000,6.81,146600,6.82,142800,6.83,2015-10-19,13:41:22,00""
					String[] str = stock.split("=");
					String code = str[0].substring(13);
					str[1] = str[1].substring(1).substring(0, str[1].length() - 2);
					String[] datas = str[1].split(",");
					datas[0] = code; // 将名称换成code
					// 根据对照自己对应数据
					// System.out.println(stock);
					dataList.add(toRealtime(datas));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				br.close();
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		return dataList;
	}

	/**
	 * 将数据转为RealTime对象
	 * @param datas
	 * @return
	 */
	private RealTime toRealtime(String[] datas) {
		RealTime realtime = new RealTime();
		realtime.code = datas[0];
		realtime.tOpen = datas[1];
		realtime.yClose = datas[2];
		realtime.now = datas[3];
		realtime.high = datas[4];
		realtime.low = datas[5];
		realtime.deals = datas[8];
		realtime.dealsum = datas[9];
		realtime.time = datas[31];
		realtime.source = "sina";

		return realtime;
	}

	/**
	 * 字符流读取
	 * @param codes
	 * @return
	 */
	@Deprecated
	public List<Object[]> getRealTime1(List<String> codes) {
		String url = realTimeUrl;
		for (String i_code : codes) {
			url = url + i_code + ",";
		}
		url.subSequence(0, url.length() - 1);
		List<Object[]> paramList = new ArrayList<Object[]>();

		try {
			URL u = new URL(url);
			byte[] b = new byte[256];
			InputStream in = null;
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			try {
				in = u.openStream();
				int i;
				while ((i = in.read(b)) != -1) {
					bo.write(b, 0, i);
				}
				String result = bo.toString("GBK");
				String[] stocks = result.split(";");
				for (String stock : stocks) {
					if (stock.length() < 10)
						continue;
					// "var hq_str_sz000830="鲁西化工,6.86,6.86,6.78,6.90,6.74,6.78,6.79,33275590,227193182.00,69000,6.78,19400,6.77,58900,6.76,122800,6.75,123900,6.74,112161,6.79,64000,6.80,49000,6.81,146600,6.82,142800,6.83,2015-10-19,13:41:22,00""
					String[] str = stock.split("=");
					String code = str[0].substring(13);
					str[1] = str[1].substring(1).substring(0, str[1].length() - 2);
					String[] datas = str[1].split(",");
					datas[0] = code; // 将名称换成code
					// 根据对照自己对应数据
					// System.out.println(stock);
					// addParam(paramList, datas);
				}
				bo.reset();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		return paramList;
	}
}
