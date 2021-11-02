# 安装Nginx

1. 安装gcc

   gcc是linux下的编译器在此不多做解释，感兴趣的小伙伴可以去查一下相关资料，它可以编译 C,C++,Ada,Object C和Java等语言

   ```
   #查看gcc版本
   gcc -v
   ```

   一般阿里云的centOS7里面是都有的，没有安装的话会提示命令找不到

   ```
   yum -y install gcc
   ```

2. zlib安装

   zlib库提供了很多种压缩和解压缩方式nginx使用zlib对http包的内容进行gzip，所以需要安装

   ```
   yum install -y zlib zlib-devel
   ```

3. 安装openssl

   openssl是web安全通信的基石，没有openssl，可以说我们的信息都是在裸奔

   ```
   yum install -y openssl openssl-devel
   ```

4. 安装nginx

   ```
   wget http://nginx.org/download/nginx-1.9.9.tar.gz  
   ```

5. 把压缩包解压到usr/local/,创建临时文件夹

   ```
   tar -zxvf  nginx-1.9.9.tar.gz
   
   mkdir -p /var/temp
   mkdir -p /var/temp/nginx
   mkdir -p /var/temp/run/nginx
   chmod a+wrx -R temp
   ```

6. 切换到cd /usr/local/nginx-1.9.9/下面

   ```
   ./configure \
   --prefix=/usr/local/nginx \
   --pid-path=/var/temp/run/nginx/nginx.pid \
   --lock-path=/var/lock/nginx.lock \
   --error-log-path=/var/log/nginx/error.log \
   --http-log-path=/var/log/nginx/access.log \
   --with-http_gzip_static_module \	#配置静态压缩
   --http-client-body-temp-path=/var/temp/nginx/client \
   --http-proxy-temp-path=/var/temp/nginx/proxy \
   --http-fastcgi-temp-path=/var/temp/nginx/fastcgi \
   --http-uwsgi-temp-path=/var/temp/nginx/uwsgi \
   --http-scgi-temp-path=/var/temp/nginx/scgi \
   --with-http_stub_status_module \	#监控模块
   --with-http_ssl_module  \	#https模块 
   ```

   > 切记，pid-path不能设置为/var/run/nginx/nginx.pid。因为CentOS每次重启后，都会删除/var/run目录中的自建目录和文件，从而导致nginx自启动失败。

7. 编译安装

   ```
   make && make install
   ```

8. 启动Nginx命令

   ```
   #切换到/usr/local/nginx/sbin下
   ./nginx -c /usr/local/nginx/conf/nginx.conf
   #如果不指定-c，nginx在启动时默认加载conf/nginx.conf文件。
   ```

9. 查看nginx服务是否启动成功

   ```
   ps -ef | grep nginx
   ```

10. 配置HTTPS访问

    ```
     server {
            listen       443 ssl;	
            server_name  bridgesms2.90600.cn bridgesms.90600.cn;	#访问域名,多个用空格分隔
    
            ssl_certificate      /usr/local/4404261__90600.cn.pem;	#证书pem路径
            ssl_certificate_key  /usr/local/4404261__90600.cn.key;	#证书key路径
    
            ssl_session_cache    shared:SSL:1m;
            ssl_session_timeout  5m;
    
            #ssl_ciphers  HIGH:!aNULL:!MD5;
            ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;    #加密算法
            ssl_protocols TLSv1 TLSv1.1 TLSv1.2;    #安全链接可选的加密协议
            ssl_prefer_server_ciphers  on;
     
            location / {
                root   /www/test;	#项目访问路径
                index  index.html index.htm;
            }
        }
    
    ```

