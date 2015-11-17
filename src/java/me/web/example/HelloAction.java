package me.web.example;

import me.common.ActionIf;
import me.common.annotation.ActionAnno.Action;
import me.common.annotation.ActionAnno.Pack;
import me.common.annotation.ActionAnno.Result;
import me.common.annotation.IocAnno.Ioc;

@Pack(path = "hello")
public class HelloAction extends ActionIf {
	@Ioc(name = "helloService")
	private HelloService hservice;

	@Action(path = "hello", targets = { @Result(name = "success", value = "Hello.jsp") })
	public String hello() {

		request.setAttribute("str", "Hello world");

		return "success";
	}

	public String getHello() {
		return hservice.getHello();
	}

	public HelloService getHservice() {
		return hservice;
	}

	public void setHservice(HelloService hservice) {
		this.hservice = hservice;
	}

}
