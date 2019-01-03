package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConstants;
import com.alipay.api.AlipayResponse;
import com.alipay.api.internal.util.AlipaySignature;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 订单
 *
 * @author zh_job
 * 2018/12/15 12:14
 */
@Controller
@RequestMapping("/order/")
public class OrderController {
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	@Resource
	private IOrderService iOrderService;
	@Resource
	private IUserService userService;

	/**
	 * 创建订单
	 */
	@RequestMapping(value = "create.do")
	@ResponseBody
	public ServerResponse createOrder(HttpServletRequest req, Integer shippingId) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		return iOrderService.createOrder(user.getId(), shippingId);
	}

	/**
	 * 取消订单
	 */
	@RequestMapping(value = "cancel.do")
	@ResponseBody
	public ServerResponse cancelOrder(HttpServletRequest req, Long orderNo) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		return iOrderService.cancelOrder(user.getId(), orderNo);
	}

	/** 获取购物车中的已经选中的商品信息 */
	@RequestMapping(value = "get_cart_order_product.do")
	@ResponseBody
	public ServerResponse getCartOrderProduct(HttpServletRequest req) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		return iOrderService.getCartOrderProduct(user.getId());
	}

	/**
	 * 订单List
	 */
	@RequestMapping(value = "list.do")
	@ResponseBody
	public ServerResponse listOrders(HttpServletRequest req, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		return iOrderService.listOrders(user.getId(), pageSize, pageNum);
	}

	/**
	 * 订单详情
	 */
	@RequestMapping(value = "order_detail.do")
	@ResponseBody
	public ServerResponse getOrderDetail(HttpServletRequest req, Long orderNo) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		return iOrderService.getOrderDetail(user.getId(), orderNo);
	}

	/**
	 * 支付
	 */
	@RequestMapping(value = "pay.do")
	public ServerResponse pay(HttpServletRequest req, Long orderNo, Writer writer) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		ServerResponse<AlipayResponse> pay = iOrderService.pay(user.getId(), orderNo);
		if (pay.isSuccess()) {
			AlipayResponse alipayResponse = pay.getData();
			//todo 创建一个支付页面
			try {
				writer.write(alipayResponse.getBody());
			} catch (IOException e) {
				logger.error("创建支付页面失败.", e);
			}
			return null;
			//return ServerResponse.createBySuccess(alipayResponse.getBody());
		}
		return pay;
	}

	/**
	 * 查询订单支付状态
	 */
	@RequestMapping(value = "query_order_pay_status.do")
	@ResponseBody
	public ServerResponse queryOrderPayStatus(HttpServletRequest req, Long orderNo) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		return iOrderService.queryOrderPayStatus(user.getId(), orderNo);
	}

	/**
	 * 支付宝回调接口，支付的结果会通过POST请求调用此接口
	 */
	@RequestMapping(value = "call_back.do")
	@ResponseBody
	public Object callBack(WebRequest webRequest) {
		//获取支付宝POST过来反馈信息

		Map<String, String> params = Maps.newHashMap();
		Map<String, String[]> requestParams = webRequest.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
			String name = iter.next();
			String[] values = requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}
		boolean signVerified = false;
		logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());
		try {
			signVerified = AlipaySignature.rsaCheckV1(params, PropertiesUtil.getValue("public_key", ""), AlipayConstants.CHARSET_UTF8, AlipayConstants.SIGN_TYPE_RSA2);
		} catch (AlipayApiException e) {
			logger.error("验签出现异常.", e);
		}

		if (!signVerified) {//验证失败
			logger.error("验证失败.");
			return Const.AlipayResponse.RESPONSE_FALSE;
		}
		//todo 实际验证过程建议商户务必添加以下校验：
		//1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
		//2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		//3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
		//4、验证app_id是否为该商户本身。
		ServerResponse<String> res = iOrderService.alipayCallBack(requestParams);
		if(!res.isSuccess()){
			return Const.AlipayResponse.RESPONSE_FALSE;
		}
		return Const.AlipayResponse.TRADE_SUCCESS;
	}


}
