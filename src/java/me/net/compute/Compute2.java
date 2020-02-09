package me.net.compute;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.common.annotation.IocAnno.Ioc;
import me.common.util.TypeUtil;
import me.net.NetType.eStockOper;
import me.net.dayHandler.Simulator;
import me.net.model.OperRecord;
import me.net.model.StockDay;

/**
 * 与 compute1 比较：
 * 1.赚10%即卖。只要当日最高价大于10%，那一般就能以10%的价格卖
 * @author James
 *
 */
public class Compute2 extends Compute {

	@Ioc
	private Simulator simulator;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void codeStart() {
		simulator.reset();
	}

	@Override
	public void codeEnd(List<OperRecord> operList) {
		
	}

	/**
	 * 买卖操作
	 */
	@Override
	public void buyOrSell(String hcode, StockDay someDay, 
			StockDay nextDay, List<OperRecord> operList) {

		Date date = null;
		BigDecimal price = BigDecimal.ZERO;
		BigDecimal sum = BigDecimal.ZERO;
		int num = 0;
		int total = 0;
		BigDecimal remain = BigDecimal.ZERO;//remain 是账户剩余钱，相反数就是付出了多少钱
		int symbol = 1; //表示正负
		int sn = 0;
		
		OperRecord last = null;
		if(operList.size() > 0)
			last = operList.get(operList.size() - 1);

		eStockOper result = simulator.handle2(someDay);

		if (nextDay != null && result == eStockOper.Buy) {
			symbol = 1;
			
			//第二天最差价格买
			price = new BigDecimal(nextDay.high);
			date = nextDay.date_;

			//每次买1个单位
//			num = one;
			// 以固定总价来买, 取一个整的数量，然后以这个数量来买。卖还是以量来卖 
			num = oneAmount/price.toBigInteger().intValue();

			if(last != null) {
				total = last.getTotal() + symbol * num;
			} else
				total = symbol * num;
			sum = price.multiply(new BigDecimal(symbol * num));//symbol * price * num;

			if (total < 0) {
				total = 0;
				return;
			}
			
			if(last != null) {
				total = last.getTotal() + symbol * num;
				remain = last.getRemain();
				sn = last.getSn()+1;
			}
			remain = remain.subtract(sum);//remain += -sum; //买是付钱，用负表示
			operList.add(new OperRecord(sn, hcode, result.toString(), num, price, sum, total, remain, date));

		} else if (result == eStockOper.None || result == eStockOper.Sell) { //最后一天是特殊的，必须全卖

			//卖时机是通过资金管理手段来控制
			if (last == null) //还没有买
				return;
			if (last.getTotal() == 0)
				return;
			if (last.getDate_() != null && last.getDate_().equals(someDay.date_)) //昨天决定今天买，今天不能卖
				return;


			// *********** 最简单的资金管理，赚10%即卖
			price = new BigDecimal(someDay.low); //取今天最低价格
			//BigDecimal cost = last.getRemain().negate(); //成本，即当前价值，也即投入了多少
			BigDecimal cost = computeCost(operList);
			int lastTotal = last.getTotal();
			//当前价格*数量=总价
			BigDecimal gain_high = new BigDecimal(someDay.high).multiply(new BigDecimal(lastTotal));
			BigDecimal gain_low = new BigDecimal(someDay.low).multiply(new BigDecimal(lastTotal));
			date = someDay.date_;
			sn = last.getSn()+1;
			//BigDecimal sum_l = BigDecimal.ZERO;

			// *********** 最简单的资金管理，赚10%即卖
			if(nextDay == null) { //最后一天全卖
				num = lastTotal;
				sn = 999;
			}else if(result == eStockOper.Sell) {
				num = lastTotal;
				logger.debug("oper is sell : {},{},{}", hcode, date, num);
			} else if (gain_high.doubleValue() > cost.doubleValue() * 1.2) {
				num = lastTotal;
				sum = new BigDecimal(cost.doubleValue() * 1.2);
			} else if (gain_high.doubleValue() > cost.doubleValue() * 0.9 &&
					gain_low.doubleValue() < cost.doubleValue() * 0.9 && 
					gain_low.doubleValue() >= cost.doubleValue() * 0.8) {
				//logger.debug("0.9 do nothing : {},{},{}", hcode, date, num_l);
				num = lastTotal;
				sum = new BigDecimal(cost.doubleValue() * 0.9);
				//num_l = (int) Math.ceil((double)total_l / 2); //进位取整
			} else if (gain_low.doubleValue() < cost.doubleValue() * 0.8) {
				//num_l = (int) Math.ceil((double)total_l / 2); 
				//num = lastTotal;
				logger.debug("0.8 sell part : {},{},{}", hcode, date, num);
			} else if (gain_low.doubleValue() < cost.doubleValue() * 0.5) {
				//num_l = total_l;
				logger.debug("0.5 sell all : {},{},{}", hcode, date, num);
			} else {
				//logger.error("***************  unexpect *************" + cost.doubleValue()/gain_low.doubleValue());
			}

			if (num > 0) {
				if(sum.doubleValue() == 0)
					sum = price.multiply(new BigDecimal(num));
				remain = last.getRemain().add(sum);//remain += -sum; //买是付钱，用负表示
				//cost = cost.multiply(new BigDecimal((total_l - num_l) / total_l));
				//输出前后2天，共5天的k线
				operList.add(new OperRecord(sn, hcode, eStockOper.Sell.toString(), num, price, sum,
						lastTotal - num, remain, date));
//				logger.info(String.format("%s:(%s, %s),%s:(%s, %s),%s:(%s, %s),%s:(%s, %s)",
//				all.get(i - 2).date_, all.get(i - 2).low, all.get(i - 2).high, all.get(i - 1).date_,
//				all.get(i - 1).low, all.get(i - 1).high, all.get(i).date_, all.get(i).low, all.get(i).high,
//				i==all.size()-1?"":all.get(i + 1).date_, i==all.size()-1?"":all.get(i + 1).low, i==all.size()-1?"":all.get(i + 1).high));
				if(printOperLog)
					logger.info(operList.get(operList.size() - 1).toString());
			}
			return;
		}
		
		
	}

