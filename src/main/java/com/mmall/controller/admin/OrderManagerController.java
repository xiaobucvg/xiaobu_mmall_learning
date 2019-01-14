package com.mmall.controller.admin;

import com.mmall.common.ServerResponse;
import com.mmall.service.IOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author zh_job
 * 2018/12/19 10:58
 */
@Controller
@RequestMapping("/admin/order/")
public class OrderManagerController {
	@Resource
	private IOrderService iOrderService;


	/**
	 * 订单List
	 */
	@RequestMapping(value = "list.do")
	@ResponseBody
	public ServerResponse listOrders(
	                                 @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
	                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
		return iOrderService.manageListOrders(pageSize, pageNum);
	}


	/**
	 * 详情
	 */
	public ServerResponse orderDetail(Long orderNo) {
		return iOrderService.manageOrderDetail(orderNo);
	}

	/**
	 * 搜索
	 */
	//todo 暂时按照订单号精确匹配
	public ServerResponse searchOrder(Long orderNo, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
	                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
		return iOrderService.manageSearchOrder(orderNo, pageSize,pageNum );
	}

	/** 发货 */
	public ServerResponse sendGoods(Long orderNo) {
		return iOrderService.sendGoods(orderNo);
	}
}
