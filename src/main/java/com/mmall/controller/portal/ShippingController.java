package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
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
 *
 * 收货地址
 *
 * @author zh_job
 * 2018/12/12 9:42
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {
	@Resource
	private IShippingService iShippingService;
	@Resource
	private IUserService userService;

	/** 添加地址 */
	@RequestMapping("add.do")
	@ResponseBody
	public ServerResponse add(HttpServletRequest req, Shipping shipping){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		shipping.setUserId(user.getId());
		return iShippingService.add(user.getId(), shipping);
	}

	/** 删除地址 */
	@RequestMapping("remove.do")
	@ResponseBody
	public ServerResponse remove(HttpServletRequest req, Integer id){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		return iShippingService.remove(user.getId(), id);
	}

	/** 登录状态更新地址 */
	@RequestMapping("update.do")
	@ResponseBody
	public ServerResponse update(HttpServletRequest req, Shipping shipping){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		shipping.setUserId(user.getId());
		return iShippingService.update(user.getId(), shipping);
	}

	/** 选中查看具体的地址 */
	@RequestMapping("detail.do")
	@ResponseBody
	public ServerResponse detail(HttpServletRequest req,Integer id){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		return iShippingService.detail(user.getId(), id);
	}

	/** 地址列表 */
	@RequestMapping("list.do")
	@ResponseBody
	public ServerResponse list(HttpServletRequest req, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,@RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆.");
		}
		return iShippingService.list(user.getId(), pageNum, pageSize);
	}
}
