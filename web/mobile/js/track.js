var TrackModel = function(){

};


var TrackService = function(model){
    var that = this;

};


var TrackController = function(){
    var model = new TrackModel();
    var service = new TrackService(model);//将model传到Service层
    var that=this;

    //初始化表格，下拉框等等
    this.init = function(){
        var code = that.getUrlParam('type');
        if(code==1){
            $("#iconone").show();
            $("#detil").text("实时定位中...");
        }else{
            $("#icontwo").show();
            $("#detil").text("报警成功!");
        }
    }

    //获取url中的参数
    this.getUrlParam = function (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
        var r = window.location.search.substr(1).match(reg);  //匹配目标参数
        if (r != null) return unescape(r[2]);
        return null; //返回参数值
     }
    }

$(function(){
    var controller = new TrackController();
    //初始化
    controller.init();

/*    $("#chat").on("click",function(){
    });*/

})