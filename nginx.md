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

	这块和虚拟主机有密切关系，虚拟主机从用户角度看，和一台独立的硬件主机是完全一样的，该技术的产生是为了节省互联网服务器硬件成本。
	
	每个http块可以包括多个server 块，而每个server块就相当于一个虚拟主机。而每个server块也分为全局 server 块，以及可以同时包含多个 locaton 块。

	1. 全局 server 块

		最常见的配置是本虚拟机主机的监听配置和本虚拟主机的名称或 IP 配置。
	2. location块

		一个server块可以配置多个location块

		这块的主要作用是基于Nginx服务器接收到的请求字符串(例如server_name/uri-string),对虚拟主机名称(也可以是IP别名)之外的字符串(例如前面的/uri-string)过行匹配,对特定的请求处理.地址定向,数据缓存和应答控制等功能,还有许多第三方模块的配置也在这里进行.

# Nginx 配置实例 #

## 反向代理实例 ##

1. 通过访问nginx跳转到Tomcat

		##在nginx进行请求转发的配置（反向代理配置）
	    server {
	        listen       90;
	        server_name  120.77.237.175;
	
	        #charset koi8-r;
	
	        #access_log  logs/host.access.log  main;
	
	        location / {
	            root   html;
	            index  index.html index.htm;
	        	proxy_pass http://172.18.112.35:9080;
	         }
		}

2. 实现效果:使用 nginx 反向代理，根据访问的路径跳转到不同端口的服务中,nginx 监听端口为 91

	- 访问 http://120.77.237.175:91/80/ 直接跳转到 http://120.77.237.175:9080/80/
	- 访问 http://120.77.237.175:91/81/ 直接跳转到 http://120.77.237.175:9081/81/

		- 准备两个 tomcat 服务器，一个 8080 端口，一个 8081 端口

			    server {
			        listen      91;
			    #    listen       somename:8080;
			        server_name  120.77.237.175;
			
			        location ~ /80/ {
			            root   html;
			            index  index.html index.htm;
			            proxy_pass http://172.18.112.35:9080;
			        }
			
			        location ~ /81/ {
			            root   html;
			            index  index.html index.htm;
			            proxy_pass http://172.18.112.35:9081;
			        }
			    }


		location指令说明 该指令用于匹配URL.语法如下:
			
				location [ = | ~ | ~* | ^~] uri {

				}

		1. =: 用于不含正则表达式的uri前,要求请求字符串与uri严格匹配,如果匹配成功,就停止继续向下搜索并立即处理该请求
		2. ~: 用于表示uri包含正则表达式,并且区分大小写
		3. ~*: 用于表示uri包含正则表达式,并且不区分大小写
		4. ^~: 用于不含正则表达式的uri前,要求Nginx服务器找到标识uri和请求字符串匹配度最高的location后,立即使用此location处理请求,而不再使用location块中的正则uri和请求字符串做匹配


		**注意:如果uri包含正则表达式,则必须要有~或者~\*标识**

## 负载均衡实例 ##

1. 实现效果
	1. 浏览器地址输入地址http://120.77.237.175:91/load/a.html,负载均衡效果,平均8080和8081端口中
2. 准备工作
	1. 准备两台Tomcat服务器,一台8080,一台8081
	2. 在两台tomcat里面webapps目录中,创建名科是load文件夹,在load文件夹中创建页面a.html测试

