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
import me.net.dayHandler.Simulator3;
import me.net.model.OperRecord;
import me.net.model.StockDay;

/**
 * 与 compute1 比较：
 * 1.赚10%即卖。只要当日最高价大于10%，那一般就能以10%的价格卖
 * @author James
 * 
 * ***** 使用 Simulator3
 *
 */
public class Compute3 extends Compute {

	@Ioc
	private Simulator3 simulator3;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void codeStart() {
		simulator3.reset();
	}

	@Override
	public void codeEnd(List<OperRecord> operList) {
		simulator3.info.printCentrals();
	}

	/**
	 * 买卖操作
	 */
	@Override
	public void buyOrSell(String hcode, StockDay someDay, 
			StockDay nextDay, List<OperRecord> operList) {

		eStockOper result = simulator3.handle(someDay);

		if (nextDay != null && result == eStockOper.Buy) {
			this.buy2(hcode, nextDay, operList);
		} else //if (result == eStockOper.None || result == eStockOper.Sell) { //最后一天是特殊的，必须全卖
		{
			this.sell2(hcode, someDay, nextDay, operList, result);
		}
		
	}
	
	//执行一买一卖
	private void buy2(String hcode, StockDay nextDay, List<OperRecord> operList) {
		int total = 0;
		BigDecimal remain = BigDecimal.ZERO;//remain 是账户剩余钱，相反数就是付出了多少钱
		int sn = 0;
		
		//第二天最差价格买
		BigDecimal price = new BigDecimal(nextDay.high);
		Date date = nextDay.date_;
		
		if(this.check(operList, price, date) == false)
			return;

		// 以固定总价来买, 取一个整的数量，然后以这个数量来买。卖还是以量来卖 
		int num = oneAmount/price.toBigInteger().intValue();

		if(operList.size() > 0) {
			OperRecord last = operList.get(operList.size() - 1);
			total = last.getTotal() + num;
			remain = last.getRemain();
			sn = last.getSn()+1;
		} else
			total = num;
		BigDecimal sum = price.multiply(new BigDecimal(num));//symbol * price * num;
		
		remain = remain.subtract(sum);//remain += -sum; //买是付钱，用负表示
		operList.add(new OperRecord(sn, hcode, eStockOper.Buy.toString(), num, price, sum, total, remain, date));
		logger.info(operList.get(operList.size() - 1).toString());
	}
	
	private boolean check(List<OperRecord> operList, BigDecimal price, Date buyDate) {
		//上一次是买，且比上次买高，则不买了
		//连续买3次后，不再买了
		int size = operList.size();
		if(size > 0 && operList.get(size-1).getOper().equals(eStockOper.Buy.toString())
				&& price.doubleValue() > operList.get(size-1).getPrice().doubleValue()) {
			
			return false;
		} 
		int buytimes = 0;
		for(int i=1; i<= size; i++ ){
			if(!operList.get(size-i).getOper().equals(eStockOper.Buy.toString()))
				break;
			buytimes++;
		}
		if(buytimes > 2) return false;
		//return true;
		
		//总的连续买次数也要限制，这样就限制了投资额度
		int all_buytimes = 0;
//		for (String code : g_operListMap.keySet()) {
//			List<OperRecord> l_operList = g_operListMap.get(code);
//			if(l_operList == null || l_operList.size() == 0)
//				continue;
//			size = l_operList.size();
//			for(int i=1; i<=size; i++) {
//				if(!l_operList.get(size-i).getOper().equals(eStockOper.Buy.toString()))
//					break;
//				all_buytimes++;
//			}
//		}
		//问题：后面的code，有可能在较早的时候买，这样就限制不了了。
		for (String code : g_operListMap.keySet()) {
			List<OperRecord> l_operList = g_operListMap.get(code);
			if(l_operList == null || l_operList.size() == 0)
				continue;
			size = l_operList.size();
			for(int i=size-1; i>=0; i--) {
				if(l_operList.get(i).getDate_().compareTo(buyDate) <= 0) {
					if(!l_operList.get(i).getOper().equals(eStockOper.Buy.toString()))
						break;
					all_buytimes++;
				}
			}
			
		}
		if(all_buytimes > 50) 
			return false;
		
		return true;
	}
	
	//简单地买一个单位
	private void buy(String hcode, OperRecord last, StockDay nextDay, List<OperRecord> operList) {

		int total = 0;
		BigDecimal remain = BigDecimal.ZERO;//remain 是账户剩余钱，相反数就是付出了多少钱
		int sn = 0;
		
		//第二天最差价格买
		BigDecimal price = new BigDecimal(nextDay.high);
		Date date = nextDay.date_;

		// 以固定总价来买, 取一个整的数量，然后以这个数量来买。卖还是以量来卖 
		int num = oneAmount/price.toBigInteger().intValue();

		if(last != null) {
			total = last.getTotal() + num;
			remain = last.getRemain();
			sn = last.getSn()+1;
		} else
			total = num;
		BigDecimal sum = price.multiply(new BigDecimal(num));//symbol * price * num;
		
		remain = remain.subtract(sum);//remain += -sum; //买是付钱，用负表示
		operList.add(new OperRecord(sn, hcode, eStockOper.Buy.toString(), num, price, sum, total, remain, date));
		logger.info(operList.get(operList.size() - 1).toString());
	}
	
