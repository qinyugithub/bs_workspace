var LoginModel = function(){
	this.xxServiceId = 'selectListServiceId';//查询表格serviceId

};

var LoginService = function(model){
	var that = this;
	/**
	*初始化表格
	*/
	this.loadGrid = function(){
		
	}
	
};

var LoginController = function(){
	var model = new LoginModel();
	var service = new LoginService(model);//将model传到Service层
	
	//初始化表格，下拉框等等
	this.init = function(){
       //alert('ok');
	}
	
}

$(function(){
	var controller = new LoginController();
	//初始化
	controller.init();
	
	//xx点击事件
	/*$("#xx").on(click,function(){
		//将点事件需要处理的代码逻辑写在controller层
		controller.xxClick();
	});*/
	
})