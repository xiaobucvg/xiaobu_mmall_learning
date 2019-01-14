package com.mmall.controller.admin;

import com.mmall.common.ServerResponse;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 分类管理
 *
 * @author zh_job
 * 2018/12/6 8:58
 **/
@Controller
@RequestMapping("/admin/category/")
public class CategoryManagerController {

	@Resource(name = "iCategoryService")
	private ICategoryService iCategoryService;

	/**
	 * 增加分类
	 */
	@RequestMapping(value = "add_category.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse addCategory(String name, @RequestParam(value = "parentId", defaultValue = "0", required = false) Integer parentId) {
		return iCategoryService.addCategory(name, parentId);
	}

	/**
	 * 修改分类名字
	 */
	@RequestMapping(value = "set_category_name.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse setCategoryName(Integer id, String name) {
		return iCategoryService.setCategoryName(id, name);
	}

	/**
	 * 获取品类子节点(平级)
	 */
	@RequestMapping(value = "get_category_sub_node.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse getParallelSubNode(@RequestParam(value = "id", defaultValue = "0") Integer id) {
		return iCategoryService.getParallelSubNode(id);
	}

	/**
	 * 获取当前分类id及递归子节点categoryId
	 */
	@RequestMapping(value = "get_category_deep_sub_node.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse getCategoryAndDeepSubNode(@RequestParam(value = "id", defaultValue = "0") Integer id) {
		return iCategoryService.getCategoryAndDeepSubNode(id);
	}
}
