app.controller('indexController',function($scope,$controller,shopLoginService){
	
	$scope.showLoginName=function(){
		shopLoginService.shopLoginName().success(
			function(response){
				$scope.shopLoginName=response.shopLoginName;				
			}
		);		
	}
	
	
});