package com.mmall.util;

import com.mmall.common.ShardedRedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ShardedJedis;

import java.util.Base64;

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
		value = new String(Base64.getEncoder().encode(value.getBytes()));
		ShardedJedis jedis = ShardedRedisPool.getResource();
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
		ShardedJedis jedis = ShardedRedisPool.getResource();
		String res = null;
		try{
			res = jedis.get(key);
			res = new String(Base64.getDecoder().decode(res.getBytes()));
		}catch(Exception e){
			log.error("获取Key：{}出错",key,e);
		}
		return res;
	}

	/** 设置key的同时设置过期时间 (秒) */
	public static String setEx(String key, String value, Integer seconds){
		value = new String(Base64.getEncoder().encode(value.getBytes()));
		ShardedJedis jedis = ShardedRedisPool.getResource();
		String res = null;
		try{
			res = jedis.setex(key, seconds, value);
		}catch(Exception e){
			log.error("设置Key：{}为{}过期时间{}出错",key,value,seconds,e);
		}
		return res;
	}

	/** 设置一个key的过期时间 */
	public static Long expire(String key, Integer seconds){
		ShardedJedis jedis = ShardedRedisPool.getResource();
		Long res = null;
		try{
			res = jedis.expire(key, seconds);
		}catch(Exception e){
			log.error("设置Key：{}的过期时间为{}出错",key,seconds,e);
		}
		return res;
	}

	/** 删除一个key */
	public static void del(String key){
		ShardedJedis jedis = ShardedRedisPool.getResource();
		jedis.del(key);
	}

	public static Long setnx(String key, String value) {
		ShardedJedis jedis = ShardedRedisPool.getResource();
		Long res = null;
		try{
			res = jedis.setnx(key, value);
		}catch(Exception e){
			log.error("setnx error:设置{}为{}",key,value,e);
		}
		return res;
	}

	public static String getSet(String key, String value) {
		ShardedJedis jedis = ShardedRedisPool.getResource();
		String res = null;
		try{
			res = jedis.getSet(key, value);
		}catch(Exception e){
			log.error("getset error:设置{}为{}",key,value,e);
		}
		return res;
	}
}
