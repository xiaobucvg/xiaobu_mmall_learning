package com.mmall.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 操作用户cookie
 *
 * @author zh_job
 * 2019/1/2 10:26
 */
public class CookieUtil {
	public static final String USE_DOMAIN = ".mmall.com";
	public static final String USER_LOGIN_INFO = "USER_LOGIN_COOKIE";

	/**
	 * 写入用户cookie
	 */
	public static void resUserCookie(HttpServletResponse response, String jsonId) {
		Cookie cookie = new Cookie(USER_LOGIN_INFO, jsonId);
		cookie.setMaxAge(60 * 60 * 24 * 30); // 一个月
		cookie.setHttpOnly(true);
		// todo cookie作用域先不设置
		//cookie.setDomain(USE_DOMAIN);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	/**
	 * 读取请求的用户信息cookie
	 */
	public static String reqUserCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null || cookies.length == 0) return null;
		for (Cookie cookie : cookies) {
			if (StringUtils.equals(cookie.getName(), USER_LOGIN_INFO)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	/**
	 * 删除用户信息cookie
	 */
	public static void removeUserCookie(HttpServletRequest req, HttpServletResponse resp) {
		Cookie[] cookies = req.getCookies();
		if (cookies.length == 0) return;
		for (Cookie cookie : cookies) {
			if (StringUtils.equals(cookie.getName(), USER_LOGIN_INFO)) {
				cookie.setMaxAge(-1); // 设置为0代表失效
				resp.addCookie(cookie);
			}
		}
	}

	/**
	 * 获取JSESSIONID的值 未获取到返回空字符串
	 */
	public static String getJessionIdValue(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if(cookies != null && cookies.length != 0){
			for (Cookie cookie : cookies) {
				if ("JSESSIONID".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return UUID.randomUUID().toString();
	}
}
