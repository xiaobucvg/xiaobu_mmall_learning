package com.mmall.service;

import com.mmall.common.ServerResponse;

public interface ICartService {
	ServerResponse add(Integer userId, Integer productId, Integer count);
	ServerResponse update(Integer userId,Integer productId, Integer count);
	ServerResponse remove(Integer userId,String productIds);
	ServerResponse selectOneProduct(Integer userId,Integer productId);
	ServerResponse getProductCount(Integer userId);
	ServerResponse unSelectAllProducts(Integer userId);
	ServerResponse selectAllProducts(Integer userId);
}
