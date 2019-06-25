var WebgisModel = function () {
    this.socket=null;
    this.map=null;
    this.markersArray=[]; //用户移动的所有坐标点对象
    this.markerIcon=null;
    this.cmarker=null;
};

var WebgisService = function (model) {
    var that = this;

    this.sendMsgToUser = function (openid,content) {
        $.ajax({
            url: "../postMessageToUser.do",
            type: "POST",
            dataType:"json",
            async:false,
            data: { openid: openid,content:content },
            success: function (result) {
              //console.log(result);
            },
            error: function (xhr, status, p3, p4) {
                alert("消息回复出错");
            }
        });
    }

    this.getUrgentMsgById=function(id){
        var rest=null;
        $.ajax({
            url: "../getUrgentMsgById.do",
            type: "GET",
            dataType:"json",
            async:false,
            data: { id: id },
            success: function (result) {
               rest=result;
            },
            error: function (xhr, status, p3, p4) {
                alert("请求数据失败");
            }
        });
        return rest;
    }

};

var WebgisController = function () {
    var model = new WebgisModel();
    var service = new WebgisService(model);//将model传到Service层
    var that=this;
    //初始化消息列表
    this.initmessagebox = function () {
        layui.use(['layim', 'flow'], function () {
            var layim = layui.layim
                , layer = layui.layer
                , laytpl = layui.laytpl
                , $ = layui.jquery
                , flow = layui.flow;

            var cache = {}; //用于临时记录请求到的数据
            //请求消息
            var renderMsg = function (page, callback) {

                //实际部署时，请将下述 getmsg.json 改为你的接口地址
                $.ajax({
                    url: "../getUrgentMsg.do",
                    type: "POST",
                    dataType:"json",
                    async:false,
                    data: {},
                    success: function (res) {
                        if(res==false){
                            window.location.href = "../login.html";
                        }
                        if (res.code != 0) {
                            return layer.msg(res.msg);
                        }
                        //记录来源用户信息
                        layui.each(res.data, function (index, item) {
                            cache[item.from] = item.user;
                        });
                        callback && callback(res.data, res.pages);
                    },
                    error: function (xhr, status, p3, p4) {
                        alert("消息列表获取出错");
                    }
                });
            };
            //消息信息流
            flow.load({
                elem: '#LAY_view' //流加载容器
                , isAuto: false
                , end: '<li class="layim-msgbox-tips">暂无更多新消息</li>'
                , done: function (page, next) { //加载下一页
                    renderMsg(page, function (data, pages) {
                        var html = laytpl(LAY_tpl.value).render({
                            data: data
                            , page: page
                        });
                        next(html, page < pages);
                    });
                }
            });

        });
    }
    //初始化地图
    this.initmap = function () {
        var map = new qq.maps.Map(document.getElementById('map'),{
            center: new qq.maps.LatLng(39.916527,116.397128),      // 地图的中心地理坐标。
            zoom:18
        });
        model.map=map;
        var scaleControl = new qq.maps.ScaleControl({  //添加比例尺
            align: qq.maps.ALIGN.BOTTOM_LEFT,
            margin: qq.maps.Size(85, 15),
            map: map
        });
        var anchor = new qq.maps.Point(12, 12), //自定义标注图标
            size = new qq.maps.Size(24, 24),
            origin = new qq.maps.Point(0, 0);
            model.markerIcon = new qq.maps.MarkerImage(
                "images/center.gif",
                size,
                origin,
                anchor
            );
    }

    //初始化layim
    this.initLayim=function(){
        layui.use('layim', function(){
            var layim = layui.layim;

            //演示自动回复
            var autoReplay = [
                '您好，我现在有事不在，一会再和您联系。',
                '你没发错吧？face[微笑] ',
                '洗澡中，请勿打扰，偷窥请购票，个体四十，团体八折，订票电话：一般人我不告诉他！face[哈哈] ',
                '你好，我是主人的美女秘书，有什么事就跟我说吧，等他回来我会转告他的。face[心] face[心] face[心] ',
                'face[威武] face[威武] face[威武] face[威武] ',
                '<（@￣︶￣@）>',
                '你要和我说话？你真的要和我说话？你确定自己想说吗？你一定非说不可吗？那你说吧，这是自动回复。',
                'face[黑线]  你慢慢说，别急……',
                '(*^__^*) face[嘻嘻] ，是贤心吗？'
            ];

            //基础配置
            layim.config({
                //初始化接口
                init: {
                    url: '../../otherui/layui/css/modules/layim/json/getList.json'
                    ,data: {}
                }
                //查看群员接口
                ,members: {
                    url: '../../otherui/layui/css/modules/layim/json/getMembers.json'
                    ,data: {}
                }

                ,uploadImage: {
                    url: '' //（返回的数据格式见下文）
                    ,type: '' //默认post
                }
                ,uploadFile: {
                    url: '' //（返回的数据格式见下文）
                    ,type: '' //默认post
                }

                ,isAudio: true //开启聊天工具栏音频
                ,isVideo: true //开启聊天工具栏视频

                //扩展工具栏
                ,tool: [{
                    alias: 'code'
                    ,title: '代码'
                    ,icon: '&#xe64e;'
                }]

                //,brief: true //是否简约模式（若开启则不显示主面板）

                ,title: '警务消息列表' //自定义主面板最小化时的标题
                //,right: '100px' //主面板相对浏览器右侧距离
                //,minRight: '90px' //聊天面板最小化时相对浏览器右侧距离
                ,initSkin: '3.jpg' //1-5 设置初始背景
                //,skin: ['aaa.jpg'] //新增皮肤
                ,isfriend: false //是否开启好友
                ,isgroup: false //是否开启群组
                //,min: true //是否始终最小化主面板，默认false
                //,notice: true //是否开启桌面消息提醒，默认false
                //,voice: false //声音提醒，默认开启，声音文件为：default.mp3

              //  ,msgbox: '../../otherui/layui/css/modules/layim/html/msgbox.html' //消息盒子页面地址，若不开启，剔除该项即可
              //  ,find: '../../otherui/layui/css/modules/layim/html/find.html' //发现页面地址，若不开启，剔除该项即可
             //   ,chatLog: '../../otherui/layui/css/modules/layim/html/chatlog.html' //聊天记录页面地址，若不开启，剔除该项即可

            });
            //监听在线状态的切换事件
            layim.on('online', function(status){
                layer.msg(status);
            });

            //监听签名修改
            layim.on('sign', function(value){
                layer.msg(value);
            });
            //监听自定义工具栏点击，以添加代码为例
            layim.on('tool(code)', function(insert){
                layer.prompt({
                    title: '插入代码 - 工具栏扩展示例'
                    ,formType: 2
                    ,shade: 0
                }, function(text, index){
                    layer.close(index);
                    insert('[pre class=layui-code]' + text + '[/pre]'); //将内容插入到编辑器
                });
            });

            //监听layim建立就绪
            layim.on('ready', function(res){
                //console.log(res.mine);
                layim.msgbox(5); //模拟消息盒子有新消息，实际使用时，一般是动态获得
            });
            //监听发送消息
            layim.on('sendMessage', function(data){
                var To = data.to;
                console.log(data);
                var content=data.mine.content;
                var openid=data.to.id;
                service.sendMsgToUser(openid,content);

                //演示自动回复
              /*  setTimeout(function(){
                    var obj = {};
                    if(To.type === 'group'){
                        obj = {
                            username: '模拟群员'+(Math.random()*100|0)
                            ,avatar: layui.cache.dir + 'images/face/'+ (Math.random()*72|0) + '.gif'
                            ,id: To.id
                            ,type: To.type
                            ,content: autoReplay[Math.random()*9|0]
                        }
                    } else {
                        obj = {
                            username: To.name
                            ,avatar: To.avatar
                            ,id: To.id
                            ,type: To.type
                            ,content: autoReplay[Math.random()*9|0]
                        }
                        layim.setChatStatus('<span style="color:#FF5722;">在线</span>');
                    }
                    layim.getMessage(obj);
                }, 1000);*/
            });
            //监听查看群员
            layim.on('members', function(data){
                //console.log(data);
            });

            //监听聊天窗口的切换
            layim.on('chatChange', function(res){
                var type = res.data.type;
                console.log(res.data.id)
                if(type === 'friend'){
                    //模拟标注好友状态
                    //layim.setChatStatus('<span style="color:#FF5722;">在线</span>');
                } else if(type === 'group'){
                    //模拟系统消息
                    layim.getMessage({
                        system: true
                        ,id: res.data.id
                        ,type: "group"
                        ,content: '模拟群员'+(Math.random()*100|0) + '加入群聊'
                    });
                }
            });


            //面板外的操作
            var $ = layui.jquery, active = {
                chat: function(){
                    //自定义会话
                    layim.chat({
                        name: '小闲'
                        ,type: 'friend'
                        ,avatar: '//tva3.sinaimg.cn/crop.0.0.180.180.180/7f5f6861jw1e8qgp5bmzyj2050050aa8.jpg'
                        ,id: 1008612
                    });
                    layer.msg('也就是说，此人可以不在好友面板里');
                }
                ,message: function(){
                    //制造好友消息
                    layim.getMessage({
                        username: "贤心"
                        ,avatar: "http://img.027cgb.com/609312/main/a2.jpg"
                        ,id: "100001"
                        ,type: "friend"
                        ,content: "嗨，你好！欢迎体验LayIM。演示标记："+ new Date().getTime()
                        ,timestamp: new Date().getTime()
                    });
                }
                ,messageAudio: function(){
                    //接受音频消息
                    layim.getMessage({
                        username: "林心如"
                        ,avatar: "//tp3.sinaimg.cn/1223762662/180/5741707953/0"
                        ,id: "76543"
                        ,type: "friend"
                        ,content: "audio[http://gddx.sc.chinaz.com/Files/DownLoad/sound1/201510/6473.mp3]"
                        ,timestamp: new Date().getTime()
                    });
                }
                ,messageVideo: function(){
                    //接受视频消息
                    layim.getMessage({
                        username: "林心如"
                        ,avatar: "//tp3.sinaimg.cn/1223762662/180/5741707953/0"
                        ,id: "76543"
                        ,type: "friend"
                        ,content: "video[http://www.w3school.com.cn//i/movie.ogg]"
                        ,timestamp: new Date().getTime()
                    });
                }
                ,messageTemp: function(){
                    //接受临时会话消息
                    layim.getMessage({
                        username: "小酱"
                        ,avatar: "//tva1.sinaimg.cn/crop.7.0.736.736.50/bd986d61jw8f5x8bqtp00j20ku0kgabx.jpg"
                        ,id: "198909151014"
                        ,type: "friend"
                        ,content: "临时："+ new Date().getTime()
                    });
                }
                ,add: function(){
                    //实际使用时数据由动态获得
                    layim.add({
                        type: 'friend'
                        ,username: '麻花疼'
                        ,avatar: '//tva1.sinaimg.cn/crop.0.0.720.720.180/005JKVuPjw8ers4osyzhaj30k00k075e.jpg'
                        ,submit: function(group, remark, index){
                            layer.msg('好友申请已发送，请等待对方确认', {
                                icon: 1
                                ,shade: 0.5
                            }, function(){
                                layer.close(index);
                            });

                        }
                    });
                }
                ,addqun: function(){
                    layim.add({
                        type: 'group'
                        ,username: 'LayIM会员群'
                        ,avatar: '//tva2.sinaimg.cn/crop.0.0.180.180.50/6ddfa27bjw1e8qgp5bmzyj2050050aa8.jpg'
                        ,submit: function(group, remark, index){
                            layer.msg('申请已发送，请等待管理员确认', {
                                icon: 1
                                ,shade: 0.5
                            }, function(){
                                layer.close(index);
                            });

                        }
                    });
                }
                ,addFriend: function(){
                    var user = {
                        type: 'friend'
                        ,id: 1234560
                        ,username: '李彦宏' //好友昵称，若申请加群，参数为：groupname
                        ,avatar: '//tva4.sinaimg.cn/crop.0.0.996.996.180/8b2b4e23jw8f14vkwwrmjj20ro0rpjsq.jpg' //头像
                        ,sign: '全球最大的中文搜索引擎'
                    }
                    layim.setFriendGroup({
                        type: user.type
                        ,username: user.username
                        ,avatar: user.avatar
                        ,group: layim.cache().friend //获取好友列表数据
                        ,submit: function(group, index){
                            //一般在此执行Ajax和WS，以通知对方已经同意申请
                            //……

                            //同意后，将好友追加到主面板
                            layim.addList({
                                type: user.type
                                ,username: user.username
                                ,avatar: user.avatar
                                ,groupid: group //所在的分组id
                                ,id: user.id //好友ID
                                ,sign: user.sign //好友签名
                            });

                            layer.close(index);
                        }
                    });
                }
                ,addGroup: function(){
                    layer.msg('已成功把[Angular开发]添加到群组里', {
                        icon: 1
                    });
                    //增加一个群组
                    layim.addList({
                        type: 'group'
                        ,avatar: "//tva3.sinaimg.cn/crop.64.106.361.361.50/7181dbb3jw8evfbtem8edj20ci0dpq3a.jpg"
                        ,groupname: 'Angular开发'
                        ,id: "12333333"
                        ,members: 0
                    });
                }
                ,removeFriend: function(){
                    layer.msg('已成功删除[凤姐]', {
                        icon: 1
                    });
                    //删除一个好友
                    layim.removeList({
                        id: 121286
                        ,type: 'friend'
                    });
                }
                ,removeGroup: function(){
                    layer.msg('已成功删除[前端群]', {
                        icon: 1
                    });
                    //删除一个群组
                    layim.removeList({
                        id: 101
                        ,type: 'group'
                    });
                }
                //置灰离线好友
                ,setGray: function(){
                    layim.setFriendStatus(168168, 'offline');

                    layer.msg('已成功将好友[马小云]置灰', {
                        icon: 1
                    });
                }
                //取消好友置灰
                ,unGray: function(){
                    layim.setFriendStatus(168168, 'online');

                    layer.msg('成功取消好友[马小云]置灰状态', {
                        icon: 1
                    });
                }
                //移动端版本
                ,mobile: function(){
                    var device = layui.device();
                    var mobileHome = '/layim/demo/mobile.html';
                    if(device.android || device.ios){
                        return location.href = mobileHome;
                    }
                    var index = layer.open({
                        type: 2
                        ,title: '移动版演示 （或手机扫右侧二维码预览）'
                        ,content: mobileHome
                        ,area: ['375px', '667px']
                        ,shadeClose: true
                        ,shade: 0.8
                        ,end: function(){
                            layer.close(index + 2);
                        }
                    });
                    layer.photos({
                        photos: {
                            "data": [{
                                "src": "http://cdn.layui.com/upload/2016_12/168_1481056358469_50288.png",
                            }]
                        }
                        ,anim: 0
                        ,shade: false
                        ,success: function(layero){
                            layero.css('margin-left', '350px');
                        }
                    });
                }
            };
            $('.site-demo-layim').on('click', function(){
                var type = $(this).data('type');
                active[type] ? active[type].call(this) : '';
            });
        });
    }

    //初始化websocket
    this.initWebSocket=function(){
        var socket;
        if(typeof(WebSocket) == "undefined") {
            alert("您的浏览器不支持WebSocket");
            return;
        }

       // $("#btnConnection").click(function() {
            //实现化WebSocket对象，指定要连接的服务器地址与端口
            socket = new WebSocket("wss://www.qinyu.online/wss/qinyu");
            //打开事件
            socket.onopen = function() {
                layui.use('layer', function () {
                    layer.msg('websocket已连接', {
                        time: 1000, //1s后自动关闭
                    });
                });
            };
            //获得消息事件
            socket.onmessage = function(msg) {
                var msgoj = JSON.parse(msg.data);
                console.log(msgoj);
                if(msgoj.messagetype=="location"){
                    //用户位置推送
                    console.log(msgoj);
                    that.drawMarker(msgoj.longitude,msgoj.latitude);
                }else if(msgoj.messagetype=="newUrgentMsg"){
                    $("#newmsgicon").show();
                }else if(msgoj.messagetype=="message"){
                    //用户消息推送
                    var layim = layui.layim;
                    layui.use('layim', function(){
                        layim.getMessage({
                            username: msgoj.nickname
                            ,avatar: msgoj.headimgurl
                            ,id: msgoj.openid
                            ,type: "friend"
                            ,content: msgoj.content
                            ,timestamp: new Date().getTime()
                        });
                    });
                }


            };
            //关闭事件
            socket.onclose = function() {
                alert("Socket已关闭");
            };
            //发生了错误事件
            socket.onerror = function() {
                alert("websocket连接发生错误");
            }
       // });

        $(window).unload(function(){
            socket.close();
        })

    }

    //利用websocket返回的坐标实时绘制地图
    this.drawMarker=function(yuanlongtitude,yuanlatitude){
        var longtitude=(parseFloat(yuanlongtitude)+0.00558).toFixed(6);
        var latitude=(parseFloat(yuanlatitude)-0.002196).toFixed(6);
        if(model.map){
            console.log("开始绘制"+longtitude+","+latitude);
            console.log(model.markersArray.length);
            var point = new qq.maps.LatLng(latitude,longtitude);
            var marker = new qq.maps.Marker({  //标注对象
                position: point,
                map: model.map
            });
            marker.setIcon(model.markerIcon);//设置标注样式
            if(model.cmarker){               //清除cmarker
                model.cmarker.setMap(null);
            }
            if (model.markersArray.length!==0) {   //清除覆盖层
                model.markersArray[model.markersArray.length-1].setMap(null);
            }else{
                model.map.panTo(new qq.maps.LatLng(latitude,longtitude)); //改变地图中心
            }
            model.markersArray.push(marker);
            marker.setMap(model.map);
            that.refreshview(yuanlongtitude,yuanlatitude);//更新界面显示
        }
    }
    //更新界面表单样式
    this.refreshview=function(longtitude,latitude){
        $("#longtitude").val(longtitude);
        $("#latitude").val(latitude);
    }

    //通过id获取详细信息
    this.getUrgentMsgByid=function(id){
        if(id){
            var msg=service.getUrgentMsgById(id);

            $("#headimage").attr("src",msg.headimgurl);
            $("#nickname").text(msg.nickname);
            $("#currentloaction").text("["+parseFloat(msg.clatitude).toFixed(6)+","+parseFloat(msg.clongitude).toFixed(6)+"]");
            $("#xq").text(msg.detil);
            $("#addresstype").text(msg.addresstype);
            $("#placelocation").text("["+msg.placelatitude+","+msg.placelongitude+"]");
            $("#placeaddress").text(msg.placeaddress);

            $("#pictureitem").empty();
            var picarr=msg.picurl.split("|");
            if(picarr.length!=0&&picarr[0]!=""){
                picarr.forEach(function(value,index){
                    var webpicurl="https://www.qinyu.online/images/"+value
                    //var webpicurl="http://localhost:8088/wxgongzhonghao/images/"+value
                    $("#pictureitem").append('<a href='+webpicurl+' target="_blank"><img class="pic" src='+webpicurl+'></a>')
                })
            }

            var clon=(parseFloat(msg.clongitude)+0.00558).toFixed(6);
            var clat=(parseFloat(msg.clatitude)-0.002196).toFixed(6);
            if(model.cmarker){
                model.cmarker.setMap(null);//清除覆盖物
            }
            if (model.markersArray.length!==0) {   //清除实时追踪覆盖层
                model.markersArray[model.markersArray.length - 1].setMap(null);
                model.markersArray=[];
            }
            //在图中绘制用户的报警位置
            model.map.panTo(new qq.maps.LatLng(clat,clon)); //改变地图中心

            var point = new qq.maps.LatLng(clat,clon);
            var marker = new qq.maps.Marker({  //标注对象
                position: point,
                map: model.map
            });
            marker.setIcon(model.markerIcon);//设置标注样式
            model.cmarker=marker;
            marker.setMap(model.map);
            that.refreshview(msg.clongitude,msg.clatitude);//更新界面显示
        }
    }

}

$(function () {
    if(sessionStorage.getItem('checked')==null||sessionStorage.getItem('checked').toString()=="false"){
        alert("请登录后重试！");
        window.location.href = "../";
    }else{
        $("#webgisbody").show();
        var controller = new WebgisController();
        //初始化
        controller.initmessagebox();   //初始化消息盒子
        controller.initmap();          //初始化地图
        controller.initLayim();        //初始化layim
        controller.initWebSocket();    //初始化websocket

        //详情按钮点击事件
        $("#message").on("click",function(e){
            if(e.target.innerText=='详情'){
                controller.getUrgentMsgByid(e.target.id);
            }else if(e.target.innerText=='完成'){
                alert(e.target.id);
            }
        });
    }

})
