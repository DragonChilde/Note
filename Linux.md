

**生产环境服务器变慢，诊断思路和性能评估谈谈**

# 整机：top #

	top - 11:31:31 up  8:11,  3 users,  load average: 0.72, 0.62, 0.39
	Tasks: 251 total,   3 running, 248 sleeping,   0 stopped,   0 zombie
	%Cpu(s):  0.7 us,  1.1 sy,  0.0 ni, 97.5 id,  0.4 wa,  0.0 hi,  0.4 si,  0.0 st
	KiB Mem :  1865284 total,    31452 free,  1206156 used,   627676 buff/cache
	KiB Swap:  2097148 total,   128764 free,  1968384 used.   440464 avail Mem 
	
	   PID USER      PR  NI    VIRT    RES    SHR S %CPU %MEM     TIME+ COMMAND                                             
	  2582 oracle    -2   0    0.0p   0.0p   0.0p S  2.3  0.1  10:59.24 ora_vktm_orclcd                                     
	  2352 mysql     20   0    0.0p   0.0p   0.0p S  0.7  1.0   1:59.52 mysqld                                              
	     9 root      20   0    0.0p   0.0p   0.0p S  0.3  0.0   0:08.54 rcu_sched                                           
	  1301 root      20   0    0.0p   0.0p   0.0p S  0.3  0.1   0:35.20 redis-server                                        
	  1445 root      20   0    0.0p   0.0p   0.0p S  0.3  0.5   1:10.61 java 	
	
	//注意重要的是看%CPU %MEM load average 这三个

**uptime，系统性能top命令的精简版**

	11:34:41 up  8:14,  3 users,  load average: 0.06, 0.35, 0.33

## CPU:vmstat ##

	[root@CentOS ~]# vmstat -n 2 3
	procs -----------memory---------- ---swap-- -----io---- -system-- ------cpu-----
	 r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa st
	 6  0 1957620  15900   6968 621960   87  121  1650   202 1482  889  2  2 96  0  0
	 0  0 1957620  15776   6984 622000   32    0    32    48 1501 2413  1  1 98  0  0
	 0  0 1957620  15620   6984 622072   16    0    16     0 1477 2392  1  1 99  0  0

vmstat -n 2 3

一般vmstat工具的使用是通过两个数字参数来完成的，第一个参数是采样的时间间隔数单位是秒，第二个参数是采样的次数。

重要的是（procs、cpu）

procs

- r：运行等待CPU时间片的进程数，原则上1核的CPU运行队列不要超过2，整个系统的运行队列不能超过总核数的2倍，否则代表系统压力过大。
- b:等待资源的进程数，比如正在等待磁盘I/O 、网络I/O等

cpu

- us：用户进程消耗CPU时间百分比，us值高，用户进程小号CPU时间多，如果长期大于50%，优化程序；
- sy：内核进程消耗的CPU百分比；
- us+sy参考值为80%，如果us+sy大于80%，说明可能存在CPU不足

（后面三个没这么重要）

id:处于空闲的CPU百分比

wa:系统等待IO的CPU时间百分比

st:来自于一个虚拟机偷取的CPU时间的百分比

**额外查看CPU命令**

**查看所有cpu核信息mpstat -P ALL 2**

	[root@CentOS ~]# mpstat -P ALL 2
	Linux 3.10.0-862.el7.x86_64 (CentOS)    2019年07月06日  _x86_64_        (1 CPU)
	
	11时58分04秒  CPU    %usr   %nice    %sys %iowait    %irq   %soft  %steal  %guest  %gnice   %idle
	11时58分06秒  all    0.54    0.00    0.54    0.00    0.00    0.00    0.00    0.00    0.00   98.91
	11时58分06秒    0    0.54    0.00    0.54    0.00    0.00    0.00    0.00    0.00    0.00   98.91

**每个进程使用cpu的用量分解信息pidstat -u 1 -p 进程编号**

	[root@CentOS ~]# pidstat -u 1 -p 1301
	Linux 3.10.0-862.el7.x86_64 (CentOS)    2019年07月06日  _x86_64_        (1 CPU)
	
	12时00分45秒   UID       PID    %usr %system  %guest    %CPU   CPU  Command
	12时00分46秒     0      1301    1.12    0.00    0.00    1.12     0  redis-server
	12时00分47秒     0      1301    0.00    0.00    0.00    0.00     0  redis-server
	12时00分48秒     0      1301    0.00    0.00    0.00    0.00     0  redis-server
	12时00分49秒     0      1301    0.00    0.00    0.00    0.00     0  redis-server
	12时00分50秒     0      1301    0.00    0.00    0.00    0.00     0  redis-server
	12时00分51秒     0      1301    0.00    0.00    0.00    0.00     0  redis-server

**内存：free**

- 应用程序可用内存/系统物理内存>70%内存充足。
- 应用程序可用内存/系统物理内存<20%内存不足，需要增加内存。
- 20%<应用程序可用内存/系统物理内存<70%内训基本够用

		/**用的比较多的是free -m比较精确，free -g会省略了后面的数字**/
		[root@CentOS ~]# free
		         	 total        used        free      shared  buff/cache   available
		Mem:        1865284     1140328       16100      167864      708856      409476
		Swap:       2097148     2096848         300
		[root@CentOS ~]# free -g
		              total        used        free      shared  buff/cache   available
		Mem:              1           1           0           0           0           0
		Swap:             1           1           0
		[root@CentOS ~]# free -m
		              total        used        free      shared  buff/cache   available
		Mem:           1821        1110          19         163         691         402
		Swap:          2047        2047           0
	

