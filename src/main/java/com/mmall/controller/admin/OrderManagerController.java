package com.mmall.controller.admin;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author zh_job
 * 2018/12/19 10:58
 */
@Controller
@RequestMapping("/manager/order/")
public class OrderManagerController {
	@Resource
	private IOrderService iOrderService;
	@Resource
	private IUserService iUserService;


	/**
	 * 订单List
	 */
	@RequestMapping(value = "list.do")
	@ResponseBody
	public ServerResponse listOrders(HttpServletRequest req,
	                                 @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
	                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {

		User user = new User();
		boolean online = iUserService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online || !iUserService.checkAdmin(user).isSuccess()){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆管理员账户.");
		}

		return iOrderService.manageListOrders(pageSize, pageNum);
	}


	/**
	 * 详情
	 */
	public ServerResponse orderDetail(HttpServletRequest req, Long orderNo) {
		User user = new User();
		boolean online = iUserService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online || !iUserService.checkAdmin(user).isSuccess()){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆管理员账户.");
		}
		return iOrderService.manageOrderDetail(orderNo);
	}

	/**
	 * 搜索
	 */
	//todo 暂时按照订单号精确匹配
	public ServerResponse searchOrder(HttpServletRequest req, Long orderNo, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
	                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
		User user = new User();
		boolean online = iUserService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online || !iUserService.checkAdmin(user).isSuccess()){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆管理员账户.");
		}
		return iOrderService.manageSearchOrder(orderNo, pageSize,pageNum );
	}

	/** 发货 */
	public ServerResponse sendGoods(HttpServletRequest req, Long orderNo) {
		User user = new User();
		boolean online = iUserService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online || !iUserService.checkAdmin(user).isSuccess()){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆管理员账户.");
		}
		return iOrderService.sendGoods(orderNo);
	}
}
