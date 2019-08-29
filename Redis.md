# Reids #

**在分布式数据库中CAP原理CAP+BASE**

- C:Consistency（强一致性）
- A:Availability（高可用性）
- P:Partition tolerance（分区容错性）

 CAP理论的核心是：一个分布式系统不可能同时很好的满足一致性，可用性和分区容错性这三个需求，

最多只能同时较好的满足两个。

因此，根据 CAP 原理将 NoSQL 数据库分成了满足 CA 原则、满足 CP 原则和满足 AP 原则三 大类：

- CA - 单点集群，满足一致性，可用性的系统，通常在可扩展性上不太强大。
- CP - 满足一致性，分区容忍必的系统，通常性能不是特别高。
- AP - 满足可用性，分区容忍性的系统，通常可能对一致性要求低一些。

**分区容错性是我们必须需要实现的**。

BASE就是为了解决关系数据库强一致性引起的问题而引起的可用性降低而提出的解决方案。
 
BASE其实是下面三个术语的缩写：

-  基本可用（Basically Available）
-  软状态（Soft state）
-  最终一致（Eventually consistent）
    
它的思想是通过让系统放松对某一时刻数据一致性的要求来换取系统整体伸缩性和性能上改观。为什么这么说呢，缘由就在于大型系统往往由于地域分布和极高性能的要求，不可能采用分布式事务来完成这些指标，要想获得这些指标，我们必须采用另外一种方式来完成，这里BASE就是解决这个问题的办法

**Redis:REmote DIctionary Server(远程字典服务器)**
    
**Redis启动后杂项基础知识讲解**

- 默认16个数据库，类似数组下表从零开始，初始默认使用零号库

设置数据库的数量，默认数据库为0，可以使用SELECT <dbid>命令在连接上指定数据库id

  databases 16

- Select命令切换数据库
- Dbsize查看当前数据库的key的数量
- Flushdb：清空当前库
- Flushall；通杀全部库
- 统一密码管理，16个库都是同样密码，要么都OK要么一个也连接不上
- Redis索引都是从零开始

# Redis数据类型 #

**Redis的五大数据类型**

- **String（字符串）**

string是redis最基本的类型，你可以理解成与Memcached一模一样的类型，一个key对应一个value。
 
string类型是二进制安全的。意思是redis的string可以包含任何数据。比如jpg图片或者序列化的对象 。
 
string类型是Redis最基本的数据类型，一个redis中字符串value最多可以是512M

- **Hash（哈希，类似java里的Map）**

Redis hash 是一个键值对集合。

Redis hash是一个string类型的field和value的映射表，hash特别适合用于存储对象。
 
类似Java里面的Map<String,Object>

- **List（列表）**

Redis 列表是简单的字符串列表，按照插入顺序排序。你可以添加一个元素导列表的头部（左边）或者尾部（右边）。

它的底层实际是个链表

- **Set（集合）**

Redis的Set是string类型的无序集合。它是通过HashTable实现实现的

- **Zset(sorted set：有序集合)**

Redis zset 和 set 一样也是string类型元素的集合,且不允许重复的成员。

不同的是每个元素都会关联一个double类型的分数。

redis正是通过分数来为集合中的成员进行从小到大的排序。
zset的成员是唯一的,但分数(score)却可以重复。

**redis常见数据类型操作命令:http://redisdoc.com/**

**Redis键(key)**