**以上三个命令在实际应用中比较重要**

**硬盘：df**

	/**查看磁盘剩余空间数**/
	[root@CentOS ~]# df
	文件系统          1K-块     已用     可用 已用% 挂载点
	/dev/sda2      47141660 27090460 17633436   61% /
	devtmpfs         916908        0   916908    0% /dev
	tmpfs            932640   413696   518944   45% /dev/shm
	tmpfs            932640    27196   905444    3% /run
	tmpfs            932640        0   932640    0% /sys/fs/cgroup
	/dev/sda5       2030416    13296  1895932    1% /home
	/dev/sda1        194235   137103    42796   77% /boot
	tmpfs            186532        8   186524    1% /run/user/42
	overlay        47141660 27090460 17633436   61% /var/lib/docker/overlay2/83e3303fc65b304a3323fbc4a88ef856e281a1be5f610929923e5648d1226b78/merged
	shm               65536        0    65536    0% /var/lib/docker/containers/fe8f80bdb42e5724f47905de199d8d14d73cb82b83b05c14bca8d8557c6f5e6d/shm
	tmpfs            186532       48   186484    1% /run/user/1000
	tmpfs            186532        0   186532    0% /run/user/0
	[root@CentOS ~]# df -h
	文件系统        容量  已用  可用 已用% 挂载点
	/dev/sda2        45G   26G   17G   61% /
	devtmpfs        896M     0  896M    0% /dev
	tmpfs           911M  404M  507M   45% /dev/shm
	tmpfs           911M   27M  885M    3% /run
	tmpfs           911M     0  911M    0% /sys/fs/cgroup
	/dev/sda5       2.0G   13M  1.9G    1% /home
	/dev/sda1       190M  134M   42M   77% /boot
	tmpfs           183M  8.0K  183M    1% /run/user/42
	overlay          45G   26G   17G   61% /var/lib/docker/overlay2/83e3303fc65b304a3323fbc4a88ef856e281a1be5f610929923e5648d1226b78/merged
	shm              64M     0   64M    0% /var/lib/docker/containers/fe8f80bdb42e5724f47905de199d8d14d73cb82b83b05c14bca8d8557c6f5e6d/shm
	tmpfs           183M   48K  183M    1% /run/user/1000
	tmpfs           183M     0  183M    0% /run/user/0

**磁盘IO：iostat**

磁盘块设备分布

- rkB/s每秒读取数据量KB
- wkB/s每秒写入数据量KB
- svctm I/O请求的平均服务时间，单位毫秒
- await I/O请求的平均等待时间，单位毫秒;值越小，性能越好
- **util 一秒中有百分几的时间用于I/O操作。接近100%时，表示磁盘带宽跑满，需要优化程序或者增加磁盘（重要）**
- rkB/s、wkB/s根据系统应用不同会有不同的值，但有规律遵循：长期、超大数据读写，肯定不正常，需要优化程序读取
- svctm的值与await的值很接近，表示几乎没有I/O等待，磁盘性能好，如果await的值远高于svctm的值，则表示I/O队列等待太长，需要优化程序或更换更快磁盘


		[root@CentOS ~]# iostat -xdk 2 3
		Linux 3.10.0-862.el7.x86_64 (CentOS)    2019年07月08日  _x86_64_        (1 CPU)
		
		Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
		sda              22.67    20.54   25.39    9.28  1291.27   179.54    84.84     0.05    1.38    1.43    1.25   0.36   1.26
		
		Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
		sda              15.14     2.70    2.70    3.78    73.51    30.27    32.00     0.01    1.50    3.20    0.29   1.50   0.97
		
		Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
		sda               0.00     0.00    0.00    1.09     0.00    17.39    32.00     0.00    0.50    0.00    0.50   0.50   0.05


**网络IO:ifstat**
	
	ifstat -l
	
	各个网卡的in out
	观察网络负载情况
	程序网络读写是否正常
	- 程序网络I/O优化
	- 增加网络I/O带宽


**Liunx目录结构**
	
	/bin[重点](/usr/bin /usr/local/bin) 	是Binary的缩写，这个目录存放着最经常使用的命令
	/sbin(/usr/sbin /usr/local/sbin)	s就是Super User的意思，这里存放的是系统管理员使用的系统管理程序
	/home[重点] 	存放普通用户的主目录，在Linux中每个用户都有一个自己的目录，一般该目录名是以用户的账号命名的
	/root[重点]	该目录为系统管理员，也称作超级权限者的用户主目录
	/lib	系统开机所需要最基本的动态连接共享库
	/lost+found	这个目录一般情况下是空的，当系统非法关机后，这里就存放了一些文件
	/etc[重点]	所有的系统管理所需要的配置文件和子目录my.conf
	/usr[重点]	这是一个非常重要的目录，用户的很多应用程序和文件都放在这个目录下，类似与windows下的program files目录
	/boot[重点]	存放的是启动Linux时使用的一些核心文件,包括一些连接文件又及镜像文件
	/proc	这个目录是一个虚拟的目录,它是系统内存的映射，访问这个目录来获取系统的信息
	/srv	service缩与，该目录存放一些服务启动之后需要提取的数据
	/sys	这是linux2.6内核的一个很大的变化。该目录下安装了2.6内核中新出现的一个文件系统
	/tmp	这个目录是用来存放一些临时文件的
	/dev	类似于windows的设备管理器，把所有的硬件用文件 的形式存储
	/media[重点]	linux系统会自动识别一些设备，例如U盘、光驱等等，当识别后，linux会把识别的设备挂载到这个目录下
	/mnt[重点]	系统提供该目录是为了让用户临时挂载别的文件系统的，我们可以将外部的存储挂载在/mnt/上，然后进入该目录就可以查看里的内容了.d:/myshare
	/opt	这是给主机额外安装软件所摆放的目录.如安装ORACLE数据库就可放到该目录下。默认为空
	/usr/local[重点]	这是另一个给主机额外安装软件所安装的目录。一般是通过编译源码方式安装的程序
	/var[重点]	这个目录中存放着在不断扩充着的东西，习惯将经常被修改的目录放在这个目录下。包括各种日志文件
	/selinux[security-enhanced linux]360	SELinux是一种安全子系统，它能控制程序只能访问特定文件

