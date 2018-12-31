package com.mmall.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *  配置文件工具类
 * @author zh_job
 *  2018/12/7 11:01
 **/
public class PropertiesUtil {
	private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
	private static Properties prop;
	// 默认加载网站配置文件
	static {
		setProperties("mmall.properties");
	}
	/**  获取配置文件的值 */
	public static String getValue(String key){
		String value = prop.getProperty(key == null ? "" : key.trim());
		if(value == null){
			logger.error("未获取到配置的值.");
			return null;
		}
		return value.trim();
	}
	/**  获取配置文件的值 如果获取不到 可以自己传一个默认值保底不会出错 */
	public static String getValue(String key,String defaultValue){
		String value = prop.getProperty(key == null ? "" : key.trim());
		if(value == null){
			logger.warn("使用了默认值",defaultValue);
			return defaultValue;
		}
		return value.trim();
	}

	/** 重新设置配置文件 */
	public static void setProperties(String filePath){
		prop = new Properties();
		InputStream resourceAsStream = PropertiesUtil.class.getClassLoader().getResourceAsStream(filePath);
		try {
			prop.load(resourceAsStream);
		} catch (IOException e) {
			logger.error("读取配置文件出错.",e);
		}
	}
}
