package com.mmall.controller.common;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JedisUtil;
import com.mmall.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * 权限认证拦截器
 *
 * @author zh_job
 * 2019/1/10 21:29
 */
public class AuthInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
		Cookie[] cookies = httpServletRequest.getCookies();
		User user;
		PrintWriter out;
		for(Cookie cookie : cookies){
			// 获取redis缓存的桥梁cookie
			if(StringUtils.equals(cookie.getName(), CookieUtil.USER_LOGIN_INFO)){
				//从redis获取json并序列化成User对象
				if((user=JsonUtil.jsonToObj(JedisUtil.get(cookie.getValue()), User.class))!= null){
					if(Const.Role.ROLE_ADMIN != user.getRole()) {
						httpServletResponse.reset();
						httpServletResponse.setContentType("application/json;charset=utf-8");
						httpServletRequest.setCharacterEncoding("utf-8");
						out = httpServletResponse.getWriter();
						out.write(JsonUtil.objToJson(ServerResponse.createByErrorMessage("拦截器：用户不是管理员.")));
						out.close();
						return false;
					}
					return true;
				}
				break;
			}
		}
		httpServletResponse.reset();
		httpServletResponse.setContentType("application/json;charset=utf-8");
		httpServletRequest.setCharacterEncoding("utf-8");
		out = httpServletResponse.getWriter();
		out.write(JsonUtil.objToJson(ServerResponse.createByErrorMessage("拦截器：用户未登录.")));
		out.close();
		return false;
	}

	@Override
	public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

	}
}