**Vim基本常用快捷键**
	
	yy 拷贝当前行
	nyy 拷贝当前后开始的n行，比如2yy拷贝当前行及其下一行。
	p  在当前光标后粘贴,如果之前使用了yy命令来复制一行，那么就在当前行的下一行粘贴。
	dd 删除当前行
	ndd 删除当前向下的n行
	/text　　查找text，按n健查找下一个，按N健查找前一个。
	:set nu	设置文件行号
	:set nonu	取消文件行号
	大写G定位文档最末行,gg定位文档最首行
	小写u	撤销文档中的输入操作
	行号+shift+g	光标移动到指定行号
	
	#插入命令
	o 在当前行之后插入一行
	O 在当前行之前插入一行
	a 在当前位置后插入
	A 在当前行尾插入
	
	#撤销和重做
	u 撤销（Undo）
	U 撤销对整行的操作
	Ctrl + r 重做（Redo），即撤销的撤销。
	
	#退出命令
	:e! 放弃所有修改，并打开原来文件。

**开机、重启和用户登录注销**
	
	shutdown -h now	立刻进行关机
	shutdown -h 1	1分钟后关机
	shutdown -r now	现在重新启动计算机
	halt			关机，作用和上面一样
	reboot			重新启动计算机
	sync			把内存数据同步到磁盘
	
	注意:不管是重启还是关闭,首先都要运行sync命令，把内存中的数据写到磁盘中

基本介绍:
	
- 登录时尽量少用root账号登录，因为它是系统管理员，最左的权限，避免操作失误。可以利用普通用户登录,登录后再用"su-用户名"命令来切换成系统管理员身分
- 在提示符下输入logout即可注销用户

**用户管理**

	添加用户 useradd [选项] 用户名
	#当创建用户成功后，会自动创建和用户同名的家目录
	给新创建的用户指定家目录 useradd -d 指定目录 新用户名	
	
	指定用户密码 passwd	用户名
	
	删除用户但保留家目录 userdel	用户名
	删除用户和家目录 userdel -r 用户名
	#在删除用户时，一般不会将家目录删除
	
	查询用户信息 id 用户名
	切换用户 su - 切换用户名
	#从权限高的用户切换到权限低的用户，不需要输入密码，反之需要
	#当需要返回到原来用户时，使用exit指令
	
	查询当前用户 whomani
	
	增加组 groupadd 组名
	删除组 groupdel 组名
	
	增加用户时直接加上组 useradd -g 用户组 用户名
	修改用户的组 usermod -g 用户组 用户名
	
	/etc/passwd 用户(user)的配置文件 ,记录用户的各种信息
	#每行含义:用户名:口令:用户标识号:组标识号:注释性描述:主目录:登录Shell
	
	/etc/shadow 口令的配置文件 
	#每行含义:登录名:加密口令:最后一次修改时间:最小时间间隔:最大时间间隔:警告时间:不活动时间:失效时间:标志
	
	/etc/group 组(group)的配置文件，记录Linux包含的组的信息
	#每行含义:组名:口令:组标识号:组内用户列表

# 实用指令 #

运行级别说明:

	0.关机
	1.单用户【找回丢失密码】
	2.多用户状态没有网络服务
	3.多用户状态有网络服务
	4.系统未使用保留给用户
	5.图形界面
	6.系统重启

**常用运行级别是3和5，修改默认的运行级别可改文件/etc/inittab的id:5:initdefault：这一行中的数字，即可运行Linux后进入指定模式**
	
	切换运行级别 init 3

假如root密码忘记了，可以进入模式1使用passwd指令修改密码

**帮助指令**
	
	获得帮助信息 man[命令或配置文件]
	获得shell内置命令的帮助信息 help 命令
