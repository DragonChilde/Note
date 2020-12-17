# 安装Composer

1. 首先先下载composer.phar

```
curl -sS https://getcomposer.org/installer | php
```

2. 将composer.phar移动到环境变量中并且更名为composer

```
mv composer.phar /usr/local/bin/composer
```

3.  切换镜像源

```
composer config -g repo.packagist composer https://mirrors.aliyun.com/composer/
```

> 注间:root无法切换镜像源,必须新建其它用户执行

```
useradd 用户名
passwd 密码
```

# 安装Maven

1. ### 下载maven包

```bash
wget https://mirrors.sonic.net/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
```

> 如果提示 `wget: 未找到命令`，请尝试如下指令安装 `wget`

```bash
yum -y install wget
```

2. ### 解压下载的maven压缩

   ```bash
   tar -xzvf apache-maven-3.3.9-bin.tar.gz
   ```

   - -x：从备份文件中还原文件

   - -z：处理备份文件
   - -v：显示指令执行过程
   - -f：指定备份文件

3. ### 编辑系统环境文件`profile`，该文件类似于windows里的环境变量

   ```
   vim /etc/profile
   ```

   > 注意，配置的变量，请指向自己解压的maven路径：

   ```
   # set mvn 
   export MAVEN_HOME=/usr/local/apache-maven-3.6.3
   export PATH=$MAVEN_HOME/bin:$PATH
   ```

   `:wq`，退出保存，然后重新加载一下配置：

   ```
   source /etc/profile
   ```

   查看maven版本，测试配置生效：

   ```bash
   [root@izwz9eq3yzu5iw2o05coffz ~]# mvn -v
   Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
   Maven home: /usr/local/apache-maven-3.6.3
   Java version: 1.8.0_211, vendor: Oracle Corporation, runtime: /opt/java1.8/jre
   Default locale: en_US, platform encoding: UTF-8
   OS name: "linux", version: "3.10.0-1062.9.1.el7.x86_64", arch: "amd64", family: "unix"
   ```

4. ### 配置镜像加速+指定仓库地址

   配置文件路径

   ```bash
   vim /usr/local/apache-maven-3.6.3/conf/settings.xml
   ```

   配置jar包下载路径，路径指向自己的

   ```bash
   <localRepository>/home/maven/repo</localRepository>
   ```

   配置阿里镜像加速，默认是从中央仓库拉取。

   ```
   <mirrors>
    <mirror>
      <id>alimaven</id>
      <name>aliyun maven</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
      <mirrorOf>central</mirrorOf>        
    </mirror>
   </mirrors>
   ```

# 安装OpenJDK

1. 查看可安装JDK版本

   ```
   yum search java | grep -i --color JDK
   ```

2. 安装指定版本JDK

   ```
   yum install java-1.8.0-openjdk java-1.8.0-openjdk-devel
   //默认安装在/usr/lib/jvm下
   ```

3. 配置环境变量,在profile末尾追加环境变量JAVA_HOME、CLASSPATH、PATH

   ```
   export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.272.b10-1.el7_9.x86_64
   export CLASSPATH=.:$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/jre/lib/dt.jar:$JAVA_HOME/lib/tool.jar
   export PATH=$PATH:$JAVA_HOME/bin
   ```

   - rt.jar:Java基础库，即Java doc里面看到的所有类。
   - dt.jar:运行环境类库，主要为swing包，使用swing时可以加上。
   - tool.jar是系统编译（javac）时要使用的一个类库。

   　　※CLASSPATH加载类库各版本并不相同，具体可视情况而定，以上配置仅供参考。

4. 使配置生效

   ```
   source /etc/profile
   ```

5. 验证是否安装成功

   ```
   java -version
   ```

# 安装Nacos

> 注意:启动不了,没有任何报错,后台没有服务是因为启动的指定内存与现有机器的空闲内存不足

```
vim startup.sh
```

```bash
if [[ "${MODE}" == "standalone" ]]; then
    JAVA_OPT="${JAVA_OPT} -Xms512m -Xmx512m -Xmn512m"
    JAVA_OPT="${JAVA_OPT} -Dnacos.standalone=true"
else
    if [[ "${EMBEDDED_STORAGE}" == "embedded" ]]; then
        JAVA_OPT="${JAVA_OPT} -DembeddedStorage=true"
    fi
    JAVA_OPT="${JAVA_OPT} -server -Xms2g -Xmx2g -Xmn1g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
    JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${BASE_DIR}/logs/java_heapdump.hprof"
    JAVA_OPT="${JAVA_OPT} -XX:-UseLargePages"

fi
```

可以看到这段配置 ： `JAVA_OPT="${JAVA_OPT} -Xms512m -Xmx512m -Xmn256m"`,将512修改为64即可

# 安装RabbitMQ

系统环境

- JDK1.8

- Centos7-64位

- Erlang-OTP 23

- RabbitMQ-3.8.5

1. 完成erlang的前置条件

   ```
   curl -s https://packagecloud.io/install/repositories/rabbitmq/erlang/script.rpm.sh | sudo bash
   ```

2. 安装erlang

   ```
   yum install -y erlang
   ```

3. 检查erlang的版本号,守成操作

   ```
   erl
   Erlang/OTP 23 [erts-11.1.3] [source] [64-bit] [smp:1:1] [ds:1:1:10] [async-threads:1] [hipe]
Eshell V11.1.3  (abort with ^G)
   //命令halt().退出erl
   ```
   
4. 先导入两个key

   ```
   rpm --import https://packagecloud.io/rabbitmq/rabbitmq-server/gpgkey
   rpm --import https://packagecloud.io/gpg.key
   rpm --import https://www.rabbitmq.com/rabbitmq-release-signing-key.asc
   ```

5. 安装socat

   ```
   yum -y install epel-release
   yum -y install socat
   ```

   

6. 完成RabbitMQ前置条件配置

   ```
   curl -s https://packagecloud.io/install/repositories/rabbitmq/rabbitmq-server/script.rpm.sh | sudo bash
   ```

7. 去RabbitMQ官网下载RabbitMQ rpm安装包

8. 下载成功后，上传到服务器，然后使用命名安装

   ```
   rpm -ivh rabbitmq-server-3.8.9-1.el7.noarch.rpm
   ```

9. 启用管理平台插件，启用插件后，可以可视化管理RabbitMQ。

   ```
   rabbitmq-plugins enable rabbitmq_management
   ```

10. 启动RabbitMQ

    ```
    systemctl start rabbitmq-server
    ```

    > 注意:开启端口5672和15672

11. 创建用户名和密码登录后台

    ```
    rabbitmqctl add_user 用户名 密码
    ```

12. 设置为超级管理员

    ```
    rabbitmqctl set_user_tags 用户名 administrator
    ```

13. 授权远程访问(也可以登录后,可视化配置)

    ```
    rabbitmqctl set_permissions -p / 用户名 "." "." ".*"
    ```

14. 重启RabbitMQ

    ```
    systemctl restart rabbitmq-server
    ```

    