11. 设置开机启动

    在系统服务目录里创建nginx.service文件

    ```
    [Unit]
    Description=nginx
    After=network.target
      
    [Service]
    Type=forking
    ExecStart=/usr/local/nginx/sbin/nginx
    ExecReload=/usr/local/nginx/sbin/nginx -s reload
    ExecStop=/usr/local/nginx/sbin/nginx -s quit
    PrivateTmp=true
    ```

    ```
    [Unit]:服务的说明
    Description:描述服务
    After:描述服务类别
    [Service]服务运行参数的设置
    Type=forking是后台运行的形式
    ExecStart为服务的具体运行命令
    ExecReload为重启命令
    ExecStop为停止命令
    PrivateTmp=True表示给服务分配独立的临时空间
    注意：[Service]的启动、重启、停止命令全部要求使用绝对路径
    [Install]运行级别下服务安装的相关设置，可设置为多用户，即系统运行级别为3
    ```

    设置开机自启动

    ```
    systemctl enable nginx.service
    ```

    查看nginx状态

    ```
    systemctl status nginx.service
    ```

    很奇怪，明明启动成功了，为什么显示Active: inactive (dead)？

    杀死nginx重启nginx

    ```
    pkill -9 nginx
    
    ps aux | grep nginx
    
    systemctl start nginx
    ```

    再次查看状态，变成了active，搞定。

# 安装PHP7.3

1. 安装依赖包

   ```
   yum -y install libxml2 libxml2-devel openssl openssl-devel bzip2 bzip2-devel libcurl libcurl-devel libjpeg libjpeg-devel libpng libpng-devel freetype freetype-devel gmp gmp-devel libmcrypt libmcrypt-devel readline readline-devel libxslt libxslt-devel zlib zlib-devel glibc glibc-devel glib2 glib2-devel ncurses curl gdbm-devel db4-devel libXpm-devel libX11-devel gd-devel gmp-devel expat-devel xmlrpc-c xmlrpc-c-devel libicu-devel libmcrypt-devel libmemcached-devel libzip
   ```

2. 下载安装包并解压

   ```
   wget http://php.net/distributions/php-7.3.27.tar.gz
   tar -zxvf php-7.3.27.tar.gz
   cd php-7.3.27
   ```

3. 编译安装

   ```
   ./configure \
   --prefix=/usr/local/php \                                  [php安装的根目录]
   --exec-prefix=/usr/local/php \                               [php执行文件所在目录]
   --bindir=/usr/local/php/bin \                            [php/bin目录]
   --sbindir=/usr/local/php/sbin \                            [php/sbin目录]
   --includedir=/usr/local/php/include \                    [php包含文件所在目录]
   --libdir=/usr/local/php/lib/php \                        [php/lib目录]
   --mandir=/usr/local/php/php/man \                        [php/man目录]
   --with-config-file-path=/usr/local/php/etc \               [php的配置目录]
   --with-mysql-sock=/tmp/mysql.sock \           [php的Unix socket通信文件]
   --with-mhash \                                            [Mhash是基于离散数学原理的不可逆向的php加密方式扩展库，其在默认情况下不开启]
   --with-openssl \                                        [OpenSSL 是一个安全套接字层密码库]
   --with-mysqli=shared,mysqlnd \                          [php依赖mysql库]
   --with-pdo-mysql=shared,mysqlnd \                       [php依赖mysql库]
   --with-gd \                                                [gd库]                                                
   --with-iconv \                                            [关闭iconv函数，种字符集间的转换]                        
   --with-zlib \                                            [zlib是提供数据压缩用的函式库]
   --enable-zip \                                            [打开对zip的支持]
   --enable-inline-optimization \                            [优化线程]
   --disable-debug \                                        [关闭调试模式]
   --disable-rpath \                                        [关闭额外的运行库文件]
   --enable-shared \                                        [启用动态库]
   --enable-xml \                                            [开启xml扩展]
   --enable-bcmath \                                        [打开图片大小调整,用到zabbix监控的时候用到了这个模块]
   --enable-shmop \                                        [共享内存]
   --enable-sysvsem \                                        [内存共享方案]
   --enable-mbregex \                                        [开启多字节正则表达式的字符编码。]
   --enable-mbstring \                                        [开启多字节字符串函数]
   --enable-ftp \                                            [开启ftp]
   --enable-pcntl \                                        [PHP的进程控制支持实现了Unix方式的多进程创建]        
   --enable-sockets \                                        [开启套节字]
   --with-xmlrpc \                                            [打开xml-rpc的c语言]
   --enable-soap \                                            [开启简单对象访问协议简单对象访问协议]
   --without-pear \                                        [开启php扩展与应用库]
   --with-gettext \                                        [开户php在当前域中查找消息]
   --enable-session \                                      [允许php会话session]
   --with-curl \                                           [允许curl扩展]
   --with-jpeg-dir \                                        [指定jpeg安装目录yum安装过后不用再次指定会自动找到]
   --with-freetype-dir \                                    [指定freetype安装目录yum安装过后不用再次指定会自动找到]
   --enable-opcache \                                      [开启使用opcache缓存]
   --enable-fpm \                                            [开启fpm]
   --with-fpm-user=nginx \                                 [php-fpm的用户]
   --with-fpm-group=nginx \                                [php-fpm的用户组]
   --without-gdbm \                                        [数据库函数使用可扩展散列和类似于标准UNIX dbm的工作]
   --enable-fast-install \                                    [为快速安装优化]
   --disable-fileinfo
   PKG_CONFIG_PATH=/usr/local/libzip/lib64/pkgconfig/		[指定下面安装的libzip的pkg路径,否则编译的时候会提示找不以libzip]
   ```

