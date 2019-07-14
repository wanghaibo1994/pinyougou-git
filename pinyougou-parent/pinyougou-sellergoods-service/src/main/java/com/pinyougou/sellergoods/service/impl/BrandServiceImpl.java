package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;
	
	/**
	 * 查询所有品牌
	 */
	@Override
	public List<TbBrand> findAll() {
		
		return brandMapper.selectByExample(null);
	}

	/**
	 * 返回分页列表
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		Page<TbBrand> page = (Page<TbBrand>)brandMapper.selectByExample(null);
		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	/**
	 * 添加品牌
	 */
	@Override
	public void add(TbBrand tbBrand) {
		
		brandMapper.insert(tbBrand);
		
	}
	
	/**
	 * 根据id返回要修改的品牌
	 */
	@Override
	public TbBrand findOne(Long id) {
		
		return brandMapper.selectByPrimaryKey(id);
	}

	/**
	 * 根据id返回后修改的品牌，存入数据库
	 */
	@Override
	public void update(TbBrand brand) {
		
		brandMapper.updateByPrimaryKey(brand);
		
	}

	/**
	 * 根据复选框选中的id删除品牌
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id : ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
		
	}

	/**
	 * 条件查询分页
	 */
	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		
		PageHelper.startPage(pageNum, pageSize);//分页	
		
		TbBrandExample example=new TbBrandExample();
		
		Criteria criteria = example.createCriteria();
		if(brand!=null){
			if(brand.getName()!=null && brand.getName().length()>0){
				criteria.andNameLike("%"+brand.getName()+"%");
			}
			if(brand.getFirstChar()!=null && brand.getFirstChar().length()>0){
				criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
			}			
		}
		
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
	
		return brandMapper.selectOptionList();
	}
	
	

}
