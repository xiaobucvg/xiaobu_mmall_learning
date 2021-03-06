package com.mmall.dao;

import com.mmall.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

	Order selectByUserIdOrderNo(@Param("userId") Integer userId,@Param("orderNo") Long orderId);

	Order selectByOrderNo(Long orderNo);

	List<Order> selectByUserId(Integer userId);

	List<Order> selectAll();

	List<Order> selectOrderStatusByTime(@Param("status") Integer status, @Param("date") String date);

	void closeOrderByOrderId(Integer id);
}