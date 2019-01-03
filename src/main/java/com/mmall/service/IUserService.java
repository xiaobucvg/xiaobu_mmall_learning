package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

import javax.servlet.http.HttpSession;

public interface IUserService {
	ServerResponse<User> login(String username, String password);
	ServerResponse<String> register(User user);
	ServerResponse<String> checkValid(String str,String type);
	ServerResponse<String> forgetGetQuestion(String username);
	ServerResponse<String> forgetCheckQuestionAnswer(String username,String question,String answer);
	ServerResponse<String> forgerRestPassword(String username,String passwordNew,String forgetToken);
	ServerResponse<String> resetPassword(String oldPassword,String newPassword,User user);
	ServerResponse<User> updateUserInformation(User user);
	ServerResponse<User> getUserInformation(User user);
	ServerResponse<String> checkAdmin(User user);
	ServerResponse checkCurrentUserAuth(User user);
	boolean isOnline(String userCookie,User user);
	boolean isOnline(String userCookie) ;
}
