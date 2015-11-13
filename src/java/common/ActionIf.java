package common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
}
