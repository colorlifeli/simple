package net;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import common.annotation.IocAnno.Ioc;

public class StockSourceImpl1 implements StockSource {

	private String realTimeUrl = "http://hq.sinajs.cn/list=";
	private String historyUrl = "";
	@Ioc
	private StockService stockService;

	@Override
	public void getRealTime(List<String> codes) {
		// TODO Auto-generated method stub
		String url = realTimeUrl;
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
					String[] datas = stock.split(",");
					// 根据对照自己对应数据
					System.out.println(stock);
				}
				bo.reset();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	@Override
	public void getRealTime(List<String> codes, int interval) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getRealTimeAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getRealTimeAll(int interval) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getHistory(List<String> codes, String startDate, String endDate) {
		// TODO Auto-generated method stub

	}

	public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

}
