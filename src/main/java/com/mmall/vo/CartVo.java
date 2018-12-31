package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * 一个用户的购物车列表 包含了购物车清单 总价 是否全部被选中
 *
 * @author zh_job
 * 2018/12/10 11:31
 */
public class CartVo {
	private Boolean checkAll;
	private BigDecimal priceAll;
	private String imgHost;
	private List<CartProductVo> cartProductVoList;

	public String getImgHost() {
		return imgHost;
	}

	public void setImgHost(String imgHost) {
		this.imgHost = imgHost;
	}

	public Boolean getCheckAll() {
		return checkAll;
	}

	public void setCheckAll(Boolean checkAll) {
		this.checkAll = checkAll;
	}

	public BigDecimal getPriceAll() {
		return priceAll;
	}

	public void setPriceAll(BigDecimal priceAll) {
		this.priceAll = priceAll;
	}

	public List<CartProductVo> getCartProductVoList() {
		return cartProductVoList;
	}

	public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
		this.cartProductVoList = cartProductVoList;
	}
}
