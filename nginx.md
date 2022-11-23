# Nginx简介 #

## 什么是nginx ##
Nginx ("engine x") 是一个高性能的 HTTP 和反向代理服务器,特点是占有内存少，并发能力强，处理高并发能力是十分强大的。
## Nginx 作为 web 服务器 ##
Nginx 可以作为静态页面的 web 服务器，同时还支持 CGI 协议的动态语言，比如 perl、php 等。但是不支持 java。Java 程序只能通过与 tomcat 配合完成。Nginx 专为性能优化而开发，性能是其最重要的考量,实现上非常注重效率，能经受高负载的考验,有报告表明能支持高达 50,000 个并发连接数
## 正向代理 ##
Nginx 不仅可以做反向代理，实现负载均衡。还能用作正向代理来进行上网等功能。

正向代理：如果把局域网外的 Internet 想象成一个巨大的资源库，则局域网中的客户端要访问 Internet，则需要通过代理服务器来访问，这种代理服务就称为正向代理。

![](http://www.dxb02.top/photos/nginx/1.jpg)

需要在客户端配置代理服务器进行指定网站访问

![](http://www.dxb02.top/photos/nginx/2.png)

## 反向代理 ##

反向代理，其实客户端对代理是无感知的，因为客户端不需要任何配置就可以访问，我们只需要将请求发送到反向代理服务器，由反向代理服务器去选择目标服务器获取数据后，在返回给客户端，此时反向代理服务器和目标服务器对外就是一个服务器，暴露的是代理服务器地址，隐藏了真实服务器 IP 地址

![](http://www.dxb02.top/photos/nginx/3.jpg)

暴露的是代理服务器地址，隐藏了真实服务器 IP 地址。

![](http://www.dxb02.top/photos/nginx/4.png)

## 负载均衡 ##


我们首先想到的可能是升级服务器的配置，比如提高 CPU 执行频率，加大内存等提高机
器的物理性能来解决此问题，但是我们知道摩尔定律的日益失效，硬件的性能提升已经不能满足日益提升的需求了。最明显的一个例子，天猫双十一当天，某个热销商品的瞬时访问量是极其庞大的，那么类似上面的系统架构，将机器都增加到现有的顶级物理配置，都是不能够满足需求的。那么怎么办呢？
上面的分析我们去掉了增加服务器物理配置来解决问题的办法，也就是说纵向解决问题的办法行不通了，那么横向增加服务器的数量呢？这时候集群的概念产生了，单个服务器解决不了，我们增加服务器的数量，然后将请求分发到各个服务器上，将原先请求集中到单个服务器上的情况改为将请求分发到多个服务器上，将负载分发到不同的服务器，也就是我们所说的负载均衡

![](http://www.dxb02.top/photos/nginx/5.jpg)

增加服务器的数量，然后将请求分发到各个服务器上，将原先请求集中到单个服务器上的情况改为将请求分发到多个服务器上，将负载分发到不同的服务器，也就是我们所说的负载均衡

![](http://www.dxb02.top/photos/nginx/6.png)

## 动静分离 ##

为了加快网站的解析速度，可以把动态页面和静态页面由不同的服务器来解析，加快解析速度。降低原来单个服务器的压力

![](http://www.dxb02.top/photos/nginx/7.jpg)

## Nginx 安装 ##

略

------

# Nginx基本运行原理

![](http://www.dxb02.top/photos/nginx/14.jpg)

# Nginx 常用的命令 #

1. 启动命令 

	在/usr/local/nginx/sbin 目录下执行 ./nginx

2. 关闭命令

	在/usr/local/nginx/sbin 目录下执行  ./nginx -s	stop
3. 重新加载命令

	在/usr/local/nginx/sbin 目录下执行  ./nginx -s	reload

4. 查看 nginx 版本号

	./nginx -v

# Nginx.conf 配置文件 #

1. nginx 配置文件位置
nginx 安装目录下，其默认的配置文件都放在这个目录的 conf 目录下，而主配置文件nginx.conf 也在其中，后续对 nginx 的使用基本上都是对此配置文件进行相应的修改

2. 配置文件中的内容

可以将nginx.conf配置文件分为三部分:

1. 全局块:配置服务器整体运行的配置指令

	从配置文件开始到events块之前的内容,主要会设置一些影响nginx服务器整体运行的配置指令,主要包括配置运行Nginx服务器的用户(组),允许生成的worker process数,进程PID存放路径,日志存放路径和类型以及配置文件的引入等

		worker_processes  1;	默认为1,表示开启一个业务进程

	这是Nginx服务器并发处理服务的关键配置,worker_processes值越大,可以支持的并发处理量越多,但是会受到硬件,软件等设备的制约

2. events 块：影响 Nginx 服务器与用户的网络连接

		events {
		    worker_connections  1024;		//表示每个word process支持的最大连接数为1024
		}

	events块涉及的指令主要影响Nginx服务器与用户的网络连接,常用的设置包话是否开启对多work process下的网络连接进行序列化,是否允许同时接收多个网络连接,选取哪种事件驱动模型来处理连接请求,每个word process可以同时支持的最大连接数等.

	这部分的配置对Nginx的性能影响较大,在实际中应该灵活配置

3. http 块

		http {
		    include       mime.types;	#引入http mime类型
		    default_type  application/octet-stream;	#如果mime类型没匹配上,默认使用二进制流的方式传输
		
		    sendfile        on;		#使用linux的sendfile(socket,file,len)高效网络传输,也就是数据0拷贝
		
		    keepalive_timeout  65;
		
		    #gzip  on;
		
			#虚拟主机 vhost
		    server {
		        listen       80;	#监听端口号
		        server_name  localhost;		#域名,主机 名
		
				#http://www.dxb02.top/xxoo/index.html	下面/匹配的是/xxoo/index.html
		        location / {	#匹配路径
		            root   html;	#文件根目录
		            index  index.html index.htm;		#默认页名称
		        }
		
				#当上面的访问地址无法访问就 访问http://www.dxb02.top/50x.html
		        error_page   500 502 503 504  /50x.html;	#报错编码对应页面
		        location = /50x.html {
		            root   html;
		        }
		
		    }
	
	> 未开启sendfile
	>
	> ![](http://www.dxb02.top/photos/nginx/15.jpg)
	>
	> 开启后
	>
	> ![](http://www.dxb02.top/photos/nginx/16.jpg)

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

------

# Nginx 配置实例 #

## 虚拟主机

原本一台服务器只能对应一个站点，通过虚拟主机技术可以虚拟化成多个站点同时对外提供服务

### servername匹配规则

我们需要注意的是servername匹配分先后顺序，写在前面的匹配上就不会继续往下匹配了。

### 完整匹配

可以在同一servername中匹配多个域名

```
server_name vod.mmban.com www1.mmban.com;
```

### 通配符匹配

```
server_name *.mmban.com
```

### 通配符结束匹配

```
server_name *.mmban.com
```

### 通配符结束匹配

```
server_name vod.*;
```

### 正则匹配

```
server_name ~^[0-9]+\.mmban\.com$;
```

## 反向代理实例 ##

1. 通过访问`nginx`跳转到`Tomcat`

		##在nginx进行请求转发的配置（反向代理配置）
		server {
		    listen       90;
		    server_name  120.77.237.175;
		
		    location / {
		        root   html;
		        index  index.html index.htm;
		    	proxy_pass http://172.18.112.35:9080;
		     }
		}
	
2. 实现效果:使用 nginx 反向代理，根据访问的路径跳转到不同端口的服务中,nginx 监听端口为 91

	- 访问 http://120.77.237.175:91/80/ 直接跳转到 http://www.dxb02.top/80/
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

​	

------

### 使用一个location

location指令说明 该指令用于匹配URL.语法如下:


	location [ = | ~ | ~* | ^~] uri {
	
	}

1. `=`: 用于不含正则表达式的uri前,要求请求字符串与uri严格匹配,如果匹配成功,就停止继续向下搜索并立即处理该请求
2. `~`: 用于表示uri包含正则表达式,并且区分大小写
3. `~*`: 用于表示uri包含正则表达式,并且不区分大小写
4. `^~`: 用于不含正则表达式的uri前,要求Nginx服务器找到标识uri和请求字符串匹配度最高的location后,立即使用此location处理请求,而不再使用location块中的正则uri和请求字符串做匹配
5. `/` :通用匹配，任何请求都会匹配到

------

### location匹配顺序

- 多个正则location直接按书写顺序匹配，成功后就不会继续往后面匹配 
- 普通（非正则）location会一直往下，直到找到匹配度最高的（最大前缀匹配）
- 当普通location与正则location同时存在，如果正则匹配成功,则不会再执行普通匹配 
- 所有类型location存在时，“=”匹配 > “^~”匹配 > 正则匹配 > 普通（最大前缀匹配）

```
location ~*/(css|img|js) {
    root /usr/local/nginx/static;
    index index.html index.htm;
}
```

### alias与root

```
location /css {
    alias /usr/local/nginx/static/css;
    index index.html index.htm;
}
```

root用来设置根目录，而alias在接受请求的时候在路径上不会加上location。

1. `alias`指定的目录是准确的，即`location`匹配访问的path目录下的文件直接是在alias目录下查找的；
2. `root`指定 的目录是`location`匹配访问的`path`目录的上一级目录,这个path目录一定要是真实存在root指定目录下的；
3. 使用 `alias`标签的目录块中不能使用`rewrite`的`break`（具体原因不明）；另外，`alias`指定的目录后面必须要加上"/"符 号！！
4. alias虚拟目录配置中，location匹配的path目录如果后面不带"/"，那么访问的url地址中这个path目录后 面加不加"/"不影响访问，访问时它会自动加上"/"； 但是如果location匹配的path目录后面加上"/"，那么访问的url地 址中这个path目录必须要加上"/"，访问时它不会自动加上"/"。如果不加上"/"，访问就会失败！
5. root目录配置 中，location匹配的path目录后面带不带"/"，都不会影响访问。

> ​	**注意:如果uri包含正则表达式,则必须要有\~或者\~\*标识**

## 负载均衡实例 ##

1. 实现效果
	1. 浏览器地址输入地址http://120.77.237.175:91/load/a.html,负载均衡效果,平均8080和8081端口中
2. 准备工作
	1. 准备两台Tomcat服务器,一台8080,一台8081
	2. 在两台tomcat里面webapps目录中,创建名科是load文件夹,在load文件夹中创建页面a.html测试

3. 在 `nginx `的配置文件中进行负载均衡的配置


		http{
		.......			
		    upstream myserver{
		        server www.dxb02.top;
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

![](http://www.dxb02.top/photos/nginx/8.png)

4. `nginx `分配服务器策略
	
	1. 轮询（默认） : 每个请求按时间顺序逐一分配到不同的后端服务器，如果后端服务器 down 掉，能自动剔除。

	2. `weight`(权重) : 代表权重默认为 1,权重越高被分配的客户端越多
	
	   ```nginx
	   upstream myserver{
	           server www.dxb02.top weight=1;
	           server 120.77.237.175:9081 weight=10;
	   }
	   ```
	
	3. `ip_hash`: 每个请求按访问 ip 的 hash 结果分配，这样每个访客固定访问一个后端服务器
	
	   ```nginx
	   upstream myserver{
	   		ip_hash;
	           server www.dxb02.top weight=1;
	           server 120.77.237.175:9081 weight=10;
	   }
	   ```
	
	4. `fair`（第三方）  :按后端服务器的响应时间来分配请求，响应时间短的优先分配。
	
	   ```nginx
	   upstream myserver{
	           server www.dxb02.top weight=1;
	           server 120.77.237.175:9081 weight=10;
	   		fair;
	   }
	   ```
	
	5. `least_conn `最少连接访问
	
	6. `url_hash `根据用户访问的url定向转发请求
	
	> ```nginx
	> upstream httpd {
	>     server 127.0.0.1:8050 weight=10 down;
	>     server 127.0.0.1:8060 weight=1;
	>     server 127.0.0.1:8060 weight=1 backup;
	> }
	> ```
	>
	> - down：表示当前的server暂时不参与负载 
	> - weight：默认为1.weight越大，负载的权重就越大。 
	> - backup： 其它所有的非backup机器down或者忙的时候，请求backup机器
	
	> **注意:在实际生产环境高并发情景下,上面的分配策略基本是用不上的,只有轮询可能有实际用途,其它只作了解即可,无实际作用**

## 动静分离实例 ##

1. 什么是动静分离

   Nginx 动静分离简单来说就是把动态跟静态请求分开，不能理解成只是单纯的把动态页面和静态页面物理分离。严格意义上说应该是动态请求跟静态请求分开，可以理解成使用 Nginx 处理静态页面，Tomcat 处理动态页面。动静分离从目前实现角度来讲大致分为两种:

   一种是纯粹把静态文件独立成单独的域名，放在独立的服务器上，也是目前主流推崇的方案；

   另外一种方法就是动态跟静态文件混合在一起发布，通过 nginx 来分开。

   通过 location 指定不同的后缀名实现不同的请求转发。通过 expires 参数设置，可以使浏览器缓存过期时间，减少与服务器之前的请求和流量。具体 Expires 定义：是给一个资源设定一个过期时间，也就是说无需去服务端验证，直接通过浏览器自身确认是否过期即可，所以不会产生额外的流量。此种方法非常适合不经常变动的资源。（如果经常更新的文件，不建议使用 Expires 来缓存），我这里设置 3d，表示在这 3 天之内访问这个 URL，发送一个请求，比对服务器该文件最后更新时间没有变化，则不会从服务器抓取，返回状态码 304，如果有修改，则直接从服务器重新下载，返回状态码 200。

   > **注意:在实际应用中,Tomcat和Nginx之间的动静分离访问并发效率几乎是相同的,并不存在Nginx的效率高于Tomcat的说法,因为Tomcat在对一个请求链接时会缓存当前会话信息,并不会每次请求都会重复新建一个会话Session**

![](http://www.dxb02.top/photos/nginx/9.png)

2. 准备工作

  - 建立/tmp/image,tmp/www文件夹,分别在里面加放aaa.jpg和index.html

3. 具体配置

   ```nginx
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
   ```

4. 分别访问http://120.77.237.175:92/image/和http://120.77.237.175:92/www/可看到配置的静态文件

------

## `UrlRewrite`

`rewrite`语法格式及参数语法:

```
rewrite是实现URL重写的关键指令，根据regex (正则表达式)部分内容，
重定向到replacement，结尾是flag标记。

rewrite 	<regex> 	<replacement> 	[flag];
关键字 		正则 			替代内容 	  flag标记


关键字：其中关键字error_log不能改变
正则：perl兼容正则表达式语句进行规则匹配
替代内容：将正则匹配的内容替换成replacement
flag标记：rewrite支持的flag标记


rewrite参数的标签段位置：
server,location,if


flag标记说明：
last #本条规则匹配完成后，继续向下匹配新的location URI规则
break #本条规则匹配完成即终止，不再匹配后面的任何规则
redirect #返回302临时重定向，浏览器地址会显示跳转后的URL地址	(针对爬虫的,搜索爬虫进入临时重定向)
permanent #返回301永久重定向，浏览器地址栏会显示跳转后的URL地址 (针对爬虫的,搜索爬虫进入永 重定向)
```

实例 

```
rewrite 	^/([0-9]+).html$ 	/index.jsp?pageNum=$1 	break;
```

------

## 同时使用负载均衡(网关)

> 只可通过内网进行访问,外网无法进行访问

应用服务器防火墙配置

- 开启防火墙

  ```shell
  systemctl start firewalld
  ```

- 重启防火墙

  ```shell
  systemctl restart firewalld
  ```

- 重载规则

  ```shell
  firewall-cmd --reload
  ```

- 查看已配置规则

  ```shell
  firewall-cmd --list-all
  ```

- 指定端口和ip访问

  ```shell
  firewall-cmd --permanent --add-rich-rule="rule family="ipv4" source address="192.168.44.101" port protocol="tcp" port="8080" accept"
  ```

- 移除规则

  ```shell
  firewall-cmd --permanent --remove-rich-rule="rule family="ipv4" source address="192.168.44.101" port port="8080" protocol="tcp" accept"
  ```

- 网关配置

  ```nginx
  upstream httpds {
      server 192.168.44.102 weight=8 down;
      server 192.168.44.103:8080 weight=2;
      server 192.168.44.104:8080 weight=1 backup;
  }
  
  location / {
      rewrite ^/([0-9]+).html$ /index.jsp?pageNum=$1 redirect;
      proxy_pass http://httpds ;
  }
  
  ```

------

## 防盗链配置

```
valid_referers none | blocked | server_names | strings ....;
```

- none， 检测 Referer 头域不存在的情况。 
- blocked，检测 Referer 头域的值被防火墙或者代理服务器删除或伪装的情况。这种情况该头域的值不以 “http://” 或 “https://” 开头。 
- server_names ，设置一个或多个 URL ，检测 Referer 头域的值是否是这些 URL 中的某一个。

在需要防盗链的location中配置

```nginx
valid_referers 192.168.44.101;
    if ($invalid_referer) {
    return 403;
}
```

使用curl测试

```shell
curl -I http://192.168.44.101/img/logo.png
```

返回信息如下

```shell
HTTP/1.1 200 OK
Accept-Ranges: bytes
Cache-Control: private, no-cache, no-store, proxy-revalidate, no-transform
Connection: keep-alive
Content-Length: 277
Content-Type: text/html
Date: Fri, 05 Aug 2022 02:23:02 GMT
Etag: "575e1f65-115"
Last-Modified: Mon, 13 Jun 2016 02:50:13 GMT
Pragma: no-cache
Server: bfe/1.0.8.18
```

带引用

```shell
curl -e "http://baidu.com" -I http://192.168.44.101/img/logo.png
```

当nginx配置了防盗链,测试带引用referrer时,返回是否允许此地址进行访问



# nginx 搭建高可用集群 #

## Keepalived+Nginx 高可用集群（主从模式） ##

暂略

## Keepalived+Nginx 高可用集群（双主模式） ##

暂略

------

# Https证书配置

```nginx
server {
  #监听443端口
    listen 443;
    #你的域名
    server_name huiblog.top; 
    ssl on;
    #ssl证书的pem文件路径(默认是读取nginx目录下的conf文件夹,也可以如下自定义目径)
    ssl_certificate  /root/card/huiblog.top.pem;
    #ssl证书的key文件路径(默认是读取nginx目录下的conf文件夹,也可以如下自定义目径)
    ssl_certificate_key /root/card/huiblog.top.key;
    location / {
     proxy_pass  http://公网地址:项目端口号;
	}
}

server {
    listen 80;
    server_name huiblog.top;
    #将请求转成https
    rewrite ^(.*)$ https://$host$1 break;
}
```

------

# Nginx 的原理 #

## mater 和 worker ##

Nginx在启动后，会有一个master进程和多个worker进程

![](http://www.dxb02.top/photos/nginx/10.png)

![](http://www.dxb02.top/photos/nginx/11.png)

![](http://www.dxb02.top/photos/nginx/12.png)

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

```nginx
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
```

## 连接数worker_connection ##

这个值表示每个worker进程所能建立连接的最大值,所以,一个nginx能建立的最大连接数,应该是worker_connections * worker_processes.当然,这里说的是最大连接数,对于HTTP请求本地资源来说,能够支持的最大并发数量是worker_connections * worker_processes,如果是支持HTTP1.1的浏览器每次访问要占两个连接,所以普通的静态问最大并发数是:worker_connections * worker_processes/2,而如果是HTTP作为反向代理来说,最大并发数量应该是worker_connections * worker_processes/4.因为作为反向代理服务器,每个并发会建立与客户端的连接与后端服务的连接,会占用两个连接

> 总结:
>
> 1. **发送请求，占用了 woker 的几个连接数？**2 或者 4 个
>
> 2. **nginx 有一个 master，有四个 woker，每个 woker 支持最大的连接数 1024，支持的最大并发数是多少？**
>
> 	- 普通的静态访问最大并发数是： worker_connections * worker\_processes /2
> 	- 而如果是 HTTP 作 为反向代理来说，最大并发数量应该是 worker\_connections * worker_processes/4
>
> ![](http://www.dxb02.top/photos/nginx/13.jpg)
>

------

# Nginx高级

## 单机垂直扩容：硬件资源增加

```
云服务资源增加
整机：IBM、浪潮、DELL、HP等
CPU/主板：更新到主流
网卡：10G/40G网卡
磁盘：SAS(SCSI) HDD（机械）、HHD（混合）、SATA SSD、PCI-e SSD、 MVMe SSD
SSD
多副本机制
系统盘/热点数据/数据库存储
HDD
冷数据存储
```

## 水平扩展：集群化

### 会话管理

#### Nginx高级负载均衡

- **ip_hash**(通过ip hash计算访问指定的服务器)

  > ip_hash是容易理解的，但是因为仅仅能用ip这个因子来分配后端，因此ip_hash是有缺陷的，不能在一些情况下使用：
  >
  > 1.  nginx不是最前端的服务器，ip_hash要求nginx一定是最前端的服务器，否则nginx得不到正确ip，就不能根据ip作hash。譬如使用的是squid为最前端，那么nginx取ip时只能得到squid的服务器ip地址，用这个地址来作分流是肯定错乱的。
  > 2. nginx的后端还有其它方式的负载均衡。假如nginx后端又有其它负载均衡，将请求又通过另外的方式分流了，那么某个客户端的请求肯定不能定位到同一台session应用服务器上。这么算起来，nginx后端只能直接指向应用服务器，或者再搭一个squid，然后指向应用服务器。最好的办法是用location作一次分流，将需要session的部分请求通过ip_hash分流，剩下的走其它后端去

  ```nginx
  upstream httpget{
  	ip_hash;
  	server 192.168.50.11;
  	server 192.168.50.12;
  }
  
  server{
      listen 80;
      server_name localhost;
      lcation / {
          proxy_pass http://httpget;
         
      }
  }
  ```

  > 问题:
  >
  > 1. ip集中访问到后面服务器
  > 2. 后端服务器如果有一台宕机,会大流量访问到另外一台服务器
  > 3. 只适合中心型项目,可以快速扩容,后端代码都无需进行变更调整

- **hash    $cookie_jsessionid;**(根据cookie hash进行访问)

  >  我们已经知道了如果使用Servlet Session的话，Web容器会自动的在用户浏览器上建立名为jsessionid的cookie，并且值就是服务器端的SessionID。另一方面，新版的NGINX不光可以通过IP的hash来分发流量，也可以通过url的hash，cookie的hash，header的hash等等进行链接的固定分配。由于用户登陆成功以后名为jsessionid的cookie就有了一个短期固定的值，而且每个用户都不一样，那么我们就可以根据这个sessionid的hash值为它分配一个服务器。在当前sessionID起作用的时候那么分配的服务器也是同一个，并且不需要安装第三方的插件

  ```nginx
  upstream httpget{
  	hash  $cookie_jsessionid;
  	server 192.168.50.11;
  	server 192.168.50.12;
  }
  
  server{
      listen 80;
      server_name localhost;
      lcation / {
          proxy_pass http://httpget;
         
      }
  }
  ```

  > 1. 登录系统 系统下发一个文件->客户端
  > 2. 客户端 携带这个文件cookie

- **hash    $request_uri;** (/index.jsp?pageNum=100&jsessionid=123456)(根据请求uri hash地址进行访问)

  ```nginx
  upstream httpget{
  	hash  $request_uri;
  	server 192.168.50.11;
  	server 192.168.50.12;
  }
  
  server{
      listen 80;
      server_name localhost;
      lcation / {
          proxy_pass http://httpget;
         
      }
  }
  ```

  > 1. 在不支持cookie的情况下
  > 2. 资源不平平均分区

  > 以上三种方式在不调整代码的情况下,根据集群nginx配置已经可以满足90%以上的项目了

  **使用lua逻辑定向分发**

  *Redis + SpringSession**

  ```nginx
     upstream httpds {
         ip_hash;
         server 192.168.44.102 ;
         server 192.168.44.103 ;
     }
  
  
      server {
          listen       80;
          server_name  localhost;
  
          location / {
  	    proxy_pass http://httpds;
  
        	# root   html;
          }
      
      
      
         location ~*/(css|img|js) {
       
          root   /usr/local/nginx/html;
  
      }
  ```

------

#### sticky模块对Nginx的负载均衡

> sticky模块使用cookie进行负载均衡

**使用参考**

http://nginx.org/en/docs/http/ngx_http_upstream_module.html#sticky

tengine中有session_sticky模块我们通过第三方的方式安装在开源版本中

sticky是第三方模块，需要重新编译Nginx,他可以对Nginx这种静态文件服务器使用基于cookie的负载均衡

1. 下载模块

   - **项目官网**

     https://bitbucket.org/nginx-goodies/nginx-sticky-module-ng/src/master/

   - 另外一个版本

     https://github.com/bymaximus/nginx-sticky-module-ng

   - **下载**

     https://bitbucket.org/nginx-goodies/nginx-sticky-module-ng/get/1.2.6.zip

2. 上传解压

3. 重新编译Nginx

   安装依赖`openssl-devel`

   ```shell
   yum install openssl-devel -y
   ```

   **进入nginx源码目录进行重新编译**

   ```shell
   ./configure --prefix=/opt/nginx --add-module=/opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d --without-pcre2
   ```

   > 注意:
   >
   > 这里因为使用的nginx版本比较新,是1.22,从1.21版本开始,编译会使用 PCRE2 库构建,而sticky是使用PCRE1库构建的,因此在执行该版本以下时可直接使用如下命令,否则使用如下命令
   >
   > ```
   > ./configure --prefix=/opt/nginx --add-module=/opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d
   > ```
   >
   > 相关issue
   >
   > https://github.com/SpiderLabs/ModSecurity/issues/2756
   >
   > https://github.com/SpiderLabs/ModSecurity-nginx/issues/261

   **执行make**

   ```shell
   make
   ```

4. 配置方法

   ```nginx
   upstream httpget {
   
       sticky;
   
       server 192.168.44.102;
       server 192.168.44.103;
   }
   ```

5. 访问测试效果如下,可以看到访问生成的cookie里会有一个带有route的参数,此参数也可以进行配置,如下

   ![](http://www.dxb02.top/photos/nginx/17.jpg)

   ```nginx
   upstream httpget {
   
       sticky name=route expires=6h;
   
       server 192.168.44.102;
       server 192.168.44.103;
   }
   ```

> **如遇报错修改源码**
>
> ```shell
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:152:15: error: 'MD5_DIGEST_LENGTH' undeclared (first use in this function)
>    u_char hash[MD5_DIGEST_LENGTH];
>                ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:152:15: note: each undeclared identifier is reported only once for each function it appears in
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:152:10: error: unused variable 'hash' [-Werror=unused-variable]
>    u_char hash[MD5_DIGEST_LENGTH];
>           ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c: In function 'ngx_http_sticky_misc_sha1':
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:171:15: error: 'SHA_DIGEST_LENGTH' undeclared (first use in this function)
>    u_char hash[SHA_DIGEST_LENGTH];
>                ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:171:10: error: unused variable 'hash' [-Werror=unused-variable]
>    u_char hash[SHA_DIGEST_LENGTH];
>           ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c: In function 'ngx_http_sticky_misc_hmac_md5':
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:189:15: error: 'MD5_DIGEST_LENGTH' undeclared (first use in this function)
>    u_char hash[MD5_DIGEST_LENGTH];
>                ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:190:12: error: 'MD5_CBLOCK' undeclared (first use in this function)
>    u_char k[MD5_CBLOCK];
>             ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:190:10: error: unused variable 'k' [-Werror=unused-variable]
>    u_char k[MD5_CBLOCK];
>           ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:189:10: error: unused variable 'hash' [-Werror=unused-variable]
>    u_char hash[MD5_DIGEST_LENGTH];
>           ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c: In function 'ngx_http_sticky_misc_hmac_sha1':
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:237:15: error: 'SHA_DIGEST_LENGTH' undeclared (first use in this function)
>    u_char hash[SHA_DIGEST_LENGTH];
>                ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:238:12: error: 'SHA_CBLOCK' undeclared (first use in this function)
>    u_char k[SHA_CBLOCK];
>             ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:238:10: error: unused variable 'k' [-Werror=unused-variable]
>    u_char k[SHA_CBLOCK];
>           ^
> /opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d/ngx_http_sticky_misc.c:237:10: error: unused variable 'hash' [-Werror=unused-variable]
>    u_char hash[SHA_DIGEST_LENGTH];
>           ^
> cc1: all warnings being treated as errors
> 
> ```
>
> > 打开 `ngx_http_sticky_misc.c`文件
> >
> > 在12行添加
> >
> > ```c
> > #include <openssl/sha.h>
> > #include <openssl/md5.h>
> > ```
> >
> > **备份之前的程序**
> >
> > ```shell
> > mv /usr/local/nginx/sbin/nginx /usr/local/nginx/sbin/nginx.old
> > ```
> >
> > 把编译好的Nginx程序替换到原来的目录里(上面编译时已经指定路径就无需再)
> >
> > ```shell
> > cp objs/nginx /usr/local/nginx/sbin/
> > ```
> >
> > **升级检测**
> >
> > ```
> > make upgrade
> > ```
> >
> > 检查程序中是否包含新模块
> >
> > ```shell
> > nginx -V
> > ```
> >
> > ```shell
> > nginx version: nginx/1.22.0
> > built by gcc 4.8.5 20150623 (Red Hat 4.8.5-44) (GCC) 
> > configure arguments: --prefix=/opt/nginx --add-module=/opt/nginx-goodies-nginx-sticky-module-ng-c78b7dd79d0d --without-pcre2
> > ```

> 注意:
>
> 上面使用的是nginx平滑升级(待解)
>
> 按照原来的编译参数安装 nginx 的方法进行安装，**只需要到 make，千万不要 make install** 。如果make install 会将原来的配置文件覆盖

------

### KeepAlive

在http协议header中可以看到当前连接状态

> Nginx关于keepalive连接保持的特性，实际上就是在一次TCP连接中，可以持续处理多个客户请求，而不断开连接。通过该机制可以减少TCP连接的建立次数，减少TIME_WAIT的状态连接。从而增加服务的吞吐量和整体服务质量。但是，长时间的TCP连接会导致系统资源被长时间占用，浪费资源，所以在实际使用的时候，还需要为keepalive设置合理的 timeout

#### 测试工具charles

**下载地址**

https://www.charlesproxy.com/assets/release/4.6.2/charles-proxy-4.6.2-win64.msi?k=fc1457e312

**官网**

https://www.charlesproxy.com

#### 什么时候使用？

- 明显的预知用户会在当前连接上有下一步操作
- 复用连接，有效减少握手次数，尤其是https建立一次连接开销会更大

#### 什么时候不用？

- 访问内联资源一般用缓存，不需要keepalive
- 长时间的tcp连接容易导致系统资源无效占用

#### 对客户端使用keepalive

- **keepalive_time** 

  限制keepalive保持连接的最大时间(1.19.10新功能)

- **keepalive_timeout**

  用于设置Nginx服务器与客户端保持连接的超时时间

  用于踢出不活动连接

  keepalive_timeout = 0 即关闭

  - send_timeout 10;  10秒
  - send_timeout 10 10; 同时下发一个header 告诉浏览器

- **send_timeout**

  两次向客户端写操作之间的间隔 如果大于这个时间则关闭连接 默认60s

  **此处有坑**，注意耗时的同步操作有可能会丢弃用户连接

  该设置表示Nginx服务器与客户端连接后，某次会话中服务器等待客户端响应超过10s，就会自动关闭连接。

- **keepalive_requests**

  默认1000

  单个连接中可处理的请求数

- **keepalive_disable**

  不对某些浏览器建立长连接

  默认msie6(IE6)
  
  ```nginx
  http {
      include       mime.types;
      default_type  application/octet-stream;
  
      sendfile        on;
  
      keepalive_timeout  65 65; #超过这个时间 没有活动，会让keepalive失效 
      keepalive_time 1h; # 一个tcp连接总时长，超过之后 强制失效
    
      send_timeout 60;# 默认60s  此处有坑！！ 系统中 若有耗时操作，超过 send_timeout 强制断开连接。 注意：准备过程中，不是传输过程
  
  
      keepalive_requests 1000;  #一个tcp复用中 可以并发接收的请求个数
  ```
  

------

### 对上游服务器使用keepalive

首先需要配置使用http1.1协议。以便建立更高效的传输，默认使用http1.0，在http1.0中需要配置header才能

在Upstream中所配置的上游服务器默认都是用短连接，即每次请求都会在完成之后断开

#### 相关配置

##### upstream中配置

- **keepalive 100;**

  向上游服务器的保留连接数

- **keepalive_timeout  **

  连接保留时间

- **keepalive_requests ** 

  一个tcp复用中 可以并发接收的请求个数

##### server中配置

```
proxy_http_version 1.1;
配置http版本号
默认使用http1.0协议，需要在request中增加”Connection： keep-alive“ header才能够支持，而HTTP1.1默认支持。
proxy_set_header Connection "";
清楚close信息
```

> proxy_set_header：**即允许重新定义或添加字段传递给代理服务器的请求头。该值可以包含文本、变量和它们的组合。**在没有定义proxy_set_header时会继承之前定义的值。默认情况下，只有两个字段被重定义：
>
> ```nginx
> proxy_set_header Host $proxy_host;
> proxy_set_header Connection close;
> ```
>
> 如果启用缓存，来自之前请求的头字段“If-Modified-Since”, “If-Unmodified-Since”, “If-None-Match”, “If-Match”, “Range”, 和 “If-Range” 将不会被代理服务器传递。
> 一个不会变化的“Host”头请求字段可通过如下方式被传递：
>
> ```nginx
> proxy_set_header Host    $http_host;
> ```
>
> 然后，当字段不在请求头中就无法传递了，在这种情况下，可通过设置Host变量，将需传递值赋给Host变量
>
> ```nginx
> proxy_set_header Host    $host;
> ```
>
> 此外，服务器名称和端口一起通过代理服务器传递
>
> ```nginx
> proxy_set_header Host    $host:$proxy_port;
> ```
>
> 如果请求头的存在空的字段将不会通过代理服务器传递出去
>
> ```nginx
> proxy_set_header Accept-Encoding "";
> ```
>
> 简而言之，proxy_set_header 就是可设置请求头-并将头信息传递到服务器端，不属于请求头的参数中也需要传递时，重定义下即可！

------

### AB安装

```shell
yum install httpd-tools
```

参数说明：

- -n  即requests，用于指定压力测试总共的执行次数。
- -c  即concurrency，用于指定的并发数。
- -t  即timelimit，等待响应的最大时间(单位：秒)。
- -b  即windowsize，TCP发送/接收的缓冲大小(单位：字节)。
- -p  即postfile，发送POST请求时需要上传的文件，此外还必须设置-T参数。
- -u  即putfile，发送PUT请求时需要上传的文件，此外还必须设置-T参数。
- -T  即content-type，用于设置Content-Type请求头信息，例如：application/x-www-form-urlencoded，默认值为text/plain。
- -v  即verbosity，指定打印帮助信息的冗余级别。
- -w  以HTML表格形式打印结果。
- -i  使用HEAD请求代替GET请求。
- -x  插入字符串作为table标签的属性。
- -y  插入字符串作为tr标签的属性。
- -z  插入字符串作为td标签的属性。
- -C  添加cookie信息，例如："Apache=1234"(可以重复该参数选项以添加多个)。
- -H  添加任意的请求头，例如："Accept-Encoding: gzip"，请求头将会添加在现有的多个请求头之后(可以重复该参数选项以添加多个)。
- -A  添加一个基本的网络认证信息，用户名和密码之间用英文冒号隔开。
- -P  添加一个基本的代理认证信息，用户名和密码之间用英文冒号隔开。
- -X  指定使用的和端口号，例如:"126.10.10.3:88"。
- -V  打印版本号并退出。
- -k  使用HTTP的KeepAlive特性。
- -d  不显示百分比。
- -S  不显示预估和警告信息。
- -g  输出结果信息到gnuplot格式的文件中。
- -e  输出结果信息到CSV格式的文件中。
- -r  指定接收到错误信息时不退出程序。
- -h  显示用法信息，其实就是ab -help。

#### 测试完整配置

```nginx
    upstream httpget{
        keepalive 100;
        keepalive_requests 1000;
        keepalive_timeout 65;
    #	sticky name=route expires=6h;
        server 192.168.50.11;
    #	server 192.168.50.12;
    }

    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
	   proxy_http_version 1.1;
	  proxy_set_header Connection "";
	   proxy_pass http://httpget;
           # root   html;
           # index  index.html index.htm;
        }
```

#### 直连nginx

```shell
[root@slave01 ~]# ab -n 10000 -c 30 http://192.168.50.11/
This is ApacheBench, Version 2.3 <$Revision: 1430300 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking 192.168.50.11 (be patient)
Completed 1000 requests
Completed 2000 requests
Completed 3000 requests
Completed 4000 requests
Completed 5000 requests
Completed 6000 requests
Completed 7000 requests
Completed 8000 requests
Completed 9000 requests
Completed 10000 requests
Finished 10000 requests


Server Software:        nginx/1.22.0
Server Hostname:        192.168.50.11
Server Port:            80

Document Path:          /
Document Length:        16 bytes

Concurrency Level:      30
Time taken for tests:   3.232 seconds
Complete requests:      10000
Failed requests:        0
Write errors:           0
Total transferred:      2470000 bytes
HTML transferred:       160000 bytes
Requests per second:    3093.61 [#/sec] (mean)
Time per request:       9.697 [ms] (mean)
Time per request:       0.323 [ms] (mean, across all concurrent requests)
Transfer rate:          746.21 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    4   0.4      4       6
Processing:     2    5   0.8      6       8
Waiting:        0    4   0.9      4       7
Total:          5   10   0.5     10      12
WARNING: The median and mean for the processing time are not within a normal deviation
        These results are probably not that reliable.

Percentage of the requests served within a certain time (ms)
  50%     10
  66%     10
  75%     10
  80%     10
  90%     10
  95%     10
  98%     11
  99%     11
 100%     12 (longest request)
```

#### 反向代理

```shell
[root@slave01 ~]# ab -n 10000 -c 30 http://192.168.50.10/
This is ApacheBench, Version 2.3 <$Revision: 1430300 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking 192.168.50.10 (be patient)
Completed 1000 requests
Completed 2000 requests
Completed 3000 requests
Completed 4000 requests
Completed 5000 requests
Completed 6000 requests
Completed 7000 requests
Completed 8000 requests
Completed 9000 requests
Completed 10000 requests
Finished 10000 requests


Server Software:        nginx/1.22.0
Server Hostname:        192.168.50.10
Server Port:            80

Document Path:          /
Document Length:        16 bytes

Concurrency Level:      30
Time taken for tests:   5.013 seconds
Complete requests:      10000
Failed requests:        0
Write errors:           0
Total transferred:      2470000 bytes
HTML transferred:       160000 bytes
Requests per second:    1994.84 [#/sec] (mean)
Time per request:       15.039 [ms] (mean)
Time per request:       0.501 [ms] (mean, across all concurrent requests)
Transfer rate:          481.18 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    3   1.8      3      18
Processing:     3   12   7.0      9      43
Waiting:        2   12   6.7      9      43
Total:          7   15   7.1     12      44

Percentage of the requests served within a certain time (ms)
  50%     12
  66%     13
  75%     13
  80%     14
  90%     28
  95%     35
  98%     36
  99%     37
 100%     44 (longest request)

```

#### 开启keepalive测试

```shell
[root@slave01 ~]# ab -n 10000 -c 30 http://192.168.50.10/
This is ApacheBench, Version 2.3 <$Revision: 1430300 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking 192.168.50.10 (be patient)
Completed 1000 requests
Completed 2000 requests
Completed 3000 requests
Completed 4000 requests
Completed 5000 requests
Completed 6000 requests
Completed 7000 requests
Completed 8000 requests
Completed 9000 requests
Completed 10000 requests
Finished 10000 requests


Server Software:        nginx/1.22.0
Server Hostname:        192.168.50.10
Server Port:            80

Document Path:          /
Document Length:        16 bytes

Concurrency Level:      30
Time taken for tests:   3.319 seconds
Complete requests:      10000
Failed requests:        0
Write errors:           0
Total transferred:      2470000 bytes
HTML transferred:       160000 bytes
Requests per second:    3012.83 [#/sec] (mean)
Time per request:       9.957 [ms] (mean)
Time per request:       0.332 [ms] (mean, across all concurrent requests)
Transfer rate:          726.73 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    2   0.6      2       4
Processing:     2    8   6.0      6      47
Waiting:        2    8   6.0      6      47
Total:          3   10   5.7      8      49

Percentage of the requests served within a certain time (ms)
  50%      8
  66%      9
  75%      9
  80%     10
  90%     21
  95%     26
  98%     27
  99%     27
 100%     49 (longest request)

```

> 可以看到开启使用keepalive参数后明显比么向代理提升明显,但没有达到nginx直连的效果

#### 直连Tomcat

```shell
[root@slave01 ~]# ab -n 100000 -c 30 http://192.168.50.11:8080/
This is ApacheBench, Version 2.3 <$Revision: 1430300 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking 192.168.50.11 (be patient)
Completed 10000 requests
Completed 20000 requests
Completed 30000 requests
Completed 40000 requests
Completed 50000 requests
Completed 60000 requests
Completed 70000 requests
Completed 80000 requests
Completed 90000 requests
Completed 100000 requests
Finished 100000 requests


Server Software:        
Server Hostname:        192.168.50.11
Server Port:            8080

Document Path:          /
Document Length:        11408 bytes

Concurrency Level:      30
Time taken for tests:   35.931 seconds
Complete requests:      100000
Failed requests:        0
Write errors:           0
Total transferred:      1152000000 bytes
HTML transferred:       1140800000 bytes
Requests per second:    2783.10 [#/sec] (mean)
Time per request:       10.779 [ms] (mean)
Time per request:       0.359 [ms] (mean, across all concurrent requests)
Transfer rate:          31309.93 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    3   0.5      3      10
Processing:     2    7   0.9      7      31
Waiting:        1    4   0.9      4      27
Total:          4   11   0.7     11      35

Percentage of the requests served within a certain time (ms)
  50%     11
  66%     11
  75%     11
  80%     11
  90%     11
  95%     12
  98%     12
  99%     12
 100%     35 (longest request)
```

#### nginx反向代理Tomcat + keepalive

```shell
[root@slave01 ~]# ab -n 100000 -c 30 http://192.168.50.10/
This is ApacheBench, Version 2.3 <$Revision: 1430300 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking 192.168.50.10 (be patient)
Completed 10000 requests
Completed 20000 requests
Completed 30000 requests
Completed 40000 requests
Completed 50000 requests
Completed 60000 requests
Completed 70000 requests
Completed 80000 requests
Completed 90000 requests
Completed 100000 requests
Finished 100000 requests


Server Software:        nginx/1.22.0
Server Hostname:        192.168.50.10
Server Port:            80

Document Path:          /
Document Length:        11408 bytes

Concurrency Level:      30
Time taken for tests:   82.165 seconds
Complete requests:      100000
Failed requests:        0
Write errors:           0
Total transferred:      1154200000 bytes
HTML transferred:       1140800000 bytes
Requests per second:    1217.06 [#/sec] (mean)
Time per request:       24.650 [ms] (mean)
Time per request:       0.822 [ms] (mean, across all concurrent requests)
Transfer rate:          13718.04 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    1   1.2      1      15
Processing:     2   23  12.6     28      64
Waiting:        1   21  12.8     25      63
Total:          3   25  12.2     30      65

Percentage of the requests served within a certain time (ms)
  50%     30
  66%     35
  75%     36
  80%     37
  90%     38
  95%     39
  98%     40
  99%     41
 100%     65 (longest request)
```

> 本地同一虚拟机多次并发十万请求反向代理居然比直连Tomcat慢.....无语(正常QPS是高于直连Tomcat的)

------

### UpStream工作流程

proxy_pass 向上游服务器请求数据共有6个阶段

- 初始化
- 与上游服务器建立连接
- 向上游服务器发送请求
- 处理响应头
- 处理响应体
- 结束

#### 配置

- **set_header**

  设置header

- **proxy_connect_timeout** 

  与上游服务器连接超时时间、快速失败

- **proxy_send_timeout**

  定义nginx向后端服务发送请求的间隔时间(不是耗时)。默认60秒，超过这个时间会关闭连接

- **proxy_read_timeout**

  后端服务给nginx响应的时间，规定时间内后端服务没有给nginx响应，连接会被关闭，nginx返回504 Gateway Time-out。默认60秒

#### 缓冲区

- **proxy_requset_buffering**

  是否完全读到请求体之后再向上游服务器发送请求

- **proxy_buffering** 

  是否缓冲上游服务器数据

- **proxy_buffers 32 64k;**

  缓冲区大小 32个 64k大小内存缓冲块

- **proxy_buffer_size**

  header缓冲区大小

  ```nginx
  proxy_requset_buffering on;
  proxy_buffering on;
  
  proxy_buffer_size 64k;
  
  proxy_buffers 32 128k;
  proxy_busy_buffers_size 8k;
  proxy_max_temp_file_size 1024m;
  ```

- **proxy_temp_file_write_size 8k**

  当启用从代理服务器到临时文件的响应的缓冲时，一次限制写入临时文件的数据的大小。 默认情况下，大小由proxy_buffer_size和proxy_buffers指令设置的两个缓冲区限制。 临时文件的最大大小由proxy_max_temp_file_size指令设置。  

- **proxy_max_temp_file_size 1024m;**

  临时文件最大值

- **proxy_temp_path** 

  ```nginx
  proxy_temp_path /spool/nginx/proxy_temp 1 2;
  ```

  a temporary file might look like this:

  ```nginx
  /spool/nginx/proxy_temp/7/45/00000123457
  ```

------

#### 对客户端的限制

可配置位置

- http
- server
- location

配置

- **client_body_buffer_size**

  对客户端请求中的body缓冲区大小

  默认32位8k 64位16k

  如果请求体大于配置，则写入临时文件

- **client_header_buffer_size**

  设置读取客户端请求体的缓冲区大小。 如果请求体大于缓冲区，则将整个请求体或仅将其部分写入临时文件。 默认32位8K。 64位平台16K。  

- **client_max_body_size 1000M;**

  默认1m，如果一个请求的大小超过配置的值，会返回413 (request Entity Too Large)错误给客户端

  将size设置为0将禁用对客户端请求正文大小的检查。  

- **client_body_timeout**

  指定客户端与服务端建立连接后发送 request body 的超时时间。如果客户端在指定时间内没有发送任何内容，Nginx 返回 HTTP 408（Request Timed Out）

- **client_header_timeout**

  客户端向服务端发送一个完整的 request header 的超时时间。如果客户端在指定时间内没有发送一个完整的 request header，Nginx 返回 HTTP 408（Request Timed Out）。

- **client_body_temp_path** *path*` [`*level1*` [`*level2*` [`*level3*`]]]

  在磁盘上客户端的body临时缓冲区位置

- **client_body_in_file_only on;**

  把body写入磁盘文件，请求结束也不会删除

- **client_body_in_single_buffer**

  尽量缓冲body的时候在内存中使用连续单一缓冲区，在二次开发时使用`$request_body`读取数据时性能会有所提高

- **client_header_buffer_size** 

  设置读取客户端请求头的缓冲区大小

  如果一个请求行或者一个请求头字段不能放入这个缓冲区，那么就会使用large_client_header_buffers

- **large_client_header_buffers**

  默认8k

------

### 反向代理中的容错机制

#### 参考文档

https://docs.nginx.com/nginx/admin-guide/load-balancer/http-load-balancer/

http://nginx.org/en/docs/stream/ngx_stream_proxy_module.html#proxy_bind

**proxy_timeout** 

#### 重试机制

**proxy_next_upstream**

作用：

当后端服务器返回指定的错误时，将请求传递到其他服务器。

`error`与服务器建立连接，向其传递请求或读取响应头时发生错误;

`timeout`在与服务器建立连接，向其传递请求或读取响应头时发生超时;

`invalid_header`服务器返回空的或无效的响应;

`http_500`服务器返回代码为500的响应;

`http_502`服务器返回代码为502的响应;

`http_503`服务器返回代码为503的响应;

`http_504`服务器返回代码504的响应;

`http_403`服务器返回代码为403的响应;

`http_404`服务器返回代码为404的响应;

`http_429`服务器返回代码为429的响应;

不了解这个机制，在日常开发web服务的时候，就可能会踩坑。

比如有这么一个场景：一个用于导入数据的web页面，上传一个excel，通过读取、处理excel，向数据库中插入数据，处理时间较长（如1分钟），且为同步操作（即处理完成后才返回结果）。暂且不论这种方式的好坏，若nginx配置的响应等待时间（proxy_read_timeout）为30秒，就会触发超时重试，将请求又打到另一台。如果处理中没有考虑到重复数据的场景，就会发生数据多次重复插入！（当然，这种场景，内网可以通过机器名访问该服务器进行操作，就可以绕过nginx了，不过外网就没办法了。）

------

### 获取客户端真实IP

#### X-Real-IP

额外模块，不推荐使用

#### setHeader

```nginx
proxy_set_header X-Forwarded-For $remote_addr;
```

> 通地在header 里传输真实ip地址,此ip地址并不能进行伪造,nginx会发现ip不符在header里进行ip重写再传输到后端服务器,而在实际应用中,如果nginx下还有另外一台反向代理的nginx时,此真实ip并不会被覆盖,而是沿用上一个nginx传过来的header ip

------

### Gzip

作用域 `http, server, location`

- gzip on;

  开关，默认关闭

- gzip_buffers 32 4k|16 8k

  缓冲区大小

- **gzip_comp_level** 1；

  压缩等级 1-9，数字越大压缩比越高

- gzip_http_version 1.1;

  使用gzip的最小版本

- gzip_min_length

  设置将被gzip压缩的响应的最小长度。 长度仅由“Content-Length”响应报头字段确定。

- gzip_proxied 多选

  off 为不做限制

  作为反向代理时，针对上游服务器返回的头信息进行压缩

  - expired - 启用压缩，如果header头中包含 "Expires" 头信息
  - no-cache - 启用压缩，如果header头中包含 "Cache-Control:no-cache" 头信息
  - no-store - 启用压缩，如果header头中包含 "Cache-Control:no-store" 头信息
  - private - 启用压缩，如果header头中包含 "Cache-Control:private" 头信息
  - no_last_modified - 启用压缩,如果header头中不包含 "Last-Modified" 头信息
  - no_etag - 启用压缩 ,如果header头中不包含 "ETag" 头信息
  - auth - 启用压缩 , 如果header头中包含 "Authorization" 头信息
  - any - 无条件启用压缩

- gzip_vary on;

  增加一个header，适配老的浏览器 `Vary: Accept-Encoding`

- gzip_types

  哪些mime类型的文件进行压缩

- gzip_disable

  禁止某些浏览器使用gzip

#### 完整实例

```nginx
  gzip on;
  gzip_buffers 16 8k;
  gzip_comp_level 6;
  gzip_http_version 1.1;
  gzip_min_length 256;
  gzip_proxied any;
  gzip_vary on;
  gzip_types text/plain application/x-javascript text/css application/xml;
  gzip_types
    text/xml application/xml application/atom+xml application/rss+xml application/xhtml+xml image/svg+xml
    text/javascript application/javascript application/x-javascript
    text/x-json application/json application/x-web-app-manifest+json
    text/css text/plain text/x-component
    font/opentype application/x-font-ttf application/vnd.ms-fontobject
    image/x-icon;
  gzip_disable "MSIE [1-6]\.(?!.*SV1)";
```

```http
HTTP/1.1 200
Server: nginx/1.21.6
Date: Wed, 18 May 2022 17:42:35 GMT
Content-Type: text/html;charset=utf-8
Content-Length: 7832
Connection: keep-alive
Keep-Alive: timeout=65
```

扩展模块

- ngx_http_gunzip_module(在nginx服务器使用gunzip解压静态资源文件)

  帮助不支持gzip的客户端解压本地文件(**实际使用过程中会比较少用到此配置,一般都是浏览器进行解压,释放服务器压力**)

- http_gzip_static_module(静态压缩模块)

  ```
  ./configure --with-http_gzip_static_module
  ```

> 默认开启的gzip使用的是动态压缩方式,而另外的静态压缩是需要开启上述`http_gzip_static_module`模块的,在http传输过程中,动态压缩是无法预知到静态资源文件大小的,只有静态压缩才能预先知道传输的文件资源大小,因为静态资源已经预先压缩存在当前服务器的
>
> 适用场景
>
> - 静态压缩适合并发量比较大的网站,可参考京东
> - 视频和音频,图片类文件 是不适合进行压缩的(压缩的只是html,js,css类的文件)

------

### Brotli

压缩性能比gzip好,是google自研使用的

#### 安装

- 官网下载 
  - `https://github.com/google/ngx_brotli`
  - `https://codeload.github.com/google/brotli/tar.gz/refs/tags/v1.0.9`

- 下载 两个项目

- 解压缩

模块化编译

```shell
./configure --with-compat --add-dynamic-module=/root/ngx_brotli-1.0.0rc --prefix=/usr/local/nginx/
```

或

```
--add-dynamic-module=brotli目录
```

- make

- 将`ngx_http_brotli_filter_module.so` `ngx_http_brotli_static_module.so`拷贝到`/usr/local/nginx/modules/`

- 复制nginx主程序

- 配置文件中添加

  ```nginx
  load_module "/usr/local/nginx/modules/ngx_http_brotli_filter_module.so";
  load_module "/usr/local/nginx/modules/ngx_http_brotli_static_module.so";
  ```

  ```nginx
  brotli on;
  brotli_static on;
  brotli_comp_level 6;
  brotli_buffers 16 8k;
  brotli_min_length 20;
  brotli_types text/plain text/css text/javascript application/javascript text/xml application/xml application/xml+rss application/json image/jpeg image/gif image/png;
  
  ```

- 测试

  默认http协议是没有br的,只有在https下header里发送`Accept-Encoding`才会进行br压缩,以下使用curl进行测试

  ```shell
  [root@master objs]# curl -H 'accept-encoding:br' -I http://192.168.50.10
  HTTP/1.1 200 
  Server: nginx/1.22.0
  Date: Sat, 20 Aug 2022 03:10:58 GMT
  Content-Type: text/html;charset=UTF-8
  Connection: keep-alive
  Content-Encoding: br
  
  ```
  

> 关于br压缩,比较好的介绍:https://www.cnblogs.com/-wenli/p/13594882.html
>
> - gzip与br是可以同时开启的,可以根据`accept-encoding`选择进行br压缩琮是gzip压缩
>
>   ```shell
>   [root@master objs]# curl -H 'accept-encoding:br' -I http://192.168.50.10
>   HTTP/1.1 200 
>   Server: nginx/1.22.0
>   Date: Sat, 20 Aug 2022 03:15:29 GMT
>   Content-Type: text/html;charset=UTF-8
>   Connection: keep-alive
>   Content-Encoding: br
>                                     
>   [root@master objs]# curl -H 'accept-encoding:gzip' -I http://192.168.50.10
>   HTTP/1.1 200 
>   Server: nginx/1.22.0
>   Date: Sat, 20 Aug 2022 03:15:31 GMT
>   Content-Type: text/html;charset=UTF-8
>   Connection: keep-alive
>   Content-Encoding: gzip
>                                     
>   ```

------

### 合并客户端请求

> 当一个请求过来时,可以把静态资源合并到一个资源请求返回,一般带有两个??的都是合并请求返回
>
> ```http
> http://example.com/??style1.css,style2.css,foo/style3.css
> ```

`Concat`模块

Tengine

Nginx官方介绍

https://www.nginx.com/resources/wiki/modules/concat/

git地址

https://github.com/alibaba/nginx-http-concat

- 下载安装

  下载源码解压缩编译安装

  ```shell
  wget https://github.com/alibaba/nginx-http-concat/archive/refs/tags/1.2.2.zip
  ```

- 编译

  ```shell
  ./configure \
  --prefix=/opt/nginx/ \
  --sbin-path=/usr/sbin/nginx \
  --conf-path=/etc/nginx/nginx.conf \
  --error-log-path=/var/log/nginx/error.log \
  --http-log-path=/var/log/nginx/access.log \
  --pid-path=/var/run/nginx/nginx.pid \
  --lock-path=/var/lock/nginx.lock \
  --with-compat --add-dynamic-module=/opt/nginx-http-concat-1.2.2
  ```

- make

  ```shell
  make
  ```

- 把编译好的nginx复制到/usr/sbin目录下

  ```shell
  cp objs/nginx /usr/sbin/ -y
  ```

- 配置

  ```nginx
      concat on;
      concat_max_files 30;
  ```

- 重启nginx加载新的配置

- 测试,在html页面设置如下

  ```html
  <link href="??font.css,bg.css" rel="stylesheet">
  ```

- 访問请求页面查看结果如下,可以看到在一个请求里合并返回了两个静态资源

  ![](http://www.dxb02.top/photos/nginx/18.jpg)

> 在实际应用中,也可以把一些小的图标合并返回,例如网页的icon,或者点击的小图标,一般高并发请求的情况下,减少静态资源的请求次数,也可以有效减轻服务器负载,这也是优化的一种思路

------

### 资源静态化

- 高并发系统资源静态化方案
- 一致性问题
- 合并文件输出
- 集群文件同步

### SSI合并服务器端文件

> **类似模板引擎,使用nginx对静态文件进行动态加载渲染(感觉比较鸡肋,现在所有后端语言都拥有自己的模板引挚渲染,把页面渲染放到服务器端会加大请求服务器nginx的压力,不建议在实际应用中把下面这么大量的命令配置放在nginx,了解即可)**

http://nginx.org/en/docs/http/ngx_http_ssi_module.html

#### 使用配置

```nginx
location / {
    ssi on;
    ...
}
```

页面如下

```html
<!--# include file="top.html" -->
<body>
    this is main html.....
</body>
<!--# include file="bottom.html" -->
```

测试访问结果如下,可以看到使用静态文件进行了加载其它页面

```html
this is top html this is main html..... this is bottom html
```

#### 配置字段介绍

- **ssi_min_file_chunk** 

  向磁盘存储并使用sendfile发送，文件大小最小值

- **ssi_last_modified** 

  是否保留lastmodified

- **ssi_silent_errors** 

  不显示逻辑错误

- **ssi_value_length**

  限制脚本参数最大长度

- **ssi_types** 

  默认text/html;如果需要其他mime类型 需要设置

- **include file**

  ```html
  <!--# include file="footer.html" -->
  ```

  静态文件直接引用

- include virtual

  可以指向location，而不一定是具体文件

- include wait

  阻塞请求

- include set

  在virtual基础上设置变量

- set

  设置临时变量

- block

  可以声明一个ssi的命令块，里面可以包裹其他命令

- config errmsg

  在模板中配置报错情况

- config timefmt

  日期格式化

- echo

  直接输出变量

  - var变量名称
  - encoding 是否使用特殊编码格式
  - default 变量没有值的时候使用默认值

- if

  逻辑判断

------

### rsync 

https://www.samba.org/ftp/rsync/rsync.html

remote synchronize是一个远程数据同步工具，可通过 LAN/WAN 快速同步多台主机之间的文件。也可以使用 rsync 同步本地硬盘中的不同目录。
rsync 是用于替代 rcp 的一个工具，rsync 使用所谓的 rsync算法 进行数据同步，这种算法只传送两个文件的不同部分，而不是每次都整份传送，因此速度相当快。

rsync 基于inotify 开发

Rsync有三种模式：

- 本地模式（类似于cp命令）
- 远程模式（类似于scp命令）
- 守护进程（socket进程：是rsync的重要功能）

#### rsync 常用选项

| 选项     | 含义                                                         |
| :------- | :----------------------------------------------------------- |
| -a       | 包含-rtplgoD                                                 |
| -r       | 同步目录时要加上，类似cp时的-r选项                           |
| -v       | 同步时显示一些信息，让我们知道同步的过程                     |
| -l       | 保留软连接                                                   |
| -L       | 加上该选项后，同步软链接时会把源文件给同步                   |
| -p       | 保持文件的权限属性                                           |
| -o       | 保持文件的属主                                               |
| -g       | 保持文件的属组                                               |
| -D       | 保持设备文件信息                                             |
| -t       | 保持文件的时间属性                                           |
| –delete  | 删除DEST中SRC没有的文件                                      |
| –exclude | 过滤指定文件，如–exclude “logs”会把文件名包含logs的文件或者目录过滤掉，不同步 |
| -P       | 显示同步过程，比如速率，比-v更加详细                         |
| -u       | 加上该选项后，如果DEST中的文件比SRC新，则不同步              |
| -z       | 传输时压缩                                                   |

#### 安装

两端安装

```shell
yum install -y rsync
```

#### 密码文件

创建文件`/etc/rsync.pwd`

内容

```shell
echo "hello:123" >> /etc/rsyncd.passwd
```

修改权限

```shell
chmod 600 /etc/rsync.pwd
```

修改配置`/etc/rsyncd.conf`

```
#增加权限认证
auth users = hello	
secrets file = /etc/rsyncd.pwd

#目标源路径
[ftp]
path = /opt/nginx/html
```

#### 开机启动

在`/etc/rc.local`文件中添加,或直接使用如下命令进行后台启动

```shell
rsync --daemon
```

```shell
[root@master html]# rsync --daemon
[root@master html]# ps -ef | grep rsync
root      88203      1  0 10:10 ?        00:00:00 rsync --daemon
root      88205  88127  0 10:10 pts/0    00:00:00 grep --color=auto rsync
```

> rsync没有关闭命令,只能使用kill直接删除后台进程

#### 查看远程目录

```shell
rsync --list-only 192.168.44.104::www/
```

带验证查看远程目录

```shell
[root@slave01 ~]# rsync --list-only hello@192.168.50.10::ftp/
Password: 
drwxr-xr-x            145 2022/08/23 17:06:01 .
-rw-r--r--            497 2022/08/18 14:30:38 50x.html
-rw-r--r--             39 2022/08/23 15:55:42 bg.css
-rw-r--r--             19 2022/08/23 17:06:29 bottom.html
-rw-r--r--             30 2022/08/23 15:55:49 font.css
-rw-r--r--            180 2022/08/23 17:09:04 hello.html
-rw-r--r--             15 2022/08/18 15:10:55 index.html
-rw-r--r--            615 2022/08/18 14:30:38 index.html_bak
-rw-r--r--             16 2022/08/23 17:06:15 top.html
```

#### 拉取数据到指定目录

```shell
rsync -avz rsync://192.168.44.104:873/www

rsync -avz 192.168.44.104::www/ /root/w
```

#### 使用SSH方式

```shell
rsync -avzP /usr/local/nginx/html/ root@192.168.44.105:/www/
```

#### 客户端免密

客户端只放密码,不需要每次都输入密码

```shell
echo "123" >> /etc/rsyncd.pwd
```

> **注意:这里也需要把文件权限改成600**

此时在客户端已经可以配合脚本实现定时同步了

```shell
[root@slave01 ~]# rsync --list-only --password-file=/etc/rsyncd.pwd hello@192.168.50.10::ftp/
drwxr-xr-x            145 2022/08/23 17:06:01 .
-rw-r--r--            497 2022/08/18 14:30:38 50x.html
-rw-r--r--             39 2022/08/23 15:55:42 bg.css
-rw-r--r--             19 2022/08/23 17:06:29 bottom.html
-rw-r--r--             30 2022/08/23 15:55:49 font.css
-rw-r--r--            180 2022/08/23 17:09:04 hello.html
-rw-r--r--             15 2022/08/18 15:10:55 index.html
-rw-r--r--            615 2022/08/18 14:30:38 index.html_bak
-rw-r--r--             16 2022/08/23 17:06:15 top.html
[root@slave01 ~]# rsync -avz  --password-file=/etc/rsyncd.pwd hello@192.168.50.10::ftp/ /opt/nginx/html/
receiving incremental file list
./
50x.html
bg.css
bottom.html
font.css
hello.html
index.html
index.html_bak
top.html

sent 197 bytes  received 775 bytes  1,944.00 bytes/sec
total size is 1,411  speedup is 1.45
```

#### 删除目录文件

当源服务器文件删除了,要删除远程服务器的目标文件时,可使用以下命令

```shell
[root@slave01 ~]# rsync -avz --delete  --password-file=/etc/rsyncd.pwd hello@192.168.50.10::ftp/ /opt/nginx/html/
receiving incremental file list
deleting top.html

sent 20 bytes  received 198 bytes  436.00 bytes/sec
total size is 1,395  speedup is 6.40
```

#### 如何实现推送？

在客户端修改配置,增加验证账号和密码(与上面的操作一样,只是把服务端的设置放在了客户端)

```shell
[root@master html]# rsync -avz --password-file=/etc/rsyncd.pwd.client /opt/nginx/html/ hello@192.168.50.11::ftp/
sending incremental file list
./
111.html

sent 263 bytes  received 38 bytes  602.00 bytes/sec
total size is 1,395  speedup is 4.63
```

```--delete 删除目标目录比源目录多余文件```

> 注意:使用服务器推送过程中,因客户端权限问题,必须在客户端修改其写入权限和用户组,配置如下
>
> ```
>  #增加其操作的用户和组
>  uid = root
>  gid = root
>  
>  #默认是只有只读权限的
>  read only = no
> ```
>
> 报错如下,提示没权限操作和推送失败
>
> ```shell
> [root@master html]# rsync -avz --password-file=/etc/rsyncd.pwd.client /opt/nginx/html hello@192.168.50.11::ftp/
> sending incremental file list
> rsync: read error: Connection reset by peer (104)
> rsync error: error in socket IO (code 10) at io.c(785) [sender=3.1.2]
> [root@master html]# rsync -avz --password-file=/etc/rsyncd.pwd.client /opt/nginx/html/ hello@192.168.50.11::ftp/
> sending incremental file list
> rsync: failed to set times on "/." (in ftp): Operation not permitted (1)
> ./
> 111
> rsync: mkstemp "/.111.sij1QF" (in ftp) failed: Permission denied (13)
> 
> sent 259 bytes  received 189 bytes  896.00 bytes/sec
> total size is 1,395  speedup is 3.11
> rsync error: some files/attrs were not transferred (see previous errors) (code 23) at main.c(1179) [sender=3.1.2]
> 
> ```

#### 实时推送

推送端安装inotify依赖

```shell
yum install -y automake
```

下载,并进行编译安装

```shell
wget https://github.com/inotify-tools/inotify-tools/archive/refs/tags/3.22.6.0.zip
unzip inotify-tools-3.22.6.0.zip
cd inotify-tools-3.22.6.0
./autogen.sh  && ./configure --prefix=/opt/inotify-tools
make && make install
```

监控目录

```shell
[root@master bin]# /opt/inotify-tools/bin/inotifywait -mrq --timefmt '%Y-%m-%d %H:%M:%S' --format '%T %w%f %e' -e close_write,modify,delete,create,attrib,move /opt/nginx/html/
2022-08-24 12:05:39 //opt/nginx/html/New Folder CREATE,ISDIR
2022-08-24 12:05:41 //opt/nginx/html/New Folder MOVED_FROM,ISDIR
2022-08-24 12:05:41 //opt/nginx/html/222 MOVED_TO,ISDIR
```

#### 简单自动化脚本

```shell
#!/bin/bash

/opt/inotify-tools/bin/inotifywait -mrq --timefmt '%d/%m/%y %H:%M' --format '%T %w%f %e' -e close_write,modify,delete,create,attrib,move /opt/nginx/html/ | while read file
do
       
        rsync -az --delete --password-file=/etc/rsyncd.pwd.client /opt/nginx/html/ hello@192.168.50.11::ftp/
done

```

#### inotify常用参数

| 参数       | 说明                                                         | 含义                                                         |
| ---------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| -r         | --recursive                                                  | #递归查询目录                                                |
| -q         | --quiet                                                      | #打印很少的信息，仅仅打印监控事件信息                        |
| -m         | --monitor                                                    | #始终保持事件监听状态                                        |
| --excludei |                                                              | #排除文件或目录时，不区分大小写                              |
| --timefmt  |                                                              | #指定事件输出格式                                            |
| --format   |                                                              | #打印使用指定的输出类似格式字符串                            |
| -e         | --event[ -e\|--event ... ]accessmodifyattribcloseopenmove_tomove createdeleteumount | #通过此参数可以指定要监控的事件 #文件或目录被读取#文件或目录的内容被修改#文件或目录属性被改变#文件或目录封闭，无论读/写模式#文件或目录被打开#文件或目录被移动至另外一个目录#文件或目录被移动另一个目录或从另一个目录移动至当前目录#文件或目录被创建在当前目录#文件或目录被删除#文件系统被卸载 |

> 关于inotify-tools的详细介绍
>
> https://docs.rockylinux.org/zh/books/learning_rsync/06_rsync_inotify/

------

### 多级缓存

静态资源缓存

#### 浏览器缓存

##### 什么时候可以用缓存？

1. 不常改变的内容
2. 过期时间
3. 针对post/get请求都可以
4. 存储位置
5. 磁盘使用空间限制

观察京东缓存及加载速度

- `deskcache`

  是从磁盘当中取出的，也是在已经在之前的某个时间加载过该资源，**不会请求服务器**但是此资源不会随着该页面的关闭而释放掉，因为是存在硬盘当中的，下次打开仍会from disk cache

- `memorycache`

  字面理解是从内存中，其实也是字面的含义，这个资源是直接从内存中拿到的，**不会请求服务器**一般已经加载过该资源且缓存在内存当中，当关闭该页面时，此资源就被内存释放掉了，再次重新打开相同页面时不会出现from memory cache的情况

- Age

  是CDN添加的属性表示在CDN中缓存了多少秒

- **via**

  用来标识CDN缓存经历了哪些服务器，缓存是否命中，使用的协议

#### Nginx默认缓存

Nginx版本不同会默认配置

#### 强制缓存与协商缓存

- 强制缓存：直接从本机读取，不请求服务器
- 协商缓存：发送请求header中携带Last-Modified，服务器可能会返回304 Not Modified

#### 浏览器强制缓存

- **cache-control**

  ```nginx
  location / {
  
      add_header cache-control "max-age:300";
  
      root   html;
      index  index.html index.htm;
  }
  ```

  http1.1的规范，使用max-age表示文件可以在浏览器中缓存的时间以秒为单位

  - 首次打开页面时走的是磁盘缓存,如下,当页面刷新时会发起请求刷新本地页面数据

    ![](http://www.dxb02.top/photos/nginx/21.jpg)

| 标记                   | 类型       | 功能                                                         |
| ---------------------- | ---------- | ------------------------------------------------------------ |
| public                 | 响应头     | 响应的数据可以被缓存，客户端和代理层都可以缓存               |
| private                | 响应头     | 可私有缓存，客户端可以缓存，代理层不能缓存（CDN，proxy_pass） |
| no-cache               | 请求头     | 可以使用本地缓存，但是必须发送请求到服务器回源验证           |
| no-store               | 请求和响应 | 应禁用缓存                                                   |
| max-age                | 请求和响应 | 文件可以在浏览器中缓存的时间以秒为单位                       |
| s-maxage               | 请求和响应 | 用户代理层缓存，CDN下发，当客户端数据过期时会重新校验        |
| max-stale              | 请求和响应 | 缓存最大使用时间，如果缓存过期，但还在这个时间范围内则可以使用缓存数据 |
| min-fresh              | 请求和响应 | 缓存最小使用时间，                                           |
| must-revalidate        | 请求和响应 | 当缓存过期后，必须回源重新请求资源。比no-cache更严格。因为HTTP 规范是允许客户端在某些特殊情况下直接使用过期缓存的，比如校验请求发送失败的时候。那么带有must-revalidate的缓存必须校验，其他条件全部失效。 |
| proxy-revalidate       | 请求和响应 | 和must-revalidate类似，只对CDN这种代理服务器有效，客户端遇到此头，需要回源验证 |
| stale-while-revalidate | 响应       | 表示在指定时间内可以先使用本地缓存，后台进行异步校验         |
| stale-if-error         | 响应       | 在指定时间内，重新验证时返回状态码为5XX的时候，可以用本地缓存 |
| only-if-cached         | 响应       | 那么只使用缓存内容，如果没有缓存 则504 getway timeout        |

在浏览器和服务器端验证文件是否过期的时候，浏览器在二次请求的时候会携带IF-Modified-Since属性

- Expires

  过期时间

  ```nginx
  expires 30s;   #缓存30秒
  expires 30m;  #缓存30分钟   
  expires 2h;     #缓存2小时
  expires 30d;    #缓存30天
  ```

#### 协商缓存

- **last-modified**
- **etag**

http1.1支持

在HTTP协议中If-Modified-Since和If-None-Match分别对应Last-Modified和ETag

Entity Tag 的缩写，中文译过来就是实体标签的意思.

HTTP中并没有指定如何生成ETag，哈希是比较理想的选择。

在计算Etag的时候，会产生CPU的耗费，所以也可以用时间戳，但这样直接使用Last-Modified即可。

ETag 用来校验用户请求的资源是否有变化，作用和lastmodified很像，区别是lastmodified精确到秒，ETag可以用hash算法来生成更精确的比对内容。

当用户首次请求资源的时候返回给用户数据和200状态码并生成ETag，再次请求的时候服务器比对ETag，没有发生变化的话返回304

Cache-Control直接是通过不请求来实现，而ETag是会发请求的，只不过服务器根据请求的东西的内容有无变化来判断是否返回请求的资源

- 默认浏览器是支持开启的,可以看到如下访问默认是带ETag和Last-Modified返回的参数,使用的是协商缓存

  ![](http://www.dxb02.top/photos/nginx/19.jpg)

- 关闭设置如下,可以看到当关闭协商缓存后,每次请求都是200,每次都会返回静态文件

  ```nginx
  location / {
      etag off;	#关闭ETag
      if_modified_since off; #关闭Last-Modified	
      #add_header Last-Modified "";	#添加空参数请求,当请求时不带此参数
      root   html;
      index  index.html index.htm;
  }
  ```

  ![](http://www.dxb02.top/photos/nginx/20.jpg)

> **总结：**
>
> - **cache-control expires 强制缓存**
>
>   **页面首次打开，直接读取缓存数据，刷新，会向服务器发起请求**
>
> - **etag lastmodify  协商缓存**
>
>   **没发生变化 返回304 不发送数据**
>
> - **last-modified 与ssi的冲突**

#### 浏览器缓存原则

- 多级集群负载时last-modified必须保持一致
- 还有一些场景下我们希望禁用浏览器缓存。比如轮训api上报数据
- 浏览器缓存很难彻底禁用，大家的做法是加版本号，随机数等方法。
- 只缓存200响应头的数据，像3XX这类跳转的页面不需要缓存。
- 对于js，css这类可以缓存很久的数据，可以通过加版本号的方式更新内容
- 不需要强一致性的数据，可以缓存几秒
- 异步加载的接口数据，可以使用ETag来校验。
- 在服务器添加Server头，有利于排查错误

- 分为手机APP和Client以及是否遵循http协议

- 在没有联网的状态下可以展示数据

- 流量消耗过多

- 提前下发  避免秒杀时同时下发数据造成流量短时间暴增
- 兜底数据 在服务器崩溃和网络不可用的时候展示
- 临时缓存  退出即清理
- 固定缓存  展示框架这种，可能很长时间不会更新，可用随客户端下发
  - **首页**有的时候可以看做是框架 应该禁用缓存，以保证加载的资源都是最新的
- 父子连接 页面跳转时有一部分内容不需要重新加载，可用从父菜单带过来
- 预加载     某些逻辑可用判定用户接下来的操作，那么可用异步加载那些资源
- 漂亮的加载过程 异步加载 先展示框架，然后异步加载内容，避免主线程阻塞

------

### GEOip(了解)

> **实际应用中比较少,完全可以使用CDN根据地区进行加速代替**

1. 下载数据库

   官网需注册登录

   下载数据库

   maxmind.com

2. 安装依赖

   官方githttps://github.com/maxmind/libmaxminddb

   下载后执行编译安装之后

   ```shell
   $ echo /usr/local/lib  >> /etc/ld.so.conf.d/local.conf 
   $ ldconfig
   ```

3. Nginx模块

   https://github.com/leev/ngx_http_geoip2_module

   更完整的配置可参考官方文档

   http://nginx.org/en/docs/http/ngx_http_geoip_module.html#geoip_proxy

4. Nginx配置

   ```nginx
   geoip2 /root/GeoLite2-ASN_20220524/GeoLite2-ASN.mmdb {
   	$geoip2_country_code country iso_code;
   }
   add_header country $geoip2_country_code;
   ```

------

### 正向代理(了解)

> **实际应用中使用nginx作为代理,即跳板机用途较少**

#### 正向代理配置

```nginx
proxy_pass $scheme://$host$request_uri;
resolver 8.8.8.8;
```

#### 代理https请求

需要第三方模块

https://github.com/chobits/ngx_http_proxy_connect_module

配置

```nginx
 server {
     listen                         3128;

     # dns resolver used by forward proxying
     resolver                       8.8.8.8;

     # forward proxy for CONNECT request
     proxy_connect;
     proxy_connect_allow            443 563;
     proxy_connect_connect_timeout  10s;
     proxy_connect_read_timeout     10s;
     proxy_connect_send_timeout     10s;

     # forward proxy for non-CONNECT request
     location / {
         proxy_pass http://$host;
         proxy_set_header Host $host;
     }
 }
```

------

### proxy缓存

官网解释

http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_cache

#### 配置

```nginx
#http模块：
proxy_cache_path /tmp/ngx_tmp levels=1:2 keys_zone=test_cache:100m inactive=1d max_size=10g;
#location模块：
add_header  Nginx-Cache "$upstream_cache_status";
proxy_cache test_cache;
proxy_cache_valid 1h;
```

- **proxy_cache_use_stale** 

  默认off

  在什么时候可以使用过期缓存

  可选`error` | `timeout` | `invalid_header` | `updating` | `http_500` | `http_502` | `http_503` | `http_504` | `http_403` | `http_404` | `http_429` | `off`

- **proxy_cache_background_update** 

  默认off

  运行开启子请求更新过期的内容。同时会把过期的内容返回给客户端

- **proxy_no_cache**  **proxy_cache_bypass** 

  指定什么时候不使用缓存而直接请求上游服务器

  ```nginx
  proxy_no_cache $cookie_nocache $arg_nocache$arg_comment;
  proxy_no_cache $http_pragma    $http_authorization;
  ```

  如果这些变量如果存在的话不为空或者不等于0，则不使用缓存

- **proxy_cache_convert_head** 

  默认 on

  是否把head请求转换成get请求后再发送给上游服务器 以便缓存body里的内容

  如果关闭 需要在 `cache key` 中添加 $request_method 以便区分缓存内容

- **proxy_cache_lock** 

  默认off

  缓存更新锁

- **proxy_cache_lock_age** 

  默认5s

  缓存锁超时时间

#### 示例配置

```nginx
http {
    include       mime.types;
    default_type  application/octet-stream;


	proxy_cache_path /tmp/ngx_tmp levels=1:2 keys_zone=test_cache:100m inactive=1d max_size=10g;

    upstream httpget{
        server 192.168.50.11:8080;
    #	server 192.168.50.12;
    }
    
server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            proxy_pass http://httpget;
            add_header  Nginx-Cache "$upstream_cache_status";
            proxy_cache test_cache;
            proxy_cache_valid 1d;

            #root   html;
            #index  index.html index.htm;
        }
        ...        
}
```

- 默认首次打开页面是没有命中缓存的,图如下

  ![](http://www.dxb02.top/photos/nginx/22.jpg)

- 当首次加载完页面后会在nginx服务器磁盘指定路径缓存了页面文件

  ![](http://www.dxb02.top/photos/nginx/23.jpg)

  ```shell
  [root@master ngx_tmp]# tree
  .
  └── a
      └── 59
          └── 2b1ca16e2a67341af5dfbbf8f501459a
  
  2 directories, 1 file
  [root@master ngx_tmp]# cat a/59/2b1ca16e2a67341af5dfbbf8f501459a 
  cÿÿÿÿÿÿÿÿºAcx\¤fԁ
  KEY: http://httpget/
  HTTP/1.1 200 
  Content-Type: text/html;charset=UTF-8
  Date: Thu, 25 Aug 2022 09:32:42 GMT
  Connection: close
  ...
  ```

  找开缓存文件可以以看到缓存key使用的是`http://httpget/`,该key是根据在nginx配置生成策略

------

#### 断点续传缓存 range

当有完整的content-length之后即可断点续传

在反向代理服务器中需向后传递header

```nginx
proxy_set_header Range $http_range;
```

proxy_cache_key中增加range

- **proxy_cache_max_range_offset** 

  range最大值，超过之后不做缓存，默认情况下 不需要对单文件较大的资源做缓存

- **proxy_cache_methods** 

  默认 head get

- **proxy_cache_min_uses** 

  默认1

  被请求多少次之后才做缓存

- **proxy_cache_path**

  path 指定存储目录

  以cache_key取md5值

  - **levels=1:2**

    目录层级数及目录名称位数

    取mdb5后几位

    TMPFS

  - **use_temp_path**

    默认创建缓存文件时，先向缓冲区创建临时文件，再移动到缓存目录

    是否使用缓冲区

  - **inactive**

    指定时间内未被访问过的缓存将被删除

------

#### 缓存清理`purger`

需要第三方模块支持

https://github.com/FRiCKLE/ngx_cache_purge

配置

```nginx
location ~ /purge(/.*) {

    proxy_cache_purge  test_cache  $1;
}
自定义cachekey
proxy_cache_key $uri;
```

- **proxy_cache_key** 

  默认`$scheme$proxy_host$request_uri`

  缓存的key

- **proxy_cache_revalidate** 

  如果缓存过期了，向上游服务器发送“If-Modified-Since” and “If-None-Match来验证是否改变，如果没有就不需要重新下载资源了

- **proxy_cache_valid**

  可以针对不容http状态码设置缓存过期时间

  不设置状态码会默认200, 301, 302

  ```nginx
  proxy_cache_valid 200 302 10m;
  proxy_cache_valid 301      1h;
  proxy_cache_valid any      1m;
  ```

  any指其他任意状态码

------

## Nginx内存缓存

1. 系统安装strace进行追踪

   ```shell
    yum install strace -y
   ```

2. 使用strace追踪nginx进程,访问2次页面,追踪结果如下

   > **注意这里是把nginx的`sendfile`设置为`on`的**
   >
   > # 指令说明
   >
   > ```
   > 语法： sendfile on | off;
   > 默认值： sendfile off;
   > 上下文： http，server，location，if in location
   > ```
   >
   > 指定是否使用sendfile系统调用来传输文件。
   >  sendfile系统调用在两个文件描述符之间直接传递数据(完全在内核中操作)，从而避免了数据在内核缓冲区和用户缓冲区之间的拷贝，操作效率很高，被称之为零拷贝。

   ```shell
   [root@master ~]# ps -ef | grep nginx
   root      92417      1  0 13:05 ?        00:00:00 nginx: master process nginx
   nginx     93100  92417  0 14:58 ?        00:00:00 nginx: worker process
   nginx     93101  92417  0 14:58 ?        00:00:00 nginx: cache manager process
   root      93103  93045  0 14:59 pts/3    00:00:00 grep --color=auto nginx
   [root@master ~]# strac -p 93100
   -bash: strac: command not found
   [root@master ~]# strace -p 93100
   strace: Process 93100 attached
   epoll_wait(10, [{EPOLLIN, {u32=4103299088, u64=140372224466960}}], 512, -1) = 1
   accept4(6, {sa_family=AF_INET, sin_port=htons(1070), sin_addr=inet_addr("192.168.50.213")}, [112->16], SOCK_NONBLOCK) = 3
   epoll_ctl(10, EPOLL_CTL_ADD, 3, {EPOLLIN|EPOLLRDHUP|EPOLLET, {u32=4103299568, u64=140372224467440}}) = 0
   epoll_wait(10, [{EPOLLIN, {u32=4103299568, u64=140372224467440}}], 512, 60000) = 1
   recvfrom(3, "GET / HTTP/1.1\r\nHost: 192.168.50"..., 1024, 0, NULL, NULL) = 468
   stat("/opt/nginx//html/index.html", {st_mode=S_IFREG|0644, st_size=15, ...}) = 0
   open("/opt/nginx//html/index.html", O_RDONLY|O_NONBLOCK) = 7
   fstat(7, {st_mode=S_IFREG|0644, st_size=15, ...}) = 0
   writev(3, [{iov_base="HTTP/1.1 200 OK\r\nServer: nginx/1"..., iov_len=235}], 1) = 235
   sendfile(3, 7, [0] => [15], 15)         = 15
   write(8, "192.168.50.213 - - [27/Aug/2022:"..., 190) = 190
   close(7)                                = 0
   setsockopt(3, SOL_TCP, TCP_NODELAY, [1], 4) = 0
   epoll_wait(10, [{EPOLLIN, {u32=4103299088, u64=140372224466960}}], 512, 75000) = 1
   accept4(6, {sa_family=AF_INET, sin_port=htons(1071), sin_addr=inet_addr("192.168.50.213")}, [112->16], SOCK_NONBLOCK) = 7
   epoll_ctl(10, EPOLL_CTL_ADD, 7, {EPOLLIN|EPOLLRDHUP|EPOLLET, {u32=4103299808, u64=140372224467680}}) = 0
   epoll_wait(10, [{EPOLLIN, {u32=4103299568, u64=140372224467440}}], 512, 60000) = 1
   recvfrom(3, "GET / HTTP/1.1\r\nHost: 192.168.50"..., 1024, 0, NULL, NULL) = 554
   stat("/opt/nginx//html/index.html", {st_mode=S_IFREG|0644, st_size=15, ...}) = 0
   open("/opt/nginx//html/index.html", O_RDONLY|O_NONBLOCK) = 12
   fstat(12, {st_mode=S_IFREG|0644, st_size=15, ...}) = 0
   writev(3, [{iov_base="HTTP/1.1 304 Not Modified\r\nServe"..., iov_len=178}], 1) = 178
   write(8, "192.168.50.213 - - [27/Aug/2022:"..., 189) = 189
   close(12)                               = 0
   epoll_wait(10, [{EPOLLIN, {u32=4103299568, u64=140372224467440}}], 512, 30043) = 1
   recvfrom(3, "GET /favicon.ico HTTP/1.1\r\nHost:"..., 1024, 0, NULL, NULL) = 391
   open("/opt/nginx//html/favicon.ico", O_RDONLY|O_NONBLOCK) = -1 ENOENT (No such file or directory)
   write(9, "2022/08/27 15:01:07 [error] 9310"..., 257) = 257
   writev(3, [{iov_base="HTTP/1.1 404 Not Found\r\nServer: "..., iov_len=155}, {iov_base="<html>\r\n<head><title>404 Not Fou"..., iov_len=100}, {iov_base="<hr><center>nginx/1.22.0</center"..., iov_len=53}, {iov_base="<!-- a padding to disable MSIE a"..., iov_len=402}], 4) = 710
   write(8, "192.168.50.213 - - [27/Aug/2022:"..., 222) = 222
   epoll_wait(10, ^Cstrace: Process 93100 detached
    <detached ...>
   ```

   > 一般应用为静态文件元数据信息缓存
   >
   > 可以看到每次访问页面,都会open打开本地磁盘的文件,这里可以进行open_file优化,免除每次的IO操作

- 选项配置

  - **open_file_cache**

    ```nginx
    open_file_cache max=500 inactive=60s;
    open_file_cache_min_uses 1; 
    open_file_cache_valid 60s; 
    open_file_cache_errors on;
    ```

    - **max**缓存最大数量，超过数量后会使用LRU淘汰

    - **inactive** 指定时间内未被访问过的缓存将被删除

    - **pen_file_cache_min_uses**

      被访问到多少次后会开始缓存

    - **open_file_cache_valid**

      间隔多长时间去检查文件是否有变化

    - **open_file_cache_errors**

      对错误信息是否缓存

- 实例配置如下

  ```nginx
  location / {
      open_file_cache max=500 inactive=60s;
      open_file_cache_min_uses 1; 
      open_file_cache_valid 60s; 
      open_file_cache_errors on;
  
      #proxy_pass http://httpget;
      root   html;
      index  index.html index.htm;
  }
  ```

  继续使用starce重复上面的操作进行追踪,结果如下

  ```shell
  [root@master ~]# ps -ef | grep nginx
  root      92417      1  0 13:05 ?        00:00:00 nginx: master process nginx
  nginx     93272  92417  0 15:07 ?        00:00:00 nginx: worker process
  nginx     93273  92417  0 15:07 ?        00:00:00 nginx: cache manager process
  root      93275  93045  0 15:07 pts/3    00:00:00 grep --color=auto nginx
  [root@master ~]# strace -p 93272
  strace: Process 93272 attached
  epoll_wait(12, [{EPOLLIN, {u32=4103299568, u64=140372224467440}}], 512, 37099) = 1
  recvfrom(4, "GET / HTTP/1.1\r\nHost: 192.168.50"..., 1024, 0, NULL, NULL) = 554
  writev(4, [{iov_base="HTTP/1.1 304 Not Modified\r\nServe"..., iov_len=178}], 1) = 178
  write(3, "192.168.50.213 - - [27/Aug/2022:"..., 189) = 189
  epoll_wait(12, [{EPOLLIN, {u32=4103299568, u64=140372224467440}}], 512, 33171) = 1
  recvfrom(4, "GET / HTTP/1.1\r\nHost: 192.168.50"..., 1024, 0, NULL, NULL) = 554
  writev(4, [{iov_base="HTTP/1.1 304 Not Modified\r\nServe"..., iov_len=178}], 1) = 178
  write(3, "192.168.50.213 - - [27/Aug/2022:"..., 189) = 189
  epoll_wait(12, [], 512, 26045)          = 0
  close(8)                                = 0
  epoll_wait(12, 
  
  ```

  > 明显看到整个过程比上面的简知短了许多,而且打开open操作也没有了,

> - **sendfile+open file方式 适合文件比较多,单个文件比较大,存储在磁盘的场景**
> - **内存存储适合热点流量,文件比较多,内容比较小的场景**

------

## Nginx外置缓存缓存

### error_page

指定状态码

```nginx
	error_page 404 =302 http://192.168.50.12/;
```

> 默认指向location
>

### 匿名location

```nginx

```

------

### nginx + memcached

http://nginx.org/en/docs/http/ngx_http_memcached_module.html

配置

```nginx
    
upstream backend {

    #   server 192.168.44.102 weight=8 down;
    server 192.168.44.104:8080;
}

location / {

    set            $memcached_key "$uri?$args";
    memcached_pass 127.0.0.1:11211;

    add_header X-Cache-Satus HIT;

    add_header Content-Type 'text/html; charset=utf-8'; # 强制响应数据格式为html


    # root   html;
}

```

------

### nginx + redis

redis2-nginx-module是一个支持 Redis 2.0 协议的 Nginx upstream 模块，它可以让 Nginx 以非阻塞方式直接防问远方的 Redis 服务，同时支持 TCP 协议和 Unix Domain Socket 模式，并且可以启用强大的 Redis 连接池功能。

https://www.nginx.com/resources/wiki/modules/redis2/

https://github.com/openresty/redis2-nginx-module

redis2-nginx-module 安装

```shell
./configure --prefix=/usr/local/nginx --with-http_ssl_module --add-module=/usr/local/redis2-nginx-module
```

- test

  ```nginx
  location = /foo {
  
  default_type text/html;
  
       redis2_query auth 123123;
  
       set $value 'first';
  
       redis2_query set one $value;
  
       redis2_pass 192.168.199.161:6379;
  
   }
  ```

- get

  ```nginx
  location = /get {
  
  default_type text/html;
  
       redis2_pass 192.168.199.161:6379;
  
       redis2_query auth 123123;
  
       set_unescape_uri $key $arg_key;  # this requires ngx_set_misc
  
       redis2_query get $key;
  
  }
  ```

- set

  ```nginx
  # GET /set?key=one&val=first%20value
  
  location = /set {
  
      default_type text/html;
  
      redis2_pass 192.168.199.161:6379;
  
      redis2_query auth 123123;
   
       set_unescape_uri $key $arg_key;  # this requires ngx_set_misc
  
       set_unescape_uri $val $arg_val;  # this requires ngx_set_misc
  
       redis2_query set $key $val;
  
   }
  ```
  
- pipeline

  ```nginx
  location = /foo {     
  	 set $value 'first';
       redis2_query set one $value;
       redis2_query get one;
       redis2_query set one two;
       redis2_query get one;
  	 redis2_query del key1;
  }    
  ```

- list

  ```nginx
  location = /init {    
  	redis2_query lpush key1 C;
      redis2_query lpush key1 B;
      redis2_query lpush key1 A;
  	redis2_query lrange key1 0 -1;
  }    
  ```

- 集群

  ```nginx
  upstream redis_cluster {
  
       server 192.168.199.161:6379;
  
       server 192.168.199.161:6379;
  
   }
  
  location = /redis {
  
  default_type text/html;
  
           redis2_next_upstream error timeout invalid_response;
  
           redis2_query get foo;
  
           redis2_pass redis_cluster;
     }
  ```

------

## Stream模块

Nginx自1.9.0版本新增了stream模块，可以实现对四层TCP/UDP协议的转发、代理、负载均衡等，比如本文中的实例就有mysql、zookeeper和dns的代理或负载方法。

http://nginx.org/en/docs/stream/ngx_stream_core_module.html

> 使用nginx对mysql进行负载均衡

- 安装Stream模块

  ```shell
  ./configure --prefix=/opt/nginx --with-stream
  ```

- 如果是yum安装nginx，可以独立安装stream模块

  ```shell
  yum -y install nginx-mod-stream
  ```

### 配置Stream模块

```nginx
stream {
    log_format proxy    '$remote_addr [$time_local] '
                        '$protocol $status $bytes_sent $bytes_received '
                        '$session_time "$upstream_addr" '
                        '"$upstream_bytes_sent" "$upstream_bytes_received" "$upstream_connect_time"';
    access_log          /var/log/nginx/stream-access.log proxy;
    open_log_file_cache off;
    include             /etc/nginx/conf.d/*.stream;
}
```

- 以下是代理/负载mysql的实例：

  ```nginx
  upstream mysql {
      hash $remote_addr consistent;
      server    10.10.100.100:3306 max_fails=3 fail_timeout=30s;
      server    10.10.100.101:3306 weight=9;
      server    10.10.100.102:3306 weight=1;
  }
  server {
      listen                13306;
      proxy_connect_timeout 30s;
      proxy_timeout         1h;
      proxy_pass            mysql;
      allow                 10.10.200.0/24;
      deny                  all;
  }
  ```

- 以下是代理zookeeper的实例：

  ```nginx
  upstream zookeeper {
      server 10.10.100.200:2181 weight=5;
  }
  server {
      listen          12181 tcp;
      proxy_responses 1;
      proxy_timeout   15s;
      proxy_pass      zookeeper;
  }
  ```

- 以下是DNS负载的实例(udp)：

  ```nginx
  upstream dns {
      server 10.10.200.101:53;
      server 10.10.200.102:53;
  }
  server {
      listen          53 udp;
      proxy_responses 1;
      proxy_timeout   15s;
      proxy_bind      $server_addr:$remote_port;
      proxy_pass      dns;
  }
  ```

------

## 限流

### QPS限制(漏桶算法)

官方文档

http://nginx.org/en/docs/http/ngx_http_limit_req_module.html

使用Jmeter进行压测

配置如下

```nginx
http {
    limit_req_zone $binary_remote_addr zone=one:10m rate=1r/s;	#设置一个大小为10m的one区域,限制每秒访问一次
    ...
	server {
        
        location / {
        	limit_req zone=one burst=5 nodelay;	#设置漏桶大小为5,当桶满了,把其访问全部报放弃
        }
```

### 带宽限制(令牌桶算法)

```nginx
location / {
	limit_rate_after 	1m;		#在下载了1m后的带宽速率
	limit_rate 1k;			#限制下载带宽速度只有1k
}
```

### 并发数限制(计数算法)

```nginx
http {
    limit_conn_zone $binary_remote_addr zone=two:10m;	#设置一个大小为10M的two区域作计数
    ...
	server {
        
        location / {
        	limit_conn two 1;	#限制客户端并发数为1
        }
```

------

## 日志

ngx_http_empty_gif_module()

http://nginx.org/en/docs/http/ngx_http_empty_gif_module.html

- 发送一象素的请求过来

```nginx
location = /_.gif {
    empty_gif;
}
```

### 日志缓存区

ngx_http_log_module

http://nginx.org/en/docs/http/ngx_http_log_module.html

> 默认日志是没有开启buffer缓存区的,是有多少请求就直接写入acces.log日志文件,IO效率极低

```nginx
http {
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/access.log  main buffer=32k;	#main这里是别名的意思,指定上面的日志格式
	#access_log  /var/log/access.log  main buffer=32k flush=1s; #如要求不写满缓存要立刻落地可配置flush=1s,意思为每秒刷新进硬盘
    #access_log  /var/log/access.log  main buffer=32k flush=1s gzip=9; #可使用gzip进行压缩写入文件,根据配置的压缩等级进行高压
    #open_log_file_cache max=5;		#每次写入日志时都会打开和关闭文件,有一定消耗,但是，由于常用文件的描述符可以存储在缓存中，因此可以在open_log_file_cache 指令的valid参数 指定的时间内继续写入旧文件,这里配置最大文件打开数为5
   	...
```

> 配置后当访问页面看不到access.log日志有信息不用急,是因为访问信息写到了缓存里,当缓存积载超过配置值后才会写进磁盘(当段电或者服务器宕机会靠成缓存丢失风险)

> 如进行压缩写入日志文件后,文件不能直接读取,要把文件进行如下操作(否则可直接与置成以.gz为后缀的日志文件)
>
> ```shell
> cp access.log nginx-access.gz	#把原日志文件改成gz文件 
> gzip -d nginx-access.gz		#解压缩日志文件,最后会生成一个nginx-acccess可读日志文件
> ```

> 日志格式以json文件格式记录,以下为范例,以文本格式记录方便如需使用关系型数据库进行大数据进行存储分析用户行为
>
> ```nginx
> log_format  ngxlog json '{"timestamp":"$time_iso8601",'
>                     '"source":"$server_addr",'
>                     '"hostname":"$hostname",'
>                     '"remote_user":"$remote_user",'
>                     '"ip":"$http_x_forwarded_for",'
>                     '"client":"$remote_addr",'
>                     '"request_method":"$request_method",'
>                     '"scheme":"$scheme",'
>                     '"domain":"$server_name",'
>                     '"referer":"$http_referer",'
>                     '"request":"$request_uri",'
>                     '"requesturl":"$request",'
>                     '"args":"$args",'
>                     '"size":$body_bytes_sent,'
>                     '"status": $status,'
>                     '"responsetime":$request_time,'
>                     '"upstreamtime":"$upstream_response_time",'
>                     '"upstreamaddr":"$upstream_addr",'
>                     '"http_user_agent":"$http_user_agent",'
>                     '"http_cookie":"$http_cookie",'
>                     '"https":"$https"'
>                     '}';
> ```
>
> 

#### errorlog

http://nginx.org/en/docs/ngx_core_module.html#error_log

#### 日志分割

1.脚本

2.Logrotate 

------

## 上游服务健康状态

### 重试机制

http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_next_upstream

```nginx
    upstream httpget{
        server 192.168.50.11:8080	max_fails=5 fail_timeout=10s;	#10秒内失败5次下线,或失败5次后间隔10秒可重上线
      	server 192.168.50.12:8080;
    }

    location / {
    	proxy_next_upstream error timeout;	#当发生错误或超时把请求传递到下一个服务器
    	proxy_next_upstream_timeout 15s;	#代表
    	proxy_next_upstream_tries 5;	#重试次数,代表当时间范围内重试次数超过指定次数
		proxy_pass http://httpget;
    }
```

- **`max_fails`**

  最大失败次数,0为标记一直可用，不检查健康状态

- **`fail_timeout`**

  失败时间,当`fail_timeout`时间内失败了`max_fails`次，标记服务不可用,`fail_timeout`时间后会再次激活次服务

- **`proxy_next_upstream`** 

- **`proxy_next_upstream_timeout`** 

  重试最大超时时间
  
- **proxy_next_upstream_tries** 

  重试次数，包括第一次,`proxy_next_upstream_timeout`时间内允许`proxy_next_upstream_tries`次重试

### 主动健康检查

tengine版

https://github.com/yaoweibin/nginx_upstream_check_module

nginx商业版

http://nginx.org/en/docs/http/ngx_http_upstream_hc_module.html

> 商业版本是付费版本,在使用上各tengine版本是一样的,tengine版本必须匹配其适合的nginx版本进行打补丁使用,以下是tengine版本配置示例

```nginx
 http {

        upstream cluster {

            # simple round-robin
            server 192.168.0.1:80;
            server 192.168.0.2:80;

            check interval=3000 rise=2 fall=5 timeout=1000 type=http;	#使用http进行访问检查,每隔5秒访问一次,
            check_http_send "HEAD / HTTP/1.0\r\n\r\n";	#发送的头信息类型
            check_http_expect_alive http_2xx http_3xx;	#当返回不是这些类型表明服务器异常
        }

        server {
            listen 80;

            location / {
                proxy_pass http://cluster;
            }

            location /status {
                check_status;

                access_log   off;
                allow SOME.IP.ADD.RESS;
                deny all;
           }
        }

}
```

补丁过程

在上面页面下载最新的补丁,示例最新为1.20.1:https://github.com/yaoweibin/nginx_upstream_check_module/blob/master/check_1.20.1%2B.patch

> **注意:必须必须必须下载对应的Nginx版本,可以使用1.20.2**

```shell
#上面的页面上的代码复制到一个新文件名字可以任意,这里使用名为path,放到/root的目录下,全路径为/root/path
$ tar -xzvf nginx-1.20.2.tar.gz
$ cd nginx-1.20.2
$ patch -p1 < /root/path	#意思是跳过第一行打补丁
```

> 如果使用patch命令失败,,因为系统没有进行安装
>
> ```shell
> yum install -y patch
> ```

下载check模块,https://github.com/yaoweibin/nginx_upstream_check_module/tags,把下载好的check添加到Nginx

```shell
$ ./configure --add-module=/root/nginx_upstream_check_module-0.4.0	#增加check模块到Nginx

$ make
$ make install
```

------

# Nginx二次开发

## Lua

https://coolshell.cn/articles/10739.html

## `Openresty`

Nginx是一个主进程配合多个工作进程的工作模式，每个进程由单个线程来处理多个连接。

在生产环境中，我们往往会把cpu内核直接绑定到工作进程上，从而提升性能。

### 安装

#### 预编译安装

以CentOS举例 其他系统参照：http://openresty.org/cn/linux-packages.html

你可以在你的 CentOS 系统中添加 openresty 仓库，这样就可以便于未来安装或更新我们的软件包（通过 yum update 命令）。运行下面的命令就可以添加我们的仓库：

```shell
yum install yum-utils
```

```shell
yum-config-manager --add-repo https://openresty.org/package/centos/openresty.repo
```

然后就可以像下面这样安装软件包，比如 openresty：

```shell
yum install openresty
```

如果你想安装命令行工具 resty，那么可以像下面这样安装 openresty-resty 包：

```shell
sudo yum install openresty-resty
```

#### 源码编译安装

- 下载 http://openresty.org/cn/download.html,最小版本基于nginx1.21

  ```shell
  wget https://openresty.org/download/openresty-1.21.4.1.tar.gz
  tar -xvzf openresty-1.21.4.1.tar.gz
  ```

  然后在进入 `openresty-VERSION/ `目录, 然后输入以下命令进行编译配置:

- 编译安装

  ```shell
  ./configure
  make && make install
  ```

   默认, `--prefix=/usr/local/openresty` 程序会被安装到`/usr/local/openresty`目录。

  依赖 `gcc openssl-devel pcre-devel zlib-devel`

  安装：`yum install gcc openssl-devel pcre-devel zlib-devel postgresql-devel`

  您可以指定各种选项，比如

  ```shell
  ./configure --prefix=/opt/openresty \
              --with-luajit \
              --without-http_redis2_module \
              --with-http_iconv_module \
              --with-http_postgres_module
  ```

  试着使用 `./configure --help` 查看更多的选项。

  检查是否安装成功

  ```shell
  cd /usr/local/openresty/bin
  [root@master bin]# ./openresty -V
  nginx version: openresty/1.21.4.1
  built by gcc 4.8.5 20150623 (Red Hat 4.8.5-44) (GCC) 
  built with OpenSSL 1.0.2k-fips  26 Jan 2017
  TLS SNI support enabled
  configure arguments: --prefix=/usr/local/openresty/nginx --with-cc-opt=-O2 --add-module=../ngx_devel_kit-0.3.1 --add-module=../echo-nginx-module-0.62 --add-module=../xss-nginx-module-0.06 --add-module=../ngx_coolkit-0.2 --add-module=../set-misc-nginx-module-0.33 --add-module=../form-input-nginx-module-0.12 --add-module=../encrypted-session-nginx-module-0.09 --add-module=../srcache-nginx-module-0.32 --add-module=../ngx_lua-0.10.21 --add-module=../ngx_lua_upstream-0.07 --add-module=../headers-more-nginx-module-0.33 --add-module=../array-var-nginx-module-0.05 --add-module=../memc-nginx-module-0.19 --add-module=../redis2-nginx-module-0.15 --add-module=../redis-nginx-module-0.3.9 --add-module=../rds-json-nginx-module-0.15 --add-module=../rds-csv-nginx-module-0.09 --add-module=../ngx_stream_lua-0.0.11 --with-ld-opt=-Wl,-rpath,/usr/local/openresty/luajit/lib --with-stream --with-stream_ssl_module --with-stream_ssl_preread_module --with-http_ssl_module
  ```

- 启动

  ```shell
  ./openresty -c /usr/local/openresty/nginx/conf/nginx.conf
  ```

- 测试

  ```nginx
  ./openresty -t
  ```

- 重载

  ```shell
  ./openresty -s reload
  ```

> **可以看到使用openresty与使用Nginx是同理的**

## 测试lua脚本

编辑openresty配置文件

```shell
vim /usr/local/openresty/nginx/conf/nginx.conf
```

添加配置内容如下

```nginx
   location /lua {

        default_type text/html;
        content_by_lua '
           ngx.say("<p>Hello, World!</p>")
         ';
      }
```

访问地址:http://hostname/lua,可以看到页面内容输出`lua`脚本本语言输出内容为`<p>Hello, World!</p>`

## `lua-nginx-module`

### 配置文件

创建`/usr/local/openresty/nginx/lua/hello.lua`文件,内容如下

```lua
ngx.say("<p>Hello, World!</p>")
```

`nginx.conf`配置文件如下

```nginx
   server {
        listen       80;
        server_name  localhost;

   location /lua {

        default_type text/html;

        content_by_lua_file conf/lua/hello.lua;

    }
}
```

### 指令

- `set_by_lua` 修改nginx变量
- `rewrite_by_lua` 修改uri
- `access_by_lua` 访问控制
- `header_filter_by_lua` 修改响应头
- `body_filter_by_lua` 修改响应体
- `log_by_lua` 日志

### 热部署

在`ngonx.conf`文件`http`里配置

```nginx
lua_code_cache off
```

> **强烈来建议进行配置,因为会影响nginx的性能**

### 获取Nginx请求头信息

```lua
local headers = ngx.req.get_headers()                         

ngx.say("Host : ", headers["Host"], "<br/>")  

ngx.say("user-agent : ", headers["user-agent"], "<br/>")  

ngx.say("user-agent : ", headers.user_agent, "<br/>")

for k,v in pairs(headers) do  

    if type(v) == "table" then  

        ngx.say(k, " : ", table.concat(v, ","), "<br/>")  

    else  

        ngx.say(k, " : ", v, "<br/>")  

    end  

end 
```

### 获取post请求参数

```lua
ngx.req.read_body()  

ngx.say("post args begin", "<br/>")  

local post_args = ngx.req.get_post_args()  

for k, v in pairs(post_args) do  

    if type(v) == "table" then  

        ngx.say(k, " : ", table.concat(v, ", "), "<br/>")  

    else  

        ngx.say(k, ": ", v, "<br/>")  

    end  
end
```

### http协议版本

```lua
ngx.say("ngx.req.http_version : ", ngx.req.http_version(), "<br/>")
```

### 请求方法

```lua
ngx.say("ngx.req.get_method : ", ngx.req.get_method(), "<br/>")  
```

### 原始的请求头内容  

```lua
ngx.say("ngx.req.raw_header : ",  ngx.req.raw_header(), "<br/>")  
```

### body内容体

```lua
ngx.say("ngx.req.get_body_data() : ", ngx.req.get_body_data(), "<br/>")
```

## Nginx缓存

### Nginx全局内存缓存

在`/usr/local/openresty/nginx/conf/nginx.conf`添加如下内容

```nginx
lua_shared_dict shared_data 1m;
```

lua脚本如下

```lua
local shared_data = ngx.shared.shared_data

local i = shared_data:get("i")  

if not i then  

    i = 1  

    shared_data:set("i", i)  

    ngx.say("lazy set i ", i, "<br/>")  
end  
 

i = shared_data:incr("i", 1)  

ngx.say("i=", i, "<br/>")
```

访问页面效果如下,初次访问页面显示

```
lazy set i 1
i=2
```

再次访问可以看到,i不段递加

### `lua-resty-lrucache`

Lua 实现的一个简单的 LRU 缓存，适合在 Lua 空间里直接缓存较为复杂的 Lua 数据结构：它相比 ngx_lua 共享内存字典可以省去较昂贵的序列化操作，相比 memcached 这样的外部服务又能省去较昂贵的 socket 操作

https://github.com/openresty/lua-resty-lrucache

在`openresty`引入配置如下

```nginx
        location /lua {

            default_type text/html;

            content_by_lua_block {
                    require("my/cache").go()
            }
        }

```

> 注意:这里的自定义函数路径如下`/usr/local/openresty/lualib/my/cache.lua`,因为上面require会按固有的路径进行匹配查询的,详细可看报错提示

自定义`lua`函数

> 注意:这里不能开启`lua_code_cache off`热部署配置,否则缓存会无效,因为下在的代码每次都会重头开始执行,都会执行new初始化空间

```lua
local _M = {}

lrucache = require "resty.lrucache"

c, err = lrucache.new(200)  -- allow up to 200 items in the cache
ngx.say("count=init")


if not c then
    error("failed to create the cache: " .. (err or "unknown"))
end

function _M.go()

count = c:get("count")

c:set("count",100)
ngx.say("count=", count, " --<br/>")


if not count then  


    c:set("count",1)

    ngx.say("lazy set count ", c:get("count"), "<br/>")  

else


c:set("count",count+1)
 


ngx.say("count=", count, "<br/>")
end


end
return _M
```

首次访问页面显示如下

```
count=init count=nil --
lazy set count 1
```

再次访问可以显示缓存递增成功

```
count=1 --
count=1
count=2 --
count=2
```

> **两个缓存的的实现和效率都是一样的,只是一个是有锁一个是无锁,两个都是线程安全的操作内存数据**

------

## `lua-resty-redis`访问`redis`

https://github.com/openresty/lua-resty-redis

- 常用方法

  ```lua
  local res, err = red:get("key")
  
  local res, err = red:lrange("nokey", 0, 1)
  
  ngx.say("res:",cjson.encode(res))
  ```

- 创建连接

  ```lua
  red, err = redis:new()
  
  ok, err = red:connect(host, port, options_table?)
  ```

- timeout

  ```lua
  red:set_timeout(time)
  ```

- keepalive

  ```lua
  red:set_keepalive(max_idle_timeout, pool_size)
  ```

- close

  ```lua
  ok, err = red:close()
  ```

- pipeline

  ```lua
  red:init_pipeline()
  
  results, err = red:commit_pipeline()
  ```

- 认证

  ```lua
  local res, err = red:auth("foobared")
  
  if not res then
  
      ngx.say("failed to authenticate: ", err)
  
      return
  end
  ```

- 完整登录示范

  ```lua
  local redis = require "resty.redis"
  local red = redis:new()
  
  red:set_timeouts(1000, 1000, 1000) -- 1 sec
  
  local ok, err = red:connect("127.0.0.1", 6379)
  if not ok then
      ngx.say("failed to connect: ", err)
      return
  end
  
  ok, err = red:set("dog", "an animal")
  if not ok then
      ngx.say("failed to set dog: ", err)
      return
  end
  
  ngx.say("set result: ", ok)
  
  local res, err = red:get("dog")
  if not res then
      ngx.say("failed to get dog: ", err)
      return
  end
  
  if res == ngx.null then
      ngx.say("dog not found.")
      return
  end
  
  
  ngx.say("dog: ", res)
  
  red:close()
  ```
  
  > 实际应用生产中可直接通过nginx访问redis,跳过中间层的Tomcat,减少代理层之前不必要的处理,是处理高并发的一种手段这一,但要注意双写缓存一致的问题

- redis-cluster支持

  https://github.com/steve0511/resty-redis-cluster

------

## `lua-resty-mysql`

https://github.com/openresty/lua-resty-mysql

```lua
local mysql = require "resty.mysql"
local db, err = mysql:new()
if not db then
    ngx.say("failed to instantiate mysql: ", err)
    return
end

db:set_timeout(1000) -- 1 sec


local ok, err, errcode, sqlstate = db:connect{
    host = "192.168.44.211",
    port = 3306,
    database = "zhangmen",
    user = "root",
    password = "111111",
    charset = "utf8",
    max_packet_size = 1024 * 1024,
}


ngx.say("connected to mysql.<br>")



local res, err, errcode, sqlstate = db:query("drop table if exists cats")
if not res then
    ngx.say("bad result: ", err, ": ", errcode, ": ", sqlstate, ".")
    return
end


res, err, errcode, sqlstate =
db:query("create table cats "
    .. "(id serial primary key, "
    .. "name varchar(5))")
if not res then
    ngx.say("bad result: ", err, ": ", errcode, ": ", sqlstate, ".")
    return
end

ngx.say("table cats created.")



res, err, errcode, sqlstate =
db:query("select * from t_emp")
if not res then
    ngx.say("bad result: ", err, ": ", errcode, ": ", sqlstate, ".")
    return
end

local cjson = require "cjson"
ngx.say("result: ", cjson.encode(res))


local ok, err = db:set_keepalive(10000, 100)
if not ok then
    ngx.say("failed to set keepalive: ", err)
    return
end
```

> **注意:不要使用带参数的sql查询,会有注入危险**

------

## 模板实时渲染 `lua-resty-template`

https://github.com/bungle/lua-resty-template

如果学习过JavaEE中的servlet和JSP的话，应该知道JSP模板最终会被翻译成Servlet来执行；

而lua-resty-template模板引擎可以认为是JSP，其最终会被翻译成Lua代码，然后通过ngx.print输出。   

lua-resty-template大体内容有： 

- 模板位置：从哪里查找模板
- 变量输出/转义：变量值输出
- 代码片段：执行代码片段，完成如if/else、for等复杂逻辑，调用对象函数/方法
- 注释：解释代码片段含义； 
- include：包含另一个模板片段
- 其他：lua-resty-template还提供了不需要解析片段、简单布局、可复用的代码块、宏指令等支持。

基础语法

- {(include_file)}：包含另一个模板文件
- {* var *}：变量输出
- {{ var }}：变量转义输出
- {% code %}：代码片段
- {# comment #}：注释
- {-raw-}：中间的内容不会解析，作为纯文本输出

### 示例

1. 初始化

   ```lua
   -- Using template.new
   local template = require "resty.template"
   local view = template.new "view.html"
   view.message = "Hello, World!"
   view:render()
   
   -- Using template.render
   -- template.render("view.html", { message = "Hel11lo, Worl1d!" })
   ```

2. 执行函数,得到渲染之后的内容

   ```lua
   local func = template.compile("view.html")  
   
   local content = func(context)  
   
   ngx.say("xx:",content) 
   ```

3. 模板文件存放位置

   `nginx.conf`中的配置如下

   ```nginx
   set $template_root /usr/local/openresty/nginx/tmp;
   ```

4. 模板文件`resty.template.html`

   ```lua
   local template = require("resty.template")
   local html = require "resty.template.html"
   
   template.render([[
   <ul>
   {% for _, person in ipairs(context) do %}
       {*html.li(person.name)*} --
   {% end %}
   </ul>
   <table>
   {% for _, person in ipairs(context) do %}
       <tr data-sort="{{(person.name or ""):lower()}}">
           {*html.td{ id = person.id }(person.name)*}
       </tr>
   {% end %}
   </table>]], {
       { id = 1, name = "Emma"},
       { id = 2, name = "James" },
       { id = 3, name = "Nicholas" },
       { id = 4 }
   })
   ```

5. 模板内容

   ```html
   <!DOCTYPE html>
   <html>
   <body>
     <h1>{{message}}</h1>
   </body>
   </html>
   ```

### 多值传入

```lua
template.caching(false)
local template = require("resty.template")
local context = {
    name = "lucy",
    age = 50,
}
template.render("view.html", context)
```

模板内容

```html
<!DOCTYPE html>
<html>
<body>
  <h1>name:{{name}}</h1>
  <h1>age:{{age}}</h1>
</body>
</html>
```

### 模板管理与缓存

模板缓存：默认开启，开发环境可以手动关闭

```template.caching(true)```

模板文件需要业务系统更新与维护，当模板文件更新后，可以通过模板版本号或消息通知Openresty清空缓存重载模板到内存中

`template.cache = {}`

### 完整页面

```lua
local template = require("resty.template")
template.caching(false)
local context = {
    title = "测试",
    name = "lucy",
    description = "<script>alert(1);</script>",
    age = 40,
    hobby = {"电影", "音乐", "阅读"},
    score = {语文 = 90, 数学 = 80, 英语 = 70},
    score2 = {
        {name = "语文", score = 90},
        {name = "数学", score = 80},
        {name = "英语", score = 70},
    }
}

template.render("view.html", context)
```

模板

```html
{(header.html)}  
   <body>  
      {# 不转义变量输出 #}  
      姓名：{* string.upper(name) *}<br/>  
      {# 转义变量输出 #}  
      简介：{{description}}
           简介：{* description *}<br/>  
      {# 可以做一些运算 #}  
      年龄: {* age + 10 *}<br/>  
      {# 循环输出 #}  
      爱好：  
      {% for i, v in ipairs(hobby) do %}  
         {% if v == '电影' then  %} - xxoo
            
              {%else%}  - {* v *} 
{% end %}  
         
      {% end %}<br/>  
  
      成绩：  
      {% local i = 1; %}  
      {% for k, v in pairs(score) do %}  
         {% if i > 1 then %}，{% end %}  
         {* k *} = {* v *}  
         {% i = i + 1 %}  
      {% end %}<br/>  
      成绩2：  
      {% for i = 1, #score2 do local t = score2[i] %}  
         {% if i > 1 then %}，{% end %}  
          {* t.name *} = {* t.score *}  
      {% end %}<br/>  
      {# 中间内容不解析 #}  
      {-raw-}{(file)}{-raw-}  
{(footer.html)}  
```

### layout 布局统一风格

使用模板内容嵌套可以实现全站风格同一布局

```lua
local template = require "resty.template"
```

1. ```lua
   local layout   = template.new "layout.html"
   
   layout.title   = "Testing lua-resty-template"
   
   layout.view    = template.compile "view.html" { message = "Hello, World!" }
   
   layout:render()
   ```

2. ```lua
   template.render("layout.html", {
   
     title = "Testing lua-resty-template",
   
     msg = "type=2",
   
     view  = template.compile "view.html" { message = "Hello, World!" }
   
   })
   ```

3. 此方式重名变量值会被覆盖

   ```lua
   local view     = template.new("view.html", "layout.html")
   
   view.title     = "Testing lua-resty-template"
   
   view.msg = "type=3"
   
   view.message   = "Hello, World!"
   
   view:render()
   ```

4. 可以区分一下

   ```lua
   local layout   = template.new "layout.html"
   
   layout.title   = "Testing lua-resty-template"
   
   layout.msg = "type=4"
   
   local view     = template.new("view.html", layout)
   
   view.message   = "Hello, World!"
   
   view:render()
   ```

layout.html

```html
<!DOCTYPE html>

<html>

<head>

	 <title>{{title}}</title>

</head>

<h1>layout</h1>

<body>

	{*view*}

</body>

</html>
```

view.html

```lua
msg:{{message}}
```

### 多级嵌套

```lua
local view     = template.new("view.html", "layout.html")

view.title     = "Testing lua-resty-template"

view.message   = "Hello, World!"

view:render()

view.html

{% layout="section.html" %}
```

- `msg:{{message}}`

  - `section.html`

    ```html
    <div id="section">
    
    	{*view*} - sss
    
    </div>
    ```

  - `layout.html`

    ```html
    <!DOCTYPE html>
    
    <html>
    
    <head>
    
     <title>{{title}}</title>
    
    </head>
    ```

- `layout {{msg}}`

  ```html
  <body>
  
  	{*view*}
  
  </body>
  
  </html>
  ```

## Redis缓存+mysql+模板输出

```lua
  cjson = require "cjson"
sql="select * from t_emp"


local redis = require "resty.redis"
                local red = redis:new()

                red:set_timeouts(1000, 1000, 1000) -- 1 sec

  local ok, err = red:connect("127.0.0.1", 6379)
 if not ok then
                    ngx.say("failed to connect: ", err)
                    return
                end


        
                local res, err = red:get(sql)
                if not res then
                    ngx.say("failed to get sql: ", err)
                    return
                end

                if res == ngx.null then
                    ngx.say("sql"..sql.." not found.")




--mysql查询
local mysql = require "resty.mysql"
                local db, err = mysql:new()
                if not db then
                    ngx.say("failed to instantiate mysql: ", err)
                    return
                end

                db:set_timeout(1000) -- 1 sec


                local ok, err, errcode, sqlstate = db:connect{
                    host = "192.168.44.211",
                    port = 3306,
                    database = "zhangmen",
                    user = "root",
                    password = "111111",
                    charset = "utf8",
                    max_packet_size = 1024 * 1024,
                }


                ngx.say("connected to mysql.<br>")


 res, err, errcode, sqlstate =
                    db:query(sql)
                if not res then
                    ngx.say("bad result: ", err, ": ", errcode, ": ", sqlstate, ".")
                    return
                end


          --ngx.say("result: ", cjson.encode(res))



      ok, err = red:set(sql, cjson.encode(res))
                if not ok then
                    ngx.say("failed to set sql: ", err)
                    return
                end

                ngx.say("set result: ", ok)

                    return
                end








local template = require("resty.template")
template.caching(false)
local context = {
    title = "测试",
    name = "lucy",
    description = "<script>alert(1);</script>",
    age = 40,
    hobby = {"电影", "音乐", "阅读"},
    score = {语文 = 90, 数学 = 80, 英语 = 70},
    score2 = {
        {name = "语文", score = 90},
        {name = "数学", score = 80},
        {name = "英语", score = 70},
    },

zhangmen=cjson.decode(res)

}





template.render("view.html", context)
```

模板文件

```html
{(header.html)}  
   <body>  
      {# 不转义变量输出 #}  
      姓名：{* string.upper(name) *}<br/>  
      {# 转义变量输出 #}  

      年龄: {* age + 10 *}<br/>  
      {# 循环输出 #}  
      爱好：  
      {% for i, v in ipairs(hobby) do %}  
         {% if v == '电影' then  %} - xxoo
            
              {%else%}  - {* v *} 
{% end %}  
         
      {% end %}<br/>  
  
      成绩：  
      {% local i = 1; %}  
      {% for k, v in pairs(score) do %}  
         {% if i > 1 then %}，{% end %}  
         {* k *} = {* v *}  
         {% i = i + 1 %}  
      {% end %}<br/>  
      成绩2：  
      {% for i = 1, #score2 do local t = score2[i] %}  
         {% if i > 1 then %}，{% end %}  
          {* t.name *} = {* t.score *}  
      {% end %}<br/>  
      {# 中间内容不解析 #}  
      {-raw-}{(file)}{-raw-}  




掌门：
{* zhangmen *}



   {% for i = 1, #zhangmen do local z = zhangmen[i] %}  
         {* z.deptId *},{* z.age *},{* z.name *},{* z.empno *},<br>
      {% end %}<br/>  

{(footer.html)} 
```

------

## Lua 开源项目

### WAF

https://github.com/unixhot/waf

https://github.com/loveshell/ngx_lua_waf

- 防止 SQL 注入，本地包含，部分溢出，fuzzing 测试，XSS/SSRF 等 Web 攻击
- 防止 Apache Bench 之类压力测试工具的攻击
- 屏蔽常见的扫描黑客工具，扫描器
- 屏蔽图片附件类目录执行权限、防止 webshell 上传
- 支持 IP 白名单和黑名单功能，直接将黑名单的 IP 访问拒绝
- 支持 URL 白名单，将不需要过滤的 URL 进行定义
- 支持 User-Agent 的过滤、支持 CC 攻击防护、限制单个 URL 指定时间的访问次数
- 支持支持 Cookie 过滤，URL 与 URL 参数过滤
- 支持日志记录，将所有拒绝的操作，记录到日志中去

### Kong 基于Openresty的流量网关

https://konghq.com/

https://github.com/kong/kong

Kong 基于 OpenResty，是一个云原生、快速、可扩展、分布式的微服务抽象层（Microservice Abstraction Layer），也叫 API 网关（API Gateway），在 Service Mesh 里也叫 API 中间件（API Middleware）。

Kong 开源于 2015 年，核心价值在于高性能和扩展性。从全球 5000 强的组织统计数据来看，Kong 是现在依然在维护的，在生产环境使用最广泛的 API 网关。

Kong 宣称自己是世界上最流行的开源微服务 API 网关（The World’s Most Popular Open Source Microservice API Gateway）。

核心优势：

- 可扩展：可以方便的通过添加节点水平扩展，这意味着可以在很低的延迟下支持很大的系统负载。
- 模块化：可以通过添加新的插件来扩展 Kong 的能力，这些插件可以通过 RESTful Admin API 来安装和配置。
- 在任何基础架构上运行：Kong 可以在任何地方都能运行，比如在云或混合环境中部署 Kong，单个或全球的数据中心。

###  APISIX

### ABTestingGateway

https://github.com/CNSRE/ABTestingGateway

ABTestingGateway 是一个可以动态设置分流策略的网关，关注与灰度发布相关领域，基于 Nginx 和 ngx-lua 开发，使用 Redis 作为分流策略数据库，可以实现动态调度功能。

ABTestingGateway 是新浪微博内部的动态路由系统 dygateway 的一部分，目前已经开源。在以往的基于 Nginx 实现的灰度系统中，分流逻辑往往通过 rewrite 阶段的 if 和 rewrite 指令等实现，优点是性能较高，缺点是功能受限、容易出错，以及转发规则固定，只能静态分流。ABTestingGateway 则采用 ngx-lua，通过启用 lua-shared-dict 和 lua-resty-lock 作为系统缓存和缓存锁，系统获得了较为接近原生 Nginx 转发的性能。

- 支持多种分流方式，目前包括 iprange、uidrange、uid 尾数和指定uid分流
- 支持多级分流，动态设置分流策略，即时生效，无需重启
- 可扩展性，提供了开发框架，开发者可以灵活添加新的分流方式，实现二次开发
- 高性能，压测数据接近原生 Nginx 转发
- 灰度系统配置写在 Nginx 配置文件中，方便管理员配置
- 适用于多种场景：灰度发布、AB 测试和负载均衡等	
