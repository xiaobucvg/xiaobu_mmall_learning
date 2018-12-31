package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 常量类
 *
 * @author zh_job
 * 2018/12/4 11:08
 */
public class Const {
	public static final String CURRENT_USER = "currentUser";

	public static final String EMAIL = "email";

	public static final String USERNAME = "username";

	public static final Set<String> ORDER = Sets.newHashSet("price_asc","price_desc");

	public interface Role {
		int ROLE_COSTUMER = 0;
		int ROLE_ADMIN = 1;
	}

	public interface Cart{
		int CHECK_TRUE = 1;
		int CHECK_FALSE = 0;

		String LIMITED_TRUE = "Adequate inventory";
		String LIMITED_FALSE = "Inventory shortage";
	}

	public interface Order{
		int ORDER_CANCELLED = 0;
		int ORDER_UNPAID = 10;
		int ORDER_PAID = 20;
		int ORDER_SHIPPED = 40;
	}

	public interface AlipayResponse{
		String RESPONSE_FALSE = "failure";
		String RESPONSE_TRUE = "success";

		String TRADE_SUCCESS = "TRADE_SUCCESS";
		String TRADE_CLOSED = "TRADE_CLOSED";
		String TRADE_FINISHED = "TRADE_FINISHED";
		String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
	}

	public enum PaymentType{
		ONLINE("在线支付",1);
		private String description;
		private Integer code;

		public static PaymentType getDesc(Integer code){
			for(PaymentType paymentType : PaymentType.values()){
				if(paymentType.getCode().equals(code)){
					return paymentType;
				}
			}
			throw new RuntimeException("没有找到对应的支付类型.");
		}

		PaymentType(String description, Integer code) {
			this.description = description;
			this.code = code;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Integer getCode() {
			return code;
		}

		public void setCode(Integer code) {
			this.code = code;
		}
	}

	public enum PayPlatformEnum{
		ALIPAY("支付宝",1);

		private String description;
		private Integer code;
		PayPlatformEnum(String description, Integer code){
			this.code = code;
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Integer getCode() {
			return code;
		}

		public void setCode(Integer code) {
			this.code = code;
		}
	}
}