- keys *
- exists key的名字，判断某个key是否存在
- move key db   --->当前库就没有了，被移除了
- expire key 秒钟：为给定的key设置过期时间
- ttl key 查看还有多少秒过期，-1表示永不过期，-2表示已过期(过期就代表消失了)
- type key 查看你的key是什么类型

		127.0.0.1:9379> set s1 v1		##设置key为s1,value v1
		OK
		127.0.0.1:9379> get s1			##获取key为s1的值
		"v1"
		127.0.0.1:9379> exists s1		##key为s1的值是否存在(注意成功为1,失败为0)
		(integer) 1
		127.0.0.1:9379> select 1		##选择健值为1的库(默认一共有16个库)
		OK
		127.0.0.1:9379[1]> get s1		##获取key为s1的值(这里因为是库值为1的库，没有此key,固返回null,库与库之间的数据是不互通的)
		(nil)
		127.0.0.1:9379[1]> select 0		##选择健值为0的库
		OK
		127.0.0.1:9379> get s1			##获取key为s1的值(成功返回值)
		"v1"
		127.0.0.1:9379> type s1			##查看key为s1的的类型，返回String类型
		string
		127.0.0.1:9379> expire s1 10	##给key为s1设置10秒后过期
		(integer) 1
		127.0.0.1:9379> get s1
		"v1"
		127.0.0.1:9379> ttl s1			##查看key为s1的过期时间
		(integer) 4
		127.0.0.1:9379> ttl s1
		(integer) 1
		127.0.0.1:9379> ttl s1
		(integer) -2
		127.0.0.1:9379> get s1
		(nil)

**Redis字符串(String)**

- **set/get/del/append/strlen**

		127.0.0.1:9379> set k1 v1
		OK
		127.0.0.1:9379> get k1
		"v1"
		127.0.0.1:9379> append k1 111
		(integer) 5
		127.0.0.1:9379> get k1
		"v1111"
		127.0.0.1:9379> strlen k1		##获取key为k1的长度
		(integer) 5
		127.0.0.1:9379> set k1 v1		##key为k1的值是可以被覆盖的
		OK
		127.0.0.1:9379> get k1
		"v1"

- **Incr/decr/incrby/decrby,一定要是数字才能进行加减**

		127.0.0.1:9379> set k2 1		##设置key为k2的值为1
		OK
		127.0.0.1:9379> Incr k2			##k2累加
		(integer) 2
		127.0.0.1:9379> get k2			
		"2"
		127.0.0.1:9379> decr k2			##k2递减
		(integer) 1
		127.0.0.1:9379> get k2
		"1"
		127.0.0.1:9379> incrby k2 5		##k2累加5
		(integer) 6
		127.0.0.1:9379> get k2
		"6"
		127.0.0.1:9379> decrby k2 1		##k2递减1
		(integer) 5
		127.0.0.1:9379> get k2
		"5"
		127.0.0.1:9379> incr k1 4		##当不为数字时报error
		(error) ERR wrong number of arguments for 'incr' command


- **getrange/setrange**

getrange:获取指定区间范围内的值，类似between......and的关系
从零到负一表示全部

setrange设置指定区间范围内的值，格式是setrange key值 具体值

	127.0.0.1:9379> set k1 v1
	OK
	127.0.0.1:9379> get k1
	"v1"
	127.0.0.1:9379> append k1 12345678
	(integer) 10
	127.0.0.1:9379> getrange k1 1 3
	"112"
	127.0.0.1:9379> setrange k1 1 123456789
	(integer) 10
	127.0.0.1:9379> get k1
	"v123456789"


- **setex(set with expire)/setnx(set if not exist)**

setex:设置带过期时间的key，动态设置。

setnx:只有在 key 不存在时设置 key 的值。

	127.0.0.1:9379> setex k1 5 v1		##setex 键 秒值 真实值
	OK
	127.0.0.1:9379> get k1
	"v1"
	127.0.0.1:9379> ttl k1
	(integer) -2
	127.0.0.1:9379> get k1
	(nil)
	127.0.0.1:9379> setnx k1 v1
	(integer) 1
	127.0.0.1:9379> setnx k1 v2
	(integer) 0

- **mset/mget/msetnx(如果有一个失败，全部都设置失败)**

mset:同时设置一个或多个 key-value 对。

mget:获取所有(一个或多个)给定 key 的值。

	127.0.0.1:9379> mset k1 v1 k2 v2
	OK
	127.0.0.1:9379> mget k1 k2
	1) "v1"
	2) "v2"
	127.0.0.1:9379> msetnx k2 v2 k3 v3
	(integer) 0

- **getset(先get再set)**

getset:将给定 key 的值设为 value ，并返回 key 的旧值(old value)。

简单一句话，先get然后立即set

	127.0.0.1:9379> set k2 v2
	OK
	127.0.0.1:9379> getset k2 v22
	"v2"
	127.0.0.1:9379> get k2
	"v22"

