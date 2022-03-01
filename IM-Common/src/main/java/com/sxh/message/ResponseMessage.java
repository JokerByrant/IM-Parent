package com.sxh.message;

import java.io.Serializable;

/**
 * 返回消息公共类
 * @author FengJuan
 * @date 2019年5月8日
 * @Description
 *
 */
public class ResponseMessage implements Serializable {


	private static final long serialVersionUID = -2436691289396540473L;

    /**
     * 返回编码，默认200，成功
     */
    private int code = 200;

    /**
     * 反馈信息
     */
    private String message;

    /**
     * 反馈数据
     */
    private Object data;

    public ResponseMessage(){
	}

	public ResponseMessage(int code, String message, Object data){
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public ResponseMessage(String message, Object data){
		this.message = message;
	    this.data = data;
	}
	public ResponseMessage(String message){
		this.message = message;
	}
	public ResponseMessage(Object data){
	    this.data = data;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}




}
