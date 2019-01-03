package com.mmall.util;

import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 *
 * JedisAPI
 *
 * @author zh_job
 * 2018/12/31 14:46
 */
@Slf4j
public class JedisUtil {

	/** 设置key - value */
	public static String set(String key, String value){
		Jedis jedis = RedisPool.getResource();
		String res = null;
		try{
			res = jedis.set(key, value);
		}catch(Exception e){
			log.error("设置Key：{}为：{}出错",key,value,e);
		}
		return res;
	}

	/** 获取key值 */
	public static String get(String key){
		Jedis jedis = RedisPool.getResource();
		String res = null;
		try{
			res = jedis.get(key);
		}catch(Exception e){
			log.error("获取Key：{}出错",key,e);
		}
		return res;
	}

	/** 设置key的同时设置过期时间 (秒) */
	public static String setEx(String key, String value, Integer seconds){
		Jedis jedis = RedisPool.getResource();
		String res = null;
		try{
			res = jedis.setex(key, seconds, value);
		}catch(Exception e){
			log.error("获取Key：{}出错",key,e);
		}
		return res;
	}

	/** 设置一个key的过期时间 */
	public static Long expire(String key, Integer seconds){
		Jedis jedis = RedisPool.getResource();
		Long res = null;
		try{
			res = jedis.expire(key, seconds);
		}catch(Exception e){
			log.error("获取Key：{}出错",key,e);
		}
		return res;
	}

	/** 删除一个key */
	public static void del(String key){
		Jedis jedis = RedisPool.getResource();
		jedis.del(key);
	}
}
