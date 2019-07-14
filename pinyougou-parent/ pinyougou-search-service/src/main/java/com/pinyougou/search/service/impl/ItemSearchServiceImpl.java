package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;


@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询品牌列表和规格列表
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		Map map=new HashMap();
		//1.根据商品分类名称得到模板ID		
		Long templateId= (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if(templateId!=null){
			//2.根据模板ID获取品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);	
			
			
			//3.根据模板ID获取规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);		
			
		}	
		
		return map;
	}
	/**
	 * 搜索
	 */
	@Override
	public Map<String,Object> search(Map searchMap) {
		
		//关键字去空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		Map<String,Object> map=new HashMap<String,Object>();
		//查询列表
		map.putAll(searchList(searchMap));
		//分组，商品的分类查询
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		//查询品牌和规格列表
		System.out.println(searchMap);
		String category= (String) searchMap.get("category");		
		if(category!= null && !category.equals("")){						
			map.putAll(searchBrandAndSpecList(category));
		}else{
			if(categoryList.size()>0){	
				System.out.println("查询的分类条件："+(String)categoryList.get(0));
				map.putAll(searchBrandAndSpecList((String)categoryList.get(0)));
			}	
		}
		
		
		return map;
	}
	//创建私有的方法，将带有高亮的结果集合返回
	private Map searchList(Map searchMap) {
		//创建Map集合
		Map map = new HashMap();
		//创建高亮显示的查询条件
		HighlightQuery query = new SimpleHighlightQuery();
		//设置高亮的域
		HighlightOptions highlightOptions =new HighlightOptions().addField("item_title");
		//设置高亮的前缀
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		//设置高亮的后缀
		highlightOptions.setSimplePostfix("</em>");
		//设置高亮的选项
		query.setHighlightOptions(highlightOptions );
		
		//1.1按照关键字进行查询 ,					从item_keywords中查找  is是匹配条件（先分词在查找）
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		///1.2 按商品分类过滤
		if(!"".equals(searchMap.get("category"))  )	{//如果用户选择了分类 
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);			
		}
		
		//1.3 按品牌过滤
		if(!"".equals(searchMap.get("brand"))  )	{//如果用户选择了品牌
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);			
		}
		//1.4 按规格过滤
		if(searchMap.get("spec")!=null){			
			Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
			for(String key :specMap.keySet()){
				
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key)  );
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);					
				
			}		
			
		}
		//1.5按价格过滤
		if(!"".equals(searchMap.get("price"))) {//如果价格区间存在
			String[] price = ((String)searchMap.get("price")).split("-");
			if(!price[0].equals("0")) {//如果最低价格不为0
				FilterQuery filterQuery = new SimpleFilterQuery();//创建查询条件
				Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			if(!price[1].equals("*")) {//如果最高价格不为*
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);	
				
			}
			
		}
		//1.6分页查询
		Integer pageNo=(Integer)searchMap.get("pageNo");//获取当前页
		if(pageNo==null) {//如果页面不存在，那么默认页面为1；
			pageNo=1;//默认该页面是第一页
		}
		Integer pageSize=(Integer)searchMap.get("pageSize");//获取每页的记录数
		if(pageSize==null) {
			pageSize=20;//默认每页显示20条
		}
		query.setOffset((pageNo-1)*pageSize);//从第几条记录开始查询
		query.setRows(pageSize);
		
		//1.7排序
		String sortValue = (String) searchMap.get("sort");//排序的方式：ASC 或  DESC
		String sortField = (String) searchMap.get("sortField");//排序的字段
		if(sortValue!=null && !sortValue.equals("")) {
			if(sortValue.equals("ASC")) {
				Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")) {
				Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
		}
		
		
		
		//获取高亮结果集
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		for(HighlightEntry<TbItem> h : page.getHighlighted()) {//循环高亮入口集合
			//获取原实体类
			TbItem item = h.getEntity();
			
			if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0) {
				//设置高亮的结果
				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		
		map.put("totalPages", page.getTotalPages());//返回总记页数
		map.put("total", page.getTotalElements());//返回总记录数
		
		return map;
		
	}
	
	/**
	 * 查询分类列表
	 * @param searchMap
	 * @return
	 */
	private List searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList();
		Query query = new SimpleQuery();
		//按照关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		//添加查询条件
		query.addCriteria(criteria);
		//设置分组选项
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//获得分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//根据分组页得到结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//根据结果集获得分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		
		for(GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
		}
		return list;
	}
	/**
	 * 导入数据
	 */
	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}
	/**
	 * 删除数据
	 */
	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		Query query = new SimpleQuery();
		Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
		
	}

}
