# 常用命令

- **帮助命令**

  ```sh
  docker version	# 版本号
  docker info		#docker 版本详细信息
  docker --help	#查看其它命令
  ```

- **镜像命令**

  ```sh
  docker images [OPTIONS]		#列出本地主机上的镜像
  	OPTIONS说明：		-a :列出本地所有的镜像（含中间映像层）
  						-q :只显示镜像ID。
  						--digests :显示镜像的摘要信息
  						--no-trunc :显示完整的镜像信息
  
  docker search [OPTIONS] 镜像名字
  	OPTIONS说明：		--no-trunc : 显示完整的镜像描述
  						-s : 列出收藏数不小于指定值的镜像。
  						--automated : 只列出 automated build类型的镜像；
  
  docker pull 镜像名字[:TAG]
  #例:
  docker pull redis  #下载最新
  docker pull redis:6.2.4
  
  docker rmi 某个XXX镜像名字ID		#删除镜像
  docker rmi  -f 镜像ID 			#删除单个
  docker rmi -f 镜像名1:TAG 镜像名2:TAG 	#删除多个	
  docker rmi -f $(docker images -qa)	#删除全部	
  ```
  
- **容器命令**

  ```sh
  docker run [OPTIONS] IMAGE [COMMAND] [ARG...]	#/新建并启动容器
  
  docker run --name=mynginx   -d  --restart=always -p  88:80   nginx
  ```

  >  OPTIONS说明（常用）：有些是一个减号，有些是两个减号
  > 		--name="容器新名字": 为容器指定一个名称；
  > 		-d: 后台运行容器，并返回容器ID，也即启动守护式容器；
  > 		-i：以交互模式运行容器，通常与 -t 同时使用；
  > 		-t：为容器重新分配一个伪输入终端，通常与 -i 同时使用；
  > 		-P: 随机端口映射；
  > 		-p: 指定端口映射，有以下四种格式
  > 		      ip:hostPort:containerPort
  > 		      ip::containerPort
  > 		      hostPort:containerPort
  > 		      containerPort
  > 		 --restart=always: 开机自启

  ```sh
  docker ps [OPTIONS]		#列出当前所有正在运行的容器
  ```

  > OPTIONS说明（常用）：
  > 	-a :列出当前所有正在运行的容器+历史上运行过的
  > 	-l :显示最近创建的容器。
  > 	-n：显示最近n个创建的容器。
  > 	-q :静默模式，只显示容器编号。
  > 	--no-trunc :不截断输出。

  ```sh
  exit	#容器停止退出
  ctrl+P+Q	#容器不停止退出
  docker start 容器ID或者容器名		#启动容器
  docker restart 容器ID或者容器名	#重启容器
  docker stop 容器ID或者容器名		#停止容器
  docker kill 容器ID或者容器名		#强制停止容器
  docker rm 容器ID					#删除已停止的容器
  docker rm -f $(docker ps -a -q)		#一次性删除多个容器
  docker ps -a -q | xargs docker rm 	#同上，一次性删除多个容器 
  ```

  ```sh
  #应用开机自启
  docker update 容器id/名字 --restart=always
  
  #重要  排错
  docker logs -f -t --tail 容器ID		#查看容器日志
  		#   -t 是加入时间戳
  		#   -f 跟随最新的日志打印
  		#   --tail 数字 显示最后多少条
  		
  docker top 容器ID					#查看容器内运行的进程
  docker inspect 容器ID				#查看容器内部细节
  ```

  ```sh
  #进入正在运行的容器并以命令行交互
  docker exec -it 容器ID /bin/bash
  docker attach 容器ID
  ```

  > 上述两个区别
  > `attach `直接进入容器启动命令的终端，不会启动新的进程
  > `exec `是在容器中打开新的终端，并且可以启动新的进程

- 挂载数据到外部修改

  ```sh
  docker run --name=mynginx   \
  -d  --restart=always \
  -p  88:80 -v /data/html:/usr/share/nginx/html:ro  \
  nginx
  
  # 修改页面只需要去 主机的 /data/html
  ```

