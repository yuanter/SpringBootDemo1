
function login(uin,pwd,steps){
    $('#load').html('正在打卡中，请稍等...');
    var request= $.ajax({
        //请求方式
        type:'GET',
        //发送请求的地址以及传输的数据
        url:"mi",
        data:{
            "phoneNumber":uin,
            "password":pwd,
            "steps":steps
        },
        async:false,
        success: function(data){
            $('#login').hide();
            $('#submit').hide();
            $('#load').html('<div class="alert alert-success">'+data+'</div>');
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

$(document).ready(function(){
    $('#submit').click(function(){
        var self=$(this);
        var uin=$('#uin').val(),
            pwd=$('#pwd').val(),
            steps=$('#steps').val();
        if(uin==''||pwd==''||steps=='') {
            alert("请确保每项不能为空！");
            return false;
        }

        $('#load').show();
        login(uin,pwd,steps);
    });

});