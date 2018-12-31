package com.mmall.common;

/**  枚举类 列举了响应码 */
public enum ResponseCode {
	SUCCESS(0,"success"),
	ERROR(1,"error"),
	ILLEGAL_ARGUMENT(2,"illegal argument"),
	NEED_LOGIN(10,"need login");

	private final int code;
	private final String msg;

	ResponseCode(int code,String msg){
		this.code = code;
		this.msg = msg;
	}

	public int getCode(){
		return code;
	}
	public String getDesc(){
		return msg;
	}
}
