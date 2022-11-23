# 安装Nginx

[官文文档](https://docs.nginx.com/nginx/admin-guide/installing-nginx/installing-nginx-open-source/)

本文以 Nginx 当前最新稳定版本 **1.22.1** 为例，详细介绍了如何在 CentOS 上安装及简单配置 Nginx。本文提供的安装步骤在 CentOS 7

本文假设你已使用管理员账户（root）登录操作系统，因为文中出现的命令大部分都需要用到 root 账户权限。如果你是用普通用户登录的，可运行 `sudo -s` 命令切换到 root 账户（或在每条命令前添加 `sudo`）

```shell
sudo -s
```

本文在用到包管理器时，会统一使用 `yum` 命令，但是要知道在 CenOS 8、CentOS Stream 8 及更新版本的 CentOS 中，`yum` 命令已被 `dnf` 命令替代（以符号链接的形式存在），运行 `yum` 命令实际上是运行 `dnf` 命令。

## 选择安装方式

Nginx 可以通过“包管理器安装”也可以通过“源代码编译安装”。这两种安装方式各有优缺点。用包管理器安装非常快捷，但是无法定制额外的功能。通过编译源代码安装有很大的定制空间，但是需要自己管理相关依赖。

另外，通过“包管理器安装”还存在两个问题。如果通过系统原生软件库安装，往往不是最新版本。如果用 Nginx 官方提供的软件库安装，虽然可以安装最新版本，但是会和系统原生软件库的安装文件混在一起，存在潜在的冲突问题。

## 下载 Nginx

Nginx 提供了两种版本，分别是“**主线版本**”（Mainline version）和“**稳定版本**”（Stable version）。前者是最新版本，虽然可用性不太会有问题，但是会引入一些实验性模块，也可能会出现一些新的错误。后者不像前者那样包含最新功能，但是在“向后移植”（[Backporting](https://en.wikipedia.org/wiki/Backporting)）时会修复主线版本存在的严重错误。官方推荐在生产服务器使用稳定版本，也是本文示例所选择使用的版本。

从 Nginx 官方下载页面获取最新稳定版源代码压缩包的下载链接：

**Nginx 官方下载页面**：https://nginx.org/en/download.html

切换到系统的源代码存放目录，将 Nginx 的源代码压缩包下载下来：

```shell
cd /usr/local/src
curl -LO https://nginx.org/download/nginx-1.22.1.tar.gz
```

## 编译 Nginx

相对于用包管理器安装 Nginx，通过源代码编译 Nginx 会有较多步骤，并且在编译过程中可能会遇到各种各样的意外情况，但是只要保持耐心，确定问题根源所在，就能有效地解决它。

编译 Nginx 主要分两步，一是用 Nginx 源代码附带的配置脚本定制功能，生成 [Makefile](https://www.gnu.org/software/make/manual/make.html#Introduction) 文件（待执行的一组任务规则），二是用 Make 工具根据 Makefile 文件中的规则自动化构建 Nginx。下文会详细解释这两个步骤。

### 编译环境

Nginx 是用 C 语言编写的，因此编译 Nginx 需要确保 CentOS 系统中安装了 C 编译器，本文以 **GCC**（[GNU Compiler Collection](https://gcc.gnu.org/)）为例，当然你也可以选择使用其它你所喜欢的 C 编译器。

另外还需要安装自动化构建工具 **Make**，它的作用是根据配置脚本生成的 Makefile 文件自动将源代码构建成可执行程序和库。

运行以下命令为 CentOS 系统安装 GCC 及 make 程序

1. 安装gcc

   gcc是linux下的编译器在此不多做解释，感兴趣的小伙伴可以去查一下相关资料，它可以编译 C,C++,Ada,Object C和Java等语言

   ```shell
   #查看gcc版本
   gcc -v
   ```

   一般阿里云的CentOS7里面是都有的，没有安装的话会提示命令找不到

   ```shell
   yum -y install gcc
   ```

2. zlib安装

   zlib库提供了很多种压缩和解压缩方式nginx使用zlib对http包的内容进行gzip，所以需要安装

   ```shell
   yum install -y zlib zlib-devel
   ```

3. 安装openssl

   openssl是web安全通信的基石，没有openssl，可以说我们的信息都是在裸奔

   ```shell
   yum install -y openssl openssl-devel
   ```

   > 

### 定制功能

压缩 Nginx 源代码压缩包，并切换到源代码所在目录（注意，除非有说明，所有操作都会在该目录下进行，切勿离开）：

```shell
tar -xvf  nginx-1.22.1.tar.gz
cd nginx-1.22.1
```

在该目录下可以找到一个名为 **configure** 的 Shell 脚本文件，它是 Nginx 源代码附带的“配置脚本（[configure script](https://en.wikipedia.org/wiki/Configure_script)）”。该脚本有两个作用：一是通过它提供的丰富的选项细颗粒度地定制 Nginx 功能；二是自动匹配系统中的库和待编译 Nginx 所依赖的库（如果依赖库有缺失，该脚本会中断执行并提示缺失库的名称，以便你来解决该依赖）。

运行以下命令查看含有所有可用配置选项的脚本帮助内容（按 q 键退出）：

```shell
./configure --help | less
```

除此外，也可以参考 Nginx 官方文档的“[从源代码构建 Nginx](https://nginx.org/en/docs/configure.html)”。这些内容对每个配置选项都做了简要说明，并提供了每个配置选项所对应功能模块的参考文档链接。

至于应该使用哪些选项，与你的实际需求有关。比如，你想要让 Nginx 支持 HTTPS 协议，就需要安装 SSL 模块，否则就不需要安装。

本文示例假设所编译的 Nginx 用途是 Web 服务器，并且能够支持 [SSL](https://nginx.org/en/docs/http/ngx_http_ssl_module.html)、[HTTP/2](https://nginx.org/en/docs/http/ngx_http_v2_module.html) 以及从指定 HTTP 请求头获取客户端 IP 地址。这些功能与编译配置选项的对应关系如下所示：

> ## nginx的configure命令编译参数
>
> | 编译参数                                | 默认值 | 参数说明                             |
> | --------------------------------------- | ------ | ------------------------------------ |
> | --prefix=PATH                           | 不编译 | nginx安装目录                        |
> | --with-select_module                    | 不编译 | 编译select I/O时间机制模块           |
> | --without-select_module                 | 编译   |                                      |
> | --with-poll_module                      | 不编译 |                                      |
> | --without-poll_module                   | 不编译 | poll事件支持模块                     |
> | --with-threads                          | 不编译 | 启用线程池支持                       |
> | --with-file-aio                         | 不编译 | 启用AIO支持                          |
> | --with-http_ssl_module                  | 不编译 | 启用SSL模块                          |
> | --with-http_v2_module                   | 不编译 | HTTP/2模块                           |
> | --with-http_realip_module               | 不编译 | HTTP的真实IP模块                     |
> | --with-http_addition_module             | 不编译 | 响应内容追加模块                     |
> | --with-http_xslt_module                 | 不编译 | XSLT样式表转换模块                   |
> | --with-http_xslt_module=dynamic         | 不编译 | 动态编译XSLT样式表转换模块           |
> | --with-http_image_filter_module         | 不编译 | 图像转换模块                         |
> | --with-http_image_filter_module=dynamic | 不编译 | 动态编译图像转换模块                 |
> | --with-http_geoip_module                | 不编译 | GEOip支持模块                        |
> | --with-http_geoip_module=dynamic        | 不编译 | 动态编译GEOip支持模块                |
> | --with-http_sub_module                  | 不编译 | 字符串替换模块                       |
> | --with-http_dav_module                  | 不编译 | WebDAV协议模块                       |
> | --with-http_flv_module                  | 不编译 | FLV文件伪流媒体服务器模块            |
> | --with-http_mp4_module                  | 不编译 | MP4文件伪流媒体服务器模块            |
> | --with-http_gunzip_module               | 不编译 | gzip压缩模块                         |
> | --with-http_gzip_static_module          | 不编译 | 发送gz与压缩文件数据模块             |
> | --with-http_auth_request_module         | 不编译 | 请求认证模块                         |
> | --with-http_random_index_module         | 不编译 | 随机首页模块                         |
> | --with-http_secure_link_module          | 不编译 | 请求连接检查模块                     |
> | --with-http_degradation_module          | 不编译 | 内存不足响应模块                     |
> | --with-http_slice_module                | 不编译 | 文件切片模块                         |
> | --with-http_stub_status_module          | 不编译 | 运行状态模块                         |
> | --without-http_charset_module           | 不编译 | 字符集转换模块                       |
> | --without-http_gzip_module              | 不编译 | gzip方式压缩输出模块                 |
> | --without-http_ssi_module               | 不编译 | ssl支持模块                          |
> | --without-http_userid_module            | 不编译 | cookie操作模块                       |
> | --without-http_access_module            | 不编译 | 基于IP的访问控制模块                 |
> | --without-http_auth_basic_module        | 不编译 | HTTP基本认证模块                     |
> | --without-http_mirror_module            | 不编译 | 访问镜像模块                         |
> | --without-http_split_clients_module     | 不编译 | 自定义客户请求分配模块               |
> | --without-http_referer_module           | 不编译 | referer操作模块                      |
> | --without-http_rewrite_module           | 不编译 | rewrite规则模块                      |
> | --without-http_autoindex_module         | 不编译 | 自动目录索引模块                     |
> | --without-http_geo_module               | 不编译 | 根据客户IP创建变量模块               |
> | --without-http_map_module               | 不编译 | 变量映射模块                         |
> | --without-http_proxy_module             | 不编译 | 代理功能模块                         |
> | --without-http_fastcgi_module           | 不编译 | FastCGI支持模块                      |
> | --without-http_uwsgi_module             | 不编译 | uWSGI支持模块                        |
> | --without-http_scgi_module              | 不编译 | SCGI支持模块                         |
> | --without-http_grpc_module              | 不编译 | gRPC支持模块                         |
> | --without-http_memcached_module         | 不编译 | Memcached服务访问模块                |
> | --without-http_limit_conn_module        | 不编译 | 并发连接数控制模块                   |
> | --without-http_limit_req_module         | 不编译 | 单IP请求数限制模块                   |
> | --without-http_empty_gif_module         | 不编译 | 空GIF图片模块                        |
> | --without-http_browser_module           | 不编译 | 客户端浏览器识别模块                 |
> | --without-http_upstream_hash_module     | 不编译 | hash负载均衡算法模块                 |
> | --with-stream                           | 不编译 | TCP/UDP代理模块                      |
> | --with-stream=dynamic                   | 不编译 |                                      |
> | --with-stream_ssl_module                | 不编译 | TCP/UDP代理的SSL支持模块             |
> | --with-stream_realip_module             | 不编译 | TCP/UDP代理的真实IP模块              |
> | --with-stream_geoip_module              | 不编译 | 编译地域信息解析模块                 |
> | --with-stream_ssl_preread_module        | 不编译 | TCP/UDP代理的SSL预处理模块           |
> | --with-google_perftools_module          |        | 启用Google的TCMalloc内存管理支持模块 |
> | --add-module                            |        | 添加第三方模块                       |
>
> > 其中，带有--with编译参数的模块默认不会被编译，因此如果要使用，只能手动提供对应编译参数。--without编译参数的模块默认启用，不使用的话需要明确提供对应编译参数

要想让 Nginx 具备上面所提到的那些附加功能，编译时可能需要依赖一些函数库（[Library](https://en.wikipedia.org/wiki/Library_(computing))），如果缺少某个库，在运行配置脚本时就会中断并抛出提示，不过每次中断只会抛出一个提示。如果上面提到的功能符合你的需求，为避免多次尝试浪费时间，可以直接运行以下代码为操作系统安装这些程序库：

> ```shell
> yum install -y gcc gcc-c++ pcre-devel zlib-devel openssl-devel libxml2-devel libxslt-devel gd-devel GeoIP-devel jemalloc-devel libatomic_ops-devel perl-devel perl-ExtUtils-Embed zlib-devel
> ```
>
> 用途：
>
> 1、gcc：GNU Compiler Collection，GNU编译器套件
>
> 2、zlib：提供压缩库
>
> 3、pcre：Perl语言兼容正则表达式
>
> 4、openssl：用于通信加密和安全通信的工具包
>
> 5、libxml2：XML文档的C解释器
>
> 6、libxslt：XSLT的C库，用于转换XML文档
>
> 7、gd：PHP图形处理的扩展库，提供处理图片的API
>
> 8、GeoIP：全球地理位置信息库
>
> 9、jemalloc：Facebook开源的内存分配器
>
> 10、libatomic：gcc的原子库

1. 创建临时文件夹

   ```shell
   mkdir -p /var/tmp
   mkdir -p /var/tmp/nginx
   mkdir -p /var/tmp/nginx/run
   chmod a+wrx -R temp
   ```

2. 创建专用账户

   正如前面所提到的，Web 服务器通常会使用专用账户，用于存放、读写 Web 文件（包括图片、HTML 文件及 Web 应用程序）。

   创建专用账户可以用任意用户名，本例使用 **www**。你可以运行以下命令检查系统中是否存在该用户：

   ```shell
   id www
   ```

   如果出现类似“no such user”的提示就表示不存在此账户，可以运行以下命令创建一个名为 www的无登录权限的系统用户：

   ```shell
   useradd www -s /sbin/nologin
   ```

3. 在安装完编译 Nginx 所依赖的库后，运行配置脚本进行配置：

   ```shell
   ./configure \
   #安装路径，但是对于Nginx来说这里也是默认页面存放路径
   --prefix=/usr/local/nginx \
   #nginx这个二进制程序放在那里,建议放在/usr/sbin/nginx
   --sbin-path=/usr/sbin/nginx
   #配置文件存位置，实际上nginx还有其他配置文件，其他的不用指定位置，nginx.conf放那里他们就放那里,建议放在/etc/nginx/nginx.conf
   --conf-path=/etc/nginx/nginx.conf
   #PID文件路径
   --pid-path=/var/tmp/nginx/run/nginx.pid \
   #锁文件路径
   --lock-path=/var/lock/nginx.lock \
   #错误日志存放位置
   --error-log-path=/var/log/nginx/error.log \
   #HTTP访问日志存放位置
   --http-log-path=/var/log/nginx/access.log \
   #属组和属主，前提是你得建立这个组和账号，如果不指定，默认都是nobody
   --user=www \
   --group=www \
   #启用哪些Nginx自带的功能模块
   --with-http_gzip_static_module \	#配置静态压缩
   --with-http_stub_status_module \	#监控模块
   --with-http_ssl_module  \	#https模块 
   --with-http_realip_module \	#真实ip模块
   --with-threads	#线程池支持
   
   #添加各种缓存路径
   --http-client-body-temp-path=/var/tmp/nginx/client/ \
   --http-proxy-temp-path=/var/tmp/nginx/proxy/ \
   --http-fastcgi-temp-path=/var/tmp/nginx/fcgi/ \
   --http-uwsgi-temp-path=/var/tmp/nginx/uwsgi/ \
   --http-scgi-temp-path=/var/tmp/nginx/scgi/ \
   
   #额外添加的第三方模块，= 后面指定的是这些模块解压后的路径
   --add-module=/opt/ngx_cache_purge-2.3      #用于做加速
   --add-module=/opt/ngx_http_lower_upper_case-master  #用于大小写转换
   --add-module=/opt/headers-more-nginx-module-0.32    #用于添加、设置和清除输入、输出
   
   #设置的其他路径
   #主体临时目录，处理HTTP请求时，如果包体需要暂时存放在磁盘中
   --http-client-body-temp-path=/var/tmp/nginx/client/
   --http-proxy-temp-path=/var/tmp/nginx/proxy/
   --http-fastcgi-temp-path=/var/tmp/nginx/fcgi/
   --http-uwsgi-temp-path=/var/tmp/nginx/uwsgi/
   --http-scgi-temp-path=/var/tmp/nginx/scgi/
    
   --with-openssl=/PATH #openssl源码解压路径，这个的作用是指定Nginx的SSL模块依赖的SSL库
   --with-http_ssl_module	#表示启用SSL模块
   --with-pcre=/PATH    #pcre源码包解压路径，无需安装
   --with-zlib=/PATH    #zlib源码包解压路径，无需安装
    
    
   #第三方模块查询
   #https://www.nginx.com/resources/wiki/modules/
   ```

   > 参考的安装参数
   >
   > ```shell
   > ./configure \
   > --prefix=/usr/local/nginx/ \
   > --sbin-path=/usr/sbin/nginx \
   > --conf-path=/etc/nginx/nginx.conf \
   > --error-log-path=/var/log/nginx/error.log \
   > --http-log-path=/var/log/nginx/access.log \
   > --pid-path=/var/tmp/nginx/run/nginx.pid \
   > --lock-path=/var/lock/nginx.lock \
   > --user=www \
   > --group=www \
   > --with-http_ssl_module \
   > --with-http_gzip_static_module \
   > --with-http_v2_module \
   > --with-http_realip_module \
   > --http-client-body-temp-path=/var/tmp/nginx/client/ \
   > --http-proxy-temp-path=/var/tmp/nginx/proxy/ \
   > --http-fastcgi-temp-path=/var/tmp/nginx/fcgi/ \
   > --http-uwsgi-temp-path=/var/tmp/nginx/uwsgi/ \
   > --http-scgi-temp-path=/var/tmp/nginx/scgi/ \
   > ```

   > 切记，pid-path不能设置为/var/run/nginx/nginx.pid。因为CentOS每次重启后，都会删除/var/run目录中的自建目录和文件，从而导致nginx自启动失败。
   >
   > 注意，命令中的 `--prefix` 选项是用来指定 Nginx 安装位置的，你可以选择任意位置，但是作为最佳实践，将其指定为系统的本地程序目录是一个不错的选择。当然，你也可以忽略该选项，这会导致 Nginx 的安装文件散落在各处（即 `/usr/local` 目录下的 `etc`、`lib`、`bin` 等目录）。
   >
   > 配置脚本运行完毕，如果你能看到如下所示的提示信息，就表示一切正常，可以正式开始进行编译了。

   ```shell
   ...
   Configuration summary
     + using system PCRE library
     + using system OpenSSL library
     + using system zlib library
   
     nginx path prefix: "/usr/local/nginx"
     nginx binary file: "/usr/local/nginx/sbin/nginx"
     nginx modules path: "/usr/local/nginx/modules"
     nginx configuration prefix: "/usr/local/nginx/conf"
     nginx configuration file: "/usr/local/nginx/conf/nginx.conf"
     nginx pid file: "/usr/local/nginx/logs/nginx.pid"
     nginx error log file: "/usr/local/nginx/logs/error.log"
     nginx http access log file: "/usr/local/nginx/logs/access.log"
     nginx http client request body temporary files: "client_body_temp"
     nginx http proxy temporary files: "proxy_temp"
     nginx http fastcgi temporary files: "fastcgi_temp"
     nginx http uwsgi temporary files: "uwsgi_temp"
     nginx http scgi temporary files: "scgi_temp"
   ```

4. 接下来就可以运行以下代码对 Nginx 源码进行编译了：

   ```shell
   make
   ```

   如果你的服务器配置较高，可以在编译时通过 `make` 命令的 `-j` 选项开启多个进程以加快编译速度，比如同时用 3 个进程进行编译：`make -j3`。

   编译时间的长短与服务器的配置、可用系统资源以及具体的编译配置有关。编译 Nginx 所需要的系统资源不多，一般情况下，即便是配置不高的服务器也能很快编译成功。

   编译成功后，你可以看到如下所示的提示信息：

   ```shell
   ...
   -ldl -lpthread -lcrypt -lpcre -lssl -lcrypto -ldl -lpthread -lz \
   -Wl,-E
   sed -e "s|%%PREFIX%%|/usr/local/nginx|" \
   	-e "s|%%PID_PATH%%|/usr/local/nginx/logs/nginx.pid|" \
   	-e "s|%%CONF_PATH%%|/usr/local/nginx/conf/nginx.conf|" \
   	-e "s|%%ERROR_LOG_PATH%%|/usr/local/nginx/logs/error.log|" \
   	< man/nginx.8 > objs/nginx.8
   make[1]: Leaving directory '/usr/local/src/nginx-1.22.1'
   ```

## 安装 Nginx

编译完成后即可运行以下命令将 Nginx 安装到之前指定的目录：

```shell
make install
```

至此 Nginx 就安装完成了。如果你是**第一次**按照本文步骤安装 Nginx，为方便之后运行 Nginx 的相关命令，可运行以下命令将 Nginx 的可执行文件目录添加到系统环境变量 `PATH` 中

```shell
echo 'export PATH="/usr/local/nginx/sbin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

现在运行以下命令，应该能够看到所安装 Nginx 的版本号：

```
nginx -v
```

## 配置 Nginx

根据 Nginx 的不同用途，Nginx 官方文档的“[管理员指南](https://docs.nginx.com/nginx/admin-guide/)”提供了丰富的配置指南，如负载均衡（[Load Balancer](https://docs.nginx.com/nginx/admin-guide/load-balancer/)）、Web 服务器（[Web Server](https://docs.nginx.com/nginx/admin-guide/web-server/)）、邮件代理（[Mail Proxy](https://docs.nginx.com/nginx/admin-guide/mail-proxy/)）等。本文仅以最佳实践配置一个简单的 Web 服务器，使其能够正常访问静态文件（如 HTML 页面、图片等）。

### 分配用户权限

Nginx 启动后会出现两种进程，分别是**主进程**（master process）和**工作进程**（worker process）。前者只有一个，作用是读取、估算配置，以及管理工作进程。后者可以有多个（个数可通过配置文件中的指令 `worker_processes` 进行设置），作用是处理请求。

主进程要以管理员账户（root）的身份运行。这是因为在 Linux 系统中，绑定低于 1024 的本地 TCP 端口号必须使用管理员账户（root）权限，而 Web 服务器所使用的 HTTP 协议和 HTTPS 协议的默认端口分别是 80 和 443，Nginx 要绑定这两个端口号，就必须有管理员账户权限。另外，还需要 root 权限读取配置文件。

而工作进程则需要以普通用户身份运行。这是因为实际处理请求的是工作进程，限制其权限可以避免出现危及操作系统安全的潜在隐患。该账户通常是 Web 服务器专用账户，如 `www`（接下来会在 Nginx 的配置文件中设定），一般与 [FastCGI](https://en.wikipedia.org/wiki/FastCGI) 程序（如 [PHP-FPM](https://osfere.com/linux/how-to-build-install-and-configure-latest-php-on-centos)）所使用的账户相同。

### 修改配置文件

Nginx 提供了一个开箱即用的配置文件，里面提供了一个静态 Web 服务器配置示例，只要启动 Nginx 就能访问。此外，该配置文件还提供了一些其它可以展示 Nginx 功能的配置示例，只是默认情况下是被注释的。

为了方便对 Nginx 配置文件中的相关指令进行解释说明，避免被不相关信息的干扰，本文不使用该默认配置文件，而是新建一个配置文件，仅放入我们所需要的指令。

首先运行以下命令将 Nginx 默认配置文件备份一下：

```shell
mv /usr/local/nginx/conf/nginx.conf /usr/local/nginx/conf/nginx.conf.bak
```

然后用你喜欢的编辑器（这里用的是 vim 编辑器）为 Nginx 创建一个新配置文件，文件名与默认配置文件一致，也是 **nginx.conf**：

将以下内容复制粘贴到新创建的配置文件中，然后保存并退出编辑器（vim 编辑器先按 ESC 再输入 `:wq` 并回车,快捷键为`shift+zz`）

```nginx
user  www;
worker_processes  auto;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    server {
        listen       80;
        server_name  localhost;

        location / {
            root   /home/www-data/www;
            index  index.html index.htm;
        }
    }
}
```

以上配置中一共出现了 13 个指令，其中包括 9 个**单指令**（simple directive）和 4 个**块指令**（block directive），即后面带有花括号 `{}` 的指令。如果一个块指令能够包含其它指令，即可称其为“上下文”（[context](https://nginx.org/en/docs/beginners_guide.html#:~:text=If a block directive can have other directives inside braces%2C it is called a context)）。

在这些指令中，有的属于 Nginx 的“[核心功能](https://nginx.org/en/docs/ngx_core_module.html)”，用来配置与 Nginx 程序自身运行相关的功能，如 user、worker_processes、events、worker_connections 和 include；有的属于 Nginx 的“[HTTP 核心模块](https://nginx.org/en/docs/http/ngx_http_core_module.html)”，如用来配置与 HTTP 服务器相关的功能，如 http、default_type、server、listen、server_name、location 和 root；还有的属于 Nginx 的“[HTTP 索引模块](https://nginx.org/en/docs/http/ngx_http_index_module.html)”，用来处理结尾为斜杠 `/` 的请求，如 index。下面会按照从上到下的顺序依次解释这些指令的作用。

- 第 1 个是 **[user](https://nginx.org/en/docs/ngx_core_module.html#user)** 指令，用来设定运行 Nginx 工作进程的用户和用户组，这里将其设定为之前创建的专用账户，以便 Nginx 有权限访问所有者是该用户的文件。
- 第 2 个是 **[worker_processes](https://nginx.org/en/docs/ngx_core_module.html#worker_processes)** 指令，用来设定 Nginx 工作进程的个数，如果你的服务器配置较高，可以更改其值增加工作进程个数，以提升 Web 服务器的性能。该指令的最佳值取决于许多因素，不过一般可设为 CPU 内核数量，或设为 **auto**（自动检测 CPU 核心数）。
- 第 3 个是一个名为 **[events](https://nginx.org/en/docs/ngx_core_module.html#events)** 的块指令，它是一个上下文，作用是通过添加一些指令来影响连接处理。
- 第 4 个是包含在 events 块指令中的 **[worker_connections](https://nginx.org/en/docs/ngx_core_module.html#worker_connections)** 指令，用来设定每个 Nginx 工作进程能够同时打开的最大连接数。你可以将其设为操作系统对每个进程打开文件最大数量限制（即通过运行命令 `ulimit -n` 获取到的值），如本例中的 **1024**。
- 第 5 个是名为 **[http](https://nginx.org/en/docs/http/ngx_http_core_module.html#http)** 的块指令，作用是通过添加一些指令来控制 HTTP 通信。接下来要介绍的 8 个指令都包含在其中。
- 第 6 个是 **[include](https://nginx.org/en/docs/ngx_core_module.html#include)** 指令，作用是将外部文件包含到配置文件中。本例将 Nginx 预置的名为 **mime.types** 的文件包含了进来，该文件中只有一个名为 types 的块指令，其作用是设置文件扩展名与[“MIME 类型“](https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types)的映射，当客户端请求带有某种扩展名的文件时，Nginx 就会用相应的 MIME 类型进行响应。
- 第 7 个是 [**default_type**](https://nginx.org/en/docs/http/ngx_http_core_module.html#default_type) 指令，用来设定默认的 MIME 类型，当 Nginx 找不到某种扩展名所对应的 MIME 类型时，会使用此默认类型。本例中将其设置为 application/octet-stream，表示默认以普通二进制文件的形式响应给客户端，换句话说就是下载这个文件。
- 第 8 个是名为 **[server](https://nginx.org/en/docs/http/ngx_http_core_module.html#server)** 的块指令，作用是通过添加一些指令配置虚拟服务器（virtual server）。一个 server 指令对应一个虚拟服务器。接下来的指令都包含在其中。
- 第 9 个是 **[listen](https://nginx.org/en/docs/http/ngx_http_core_module.html#listen)** 指令，通常用来为虚拟服务器设定一个供请求的端口。如果没有特殊需要，需要将该指令的值设为 HTTP 协议的默认端口 80，如果虚拟服务器要使用 HTTPS 协议，则要使用其默认端口 443。
- 第 10 个是 **[server_name](https://nginx.org/en/docs/http/ngx_http_core_module.html#server_name)** 指令，作用是为虚拟服务器设定名称，通常利用其绑定一个或多个域名，由于绑定域名超出了本文范围，所以本文仅以本地主机名 **localhost** 为例。
- 第 11 个是名为 **[location](https://nginx.org/en/docs/http/ngx_http_core_module.html#location)** 的块指令，用来设定与“[请求 URI](https://www.rfc-editor.org/rfc/rfc2616#section-5.1.2)”（Request URI）[[1\]](https://osfere.com/linux/how-to-build-install-and-configure-latest-nginx-on-centos#footnote-1) 进行比对的“前缀字符串”（prefix strings），如果请求 URI 与前缀相匹配，就将请求 URI 添加到 root 指令设定的路径上，形成本地文件系统上的请求文件路径。本例中将该指令的字符串前缀设定为斜杠 `/`，即匹配所有请求，如果请求的文件路径存在，Nginx 就会发送该文件。
- 第 12 个是 **[root](https://nginx.org/en/docs/http/ngx_http_core_module.html#root)** 指令，用来设定供客户端请求的根目录。该指令可用在 http、server、location 等多个上下文中，默认值为安装目录下的 html 文件夹。本例将其用在了 location 上下文中，并将其值设定为 **/home/www**。
- 第 13 个是 **[index](https://nginx.org/en/docs/http/ngx_http_index_module.html#index)** 指令，用来指定用作索引的文件，其值可以是用空格分隔的多个文件名。如果请求的是一个目录，即请求 URI 是以斜杠 `/` 结尾的，Nginx 将会尝试在 root 指令所设定的目录中逐个查找该指令所设定的文件，一旦找到就会导致一个内部重定向，进而使用匹配该请求的 location 块指令进行处理。

正如前面所提到的，默认情况下 `root` 指令设定的存放 Web 文件目录是与 Nginx 的安装文件混放在一起的，如下所示：

```shell
/usr/local/nginx/html
```

这显然不是一个好习惯，因此，作为最佳实践，我们将其转移至专用账户 www 的家目录（`/home/www`）下。

运行以下两条命令，将 Nginx 默认的 Web 文件存放目录其转移至 www 家目录下，同时重命名为 www，并更改该文件夹的所有者和用户组为 www：

```shell
mv /usr/local/nginx/html /home/www
chown -R www:www /home/www
```

配置修改完成后，可以运行以下命令进行测试，如果能看到 **successful** 字样就表示配置文件不会影响 Nginx 的运行：

```shell
nginx -t
```

## 启动 Nginx

虽然可以直接通过 `nginx` 命令启动 Nginx，但是为了让其更稳定的运行（避免因服务器宕机或维护性重启导致 Nginx 停止运行），有必要将其作为服务托管给操作系统，这样不论何种原因导致的系统重启，Nginx 都能随机启动。

在 CenOS 中，可以使用系统服务管理器 **[systemd](https://www.freedesktop.org/wiki/Software/systemd/)** 来管理各种服务。要将 Nginx 托管给 systemd，需要为其创建一个配置文件，以便让其管理。

使用你喜欢的编辑器创建一个名为 **nginx.service** 的配置文件到如下所示路径（这里使用的是 vim 编辑器）：

```nginx
vim /etc/systemd/system/nginx.service
```

然后将如下所示的内容复制粘贴到该文件中并保存（vi 编辑器先按 ESC 再输入 `:wq`,快捷键:`shift+zz`）：

```
[Unit]
Description=The NGINX HTTP and reverse proxy server
After=syslog.target network.target remote-fs.target nss-lookup.target

[Service]
Type=forking
ExecStartPre=/usr/local/nginx/sbin/nginx -t
ExecStart=/usr/local/nginx/sbin/nginx
ExecReload=/bin/kill -s HUP $MAINPID
ExecStop=/bin/kill -s QUIT $MAINPID

[Install]
WantedBy=multi-user.target
```

> ```
> [Unit]:服务的说明
> Description:描述服务
> After:描述服务类别
> [Service]服务运行参数的设置
> Type=forking是后台运行的形式
> ExecStart为服务的具体运行命令
> ExecReload为重启命令
> ExecStop为停止命令
> PrivateTmp=True表示给服务分配独立的临时空间
> 注意：[Service]的启动、重启、停止命令全部要求使用绝对路径
> [Install]运行级别下服务安装的相关设置，可设置为多用户，即系统运行级别为3
> ```

接着运行以下命令，让 systemd 读取新创建的配置文件：

```shell
systemctl daemon-reload
```

现在通过运行以下命令启动 Nginx 服务：

```shell
systemctl start nginx
```

通过运行以下命令重新启动 Nginx 服务：

```shell
systemctl restart nginx
```

如果修改了 Nginx 的配置文件，可以通过运行以下命令重新加载配置文件，并使用新配置启动新的工作进程，优雅地关闭旧的工作进程：

```shell
systemctl reload nginx
```

当然你也可以直接运行 `nginx` 命令达到与此相同的效果：

```shell
nginx -s reload
```

运行以下命令查看 Nginx 服务的状态：

```shell
systemctl status nginx
```

如果服务的状态为 **Active: active (running)** 且没有显示任何错误或警告信息的话，就表示一切正常。可以运行以下命令进行测试：

```shell
curl 127.0.0.1
```

如果能看到含有“Welcome to nginx!”字样的 HTML 代码，说明 Nginx 服务已经可以正常运行了。如果你的云服务器 VPC 安全组规则和 CentOS 系统防火墙均已放行 **80** 端口入站，也可以在网页浏览器通过服务器的 IP 地址访问该页面。

最后，将 FPM 作为服务添加到随机启动：

```shell
systemctl enable nginx
```

至此，就完成了在 CentOS 上安装配置 Nginx 的全部流程。

## 升级 Nginx

如果想要升级到 Nginx 今后发布的新版本，只需要按照以上步骤重新操作一到四步即可。因为重新安装 Nginx 时不会覆盖已有配置文件，所以无需修改它们。

## 总结

完成以上流程后，应该能够得到如下所示的这些可用文件或信息。这些信息可能会在维护 Nginx 时用到。

Nginx 配置文件路径：

```
/usr/local/nginx/conf/nginx.conf
```

Nginx 的系统服务配置文件路径：

```
/etc/systemd/system/nginx.service
```

## 附录

- 配置HTTPS访问

  ```nginx
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

------

# 安装PHP7.4

在 CentOS 中，虽然可以通过包管理器 YUM 或 DNF 快捷方便地安装 PHP，但是往往不是最新版本。因此，想要体验新版本 PHP 才具备的功能特性，通过源代码编译安装 PHP 是个不错的选择。

关于如何在不同的操作系统中以不同的方式安装 PHP，[官方文档](https://www.php.net/manual/en/install.php)提供了丰富的资料可供参考。如果你的时间有限，急需在短时间内安装使用 PHP，可参考本文提供的最佳实践快速进行安装配置。

本文以 PHP 当前最新稳定版本 **7.4.33** 为例，详细介绍了如何在 CentOS 上通过编译源代码的方式安装配置 PHP，以及如何配置启动 PHP 的 FastCGI 进程管理器 **FPM**（[FastCGI 进程管理器](https://www.php.net/manual/en/install.fpm.php)）。本文提供的安装步骤在 CentOS 7中均实际测试过，对于在编译过程中可能遇到的问题也做了详细说明。

本文假设你已使用管理员账户（root）登录操作系统，因为文中出现的命令大部分都需要用到 root 账户权限。如果你是用普通账户登录的，可运行 `sudo -s` 命令切换到 root 账户（或在每条命令前添加 `sudo`）：

```shell
sudo -s
```

本文在用到包管理器时，会统一使用 `yum` 命令，但是要知道在 CenOS 8、CentOS Stream 8 及更新版本的 CentOS 中，`yum` 命令已被 `dnf` 命令替代（以符号链接的形式存在），运行 `yum` 命令实际上是运行 `dnf` 命令。

## 下载 PHP

从 PHP 官方下载页面获取最新稳定版源代码压缩包的下载链接：

**PHP 官方下载下面：**https://www.php.net/downloads

PHP 提供了三种类型的压缩包格式，Gzip（扩展名为 .gz）、Bzip2（扩展名为 .bz2）、XZ（扩展名为 .xz），可根据自己的需要选择，本文以 Gzip 格式为例。

切换到系统的源代码存放目录，将 PHP 的源代码压缩包下载下来：

```shell
cd /usr/local/src
curl -LO https://www.php.net/distributions/php-7.4.33.tar.gz
```

## 编译 PHP

相对于用包管理器安装 PHP，通过源代码编译 PHP 会有较多步骤，并且在编译过程中可能会遇到各种各样的意外情况，但是只要保持耐心，确定问题根源所在，就能有效地解决它。

编译 PHP 主要分两步，一是用 PHP 源代码附带的配置脚本定制功能，生成 [Makefile](https://www.gnu.org/software/make/manual/make.html#Introduction) 文件（待执行的一组任务规则，告诉 Make 工具如何编译和链接程序），二是用 Make 工具根据 Makefile 文件中的规则自动化构建 PHP。下文会详细解释这两个步骤。

### 编译环境

PHP 主要是用 C 语言编写的，因此编译 PHP 需要确保 CentOS 系统中安装了 C 编译器，本文以 **GCC**（[GNU Compiler Collection](https://gcc.gnu.org/)）为例，当然你也可以使用你所喜欢的其它 C 编译器。

另外还需要安装自动化构建工具 **Make**，它的作用是根据配置好的 Makefile 文件自动将源代码构建成可执行程序和库。

运行以下命令为 CentOS 系统安装 GCC 及 make 程序

```shell
yum install -y gcc make
```

### 定制功能

解压缩 PHP 源代码压缩包，并切换到源代码所在目录（注意，除非有说明，所有操作都会在该目录下进行，切勿离开）：

```shell
tar -xvf php-7.4.33.tar.gz
cd php-7.4.33
```

在该目录下可以找到一个名为 **configure** 的 Shell 脚本文件，它是 PHP 源代码附带的“配置脚本（[configure script](https://en.wikipedia.org/wiki/Configure_script)）”。该脚本有两个作用：一是通过它提供的丰富的选项细颗粒度地定制 PHP 功能；二是自动匹配系统中的库和待编译 PHP 所依赖的库（如果依赖库有缺失，该脚本会中断执行并提示缺失库的名称，以便你来解决该依赖）。

运行以下命令查看含有所有可用配置选项的脚本帮助内容（按 q 键退出）：

```shell
./configure --help | less
```

除此外，也可以参考 PHP 官方文档的“[核心配置选项列表](https://www.php.net/manual/en/configure.about.php)”。这些内容对每个配置选项都做了简要说明，想要查看选项所对应功能的详细介绍可以参考 PHP 官方文档的“[功能参考](https://www.php.net/manual/en/funcref.php)”。

至于应该使用哪些选项，与你的实际需求有关。比如，你想要让 PHP 程序支持图形的处理，就需要安装 GD 图形库，否则就不需要安装。如果你的 PHP 应用采用了 Laravel、Symfony、Wordpress 等框架或 [CMS](https://en.wikipedia.org/wiki/Content_management_system)，可以在其文档中找到对 PHP 运行环境的要求。

本文示例假设所编译的 PHP 能够支持 FPM，以便通过 FastCGI 协议与 Web 服务器协作处理对 PHP 程序的请求，并且能够让 PHP 程序支持 [OpenSSL](https://www.php.net/manual/en/book.openssl.php)、[zlib](https://www.php.net/manual/en/book.zlib)、 [cURL](https://www.php.net/manual/en/book.curl)、[GD](https://www.php.net/manual/en/book.image) 图形库（以及 GD [对 webP、JPEG、FreeType 的支持](https://www.php.net/manual/en/image.installation.php)）、 [Multibyte String](https://www.php.net/manual/en/book.mbstring.php)（多字节字符串）、[Zip](https://www.php.net/manual/en/book.zip.php)、访问 MySQL 的两种扩展 [mysqli](https://www.php.net/manual/en/book.mysqli.php) 和 [PDO_MYSQL](https://www.php.net/manual/en/ref.pdo-mysql.php)。这些功能与编译配置选项的对应关系如下所示：

| **配置选项**            | **对应功能**               |
| ----------------------- | -------------------------- |
| –prefix                 | 指定安装位置               |
| –enable-fpm             | 支持 FPM 功能              |
| –with-openssl           | 支持 OpenSSL 功能          |
| –with-zlib              | 支持 zlib 功能             |
| –with-zip               | 支持 Zip 功能              |
| –with-curl              | 支持 cURL 功能             |
| –enable-gd              | 支持 GD 功能               |
| –enable-mbstring        | 支持多字节字符串的处理     |
| –with-mysqli=mysqlnd    | 支持通过 mysqli 访问 MySQL |
| –with-pdo-mysql=mysqlnd | 支持通过 PDO 访问 MySQ     |

要想让 PHP 具备上面所提到的那些附加功能，编译时可能需要依赖很多函数库（[Library](https://en.wikipedia.org/wiki/Library_(computing))），如果缺少某个库，在运行配置脚本时就会中断并抛出提示，不过每次中断只会抛出一个提示。如果上面提到的功能符合你的需求，为避免多次尝试浪费时间，可以直接运行以下命令为操作系统安装这些库

```shell
yum -y install libxml2 libxml2-devel openssl openssl-devel bzip2 bzip2-devel libcurl libcurl-devel libjpeg libjpeg-devel libpng libpng-devel freetype freetype-devel gmp gmp-devel libmcrypt libmcrypt-devel readline readline-devel libxslt libxslt-devel zlib zlib-devel glibc glibc-devel glib2 glib2-devel ncurses curl gdbm-devel db4-devel libXpm-devel libX11-devel gd-devel gmp-devel expat-devel xmlrpc-c xmlrpc-c-devel libicu-devel libmcrypt-devel libmemcached-devel libzip libzip-devel sqlite-devel libwebp-devel
```

> 除此外，为了让 PHP 具备读写 Zip 的功能（比如PHP 依赖管理工具 [Composer](https://getcomposer.org/) 就需要此功能 ），还需要安装一个名为 [Libzip](https://libzip.org/) 的库。

而对于 CentOS 7，通过包安装管理器安装的版本为 0.10，低于 PHP 对 Libzip 的最低版本要求 **0.11**，所以需要额外手动编译。具体方法可参考下面这篇文章：

- [如何在 CentOS 上编译安装最新版 libzip](https://osfere.com/linux/how-to-build-and-install-latest-libzip-on-centos)

安装完成后，运行以下命令将 libzip 的 pkg-config 配置文件目录添加到系统环境变量 `PKG_CONFIG_PATH` 中：

```shell
export PKG_CONFIG_PATH=/usr/local/libzip/lib64/pkgconfig [指定下面安装的libzip的pkg路径,否则编译的时候会提示找不以libzip]
```

这样 PHP 的配置脚本就能自动识别并使用你所安装的 libzip。

此外，如果想让正则表达式功能支持多字节字符串，需要用到名为 **Oniguruma** 的库，但是 CentOS 的默认软件库中不包含该库，需要手动激活或指定软件库。

在 CentOS 8 和 CentOS Stream 8 中可以运行以下命令安装：

```shell
yum install oniguruma-devel --enablerepo=powertools
```

在 CentOS 7 中可以运行以下命令安装：

```shell
yum install oniguruma-devel --enablerepo=epel
```

在安装完编译 PHP 所依赖的软件库后，运行配置脚本进行配置：

```
./configure \
--prefix=/usr/local/php-7.4.33 \                                  [php安装的根目录]
--exec-prefix=/usr/local/php-7.4.33 \                               [php执行文件所在目录]
--bindir=/usr/local/php-7.4.33/bin \                            [php/bin目录]
--sbindir=/usr/local/php-7.4.33/sbin \                            [php/sbin目录]
--includedir=/usr/local/php-7.4.33/include \                    [php包含文件所在目录]
--libdir=/usr/local/php-7.4.33/lib/php \                        [php/lib目录]
--mandir=/usr/local/php-7.4.33/man \                        [php/man目录]
--with-config-file-path=/usr/local/php-7.4.33/etc \               [php的配置目录]
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
```

> ```shell
> ./configure \
> --prefix=/usr/local/php-7.4.33 \
> --exec-prefix=/usr/local/php-7.4.33 \
> --bindir=/usr/local/php-7.4.33/bin \
> --sbindir=/usr/local/php-7.4.33/sbin \
> --includedir=/usr/local/php-7.4.33/include \
> --libdir=/usr/local/php-7.4.33/lib/php \
> --mandir=/usr/local/php-7.4.33/man \
> --with-config-file-path=/usr/local/php-7.4.33/etc \
> --with-mysql-sock=/tmp/mysql.sock \
> --with-mhash \
> --with-openssl \
> --with-mysqli=shared,mysqlnd \
> --with-pdo-mysql=shared,mysqlnd \
> --with-gd \
> --with-iconv \
> --with-zlib \
> --enable-zip \
> --enable-inline-optimization \
> --disable-debug \
> --disable-rpath \
> --enable-shared \
> --enable-xml \
> --enable-bcmath \
> --enable-shmop \
> --enable-sysvsem \
> --enable-mbregex \
> --enable-mbstring \
> --enable-ftp \
> --enable-pcntl \
> --enable-sockets \
> --with-xmlrpc \
> --enable-soap \
> --without-pear \
> --with-gettext \
> --enable-session \
> --with-curl \
> --with-jpeg-dir \
> --with-freetype-dir \
> --enable-opcache \
> --enable-fpm \
> --with-fpm-user=nginx \
> --with-fpm-group=nginx \
> --without-gdbm \
> --enable-fast-install \
> --with-webp \
> --with-jpeg \
> --with-freetype \
> --disable-fileinfo
> ```

注意，命令中的 `--prefix` 选项是用来指定 PHP 安装位置的，你可以选择任意位置，但是作为最佳实践，将其指定为系统的本地程序目录（`/usr/local/php`）是一个不错的选择。当然，你也可以忽略该选项，这会导致 PHP 的安装文件散落在各处（即 `/usr/local` 目录下的 `etc`、`lib`、`bin` 等目录）。

另外，这里使用了 `--with-mysqli `和 `--with-pdo-mysql` 两个选项让 PHP 同时支持通过 mysqli 和 PDO 两种方式访问 MySQL，这是为了兼顾有的 PHP 程序只采用两者之一访问 MySQL。选项的值可以使用 mysqlnd 也可以不带值。关于为何以这样的形式设置对 MySQL 的支持，可参考 PHP 官方文档的“[MySQL原生驱动程序](https://www.php.net/manual/en/book.mysqlnd.php)”。

配置脚本运行完毕，如果你能看到如下所示的提示信息，就表示一切正常，成功生成了 Makefile 文件，可以正式开始进行编译了。

```
+--------------------------------------------------------------------+
| License:                                                           |
| This software is subject to the PHP License, available in this     |
| distribution in the file LICENSE. By continuing this installation  |
| process, you are bound by the terms of this license agreement.     |
| If you do not agree with the terms of this license, you must abort |
| the installation process at this point.                            |
+--------------------------------------------------------------------+

Thank you for using PHP.
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

## 直接安装

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
   > ```shell
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

------

## 编译安装

关于 MySQL 服务器（以下简称 MySQL）在 Linux 系统上的安装及初始化设置，均可在 MySQL 官方文档的“[安装及升级 MySQL](https://dev.mysql.com/doc/refman/8.0/en/installing.html)”章节中找到详细介绍。如果你的时间有限，急需在短时间内安装使用 MySQL，可参考本文提供的最佳实践快速进行安装配置。

本文以 **MySQL Community Server 5.7** 为例，详细介绍了如何在 CenOS 上下载、安装、初始化以及启动 MySQL。本文提供的安装步骤在 CentOS 7中均实际测试过。

本文假设你已使用管理员账户（root）登录操作系统，因为文中出现的命令大部分都需要用到 root 账户权限。如果你是用普通账户登录的，可运行 `sudo -s` 命令切换到 root 账户（或在每条命令前添加 `sudo`）：

```shell
sudo -s
```

本文在用到包管理器时，会统一使用 `yum` 命令，但是要知道在 CenOS 8、CentOS Stream 8 及更新版本的 CentOS 中，`yum` 命令已被 `dnf` 命令替代（以符号链接的形式存在），运行 `yum` 命令实际上是运行 `dnf` 命令

### 选择安装方式

MySQL 提供了多种安装最新版本方式，包括但不限于利用包管理器（如 YUM 或 DNF）从自定义软件库自动下载安装，下载通用二进制分发文件手动安装，以及通过编译源代码手动安装。

这些安装方式各有优缺点。用包管理器安装非常快捷，但是如果用原生软件库安装，往往不是最新版本，如果用 MySQL 提供的软件库，又容易与系统原生软件库的安装文件混在一起，存在潜在的冲突问题，并且还无法定制额外的功能。通过编译源代码安装有很大的定制空间，但是需要自己管理相关依赖，并且需要服务器有较高的配置。

因此，这里选择一种折衷方案，使用通用二进制分发文件来安装 MySQL（即官方文档中“[在 Unix/Linux 上用通用二进制文件安装 MySQL](https://dev.mysql.com/doc/refman/8.0/en/binary-installation.html)”所介绍的方式），这样既可以安装最新版本，又能独立于系统原生软件库的安装文件，还不必花费太多时间编译源代码。

### 下载 MySQL

这里要下载的 MySQL 二进制文件是最小安装分发版（Minimal Install），由于它不包含与调试相关的文件，所以体积比普通二进制分发版小得多。

使用此版本需要确保当前系统环境满足两个条件：**glibc**（GNU C Library）版本大于等于 **2.17** 以及操作系统位数是 **x86_64**。

在终端运行以下命令检查 glibc 版本：

```shell
ldd --version
```

在终端运行以下命令检查操作系统位数：

```shell
uname -m
```

条件满足就可以从 MySQL 官方下载页面下载二进制文件了：

**MySQL 官方下载页面：**https://dev.mysql.com/downloads/mysql/

选择版本时，选择操作系统（Select Operating System）需要选“**Linux – Generic**”。选择操作系统版本（Select OS Version）时需要选择“**Linux – Generic (glibc 2.17) (x86, 64-bit)**”。

接下来在列表中找到“***\*Compressed TAR Archive, Minimal Install\****”并点击后面的【 **Download** 】按钮进入下载页面，接着通过链接“**No thanks, just start my download.**”获取文件的直接下载链接。

切换到系统存放源代码的目录，用 `curl` 命令通过该链接将 MySQL 的二进制分发压缩包下载下来：

```shell
cd /usr/local/src
curl -LO https://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.39-linux-glibc2.12-x86_64.tar.gz
```

### 安装 MySQL

MySQL 二进制分发压缩包下载完毕后，运行一下两条命令将压缩包解压缩，并将得到的文件夹移动到系统的本地程序目录，同时将文件夹重命名为 mysql（即 `/usr/local/mysql`）：

```shell
tar -xvf mysql-5.7.39-linux-glibc2.12-x86_64.tar.gz
mv mysql-5.7.39-linux-glibc2.12-x86_64 /usr/local/mysql
```

CentOS 7 需要运行以下命令安装启动 MySQL 所依赖的库文件：

```shell
yum install -y libaio numactl-libs
```

CentOS 8 或 CentOS Stream 8 需要运行以下命令安装启动 MySQL 所依赖的库文件：

```shell
dnf install -y libaio ncurses-compat-libs
```

至此 MySQL 就安装完成了。如果你是**第一次**按照本文步骤安装 MySQL，为方便之后运行 MySQL 的相关命令，请运行以下两条命令将 MySQL 的可执行文件目录添加到系统环境变量 `PATH` 中：

```shell
echo 'export PATH="/usr/local/mysql/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

现在运行以下命令，应该能够看到所安装 MySQL 的版本号：

```shell
mysql --version
```

### 配置 MySQL

这里配置 MySQL 主要为了实现三个目的，创建 MySQL 专用的系统账户，指定 MySQL 数据的存放位置，添加可供远程 MySQL 客户端使用的数据库用户。

#### 创建专用账户

首先需要创建一个 MySQL 专用账户（这里以 mysql 为例），用于运行 MySQL 服务器，以及存放数据库文件。

创建专用账户可以用任意用户名，本文示例使用 **mysql**。你可以运行以下命令检查系统中是否存在该用户：

```shell
id mysql
```

如果出现类似“no such user”的提示就表示不存在此账户，可以运行以下命令创建一个名为 mysql 的无登陆权限系统账户：

```shell
useradd mysql -s /sbin/nologin
```

#### 初始化数据库

默认情况下，数据库初始化的位置是 MySQL 的程序目录，但是将数据文件和程序文件混放在一起不是一个好习惯，因此这里通过命令选项 `--datadir` 将数据库的初始化位置指定到了 mysql 用户的家目录（`/home/mysql`）下。

运行以下两条命令即可将数据库初始化到 mysql 家目录：

```shell
sudo -u mysql mkdir /home/mysql/data
mysqld --initialize --user=mysql --datadir=/home/mysql/data
```

命令运行成功之后，程序会自动生成一个名为 root 的数据库管理员账户及其临时密码，请记录此密码，之后登录 MySQL 时会用到。

如果嫌记录密码麻烦，也可以将命令中的 `--initialize` 替换成 `--initialize-insecure`，这样程序就会为 root 用户生成一个空密码。

安全起见，切记要在后面的步骤中修改 root 账户的密码。

#### 试运行数据库

数据库初始化成功后就可以尝试启动运行数据库了。在继续下面的步骤之前务必清理一下可能存在的与 MySQL 有关的旧的遗留文件（如果不太确定这些文件后续是否有用，也可备份到其它地方）：

```shell
rm -rf /etc/my.cnf*
```

MySQL 自带一个名为 **mysql.server** 的启动脚本，这里主要用它来启动 MySQL。首先切换到 MySQL 程序目录的 **support-files** 文件夹：

```shell
cd /usr/local/mysql/support-files
```

根据之前的配置信息，按照如下所示，修改脚本 mysql.server 中 **basedir** 和 **datadir** 这两项参数的值：

```shell
basedir=/usr/local/mysql
datadir=/home/mysql/data
```

你可以直接运行下面这两条命令，快速更改：

```shell
sed -i 's/^\(basedir=\)$/\1\/usr\/local\/mysql/' mysql.server
sed -i 's/^\(datadir=\)$/\1\/home\/mysql\/data/' mysql.server
```

接着就可以运行以下命令以用户 mysql 的身份启动 MySQL：

```shell
sudo -u mysql ./mysql.server start
```

如果你能看到下面这条信息且没有任何其它错误的话，就表示 MySQL 的启动和运行一切正常。

```
Starting MySQL. SUCCESS!
```

#### 修改账户密码

在初始化 MySQL 数据库时，程序自动生成了一个名为 root 的数据库用户，这也是数据库的管理员账户，是数据库中最高权限，因此必须为其修改密码。

运行以下命令，以用户名 root 的身份登录 MySQL 数据库：

```shell
mysql -u root -p
```

再出现提示文字“**Enter password:**”后，输入之前生成的临时密码登录（如果之前为 root 生成的是空密码则直接按回车键登录）。如看到如下所示的提示信息，就表示登录成功：

```shell
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 8
Server version: 8.0.27 MySQL Community Server - GPL

Copyright (c) 2000, 2021, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql>
```

接着在提示符 `**mysql>**` 后运行如下所示的 SQL 命令修改 root 用户的密码（请将命令中的 `yourpassword` 改成你要设置的密码）：

```shell
ALTER USER 'root'@'localhost' IDENTIFIED BY 'yourpassword';
```

命令执行后如看到如下所示的提示信息，就表示修改成功：

```shell
Query OK, 0 rows affected (0.01 sec)
```

你可以通过输入 QUIT 命令退出数据库然后重新登录 root 以测试新密码。

#### 添加本地账户

你可以通过管理员账户 root 为数据库添加任意账户，并控制所添加账户的权限。关于数据库账户的 SQL 命令可以参考 MySQL 官方文档“SQL 语句”中的“[账户管理语句](https://dev.mysql.com/doc/refman/8.0/en/account-management-statements.html)”章节。

这里所说的“本地账户”是指用于本机应用程序上的数据库账户。虽然你可以直接使用 root 账户，但是为了安全起见不建议这样做。

建议为每一个数据库创建一个专用账户。假设某应用使用的数据库名为 **myapp**，那么就可以通过运行如下两条 SQL 命令为其创建专用账户（请将命令中的 `yourpassword` 改成你要设置的密码）：

```mysql
CREATE USER 'myapp'@'localhost' IDENTIFIED BY 'yourpassword';
GRANT ALL ON myapp.* TO 'myapp'@'localhost';
```

这里假设应用是在本机访问数据库的，如果是非本机应用访问本机数据库，则需要为数据库添加允许远程访问的账户。

### 添加远程账户

这里所说的“远程账户”是指可以在任意网络位置通过 MySQL 服务器所在主机的 IP 登录数据库的账户。如果没有需要远程使用数据库的应用，也没有用 MySQL 客户端远程管理数据库的需求，则可以忽略此步骤。

下面的示例创建的是一个可以用任意 MySQL 客户端远程管理所有数据库的用户，如果远程账户是用在应用上的，可以像第 5 步那样，在为账户赋权时将其限定在某个数据库上。

首先用管理员账户 root 登录 MySQL，并输入如下所示的两条命令创建一个可在 MySQL 客户端远程登录的数据库用户（请将命令中的 `yourpassword` 改成你要设置的密码）：

```mysql
CREATE USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'yourpassword';
GRANT ALL ON *.* TO 'root'@'%';
```

创建普通账户和远程账户的主要区别是 @ 后的主机名，这里使用了通配符 %，即允许任意主机名或 IP 登录。关于通配符的更多用法可参考[官方文档](https://dev.mysql.com/doc/refman/8.0/en/grant.html#grant-accounts)。

注意不要把新创建的这个 root 账户和管理员账户 root 相混淆，它和管理员账户并非同一个用户。MySQL 会通过登录的主机名或 IP 自动识别是哪个 root，如果是登录的主机名为 localhost 或 IP 为 127.0.0.1，则会认为是管理员账户，否则是新创建的这个 root 账户。

当然，这是一个故意为之的示例，用以说明 ‘root’@’localhost’ 和 ‘root’@’%’ 并非同一个账户。如果你感到混淆，完全可以使用其它任意名称，如 myapp、zhangsan、lisi。

注意，MySQL 自 8.0 开始采用 **caching_sha2_password** 作为默认的身份验证插件，这在本机是没有问题的，但是如果你使用的 MySQL 客户端不支持该验证机制，则最好在创建远程账户时在 SQL 命令中额外声明使用 **mysql_native_password** 身份验证插件。

如果你的云服务器 VPC 安全组规则和 CentOS 系统防火墙均已放行 **3306** 端口入站，那么就可以通过任一 MySQL 客户端登录 MySQL 数据库了。

### 启动 MySQL

到目前为止 MySQL 还是在试运行时启动的。为预防主机宕机或维护性重启导致 MySQL 停止运行，有必要将其作为服务托管给操作系统，这样不论身原因导致的系统重启，MySQL 都能随机启动。

在开始之前，运行以下两条命令停止运行之前手动启动的 MySQL ：

```shell
cd /usr/local/mysql/support-files
sudo -u mysql ./mysql.server stop
```

在 CenOS 中，可以使用系统服务管理器 **[systemd](https://www.freedesktop.org/wiki/Software/systemd/)** 来管理各种服务。要将 MySQL 托管给 systemd，需要为其创建一个配置文件，以便让其管理。

使用你喜欢的编辑器创建一个名为 **mysql.service** 的配置文件到如下所示路径（这里使用的是 vim 编辑器）：

```shell
vim /etc/systemd/system/mysql.service
```

然后将如下所示的内容复制粘贴到该文件中并保存退出（vi 编辑器先按 ESC 再输入 `:wq` 并回车）：

```shell
[Unit]
Description=MySQL database server
After=syslog.target
After=network.target

[Service]
Type=forking
User=mysql
Group=mysql
ExecStart=/usr/local/mysql/support-files/mysql.server start
ExecStop=/usr/local/mysql/support-files/mysql.server stop
ExecReload=/usr/local/mysql/support-files/mysql.server reload

[Install]
WantedBy=multi-user.target
```

接着运行以下命令，让 systemd 读取新创建的配置文件：

```shell
systemctl daemon-reload
```

现在即可通过运行以下命令启动 MySQL 服务：

```shell
systemctl start mysql
```

运行以下命令查看 MySQL 服务的状态：

```shell
systemctl status mysql
```

如果服务的状态为 **Active: active (running)** 且没有显示任何错误或警告信息的话，就表示一切正常。

最后，将 MySQL 作为服务添加到随机启动：

```shell
systemctl enable mysql
```

至此，就完成了在 CentOS 上安装配置 MySQL 的全部流程。

### 升级 MySQL

如果想要升级到 MySQL 今后发布的新版本，只需要按照以上步骤重新操作一到三步即可。因为我们已经把 MySQL 的数据文件和安装文件分离了，所以可以直接用新的二进制文件覆盖旧的二进制文件

### 总结

完成以上流程后，应该能够得到如下所示的这些可用文件或信息。这些信息可能会在维护 MySQL 时用到，也可能会被其它软件用到。

MySQL 数据库文件存放路径：

```shell
/home/mysql/data
```

MySQL 的 Socket 文件路径：

```shell
/tmp/mysql.sock
```

MySQL 的系统服务配置文件路径：

```shell
/etc/systemd/system/mysql.service
```

------

## `docker`安装Mysql配置主从

1. 拉取mysql容器

   ```shell
   docker pull mysql:5.7
   ```

2. 创建主从数据映射目录

   ```
   // 数据和配置不至于丢失
   /mydata/mysql/master/conf.d
   /mydata/mysql/slave/conf.d 
   ```

3. 创建master配置文件`/mydata/mysql/master/conf.d/my.cnf`

   ```
   [mysqld]
   log-bin=master-bin
   server-id=1
   ```

4. 创建slave配置文件`/mydata/mysql/slave/conf.d/my.cnf`

   ```
   [mysqld]
   log-bin=slave-bin
   server-id=2
   ```

5. 启动master服务

   ```shell
   $ docker run -d --name mysql-master \
       -p 3306:3306 \
       -v /mydata/mysql/master/conf.d/data:/var/lib/mysql \
       -v /mydata/mysql/master/conf.d:/etc/mysql/conf.d \
       -e MYSQL_ROOT_PASSWORD=root \
       -d mysql:5.7
   ```

6. 启动slave服务

   ```shell
   $ docker run -d --name mysql-slave \
       -p 3307:3306 \
       -v /mydata/mysql/slave/conf.d/data:/var/lib/mysql \
       -v /mydata/mysql/slave/conf.d:/etc/mysql/conf.d \
       -e MYSQL_ROOT_PASSWORD=root \
       -d mysql:5.7
   ```

7. 进入容器修改master slave的读写权限

   ```shell
   $ docker exec -it mysql-master /bin/sh
   ```

   ```shell
   mysql -u root -p root
   GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION;
   ```

8. 进入mysql-master查看master状态

   ```
   mysql> show master status;
   +-------------------+----------+--------------+------------------+-------------------+
   | File              | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
   +-------------------+----------+--------------+------------------+-------------------+
   | master-bin.000003 |      452 |              |                  |                   |
   +-------------------+----------+--------------+------------------+-------------------+
   ```

9. 获取master容器的host

   ```shell
   $ docker inspect --format='{{.NetworkSettings.IPAddress}}' mysql-master
   172.17.0.2
   ```

10. 进入mysql-slave关联主从模式

    ```shell
    // mysql shell
    change master to master_host='172.17.0.2', master_user='root',master_password='root', master_log_file='master-bin.000003',master_log_pos=452;
    ```

    ```shell
    // mysql shell
    // 启用slave
    start slave;
    ```

11. 测试主从
    在master创建testdb数据库，再到slave中查看是否存在testdb库，若存在则基本完成，若未成功检查binlog、master host、position是否正确