```
./configure \
--prefix=/usr/local/php \
--exec-prefix=/usr/local/php \
--bindir=/usr/local/php/bin \
--sbindir=/usr/local/php/sbin \
--includedir=/usr/local/php/include \
--libdir=/usr/local/php/lib/php \
--mandir=/usr/local/php/php/man \
--with-config-file-path=/usr/local/php/etc \
--with-mysql-sock=/tmp/mysql.sock \
--with-mhash \
--with-openssl \
--with-mysqli=shared,mysqlnd \
--with-pdo-mysql=shared,mysqlnd \
--with-gd \
--with-iconv \
--with-zlib \
--enable-zip \
--enable-inline-optimization \
--disable-debug \
--disable-rpath \
--enable-shared \
--enable-xml \
--enable-bcmath \
--enable-shmop \
--enable-sysvsem \
--enable-mbregex \
--enable-mbstring \
--enable-ftp \
--enable-pcntl \
--enable-sockets \
--with-xmlrpc \
--enable-soap \
--without-pear \
--with-gettext \
--enable-session \
--with-curl \
--with-jpeg-dir \
--with-freetype-dir \
--enable-opcache \
--enable-fpm \
--with-fpm-user=nginx \
--with-fpm-group=nginx \
--without-gdbm \
--enable-fast-install \
--disable-fileinfo
```



```
   make && make install
```

> **注意**：如果在配置时报libzip版本太低，请卸载重新安装高版本

```
   解决办法  这是由于系统的自带的 libzip版本低了，所以先卸载系统自带的libzip
   yum  -y remove libzip-devel
```



```
   configure: error: Please reinstall the libzip distribution
   在 https://libzip.org/download/ 下载源码，进行安装
   wget https://libzip.org/download/libzip-1.7.3.tar.xz
   
   #https://cmake.org/download/ 官网下载网址
   #(由于1.5以及以上的安装方式和版本下的不同，所以这里还需要安装cmake,安装cmake过程比较长)
   #wget https://github.com/Kitware/CMake/releases/download/v3.15.2/cmake-3.15.2.tar.gz
   tar -xvf cmake-3.15.2.tar.gz
   cd cmake-3.15.2
   ##cmake在配置时有两种方式，一个是经常的使用./configure，另一个就是./bootstrap。我查看了configure文件内容，发现使用./configure最后也是去调用bootstrap，所以我就直接使用bootstrap
   ##./configure
   ./bootstrap --prefix=/usr/local/cmake
   make
   make install
   #为cmake创建软链接
   cd /usr/bin
   sudo ln -s /usr/local/cmake/bin/cmake cmake
   #添加系统环境变量
   sudo vi /etc/profile
   然后在profile文件的最后添加一行
   export PATH=$PATH:/usr/local/cmake/bin
   source /etc/profile
   此时，你再次查看cmake版本，就已经是3.15.2了
   cmake --version
   cmake version 3.15.2
   
   #接着继续安装libzip
   tar zxvf libzip-1.5.2.tar.gz
   cd libzip-1.5.2
   mkdir build  
   cd build
   cmake ..	(注意:后面必须带有两个点)
   make
   make install
   
   #接着可以看到在生成了/usr/local/libzip
   #编辑vim /etc/ld.so.conf
   把/usr/local/libzip/lib64写进去
   # 更新配置
   ldconfig -v
   #同时需要指定/usr/local/libzip/lib64/pkgconfig的路径
   export PKG_CONFIG_PATH="/usr/local/libzip/lib64/pkgconfig"
   #可以使用echo $PKG_CONFIG_PATH验证是否成功,注意这种添加方式只会在当前会话窗口生效
   
   #安装过程中如果出现其它异常,说明还需要安装其它依赖,后补...
   #将php安装成php-fpm编译时报错/usr/lib64/libpthread.so.0: error adding symbols: DSO missing from command li
   #解决: 编译前执行 make clean 清除一下之前编译产生的旧objects
```