**文件目录类**

	显示当前工作目录的绝对路径 pwd
	ls [选项] [目录或是文件]
		-a:显示当前目录所有的文件和目录，包括隐藏的
		-l:以列表的方式显示信息
	
	切换到指定目录 cd [参数]
	
	创建目录 mkdir [选项] 要创建的目录
		-p：创建多级目录
	删除空目录 rmdir [选项] 要删除的空目录
	#rmdir不能删除非空的目录
	rm -rf 要删除的目录
	#删除非空目录
	
	创建空文件 touch 文件名称
	
	拷贝文件到指定目录 cp [选项] source dest
							-r:递归复制整个文件夹
	#强制覆盖不提示的方法:\cp
	
	删除文件或目录 rm [选项] 要删除的文件或目录
					-r:递归删除整个文件夹
					-f:强制删除不提示
	
	移动文件与目录或重命名 
	重命名 mv oldNameFile newNameFile
	移动文件 mv /temp/movefile /targetFolder
	
	查看文件内容，以只读的方式打开 cat [选项] 要查看的文件
									-n：显示行号
	cat只能浏览文件，而不能修改文件，为了浏览方法,一般会带上管道命令 |more
	cat 文件名 |more [分面浏览]
	
	more 要查看的文件(一次性加载文件)
	分屏查看文件内容 less 要查看的文件(不是一次性加载文件，显示大型文件具有较高效率)
	
	>输出重定向:会将原来的文件的内容覆盖
	>>追加：不会覆盖原来文件的内容,而是追加到文件的尾部
	ls -l > 文件 列表的内容写入文件(覆盖写)
	#如果文件不存在，就创建该文件
	ls -al >> 文件 列表的内容追加到文件的末尾
	cat 文件1 > 文件2 将文件1的内容覆盖到文件2
	echo "内容" >> 文件
	
	echo输出内容到控制台 echo [选项] [输出内容]
	
	head用于显示文件的开头部分内容 
	head 文件 查看文件头10行内容
	head -n 5 文件 查看文件头5行内容
	
	tail用于输出文件尾部的内容
	tail 文件 查看文件后10行内容
	tail -n 5 查看文件后5行内容
	tail -f 文件 实时追踪文档的所有更新,工作中经常使用
	
	给原文件创建一个软链接 ln -s [原文件或目录] [软链接名]
	
	查看已经执行过历史命令 history
	显示最近使用过的10个指令 history 10
	执行历史编号为5的指令！178
**时间日期类**

	date 显示当前时间
	date "+%Y-%m-%d %H:%M:%S" 格式化显示
	注意格式化前面的+是不能少的
	date -s 字符串时间 设置日期
	date -s "2019-07-23 10:40:00"
	
	cal [选项] 不加选项,显示本月日历

**搜索查找类**

	find [搜索范围] [选项]
			-name<查询方式> 按照指定的文件名查找模式查找文件
			-user<用户名> 查找属于指定用户名所有文件
			-size<文件大小> 按照指定的文件大小查找文件(+n大于 -n小于 n等 于)
	find /home -name hello.txt
	find /opt/ -user root
	find / -size +1M
	find / -name *.txt
	
	locate指令可以快速定位文件路径.locate指令无需遍历整个文件系统,查询速度软快,根据创建的数据库快速定位文件
	locate 搜索文件(第一次运行前，必须使用updatedb指令创建locate数据库)
	
	grep过滤查找，管道符"|"表示将前一个命令的处理结果输出传递给后面的命令处理
	grep [选项] 查找内容 源文件
			-n 显示匹配行及行号
			-i 忽略字母大小写
	cat a.txt | grep -ni hello

**压缩和解压类**
	
	gzip 文件 压缩文件,只能将文件压缩为*.gz文件
	gunzip 文件.gz 解压缩文件命令
	#注：当使用gzip对文件进行压缩后，不会保留原来的文件
	
	zip [选项] XXX.zip 将要压缩的内容 压缩文件和目录的命令
		-r:递归压缩，即压缩目录
	unzip [选项] XXX.zip 解压缩文件
		-d<目录>：指定解压后文件的存放目录
	
	tar [选项] XXX.tar.gz 打包的内容 打包目录,压缩后的文件格式.tar.gz
		-c 产生.tar打包文件
		-v 显示详细信息
		-f 指宝压缩后的文件名
		-z 打包同时压缩
		-x 解包.tar文件
		解压:-zxvf 压缩：zcvf
	tar -zxvf a.tar.gz -C /home/
	#指压到指定的目录，目录要事先存在，否则报错

## 组管理和权限管理 ##

	usermod -g 组名 用户名 改变用户所在组
	usermod -d 目录名 用户名 改变该用户登录的初始目录
	
	u:所有者 g:所有组 o:其他人 a:所有人（u、g、o的总和）
	chmod u=rwx,g=rx,o=x 文件目录名
	chmod o+w 文件目录名
	chmod a-x 文件目录名
	
	chown newowner file 改变文件的所有者
	chown newowner:newgroup file 改变用户的所有者和所有组
		-R 如果是目录 则使其下所有子文件或目录递归生效
	
	chgrp newgroup file 改变文件的所有组

### crond任务调度 ###

	设置任务调度文件:/etc/crontab
	设置个人任务调度crontab [选项]
			 -e 编辑crontab定时任务
			 -l 列出当前任务调度
			 -r 终止任务调度
	接着输入任务到调度文件
	#例:*/1 * * * * ls -l /etc >> /tmp/to.txt
	#如果只是简单的任务，可以不用写脚本,直接在crontab中加入任务即可
	#对于比较复杂的任务,需要写脚本
	
	5个占位符说明
	第一个* 一小时当中的第几分钟 0-59
	第二个* 一天当中的第几小时	  0-23
	第三个* 一个月当中的第几天	  1-31
	第四个* 一年当中的第几月	  1-12
	第五个* 一周当中的星期几 	  0-7(0和7都代表星期日)
	
	特殊符号说明
	* 代表任何时间.比如第一个*就代表一小时中每分钟都执行一次的意思
	， 代表不连续的时间.比如0 8,12,16 * * *命令，就代表在每天的8点0分，12点0分，16点0分都执行一次命令
	-代表连续的时间范围.比如0 5 * * 1-6命令，代表在周一到周六的凌晨5点0分执行命令
	*/n 代表每隔多久执行一次.比如*/10 * * * * 命令,代表每隔10分钟就执行一遍命令
	
	特定时间执行任务案例
	45 22 * * * 在22点45分执行命令
	0 17 * * 1 每周1的17点0分执行命令
	0 5 1,15 * * 每月1号和15号的凌晨5点0分执行命令
	40 4 * * 1-5 每周一到周五的凌晨4点40分执行命令
	*/10 4 * * * 	每天的凌晨4点,每隔10分钟执行一次命令
	0 0 1,15 * 1 每月1号和15号,每周1的0点0分都会执行命令.注意：星期几和几号最好不要同时出现，因为它们定义的都是天.非常容易让管理员混乱

