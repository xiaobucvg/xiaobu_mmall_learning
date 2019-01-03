package com.mmall.controller.admin;

import com.github.pagehelper.StringUtil;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JedisUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.PropertiesUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * 产品后台
 *
 * @author zh_job
 * 2018/12/7 9:01
 **/
@Controller
@RequestMapping("/admin/product/")
public class ProductManagerController {

	@Resource
	private IUserService iUserService;

	@Resource
	private IProductService iProductService;

	@Resource
	private IFileService iFileService;

	@Resource
	private IUserService userService;

	/**
	 * 新增OR更新产品
	 */
	//todo 产品不能为NULL的字段必须添加 不然会报出Sql异常 正在想怎么修改
	@RequestMapping("save.do")
	@ResponseBody
	public ServerResponse productSaveOrUpdate(HttpServletRequest req, Product product) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		ServerResponse res = iUserService.checkCurrentUserAuth(user);
		if (!res.isSuccess()) {
			return res;
		}
		return iProductService.productSaveOrUpdate(product);
	}

	/**
	 * 更新产品销售状态（产品上下架）
	 */
	@RequestMapping("set_sale_status.do")
	@ResponseBody
	public ServerResponse setSaleStatus(HttpServletRequest req, @RequestParam("id") Integer id, @RequestParam(value = "status") Integer status) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		ServerResponse res = iUserService.checkCurrentUserAuth(user);
		if (!res.isSuccess()) {
			return res;
		}
		return iProductService.setSaleStatus(id, status);
	}

	/**
	 * 获取产品详情
	 */
	@RequestMapping("get_product_detail.do")
	@ResponseBody
	public ServerResponse getProductDetail(HttpServletRequest req, @RequestParam("id") Integer id) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		ServerResponse res = iUserService.checkCurrentUserAuth(user);
		if (!res.isSuccess()) {
			return res;
		}
		return iProductService.getProductDetail(id);
	}

	/**
	 * 获取产品list
	 */
	@RequestMapping("get_product_list.do")
	@ResponseBody
	public ServerResponse getProductList(HttpServletRequest req, @RequestParam(value = "pageNumber", defaultValue = "1") Integer pageNumber, @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		ServerResponse res = iUserService.checkCurrentUserAuth(user);
		if (!res.isSuccess()) {
			return res;
		}
		return iProductService.getProductList(pageNumber, pageSize);
	}

	/**
	 * 模糊搜索
	 */
	@RequestMapping("search_product.do")
	@ResponseBody
	public ServerResponse searchProduct(HttpServletRequest req, String productName, Integer id, @RequestParam(value = "pageNumber", defaultValue = "1") Integer pageNumber, @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req));
		if(!online){
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		return iProductService.searchProduct(productName, id, pageNumber, pageSize);
	}

	/**
	 * 上传文件
	 */
	@RequestMapping("upload.do")
	@ResponseBody
	public ServerResponse upload(HttpServletRequest req, HttpServletRequest request, @RequestParam(value = "file") MultipartFile file) {
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online){
			return ServerResponse.createByErrorMessage("用户未登录.");
		}
		ServerResponse res = iUserService.checkCurrentUserAuth(user);
		if (!res.isSuccess()) {
			return res;
		}
		String path = request.getSession().getServletContext().getRealPath("upload");
		String targetFileName = iFileService.uploadFile(file, path);
		if (targetFileName != null) {
			ConcurrentMap<String, String> map = Maps.newConcurrentMap();
			map.put("uri", targetFileName);
			map.put("url", PropertiesUtil.getValue("ftp.server.http.prefix","http://img.xiaobu.com/") + targetFileName);
			return ServerResponse.createBySuccess(map);
		}
		return ServerResponse.createByErrorMessage("上传的文件为空或者不支持.");
	}

	/**
	 * 富文本上传
	 * 由于使用simditor富文本插件，所以对返回值有要求 而且只针对此插件
	 * {
	 * "success": true/false,
	 * "msg": "error message", # optional
	 * "file_path": "[real file path]"
	 * }
	 * 成功返回后需要设置响应头 "Access-Control-Allow-Headers","X-File-Name"
	 */
	@RequestMapping("rich_text_upload.do")
	@ResponseBody
	public Map uploadRichText(HttpServletRequest req, HttpServletRequest request, @RequestParam(value = "file", required = false) MultipartFile file, HttpServletResponse response) {
		Map<String, String> map = Maps.newConcurrentMap();
		User user = new User();
		boolean online = userService.isOnline(CookieUtil.reqUserCookie(req),user);
		if(!online || !iUserService.checkCurrentUserAuth(user).isSuccess()){
			map.put("success", "false");
			map.put("msg", "需要登陆管理员账户才能上传.");
		}
		String path = request.getSession().getServletContext().getRealPath("upload");
		String targetFileName = iFileService.uploadFile(file, path);
		if (targetFileName != null) {
			map.put("success", "true");
			map.put("msg", "上传文件成功.");
			map.put("file_path", PropertiesUtil.getValue("ftp.server.http.prefix","http://img.xiaobu.com/") + targetFileName);
			response.setHeader("Access-Control-Allow-Headers", "X-File-Name");
		} else {
			map.put("success", "false");
			map.put("msg", "上传文件失败.");
		}
		return map;
	}
}
