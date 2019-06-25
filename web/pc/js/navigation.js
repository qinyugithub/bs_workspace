var NavigationModel = function(){
    this.NavigationServiceId = 'selectListServiceId';//查询表格serviceId
};

var NavigationService = function(model){
    var that = this;

    this.loadGrid = function(){

    }
};

/**
 *Xxx控制层，该层主要用于逻辑处理
 */
var NavigationController = function(){
    var model = new NavigationModel();
    var service = new NavigationService(model);//将model传到Service层

    this.init = function(){
        //初始化导航栏
        layui.use('element', function(){
            var element = layui.element; //导航的hover效果、二级菜单等功能，需要依赖element模块
            //监听导航点击
            element.on('nav(demo)', function(elem){
                console.log("ok")
                //layer.msg(elem.text());
            });
        });
    }

}


$(function(){
    var controller = new NavigationController();
    //初始化
    controller.init();

    //xx点击事件
/*    $("#xx").on(click,function(){
        //将点事件需要处理的代码逻辑写在controller层
        controller.xxClick();
    });*/

})