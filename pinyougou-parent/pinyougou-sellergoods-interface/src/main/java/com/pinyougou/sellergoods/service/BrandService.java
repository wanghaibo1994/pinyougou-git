package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;
import entity.Result;

/**
 * 品牌列表的接口
 * @author asus-1
 *
 */
public interface BrandService {
	/**
	 * 查询所有品牌
	 * @return
	 */
	public List<TbBrand> findAll(); 
	
	/**
	 * 返回的分页列表
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	/**
	 * 添加品牌
	 * @param tbBrand
	 * @return
	 */
	
	public void add(TbBrand tbBrand);
	
	
	/**
	 * 根据id返回要修改的品牌
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	
	
	/**
	 * 根据id返回的品牌，修改后保存到数据库
	 * @param brand
	 */
	public void update(TbBrand brand);
	
	/**
	 * 根据复选框选中的id删除品牌
	 * @param ids
	 */
	public void delete(Long[] ids);
	
	/**
	 * 条件查询分页
	 * @param brand
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
	
	/**
	 * 返回下拉列表数据
	 * @return
	 */
	public List<Map> selectOptionList();

}
