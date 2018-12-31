package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectProductList();

    List<Product> selectSearchProduct(@Param("productName") String productName,@Param("id") Integer id);

	List<Product> selectByNameAndCategoryIds(@Param("keyWord") String keyWord,@Param("categoryIds") List<Integer> categoryIds);

	List<Product> selectByProductIds(List<Integer> ids);
}