4. 安装后配置

   执行完安装命令后php7就已经安装在到了/usr/local/php目录下了。

   ```
   /usr/local/php/bin/php -v #查看安装是否成功
   ```

   为了以后方便，可以编辑 /etc/profile 添加环境变量 ，添加到最后面

   ```
   vim /etc/profile
   #添加以下内容
   PATH=/usr/local/php/bin:/usr/local/php/sbin:$PATH
   #保存后刷新环境变量
   source /etc/profile
   ```

   配置php,修改php配置文件之添加php配置文件

   ```
   #将php源码编译目录下的 `php.ini-production` 拷贝到php安装目录下的 `etc` 目录下
   cp php.ini-production /usr/local/php/etc/php.ini
   ```

   修改php配置文件之修改php扩展目录所在位置

   ```
   vim /usr/local/php/etc/php.ini
   #找到 `extension_dir` 修改扩展目录位置如下:
   extension_dir = "/usr/local/php/lib/php/extensions/no-debug-zts-20180731"
   #修改php配置文件之修改服务器所在时区
   date.timezone = PRC
   #修改php内存限制
   memory_limit = 1024M
   #最大执行时间
   max_execution_time = 60
   ```

   添加 php-fpm 管理相关的配置文件到系统配置目录 /etc/init.d

   ```
   #将php源码编译目录下的 `sapi/fpm/init.d.php-fpm` 文件拷贝到系统配置 `/etc/init.d` 目录下并重命名为 `php-fpm`
   cp sapi/fpm/init.d.php-fpm /etc/init.d/php-fpm
   ```

   添加 php-fpm 配置文件

   ```
   #将php安装目录下的 `/usr/local/php/etc/php-fpm.conf.default` 文件拷贝同目录下并重命名为 `php-fpm.conf`
   cp /usr/local/php/etc/php-fpm.conf.default /usr/local/php/etc/php-fpm.conf
   ```

   添加 www.conf 配置文件

   ```
   #将php安装目录下的 `/usr/local/php/etc/php-fpm.d/www.conf.default` 文件拷贝同目录下并重命名为 `www.conf`
   cp /usr/local/php/etc/php-fpm.d/www.conf.default /usr/local/php/etc/php-fpm.d/www.conf
   ```

   创建用户组和用户,php-fpm默认使用此用户

   ```
   groupadd www
   useradd -g www www
   ```

   设置PHP日志目录和php-fpm运行进程的ID文件目录
   php-fpm运行进程的ID文件也就是 php-fpm.pid
   其中设置php-fpm进程目录的用户和用户组为www

   ```
   #创建 `php-fpm` 日志目录
   mkdir -p /var/log/php-fpm/
   #创建 `php-fpm` 进程的ID(php-fpm.pid)文件运行目录
   mkdir -p /var/run/php-fpm
   #修改 `php-fpm` 进程的ID(php-fpm.pid)文件运行目录的所属用户和组
   own -R www:www /var/run/php-fpm/
   ```

   修改session的目录配置

   ```
   mkdir -p /var/lib/php/session
   修改 `session` 存放目录的所属用户和组
   chown -R www:www /var/lib/php
   #再次修改 `php.ini` 文件
   vim /usr/local/php/etc/php.ini
   #找到 `session.save_path` 修改为如下内容:
   session.save_path = "/var/lib/php/session"
   #保存并退出
   :wq
   ```

   将php-fpm加入启动服务

   ```
   cp sapi/fpm/php-fpm.service /usr/lib/systemd/system/php-fpm.service
   #修改php-fpm.service
   PIDFile=/var/run/php-fpm/php-fpm.pid
   ```

   测试PHP的配置文件是否无误

   ```
   php-fpm -t
   ```

   使用 `systemctl start` + `服务名` 启动系统服务

   ```
   systemctl start php-fpm.service
   ```

