package me.app;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import me.common.util.TypeUtil;

public class Temp {
	public static void main(String[] args) {
		Temp tmp = new Temp();
		try {
			tmp.tmp();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	class A {
		int i = 0;
		List<String> list = new ArrayList<String>();
		List<Integer> list2 = new ArrayList<Integer>();
		boolean b = true;
	}
}
