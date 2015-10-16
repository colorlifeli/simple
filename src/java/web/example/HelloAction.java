package web.example;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import common.ActionIf;
import common.annotation.ActionAnno.Action;
import common.annotation.ActionAnno.Pack;
import common.annotation.ActionAnno.Result;
import common.util.NetUtil;

@Pack(path = "hello")
public class HelloAction extends ActionIf {

	@Action(path = "hello", targets = { @Result(name = "success", value = "Hello.jsp") })
	public String hello() {

		request.setAttribute("str", "Hello world");

		return "success";
	}

	public void getData() {

		String url = "http://hq.sinajs.cn/list=sh600151,sz000830,s_sh000001,s_sz399001,s_sz399106";
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
					String[] datas = stock.split(",");
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
