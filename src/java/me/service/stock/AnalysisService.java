package me.service.stock;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.Constant;
import me.net.NetType.eStockOper;
import me.net.NetType.eStrategy;
import me.net.dao.StockAnalysisDao;
import me.net.dayHandler.Simulator;
import me.net.model.OperRecord;
import me.net.model.StockDay;
import me.net.model.StockOperSum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Ioc
	private StockAnalysisDao stockAnalysisDao;
	@Ioc
	Simulator simulator;

	/**   配置参数   *****/
	private eStrategy strategy = eStrategy.One; //策略
	private double abnormal = 200; //绝对值超过这个值视为异常值

	private final int one = 1;

	/**
	 * 生成此 code 的所有操作数据
	 * @param hcode
	 * @throws SQLException
	 */
	public void compute(String hcode) throws SQLException {
		List<StockDay> all = null;
		List<StockDay> his = new ArrayList<StockDay>();// 已进行分析过的历史数据
		List<OperRecord> operList = new ArrayList<OperRecord>();

		all = stockAnalysisDao.getDay(hcode, Constant.simulate.startDate, null);

		if (all.size() < 50) {
			logger.error("数据太少，只有：" + all.size());
			return;
		}

		BigDecimal price = BigDecimal.ZERO;
		BigDecimal sum = BigDecimal.ZERO;
		int num = 0;
		int total = 0;
		BigDecimal remain = BigDecimal.ZERO;
		int symbol = 1; //表示正负
		int sn = 0;

		for (int i = 0; i < all.size() - 1; i++) {
			StockDay someDay = all.get(i);
			StockDay nextDay = all.get(i + 1);

			eStockOper result = simulator.handle(someDay, his);

			if (result == eStockOper.None)
				continue;

			if (operList.size() == 0 && result != eStockOper.Buy) {
				//第一次必须是买
				continue;
			}

			sn++;

			//如果能在第二天以中间价处理，结果会理想很多
			if (result == eStockOper.Buy) {
				price = new BigDecimal(nextDay.high);
				//price = (Double.parseDouble(nextDay.high) + Double.parseDouble(nextDay.low)) / 2;
				symbol = 1;
			} else if (result == eStockOper.Sell) {
				price = new BigDecimal(nextDay.low);
				//price = (Double.parseDouble(nextDay.high) + Double.parseDouble(nextDay.low)) / 2;
				symbol = -1;
			}

			switch (strategy) {
			case One:
				//每次操作一单位
				num = one;
				break;
			case OneBuyOneSell:
				//严格执行一买一卖，不然放弃
				if (operList.size() != 0 && result == operList.get(operList.size() - 1).getOper())
					continue;
				num = one;
			case Double:
				//第二次出现相同操作（如连续第二次买），则执行2倍的量
				break;
			}

			total += symbol * num;
			sum = price.multiply(new BigDecimal(symbol * num));//symbol * price * num;

			if (total < 0) {
				total = 0;
				continue;
			}
			remain = remain.subtract(sum);//remain += -sum; //买是付钱，用负表示
			operList.add(new OperRecord(sn, hcode, result, one, price, sum, total, remain));

		}

		// 保存至数据库
		stockAnalysisDao.saveOperList(operList);

		/*******************   下面是对操作数据的分析与汇总   *********************/

		BigDecimal lastRemain = BigDecimal.ZERO;
		String lastFlag = "00";

		//最后一次卖光时的情况
		for (int i = operList.size() - 1; i >= 0; i--) {
			OperRecord record = operList.get(i);
			if (record.getTotal() == 0) {
				lastRemain = record.getRemain();

				//去除结果过好过坏数据
				if (Math.abs(record.getRemain().doubleValue()) > abnormal) {
					lastFlag = "01";
				}

				break;
			}
		}

		BigDecimal minRemain = BigDecimal.ZERO; //余额最小时，即最大的投资额度
		int buys = 0, sells = 0, times = 0, winTimes = 0, loseTimes = 0;
		for (int i = operList.size() - 1; i >= 0; i--) {
			OperRecord record = operList.get(i);
			if (record.getRemain().compareTo(minRemain) == -1) //record.remain < minRemain
				minRemain = record.getRemain();
			if (record.getTotal() == 0) {
				times++;
				if (record.getRemain().doubleValue() > 0)
					winTimes++;
				else
					loseTimes++;
			}
			if (record.getOper() == eStockOper.Buy)
				buys++;
			if (record.getOper() == eStockOper.Sell)
				sells++;
		}

		StockOperSum operSum = new StockOperSum(buys, sells, times, winTimes, loseTimes, lastRemain, minRemain,
				lastFlag);
		operSum.setCode(hcode);
		operSum.setName(stockAnalysisDao.getName(hcode.substring(0, hcode.length() - 3)));
		stockAnalysisDao.saveOperSum(operSum);

		logger.info(operSum.toString());
	}

	/**
	 * 对所有 code 的汇总数据再次汇总
	 * @return
	 * @throws SQLException
	 */
	public String summary() throws SQLException {

		int win = 0;
		int lose = 0;
		BigDecimal allRecordsSum = BigDecimal.ZERO;
		BigDecimal investment = BigDecimal.ZERO;

		List<StockOperSum> operSumList = stockAnalysisDao.getAllCodeSum(false);

		for (StockOperSum record : operSumList) {
			if (record.getLastRemain().doubleValue() > 0)
				win++;
			else
				lose++;

			allRecordsSum = allRecordsSum.add(record.getLastRemain()); //allRecordsSum += record.remain;
			investment = investment.subtract(record.getMinRemain()); //investment -= minRemain;

		}

		return String.format("total:%s, win:%s, lose:%s, remain:%s, investment:%s", operSumList.size(), win, lose,
				allRecordsSum, investment);
	}

}
