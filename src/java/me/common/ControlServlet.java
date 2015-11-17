package me.common;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.common.annotation.ActionAnno;

public class ControlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		/**
		 * 得到当前Servlet的请求路径   
		 * */
		String pathName = request.getServletPath();
		System.out.println("pathName:" + pathName);

		// 执行Action的execute得到要返回的URL路径
		ActionAnno.ActionInfo info = ActionAnno.actions.get(pathName);
		String url = null;
		if (info != null) {
			try {
				url = info.execute(request, response);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (url != null && !"".equals(url)) {
			request.getRequestDispatcher(url).forward(request, response);
		}
		// 没有返回url认为是 ajax请求了，不作处理。至于网页找不到等的错误，在 web.xml中配置相应页面。
		// request.getRequestDispatcher("error.jsp").forward(request, response);
	}
}
