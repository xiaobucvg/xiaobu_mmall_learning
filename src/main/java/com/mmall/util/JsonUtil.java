package com.mmall.util;

import com.google.common.collect.Lists;
import com.mmall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 对象序列化成Json和Json反序列化成对象
 *
 * @author zh_job
 * 2018/12/31 15:12
 */
@Slf4j
public class JsonUtil {
	private static ObjectMapper objectMapper = new ObjectMapper();

	static {
		// 对象的所有字段全部列入
		objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);
		// 取消默认转换timestamp形式
		objectMapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
		// 忽略空Bean转换报错
		objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		// 统一设置时间格式
		objectMapper.setDateFormat(new SimpleDateFormat(DateUtil.STANDARD_TIME_FORMAT));
		// 忽略Json中存在但是在对象中没有对应属性的错误
		objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}


	/**
	 * 序列化成Json字符串，未格式化，可以传入List
	 */
	public static <T> String objToJson(T obj) {
		if (obj == null) {
			return null;
		}
		try {
			return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
		} catch (IOException e) {
			log.warn("转换成Json失败.");
		}
		return null;
	}

	/**
	 * 序列化成Json字符串，已格式化，可以传入List
	 */
	public static <T> String objToFormatJson(T obj) {
		if (obj == null) {
			return null;
		}
		try {
			return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (IOException e) {
			log.warn("转换成FormatJson失败.");
		}
		return null;
	}

	/**
	 * 反序列化
	 */
	public static <T> T jsonToObj(String json, Class<T> clazz) {
		if (json == null) return null;
		if (clazz.equals(String.class)) return (T) json;
		try {
			return objectMapper.reader(clazz).readValue(json);
		} catch (IOException e) {
			log.warn("转换成对象失败.");
		}
		return null;
	}

	/**
	 * 反序列化，可以传入json数组，返回List<T>
	 */
	public static <T> T jsonToObj(String json, TypeReference<T> typeReference) {
		if (json.isEmpty() || typeReference == null) return null;
		Type type = typeReference.getType();
		try {
			return type.equals(String.class) ? (T) json : objectMapper.reader(typeReference).readValue(json);
		} catch (IOException e) {
			log.warn("转换成对象失败.");
		}
		return null;
	}

	/**
	 * 反序列化，可以传入json数组，返回List<T>
	 * @param collectionClass 集合类型
	 * @param elementsClasses 可变长参数类型
	 */
	public static <T> T jsonToObj(String json, Class<T> collectionClass, Class<?>... elementsClasses) {
		if (json.isEmpty() || collectionClass == null) return null;
		JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementsClasses);
		try {
			return objectMapper.reader(javaType).readValue(json);
		} catch (IOException e) {
			log.warn("转换成对象失败.");
		}
		return null;
	}

}
