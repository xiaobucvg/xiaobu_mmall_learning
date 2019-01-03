package com.mmall.common;

import com.mmall.util.JedisUtil;
import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * redis连接池
 *
 * @author zh_job
 * 2018/12/31 13:21
 */
public class RedisPool {
	private static JedisPool jedisPool;
	private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getValue("redis.max.total", "20"));// 最大连接数
	private static Integer maxIdel = Integer.parseInt(PropertiesUtil.getValue("redis.max.idel", "10")); // 连接池实例最大空闲时间
	private static Integer minIdel = Integer.parseInt(PropertiesUtil.getValue("redis.min.idel", "10")); // 连接池实例最小空闲事件
	private static boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getValue("redis.test.borrow", "true")); // 得到实例的时候是否进行验证 如果开启了验证 则获取的全部都是可用的
	private static boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getValue("redis.test.return", "false")); // 释放实例的时候是否进行验证 如果开启了验证 则获取的全部都是可用的

	static {
		init();
	}

	private static void init(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxIdle(maxIdel);
		config.setMinIdle(minIdel);
		config.setMaxTotal(maxTotal);
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnReturn(testOnReturn);
		jedisPool = new JedisPool(config,PropertiesUtil.getValue("redis.ip") , Integer.parseInt(PropertiesUtil.getValue("redis.port"))) ;
	}


	/** 获取资源 */
	public static Jedis getResource(){
		return jedisPool.getResource();
	}

	/** 释放资源 */
	public static void freeResource(Jedis jedis){
		// 源码内部已经判断是否是坏资源
		jedis.close();
	}

}
