package com.mmall.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  时间转换的工具
 * @author zh_job
 *  2018/12/9 13:13
 **/
public class DateUtil {
	private static Logger logger = LoggerFactory.getLogger(DateUtil.class);

	private static DateFormat dateFormat;

	public static Date stringToDate(String time){
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return dateFormat.parse(time);
		} catch (ParseException e) {
			logger.error("日期无法被转换 - " + time,e);
		}
		return null;
	}

	public static String dateToString(Date date,String format){
		if(date == null){
			return "";
		}
		dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	public static String dateToString(Date date){
		if(date == null){
			return "";
		}
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}
}
