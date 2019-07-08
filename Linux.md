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
