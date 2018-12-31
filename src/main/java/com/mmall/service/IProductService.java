package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;

/** 产品 */
public interface IProductService {
	ServerResponse productSaveOrUpdate(Product product);
	ServerResponse setSaleStatus(Integer id, Integer status);
	ServerResponse getProductDetail(Integer id);
	ServerResponse getProductList(Integer pageNumber,Integer pageSize);
	ServerResponse searchProduct(String productName,Integer id,Integer pageNumber,Integer pageSize);
	ServerResponse searchAndList(Integer categoryId, String keyWord,Integer pageNumber,Integer pageSize,String orderBy);
	ServerResponse getProductDetailByPortal(Integer id);
}
