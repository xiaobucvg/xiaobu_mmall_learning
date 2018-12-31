package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

public interface IShippingService {
	ServerResponse add(Integer userId, Shipping shipping);
	ServerResponse remove(Integer userId, Integer id);
	ServerResponse update(Integer userId, Shipping shipping);
	ServerResponse detail(Integer userId, Integer id);
	ServerResponse list(Integer userId,Integer pageNum,Integer pageSize);
}
