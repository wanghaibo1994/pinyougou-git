 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){

    //注册
    $scope.reg=function () {
        if($scope.entity.password != $scope.password){
            $scope.password="";
            $scope.entity.password="";
            alert("两次密码不一致");
            return;
        }

        userService.add($scope.entity,$scope.smscode).success(
            function (response) {
                alert(response.message)
            }
        );
    };

    $scope.sendCode=function () {
        if($scope.entity.phone==null || $scope.entity.phone==""){
            alert("请输入手机号");
            return;
        }

        userService.sendCode($scope.entity.phone).success(

            function (response) {
                alert(response.message)
            }
            
        );
    }
	
});	
