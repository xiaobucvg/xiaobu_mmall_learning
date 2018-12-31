package com.mmall.vo;

import com.mmall.pojo.Product;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 产品VO
 *
 * @author zh_job
 * 2018/12/7 10:41
 **/
public class ProductVo extends Product {

	private Integer parentCategoryId;

	private String imageHost;

	private String createTimeStr;

	private String updateTimeStr;

	public ProductVo(Product product) {
		this(product.getId(), product.getCategoryId(), product.getName(), product.getSubtitle(), product.getMainImage(), product.getSubImages(), product.getDetail(), product.getPrice(), product.getStock(), product.getStatus(), product.getCreateTime(), product.getUpdateTime());
	}

	public ProductVo() {
	}

	private ProductVo(Integer id, Integer categoryId, String name, String subtitle, String mainImage, String subImages, String detail, BigDecimal price, Integer stock, Integer status, Date createTime, Date updateTime) {
		super(id, categoryId, name, subtitle, mainImage, subImages, detail, price, stock, status, createTime, updateTime);
	}

	public Integer getParentCategoryId() {
		return parentCategoryId;
	}

	public void setParentCategoryId(Integer parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}

	public String getImageHost() {
		return imageHost;
	}

	public void setImageHost(String imageHost) {
		this.imageHost = imageHost;
	}

	public String getCreateTimeStr() {
		return createTimeStr;
	}

	public void setCreateTimeStr(String createTimeStr) {
		this.createTimeStr = createTimeStr;
	}

	public String getUpdateTimeStr() {
		return updateTimeStr;
	}

	public void setUpdateTimeStr(String updateTimeStr) {
		this.updateTimeStr = updateTimeStr;
	}

	@Override
	public String toString() {
		return super.toString() + "ProductVo{" +
				"parentCategoryId=" + parentCategoryId +
				", imageHost='" + imageHost + '\'' +
				'}';
	}
}