	@Override
	public List<EveryDayGain> computeGain(List<OperRecord> operList, List<StockDay> all) {
		return super.computeGain(operList, all);
	}
	
	/**
	 * 计算成本：
	 * 		卖之前的所有连续买的总和。
	 * 成本要忽略上一次卖的盈利或亏损，也就是卖了之后再次买就重新计算成本。否则会影响第二次卖的时机
	 * 假设第一次卖亏了20%，再次买时，因为叠加了上一次的亏损，第二天基本上必然要卖
	 * @param operList
	 * @return
	 */
	private BigDecimal computeCost(List<OperRecord> operList) {
		BigDecimal cost = BigDecimal.ZERO;
		for(int i=operList.size()-1; i>=0; i--) {
			OperRecord record = operList.get(i);
			if(record.getOper().equals(eStockOper.Sell.toString()))
				break;
			cost = cost.add(record.getSum());
		}
		
		return cost;
	}

	public void output(List<EveryDayGain> gainList) {
		try {
			Calendar c = Calendar.getInstance();
			Date start = format.parse(startDate);
//			Date end = all.get(all.size() - 1).date_;
//			c.setTime(end);
//			c.add(Calendar.DAY_OF_YEAR, 2);
//			end = c.getTime();
			Date end = new Date();
			if(!TypeUtil.isEmpty(endDate))
				end = format.parse(endDate);

			c.setTime(start);
			BigDecimal balance = BigDecimal.ZERO;
			while (start.before(end)) {
				for (int i = 0; i < gainList.size(); i++) {
					EveryDayGain gain = gainList.get(i);
					if (start.before(gain.date_)) { //start < gain.date
						break;
					} 
					balance = gain.balance;
				}

				logger.info(format.format(start) + " " + balance);
				c.add(Calendar.DAY_OF_YEAR, 1);
				start = c.getTime();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
