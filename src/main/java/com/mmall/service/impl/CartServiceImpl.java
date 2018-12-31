package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zh_job
 * 2018/12/10 11:03
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {
	@Resource
	private CartMapper cartMapper;
	@Resource
	private ProductMapper productMapper;

	public ServerResponse add(Integer userId,Integer productId, Integer count) {
		if(userId == null || productId == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		Cart res = cartMapper.selectCartByUserIdProductId(userId,productId);
		// 查询数据库中是否存在购物车的记录
		// 如果不存在就是新增记录
		// 如果存在就是更新数量
		int opRes;
		if(res == null){
			Cart cart = new Cart();
			cart.setUserId(userId);
			cart.setProductId(productId);
			cart.setQuantity(count);
			cart.setChecked(Const.Cart.CHECK_TRUE); // 修改为选中状态
			opRes = cartMapper.insertSelective(cart);
		} else {
			res.setQuantity(res.getQuantity() + count);
			opRes = cartMapper.updateByPrimaryKeySelective(res);
		}
		if(opRes == 0){
			return ServerResponse.createByErrorMessage("添加购物车记录失败.");
		}
		CartVo cartVo = assembleCartVoLimited(userId);
		return ServerResponse.createBySuccess(cartVo);
	}
	private CartVo assembleCartVoLimited(Integer userId){
		List<Cart> carts = cartMapper.selectCartByUserId(userId);
		List<CartProductVo> cartProductVoList;
		CartVo cartVo = new CartVo();
		if(carts.size() != 0){
			cartProductVoList = new ArrayList<>();
			Product productItem;
			int productStock;
			int cartQuantity;
			BigDecimal cartTotalPrice = new BigDecimal(0);
			for(Cart cartItem : carts){
				CartProductVo cartProductVoItem;
				productItem = productMapper.selectByPrimaryKey(cartItem.getProductId());
				cartProductVoItem = new CartProductVo();
				cartProductVoItem.setId(cartItem.getId());
				cartProductVoItem.setProductId(productItem.getId());
				cartProductVoItem.setProductStock(productItem.getStock());
				cartProductVoItem.setProductName(productItem.getName());
				cartProductVoItem.setMainImage(productItem.getMainImage());
				cartProductVoItem.setProductPrice(productItem.getPrice());
				cartProductVoItem.setProductStatus(productItem.getStatus());
				cartProductVoItem.setSubtitle(productItem.getSubtitle());
				cartProductVoItem.setProductChecked(cartItem.getChecked());
				cartProductVoItem.setUserId(cartItem.getUserId());
				// 获取库存和购买量
				productStock = productItem.getStock();
				cartQuantity = cartItem.getQuantity();
				// 判断库存是否充足，如果不充足需要对购买量做限制
				if(productStock >= cartQuantity){
					cartProductVoItem.setLimitQuantity(Const.Cart.LIMITED_TRUE);
				} else {
					cartQuantity = productStock;
					cartItem.setQuantity(cartQuantity);
					cartMapper.updateByPrimaryKeySelective(cartItem);
					cartProductVoItem.setLimitQuantity(Const.Cart.LIMITED_FALSE);
				}
				cartProductVoItem.setQuantity(cartQuantity);
				// 当前商品的总价格 购买数量 * 单价
				cartProductVoItem.setProductTotalPrice(BigDecimalUtil.multi(cartQuantity, cartProductVoItem.getProductPrice().doubleValue()));
				cartProductVoList.add(cartProductVoItem);
				// 如果已经被选中 增加到购物车总价中
				if(cartProductVoItem.getProductChecked() == Const.Cart.CHECK_TRUE){
					cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVoItem.getProductTotalPrice().doubleValue());
				}
			}
			cartVo.setCartProductVoList(cartProductVoList);
			cartVo.setCheckAll(getCheckedAllStatus(userId));
			cartVo.setPriceAll(cartTotalPrice);
		}
		cartVo.setImgHost(PropertiesUtil.getValue("ftp.server.http.prefix", "http://img.xiaobu.com/"));
		return cartVo;
	}
	/** 查询商品是否全部被选中 （sql查的是是否全部未被选中）*/
	private Boolean getCheckedAllStatus(Integer userId){
		if(userId == null){
			return false;
		}
		return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
	}

	public ServerResponse update(Integer userId,Integer productId, Integer count){
		if(userId == null || productId == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
		// 如果没有记录就直接添加添加记录
		if(cart != null){
			cart.setQuantity(count);
			cartMapper.updateByPrimaryKeySelective(cart);
		}
		CartVo cartVo = assembleCartVoLimited(userId);
		return ServerResponse.createBySuccess(cartVo);
	}

	public ServerResponse remove(Integer userId,String productIds){
		if(userId == null || productIds == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		List<String> proIds = Splitter.on(",").splitToList(productIds);
		int res = cartMapper.deleteProductByProductsIds(userId, proIds);
		if(res == 0){
			return ServerResponse.createByErrorMessage("移除商品失败.");
		}
		CartVo cartVo = assembleCartVoLimited(userId);
		return ServerResponse.createBySuccess(cartVo);
	}

	public ServerResponse selectOneProduct(Integer userId,Integer productId){
		if(userId == null || productId == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
		if(cart == null){
			return ServerResponse.createByError();
		}
		CartVo cartVo = new CartVo();
		// 如果是选中改为不选中 需要更改购物车选中的总价
		if(cart.getChecked() == Const.Cart.CHECK_TRUE){
			cart.setChecked(Const.Cart.CHECK_FALSE);
		} else {
			cart.setChecked(Const.Cart.CHECK_TRUE);
		}
		cartMapper.updateByPrimaryKey(cart);
		cartVo = assembleCartVoLimited(userId);
		return ServerResponse.createBySuccess(cartVo);
	}

	public ServerResponse getProductCount(Integer userId){
		if(userId == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		List<Cart> carts = cartMapper.selectCartByUserId(userId);
		int count = 0;
		for(Cart cart : carts){
			count += cart.getQuantity();
		}
		return ServerResponse.createBySuccess(count);
	}

	public ServerResponse unSelectAllProducts(Integer userId){
		if(userId == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		List<Cart> carts = cartMapper.selectCheckedCartByUserId(userId);
		for(Cart cartItem : carts){
			cartItem.setChecked(Const.Cart.CHECK_FALSE);
			cartMapper.updateByPrimaryKeySelective(cartItem);
		}
		CartVo cartVo = assembleCartVoLimited(userId);
		return ServerResponse.createBySuccess(cartVo);
	}

	public ServerResponse selectAllProducts(Integer userId){
		if(userId == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		List<Cart> carts = cartMapper.selectUnCheckedBuUserId(userId);
		for(Cart cartItem : carts){
			cartItem.setChecked(Const.Cart.CHECK_TRUE);
			cartMapper.updateByPrimaryKeySelective(cartItem);
		}
		CartVo cartVo = assembleCartVoLimited(userId);
		return ServerResponse.createBySuccess(cartVo);
	}
}