3. 在 nginx 的配置文件中进行负载均衡的配置


		http{
		.......			
		    upstream myserver{
		        server 120.77.237.175:9080;
		        server 120.77.237.175:9081;
		    }
		.......
			 server {
				..........
		        location / {
					........
		            proxy_pass http://myserver;
		         }
			}

		}

	![](http://120.77.237.175:9080/photos/nginx/8.png)

4. nginx 分配服务器策略
	1. 轮询（默认） : 每个请求按时间顺序逐一分配到不同的后端服务器，如果后端服务器 down 掉，能自动剔除。
	2. weight weight : 代表权重默认为 1,权重越高被分配的客户端越多

			upstream myserver{
			        server 120.77.237.175:9080 weight=1;
			        server 120.77.237.175:9081 weight=10;
		    }
	3. ip_hash	: 每个请求按访问 ip 的 hash 结果分配，这样每个访客固定访问一个后端服务器

			upstream myserver{
					ip_hash;
			        server 120.77.237.175:9080 weight=1;
			        server 120.77.237.175:9081 weight=10;
		    }
	4. fair（第三方）  :按后端服务器的响应时间来分配请求，响应时间短的优先分配。

			upstream myserver{
			        server 120.77.237.175:9080 weight=1;
			        server 120.77.237.175:9081 weight=10;
					fair;
		    }

## 动静分离实例 ##

1. 什么是动静分离

Nginx 动静分离简单来说就是把动态跟静态请求分开，不能理解成只是单纯的把动态页面和静态页面物理分离。严格意义上说应该是动态请求跟静态请求分开，可以理解成使用 Nginx 处理静态页面，Tomcat 处理动态页面。动静分离从目前实现角度来讲大致分为两种:

一种是纯粹把静态文件独立成单独的域名，放在独立的服务器上，也是目前主流推崇的方案；

另外一种方法就是动态跟静态文件混合在一起发布，通过 nginx 来分开。

通过 location 指定不同的后缀名实现不同的请求转发。通过 expires 参数设置，可以使浏览器缓存过期时间，减少与服务器之前的请求和流量。具体 Expires 定义：是给一个资源设定一个过期时间，也就是说无需去服务端验证，直接通过浏览器自身确认是否过期即可，所以不会产生额外的流量。此种方法非常适合不经常变动的资源。（如果经常更新的文件，不建议使用 Expires 来缓存），我这里设置 3d，表示在这 3 天之内访问这个 URL，发送一个请求，比对服务器该文件最后更新时间没有变化，则不会从服务器抓取，返回状态码 304，如果有修改，则直接从服务器重新下载，返回状态码 200。

![](http://120.77.237.175:9080/photos/nginx/9.png)

2. 准备工作

	- 建立/tmp/image,tmp/www文件夹,分别在里面加放aaa.jpg和index.html

3. 具体配置

	    server {
	        listen          92;
	        server_name     120.77.237.175;
	
	        location /www/ {
	                root /tmp;
	                index index.html index.htm;
	        }
	
	        location /image/ {
	                root /tmp/;
	                autoindex       on;		##Nginx打开目录浏览功能
	        }
	    }

4. 分别访问http://120.77.237.175:92/image/和http://120.77.237.175:92/www/可看到配置的静态文件

# nginx 搭建高可用集群 #

## Keepalived+Nginx 高可用集群（主从模式） ##

暂略

## Keepalived+Nginx 高可用集群（双主模式） ##

暂略

# Nginx 的原理 #

## mater 和 worker ##

Nginx在启动后，会有一个master进程和多个worker进程

![](http://120.77.237.175:9080/photos/nginx/10.png)

![](http://120.77.237.175:9080/photos/nginx/11.png)

![](http://120.77.237.175:9080/photos/nginx/12.png)

处理流程:

1. HTTP访问请求到nginx
2. nginx的master进程接收到信号,通知各个worker进程(master进程管理监控各个worker)
3. 各个worker进程接收到通知,开始争抢client客户端
4. 因为java语言必须通过Tomcat服务进行解析,所以client用反向代理的方式进行访问

## master-workders的机制的好处 ##

首先,对于每个worker进程来说,独立的进程,不需要加载,所以省掉了锁带来的开销,同时在编程以及问题查找时,也会方便很多,其次,采用独立的进程,可以让互相之间不会影响,一个进程退出后,其它进程还在工作,服务不会中断,master进程则快启动新的workder进程,当然,worker进程的异常退出,肯定是程序有BUG,异常退出,会导致当前worker上的所有请求失败,不过不会影响到所有请求,所以降低了风险

**总结:**

1. **可以使用 nginx –s reload 热部署，利用 nginx 进行热部署操作**
2. **每个 woker 是独立的进程，如果有其中的一个 woker 出现问题，其他 woker 独立的，继续进行争抢，实现请求过程，不会造成服务中断**

## 需要设置多少个worker ##

Nginx同redis类似都采用了io多路复用机制,每个worker都是一个独立的进程,但每个进程里只有一个主进程,通过异步非阻塞的方式来处理请求,即使是上千上万个请求也不在话下.每个worker的线程可以把一个cpu的性能发挥到极致.所以worker数和服务器的cpu数相等是最为适宜的.设少了会浪费cpu,设多了会造成cpu频繁切换上下文带来的损耗

**总结:worker 数和服务器的 cpu 数相等是最为适宜的**

	#设置worker数量
	#4核CPU，开启4个进程
	worker_processes 4
	#work绑定cpu(4 work 绑定 4 cpu)
	worker_cpu_affinity 0001 0010 0100 1000
	
	#8核CPU，开启4个进程
	#work绑定cpu(4 work 绑定 8cpu中的4个)
	worker_cpu_affinity 00000001 00000010 00000100 00001000

	#2核是01，四核是0001，8核是00000001，有多少个核，就有几位数，1表示该内核开启，0表示该内核关闭。
	#0001表示启用第一个CPU内核，0010表示启用第二个CPU内核，依此类推
	#worker_processes最多开启8个，8个以上性能提升不会再提升了，而且稳定性变得更低，所以8个进程够用了。

## 连接数worker_connection ##

这个值表示每个worker进程所能建立连接的最大值,所以,一个nginx能建立的最大连接数,应该是worker_connections * worker_processes.当然,这里说的是最大连接数,对于HTTP请求本地资源来说,能够支持的最大并发数量是worker_connections * worker_processes,如果是支持HTTP1.1的浏览器每次访问要占两个连接,所以普通的静态问最大并发数是:worker_connections * worker_processes/2,而如果是HTTP作为反向代理来说,最大并发数量应该是worker_connections * worker_processes/4.因为作为反向代理服务器,每个并发会建立与客户端的连接与后端服务的连接,会占用两个连接

总结:

1. **发送请求，占用了 woker 的几个连接数？**2 或者 4 个

2. **nginx 有一个 master，有四个 woker，每个 woker 支持最大的连接数 1024，支持的最大并发数是多少？**

	- 普通的静态访问最大并发数是： worker_connections * worker\_processes /2
	- 而如果是 HTTP 作 为反向代理来说，最大并发数量应该是 worker\_connections * worker_processes/4

![](http://120.77.237.175:9080/photos/nginx/13.jpg)