**Redis列表(List)**

- **lpush/rpush/lrange**

		127.0.0.1:9379> lpush list01 1 2 3 4 5 6
		(integer) 6
		127.0.0.1:9379> rpush list02 1 2 3 4 5 6
		(integer) 6
		127.0.0.1:9379> lrange list01 0 -1
		1) "6"
		2) "5"
		3) "4"
		4) "3"
		5) "2"
		6) "1"
		127.0.0.1:9379> lrange list02 0 -1
		1) "1"
		2) "2"
		3) "3"
		4) "4"
		5) "5"
		6) "6"

- **lpop/rpop**

		127.0.0.1:9379> lpop list01
		"6"
		127.0.0.1:9379> rpop list02
		"6"
		127.0.0.1:9379> lrange list 0 -1
		(empty list or set)
		127.0.0.1:9379> lrange list01 0 -1
		1) "5"
		2) "4"
		3) "3"
		4) "2"
		5) "1"
		127.0.0.1:9379> lrange list02 0 -1
		1) "1"
		2) "2"
		3) "3"
		4) "4"
		5) "5"

- **lindex，按照索引下标获得元素(从上到下)**

		127.0.0.1:9379> lindex list02 0
		"1"
		127.0.0.1:9379> lindex list01 0
		"5"

- **llen**

		127.0.0.1:9379> llen list01
		(integer) 5

- **lrem key 删N个value**

		127.0.0.1:9379> lpush list03 1 1 1 2 2 2 3 3 3 4 4 4
		(integer) 12
		127.0.0.1:9379> lrem list03 2 3
		(integer) 2
		127.0.0.1:9379> lrange list03 0 -1
		 1) "4"
		 2) "4"
		 3) "4"
		 4) "3"
		 5) "2"
		 6) "2"
		 7) "2"
		 8) "1"
		 9) "1"
		10) "1"

- **ltrim key 开始index 结束index，截取指定范围的值后再赋值给key**

		127.0.0.1:9379> lrange list01 0 -1
		1) "5"
		2) "4"
		3) "3"
		4) "2"
		5) "1"
		127.0.0.1:9379> ltrim list01 1 2
		OK
		127.0.0.1:9379> lrange list01 0 -1
		1) "4"
		2) "3"
	
- **rpoplpush 源列表 目的列表**

		127.0.0.1:9379> lrange list02 0 -1
		1) "1"
		2) "2"
		3) "3"
		4) "4"
		5) "5"
		127.0.0.1:9379> rpoplpush list01 list02
		"3"
		127.0.0.1:9379> lrange list02 0 -1
		1) "3"
		2) "1"
		3) "2"
		4) "3"
		5) "4"
		6) "5"

- **lset key index value**

		127.0.0.1:9379> lset list02 0 0
		OK
		127.0.0.1:9379> lrange list02 0 -1
		1) "0"
		2) "1"
		3) "2"
		4) "3"
		5) "4"
		6) "5"

- **linsert key  before/after 值1 值2**

		127.0.0.1:9379> linsert list02 before 2 1.5
		(integer) 7
		127.0.0.1:9379> linsert list02 after 2 2.5
		(integer) 8
		127.0.0.1:9379> lrange list02 0 -1
		1) "0"
		2) "1"
		3) "1.5"
		4) "2"
		5) "2.5"
		6) "3"
		7) "4"
		8) "5"

**总结:**

- 它是一个字符串链表，left、right都可以插入添加；
- 如果键不存在，创建新的链表；
- 如果键已存在，覆盖；
- 如果值全移除，对应的键也就消失了。
- 链表的操作无论是头和尾效率都极高，但假如是对中间元素进行操作，效率就很惨淡了。

**Redis集合(Set)**

- **sadd/smembers/sismember**

		127.0.0.1:9379>  sadd set01 0 1 2 3 4 5		##添加新元素
		(integer) 6
		127.0.0.1:9379> smembers set01				##获取全部元素
		1) "0"
		2) "1"
		3) "2"
		4) "3"
		5) "4"
		6) "5"
		127.0.0.1:9379> sadd set01 5 6				##如果元素已经存在，只会添加不存的值进去，不会报错
		(integer) 1
		127.0.0.1:9379> smembers set01
		1) "0"
		2) "1"
		3) "2"
		4) "3"
		5) "4"
		6) "5"
		7) "6"
		127.0.0.1:9379> sismember set01 0			##判断该元素是否在set里，存在返回1，不存在返回0
		(integer) 1
		127.0.0.1:9379> sismember set01 9
		(integer) 0

