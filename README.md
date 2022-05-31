* 最新版集成了手动打卡和自动打卡方式（包较大）
* v2.0为手动打卡，需要和curlSH.sh文件一起使用实现自动打卡

# 最新版教程
1、安装Redis
拉取redis的镜像 
```
docker pull redis  
```

运行redis （请注意：命令行最后一部分，这里涉及到步骤4中对应的redis部分，密码请不要用简单密码或者不设置密码，容易被扫放病毒）
```
docker run --privileged=true --restart=always --name redis -p 6379:6379 -d redis redis-server --appendonly yes --requirepass "这里是你要设置的密码，双引号保留，如果不需要请连同--requirepass将之删除"
```

2、拉取镜像
```
docker pull yuanter/demo
```

3、下载配置application.yml文件
```
wget -O application.yml https://ghproxy.com/https://raw.githubusercontent.com/yuanter/shell/main/demo/application.yml
```

4、配置application.yml文件（需要注意本地redis和本地容器redis的host区别，本地默认127.0.0.1，docker默认redis,如果有公网直接填写公网）
```
vi application.yml
```

5、运行容器  
需要自行对应application.yml文件配置，如果redis不是本地docker运行的方式，请将下方的```--link redis:redis```删除  
```
docker run -d --privileged=true --restart=always  --name demo -p 8080:8080  -v $PWD/application.yml:/application.yml --link redis:redis yuanter/demo
```

6、浏览器访问路径
http://ip:8080


7、查看运行日志
```
docker logs --tail  300 -f  demo
```
或者
```
docker logs  demo
```

# 更新教程
1、删除容器
docker rm -f  demo

2、重新拉取镜像
docker pull yuanter/demo

3、后续教程，同使用教程第3步开始  



# V2.0
* 小米运动刷步数，同步微信运动和支付宝运动  

* 运行命令如下（8080:8080中第一个8080可自定义修改）：  
```
docker run -d --restart=always --name demo -p 8080:8080  yuanter/demo:2.0
```

* 页面输入打卡
```
http://ip:8080
```

* api接口
```
http://ip:8080/mi?phoneNumber=手机号码&password=密码&steps=步数
```


# 源码和curlSH.sh一起使用方式，定时打卡
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
    
