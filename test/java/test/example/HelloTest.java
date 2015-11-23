package test.example;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class HelloTest {

	@Test
	public void hellotest() {
		System.out.println("cccc");
	}

	@Test
	public void hellotest2() throws Exception {
		System.out.println("aaaaa");
		throw new Exception("aaa");
	}

	@Test
	public void tello() {
		System.out.println("bbbb");
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);//小时
		System.out.println(hour);

		Date date = new Date();
		DateFormat df3 = DateFormat.getTimeInstance();//只显示出时分秒
		System.out.println(df3.format(date));

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		try {
			Date start = sdf.parse("10:12:30");
			System.out.println(start);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
