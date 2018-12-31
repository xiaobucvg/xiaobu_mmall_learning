package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zh_job
 * 2018/12/12 9:43
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {
	@Resource
	private ShippingMapper shippingMapper;

	public ServerResponse add(Integer userId, Shipping shipping){
		if(userId == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误.");
		}
		int res = shippingMapper.insertSelective(shipping);
		if(res == 0){
			return ServerResponse.createByErrorMessage("新建地址失败.");
		}
		return ServerResponse.createBySuccess("新建地址成功.",shipping.getId());
	}

	public ServerResponse remove(Integer userId, Integer id){
		if(userId == null || id == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误.");
		}
		int res = shippingMapper.deleteAddressByUserId(userId, id);
		if(res == 0){
			return ServerResponse.createByErrorMessage("删除地址失败.");
		}
		return ServerResponse.createBySuccessMessage("删除地址成功.");
	}

	public ServerResponse update(Integer userId, Shipping shipping){
		if(userId == null || shipping == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误.");
		}
		Shipping shippingRes = shippingMapper.selectByPrimaryKey(shipping.getId());
		if(shippingRes == null){
			return ServerResponse.createByErrorMessage("没有此地址信息.");
		}
		if(!shippingRes.getUserId().equals(userId)){
			return ServerResponse.createByErrorMessage("您没有权限.");
		}
		int res = shippingMapper.updateByPrimaryKeySelective(shipping);
		if(res == 0){
			return ServerResponse.createByErrorMessage("更新地址失败.");
		}
		return ServerResponse.createBySuccessMessage("更新地址成功.");
	}

	public ServerResponse detail(Integer userId, Integer id){
		if(userId == null || id == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误.");
		}
		Shipping shipping = shippingMapper.selectByPrimaryKey(id);
		if(shipping == null){
			return ServerResponse.createByErrorMessage("没有此地址信息.");
		}
		return ServerResponse.createBySuccess(shipping);
	}

	public ServerResponse list(Integer userId,Integer pageNum,Integer pageSize){
		if(userId == null || pageNum <= 0 || pageSize <= 0){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误.");
		}
		PageHelper.startPage(pageNum, pageSize);
		List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
		PageInfo pageResult = new PageInfo(shippingList);
		return ServerResponse.createBySuccess(pageResult);
	}
}
