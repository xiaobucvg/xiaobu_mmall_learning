package com.mmall.controller.portal;

import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author zh_job
 * 2018/12/9 17:38
 */
@Controller
@RequestMapping("/product/")
public class ProductController {
	@Resource
	private IProductService iProductService;

	/** 产品搜索及动态排序List */
	@RequestMapping("list.do")
	@ResponseBody
	public ServerResponse searchAndList(
			@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId,
			@RequestParam(value = "keyWord",defaultValue = "%") String keyWord,
			@RequestParam(value = "pageNumber",defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize,
			@RequestParam(value = "orderBy",defaultValue = "price_asc") String orderBy
	){
		return iProductService.searchAndList(categoryId, keyWord, pageNumber, pageSize, orderBy);
	}

	/** 获取产品详情 */
	@RequestMapping("get_detail_by_portal.do")
	@ResponseBody
	public ServerResponse getProductDetail(Integer id){
		return iProductService.getProductDetailByPortal(id);
	}
}