- **scard，获取集合里面的元素个数**

		127.0.0.1:9379> scard set01
		(integer) 7

- **srem key value 删除集合中元素**

		127.0.0.1:9379> srem set01 0 1
		(integer) 2
		127.0.0.1:9379> smembers set01
		1) "2"
		2) "3"
		3) "4"
		4) "5"
		5) "6"

- **srandmember key 某个整数**

		127.0.0.1:9379> srandmember set01 2		##从set集合里面随机取出2个，如果超过最大数量就全部取出
		1) "5"
		2) "6"
		127.0.0.1:9379> smembers set01
		1) "2"
		2) "3"
		3) "4"
		4) "5"
		5) "6"
		127.0.0.1:9379> srandmember set01 -7	##如果写的值是负数，比如-7 ，表示需要取出7个，但是可能会有重复值
		1) "5"
		2) "5"
		3) "4"
		4) "5"
		5) "2"
		6) "2"
		7) "2"

- **spop key 随机出栈**

		127.0.0.1:9379> sadd set01 0 1 2 3 4 5 6 7
		(integer) 8
		127.0.0.1:9379> spop set01
		"4"
		127.0.0.1:9379> spop set01 2
		1) "6"
		2) "7"

- **smove key1 key2 在key1里某个值，将key1里的某个值赋给key2**

		127.0.0.1:9379> smembers set01
		1) "0"
		2) "1"
		3) "2"
		4) "3"
		5) "4"
		6) "5"
		127.0.0.1:9379> sadd set02 x y z
		(integer) 3
		127.0.0.1:9379> smove set01 set02 0
		(integer) 1
		127.0.0.1:9379> smembers set02
		1) "0"
		2) "y"
		3) "x"
		4) "z"

- **数学集合类**

	- **差集：sdiff**

			127.0.0.1:9379> sadd set01 0 1 2 3
			(integer) 4
			127.0.0.1:9379> sadd set02 a b 2 c
			(integer) 4
			127.0.0.1:9379> sdiff set01 set02
			1) "0"
			2) "1"
			3) "3"
			127.0.0.1:9379> sdiff set02 set01
			1) "b"
			2) "a"
			3) "c"

	- **交集：sinter**

			127.0.0.1:9379> sinter set01 set02
			1) "2"

	- **并集：sunion**

			127.0.0.1:9379> sunion set01 set02
			1) "3"
			2) "b"
			3) "0"
			4) "1"
			5) "2"
			6) "a"
			7) "c"

# Redis哈希(Hash) #

- **hset/hget/hmset/hmget/hgetall/hdel(重点)**

		127.0.0.1:9379> hset customer id 1
		(integer) 1
		127.0.0.1:9379> hget customer id
		"1"
		127.0.0.1:9379> hmset customer id 2 name tom age 13
		OK
		127.0.0.1:9379> hgetall customer
		1) "id"
		2) "2"
		3) "name"
		4) "tom"
		5) "age"
		6) "13"
		127.0.0.1:9379> hdel customer age
		(integer) 1
		127.0.0.1:9379> hgetall customer
		1) "id"
		2) "2"
		3) "name"
		4) "tom"
		127.0.0.1:9379> hget customer age
		(nil)

- **hlen**

		127.0.0.1:9379> hlen customer
		(integer) 2

- **hexists**

		127.0.0.1:9379> hexists customer name
		(integer) 1

- **hkeys/hvals获取所有的key和val值(重点)**

		127.0.0.1:9379> hkeys customer
		1) "id"
		2) "name"
		3) "age"
		127.0.0.1:9379> hvals customer
		1) "2"
		2) "tom"
		3) "null"

