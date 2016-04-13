package me.common;

/**
 * 简单的自定义异常类
 * 
 * @author James
 *
 */
public class SimpleException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String retCd; //异常对应的返回码
	private String msgDes; //异常对应的描述信息

	public SimpleException() {
		super();
	}

	public SimpleException(String message) {
		super(message);
		msgDes = message;
	}

	public SimpleException(String retCd, String msgDes) {
		super();
		this.retCd = retCd;
		this.msgDes = msgDes;
	}

	public String getRetCd() {
		return retCd;
	}

	public String getMsgDes() {
		return msgDes;
	}
}
