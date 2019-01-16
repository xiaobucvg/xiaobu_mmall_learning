package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.JedisUtil;
import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
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
	@Resource
	private RedissonManager redissonManager;


	/**
	 * 定时关单v1
	 */
	//@Scheduled(cron = "0 */1 * * * ?") // 每一分钟的整数倍
	public void closeOrderTaskV1() {
		int hour = Integer.parseInt(PropertiesUtil.getValue("close.order.task.hour", "2"));
		orderService.closeOrder(hour);
	}

	/**
	 * 定时关单v2
	 */
	//@Scheduled(cron = "0 */1 * * * ?") // 每一分钟的整数倍
	public void closeOrderTaskV2() {
		Long timeout = Long.valueOf(PropertiesUtil.getValue("redis.close.order.clock", "5000"));
		Long res = JedisUtil.setnx(Const.REDIS_CLOCK.REDIS_CLOSE_ORDER_CLOCK, String.valueOf(System.currentTimeMillis() + timeout));
		// 成功设置了锁
		if (res != null && res == 1) {
			closeOrder(Const.REDIS_CLOCK.REDIS_CLOSE_ORDER_CLOCK);
			log.info("成功获取了分布式锁");
		}
		// 没有成功设置锁
		else {
			String value = JedisUtil.get(Const.REDIS_CLOCK.REDIS_CLOSE_ORDER_CLOCK);
			// 锁已经超时
			if (value != null && System.currentTimeMillis() > Long.valueOf(value)) {
				String getSetRes = JedisUtil.getSet(Const.REDIS_CLOCK.REDIS_CLOSE_ORDER_CLOCK, String.valueOf(System.currentTimeMillis() + timeout));
				if (getSetRes == null || StringUtils.equals(getSetRes, value)) {
					closeOrder(Const.REDIS_CLOCK.REDIS_CLOSE_ORDER_CLOCK);
					log.info("成功获取了分布式锁");
				}
			}
			log.info("未获取分布式锁");
		}
	}

	public void closeOrder(String closeKey) {
		JedisUtil.expire(closeKey, 50); // 50秒
		int hour = Integer.parseInt(PropertiesUtil.getValue("close.order.task.hour", "2"));
		orderService.closeOrder(hour);
		JedisUtil.del(closeKey);
	}

	/**
	 * 定时关单v3
	 *
	 * lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
	 * waitTime 是等待时间，如果等待时间大于业务的执行时间，可能会出现几个tomcat实例都获取到锁的问题
	 * 建议waitTime设置为0
	 */
	@Scheduled(cron = "0 */1 * * * ?") // 每一分钟的整数倍
	public void closeOrderTaskV3() {
		RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_CLOCK.REDIS_CLOSE_ORDER_CLOCK);
		boolean getLock = false;
		try {
			getLock = lock.tryLock(5, 50, TimeUnit.SECONDS);
			if(getLock){
				int hour = Integer.parseInt(PropertiesUtil.getValue("close.order.task.hour", "2"));
				orderService.closeOrder(hour);
			} else {
				log.info("redisson:获取锁{}失败",Const.REDIS_CLOCK.REDIS_CLOSE_ORDER_CLOCK);
			}
		} catch (InterruptedException e) {
			log.info("redisson:获取锁{}失败",Const.REDIS_CLOCK.REDIS_CLOSE_ORDER_CLOCK,e);
		} finally {
			if(!getLock){
				return;
			}
			lock.unlock();
			log.info("redisson:释放了锁");
		}
	}
}
