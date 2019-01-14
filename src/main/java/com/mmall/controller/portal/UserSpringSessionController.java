package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 *
 * 使用了springSession的用户控制
 *
 * @author zh_job
 * 2019/1/6 16:42
 */
@Controller
@RequestMapping("/user/spring/")
public class UserSpringSessionController {
	@Resource(name = "iUserService")
	private IUserService iUserService;

	/**
	 * 登陆接口
	 */
	@RequestMapping(value = "login.do")
	@ResponseBody
	public ServerResponse<User> login(String username, String password, HttpSession session) {
		ServerResponse<User> loginResponse = iUserService.login(username, password);
		if(loginResponse.isSuccess()){
			session.setAttribute(Const.CURRENT_USER, loginResponse.getData());
		}
		return loginResponse;
	}

	/**
	 * 登出接口
	 */
	@RequestMapping(value = "logout.do")
	@ResponseBody
	public ServerResponse<String> logout(HttpSession session) {
		session.removeAttribute(Const.CURRENT_USER);
		return ServerResponse.createBySuccess();
	}

	/**
	 * 获取用户信息
	 */
	@RequestMapping(value = "get_user_info.do")
	@ResponseBody
	public ServerResponse<User> getUserInfo(HttpSession session) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null) return ServerResponse.createByErrorMessage("用户未登录");
		return ServerResponse.createBySuccess(user);
	}
}