> ```
> #可以追踪执行过程打印日志方便DEBUG
> strace /usr/local/php/sbin/php-fpm -i 2>1.log
> ```

## 安装amqp依赖

1. 安装rabbitmq-c

   ```
   wget https://github.com/alanxz/rabbitmq-c/archive/v0.10.0.tar.gz
   tar zxf v0.10.0.tar.gz
   make && make install
   ```

2. 安装amqp

   ```
   wget https://pecl.php.net/get/amqp-1.6.1.tgz
   tar zxf amqp-1.6.1.tgz
   cd amqp-1.6.1.tgz
   phpize
   ./configure --with-php-config=/usr/local/php/bin/php-config --with-amqp --with-librabbitmq-dir=/usr/local/rabbitmq-c	#注意这里的rabbitmq-c是上面的安装后的地址
   make && make install
   ```

3. 添加php模块

   ```
   vim /usr/local/php/etc/php.ini
   #添加以下内容
   extension = amqp.so
   ```

4. 重启服务,使用php-m检测

## 安装PDO_MYSQL扩展

1. 进入php7解压好的源码包下面路径,看到所需的扩展pdo_mysql

   ```
   /usr/local/php-7.3.27/ext/pdo_mysql
   ```

2. 加入扩展模块

   ```
   phpize
   ```

3. 编译到指定路径

   ```
   ./configure -with-php-config=/usr/local/php/bin/php-config -with-pdo-mysql=mysqlnd
   #这里使用mysqlnd 而不是 /usr/local/mysql，PHP7 正式移除了 mysql 扩展；如果有多个版本的 PHP，使用-with-php-config=/usr/local/bin/php-config 指向对应版本的php-config 可以将扩展安装到对应的 PHP 版本；
   ```

4. 执行编译

   ```
   make && make install
   ```

5. 在php.ini添加配置

   ```
   extension=pdo_mysql.so
   ```

6. 重启服务,php-m检测

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

------

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

可以看到这段配置 ： `JAVA_OPT="${JAVA_OPT} -Xms512m -Xmx512m -Xmn256m"`,将512修改为128即可

------

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

## 开机自启设置

1. 在/etc/init.d 目录下新建一个 rabbitmq

   具体脚本如下所示

   ```sh
   #!/bin/bash
   #
   # chkconfig: 2345 80 05
   # description: rabbitmq 
   # processname: rabbitmq
    
   #RabbitMQ安装目录
   RABBITMQ_HOME=/usr/lib/rabbitmq/lib/rabbitmq_server-3.8.14
   export RABBITMQ_HOME
    
   case "$1" in
       start)
       echo "Starting RabbitMQ ..."
   	cd $RABBIT_HOME/sbin
   	rabbitmq-server
       ;;
   stop)
       echo "Stopping RabbitMQ ..."
   	cd $RABBIT_HOME/sbin
       rabbitmqctl stop
       ;;
   status)
       echo "Status RabbitMQ ..."
   	cd $RABBIT_HOME/sbin
       rabbitmqctl status
       ;;
   restart)
       echo "Restarting RabbitMQ ..."
   	cd $RABBIT_HOME/sbin
       rabbitmq-server
       ;;
    
   *)
       echo "Usage: $prog {start|stop|status|restart}"
       ;;
   esac
   exit 0
    
   ```

2. 对rabbitmq授予可执行权限

   ```
   chmod 777 rabbitmq
   ```

3. 添加rabbitmq服务到系统服务中

   ```
   chkconfig --add rabbitmq
   ```

4. 设置自启动

   ```
   chkconfig rabbitmq on
   ```

5. 查看自启动项是否设置成功

   ```
   chkconfig --list rabbitmq
   ```


------

# 安装Git



------

# 安装Elasticsearch

## Docker安装

```
//拉取镜像
docker pull docker.elastic.co/elasticsearch/elasticsearch-oss:7.9.3-arm64
```

