var NavigationModel = function(){
    this.NavigationServiceId = 'selectListServiceId';//��ѯ���serviceId
};

var NavigationService = function(model){
    var that = this;

    this.loadGrid = function(){

    }
};

/**
 *Xxx���Ʋ㣬�ò���Ҫ�����߼�����
 */
var NavigationController = function(){
    var model = new NavigationModel();
    var service = new NavigationService(model);//��model����Service��

    this.init = function(){
        //��ʼ��������
        layui.use('element', function(){
            var element = layui.element; //������hoverЧ���������˵��ȹ��ܣ���Ҫ����elementģ��
            //�����������
            element.on('nav(demo)', function(elem){
                console.log("ok")
                //layer.msg(elem.text());
            });
        });
    }

}


$(function(){
    var controller = new NavigationController();
    //��ʼ��
    controller.init();

    //xx����¼�
/*    $("#xx").on(click,function(){
        //�����¼���Ҫ����Ĵ����߼�д��controller��
        controller.xxClick();
    });*/

})