[nginx](http://120.77.237.175:9080/photos/nginx/)

# nginx简介 #

## 什么是nginx ##
Nginx ("engine x") 是一个高性能的 HTTP 和反向代理服务器,特点是占有内存少，并发能力强，处理高并发能力是十分强大的。
## Nginx 作为 web 服务器 ##
Nginx 可以作为静态页面的 web 服务器，同时还支持 CGI 协议的动态语言，比如 perl、php 等。但是不支持 java。Java 程序只能通过与 tomcat 配合完成。Nginx 专为性能优化而开发，性能是其最重要的考量,实现上非常注重效率，能经受高负载的考验,有报告表明能支持高达 50,000 个并发连接数
## 正向代理 ##
Nginx 不仅可以做反向代理，实现负载均衡。还能用作正向代理来进行上网等功能。

正向代理：如果把局域网外的 Internet 想象成一个巨大的资源库，则局域网中的客户端要访问 Internet，则需要通过代理服务器来访问，这种代理服务就称为正向代理。

![](http://120.77.237.175:9080/photos/nginx/1.jpg)

需要在客户端配置代理服务器进行指定网站访问

![](http://120.77.237.175:9080/photos/nginx/2.png)

## 反向代理 ##

反向代理，其实客户端对代理是无感知的，因为客户端不需要任何配置就可以访问，我们只需要将请求发送到反向代理服务器，由反向代理服务器去选择目标服务器获取数据后，在返回给客户端，此时反向代理服务器和目标服务器对外就是一个服务器，暴露的是代理服务器地址，隐藏了真实服务器 IP 地址

![](http://120.77.237.175:9080/photos/nginx/3.jpg)

暴露的是代理服务器地址，隐藏了真实服务器 IP 地址。

![](http://120.77.237.175:9080/photos/nginx/4.png)

## 负载均衡 ##


我们首先想到的可能是升级服务器的配置，比如提高 CPU 执行频率，加大内存等提高机
器的物理性能来解决此问题，但是我们知道摩尔定律的日益失效，硬件的性能提升已经不能满足日益提升的需求了。最明显的一个例子，天猫双十一当天，某个热销商品的瞬时访问量是极其庞大的，那么类似上面的系统架构，将机器都增加到现有的顶级物理配置，都是不能够满足需求的。那么怎么办呢？
上面的分析我们去掉了增加服务器物理配置来解决问题的办法，也就是说纵向解决问题的办法行不通了，那么横向增加服务器的数量呢？这时候集群的概念产生了，单个服务器解决不了，我们增加服务器的数量，然后将请求分发到各个服务器上，将原先请求集中到单个服务器上的情况改为将请求分发到多个服务器上，将负载分发到不同的服务器，也就是我们所说的负载均衡

![](http://120.77.237.175:9080/photos/nginx/5.jpg)

增加服务器的数量，然后将请求分发到各个服务器上，将原先请求集中到单个服务器上的情况改为将请求分发到多个服务器上，将负载分发到不同的服务器，也就是我们所说的负载均衡

![](http://120.77.237.175:9080/photos/nginx/6.png)

## 动静分离 ##

为了加快网站的解析速度，可以把动态页面和静态页面由不同的服务器来解析，加快解析速度。降低原来单个服务器的压力

![](http://120.77.237.175:9080/photos/nginx/7.jpg)

## Nginx 安装 ##

略

## nginx 常用的命令和配置文件 ##

# nginx 常用的命令 #
1. 启动命令 

	在/usr/local/nginx/sbin 目录下执行 ./nginx

2. 关闭命令

	在/usr/local/nginx/sbin 目录下执行  ./nginx -s	stop
3. 重新加载命令

	在/usr/local/nginx/sbin 目录下执行  ./nginx -s	reload

4. 查看 nginx 版本号

	./nginx -v

## nginx.conf 配置文件 ##

1. nginx 配置文件位置
nginx 安装目录下，其默认的配置文件都放在这个目录的 conf 目录下，而主配置文件nginx.conf 也在其中，后续对 nginx 的使用基本上都是对此配置文件进行相应的修改

2. 配置文件中的内容

可以将nginx.conf配置文件分为三部分:

1. 全局块:配置服务器整体运行的配置指令

	从配置文件开始到events块之前的内容,主要会设置一些影响nginx服务器整体运行的配置指令,主要包括配置运行Nginx服务器的用户(组),允许生成的worker process数,进程PID存放路径,日志存放路径和类型以及配置文件的引入等

		worker_processes  1;

	这是Nginx服务器并发处理服务的关键配置,worker_processes值越大,可以支持的并发处理量越多,但是会受到硬件,软件等设备的制约

2. events 块：影响 Nginx 服务器与用户的网络连接

		events {
		    worker_connections  1024;		//表示每个word process支持的最大连接数为1024
		}

	events块涉及的指令主要影响Nginx服务器与用户的网络连接,常用的设置包话是否开启对多work process下的网络连接进行序列化,是否允许同时接收多个网络连接,选取哪种事件驱动模型来处理连接请求,每个word process可以同时支持的最大连接数等.

	这部分的配置对Nginx的性能影响较大,在实际中应该灵活配置

3. http 块

		http {
		    include       mime.types;
		    default_type  application/octet-stream;
		
		    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
		    #                  '$status $body_bytes_sent "$http_referer" '
		    #                  '"$http_user_agent" "$http_x_forwarded_for"';
		
		    #access_log  logs/access.log  main;
		
		    sendfile        on;
		    #tcp_nopush     on;
		
		    #keepalive_timeout  0;
		    keepalive_timeout  65;
		
		    #gzip  on;
		
		    server {
		        listen       90;
		        server_name  localhost;
		
		        #charset koi8-r;
		
		        #access_log  logs/host.access.log  main;
		
		        location / {
		            root   html;
		            index  index.html index.htm;
		        }
		
		        #error_page  404              /404.html;
		
		        # redirect server error pages to the static page /50x.html
		        #
		        error_page   500 502 503 504  /50x.html;
		        location = /50x.html {
		            root   html;
		        }
		
		        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
		        #
		        #location ~ \.php$ {
		        #    proxy_pass   http://127.0.0.1;
		        #}
		
		        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
		        #
		        #location ~ \.php$ {
		        #    root           html;
		        #    fastcgi_pass   127.0.0.1:9000;
		        #    fastcgi_index  index.php;
		        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
		        #    include        fastcgi_params;
		        #}
		
		        # deny access to .htaccess files, if Apache's document root
		        # concurs with nginx's one
		        #
		        #location ~ /\.ht {
		        #    deny  all;
		        #}
		    }
		
		
		    # another virtual host using mix of IP-, name-, and port-based configuration
		    #
		    #server {
		    #    listen       8000;
		    #    listen       somename:8080;
		    #    server_name  somename  alias  another.alias;
		
		    #    location / {
		    #        root   html;
		    #        index  index.html index.htm;
		    #    }
		    #}
		
		
		    # HTTPS server
		    #
		    #server {
		    #    listen       443 ssl;
		    #    server_name  localhost;
		
		    #    ssl_certificate      cert.pem;
		    #    ssl_certificate_key  cert.key;
		
		    #    ssl_session_cache    shared:SSL:1m;
		    #    ssl_session_timeout  5m;
		
		    #    ssl_ciphers  HIGH:!aNULL:!MD5;
		    #    ssl_prefer_server_ciphers  on;
		
		    #    location / {
		    #        root   html;
		    #        index  index.html index.htm;
		    #    }
		    #}
		
		}

这算是 Nginx 服务器配置中最频繁的部分，代理、缓存和日志定义等绝大多数功能和第三方模块的配置都在这里。

需要注意的是：http 块也可以包括 **http 全局块**、**server 块**。

1. http 全局块

http 全局块配置的指令包括文件引入、MIME-TYPE 定义、日志自定义、连接超时时间、单链接请求数上限等。

2. server块