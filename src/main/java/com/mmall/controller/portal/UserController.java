package com.mmall.controller.portal;

import com.github.pagehelper.StringUtil;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JedisUtil;
import com.mmall.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户控制
 *
 * @author zh_job
 * 2018/12/4 8:12
 **/
@Controller
@RequestMapping("/user/")
public class UserController {
	@Resource(name = "iUserService")
	private IUserService iUserService;

	/**
	 * 登陆接口
	 */
	@RequestMapping(value = "login.do")
	@ResponseBody
	public ServerResponse<User> login(String username, String password, HttpServletRequest req, HttpServletResponse resp) {
		ServerResponse<User> loginResponse = iUserService.login(username, password);
		if (loginResponse.isSuccess()) {
			String jsessionIdValue = CookieUtil.getJessionIdValue(req);
			CookieUtil.resUserCookie(resp, jsessionIdValue);
			String res = JedisUtil.setEx(jsessionIdValue, JsonUtil.objToJson(loginResponse.getData()), Const.UserConst.ONLINE_TIME);
			if (res != null)
				return loginResponse;
		}
		return ServerResponse.createByErrorMessage("未知错误,登陆失败.");
	}

	/**
	 * 登出接口
	 */
	@RequestMapping(value = "logout.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse<String> logout(HttpServletResponse resp, HttpServletRequest req) {
		String key = CookieUtil.reqUserCookie(req);
		JedisUtil.del(key);
		CookieUtil.removeUserCookie(req, resp);
		return ServerResponse.createBySuccess();
	}

	/**
	 * 注册接口
	 */
	@RequestMapping(value = "register.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> register(User user) {
		return iUserService.register(user);
	}

	/**
	 * 验证有效性（用户名 或者 邮箱）
	 */
	@RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> checkValid(String str, String type) {
		return iUserService.checkValid(str, type);
	}

	/**
	 * 获取用户信息
	 */
	@RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> getUserInfo(HttpServletRequest req, HttpServletResponse resp) {
		String key = CookieUtil.reqUserCookie(req);
		if (StringUtil.isEmpty(key)) {
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		String jsonRes = JedisUtil.get(key);
		User user = JsonUtil.jsonToObj(jsonRes, User.class);
		if (user == null) {
			return ServerResponse.createByErrorMessage("获取用户信息失败.");
		}
		return ServerResponse.createBySuccess(user);
	}

	/**
	 * 获取找回密码的问题
	 */
	@RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetGetQuestion(String username) {
		return iUserService.forgetGetQuestion(username);
	}

	/**
	 * 验证找回密码的问题
	 */
	@RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetCheckQuestionAnswer(String username, String question, String answer) {
		return iUserService.forgetCheckQuestionAnswer(username, question, answer);
	}

	/**
	 * 忘记密码的重置密码
	 */
	@RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgerRestPassword(String username, String passwordNew, String forgetToken) {
		return iUserService.forgerRestPassword(username, passwordNew, forgetToken);
	}

	/**
	 * 登录状态重置密码
	 */
	@RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> restPassword(HttpServletRequest req, String newPassword, String oldPassword) {
		User user = new User();
		boolean online = iUserService.isOnline(CookieUtil.reqUserCookie(req), user);
		if (!online) {
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		return iUserService.resetPassword(oldPassword, newPassword, user);
	}

	/**
	 * 登录状态修改个人信息
	 */
	@RequestMapping(value = "update_user_info.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> updateUserInformation(User user, HttpServletRequest req) {
		User oldUser = new User();
		boolean online = iUserService.isOnline(CookieUtil.reqUserCookie(req), oldUser);
		if (!online) {
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		//防止横向越权 只能修改当前登陆的用户信息
		user.setId(oldUser.getId());

		ServerResponse<User> res = iUserService.updateUserInformation(user);

		// 修改Redis
		if (res.isSuccess()) {
			String key = CookieUtil.reqUserCookie(req);
			JedisUtil.setEx(key, JsonUtil.objToJson(user), Const.UserConst.ONLINE_TIME);
		}
		return res;
	}

	/**
	 * 获取当前用户的信息 未登录强制登陆
	 */
	@RequestMapping(value = "get_user_info_login.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> getUserInformation(HttpServletRequest req) {
		User user = new User();
		boolean online = iUserService.isOnline(CookieUtil.reqUserCookie(req), user);
		if (!online) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc()); //status = 10
		}

		return iUserService.getUserInformation(user);
	}

}
