var LoginModel = function(){
	this.xxServiceId = 'selectListServiceId';//查询表格serviceId

};

var LoginService = function(model){
	var that = this;
	/**
	*初始化表格
	*/
	this.checkuser = function(username,password){
		var ret=null;
        $.ajax({
            url: "checkuser.do",
            type: "POST",
            dataType:"json",
            async:false,
            data: {username: username,password:password},
            success: function (result) {
                ret=result;
            },
            error: function (xhr, status, p3, p4) {
                alert("登录验证出错");
            }
        });
        return ret;
	}
	
};

var LoginController = function(){
	var model = new LoginModel();
	var service = new LoginService(model);//将model传到Service层

	this.init = function(){
	}

	this.checkuser=function() {
        if ($("#username").val() !== "" && $("#password").val() !== "") {
            var ret = service.checkuser($("#username").val(), $("#password").val());
            if (ret) {
                sessionStorage.setItem('checked',true);
                window.location.href = "pc/webgis.html";
            } else {
                sessionStorage.setItem('checked',false);
                layui.use('layer', function () { //独立版的layer无需执行这一句
                    // layer = layui.layer;
                    layer.open({
                        title: '登录提示'
                        , content: '用户名或密码错误'
                        , anim: 6
                        , icon: 2
                    });
                });
            }
        }
    }
}

$(function(){
	var controller = new LoginController();
	//初始化
	controller.init();
	//登录点击事件
	$("#login").on("click",function(){
        controller.checkuser();
	});
    //回车事件
    $('#password').bind('keypress',function(event){
        if(event.keyCode == "13")
        {
        	controller.checkuser();
        }
    });
})