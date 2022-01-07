###小米运动刷步数，可同步支付宝运动+微信运动
##使用教程
1.先下载小米运动APP。使用手机号码注册，在APP中，在设置里找到第三方接入，绑定微信和支付宝（需要使用哪个就绑定哪个，QQ绑定不起作用）  
2.打开```javascript http://ip:8080 ```网站，提交手机号码和密码，以及步数，点击打卡按钮即可提交步数  



##关于源码和curlSH.sh一起使用，为linux服务器定时器
* 1、直接docker下载
```javascript
docker pull yuanter/demo:latest
```

* 2、运行demo（#8080:8080中第一个8080是自定义映射端口，自己可以随意改，记得开放端口）
```javascript
docker run -d --restart=always --name demo -p 8080:8080 yuanter/demo 
```

* 3、查看启动日志
```javascript
docker logs --tail 300 -f demo
```

* 4、下载脚本curlSH.sh
```javascript
wget https://ghproxy.com/https://raw.githubusercontent.com/yuanter/SpringBootDemo1/master/curlSH.sh && chmod +x curlSH.sh
```

* 5、修改curl.sh内容（一定要修改）
```javascript
vi curlSH.sh
```
参数说明  
* ip：为您部署的服务器ip  
* 手机号码：替换为您的小米运动APP账号，一定要绑定手机号码，使用手机  
* 密码：替换为您的小米运动APP密码  
* 范围步数和起始步数：随机打卡步数范围=起始步数+范围步数-1.如打卡范围是20000-22222，则起始步数为20000，范围步数为2223，即最大步数为20000+2223-1=22222  
* 例如我修改的部分
```javascript
curl http://119.29.xxx.xxx:8080/mi?phoneNumber=131xxxxxxx\&password=xxxxxxxx\&steps=$[$[RANDOM%2223]+20000]
```

* 6、开启定时任务
    * 1、先检查是否安装了crontab  
	```javascript
    which crontab
    ```
	* 2、如果未安装，则先安装，否之跳过该步骤    （PS：Ubuntu操作系统下请使用apt-get）
    ```javascript
	yum install vixie-cron
    yum install crontabs
    ```
	* 3、创建定时任务  
	```javascript
    sudo vi  /etc/crontab
    ```
	* 4、在编辑框按i添加以下定时任务（我设置为晚上18、20、22三个小时，同步三次。晚上12点至上午10点前不同步）
	corn表达式 用户名 脚本路径
    ```javascript
	0 18,20,22 * * * root /root/curlSH.sh
    ```
	* 5、按ESC，接着输入字符 ***:wq***保存
	* 6、开启crontab服务 
    ```javascript
	service crond start
    ```
* 7、做测试时，可以用这个表达式，表示1分钟执行一次任务，后续完毕再改回来
    ```javascript
    */1 * * * *  
    ```
    
    
###常见问题
* Q:我的账号安全吗？  
* A:本站不保存任何形式的密码。  
* Q:这个程序是干嘛的？  
* A:用来给你的支付宝或者微信刷步数的。  
* Q:使用这个会影响我的账号安全吗？  
* A:有影响，只要你刷的步数不过分（如98800、66666、88888等），一般不会被举报，即使举报封号，也是运动部分。  
* Q:不起作用该怎么办？  
* A:首先在早上10点前一般不起作用。其次在后面时间段打卡还是不起作用就重新绑定账号。  
