package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * 订单展示列表的商品
 *
 * @author zh_job
 * 2018/12/19 10:18
 */
public class OrderProductVo {
	private List<OrderItemVo> orderItemVos;
	private String imageHost;
	private BigDecimal totalPrice;

	public List<OrderItemVo> getOrderItemVos() {
		return orderItemVos;
	}

	public void setOrderItemVos(List<OrderItemVo> orderItemVos) {
		this.orderItemVos = orderItemVos;
	}

	public String getImageHost() {
		return imageHost;
	}

	public void setImageHost(String imageHost) {
		this.imageHost = imageHost;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}
}
