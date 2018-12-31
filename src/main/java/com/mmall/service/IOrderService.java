package com.mmall.service;

import com.alipay.api.AlipayResponse;
import com.mmall.common.ServerResponse;

import java.util.Map;

/**
 * @author zh_job
 * 2018/12/15 13:26
 */
public interface IOrderService {
	ServerResponse<AlipayResponse> pay(Integer userId, Long orderNo);
	ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);
	ServerResponse<String> alipayCallBack(Map<String,String[]> paraMap);
	ServerResponse createOrder(Integer userId, Integer shippingId);
	ServerResponse cancelOrder(Integer userId, Long orderNo);
	ServerResponse listOrders(Integer userId,Integer pageSize,Integer pageNum);
	ServerResponse getOrderDetail(Integer userId, Long orderNo);
	ServerResponse getCartOrderProduct(Integer userId);

	ServerResponse manageListOrders(Integer pageSize,  Integer pageNum);
	ServerResponse manageOrderDetail(Long orderNo);
	ServerResponse manageSearchOrder(Long orderNo,Integer pageSize,Integer pageNum);
	ServerResponse sendGoods(Long orderNo);
}
