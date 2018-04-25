package me.app;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.common.internal.BeanContext;
import me.common.jdbcutil.SqlRunner;
import me.common.jdbcutil.h2.H2Helper;
import me.common.util.TypeUtil;
import me.net.dao.StockSourceDao;
import me.service.stock.AnalysisService;
import me.service.stock.AnalysisService2;

public class Temp2 {
	public static void main(String[] args) {
		Temp2 tmp = new Temp2();
		try {
			tmp.testAnalysisService2();
			//tmp.testSellSomeDay();
			//tmp.tmp2();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}


	public void testAnalysisService2() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = BeanContext.me();
		AnalysisService service = (AnalysisService) bc.getBean("analysisService");
		StockSourceDao stockSourceDao = (StockSourceDao) bc.getBean("stockSourceDao");
		//service.computeAll();

		try {
			//List<String> codes = stockSourceDao.getAllAvailableCodes(0, eStockSource.YAHOO);
			List<String> codes = stockSourceDao.getAllAvailableCodes(0, null);
			for (String code : codes) {
				service.compute(code);

			}

			try {
				System.out.println(service.summary(false));
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		H2Helper.close(SqlRunner.me().getConn());
	}

	public void testAnalysisService3() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = BeanContext.me();
		AnalysisService2 service = (AnalysisService2) bc.getBean("analysisService2");
		//service.computeAll();

		try {
			//600980 生成的中枢非常经典，posision由1，2，3，－1，4，4，5... 经查看是正确的。因为 －1 的 central 形成后，下一点是更高的点，
			//这时-1 central的最后一点要舍弃，要重新计算central，这时又与前一个central冲突，所以central 被被抛弃了
			//仅是从表面实在很难想象这个过程。所以正确的逻辑真的很重要！！
			//603116, 601137
			service.compute("601137");
			//service.sellSomeday2();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		H2Helper.close(SqlRunner.me().getConn());
	}
	
	public void testSellSomeDay() {
		SqlRunner.me().setConn(H2Helper.connEmbededDb());

		BeanContext bc = BeanContext.me();
		AnalysisService2 service = (AnalysisService2) bc.getBean("analysisService2");

		service.sellSomeday3();

		H2Helper.close(SqlRunner.me().getConn());
	}

	public void tmp() throws Exception {

		System.out.println(int.class.getName());
		System.out.println(String.class.getName());

		Field field = A.class.getDeclaredField("i");
		if (int.class.equals(field.getType()))
			System.out.println("yes");
		System.out.println(field.getType());

		Field field2 = A.class.getDeclaredField("list");
		Type gt = field2.getGenericType(); //得到泛型类型  
		ParameterizedType pt = (ParameterizedType) gt;
		Class<?> lll = (Class<?>) pt.getActualTypeArguments()[0];
		System.out.println("list:" + lll.getName());

		Field field3 = A.class.getDeclaredField("b");
		System.out.println(field3.getType());

		A a = new A();
		field.set(a, TypeUtil.stringValueToObject("10", int.class));
		System.out.println(a.i);

		field.set(a, TypeUtil.stringValueToObject("10", Integer.class));
		System.out.println(a.i);

		TypeUtil.setField(a, "list2", new String[] { "1", "3" });
		for (int i : a.list2) {
			System.out.println(i);
		}
	}
	
	public void tmp2() throws Exception {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date start = format.parse("2016-08-30");
		Date end = new Date();
		c.setTime(start);
		while(start.before(end)) {
			System.out.println(format.format(start));
			c.add(Calendar.DAY_OF_YEAR, 1);
			start = c.getTime();
		}
	}
	

	class A {
		int i = 0;
		List<String> list = new ArrayList<String>();
		List<Integer> list2 = new ArrayList<Integer>();
		boolean b = true;
	}
}
