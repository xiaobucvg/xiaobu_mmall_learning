package com.mmall.service.impl;

import com.github.pagehelper.StringUtil;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JedisUtil;
import com.mmall.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * 用户接口实现类
 *
 * @author zh_job
 * 2018/12/4 9:16
 **/
@Service("iUserService")
public class UserServiceImpl implements IUserService {
	@Resource
	private UserMapper userMapper;

	@Override
	public ServerResponse<User> login(String username, String password) {
		int resCount = userMapper.checkUsername(username);

		if (resCount == 0) {
			return ServerResponse.createByErrorMessage("用户名不存在.");
		}

		//todo MD5加密密码后再查询

		User user = userMapper.selectLogin(username, password);
		if (user == null) {
			return ServerResponse.createByErrorMessage("密码输入错误.");
		}

		user.setPassword("");
		return ServerResponse.createBySuccess("登录成功", user);
	}

	@Override
	public ServerResponse<String> register(User user) {
		ServerResponse<String> res = checkValid(user.getUsername(), Const.USERNAME);

		if (!res.isSuccess()) {
			return res;
		}

		res = checkValid(user.getEmail(), Const.EMAIL);

		if (!res.isSuccess()) {
			return res;
		}

		user.setRole(Const.Role.ROLE_COSTUMER);

		//todo 对密码进行MD5加密

		if (userMapper.insert(user) == 0) {
			return ServerResponse.createByErrorMessage("注册失败.");
		}

		return ServerResponse.createBySuccessMessage("注册成功.");
	}

	@Override
	public ServerResponse<String> checkValid(String str, String type) {
		int res;
		switch (type) {
			case Const.USERNAME: {
				res = userMapper.checkUsername(str);
				if (res > 0) {
					return ServerResponse.createByErrorMessage("用户名已经存在.");
				}
			}
			break;
			case Const.EMAIL: {
				res = userMapper.checkEmail(str);
				if (res > 0) {
					return ServerResponse.createByErrorMessage("邮箱已经存在.");
				}
			}
			break;
			default: {
				return ServerResponse.createByErrorMessage("校验类型不存在.");
			}
		}
		return ServerResponse.createBySuccessMessage("校验成功.");
	}

	@Override
	public ServerResponse<String> forgetGetQuestion(String username) {
		ServerResponse<String> res = checkValid(username, Const.USERNAME);
		if (res.isSuccess()) {
			return ServerResponse.createByErrorMessage("用户不存在.");
		}
		String question = userMapper.selectQuestionByUsername(username);
		if (question == null && "".equals(question.trim())) {
			return ServerResponse.createByErrorMessage("找回密码的问题是空的.");
		}
		return ServerResponse.createBySuccess(question);
	}

