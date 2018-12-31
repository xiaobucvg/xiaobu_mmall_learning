package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 分类Service
 *
 * @author zh_job
 * 2018/12/6 9:33
 **/
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
	private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

	@Resource
	private CategoryMapper categoryMapper;

	public ServerResponse<String> addCategory(String name, Integer parentId) {
		if (null == name || "".equals(name)) {
			return ServerResponse.createByErrorMessage("分类名不合理.");
		}

		//todo 或许要检查一下分类名是否会重复

		Category category = new Category();
		category.setName(name);
		category.setParentId(parentId);
		category.setStatus(true);
		int res = categoryMapper.insert(category);
		if (res > 0) {
			return ServerResponse.createBySuccessMessage("添加分类成功.");
		}
		return ServerResponse.createByErrorMessage("添加分类失败.");
	}

	public ServerResponse<String> setCategoryName(Integer id, String name) {
		if (null == name || "".equals(name)) {
			return ServerResponse.createByErrorMessage("分类名不合理.");
		}
		Category category = new Category();
		category.setName(name);
		category.setId(id);
		int res = categoryMapper.updateByPrimaryKeySelective(category);
		if (res > 0) {
			return ServerResponse.createBySuccessMessage("更新分类名成功.");
		}
		return ServerResponse.createByErrorMessage("更新分类名失败.");
	}

	public ServerResponse getParallelSubNode(Integer id) {
		if (id == null || "".equals(id.toString().trim())) {
			logger.error("没有分类.");
			return ServerResponse.createByErrorMessage("没有该分类.");
		}
		List<Category> subNodes = categoryMapper.selectCategoryChildrenByParentId(id);
		if (subNodes.size() == 0) {
			logger.error("没有找到子节点.");
			return ServerResponse.createByErrorMessage("该节点下没有子节点.");
		}
		return ServerResponse.createBySuccess(subNodes);
	}

	public ServerResponse<List<Integer>> getCategoryAndDeepSubNode(Integer id) {
		Set<Category> categories = Sets.newHashSet();
		Set<Category> subNodes = getSubNode(categories, id);
		List<Integer> lists = Lists.newArrayList();
		for (Category categoryItem : subNodes) {
			lists.add(categoryItem.getId());
		}
		return ServerResponse.createBySuccess(lists);
	}

	private Set<Category> getSubNode(Set<Category> categorySet, Integer id) {
		Category category = categoryMapper.selectByPrimaryKey(id);
		if (category != null) {
			categorySet.add(category);
		}
		// 递归查找子节点
		List<Category> categories = categoryMapper.selectCategoryChildrenByParentId(id);
		for (Category categoryItem : categories) {
			getSubNode(categorySet, categoryItem.getId());
		}
		return categorySet;
	}
}
