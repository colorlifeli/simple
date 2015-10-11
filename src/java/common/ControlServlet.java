package common;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.annotation.ActionAnno;

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
		// System.out.println("pathName:"+pathName);
		/**
		 * 得到请求的Action名字
		 * */
		int index = pathName.indexOf(".");
		String ActionName = pathName.substring(1, index);

		// String ActionClassName = this.getInitParameter(ActionName);
		System.out.println(pathName);
		System.out.println(ActionName);

		// 执行Action的execute得到要返回的URL路径
		ActionAnno.ActionInfo info = ActionAnno.actions.get(ActionName);
		String url = null;
		if (info != null) {
			try {
				url = info.execute(request, response);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (url == null) {
			request.getRequestDispatcher("error.jsp").forward(request, response);
		} else {
			request.getRequestDispatcher(url).forward(request, response);
		}
	}
}
