var UrgentModel = function(){
    this.user={
        "personinformation":{"name":1},  //个人信息，json对象
        "latitude":null,           //当前纬度
        "longitude":null,          //当前经度
        "speed":null,              //当前速度
        "accuracy":null,           //位置精度
        "picurl":[],               //图片名称数组
        "addresstype":1,           //位置类型（1：实时追踪 0：选取的固定位置）
        "message":"",              //警况描述

        "placelatitude":null,      //选取固定位置时的经纬度和地址
        "placelongitude":null,
        "placeaddress":""
    }
    this.files=null;
    this.code=null;
    this.flag=true;  //避免重复请求
};


var UrgentService = function(model){
    var that = this;

    //根据code获取用户个人信息
    this.getWebToken = function(code){
        $.ajax({
                url: "../gettokenAndUserid.do",
                type: "POST",
                dataType:"json",
                async:false,
                data: { code: code },
                success: function (result) {
                    model.user.personinformation= result;
                   // sessionStorage.setItem('userinformation', JSON.stringify(result));
                  //  var userJsonStr = sessionStorage.getItem('userinformation');
                  //  var userEntity = JSON.parse(userJsonStr);
                  //  console.log(userEntity.get("city"))
                },
                error: function (xhr, status, p3, p4) {
                    alert("授权出错");
                }
            });
    }
    //获取页面的签名
    this.getSignature=function(){
        var res=null;
        $.ajax({
            url: "../getSignature.do",
            type: "POST",
            dataType:"json",
            async:false,
            data: { url: window.location.href },
            success: function (result) {
                res=result;
            },
            error: function (xhr, status, p3, p4) {
                alert("获取签名出错");
            }
        });
        return res;
    }

    this.urgentmessage=function(user){
        console.log(JSON.stringify(user));
        var res=null;
        $.ajax({
            url: "../urgent.do",
            type: "POST",
            dataType:"json",
            async:false,
            data: { user: JSON.stringify(user) },
            success: function (result) {
                if(result.msg){
                    var val=$('input:radio[name="radio1"]:checked').val();
                    if(val==0){//选取了固定位置
                        window.location.href = "track.html";
                         }else{
                        window.location.href = "track.html?type=1";
                    }

                }
            },
            error: function (xhr, status, p3, p4) {
                alert("请求出错");
            }
        });
        return res;
    }

};


