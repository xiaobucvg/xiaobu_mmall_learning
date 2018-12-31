package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 *  用户控制
 * @author zh_job
 *  2018/12/4 8:12
 **/
@Controller
@RequestMapping("/user/")
public class UserController {
	@Resource(name = "iUserService")
	private IUserService iUserService;

	/**  登陆接口 */
	@RequestMapping(value = "login.do",method = RequestMethod.POST)
	@ResponseBody //序列化成Json
	public ServerResponse<User> login(String username, String password, HttpSession session){
		ServerResponse<User> loginResponse = iUserService.login(username, password);
		if(loginResponse.isSuccess()){
			session.setAttribute(Const.CURRENT_USER, loginResponse.getData());
		}
		return loginResponse;
	}
	
	/**  登出接口 */
	@RequestMapping(value = "logout.do",method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse<String> logout(HttpSession session){
		session.removeAttribute(Const.CURRENT_USER);
		return ServerResponse.createBySuccess();
	}
	
	/**  注册接口 */
	@RequestMapping(value = "register.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> register(User user){
		return iUserService.register(user);
	}

	/**  验证有效性（用户名 或者 邮箱） */
	@RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> checkValid(String str,String type){
		return iUserService.checkValid(str, type);
	}

	/**  获取用户信息 */
	@RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> getUserInfo(HttpSession session){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		return ServerResponse.createBySuccess(user);
	}

	/**  获取找回密码的问题 */
	@RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetGetQuestion(String username){
		return iUserService.forgetGetQuestion(username);
	}

	/**  验证找回密码的问题 */
	@RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetCheckQuestionAnswer(String username,String question,String answer){
		return iUserService.forgetCheckQuestionAnswer(username,question, answer);
	}

	/**  忘记密码的重置密码 */
	@RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgerRestPassword(String username,String passwordNew,String forgetToken){
		return iUserService.forgerRestPassword(username, passwordNew, forgetToken);
	}

	/**  登录状态重置密码 */
	@RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> restPassword(String oldPassword,String newPassword,HttpSession session){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null){
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		return iUserService.resetPassword(oldPassword,newPassword,user);
	}

	/**  登录状态修改个人信息 */
	@RequestMapping(value = "update_user_info.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> updateUserInformation(User user,HttpSession session){
		User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
		if(currentUser == null){
			return ServerResponse.createByErrorMessage("用户未登录.");
		}

		//防止横向越权 只能修改当前登陆的用户信息
		user.setId(currentUser.getId());

		ServerResponse<User> res = iUserService.updateUserInformation(user);

		// 修改Session
		if(res.isSuccess()){
			res.getData().setUsername(user.getUsername());
			session.setAttribute(Const.CURRENT_USER, res.getData());
		}
		return res;
	}

	/**  获取当前用户的信息 未登录强制登陆 */
	@RequestMapping(value = "get_user_info_login.do",method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> getUserInformation(HttpSession session){
		User user = (User) session.getAttribute(Const.CURRENT_USER);

		if(user == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc()); //status = 10
		}

		return iUserService.getUserInformation(user);
	}

}
