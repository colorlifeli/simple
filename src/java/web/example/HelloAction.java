package web.example;

import common.ActionIf;
import common.annotation.ActionAnno.Action;
import common.annotation.ActionAnno.Pack;
import common.annotation.ActionAnno.Result;
import common.annotation.IocAnno.Ioc;

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