## 应用安装

1. ### 创建独立用户与组

   ```bash
   #创建用户组
   groupadd es
   #创建用户，并且加入用户组
   useradd -g es es
   #为用户设置密码
   passwd es
   ```

   

2. ### 下载ElasticSearch

   ```
   #切换到 /opt 目录，下载Elasticsearch
   cd /opt
   wget https://mirrors.huaweicloud.com/elasticsearch/7.9.3/elasticsearch-7.9.3-linux-aarch64.tar.gz
   ```

# 安装Redis

1. 下载安装包编译安安装

```
# 安装gcc环境 如果没有安装gcc会出现错误(此时需要删除文件夹重新解压)
yum install gcc-c++
# 将redis-5.0.5.tar.gz下载到/home
cd /home
wget https://download.redis.io/releases/redis-5.0.11.tar.gz
tar xvzf redis-5.0.11.tar.gz
cd redis-5.0.11
# 安装到/usr/local/redis目录下
make PREFIX=/usr/local/redis install
# 拷贝配置文件
cp redis.conf /usr/local/redis/

```

2. 后端启动

   ```
   # 设置daemonize为yes
   vim /usr/local/redis/redis.conf
   # redis-server根据redis.conf文件启动redis
   cd /usr/local/redis/
   ./bin/redis-server ./redis.conf
   # 启动后查看redis默认端口号为6379
   ps -ef | grep -i redis
   # 关闭redis
   ./bin/redis-cli shutdown
   ```

3. 开机自启

   ```
   #修改配置文件支持systemctl启动方式
   sed -i -e 's:^daemonize .*:daemonize yes:' -e 's:^supervised .*:supervised systemd:' /usr/local/redis/bin/redis.conf
   #daemonize： 是否以守护进程方式运行，默认为 no，改为yes 以守护进程的方式运行。
   #supervised： 可以通过upstart和systemd等方式管理Redis守护进程 ，默认为 no，改为 systemd 通过systemd管理Redis守护进程。
   
   # 添加自定义系统服务
   cat > /usr/lib/systemd/system/redis.service <<EOF
   [Unit]
   Description=Redis Server Manager
   After=network.target
   
   [Service]
   Type=forking
   PIDFile=/var/run/redis_6379.pid
   ExecStart=/usr/local/redis/bin/redis-server /usr/local/redis/bin/redis.conf
   ExecReload=/bin/kill -USR2 $MAINPID
   ExecStop=/bin/kill -SIGINT $MAINPID
   PrivateTmp=true
   
   [Install]
   WantedBy=multi-user.target
   EOF
   
   # 重新加载系统服务配置
   systemctl daemon-reload
   # 查看redis服务状态
   systemctl status redis
   ```


------

# 安装Mysql5.7