	private void sell2(String hcode, StockDay someDay, StockDay nextDay, 
			List<OperRecord> operList, eStockOper result) {
		BigDecimal remain = BigDecimal.ZERO;//remain 是账户剩余钱，相反数就是付出了多少钱
		int sn = 0;
		BigDecimal sum = BigDecimal.ZERO;
		int num = 0;
		
		if(operList.size() == 0) return;
		OperRecord last = operList.get(operList.size() - 1);
		
		if (last.getDate_() != null && last.getDate_().equals(someDay.date_)) { //昨天决定今天买，今天不能卖
			//****** 昨天决定今天买，且今天是最后一天，则取消昨天的买
			if(nextDay == null) {
				operList.remove(operList.size() - 1);
				//删除之后就没有其它买，则最后也不用卖了
				if (operList.size() == 0)
					return;
				else
					last = operList.get(operList.size() - 1);
			}
			else 
				return;
		}
		
		if (last.getTotal() == 0)
			return;

		BigDecimal price = new BigDecimal(someDay.low); //取今天最低价格
		int lastTotal = last.getTotal();
		//当前价格*数量=总价
		BigDecimal gain_high = new BigDecimal(someDay.high).multiply(new BigDecimal(lastTotal));
		Date date = someDay.date_;
		sn = last.getSn()+1;
		BigDecimal cost = computeCost(operList);
		
		if(nextDay == null) { //最后一天全卖
			num = lastTotal;
			sn = 999;
		} else if(result == eStockOper.Sell) {
			num = lastTotal;
//			logger.debug("oper is sell : {},{},{}", hcode, date, num);
//		} else if (gain_high.doubleValue() > cost.doubleValue() * 1.2) {
//			num = lastTotal;
//			sum = new BigDecimal(cost.doubleValue() * 1.2);
		} else if (gain_high.doubleValue() > cost.doubleValue() * 1.2) {
			num = lastTotal;
			sum = new BigDecimal(cost.doubleValue() * 1.2);
		} else {
			//logger.error("***************  unexpect *************" + cost.doubleValue()/gain_low.doubleValue());
		}
		
		if (num > 0) {
			if(sum.doubleValue() == 0)
				sum = price.multiply(new BigDecimal(num));
			remain = last.getRemain().add(sum);

			operList.add(new OperRecord(sn, hcode, eStockOper.Sell.toString(), num, price, sum,
					lastTotal - num, remain, date));
			logger.info(operList.get(operList.size() - 1).toString());
		}
	}
	
	
	//资金管理手段来控制卖
	private void sell(String hcode, OperRecord last, StockDay someDay, StockDay nextDay, 
			List<OperRecord> operList, eStockOper result) {

		BigDecimal remain = BigDecimal.ZERO;//remain 是账户剩余钱，相反数就是付出了多少钱
		int sn = 0;
		BigDecimal sum = BigDecimal.ZERO;
		int num = 0;
		
		//卖时机是通过资金管理手段来控制
		if (last == null) //还没有买
			return;
		if (last.getTotal() == 0)
			return;
		if (last.getDate_() != null && last.getDate_().equals(someDay.date_)) //昨天决定今天买，今天不能卖
			return;


		// *********** 最简单的资金管理，赚10%即卖
		BigDecimal price = new BigDecimal(someDay.low); //取今天最低价格
		//BigDecimal cost = last.getRemain().negate(); //成本，即当前价值，也即投入了多少
		BigDecimal cost = computeCost(operList);
		int lastTotal = last.getTotal();
		//当前价格*数量=总价
		BigDecimal gain_high = new BigDecimal(someDay.high).multiply(new BigDecimal(lastTotal));
		BigDecimal gain_low = new BigDecimal(someDay.low).multiply(new BigDecimal(lastTotal));
		Date date = someDay.date_;
		sn = last.getSn()+1;
		//BigDecimal sum_l = BigDecimal.ZERO;

		// *********** 最简单的资金管理，赚10%即卖
		if(nextDay == null) { //最后一天全卖
			num = lastTotal;
			sn = 999;
		}else if(result == eStockOper.Sell) {
			num = lastTotal;
//			logger.debug("oper is sell : {},{},{}", hcode, date, num);
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
//			logger.info(String.format("%s:(%s, %s),%s:(%s, %s),%s:(%s, %s),%s:(%s, %s)",
//			all.get(i - 2).date_, all.get(i - 2).low, all.get(i - 2).high, all.get(i - 1).date_,
//			all.get(i - 1).low, all.get(i - 1).high, all.get(i).date_, all.get(i).low, all.get(i).high,
//			i==all.size()-1?"":all.get(i + 1).date_, i==all.size()-1?"":all.get(i + 1).low, i==all.size()-1?"":all.get(i + 1).high));
			if(printOperLog)
				logger.info(operList.get(operList.size() - 1).toString());
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
