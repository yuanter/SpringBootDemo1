* 最新版集成了手动打卡和自动打卡方式（包较大）

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
