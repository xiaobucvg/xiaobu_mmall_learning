package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author zh_job
 * 2018/12/10 11:02
 */
@Controller
@RequestMapping("/cart/")
public class CartController {
	@Resource
	private ICartService iCartService;
	@Resource
	private IUserService userService;

	/** 往购物车里面添加商品 */
	@RequestMapping("add.do")
	@ResponseBody
	public ServerResponse add(HttpServletRequest req,Integer productId,Integer count){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆才能使用购物车.");
		}
		return iCartService.add(user.getId(), productId, count);
	}

	/** 更新购物车某个产品数量 */
	@RequestMapping("update.do")
	@ResponseBody
	public ServerResponse update(HttpServletRequest req, Integer productId, Integer count){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆才能使用购物车.");
		}
		return iCartService.update(user.getId(), productId, count);
	}

	/** 移除购物车一个或者多个产品 */
	@RequestMapping("remove.do")
	@ResponseBody
	public ServerResponse remove(HttpServletRequest req,String productIds){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆才能使用购物车.");
		}
		return iCartService.remove(user.getId(), productIds);
	}

	/** 购物车选中或者取消选中某个商品 */
	@RequestMapping("select.do")
	@ResponseBody
	public ServerResponse selectOneProduct(HttpServletRequest req,Integer productId){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆才能使用购物车.");
		}
		return iCartService.selectOneProduct(user.getId(), productId);
	}

	/** 查询在购物车里的产品数量 */
	@RequestMapping("get_product_count.do")
	@ResponseBody
	public ServerResponse getProductCount(HttpServletRequest req){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆才能使用购物车.");
		}
		return iCartService.getProductCount(user.getId());
	}

	/** 购物车取消全选 */
	@RequestMapping("un_select_all_products.do")
	@ResponseBody
	public ServerResponse unSelectAllProducts(HttpServletRequest req){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆才能使用购物车.");
		}
		return iCartService.unSelectAllProducts(user.getId());
	}

	/** 购物车全选 */
	@RequestMapping("select_all_products.do")
	@ResponseBody
	public ServerResponse selectAllProducts(HttpServletRequest req){
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "需要登陆才能使用购物车.");
		}
		return iCartService.selectAllProducts(user.getId());
	}
}
