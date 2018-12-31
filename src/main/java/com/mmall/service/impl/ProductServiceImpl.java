package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductListVo;
import com.mmall.vo.ProductVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 产品
 *
 * @author zh_job
 * 2018/12/7 9:36
 **/
@Service("iProductService")
public class ProductServiceImpl implements IProductService {
	@Resource
	private ProductMapper productMapper;

	@Resource
	private CategoryMapper categoryMapper;

	@Resource
	private ICategoryService iCategoryService;

	public ServerResponse productSaveOrUpdate(Product product) {
		if (product == null) {
			return ServerResponse.createByErrorMessage("需要操作的产品为空.");
		}
		//如果主图为空 并且有子图 则子图的第一张作为主图
		if (product.getMainImage() == null && product.getSubImages() != null) {
			String[] subImages = product.getSubImages().split(",");
			if (subImages.length > 0) {
				product.setMainImage(subImages[0]);
			}
		}
		//首先在数据库里面查询一把 找找有没有相同ID的数据 有的话就是更新 没有的话就是添加
		Product resPro = productMapper.selectByPrimaryKey(product.getId());
		int res;
		if (resPro != null) {
			res = productMapper.updateByPrimaryKeySelective(product);
			if (res > 0) {
				return ServerResponse.createBySuccessMessage("更新产品成功.");
			}
		}
		res = productMapper.insertSelective(product);
		if (res > 0) {
			return ServerResponse.createBySuccessMessage("添加产品成功.");
		}
		return ServerResponse.createByErrorMessage("更新或者添加产品失败.");
	}

	public ServerResponse setSaleStatus(Integer id, Integer status) {
		Product product = new Product();
		product.setId(id);
		product.setStatus(status);

		//todo 或需要加一个产品状态码的判断 这样的话就需要加一个常量类与状态码对应 如果接收到的状态码找不到映射 就响应一个参数错误

		int res = productMapper.updateByPrimaryKeySelective(product);
		if (res > 0) {
			return ServerResponse.createBySuccessMessage("更新产品状态成功.");
		}
		return ServerResponse.createByErrorMessage("更新产品状态失败.");
	}

	public ServerResponse getProductDetail(Integer id) {
		Product resPro = productMapper.selectByPrimaryKey(id);
		if (resPro == null) {
			return ServerResponse.createByErrorMessage("产品不存在或者已经下架.");
		}
		ProductVo productVo = assembleProduct(resPro);
		return ServerResponse.createBySuccess(productVo);
	}

	/**
	 * 获取产品的vo对象
	 */
	private ProductVo assembleProduct(Product product) {
		ProductVo productVo = new ProductVo(product);
		Integer categoryId = product.getCategoryId();

		productVo.setCreateTimeStr(DateUtil.dateToString(product.getCreateTime()));
		productVo.setUpdateTimeStr(DateUtil.dateToString(product.getUpdateTime()));

		// 查找父ID 如果找不到赋默认值 0 表示根节点
		Category category = categoryMapper.selectByPrimaryKey(categoryId);
		if (category != null) {
			productVo.setParentCategoryId(category.getId());
		} else {
			productVo.setParentCategoryId(0);
		}
		// 图片服务器地址 使用Properties工具读取 方便配置
		String ftpAddress = PropertiesUtil.getValue("ftp.server.http.prefix", "http://img.xiaobu.com/");
		productVo.setImageHost(ftpAddress);
		return productVo;
	}

	public ServerResponse getProductList(Integer pageNumber, Integer pageSize) {
		//pageHelper -- start;
		//填充自己的查询逻辑
		//pageHelper收尾
		PageHelper.startPage(pageNumber, pageSize);
		List<Product> products = productMapper.selectProductList();
		PageInfo pageResult = new PageInfo<>(products);
		products = pageResult.getList();

		List<ProductListVo> productListVos = Lists.newArrayList();
		for (Product productItem : products) {
			productListVos.add(assembleProductListVo(productItem));
		}
		pageResult.setList(productListVos);
		return ServerResponse.createBySuccess(pageResult);
	}

	/**
	 * Description: 封装成ProductListVo
	 */
	private ProductListVo assembleProductListVo(Product product) {
		ProductListVo proVo = new ProductListVo();
		proVo.setId(product.getId());
		proVo.setName(product.getName());
		proVo.setCategoryId(product.getCategoryId());
		proVo.setSubtitle(product.getSubtitle());
		proVo.setMainImage(product.getMainImage());
		proVo.setPrice(product.getPrice());
		proVo.setStatus(product.getStatus());
		proVo.setImageHost(PropertiesUtil.getValue("ftp.server.http.prefix", "http://img.xiaobu.com/"));
		return proVo;
	}

	public ServerResponse searchProduct(String productName, Integer id, Integer pageNumber, Integer pageSize) {
		if (StringUtils.isEmpty(productName) && id == null) {
			return ServerResponse.createByError();
		}
		PageHelper.startPage(pageNumber, pageSize);
		productName = "%" + (productName) + "%";
		List<Product> products = productMapper.selectSearchProduct(productName, id);
		PageInfo pageResult = new PageInfo<>(products);
		products = pageResult.getList();

		List<ProductListVo> productListVos = Lists.newArrayList();
		for (Product productItem : products) {
			productListVos.add(assembleProductListVo(productItem));
		}
		pageResult.setList(productListVos);
		return ServerResponse.createBySuccess(pageResult);
	}

	//portal

	/**
	 * 前台搜索并排序
	 *
	 * 由于收到的可能是父类ID 所以需要将父类ID的所有孩子全部取到再查询
	 * */
	public ServerResponse searchAndList(Integer categoryId, String keyWord,Integer pageNumber,Integer pageSize,String orderBy){
		if(StringUtils.isEmpty(keyWord) && categoryId == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "关键字和分类不能都为空.");
		}
		if(!StringUtils.isEmpty(keyWord)){
			keyWord = "%" + keyWord + "%";
		}
		List<Integer> allNodes = iCategoryService.getCategoryAndDeepSubNode(categoryId).getData();

		PageHelper.startPage(pageNumber,pageSize);
		if(!StringUtils.isEmpty(orderBy)){
			if(Const.ORDER.contains(orderBy)){
				String[] orderByArray = orderBy.split("_");
				PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
			}
		}
		List<Product> products = productMapper.selectByNameAndCategoryIds(keyWord, allNodes);
		PageInfo pageResult = new PageInfo(products);
		products = pageResult.getList();
		List<ProductListVo> productListVos = Lists.newArrayList();
		for(Product productItem : products){
			productListVos.add(assembleProductListVo(productItem));
		}
		pageResult.setList(productListVos);
		return ServerResponse.createBySuccess(pageResult);
	}

	public ServerResponse getProductDetailByPortal(Integer id){
		if(id == null){
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "请求参数无效.");
		}
		Product product = productMapper.selectByPrimaryKey(id);
		//如果商品查不到或者查到了但是不是在售状态 返回空
		if(product == null || product.getStatus() != 1){
			return ServerResponse.createByErrorMessage("查询失败，商品不存在或者已经下架.");
		}
		ProductVo productVo = assembleProduct(product);
		return ServerResponse.createBySuccess(productVo);
	}
}