# Linux磁盘分区、挂载 #

	lsblk -f	查系系统的分区和挂载情况

**如何增加一块硬盘**

1. 先虚拟机添加一块硬盘
2. 分区 fdisk /dev/sdb
	
	- m 显示命令列表
	- p 显示磁盘分区同fdisk -l
	- n 新增分区
	- d 删除分区
	- w 写入并退出

	开始分区后输入n,新增分区,然后选择p,分区类型为主分区。两次回车默认剩余全部空间。最后输入w写入分区并退出，若不保存退出输入q

3. 格式化磁盘 mkfs -t ext4 /dev/sdb1 (其中ext4是分区类型)
4. 挂载:将一个分区与一个目录联系起来 mount 设备名称 挂载目录

		先创建一个/home/newdisk文件夹
		例：mount /dev/sdb1 /home/newdisk
		
		卸载 umount 设备名称 或者 挂载目录
		例: umount /dev/sdb1 或者 umount /newdisk

5. 永久挂载:通过修改/etc/fstab实现挂载 添加完成后执行mount -a即刻生效

		vim /etc/fstab
		/dev/sdb1	/home/newdisk	ext4 defaults 0 0

**磁盘情况查询**

	df -h	查询系统整体磁盘使用情况
	du -h /目录 查询指定目录的磁盘占用情况，默认为当前目录
		-s 指定目录占用大小汇总
		-h 带计量单位
		-a 含文件
		--max-depth=1 子目录深度
		-c 列出明细的同时，增加汇总值
	
	例：du -ach --max-depth=1 /opt/
	
	工作实用指令：
	
	统计/home文件夹下的文件的个数
	ls -l /home | grep "^-" | wc -l
	
	统计/home文件夹下目录的个数
	ls -l /home | grep "^d" | wc -l
	
	统计/home文件夹下文件的个数，包括子文件夹里的
	ls -lR /home | grep "^-" | wc -l
	
	统计文件夹下目录的个数,包括子文件夹里的
	ls -lR /home | grep "^d" | wc -l
	
	以树状显示目录结构

# 网络配置 #

指定固定IP:编辑vim /etc/sysconfig/network-scripts/ifcfg-eth0

	ifcfg-eth0文件说明
	DEVICE=eth0	#接口名(设备，网卡)
	HWADDR=00:0C:2x:6x:0x:xx #MAC地址
	TYPE=Ethernet #网络类型(通常是Ethemet)
	UUID=eb448abb-3012-4d8d-bcde-94434d586a31 #随机id
	#系统启动的时候网络接口是否有效(yes/no)
	ONBOOT=yes
	#IP的配置方法[none|static|bootp|dhcp](引导时不使用协议|静态分配IP|BOOTP协议|DHCP协议)
	BOOTPROTO=staic
	#IP地址
	IPADDR=192.168.184.130
	#网关
	GATEWAY=192.168.184.2
	#域名解析器
	DNS1=192.168.184.2

重启网络服务或重启系统生效

	service network restart、reboot

# 进程管理 #

	ps -a 显示当前终端的所有进程信息
	ps -u 以用户的格式显示进程信息
	ps -x 显示后台进程运行的参数
	一般使用的参数是ps -aux | grep xxx
	指令说明
	USER:用户名称
	PID:进程号
	%CPU:进程占用CPU的百分比
	%MEM:进程占用物理内存的百分比
	VSZ:进程占用的虚拟内存大小(单位KB)
	RSS:进程占用的物理内存大小(单位KB)
	TT:终端名称，缩写
	STAT:进程状态,其中S-睡眠，s-表示该进程是会话的先导进程，N-表示进程拥有比普通优先级更低的优先级,R-正在运行，D-短期等待,Z-僵死进程,T-被跟踪或者被停止等等 
	STARTED：进程的启动时间
	TIME:CPU时间，即进程使用CPU的总时间
	COMMAND:启动进程所用的命令和参数，如果过长会被截断显示
	
	ps -ef | grep xxx 查看进程的父进程
	-e显示所有进程 -f全格式
	UID:用户ID
	PID:进程ID
	PPID:父进程ID
	C:CPU用于计算执行优先级的因子。数值越大，表明进程是CPU密集型运算，执行优先级会降低；数值越小，表明进程是I/O密集型运算，执行优先级会提高
	STIME:进程启动的时间
	TTY:完整的终端名称
	TIME:CPU时间
	CMD:启动进程所用的命令和参数

**终止进程kill和killall**

	kill [选项] 进程号 通过进程号杀死进程
	killall 进程名称 通过进程名称杀死进程,也支持通配符,这在系统因负载过大而变得很慢时很有用
	
	-9：表示强迫进程立即停止