- 提交改变

  > 将自己修改好的镜像提交

  ```shell
  #commit一个自定义的容器到本地镜像库
  #docker commit -m=“提交的描述信息” -a=“作者” 容器ID 要创建的目标镜像名:[标签名]
  
  #docker commit [OPTIONS] CONTAINER [REPOSITORY[:TAG]]
  
  docker commit -a "leifengyang"  -m "首页变化" 341d81f7504f guignginx:v1.0
  ```

- 镜像传输(适合离线安装)

  ```sh
  # 将镜像保存成压缩包
  docker save -o abc.tar guignginx:v1.0
  
  # 别的机器加载这个镜像
  docker load -i abc.tar
  ```

- 推送远程仓库

  > 推送镜像到docker hub；应用市场

  ```shell
  docker tag local-image:tagname new-repo:tagname
  docker push new-repo:tagname
  ```

  ```sh
  # 把旧镜像的名字，改成仓库要求的新版名字
  docker tag guignginx:v1.0 leifengyang/guignginx:v1.0
  
  # 登录到docker hub
  docker login       
  
  docker logout（推送完成镜像后退出）
  
  # 推送
  docker push leifengyang/guignginx:v1.0
  
  # 别的机器下载
  docker pull leifengyang/guignginx:v1.0
  ```
  
- 补充

  ```sh
  # docker 经常修改nginx配置文件
  docker run -d -p 80:80 \
  -v /data/html:/usr/share/nginx/html:ro \
  -v /data/conf/nginx.conf:/etc/nginx/nginx.conf \
  --name mynginx-02 \
  nginx
  
  docker cp  容器ID:容器内路径 目的主机路径		#从容器内拷贝文件到主机上
  
  #把容器指定位置的东西复制出来 
  docker cp 5eff66eec7e1:/etc/nginx/nginx.conf  /data/conf/nginx.conf
  #把外面的内容复制到容器里面
  docker cp  /data/conf/nginx.conf  5eff66eec7e1:/etc/nginx/nginx.conf
  ```

# Docker容器数据卷

- 容器数据的持久化

		/**自定义宿主机目录和容器内的目录的数据持久化**/
		docker run -it -v /宿主机绝对路径目录:/容器内目录      镜像名
		/**容器停止退出后，主机数据有变动，容器重新启动会自动同步**/

- 容器间继承+共享数据

  上面介绍的通过docker run命令的-v标识创建的挂载点只能对创建的容器有效。

  通过dockerfile的 VOLUME 指令可以在镜像中创建挂载点，这样只要通过该镜像创建的容器都有了挂载点。

  还有一个区别是，通过 VOLUME 指令创建的挂载点，无法指定主机上对应的目录，是自动生成的。

  出于可移植和分享的考虑，用-v 主机目录:容器目录这种方法不能够直接在Dockerfile中实现。

  由于宿主机目录是依赖于特定宿主机的，并不能够保证在所有的宿主机上都存在这样的特定目录。

  ```dockerfile
  #创建一个DockerFile执行文件，创建了两个挂载点
  # volume test
  FROM centos
  VOLUME ["/dataVolumeContainer1","/dataVolumeContainer2"]
  CMD echo "finished,--------success1"
  CMD /bin/bash
  
  #DockerFile添加一个新的挂载的镜像文件
  docker build -f 执行文件路径 -t 镜像名 .
  ```

  通过上述方式创建的镜像可实现多个容器间的数据共享

  ```sh
  #父和子容器可以是两个不同的镜像
  #先启动父容器
  docker run -it --name 父名字 镜像名
  
  #再启动子容器继承父容器
  docker run it --volumes-from 父名字 --name 子名字 镜像名
  #父容器目录内数据会同步到子容器，子容器目录内数据会同步到父容器，是相互同步
  ```

  容器之间配置信息的传递，数据卷的生命周期一直持续到没有容器使用它为止

# `DockerFile`

