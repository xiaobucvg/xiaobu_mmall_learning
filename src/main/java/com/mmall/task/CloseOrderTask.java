package com.mmall.task;

import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 *
 * 定时关单
 *
 * @author zh_job
 * 2019/1/14 21:04
 */
@Component
@Slf4j
public class CloseOrderTask {
	@Resource
	private IOrderService orderService;


	/** 定时关单v1 */
	@Scheduled(cron = "0 */1 * * * ?") // 每一分钟的整数倍
	public void closeOrderTaskV1(){
		int hour = Integer.parseInt(PropertiesUtil.getValue("close.order.task.hour", "2"));
		orderService.closeOrder(hour);
	}

}
