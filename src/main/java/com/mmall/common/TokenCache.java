package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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

	//LRU算法
	private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(30, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
		//默认的数据加载实现，当get取值 ，如果Key没有对应值 ，就调用这个方法加载
		@Override
		public String load(String s) {
			return "null";
		}
	});

	public static void setKey(String key, String value) {
		localCache.put(key, value);
	}

	public static String getKey(String key) {
		String value;
		try {
			value = localCache.get(key);
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
