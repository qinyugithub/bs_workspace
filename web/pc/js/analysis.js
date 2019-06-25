var AnalysisModel = function(){

};


var AnalysisService = function(model){
    var that = this;

    this.getHotMsg = function (openid,content) {
        var ret=null;
        $.ajax({
            url: "../getHotData.do",
            type: "POST",
            dataType:"json",
            async:false,
            data: {},
            success: function (result) {
                ret=result;
                console.log(ret)
            },
            error: function (xhr, status, p3, p4) {
                alert("获取热力图数据出错");
            }
        });
        return ret;
    }
};


var AnalysisController = function(){
    var model = new AnalysisModel();
    var service = new AnalysisService(model);//将model传到Service层
    var that=this;

    this.init = function(){
        // 创建地图
        var map = new qq.maps.Map(document.getElementById("content"), {
            center: new qq.maps.LatLng(30.531717,114.02881),
            zoom:15
        });
        // 创建热力图对象
        var heat = new qq.maps.visualization.Heat({
            map: map, // 必填参数，指定显示热力图的地图对象
            radius: 30, // 辐射半径，默认为20
        });
        // 获取热力数据
        var data = service.getHotMsg();
        //var data = getHeatData();
        // 向热力图传入数据
        heat.setData(data);

        // 监听button事件，更改热力图配置参数
        document.getElementById("setOptions").addEventListener("click", function(e) {
            var target = e.target;
            switch (target.id) {
                case "show":
                    if (heat.visible) {
                        heat.hide(); // 显示热力图
                    } else {
                        heat.show(); // 隐藏热力图
                    }
                    break;
                case "data":
                    data = getHeatData(10);
                    heat.setData(data); // 重置热力图数据
                    break;
                case "radius":
                    let radius = heat.getRadius(); // 获取辐射半径
                    heat.setRadius(radius + 10); // 设置辐射半径
                    break;
                case "gradient":
                    let gradient = heat.getGradient(); // 获取渐变色
                    gradient[1.0] = "#fff"; // 强度最大为白色
                    heat.setGradient(gradient); // 设置渐变色
                    break;
                case "opacity":
                    let opacity = heat.getOpacity();
                    opacity = [0.1, 0.8]; // 透明度变化范围为0.2～0.8
                    heat.setOpacity(opacity); // 设置透明度变化范围
                    break;
                case "destroy":
                    heat.destroy();
                default:
            }
        });

        function getHeatData(cnt, max, min) {
            let data = [];
            let center = {
                lat: 39.9,
                lng: 116.4
            };
            cnt = cnt || 100;
            max = max || 100;
            min = min || 0;
            for (let index = 0; index < cnt; index++) {
                let r = Math.random();
                let angle = Math.random() * Math.PI * 2;
                let heatValue = Math.random() * (max - min) + min;
                data.push({
                    lat: center.lat + r * Math.sin(angle),
                    lng: center.lng + r * Math.cos(angle),
                    value: 50
                });
            }
            var ret={
                max: max,
                min: min,
                data: data
            };
            console.log(ret);
            return {
                max: max,
                min: min,
                data: data
            };
        }
    }

    this.initlt=function(){
        var dom = document.getElementById("rt");
        var myChart = echarts.init(dom);
        var app = {};
        option = null;
        app.title = '多 Y 轴示例';

        var colors = ['#5793f3', '#d14a61', '#675bba'];

        option = {
            title: {
                top: 30,
                left: 'center',
                text: '昨日报警次数分时统计图'
            },
            color: colors,
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross'
                }
            },
            grid: {
                right: '20%'
            },
            toolbox: {
                feature: {
                    dataView: {show: true, readOnly: false},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            legend: {
                data:['男','女','权值']
            },
            xAxis: [
                {
                    type: 'category',
                    axisTick: {
                        alignWithLabel: true
                    },
                    data: ['0时','2时','4时','6时','8时','10时','12时','14时','16时','18时','20时','22时']
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    name: '男',
                    min: 0,
                    max: 250,
                    position: 'right',
                    axisLine: {
                        lineStyle: {
                            color: colors[0]
                        }
                    },
                    axisLabel: {
                        formatter: '{value}次'
                    }
                },
                {
                    type: 'value',
                    name: '女',
                    min: 0,
                    max: 250,
                    position: 'right',
                    offset: 80,
                    axisLine: {
                        lineStyle: {
                            color: colors[1]
                        }
                    },
                    axisLabel: {
                        formatter: '{value} 次'
                    }
                },
                {
                    type: 'value',
                    name: '权重',
                    min: 0,
                    max: 25,
                    position: 'left',
                    axisLine: {
                        lineStyle: {
                            color: colors[2]
                        }
                    },
                    axisLabel: {
                        formatter: '{value}'
                    }
                }
            ],
            series: [
                {
                    name:'男',
                    type:'bar',
                    data:[2.0, 4.9, 7.0, 23.2, 25.6, 76.7, 135.6, 162.2, 32.6, 20.0, 6.4, 3.3]
                },
                {
                    name:'女',
                    type:'bar',
                    yAxisIndex: 1,
                    data:[2.6, 5.9, 9.0, 26.4, 28.7, 70.7, 175.6, 182.2, 48.7, 18.8, 6.0, 2.3]
                },
                {
                    name:'权值',
                    type:'line',
                    yAxisIndex: 2,
                    data:[2.0, 2.2, 3.3, 4.5, 6.3, 10.2, 20.3, 23.4, 23.0, 16.5, 12.0, 6.2]
                }
            ]
        };
        ;
        if (option && typeof option === "object") {
            myChart.setOption(option, true);
        }
    }

    this.initlb=function(){
        var dom = document.getElementById("rbl");
        var myChart = echarts.init(dom);
        var app = {};
        option = null;
        option = {
            title: {
                top: 20,
                left: 'center',
                text: '2019年性别和报警类型关系统计图'
            },
            tooltip: {},
            radar: {
                // shape: 'circle',
                name: {
                    textStyle: {
                        color: '#fff',
                        backgroundColor: '#999',
                        borderRadius: 3,
                        padding: [3, 5]
                    }
                },
                indicator: [
                    { name: '刑事案件', max: 6500},
                    { name: '治安问题', max: 16000},
                    { name: '个人求助', max: 30000},
                    { name: '公共安全', max: 38000},
                    { name: '突发事件', max: 52000},
                    { name: '其他问题', max: 25000}
                ]
            },
            series: [{
                name: '男 vs 女',
                type: 'radar',
                // areaStyle: {normal: {}},
                data : [
                    {
                        value : [4300, 10000, 28000, 35000, 50000, 19000],
                        name : '男'
                    },
                    {
                        value : [5000, 14000, 28000, 31000, 42000, 21000],
                        name : '女'
                    }
                ]
            }]
        };;
        if (option && typeof option === "object") {
            myChart.setOption(option, true);
        }
    }

    this.initrbr=function(){
        var dom = document.getElementById("rbr");
        var myChart = echarts.init(dom);
        var app = {};
        option = null;
        app.title = '环形图';

        option = {
            title: {
                top: 20,
                left: 'center',
                text: '类型统计图'
            },
            tooltip: {
                trigger: 'item',
                formatter: "{a} <br/>{b}: {c} ({d}%)"
            },
            legend: {
                orient: 'vertical',
                x: 'left',
                data:['刑事案件','治安问题','个人求助','公共安全','其他']
            },
            series: [
                {
                    name:'访问来源',
                    type:'pie',
                    radius: ['50%', '70%'],
                    avoidLabelOverlap: false,
                    label: {
                        normal: {
                            show: false,
                            position: 'center'
                        },
                        emphasis: {
                            show: true,
                            textStyle: {
                                fontSize: '30',
                                fontWeight: 'bold'
                            }
                        }
                    },
                    labelLine: {
                        normal: {
                            show: false
                        }
                    },
                    data:[
                        {value:335, name:'刑事案件'},
                        {value:310, name:'治安问题'},
                        {value:234, name:'个人求助'},
                        {value:135, name:'公共安全'},
                        {value:1548, name:'其他'}
                    ]
                }
            ]
        };
        ;
        if (option && typeof option === "object") {
            myChart.setOption(option, true);
        }
    }

}

$(function(){
    if(sessionStorage.getItem('checked')==null||sessionStorage.getItem('checked').toString()=="false"){
        alert("请登录后重试！");
        window.location.href = "../";
    }else{
        $("#analysisbody").show();
        var controller = new AnalysisController();
        //初始化
        controller.init();
        controller.initlt();
        controller.initlb();
        controller.initrbr();
    }

})
