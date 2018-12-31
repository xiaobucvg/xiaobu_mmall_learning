package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.List;

/** 分类接口 */
public interface ICategoryService {
	ServerResponse<String> addCategory(String name, Integer parentId);
	ServerResponse<String> setCategoryName(Integer id,String name);
	ServerResponse getParallelSubNode(Integer id);
	ServerResponse<List<Integer>> getCategoryAndDeepSubNode(Integer id);
}
