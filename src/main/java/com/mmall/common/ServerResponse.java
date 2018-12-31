package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 *  通用响应对象
 * @author zh_job
 *  2018/12/4 9:17
 **/
//空属性不构建成Json元素
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
	private int status;
	private String msg;
	private T data;

	/*构造*/
	private ServerResponse(int status){
		this.status = status;
	}
	private ServerResponse(int status,T data){
		this.status = status;
		this.data =data;
	}
	private ServerResponse(int status,String msg){
		this.status = status;
		this.msg = msg;
	}
	private ServerResponse(int status,String msg,T data){
		this.status = status;
		this.msg = msg;
		this.data = data;
	}

	//这条属性不构建成Json
	@JsonIgnore
	public boolean isSuccess(){
		return status == ResponseCode.SUCCESS.getCode();
	}

	/*成功*/
	public static <T> ServerResponse<T> createBySuccess(){
		return new ServerResponse<>(ResponseCode.SUCCESS.getCode(),ResponseCode.SUCCESS.getDesc());
	}
	public static <T> ServerResponse<T> createBySuccessMessage(String message){
		return new ServerResponse<>(ResponseCode.SUCCESS.getCode(),message);
	}
	public static <T> ServerResponse<T> createBySuccess(T data){
		return new ServerResponse<>(ResponseCode.SUCCESS.getCode(),data);
	}
	public static <T> ServerResponse<T> createBySuccess(String message,T data){
		return new ServerResponse<>(ResponseCode.SUCCESS.getCode(),message,data);
	}

	/*错误*/
	public static <T> ServerResponse<T> createByError(){
		return new ServerResponse<>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
	}
	public static <T> ServerResponse<T> createByErrorMessage(String message){
		return new ServerResponse<>(ResponseCode.ERROR.getCode(),message);
	}
	public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode,String message){
		return new ServerResponse<>(errorCode,message);
	}

	/*Getter And Setter*/
	public T getData(){
		return this.data;
	}
	public String getMsg(){
		return this.msg;
	}
	public int getStatus(){
		return this.status;
	}
}