- **hincrby/hincrbyfloat**
	
		127.0.0.1:9379> hset customer score 60
		(integer) 1
		127.0.0.1:9379> hincrby customer score 1
		(integer) 61
		127.0.0.1:9379> hincrbyfloat customer score 0.5
		"61.5"
		
- **hsetnx**

		127.0.0.1:9379> hsetnx customer id 1		##key存在，赋值失败,返回0
		(integer) 0
		127.0.0.1:9379> hsetnx customer email test@test.com		##key不存在，赋值成功,返回1
		(integer) 1
		127.0.0.1:9379> hgetall customer
		 1) "id"
		 2) "2"
		 3) "name"
		 4) "tom"
		 5) "age"
		 6) "null"
		 7) "score"
		 8) "60.5"
		 9) "email"
		10) "test@test.com"

**Redis有序集合Zset(sorted set)**

在set基础上，加一个score值。之前set是k1 v1 v2 v3，现在zset是k1 score1 v1 score2 v2

- **zadd/zrange**

		127.0.0.1:9379> zadd zset01 60 v1 70 v2 80 v3 90 v4 100 v5
		(integer) 5
		127.0.0.1:9379> zrange zset01 0 -1
		1) "v1"
		2) "v2"
		3) "v3"
		4) "v4"
		5) "v5"
		127.0.0.1:9379> zrange zset01 0 -1 withscores		##withscores 带分数key显示
		 1) "v1"
		 2) "60"
		 3) "v2"
		 4) "70"
		 5) "v3"
		 6) "80"
		 7) "v4"
		 8) "90"
		 9) "v5"
		10) "100"


- **zrangebyscore key 开始score 结束score**
	
		127.0.0.1:9379> zrangebyscore zset01 60 70			##获取分数60-70区间的值
		1) "v1"
		2) "v2"
		127.0.0.1:9379> zrangebyscore zset01 60 70 withscores	##获取分数60-70区间的带分数的值
		1) "v1"
		2) "60"
		3) "v2"
		4) "70"
		127.0.0.1:9379> zrangebyscore zset01 60 (90		##(不包含,获取大于等于60小于90区间的值
		1) "v1"
		2) "v2"
		3) "v3"
		127.0.0.1:9379> zrangebyscore zset01 (70 (90	##(不包含,获取大于70小于90区间的值
		1) "v3"
		127.0.0.1:9379> zrangebyscore zset01 70 (90 withscores limit 0 2		##获取大于等于70小于90区间的值limit从下标0开始获取2个
		1) "v2"
		2) "70"
		3) "v3"
		4) "80"

- **zrem key 某score下对应的value值**

		127.0.0.1:9379> zrem zset01 v5		##删除元素，格式是zrem zset的key项的值，项的值可以是多个
		(integer) 1

- **zcard/zcount key score区间/zscore key 某分数对应值**

		127.0.0.1:9379> zcard zset01		##获取元素个数
		(integer) 4
		127.0.0.1:9379> zcount zset01 60 80		##获取分数区间的元素个数
		(integer) 3

		127.0.0.1:9379> zscore zset01 v2		##获取元素的分数值
		"70"

- **zrevrank/zrank**

		127.0.0.1:9379> zrank zset01 v1			##正序获取元素的下标值
		(integer) 0
		127.0.0.1:9379> zrevrank zset01 v1		##反序获取元素的下标值
		(integer) 3
		127.0.0.1:9379> zrange zset01 0 -1
		1) "v1"
		2) "v2"
		3) "v3"
		4) "v4"

- **zrevrange**

		127.0.0.1:9379> zrange zset01 0 -1			##正序获取区间值
		1) "v1"
		2) "v2"
		3) "v3"
		4) "v4"
		127.0.0.1:9379> zrevrange zset01 0 -1		##倒序获取区间值
		1) "v4"
		2) "v3"
		3) "v2"
		4) "v1"

- **zrevrangebyscore  key 结束score 开始score**

		127.0.0.1:9379> zrevrangebyscore zset01 90 60 withscores limit 1 2	##倒序获取带分数的区间值
		1) "v3"
		2) "80"
		3) "v2"
		4) "70"

# 解析配置文件redis.conf #

