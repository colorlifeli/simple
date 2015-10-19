package web.example;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import common.ActionIf;
import common.annotation.ActionAnno.Action;
import common.annotation.ActionAnno.Pack;
import common.annotation.ActionAnno.Result;
import common.annotation.IocAnno.Ioc;
import common.util.NetUtil;

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

	public void getData() {

		String url1 = "http://hq.sinajs.cn/list=sh600151,sz000830,s_sh000001,s_sz399001,s_sz399106";
		String url = "http://hq.sinajs.cn/list=sh600151,sz000830,";

		try {
			URL u = new URL(url);
			byte[] b = new byte[256];
			InputStream in = null;
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			try {
				in = u.openStream();
				int i;
				while ((i = in.read(b)) != -1) {
					bo.write(b, 0, i);
				}
				String result = bo.toString("GBK");
				String[] stocks = result.split(";");
				for (String stock : stocks) {
					// "var hq_str_sz000830="鲁西化工,6.86,6.86,6.78,6.90,6.74,6.78,6.79,33275590,227193182.00,69000,6.78,19400,6.77,58900,6.76,122800,6.75,123900,6.74,112161,6.79,64000,6.80,49000,6.81,146600,6.82,142800,6.83,2015-10-19,13:41:22,00""
					String[] str = stock.split("=");
					String code = str[0].substring(13);
					str[1] = str[1].substring(1).substring(0, str[1].length() - 2);
					String[] datas = str[1].split(",");
					datas[0] = code; // 将名称换成code
					// 根据对照自己对应数据
					System.out.println(stock);
				}
				bo.reset();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

	}

	public static void main(String args[]) {
		NetUtil.me().setProxy();
		HelloAction action = new HelloAction();
		action.getData();
	}

}
