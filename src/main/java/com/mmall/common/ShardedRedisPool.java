package com.mmall.common;

import com.google.common.collect.Lists;
import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;

import java.util.List;

/**
 *
 * 分布式redis缓存池
 *
 * @author zh_job
 * 2019/1/3 18:10
 */
public class ShardedRedisPool {
	private static ShardedJedisPool jedisPool;
	private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getValue("redis.max.total", "20"));// 最大连接数
	private static Integer maxIdel = Integer.parseInt(PropertiesUtil.getValue("redis.max.idel", "10")); // 连接池实例最大空闲时间
	private static Integer minIdel = Integer.parseInt(PropertiesUtil.getValue("redis.min.idel", "10")); // 连接池实例最小空闲事件
	private static boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getValue("redis.test.borrow", "true")); // 得到实例的时候是否进行验证 如果开启了验证 则获取的全部都是可用的
	private static boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getValue("redis.test.return", "false")); // 释放实例的时候是否进行验证 如果开启了验证 则获取的全部都是可用的

	static {
		init();
	}

	private static void init() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxIdle(maxIdel);
		config.setMinIdle(minIdel);
		config.setMaxTotal(maxTotal);
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnReturn(testOnReturn);

		// todo 这样处理不好 可以配置文件写成ip地址用逗号分隔 获取后遍历
		JedisShardInfo info = new JedisShardInfo(PropertiesUtil.getValue("redis.ip.2"), Integer.parseInt(PropertiesUtil.getValue("redis.port.2")));
		JedisShardInfo info2 = new JedisShardInfo(PropertiesUtil.getValue("redis.ip"), Integer.parseInt(PropertiesUtil.getValue("redis.port")));
		List<JedisShardInfo> list = Lists.newArrayList();
		list.add(info);
		list.add(info2);

		jedisPool = new ShardedJedisPool(config, list);
	}


	/**
	 * 获取资源
	 */
	public static ShardedJedis getResource() {
		return jedisPool.getResource();
	}

	/**
	 * 释放资源
	 */
	public static void freeResource(ShardedJedis jedis) {
		// 源码内部已经判断是否是坏资源
		jedis.close();
	}


}
