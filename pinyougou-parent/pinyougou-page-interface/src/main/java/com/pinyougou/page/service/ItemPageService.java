package com.pinyougou.page.service;

/**
 * 商品详细页接口
 * @author asus-1
 *
 */
public interface ItemPageService {
	
	/**
	 * 生成商品详细页
	 * @param goodsId
	 * @return
	 */
	public boolean genItemHtml(Long goodsId);

	/**
	 * 用于删除静态页面
	 * @param goodsIds
	 * @return
	 */
	public  boolean deleteItemHtml(Long[] goodsIds);

}
