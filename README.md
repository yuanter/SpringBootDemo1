##关于源码和curlSH.sh一起使用
* 1、直接docker下载
```javascript
docker pull yuanter/demo:latest
```

* 2、运行demo（#8080:8080中第一个8080是自定义映射端口，自己可以随意改）
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
* 范围步数和起始步数：随机打卡步数范围=起始步数+范围步数-1.如打卡范围是20000-22222，则起始步数为20000，范围步数为2223，即最大步数为20000+2223-1=222222  
* 例如我修改的部分
```javascript
curl http://119.29.xxx.xxx:8080/mi?phoneNumber=131xxxxxxx\&password=xxxxxxxx\&steps=$[$[RANDOM%22223]+20000]
```

* 6、开启定时任务
    * 1、先检查是否安装了crontab  
	```javascript
    which crontab
    ```
	* 2、如果未安装，则先安装，否之跳过该步骤
    ```javascript
	yum install vixie-cron
    yum install crontabs
    ```
    （PS：Ubuntu操作系统下请使用apt-get）
	* 3、创建定时任务  
	```javascript
    sudo vi  /etc/crontab
    ```
	* 4、在编辑框按i添加以下定时任务（我设置为晚上18、20、22三个小时，同步三次）
	corn表达式 用户名 脚本路径
    ```javascript
	0 18,20,22 * * * root /root/curlSH.sh
    ```
	* 5、按ESC，接着输入字符***:wq***保存
	* 6、开启crontab服务 
    ```javascript
	service crond start
    ```
* 7、做测试时，可以用这个表达式，表示1分钟执行一次任务
    ```javascript
    */1 * * * *  
    ```
    