var UrgentController = function() {
    var model = new UrgentModel();
    var service = new UrgentService(model);//将model传到Service层
    var that = this;
    //初始化表格，下拉框等等
    this.init = function () {
        var code = that.getUrlParam('code');
        model.code=code;
        if (code != null) {
            service.getWebToken(code);
        } else {
            alert("非法访问！");
            return;
        }

        that.makeSignature();

    }

    //获取url中的参数
    this.getUrlParam = function (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
        var r = window.location.search.substr(1).match(reg);  //匹配目标参数
        if (r != null) return unescape(r[2]);
        return null; //返回参数值
    }

    //签名验证
    this.makeSignature = function () {
        var result = service.getSignature();
        //console.log(result.signature);
        wx.config({
            debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
            appId: result.app_id, // 必填，公众号的唯一标识
            timestamp: result.timestamp, // 必填，生成签名的时间戳
            nonceStr: result.nonceStr, // 必填，生成签名的随机串
            signature: result.signature,// 必填，签名
            jsApiList: ['getLocation', 'openLocation', 'chooseImage'] // 必填，需要使用的JS接口列表
        });
        wx.ready(function () {
            console.log("success ready!");

            wx.getLocation({
                type: 'wgs84', // 默认为wgs84的gps坐标，如果要返回直接给openLocation用的火星坐标，可传入'gcj02'
                success: function (res) {
                    model.user.latitude = res.latitude; // 纬度，浮点数，范围为90 ~ -90
                    model.user.longitude = res.longitude; // 经度，浮点数，范围为180 ~ -180。
                    model.user.speed = res.speed; // 速度，以米/每秒计
                    model.user.accuracy = res.accuracy; // 位置精度
                }
            });

            /*            wx.openLocation({
                            latitude: Number(model.latitude), // 纬度，浮点数，范围为90 ~ -90
                            longitude: Number(model.longitude), // 经度，浮点数，范围为180 ~ -180。
                            name: '位置名', // 位置名
                            address: '地址详情说明', // 地址详情说明
                            //scale: 1, // 地图缩放级别,整形值,范围从1~28。默认为最大
                            infoUrl: 'https://www.baidu.com' // 在查看位置界面底部显示的超链接,可点击跳转
                        });*/

        });
        wx.error(function (res) {
            alert("签名验证出错" + res);
        });

    }


    function adaptHeight() {
        var winH = $(window).height();
        var bodyH = document.documentElement.clientHeight;
        if (winH > bodyH) {
            window.parent.document.getElementById("iframe").height=winH;
        } else {
            window.parent.document.getElementById("iframe").height=bodyH;
        }
    }
    //初始化地图弹框
    this.initMap=function(){
        adaptHeight();//动态适配高度

        window.onresize = function() { //横屏、QQ浏览器变全屏模式下的时候，需要重新计算高度
            adaptHeight();
        }

        window.addEventListener('message', function(event) {
            var loc = event.data;
            //alert('你使用的组件是'+loc.module+',刚选择了'+loc.poiname+',它位于'+loc.poiaddress+',它的经纬度是：'+loc.latlng.lat+','+loc.latlng.lng+',所属城市为:'+loc.cityname)
            model.user.placeaddress=loc.poiaddress+","+loc.poiname;
            model.user.placelatitude=loc.latlng.lat;
            model.user.placelongitude=loc.latlng.lng;

            layui.use('layer', function () {layer.closeAll();})
            $("#address").text(loc.poiaddress);
            $("#address_detil").show();
            $("#detil").focus();
        }, false);
    }

    //显示地图弹框
    this.showmap=function(){
        layui.use('layer', function () {
            layer.open({
                type:1
                ,title: false
                ,closeBtn: false
                ,shadeClose: true
                ,content: $('#iframe')
                ,area: ['100%', '100%']
                ,anim: 2,
                success: function(layero, index){
                    $("#iframe").attr("src", "https://apis.map.qq.com/tools/locpicker?search=1&type=1&key=FMIBZ-R3LW6-ITPS7-EXR62-WHVWV-5ABGN&referer=myapp");
                }
            });
        });
    }

    this.initFileupload=function(){
        layui.use('upload', function() {
            var $ = layui.jquery
                ,upload = layui.upload;

            //多图片上传
            upload.render({
                elem: '#addpic'
                ,url: '../uploadfile.do'
                ,multiple: true
                , auto: false
                , bindAction: '#uploadpic'
                ,choose: function(obj){
                    model.files = obj.pushFile();
                    //预读本地文件示例，不支持ie8
                    obj.preview(function(index, file, result){
                        $('#picset').prepend('<li id="'+index+'" class="weui-uploader__file" style="background-image:url('+result+')"></li>');
                    });
                }
                ,done: function(res, index, upload){
                        $('#'+index).addClass("weui-uploader__file_status");
                    if(res.code == 0){
                        $('#'+index).html('<div class="weui-uploader__file-content">' +
                            '<i class="weui-icon-success"></i>' +
                            '</div>');
                        model.user.picurl.push(res.filename);
                    }else{
                        $('#'+index).html('<div class="weui-uploader__file-content">' +
                            '<i class="weui-icon-warn"></i>' +
                            '</div>');
                    }
                }
                ,before: function(obj){ //obj参数包含的信息，跟 choose回调完全一致，可参见上文。
                    layer.load(); //上传loading
                }
                ,error: function(index, upload){
                    layer.closeAll('loading'); //关闭loading
                }
                ,allDone: function(obj){ //当文件全部被提交后，才触发
                    layer.closeAll('loading'); //关闭loading
                    //console.log(obj.total); //得到总文件数
                    //console.log(obj.successful); //请求成功的文件数
                    //console.log(obj.aborted); //请求失败的文件数
                    if(obj.total==obj.successful){
                        model.user.picurl=model.user.picurl.join("|");
                        that.urgent();//图片正常上传后调用方法上传表单数据
                    }else{
                        $.alert("网络错误...");
                    }
                }
            });
        });

    }
    //读取表单数据并上传
    this.urgent=function(){
        model.user.message=$("#message").val();
        var val=$('input:radio[name="radio1"]:checked').val();
        if(val==0){//选取了固定位置
            model.user.addresstype=0;
            model.user.placeaddress+="("+ $("#detil").val() +")";
            //console.log(model.user);
        }else{
            //清除固定位置数据
            model.user.addresstype=1;
            model.user.placeaddress="";
            model.user.placelatitude=null;
            model.user.placelongitude=null;
            //console.log(model.user);
        }
        service.urgentmessage(model.user);
    }
    //判断是否有文件需要上传
    this.clickurgent=function(){
        if(model.code!=null){
            if(model.flag){
                model.flag=false;
                if(model.files){
                    $("#uploadpic").click();
                }else{
                    model.user.picurl="";
                    that.urgent();
                }
            }else{
                alert("数据连接中，请勿重复点击");
            }
        }else{
            alert("非法访问！");
        }
    }
}

$(function(){
    var controller = new UrgentController();
    //初始化
    controller.init();  //通过code获取信息、验证签名
    //初始化地图弹框
    controller.initMap();
    //初始化图片上传
    controller.initFileupload();


    //选取固定位置点击事件
    $("#x12").on("click",function(){
        controller.showmap();
    });

    //实时追踪点击事件
    $("#x11").on("click",function(){
         $("#address").text("选取固定位置");
         $("#address_detil").hide();
         $("#detil").val("");
    });

    $("#urgent_button").on("click",function(){
        controller.clickurgent();
    });

})

