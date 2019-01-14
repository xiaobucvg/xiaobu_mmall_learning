package com.mmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * 全局异常处理器
 *
 * @author zh_job
 * 2019/1/10 16:37
 */
@Component
@Slf4j
public class ExceptionHandler implements HandlerExceptionResolver {
	@Override
	public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
		log.info("出现异常", e);
		MappingJacksonJsonView mView = new MappingJacksonJsonView();
		mView.setPrettyPrint(true);
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setView(mView);
		modelAndView.addObject("status", ResponseCode.ERROR.getCode());
		modelAndView.addObject("msg", "出现了异常，请检查后台日志.");
		modelAndView.addObject("data", e.toString());
		return modelAndView;
	}
}
