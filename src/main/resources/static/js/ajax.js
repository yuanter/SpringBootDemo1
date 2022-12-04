//默认不开启保存
var isSaveParam = "false";
//打卡类型
var mode = "noSave";
//加载是否启用保存功能页面
function onloadIsShow() {
    $.ajax({
        //请求方式
        type:'GET',
        //发送请求的地址以及传输的数据
        url:"isShow",
        success: function(data){
            //$('#query').hide();
            //$('#submit').hide();
            if(data.code==0){
                if (data.data) {
                    if (data.data.flag == "true" || data.data.flag){
                        //启用tip
                        $("#tipId").show();
                        //修改tip
                        $("#tipData").html(data.data.tip);
                        //启用是否打开功能
                        $("#isShowDiv").show();
                    }else {
                        //启用tip
                        $("#tipId").hide();
                        //启用是否打开功能
                        $("#isShowDiv").hide();
                    }
                }
            }else if(data.code == -1){
                $.globalMessenger().post({
                    message: data.msg,//提示信息
                    type: 'error',//消息类型。error、info、success
                    hideAfter: 5,//多长时间消失
                    showCloseButton:true,//是否显示关闭按钮
                    hideOnNavigate: true //是否隐藏导航
                });
                //$('#load').html('查询失败！查询信息如下：<br/> '+data.msg+'<br/><a href="./">返回输入查询</a>');
            }else {
                $.globalMessenger().post({
                    message: "未知查询结果...请重试",//提示信息
                    type: 'error',//消息类型。error、info、success
                    hideAfter: 2,//多长时间消失
                    showCloseButton:true,//是否显示关闭按钮
                    hideOnNavigate: true //是否隐藏导航
                });
                //$('#load').html('未知查询结果...请重试<br/><a href="./">返回输入查询</a>');
            }

        },
        error:function(jqXHR){
            $('#load').html('查询失败！');
            //请求失败函数内容
            console.log('错误原因：',jqXHR);
            $.globalMessenger().post({
                message: "未知查询结果...请重试",//提示信息
                type: 'error',//消息类型。error、info、success
                hideAfter: 2,//多长时间消失
                showCloseButton:true,//是否显示关闭按钮
                hideOnNavigate: true //是否隐藏导航
            });
        },
        failure:function (result) {
            console.log('失败原因：',result);
            $.globalMessenger().post({
                message: "未知查询结果...请重试",//提示信息
                type: 'error',//消息类型。error、info、success
                hideAfter: 2,//多长时间消失
                showCloseButton:true,//是否显示关闭按钮
                hideOnNavigate: true //是否隐藏导航
            });
        },
    });
}

function login(uin,pwd,mode,steps,minSteps,maxSteps){
    $('#load').html('正在打卡中，请稍等...');
    var request= $.ajax({
        //请求方式
        type:'GET',
        //发送请求的地址以及传输的数据
        url:"mi",
        data:{
            "username":uin,
            "password":pwd,
            "steps":steps,
            "mode":mode,
            "minSteps":minSteps,
            "maxSteps":maxSteps,
            "isSave":isSaveParam
        },
        async:false,
        success: function(data){
            $('#login').hide();
            $('#submit').hide();
            if(data.code==0) {
                $('#load').html('<div class="alert alert-success">' + data.data + '<br/><a href="./">返回首页</a></div>');
            }else if(data.code == -1){
                $.globalMessenger().post({
                    message: data.msg,//提示信息
                    type: 'error',//消息类型。error、info、success
                    hideAfter: 5,//多长时间消失
                    showCloseButton:true,//是否显示关闭按钮
                    hideOnNavigate: true //是否隐藏导航
                });
                $('#load').html(data.msg+'<br/><a href="./">返回首页</a>');
            }else {
                $.globalMessenger().post({
                    message: "未知查询结果...请重试",//提示信息
                    type: 'error',//消息类型。error、info、success
                    hideAfter: 2,//多长时间消失
                    showCloseButton:true,//是否显示关闭按钮
                    hideOnNavigate: true //是否隐藏导航
                });
                $('#load').html('未知查询结果...请重试<br/><a href="./">返回首页</a>');
            }
        },
        error:function(jqXHR){
            $('#load').html('打卡失败！');
            //请求失败函数内容
            console.log('错误原因：',jqXHR);
        },
        failure:function (result) {
            console.log('失败原因：',result);
        },
    });
    //终止请求动作.
    request.abort();
}

//是否启用保存功能
function isSave(data){
    //点击是否保存
    if (data == 1){
        $('#isSaveBt').html($('#save').html()+"<span class=\"caret\"></span>");
        isSaveParam = true;
        //启用最小和最大步数，关闭步数
        $("#minStepsDiv").show();
        $("#maxStepsDiv").show();
        $("#stepsDiv").hide();
        mode = "save";
    } else if (data == 0){
        $('#isSaveBt').html($('#noSave').html()+"<span class=\"caret\"></span>");
        //关闭最小和最大步数，开启步数
        $("#minStepsDiv").hide();
        $("#maxStepsDiv").hide();
        $("#stepsDiv").show();
        mode = "noSave";
    } else {
        $('#isSaveBt').html("出错了，获取未知选项");
    }



}


$(document).ready(function(){
    //自动加载是否启用保存功能
    onloadIsShow();
    //打卡
    $('#submit').click(function(){
        var self=$(this);
        var uin=$('#uin').val(),
            pwd=$('#pwd').val(),
            steps=$('#steps').val(),
            minSteps=$('#minSteps').val(),
            maxSteps=$('#maxSteps').val();
        if(uin==''||pwd=='') {
            alert("请确保每项不能为空！");
            return false;
        }

        if(isSaveParam == ''||isSaveParam == "false") {
            if (steps == ""){
                alert("请确保每项不能为空！");
                return false;
            }
        }else {
            if (minSteps == "" || maxSteps == ""){
                alert("请确保每项不能为空！");
                return false;
            }
        }

        $('#load').show();
        login(uin,pwd,mode,steps,minSteps,maxSteps);
    });



});