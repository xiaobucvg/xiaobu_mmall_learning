package com.mmall.controller.admin;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 分类管理
 *
 * @author zh_job
 * 2018/12/6 8:58
 **/
@Controller
@RequestMapping("/admin/category/")
public class CategoryManagerController {

	@Resource
	private IUserService iUserService;

	@Resource(name = "iCategoryService")
	private ICategoryService iCategoryService;

	/**
	 * 增加分类
	 */
	@RequestMapping(value = "add_category.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse addCategory(HttpSession session, String name, @RequestParam(value = "parentId", defaultValue = "0", required = false) Integer parentId) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorMessage("需要登陆.");
		}
		ServerResponse res = iUserService.checkCurrentUserAuth(user);
		if (!res.isSuccess()) {
			return res;
		}
		return iCategoryService.addCategory(name, parentId);
	}

	/**
	 * 修改分类名字
	 */
	@RequestMapping(value = "set_category_name.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse setCategoryName(HttpSession session, Integer id, String name) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorMessage("需要登陆.");
		}
		ServerResponse res = iUserService.checkCurrentUserAuth(user);
		if (!res.isSuccess()) {
			return res;
		}
		return iCategoryService.setCategoryName(id, name);
	}

	/**
	 * 获取品类子节点(平级)
	 */
	@RequestMapping(value = "get_category_sub_node.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse getParallelSubNode(HttpSession session, @RequestParam(value = "id", defaultValue = "0") Integer id) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorMessage("需要登陆.");
		}
		ServerResponse res = iUserService.checkCurrentUserAuth(user);
		if (!res.isSuccess()) {
			return res;
		}
		return iCategoryService.getParallelSubNode(id);
	}

	/**
	 * 获取当前分类id及递归子节点categoryId
	 */
	@RequestMapping(value = "get_category_deep_sub_node.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse getCategoryAndDeepSubNode(HttpSession session, @RequestParam(value = "id", defaultValue = "0") Integer id) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorMessage("需要登陆.");
		}
		ServerResponse res = iUserService.checkCurrentUserAuth(user);
		if (!res.isSuccess()) {
			return res;
		}
		return iCategoryService.getCategoryAndDeepSubNode(id);
	}
}