`Dockerfile`是用来构建`Docker`镜像的构建文件，是由一系列命令和参数构成的脚本。

构建三步骤

- 编写`Dockerfile`文件
- `docker build`
- `docker run`

		FROM 基础镜像，当前新镜像是基于哪个镜像的
		MAINTAINER 镜像维护者的姓名和邮箱地址
		RUN 容器构建时需要运行的命令
		EXPOSE 当前容器对外暴露出的端口
		WORKDIR 指定在创建容器后，终端默认登陆的进来工作目录，一个落脚点
		ENV 用来在构建镜像过程中设置环境变量
			ENV MY_PATH /usr/mytest
			这个环境变量可以在后续的任何RUN指令中使用，这就如同在命令前面指定了环境变量前缀一样；
			也可以在其它指令中直接使用这些环境变量，
			 
			比如：WORKDIR $MY_PATH
		ADD 将宿主机目录下的文件拷贝进镜像且ADD命令会自动处理URL和解压tar压缩包
		COPY 类似ADD，拷贝文件和目录到镜像中。
				将从构建上下文目录中 <源路径> 的文件/目录复制到新的一层的镜像内的 <目标路径> 位置
					COPY src dest
					COPY ["src", "dest"]
		VOLUME 容器数据卷，用于数据保存和持久化工作
		CMD 指定一个容器启动时要运行的命令
				Dockerfile 中可以有多个 CMD 指令，但只有最后一个生效，CMD 会被 docker run 之后的参数替换
		
				shell格式：CMD<命令>
				exec格式：CMD["可执行文件"，“参数”，“参数”]
		ENTRYPOINT 指定一个容器启动时要运行的命令
				ENTRYPOINT 的目的和 CMD 一样，都是在指定容器启动程序及参数
		ONBUILD 当构建一个被继承的Dockerfile时运行命令，父镜像在被子继承后父镜像的onbuild被触发

```sh
docker build -f 执行文件路径 -t 镜像名 .	# 生成Dockerfile文件，注意最后是有一个.的，表示当前目录
```

```sh
docker history 镜像名 #列出镜像的变更历史
```

## 例1-自定义Centos 

```dockerfile
#支持vim编辑器
#查看网络配置ifconfig支持
#容器默认工作目录/usr/local
FROM centos
MAINTAINER zzyy<zzyy167@126.com>
ENV MYPATH /usr/local
WORKDIR $MYPATH
RUN yum -y install vim
RUN yum -y install net-tools
EXPOSE 80
CMD echo $MYPATH
CMD echo "success--------------ok"
CMD /bin/bash
```

## 例2-使用`ENTRYPOINT`后可加参数

> （如果是`CMD`执行，在执行容器进时会报没有命令的`error`）

```dockerfile
FROM centos
RUN yum install -y curl
ENTRYPOINT [ "curl", "-s", "http://ip.cn" ]
```

## 例3-自定义Tomcat9（集成JAVA8）

```dockerfile
	#注意这里执行Dockerfile文件时把要COPY的文件和ADD的文件 与Dockerfile文件放在一起
	FROM centos
	MAINTAINER    ljw<fantasydragon_echo@163.com>
	#把宿主机当前上下文的c.txt拷贝到容器/usr/local/路径下
	#COPY c.txt /usr/local/cincontainer.txt
	#把java与tomcat添加到容器中并解压
	ADD apache-tomcat-9.0.22.tar.gz /usr/local/
	ADD jdk-8u211-linux-x64.tar.gz /usr/local/
	#安装vim编辑器
	RUN yum -y install vim
	#设置工作访问时候的WORKDIR路径，登录落脚点
	ENV MYPATH /usr/local
	WORKDIR $MYPATH
	#配置java与tomcat环境变量
	ENV JAVA_HOME /usr/local/jdk1.8.0_211
	ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
	ENV CATALINA_HOME /usr/local/apache-tomcat-9.0.22
	ENV CATALINA_BASE /usr/local/apache-tomcat-9.0.22
	ENV PATH $PATH:$JAVA_HOME/bin:$CATALINA_HOME/lib:$CATALINA_HOME/bin
	#容器运行时监听的端口
	EXPOSE  8080
	#启动时运行tomcat，并打印日志
	# ENTRYPOINT ["/usr/local/apache-tomcat-9.0.22/bin/startup.sh" ]
	# CMD ["/usr/local/apache-tomcat-9.0.22/bin/catalina.sh","run"]
	CMD /usr/local/apache-tomcat-9.0.22/bin/startup.sh && tail -F /usr/local/apache-tomcat-9.0.22/bin/logs/catalina.out
```

