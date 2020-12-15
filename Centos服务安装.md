# 安装composer

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
useradd wen
passwd wen
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

   