[下载地址](https://downloads.mysql.com/archives/community/)

1. 下载mysql离线安装包

   选择如下：

   【Red Hat Enterprise Linux 7 / Oracle Linux】

   【Red Hat Enterprise Linux 7 / Oracle Linux 7 (x86, 64-bit)】

   选择完整的**RPM Bundle**下载

2. 上传tar包至服务器

3. 删除原有的mariadb或者mysql

   > 　注：卸载之前请关闭mysql服务，命令：
   >
   > ```
   > systemctl stop mysqld 
   > ```

   ```shell
   先查看一下是否已经安装了
   rpm -qa | grep mysql
   rpm -qa | grep mariadb
   
   #按照顺序卸载：
   rpm -e --nodeps mysql-community-server
   rpm -e --nodeps mysql-community-client
   rpm -e --nodeps mysql-community-libs
   rpm -e --nodeps mysql-community-common
   
   #删除mariadb，命令：
   rpm -e --nodeps mariadb-libs
   ```

   　**卸载完基本的环境后，我们要清理依赖的文件（数据库配置文件及数据库数据文件）**

   ```shell
   #删除数据库配置文件（一般情况下卸载了mysql这个文件也会被自动删除）
       rm -rf  /etc/my.cnf
   #删除数据库数据文件（包含系统数据库表和自定义数据库表）
       rm -rf /var/lib/mysql
   #删除日志临时文件（比如安装后产生密码的文件，不删除会发现安装后查询2个初始密码，但以下面的为主）
       rm -rf /var/log/mysqld.log
   ```

4. 解压缩mysql离线安装包

   ```shell
   cd /opt/
   tar -xvf mysql-5.7.35-1.el7.x86_64.rpm-bundle.tar
   ```

   解压缩之后，包含以下rpm包

   ```shell
   -rw-r--r--. 1 7155 31415    317800 6月   8 17:15 mysql-community-common-5.7.35-1.el7.x86_64.rpm
   -rw-r--r--. 1 7155 31415   4120044 6月   8 17:15 mysql-community-devel-5.7.35-1.el7.x86_64.rpm
   -rw-r--r--. 1 7155 31415  47722528 6月   8 17:15 mysql-community-embedded-5.7.35-1.el7.x86_64.rpm
   -rw-r--r--. 1 7155 31415  23313776 6月   8 17:15 mysql-community-embedded-compat-5.7.35-1.el7.x86_64.rpm
   -rw-r--r--. 1 7155 31415 132130800 6月   8 17:15 mysql-community-embedded-devel-5.7.35-1.el7.x86_64.rpm
   -rw-r--r--. 1 7155 31415   2473348 6月   8 17:15 mysql-community-libs-5.7.35-1.el7.x86_64.rpm
   -rw-r--r--. 1 7155 31415   1263816 6月   8 17:15 mysql-community-libs-compat-5.7.35-1.el7.x86_64.rpm
   -rw-r--r--. 1 7155 31415 182213816 6月   8 17:15 mysql-community-server-5.7.35-1.el7.x86_64.rpm
   -rw-r--r--. 1 7155 31415 125392624 6月   8 17:16 mysql-community-test-5.7.35-1.el7.x86_64.rpm
   ```

5. 安装rmp包

   > 安装时必须严格遵守安装顺序 **依赖关系依次为 common → libs → client → server**
   >
   > **注：ivh中， i-install安装；v-verbose进度条；h-hash哈希校验**

   ```shell
   rpm -ivh mysql-community-common-5.7.35-1.el7.x86_64.rpm
   rpm -ivh mysql-community-libs-5.7.35-1.el7.x86_64.rpm 
   rpm -ivh mysql-community-client-5.7.35-1.el7.x86_64.rpm
   rpm -ivh mysql-community-server-5.7.35-1.el7.x86_64.rpm
   # 安装后查询安装的MySQL版本 
   mysqladmin --version
   ```

6. 启动及查询状态

   ```shell
   # 查询MySQL在系统的状态
   systemctl status mysqld
   # 启动MySQL数据库
   systemctl start mysqld
   # 关闭MySQL数据库
   systemctl stop mysqld
   # 重启MySQL数据库
   systemctl restart mysqld
   # 查看MySQL进程
   ps -ef | grep mysql
   ```

7. 登录安装的MySQL5.7

   1. 查看临时密码

      由于MySQL5.7.4之前的版本中默认是没有密码的，登录后直接回车就可以进入数据库，从而在里面进行设置密码等操作。其后版本对密码等安全相关操作进行了一些改变，在安装过程中，会在安装日志中生成一个临时密码

      ```shell
      #获取数据库临时密码：
      grep 'temporary password' /var/log/mysqld.log
      ```

   2. 用临时密码登录数据库

      ```mysql
      mysql -u root -p 回车键
      #然后输入临时密码（输入时不会显示出来，输入完直接回车）
      ```

   3. 修改mysql密码

      初始化密码只是提供给你登录到内部，而我们进入到内部必须修改密码，否则无法对数据库操作

      ```mysql
       # new_password替换成自己的密码
       ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
      #什么？？不符合策略，这里告诉你，密码为8位并且包含特殊字符、大小写字母、数字
      #如： ALTER USER 'root'@'localhost' IDENTIFIED BY  'aaAA$$!!66' ;
      ```

      查看密码策略及更改策略

      这是因为`MySQL`有密码设置的规范，具体是与`validate_password_policy`的值有关：

      |   Policy    |                       Tests Performed                        |
      | :---------: | :----------------------------------------------------------: |
      |  0 or LOW   |                            Length                            |
      | 1 or MEDIUM | Length; numeric, lowercase/uppercase,and special characters  |
      | 2 or STRONG | Length;numeric,lowercase/uppercase,and special characters;dictionary file |

      ```mysql
      #查看默认的策略配置 ： 
      show variables like 'validate_password%';  
      ```

      ```mysql
      mysql>  show variables like 'validate_password%';  
      +--------------------------------------+--------+
      | Variable_name                        | Value  |
      +--------------------------------------+--------+
      | validate_password_check_user_name    | OFF    |　　是否验证用户名
      | validate_password_dictionary_file    |        |　　密码策略文件，策略为STRONG才需要
      | validate_password_length             | 8      |　　密码长度
      | validate_password_mixed_case_count   | 1      |　　大小写字符长度，至少一个
      | validate_password_number_count       | 1      |　　数字至少一个
      | validate_password_policy             | MEDIUM |　　密码策略
      | validate_password_special_char_count | 1      |　　特殊字符至少一个
      +--------------------------------------+--------+
      ```

      看到上面设置个密码那么复杂，还记不住，那我们修改一下策略（设置LOW并且长度设置为3）

      ```mysql
      set global validate_password_policy=0;       # 关闭密码复杂性策略(LOW)
      set global validate_password_length=3;       # 设置密码复杂性要求密码最低长度为3
      
      select @@validate_password_policy;        # 查看密码复杂性策略
      select @@validate_password_length;        # 查看密码复杂性要求密码最低长度大小
      
      show variables like 'validate_password%';   # 查询具体策略
      +--------------------------------------+-------+
      | Variable_name                        | Value |
      +--------------------------------------+-------+
      | validate_password_check_user_name    | OFF   |
      | validate_password_dictionary_file    |       |
      | validate_password_length             | 3     |
      | validate_password_mixed_case_count   | 1     |
      | validate_password_number_count       | 1     |
      | validate_password_policy             | LOW   |
      | validate_password_special_char_count | 1     |
      +--------------------------------------+-------+
      ```

      ```mysql
      #终于可以设置灵魂密码了 ： 
      mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';
      ```

   4. 授权远程连接

      ```mysql
      #命令
      mysql> show databases;
      +--------------------+
      | Database           |
      +--------------------+
      | information_schema |
      | mysql              |
      | performance_schema |
      | sys                |
      +--------------------+
      4 rows in set (0.00 sec)
      #命令
      mysql> use mysql;
      Database changed
      #命令
      mysql> select host, user, authentication_string, plugin from user;
      +-----------+---------------+-------------------------------------------+-----------------------+
      | host      | user          | authentication_string                     | plugin                |
      +-----------+---------------+-------------------------------------------+-----------------------+
      | localhost | root          | *6BB4837EB74329105EE4568DDA7DC67ED2CA2AD9 | mysql_native_password |
      | localhost | mysql.session | *THISISNOTAVALIDPASSWORDTHATCANBEUSEDHERE | mysql_native_password |
      | localhost | mysql.sys     | *THISISNOTAVALIDPASSWORDTHATCANBEUSEDHERE | mysql_native_password |
      +-----------+---------------+-------------------------------------------+-----------------------+
      3 rows in set (0.00 sec)
      #命令
      mysql> update user set host = "%" where user='root';
      Query OK, 1 row affected (0.00 sec)
      Rows matched: 1  Changed: 1  Warnings: 0
      #命令
      mysql> select host, user, authentication_string, plugin from user;
      +-----------+---------------+-------------------------------------------+-----------------------+
      | host      | user          | authentication_string                     | plugin                |
      +-----------+---------------+-------------------------------------------+-----------------------+
      | %         | root          | *6BB4837EB74329105EE4568DDA7DC67ED2CA2AD9 | mysql_native_password |
      | localhost | mysql.session | *THISISNOTAVALIDPASSWORDTHATCANBEUSEDHERE | mysql_native_password |
      | localhost | mysql.sys     | *THISISNOTAVALIDPASSWORDTHATCANBEUSEDHERE | mysql_native_password |
      +-----------+---------------+-------------------------------------------+-----------------------+
      3 rows in set (0.00 sec)
      
      mysql> flush privileges;
      ```

   5. 尝试使用navacat远程连接

   

   