​	
```sh
#运行镜像文件，指定宿主机目录和tomcat目录webapps，logs目录同步
docker run -d -p 9080:8080 --name myt9 
-v /testdocker/mytomcat/tomcat9/test:/usr/local/apache-tomcat-9.0.22/webapps/test 
-v /testdocker/mytomcat/tomcat9/tomcat9logs/:/usr/local/apache-tomcat-9.0.22/logs 
--privileged=true mytomcat9

#注意如果在阿里云服务器部署要在安全组里开放所需端口
```

# Docker常用安装 #

## Tomcat

```sh
docker run -it -p 9081:8080 tomcat
```

## Mysql

```sh
docker run -p 9082:3306 --name mysql 
-v /testdocker/mysql/conf:/etc/mysql/conf.d 
-v /testdocker/mysql/logs:/logs 
-v /testdocker/mysql/data:/var/lib/mysql 
-e MYSQL_ROOT_PASSWORD=123456 -d mysql:5.7
```

> 命令说明：
> -p 9082:3306：将主机的9082端口映射到docker容器的3306端口。
> --name mysql：运行服务名字
> -v /testdocker/mysql/conf:/etc/mysql/conf.d ：将主机/testdocker/mysql录下的conf/my.cnf 挂载到容器的 /etc/mysql/conf.d
> -v /testdocker/mysql/logs:/logs：将主机/testdocker/mysql目录下的 logs 目录挂载到容器的 /logs。
> -v /testdocker/mysql/data:/var/lib/mysql ：将主机/testdocker/mysql目录下的data目录挂载到容器的 /var/lib/mysql 
> -e MYSQL_ROOT_PASSWORD=123456：初始化 root 用户的密码。
> -d mysql:5.7 : 后台程序运行mysql5.7

## Redis

```sh
docker run -p 9083:6379 
-v /testdocker/myredis/data:/data 
-v /testdocker/myredis/conf/redis.conf:/usr/local/etc/redis/redis.conf  
-d redis redis-server /usr/local/etc/redis/redis.conf --appendonly yes
```

> 命令说明：
> --appendonly yes ：是否生成持久化文件
> 	在data目录里生成
> #在主机/testdocker/myredis/conf/redis.conf目录下新建redis.conf文件vim /testdocker/myredis/conf/redis.conf/redis.conf
> #在宿主机里新建一份redis的配置文件同步到容器里，把配置文件里的绑定IP注释掉
>
> #测试redis-cli连接上来
> docker exec -it 运行着Rediis服务的容器ID redis-cli

## jar应用（示例）

- 创建`dockerfile`文件

  ```dockerfile
  FROM openjdk:8-jdk-slim
  LABEL maintainer=leifengyang
  
  COPY target/*.jar   /app.jar
  
  ENTRYPOINT ["java","-jar","/app.jar"]
  ```

- 执行jar应用文件

  ```sh
  docker build -t java-demo:v1.0 .
  ```


# 本地镜像发布到阿里云 #

```sh
#登录阿里云-容器镜像服务-镜像仓库，创建所需仓库
$docker login --username=xxxx registry.cn-shenzhen.aliyuncs.com
$docker tag [ImageId] registry.cn-shenzhen.aliyuncs.com/命名空间/仓库名:[镜像版本号]
$docker push registry.cn-shenzhen.aliyuncs.com/命名空间/仓库名:[镜像版本号]
```