package com.mmall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayObject;
import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.GoodsDetail;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import org.apache.commons.lang.time.DateUtils;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 订单
 *
 * @author zh_job
 * 2018/12/15 13:26
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {
	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
	@Resource
	// 这个是线程安全的
	private AlipayClient alipayClient;
	@Resource
	private OrderMapper orderMapper;
	@Resource
	private OrderItemMapper orderItemMapper;
	@Resource
	private PayInfoMapper payInfoMapper;
	@Resource
	private CartMapper cartMapper;
	@Resource
	private ProductMapper productMapper;
	@Resource
	private ShippingMapper shippingMapper;


	@Transactional
	public ServerResponse createOrder(Integer userId, Integer shippingId) {
		if (userId == null || shippingId == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数无效.");
		}
		List<Cart> carts = cartMapper.selectCheckedCartByUserId(userId);
		if (carts == null || carts.size() == 0) {
			return ServerResponse.createByErrorMessage("用户购物车中没有商品.");
		}
		List<OrderItem> orderItems = createOrderItems(carts);
		// 组装订单
		Order order = assembleOrder(userId, shippingId, orderItems);
		if(order == null){
			return ServerResponse.createByErrorMessage("生成订单错误.");
		}
		// 批量插入到数据库
		int res = orderItemMapper.batchInsert(orderItems);
		// 减少产品库存
		reduceStock(orderItems);
		// 清空购物车
		clearCart(carts);
		// 将结果包装成VO对象
		OrderVo orderVo = assembleOrderVo(order, orderItems);
		return ServerResponse.createBySuccess(orderVo);
	}

	public ServerResponse cancelOrder(Integer userId, Long orderNo){
		if (userId == null || orderNo == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数无效.");
		}
		Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
		if(order == null){
			return ServerResponse.createByErrorMessage("用户没有此订单.");
		}
		if(order.getStatus() != Const.Order.ORDER_UNPAID){
			return ServerResponse.createByErrorMessage("该订单无法取消.因为已经取消或者已经付款.");
		}
		order.setStatus(Const.Order.ORDER_CANCELLED);
		int res = orderMapper.updateByPrimaryKeySelective(order);
		if(res == 0){
			return ServerResponse.createByErrorMessage("取消订单失败.");
		}
		return ServerResponse.createBySuccess("取消订单成功.");
	}

	public ServerResponse listOrders(Integer userId,Integer pageSize,Integer pageNum){
		if(userId == null || pageNum == null || pageSize == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数无效.");
		}
		PageHelper.startPage(pageNum, pageSize);
		List<Order> orders = orderMapper.selectByUserId(userId);
		List<OrderVo> orderVos = assembleOrderVoList(orders, userId);
		PageInfo pageInfo = new PageInfo(orderVos);
		return ServerResponse.createBySuccess(pageInfo);
	}

	/** 组装订单VO列表 */
	private List<OrderVo> assembleOrderVoList(List<Order> orders,Integer userId){
		List<OrderVo> orderVos = Lists.newArrayList();
		if(userId == null){
			for(Order order : orders){
				// todo 管理员查询不需要userId
				List<OrderItem> orderItems = orderItemMapper.getByOrderNo(order.getOrderNo());
				orderVos.add(assembleOrderVo(order, orderItems));
			}
		} else {
			for(Order order : orders){
				List<OrderItem> orderItems = orderItemMapper.getByOrderNoUserId(userId, order.getOrderNo());
				orderVos.add(assembleOrderVo(order, orderItems));
			}
		}
		return orderVos;
	}

	public ServerResponse getOrderDetail(Integer userId, Long orderNo){
		if(userId == null || orderNo == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数无效.");
		}
		Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
		if(order == null){
			return ServerResponse.createByErrorMessage("没有找到该订单.");
		}
		List<OrderItem> orderItems = orderItemMapper.getByOrderNoUserId(userId, orderNo);
		OrderVo orderVo = assembleOrderVo(order, orderItems);
		return ServerResponse.createBySuccess(orderVo);
	}

	/** 包装成OrderVo对象 */
	private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
		if(order.getCreateTime() == null){
			order = orderMapper.selectByOrderNo(order.getOrderNo());
		}
		List<OrderItemVo> list = Lists.newArrayList();
		for(OrderItem orderItem : orderItemList){
			list.add(createOrderItemVo(orderItem));
		}
		Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
		OrderVo orderVo = new OrderVo();
		orderVo.setOrderNo(order.getOrderNo());
		orderVo.setPayment(order.getPayment());
		orderVo.setPaymentType(order.getPaymentType());
		orderVo.setPostage(order.getPostage());
		orderVo.setStatus(order.getStatus());
		orderVo.setCreateTime(DateUtil.dateToString(order.getCreateTime()));
		orderVo.setPaymentTime(DateUtil.dateToString(order.getPaymentTime()));
		orderVo.setCloseTime(DateUtil.dateToString(order.getCloseTime()));
		orderVo.setEndTime(DateUtil.dateToString(order.getEndTime()));
		orderVo.setSendTime(DateUtil.dateToString(order.getSendTime()));
		orderVo.setOrderItemVoList(list);
		orderVo.setShippingId(order.getShippingId());
		orderVo.setImageHost(PropertiesUtil.getValue("ftp.server.http.prefix"));
		orderVo.setShipping(shipping);
		orderVo.setPaymentDescription(Const.PaymentType.getDesc(order.getPaymentType()).getDescription());
		return orderVo;
	}

	/** 修改成OrderItemVo对象 */
	private OrderItemVo createOrderItemVo(OrderItem orderItem){
		OrderItemVo orderItemVo = new OrderItemVo();
		orderItemVo.setOrderNo(orderItem.getOrderNo());
		orderItemVo.setCreateTime(orderItem.getCreateTime());
		orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
		orderItemVo.setProductId(orderItem.getProductId());
		orderItemVo.setProductImage(orderItem.getProductImage());
		orderItemVo.setQuantity(orderItem.getQuantity());
		orderItemVo.setTotalPrice(orderItem.getTotalPrice());
		orderItemVo.setProductName(orderItem.getProductName());
		return orderItemVo;
	}

	/** 清空购物车 */
	private void clearCart(List<Cart> carts){
		for(Cart cart : carts){
			cartMapper.deleteByPrimaryKey(cart.getId());
		}
	}

	/**
	 * 生成订单商品明细表
	 */
	private List<OrderItem> createOrderItems(List<Cart> carts) {
		List<OrderItem> orderItems = Lists.newArrayList();
		for (Cart cart : carts) {
			OrderItem orderItem = new OrderItem();
			Product product = productMapper.selectByPrimaryKey(cart.getProductId());
			orderItem.setProductId(product.getId());
			orderItem.setProductName(product.getName());
			orderItem.setCurrentUnitPrice(product.getPrice());
			orderItem.setProductImage(product.getMainImage());
			orderItem.setTotalPrice(BigDecimalUtil.multi(product.getPrice().doubleValue(), cart.getQuantity()));
			orderItem.setQuantity(cart.getQuantity());
			orderItem.setUserId(cart.getUserId());
			orderItems.add(orderItem);
		}
		return orderItems;
	}

	/** 减少产品库存 */
	private void reduceStock(List<OrderItem> orderItems){
		for(OrderItem orderItem : orderItems){
			Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
			product.setStock(product.getStock() - orderItem.getQuantity());
			productMapper.updateByPrimaryKeySelective(product);
		}
	}

	/**
	 * 组装订单
	 */
	private Order assembleOrder(Integer userId,Integer shippingId,List<OrderItem> orderItems) {
		Order order = new Order();
		order.setOrderNo(generatorOrderNo());
		order.setStatus(Const.Order.ORDER_UNPAID);
		order.setPostage(0);
		order.setShippingId(shippingId);
		order.setUserId(userId);
		order.setPayment(getPayment(orderItems));
		order.setPaymentType(Const.PaymentType.ONLINE.getCode());
		// 给订单明细赋予订单号
		for(OrderItem orderItem : orderItems){
			orderItem.setOrderNo(order.getOrderNo());
		}
		int res = orderMapper.insertSelective(order);
		if(res == 0){
			return null;
		}
		return order;
	}

	/** 计算总价 */
	private BigDecimal getPayment(List<OrderItem> orderItems){
		BigDecimal bigDecimal = new BigDecimal("0");
		for(OrderItem orderItem : orderItems){
			bigDecimal = BigDecimalUtil.add(orderItem.getTotalPrice().doubleValue(), bigDecimal.doubleValue());
		}
		return bigDecimal;
	}

	/**
	 * 生成订单号
	 * 实际应用中订单号生成是很复杂的
	 * 在这里简单的用时间戳生成了
	 * 为了放置并发生成了相同的订单号 后面加上了随机数
	 */
	private Long generatorOrderNo() {
		long currentTime = new Date().getTime();
		return currentTime + new Random().nextInt(100);
	}

	public ServerResponse<AlipayResponse> pay(Integer userId, Long orderNo) {
		if (userId == null || orderNo == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数无效.");
		}
		Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
		if (order == null) {
			return ServerResponse.createByErrorMessage("未查询到该订单.");
		}
		// 把订单组组装成 BizModel
		AlipayTradePagePayModel bizModel = assembledBizModel(userId, order);
		if (bizModel == null) {
			return ServerResponse.createByErrorMessage("无法创建支付订单.");
		}
		// 创建支付宝订单
		AlipayResponse alipayOrder = createAlipayOrder(bizModel);
		return ServerResponse.createBySuccess(alipayOrder);
	}

	/**
	 * 组装成 BizModel
	 */
	private AlipayTradePagePayModel assembledBizModel(Integer userId, Order order) {
		if (order.getStatus() != Const.Order.ORDER_UNPAID) {
			return null;
		}
		AlipayTradePagePayModel bizModel = new AlipayTradePagePayModel();
		bizModel.setOutTradeNo(order.getOrderNo().toString());
		bizModel.setProductCode("FAST_INSTANT_TRADE_PAY");
		bizModel.setTotalAmount(order.getPayment().toString());
		//todo 显示的标题是第一个商品的标题 这里是固定值
		bizModel.setSubject("支付宝订单标题.");
		//todo 显示的主体没想好 先设置固定值
		//bizModel.setBody("这里是描述.");
		return bizModel;
	}

	/**
	 * 创建支付宝网页订单
	 */
	private AlipayResponse createAlipayOrder(AlipayObject alipayBizModel) {
		AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
		AlipayTradePagePayResponse response = null; // 响应
		request.setNotifyUrl(PropertiesUtil.getValue("alipay.callback.url"));// 回调地址
		//todo 测试回调地址用的
		request.setNotifyUrl("http://9xznih.natappfree.cc/order/call_back.do");// 回调地址
		request.setBizModel(alipayBizModel);
		try {
			response = alipayClient.pageExecute(request);
		} catch (AlipayApiException e) {
			logger.error("创建支付宝订单失败.", e);
		}
		return response;
	}

	public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
		if (userId == null || orderNo == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数无效.");
		}
		Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
		if (order == null) {
			return ServerResponse.createByErrorMessage("用户没有该订单.");
		}
		if (order.getStatus() == Const.Order.ORDER_UNPAID) {
			return ServerResponse.createBySuccess("false");
		} else if (order.getStatus() == Const.Order.ORDER_PAID) {
			return ServerResponse.createBySuccess("true");
		}
		return ServerResponse.createByErrorMessage("该订单不是可支付状态");
	}

	/**
	 * 支付后返回订单状态
	 */
	public ServerResponse<String> alipayCallBack(Map<String, String[]> paraMap) {
		String tradeNo = paraMap.get("out_trade_no")[0];
		String payNo = paraMap.get("trade_no")[0];
		String tradeStatus = paraMap.get("trade_status")[0];

		Order order = null;
		order = orderMapper.selectByOrderNo(new Long(tradeNo));
		if(order == null){
			return ServerResponse.createByErrorMessage("不是本商城订单.");
		}
		// 交易成功 付款完成后，支付宝系统发送该交易状态通知
		if (tradeStatus.equals(Const.AlipayResponse.TRADE_SUCCESS)) {
			if (order.getStatus() > Const.Order.ORDER_UNPAID) {
				return ServerResponse.createBySuccess("支付宝重复调用.");
			}
			order.setStatus(Const.Order.ORDER_PAID);
			order.setPaymentTime(DateUtil.stringToDate(paraMap.get("gmt_payment")[0]));
			orderMapper.updateByPrimaryKeySelective(order);
		}
		//todo 交易完成等 退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知 不支持退款 所以做处理
		PayInfo payInfo = new PayInfo();
		payInfo.setUserId(order.getUserId());
		payInfo.setOrderNo(order.getOrderNo());
		payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
		payInfo.setPlatformNumber(payNo);
		payInfo.setPlatformStatus(tradeStatus);
		payInfoMapper.insert(payInfo);
		return ServerResponse.createBySuccess();
	}


	public ServerResponse getCartOrderProduct(Integer userId) {
		OrderProductVo orderProductVo = new OrderProductVo();
		// 从购物车获取数据
		List<Cart> carts = cartMapper.selectCheckedCartByUserId(userId);
		List<OrderItem> orderItems = createOrderItems(carts);
		List<OrderItemVo> orderItemVos = Lists.newArrayList();
		BigDecimal payment = getPayment(orderItems);
		for(OrderItem orderItem : orderItems){
			orderItemVos.add(createOrderItemVo(orderItem));
		}
		orderProductVo.setTotalPrice(payment);
		orderProductVo.setOrderItemVos(orderItemVos);
		orderProductVo.setImageHost(PropertiesUtil.getValue("ftp.server.http.prefix"));
		return ServerResponse.createBySuccess(orderProductVo);
	}


	//admin

	public ServerResponse manageListOrders(Integer pageSize,  Integer pageNum) {
		if(pageNum == null || pageSize == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数无效.");
		}
		PageHelper.startPage(pageNum, pageSize);
		List<Order> orders = orderMapper.selectAll();
		List<OrderVo> orderVos = assembleOrderVoList(orders, null);
		PageInfo pageInfo = new PageInfo(orderVos);
		return ServerResponse.createBySuccess(pageInfo);
	}

	public ServerResponse manageOrderDetail(Long orderNo){
		Order order = orderMapper.selectByOrderNo(orderNo);
		if(order == null){
			return ServerResponse.createByErrorMessage("订单不存在.");
		}
		List<OrderItem> orderItems = orderItemMapper.getByOrderNo(orderNo);
		OrderVo orderVo = assembleOrderVo(order, orderItems);
		return ServerResponse.createBySuccess(orderVo);
	}

	public ServerResponse manageSearchOrder(Long orderNo,Integer pageSize,Integer pageNum) {
		PageHelper.startPage(pageNum, pageSize);
		Order order = orderMapper.selectByOrderNo(orderNo);
		if(order == null){
			return ServerResponse.createByErrorMessage("订单不存在.");
		}
		List<OrderItem> orderItems = orderItemMapper.getByOrderNo(orderNo);
		OrderVo orderVo = assembleOrderVo(order, orderItems);
		PageInfo pageInfo = new PageInfo(Lists.newArrayList(orderVo));
		return ServerResponse.createBySuccess(pageInfo);
	}

	public ServerResponse sendGoods(Long orderNo) {
		Order order = orderMapper.selectByOrderNo(orderNo);
		if(order == null){
			return ServerResponse.createByErrorMessage("订单不存在.");
		}
		if(order.getStatus() != Const.Order.ORDER_PAID){
			return ServerResponse.createByErrorMessage("订单状态错误.");
		}
		order.setStatus(Const.Order.ORDER_SHIPPED);
		order.setSendTime(new Date());
		return ServerResponse.createBySuccess("发货成功.");
	}

	/** 关闭订单 hour个小时之内*/
	public void closeOrder(int hour){
		Date date = DateUtils.addHours(new Date(), -hour);
		List<Order> orders = orderMapper.selectOrderStatusByTime(Const.Order.ORDER_UNPAID, DateUtil.dateToString(date));
		for (Order order : orders){
			List<OrderItem> orderItems = orderItemMapper.getByOrderNo(order.getOrderNo());
			for(OrderItem orderItem : orderItems){
				// 使用了锁
				Integer stock = productMapper.selectStockById(orderItem.getProductId());
				if(stock == null){
					continue;
				}
				Product product = new Product();
				product.setId(orderItem.getProductId());
				product.setStock(orderItem.getQuantity() + stock);
				productMapper.updateByPrimaryKeySelective(product);
			}
			orderMapper.closeOrderByOrderId(order.getId());
			logger.info("关闭订单：{}", order.getOrderNo());
		}
	}
}
