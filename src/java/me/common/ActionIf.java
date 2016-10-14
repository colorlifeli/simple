package me.common;

import java.lang.reflect.Field;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.common.jdbcutil.h2.H2Helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ActionIf {

	// public String execute(HttpServletRequest request, HttpServletResponse
	// response);
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected HttpServletRequest request;
	protected HttpServletResponse response;

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * 将页面传递的参数组合成map
	 * 
	 * 由于此函数与 action的自动注入有冲突（会找不到字段的set函数），因此尽量少用
	 * 
	 * 如果是日期，则需要增加辅助字段，比较复杂，暂不支持
	 * @param classType：对于数据库字段的实体类或 vo类。
	 * @param voMap
	 */
	protected <T> void getVoMapFromJsp(Class<T> classType, Map<String, String> voMap) {
		// voMap is use for recording query rule
		// voMap是用于记录查询规则的，如整型字段，对于区间的查询
		Field[] ff = classType.getDeclaredFields();
		for (Field f : ff) {
			f.setAccessible(true);
			String name = f.getName();
			String type = f.getType().getName().substring(f.getType().getName().lastIndexOf(".") + 1);
			String value = request.getParameter(name);

			switch (H2Helper.Type.toType(type.toUpperCase())) {
			case NOVALUE:
				//复合主键
				if (name.equals("id")) {
					getVoMapFromJsp(f.getType(), voMap);
				}
				break;
			default:
				voMap.put(name, value);
				//logger.debug(name + "=" + value);
				break;
			}

		}
	}
	
}