**查看进程树pstree**
	
	pstree [选项] 可以更直观的来看进程信息
			-p：显示进程的PID
			-u:显示进程的所属用户

**服务管理**

	service 服务名 [start|stop|restart|reload|status]
	在CentOS7后 不再使用service,而是systemctl
	
	service iptables stop 关闭防火墙
	talnet ip 端口 #通过talnet指令检查Linux实用指令实用指令的某个端口是否在监听,并且可以访问
	
	ls -l /etc/init.d/ 列出系统有哪些服务
	
	查看或者修改默认级别: vim /etc/inittab
	
	#通过chkconfig命令可以给每个服务的各个运行级别设置自启动/关闭
	chkconfig --list | grep xxx 查看服务
	
	例：
	#将sshd服务在运行级别5下设置为不自动启动
	chkconfig --level 5 sshd off
	
	#在所有运行级别下，关闭防火墙
	chkconfig iptables off
	
	#chkconfig重新设置服务后自启动和或关闭，需要重启机器reboot才能生效

**动态监控进程**

	top [选项]
		-d 指定top命令每隔几秒更新.默认是3秒在top命令的交互模式当中可以执行的
		-i 使top不显示任何闲置或者僵死进程
		-p 通过指定监控进程ID来仅仅监控某个进程的状态
	
		交互操作:
		P 以CPU使用率排序，默认就是此项
		M 以内存的使用率排序
		N 以PID排序
		Q 退出top
	
	netstat[选项] 查看系统网络情况(重要)
	netstat -anp
			-an 按一定顺序排列输出
			-p 显示哪个进程在调用

## systemd

### systemd简介

CentOS7已不再使用chkconfig管理启动项，而是使用systemd

Linux系统从启动到提供服务的过程是这样，先是机器加电，然后通过MBR或者UEFI加载GRUB，再启动内核，内核启动服务，然后开始对外服务。

SysV init、UpStart、systemd主要是解决服务引导管理的问题。

SysV init是最早的解决方案，依靠划分不同的运行级别，启动不同的服务集，服务依靠脚本控制，并且是顺序执行的。在CentOS5中使用，配置文件为/etc/inittab。
 SysV init方案的优点是：原理简单，易于理解；依靠shell脚本控制，编写服务脚本门槛比较低。
 缺点是：服务顺序启动，启动过程比较慢；不能做到根据需要来启动服务，比如通常希望插入U盘的时候，再启动USB控制的服务，这样可以更好的节省系统资源。