redis.conf 配置项说明如下：

	1. Redis默认不是以守护进程的方式运行，可以通过该配置项修改，使用yes启用守护进程
	  daemonize no
	2. 当Redis以守护进程方式运行时，Redis默认会把pid写入/var/run/redis.pid文件，可以通过pidfile指定
	  pidfile /var/run/redis.pid
	3. 指定Redis监听端口，默认端口为6379，作者在自己的一篇博文中解释了为什么选用6379作为默认端口，因为6379在手机按键上MERZ对应的号码，而MERZ取自意大利歌女Alessia Merz的名字
	  port 6379
	4. 绑定的主机地址
	  bind 127.0.0.1
	5.当 客户端闲置多长时间后关闭连接，如果指定为0，表示关闭该功能
	  timeout 300
	6. 指定日志记录级别，Redis总共支持四个级别：debug、verbose、notice、warning，默认为verbose
	  loglevel verbose
	7. 日志记录方式，默认为标准输出，如果配置Redis为守护进程方式运行，而这里又配置为日志记录方式为标准输出，则日志将会发送给/dev/null
	  logfile stdout
	8. 设置数据库的数量，默认数据库为0，可以使用SELECT <dbid>命令在连接上指定数据库id
	  databases 16
	9. 指定在多长时间内，有多少次更新操作，就将数据同步到数据文件，可以多个条件配合
	  save <seconds> <changes>
	  Redis默认配置文件中提供了三个条件：
	  save 900 1
	  save 300 10
	  save 60 10000
	  分别表示900秒（15分钟）内有1个更改，300秒（5分钟）内有10个更改以及60秒内有10000个更改。
	 
	10. 指定存储至本地数据库时是否压缩数据，默认为yes，Redis采用LZF压缩，如果为了节省CPU时间，可以关闭该选项，但会导致数据库文件变的巨大
	  rdbcompression yes
	11. 指定本地数据库文件名，默认值为dump.rdb
	  dbfilename dump.rdb
	12. 指定本地数据库存放目录
	  dir ./
	13. 设置当本机为slav服务时，设置master服务的IP地址及端口，在Redis启动时，它会自动从master进行数据同步
	  slaveof <masterip> <masterport>
	14. 当master服务设置了密码保护时，slav服务连接master的密码
	  masterauth <master-password>
	15. 设置Redis连接密码，如果配置了连接密码，客户端在连接Redis时需要通过AUTH <password>命令提供密码，默认关闭
	  requirepass foobared
	16. 设置同一时间最大客户端连接数，默认无限制，Redis可以同时打开的客户端连接数为Redis进程可以打开的最大文件描述符数，如果设置 maxclients 0，表示不作限制。当客户端连接数到达限制时，Redis会关闭新的连接并向客户端返回max number of clients reached错误信息
	  maxclients 128
	17. 指定Redis最大内存限制，Redis在启动时会把数据加载到内存中，达到最大内存后，Redis会先尝试清除已到期或即将到期的Key，当此方法处理 后，仍然到达最大内存设置，将无法再进行写入操作，但仍然可以进行读取操作。Redis新的vm机制，会把Key存放内存，Value会存放在swap区
	  maxmemory <bytes>
	18. 指定是否在每次更新操作后进行日Maxclients志记录，Redis在默认情况下是异步的把数据写入磁盘，如果不开启，可能会在断电时导致一段时间内的数据丢失。因为 redis本身同步数据文件是按上面save条件来同步的，所以有的数据会在一段时间内只存在于内存中。默认为no
	  appendonly no
	19. 指定更新日志文件名，默认为appendonly.aof
	   appendfilename appendonly.aof
	20. 指定更新日志条件，共有3个可选值： 
	  no：表示等操作系统进行数据缓存同步到磁盘（快） 
	  always：表示每次更新操作后手动调用fsync()将数据写到磁盘（慢，安全） 
	  everysec：表示每秒同步一次（折衷，默认值）
	  appendfsync everysec
	 
	21. 指定是否启用虚拟内存机制，默认值为no，简单的介绍一下，VM机制将数据分页存放，由Redis将访问量较少的页即冷数据swap到磁盘上，访问多的页面由磁盘自动换出到内存中（在后面的文章我会仔细分析Redis的VM机制）
	   vm-enabled no
	22. 虚拟内存文件路径，默认值为/tmp/redis.swap，不可多个Redis实例共享
	   vm-swap-file /tmp/redis.swap
	23. 将所有大于vm-max-memory的数据存入虚拟内存,无论vm-max-memory设置多小,所有索引数据都是内存存储的(Redis的索引数据 就是keys),也就是说,当vm-max-memory设置为0的时候,其实是所有value都存在于磁盘。默认值为0
	   vm-max-memory 0
	24. Redis swap文件分成了很多的page，一个对象可以保存在多个page上面，但一个page上不能被多个对象共享，vm-page-size是要根据存储的 数据大小来设定的，作者建议如果存储很多小对象，page大小最好设置为32或者64bytes；如果存储很大大对象，则可以使用更大的page，如果不 确定，就使用默认值
	   vm-page-size 32
	25. 设置swap文件中的page数量，由于页表（一种表示页面空闲或使用的bitmap）是在放在内存中的，，在磁盘上每8个pages将消耗1byte的内存。
	   vm-pages 134217728
	26. 设置访问swap文件的线程数,最好不要超过机器的核数,如果设置为0,那么所有对swap文件的操作都是串行的，可能会造成比较长时间的延迟。默认值为4
	   vm-max-threads 4
	27. 设置在向客户端应答时，是否把较小的包合并为一个包发送，默认为开启
	  glueoutputbuf yes
	28. 指定在超过一定的数量或者最大的元素超过某一临界值时，采用一种特殊的哈希算法
	  hash-max-zipmap-entries 64
	  hash-max-zipmap-value 512
	29. 指定是否激活重置哈希，默认为开启（后面在介绍Redis的哈希算法时具体介绍）
	  activerehashing yes
	30. 指定包含其它的配置文件，可以在同一主机上多个Redis实例之间使用同一份配置文件，而同时各个实例又拥有自己的特定配置文件
	  include /path/to/local.conf
	31. Maxclients 设置redis同时可以与多少个客户端进行连接。默认情况下为10000个客户端
	32. Maxmemory-policy
		1.  volatile-lru：使用LRU算法移除key，只对设置了过期时间的键
		2.  allkeys-lru：使用LRU算法移除key
		3.  volatile-random：在过期集合中移除随机的key，只对设置了过期时间的键
		4.  allkeys-random：移除随机的key
		5.  volatile-ttl：移除那些TTL值最小的key，即那些最近要过期的key
		6.  noeviction：不进行移除。针对写操作，只是返回错误信息
	33. Maxmemory-samples  设置样本数量，LRU算法和最小TTL算法都并非是精确的算法，而是估算值，所以你可以设置样本的大小，redis默认会检查这么多个key并选择其中LRU的那个
	34. Syslog-enabled  是否把日志输出到syslog中
	35. Syslog-ident  指定syslog里的日志标志
	36. Syslog-facility 指定syslog设备，值可以是USER或LOCAL0-LOCAL7
	37. Tcp-keepalive 单位为秒，如果设置为0，则不会进行Keepalive检测，建议设置成60 
	38. Tcp-backlog 设置tcp的backlog，backlog其实是一个连接队列，backlog队列总和=未完成三次握手队列 + 已经完成三次握手队列。在高并发环境下你需要一个高backlog值来避免慢客户端连接问题。注意Linux内核会将这个值减小到/proc/sys/net/core/somaxconn的值，所以需要确认增大somaxconn和tcp_max_syn_backlog两个值来达到想要的效果

	##SECURITY安全 访问密码的查看、设置和取消
	127.0.0.1:9379> config get requirepass
	1) "requirepass"
	2) ""
	127.0.0.1:9379> config set requirepass "123456"
	OK
	127.0.0.1:9379> config get requirepass
	(error) NOAUTH Authentication required.
	127.0.0.1:9379> auth 123456
	OK
	127.0.0.1:9379> config get requirepass
	1) "requirepass"
	2) "123456"
	127.0.0.1:9379> config set requirepass ""
	OK
	127.0.0.1:9379> ping
	PONG


# Redis的持久化 #


