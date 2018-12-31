package com.mmall.util;

import java.math.BigDecimal;

/**
 *
 * 封装的计算价格的方法 不会丢失精度
 * 使用BigDecimal计算，但是要注意一定要使用String的构造器
 *
 * @author zh_job
 * 2018/12/11 9:11
 */
public class BigDecimalUtil {
	public static BigDecimal add(double one, double two){
		BigDecimal oneStr = new BigDecimal(Double.toString(one));
		BigDecimal twoStr = new BigDecimal(Double.toString(two));
		return oneStr.add(twoStr);
	}
	public static BigDecimal sub(double one, double two){
		BigDecimal oneStr = new BigDecimal(Double.toString(one));
		BigDecimal twoStr = new BigDecimal(Double.toString(two));
		return 	oneStr.subtract(twoStr);
	}
	public static BigDecimal multi(double one, double two){
		BigDecimal oneStr = new BigDecimal(Double.toString(one));
		BigDecimal twoStr = new BigDecimal(Double.toString(two));
		return 	oneStr.multiply(twoStr);
	}
	public static BigDecimal div(double one, double two){
		BigDecimal oneStr = new BigDecimal(Double.toString(one));
		BigDecimal twoStr = new BigDecimal(Double.toString(two));
		// 四舍五入
		return 	oneStr.divide(twoStr, BigDecimal.ROUND_HALF_DOWN);
	}
}