为了解决系统服务的即插即用，UpStart应运而生，在CentOS6系统中，SysV init和UpStart是并存的，UpStart主要解决了服务的即插即用。服务顺序启动慢的问题，UpStart的解决办法是把相关的服务分组，组内的服务是顺序启动，组之间是并行启动。在CentOS6系统中，配置文件为/etc/inittab和/etc/init/*.conf。

但是随着移动互联网的到来，SysV init服务启动慢的问题显得越来越突出，许多移动设备都是基于Linux内核，比如安卓。移动设备启动比较频繁，每次启动都要等待服务顺序启动，显然难以接受，systemd就是为了解决这个问题诞生的。在CentOS7中使用，其配置文件为/usr/lib/systemd/system/ 和 /etc/systemd/system/ 中的文件。
 systemd的设计思路是：尽可能的快速启动服务；尽可能的减少系统资源占用。

### systemd使用

在CentOS7中，systemctl命令主要负责控制systemd系统和服务管理器。基本取代了service和chkconfig命令，虽然service和chkconfig命令依然保留，但是据说已经被阉割过。

- `systemctl --version`，查看版本。
- `whereis systemctl`，查看位置。
- `systemctl list-unit-files`，列出所有可用单元（服务）。
- `systemctl list-units`，列出所有运行中的单元。
- `systemctl --failed`，列出所有失败的单元。
- `systemctl list-unit-files | grep enable`，查看自启动的软件。
- `systemctl is-enabled mysqld.service`，查看某个单元是否开机启动。
- `systemctl status mysqld.service`，查看某个单元的状态。
- `systemctl start mysqld.service`，启动某个单元。
- `systemctl restart mysqld.service`，重启某个单元。
- `systemctl stop mysqld.service`，停止某个单元。
- `systemctl daemon-reload`，修改了某个单元的配置文件后，重载配置文件。
- `systemctl reload mysqld.service`，重载某个单元。
- `systemctl enable mysqld.service`，设置开机自启动。
- `systemctl disable mysqld.service`，关闭开机自启动。
- `systemctl kill mysqld`，杀死单元。

# RPM和YUM #

	rpm -qa | grep xxx 查询已安装rpm列表
	rpm -qi xxx 查询软件包信息
	rpm -ql xxx 查询软件包中的文件
	rpm -qf 文件全路径名 查询文件所属的软件包
	
	rpm -e xxx 卸载rpm包
	#带上--nodeps就是强制删除(不推荐这样做，因为依赖于该软件包的程序可能无法过行)
	
	rpm -ivh xxx 安装rpm包
		i=install安装 v=verbose提示 h=hash进度条
	
	yum list | grep xx 查询yum服务器是否有需要安装的软件
	yum install xxx 下载安装


# Shell编程 #

	#脚本以#!/bin/bash开头
	#脚本需要有可执行权限
	#!/bin/bash
	echo "hello world!"
	
	#shell变量分为系统变量和用户自定义变量
	#系统变量:$HOME $PWD $SHELL $USER等等
	echo "PATH=$PAHT"
	echo "USER=$USER"
	echo "HOME=$HOME"
	#显示当前shell中所有变量:set | more
	
	#Shell变量定义
	#1. 定义变量:变量=值
	A=100
	echo "A=$A"
	#2. 撤销变量:unset变量
	unset A
	echo A=$A	#A=
	#3. 声明静态变量:readonly变量 注意：不能unset
	readonly B=200
	unset B		#unset: B: cannot unset: readonly variable
	echo B=$B
	
	#定义变量的规则
	#1. 变量名称可以由字母、数字和下划线组成，但是不能以数字开头
	#2. 等号两侧不能有空格
	#3. 变量名称一般习惯为大写 
	
	#将命令的返回值赋给变量
	#1. A=`ls -la`反引号，运行里面的命令，并把结果返回给变量A
	#2. A=$(ls -la)等价于反引号
	C=`ls -ls`
	echo C=$C
	D=$(date)
	echo D=$D

**设置环境变量**

	export 变量名=变量值 (将shell变量输出为环境变量)
	source 配置文件(让修改后的配置信息立即生效)
	echo $变量名(查询环境变量的值)
	
	#多行注释
	:<<!
	
	!

**位置参数变量**

	$n n为数字,$0代表命令本身,$1-$9代表第一到第九个参数，十以上的参数需要用大括号包含,如${10}
	$* 这个变量代表命令行中所有的参数,$*把所有的参数看成一个整体
	$@ 这个变量也代表命令行中所有的参数，不过$@把每个参数区分对待
	$# 这个变量代表命令行中所有参数的个数
	
	#!/bin/bash
	echo "$0 $1 $2"
	echo "$*"
	echo "$@"
	echo "参数个数$#"
	
	[root@iZwz9eq3yzu5iw2o05coffZ shell]# ./positionPara.sh 30 60
	./positionPara.sh 30 60
	30 60
	30 60
	参数个数2

**预定义变量**

	$$ 当前进程的进程号(PID)
	$! 后台运行的最后一个进程的进程号(PID)
	$? 最后一次执行的命令的返回状态。如果然这个变量的值为0，证明上一个命令正确执行;如果这个变量的值为非0(具体是哪个数，由命令自己来决定)，则证明上一个命令执行不正确了
	
	echo "当前的进程号=$$"
	#后台的方式运行myShell.sh
	./myShell.sh &
	echo "最后的进程号=$!"
	echo "执行的值=$?"

**运算符**

	$((运算式)) 或 $[运算式]
	expr m + n 注意expr运算符间要有空格
	expr m - n
	expr \*,/,% 乘，除，取余
	
	#!/bin/bash
	RESULT1=$(((2+3)*4))
	echo "RESULT1=$RESULT1"
	
	#推荐用这种方式
	RESULT2=$[(2+3)*4]
	echo "RESULT2=$RESULT2"
	
	TEMP=`expr 2 + 3`
	RESULT3=`expr $TEMP \* 4`
	echo "RESULT3=$RESULT3"
	
	SUM=$[$1+$2]
	echo "SUM=$SUM"

**条件判断**

	[ condition ] 	注意condition前后要有空格
	#非空返回true,可使用$?(0为true,>1为false)
	
	常用判断条件
	1)两个整数的比较
	= 字符串比较
	-lt 小于
	-le 小于等于
	-eq 等于
	-gt 大于
	-ge 大于等于
	-ne 不等于
	2)按照文件权限进行判断
	-r 有读的权限[ -r 文件 ]
	-w 有写的权限
	-x 有执行的权限
	3)按照文件类型进行判断
	-f 文件存在并且是一个常规的文件
	-e 文件存在
	-d 文件存在并是一个目录
	
	#案例1 "ok"是否等于"ok"
	if [ "ok" = "ok" ]
	then
	        echo "equal"
	fi
	
	#案例2 23是否大于等于22
	if [ 23 -ge 22 ]
	then
	        echo "大于"
	fi
	
	#案例3 /shell/aaa.txt目录中的文件是否存在	
	if [ -e /shell/aaa.txt ]
	then
	        echo "存在"
	fi

**流程控制**

**if语句**

	if[ 条件判断式 ];then
		程序
	fi
	
	if[ 条件判断式 ]
	then
		程序
	elif[ 条件判断式 ]
	then
		程序
	if
	#注意:1[ 条件判断式 ],中括号和条件判断式之间必须有空格 2 推荐使用第二种方式
	
	#案例 如果输入的参数大于等于60,则输出"及格了",如果小于60,则输出"不及格"
	#!/bin/bash
	if [ $1 -ge 60 ]
	then
	        echo "及格了"
	elif [ $1 -lt 60 ]
	then
	        echo "不及格"
	fi

**case语句**

	case $变量名 in
	"值1")
		如果变量的值等于值1，则执行程序1
	;;
	"值2")
		如果变量的值等于值2，则执行程序2
	;;
	.....
	*)
		如果变量的值都不是以上的值，则执行此程序
	;;
	esac
	
	案例 当命令行参数是1时,输出“周一”,是2时,就输出"周二",其它情况输出"other"
	#!/bin/bash
	case $1 in
	"1")
	        echo "周一"
	;;
	"2")    echo "周二"
	;;
	*)
	        echo "other"
	;;
	esac

**for循环**

	for 变量 in 值1 值2 值3
	do
		程序
	done
	
	案例 打印命令行输入的参数
	
	#!/bin/bash
	for i in "$*"
	do
	        echo "the num is $i"
	done
	
	#[root@iZwz9eq3yzu5iw2o05coffZ shell]# ./testif1.sh 10 20 30 40
	#the num is 10 20 30 40
	
	#!/bin/bash
	for j in "$@"
	do
	        echo "num is $j"
	done
	
	#[root@iZwz9eq3yzu5iw2o05coffZ shell]# ./testfor.sh 10 20 30
	#num is 10
	#num is 20
	#num is 30
	
	for((初始值;循环控制条件;变量变化))
	do
		程序
	done
	
	案例 从1加到100的值输出显示
	#!/bin/bash
	SUM=0
	for((i=1;i<=100;i++))
	do
	        SUM=$[$SUM+$i]
	done
	echo "SUM=$SUM"
	
	#SUM=5050

**while循环**

	while [ 条件判断式 ]
	do
		程序
	done
	
	案例 从命令行输入一个数n,统计从1+..+n的值是多少
	#!/bin/bash
	SUM=0
	i=0
	while [ $i -le $1 ]
	do
	        SUM=$[$SUM+$i]
	        i=$[$i+1]
	done
	echo "SUM=$SUM"
	
	#[root@iZwz9eq3yzu5iw2o05coffZ shell]# ./testwhile.sh 10
	#SUM=55    

**read读取控制台输入**

	read(选项)(参数)
	-p:指定读取值时的提示符;
	-t:指定读取值时等待的时间(秒),如果没有在指定时间内输入,就不再等待了
	变量:指定读取值的变量名
	
	案例 读取控制台输入一个num值
	#!/bin/bash
	read -p "请输入一个数字num=" num
	echo "你输入的值是num=$num"
	
	#请输入一个数字num=10
	#你输入的值是num=10
	
	案例 读取控制台输入一个NUM值，在10内输入
	#!/bin/bash
	read -t 10 -p "请输入一个数字num=" num
	echo "你输入的数字num是$num" 

# 函数 #
**系统函数**

	basename [pathname] [suffix]
	返回完整路径最后/的部分，常用于获取文件名
	
	basename [string] [suffix] basename命令会删掉所有的前缀包括最后一个('/')字符，然后将字符串显示出来
	
	选项:suffix为后缀,如果suffix被指定了，basename会将pathname或string中的suffix去掉.
	
	[root@iZwz9eq3yzu5iw2o05coffZ shell]# basename /shell/aaa.txt 
	aaa.txt
	[root@iZwz9eq3yzu5iw2o05coffZ shell]# basename /shell/aaa.txt .txt
	aaa
	
	dirname 返回完整路径最后/的前面的部分,常用于返回路径部分
	dirname 文件绝对路径 从给定的包含绝对路径的文件名中云除文件名(非目录的部分)，然后返回剩下的路径
	
	[root@iZwz9eq3yzu5iw2o05coffZ shell]# dirname /shell/aaa.txt 
	/shell

**自定义函数**
	
	[function] funname[()]
	{
		Action;
		[return int;]
	}
	调用直接写函数名:funname [值]
	
	#!/bin/bash
	function getSum()
	{
	        SUM=$[$num1+$num2]
	        echo "SUM=$SUM"
	}
	read -p "请输入第一个数" num1
	read -p "请输入第二个数" num2
	
	getSum $num1 $num2
	
	#[root@iZwz9eq3yzu5iw2o05coffZ shell]# ./testfun.sh 
	#请输入第一个数10
	#请输入第二个数20
	#SUM=3

Shell编程综合案例

1）每天凌晨2：10备份数据库atguiguDB到/data/backup/db

2)备份开始和备份结束能够给出相应的提示信息

3)备份后的文件要求以备份时间文件名，并打包成.tar.gz的形式，比如:2018-03-12_230201.tar.gz

4在备份的同时，检查是否有10天前备份的数据库文件，如果有就将其删除

	#!/bin/bash
	
	BACKUP=/bakcup/
	DATETIME=$(date "+%Y-%m-%d_%H%M%S")
	
	echo "=============开始备份=============="
	echo "=============备份的路径是 $BACKUP$DATETIME.tar.gz"
	
	#主机
	HOST="120.77.237.175"
	#端口
	PORT="9306"
	#用户名
	DB_USER="root"
	#密码
	DB_PWD="#edc@wsx123"
	#备份数据库名
	DATABASE="sys"
	#创建备份的路径
	#如果备份的路径文件夹存在，就使用，否则就创建
	if [ ! -d "$BACKUP$DATETIME" ]
	then
		mkdir -p "$BACKUP$DATETIME"
	fi
	#执行mysql的备份数据库指令
	mysqldump -u${DB_USER} -p${DB_PWD} -h${HOST} -P${PORT} $DATABASE | gzip > $BACKUP$DATETIME/$DATETIME.tar.gz
	#打包备份文件
	cd #BACKUP
	tar -zcvf $DATETIME.tar.gz $DATETIME
	#删除临时目录
	rm -rf $BACKUP$DATETIME
	
	#删除10天前的备份文件
	find $BACKUP -mtime +10 -name "*.tar.gz" -exec rm -rf {} \;
	echo "========备份文件成功=========="
	
	#设置定时任务
	crontab -e
	
	10 2 * * * /usr/sbin/mysql_db_backup.sh
