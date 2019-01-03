package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mmall.util.JedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存
 *
 * @author zh_job
 * 2018/12/5 9:16
 **/
public class TokenCache {
	private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

	public static final String TOKEN_PREFIX = "token_";

	public static void setKey(String key, String value) {
		JedisUtil.setEx(key, value, Const.UserConst.FORGET_TOKEN);
	}

	public static String getKey(String key) {
		String value;
		try {
			value = JedisUtil.get(key);
			if ("null".equals(value)) {
				return null;
			}
			return value;
		} catch (Exception e) {
			logger.error("localCache get error", e);
		}
		return null;
	}

}
