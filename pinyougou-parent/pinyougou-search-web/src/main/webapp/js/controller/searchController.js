app.controller('searchController',function($scope,$location,searchService){
	
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sortField':'','sort':''};//把搜索的条件封装成对象
	
	//添加搜索选项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand' || key=='price'){
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//执行搜索
	}
	
	//移除符合搜索选项
	$scope.removeSearchItem=function(key){
		if(key=="category" || key=="brand" || key=='price'){//如果是分类或品牌
			$scope.searchMap[key]="";
		}else{
			delete $scope.searchMap.spec[key];//移除此属性
		}
		$scope.search();
	}
	
	
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap=response;
				buildPageLabel();//调用构建分页标签
			}
		);		
	}
	//构建分页标签
	buildPageLabel=function(){
		$scope.pageLabel=[];//新增分页栏属性
		var maxPageNo= $scope.resultMap.totalPages;//得到最后的页码
		var firstPage=1;//开始的页码
		var lastPage=maxPageNo;//最后的页码
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后面有点
		if($scope.resultMap.totalPages> 5){//如果总也页数大于5，那么显示部分页面
			if($scope.searchMap.pageNo<=3){//如果当前的页码小于等于3，那么最后一页为第5页
				lastPage=5;
				$scope.firstDot=false;//前面没有点
			}else if($scope.searchMap.pageNo>=lastPage-2){//如果当前页码大于等于最大页码-2，那么开始的页码为最大页码-4；
				firstPage= maxPageNo-4;
				$scope.lastDot=false;//后面没有点
			
			}else{
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2; 
				
			}
		}else{
			$scope.firstDot=false;//前面没有点
			$scope.lastDot=false;//后面没有点
			
		}
		//循环页码标签，放入数组中
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		//页码验证
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo=pageNo; 
		$scope.search();
	}
	
	//判断当前是不是第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	//判断当前页是不是最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
	//设置排序规则
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		$scope.search();
		
	}
	//判断关键字是不是品牌
	$scope.keywordsIsBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
				return true;
			}
		}
		return false;
	}
	//加载查询字符串
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords= $location.search()['keywords'];
		$scope.search();
	}
	
	
});