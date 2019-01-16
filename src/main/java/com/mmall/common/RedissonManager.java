package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author zh_job
 * 2019/1/16 14:38
 */
@Component
@Slf4j
public class RedissonManager {
	private Redisson redisson;
	private Config config;

	@PostConstruct
	private void init(){
		try{
			config = new Config();
			config.useSingleServer().setAddress(PropertiesUtil.getValue("redis.ip") +":"+ PropertiesUtil.getValue("redis.port"));
			redisson = (Redisson) Redisson.create(config);
			log.info("Redisson 初始化完毕." );
		}catch (Exception e){
			log.error("Redisson 初始化失败", e);
		}
	}

	public Redisson getRedisson(){
		return this.redisson;
	}
}