	@Override
	public ServerResponse<String> forgetCheckQuestionAnswer(String username, String question, String answer) {
		int count = userMapper.checkAnswer(username, question, answer);
		if (count > 0) {
			// 生成 Token
			String forgetQuestionToken = UUID.randomUUID().toString();
			TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetQuestionToken);
			return ServerResponse.createBySuccess(forgetQuestionToken);
		}
		return ServerResponse.createByErrorMessage("问题的答案错误.");
	}

	@Override
	public ServerResponse<String> forgerRestPassword(String username, String passwordNew, String forgetToken) {
		if (StringUtils.isEmpty(forgetToken)) {
			return ServerResponse.createByErrorMessage("Token不存在.");
		}
		ServerResponse<String> res = checkValid(username, Const.USERNAME);
		if (res.isSuccess()) {
			return ServerResponse.createByErrorMessage("用户不存在.");
		}
		String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
		if (StringUtils.isEmpty(token)) {
			return ServerResponse.createByErrorMessage("Token无效或者过期.");
		}

		if (forgetToken.equals(token)) {

			//todo MD5加密密码

			int count = userMapper.updatePasswordByUsername(username, passwordNew);

			if (count > 0) {
				return ServerResponse.createBySuccess("修改密码成功.");
			}
		} else {
			return ServerResponse.createByErrorMessage("Token错误，请重新获取.");
		}
		return ServerResponse.createByErrorMessage("修改密码失败.");
	}

	@Override
	public ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user) {
		//防止横向越权
		int res = userMapper.checkPassword(oldPassword, user.getId());
		if (res <= 0) {
			return ServerResponse.createByErrorMessage("旧密码错误.");
		}

		//todo 密码MD5加密
		user.setPassword(newPassword);

		int updateRes = userMapper.updateByPrimaryKeySelective(user);
		if (updateRes > 0) {
			return ServerResponse.createBySuccess("密码修改成功.");
		}
		return ServerResponse.createByErrorMessage("密码修改失败.");
	}

	@Override
	public ServerResponse<User> updateUserInformation(User user) {
		//检查需要邮箱是否已经被别人占用
		int res = userMapper.checkEmailByUserId(user.getEmail(), user.getId());

		if (res > 0) {
			return ServerResponse.createByErrorMessage("邮箱已经被注册,重新修改邮箱.");
		}

		User upUser = new User();
		upUser.setId(user.getId());
		upUser.setEmail(user.getEmail());
		upUser.setPhone(user.getPhone());
		upUser.setQuestion(user.getQuestion());
		upUser.setAnswer(user.getAnswer());

		res = userMapper.updateByPrimaryKeySelective(user);
		if (res > 0) {
			return ServerResponse.createBySuccess("更新个人信息成功.", upUser);
		}

		return ServerResponse.createByErrorMessage("更新个人信息失败.");
	}

	@Override
	public ServerResponse<User> getUserInformation(User user) {
		User res = userMapper.selectByPrimaryKey(user.getId());
		if (res == null) {
			return ServerResponse.createByErrorMessage("用户不存在.");
		}
		res.setPassword("");
		return ServerResponse.createBySuccess("查询成功.", res);
	}

	//Admin

	/**
	 * 检查用户是不是管理员
	 */
	@Override
	public ServerResponse<String> checkAdmin(User user) {
		User res = userMapper.selectByPrimaryKey(user.getId());
		if (res != null && res.getRole() == Const.Role.ROLE_ADMIN) {
			return ServerResponse.createBySuccess();
		}
		return ServerResponse.createByError();
	}

	/**
	 * 判断当前登陆的用户的权限（是不是管理员）并让其前台登陆
	 */
	public ServerResponse checkCurrentUserAuth(User user) {
		ServerResponse<String> res = checkAdmin(user);
		if (res.isSuccess()) {
			return res;
		}
		return ServerResponse.createByErrorMessage("您没有权限.");
	}

	// 二期

	/**
	 * 判断用户是否在线
	 */
	public boolean isOnline(String userCookie) {
		String jsonRes = JedisUtil.get(userCookie);
		if (StringUtils.isNotEmpty(jsonRes)) {
			User user = JsonUtil.jsonToObj(jsonRes, User.class);
			return user == null;
		}
		return false;
	}
	/**
	 * 判断用户是否在线
	 * 把用户赋值给参数
	 */
	public boolean isOnline(String userCookie,User user){
		String jsonRes = JedisUtil.get(userCookie);
		if (StringUtils.isNotEmpty(jsonRes)) {
			User userRes = JsonUtil.jsonToObj(jsonRes, User.class);
			if(userRes != null){
				user.setUsername(userRes.getUsername());
				user.setPhone(userRes.getPhone());
				user.setPassword(userRes.getPassword());
				user.setId(userRes.getId());
				user.setQuestion(userRes.getQuestion());
				user.setAnswer(userRes.getAnswer());
				user.setEmail(userRes.getEmail());
				user.setRole(userRes.getRole());
				user.setCreateTime(userRes.getCreateTime());
				user.setUpdateTime(userRes.getUpdateTime());
				return true;
			}
		}
		return false;
	}

}
