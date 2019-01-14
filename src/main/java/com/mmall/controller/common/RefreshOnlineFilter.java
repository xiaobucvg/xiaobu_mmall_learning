package com.mmall.controller.common;

import com.github.pagehelper.StringUtil;
import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JedisUtil;
import com.mmall.util.JsonUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 刷新保持在线时长过滤器
 * 每次.do的请求都会将redis的token刷新，否则每隔30秒就要重新登陆
 *
 * @author zh_job
 * 2019/1/3 9:41
 */
public class RefreshOnlineFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		String key = CookieUtil.reqUserCookie((HttpServletRequest) req);
		if (StringUtil.isNotEmpty(key)) {
			String jsonRes = JedisUtil.get(key);
			if(jsonRes != null){
				User user = JsonUtil.jsonToObj(jsonRes, User.class);
				if(user != null){
					JedisUtil.expire(key, Const.UserConst.ONLINE_TIME);
				}
			}
		}
		chain.doFilter(req, resp);
	}

	@Override
	public void destroy() {

	}
}
