package net;

import java.util.List;

import common.annotation.IocAnno.Ioc;

public class StockSourceImpl1 implements StockSource {

	@Ioc(name = "sinaSourceService")
	private SinaSourceService sina;
	@Ioc
	private StockService stockService;

	@Override
	public void getRealTime(List<String> codes) {
		List<Object[]> list = sina.getRealTime(codes);
		stockService.saveRealTimeData(list);
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

	public SinaSourceService getSina() {
		return sina;
	}

	public void setSina(SinaSourceService sina) {
		this.sina = sina;
	}

}
