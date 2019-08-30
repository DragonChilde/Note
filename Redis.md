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

**RDB（Redis DataBase）**

在指定的时间间隔内将内存中的数据集快照写入磁盘，也就是行话讲的Snapshot快照，它恢复时是将快照文件直接读到内存里

Redis会单独创建（fork）一个子进程来进行持久化，会先将数据写入到一个临时文件中，待持久化过程都结束了，再用这个临时文件替换上次持久化好的文件。整个过程中，主进程是不进行任何IO操作的，这就确保了极高的性能如果需要进行大规模数据的恢复，且对于数据恢复的完整性不是非常敏感，那RDB方式要比AOF方式更加的高效。RDB的缺点是最后一次持久化后的数据可能丢失。

**Fork**:Fork的作用是复制一个与当前进程一样的进程。新进程的所有数据（变量、环境变量、程序计数器等）数值都和原进程一致，但是是一个全新的进程，并作为原进程的子进程

**Rdb保存的是dump.rdb文件**

**配置位置**

		################################ SNAPSHOTTING  ################################
	#
	# Save the DB on disk:
	#
	#   save <seconds> <changes>
	#
	#   Will save the DB if both the given number of seconds and the given
	#   number of write operations against the DB occurred.
	#
	#   In the example below the behaviour will be to save:
	#   after 900 sec (15 min) if at least 1 key changed
	#   after 300 sec (5 min) if at least 10 keys changed
	#   after 60 sec if at least 10000 keys changed
	#
	#   Note: you can disable saving completely by commenting out all "save" lines.
	#
	#   It is also possible to remove all the previously configured save
	#   points by adding a save directive with a single empty string argument
	#   like in the following example:
	#
	#   save ""
	##如果想禁用RDB持久化的策略，只要不设置任何save指令，或者给save传入一个空字符串参数也可以

	save 900 1
	save 300 10
	save 60 10000

	## save 秒钟 写操作次数
	##RDB是整个内存的压缩过的Snapshot，RDB的数据结构，可以配置复合的快照触发条件，
	##默认
	##是1分钟内改了1万次，
	##或5分钟内改了10次，
	##或15分钟内改了1次。
	
	# By default Redis will stop accepting writes if RDB snapshots are enabled
	# (at least one save point) and the latest background save failed.
	# This will make the user aware (in a hard way) that data is not persisting
	# on disk properly, otherwise chances are that no one will notice and some
	# disaster will happen.
	#
	# If the background saving process will start working again Redis will
	# automatically allow writes again.
	#
	# However if you have setup your proper monitoring of the Redis server
	# and persistence, you may want to disable this feature so that Redis will
	# continue to work as usual even if there are problems with disk,
	# permissions, and so forth.
	stop-writes-on-bgsave-error yes
	##如果配置成no，表示你不在乎数据不一致或者有其他的手段发现和控制
	
	# Compress string objects using LZF when dump .rdb databases?
	# For default that's set to 'yes' as it's almost always a win.
	# If you want to save some CPU in the saving child set it to 'no' but
	# the dataset will likely be bigger if you have compressible values or keys.
	rdbcompression yes
	##rdbcompression：对于存储到磁盘中的快照，可以设置是否进行压缩存储。如果是的话，redis会采用LZF算法进行压缩。如果你不想消耗CPU来进行压缩的话，可以设置为关闭此功能
	
	# Since version 5 of RDB a CRC64 checksum is placed at the end of the file.
	# This makes the format more resistant to corruption but there is a performance
	# hit to pay (around 10%) when saving and loading RDB files, so you can disable it
	# for maximum performances.
	#
	# RDB files created with checksum disabled have a checksum of zero that will
	# tell the loading code to skip the check.
	rdbchecksum yes
	##rdbchecksum：在存储快照后，还可以让redis使用CRC64算法来进行数据校验，但是这样做会增加大约10%的性能消耗，如果希望获取到最大的性能提升，可以关闭此功能

	
	# The filename where to dump the DB
	dbfilename dump.rdb
	##保存的文件名
	
	# The working directory.
	#
	# The DB will be written inside this directory, with the filename specified
	# above using the 'dbfilename' configuration directive.
	#
	# The Append Only File will also be created inside this directory.
	#
	# Note that you must specify a directory here, not a file name.
	dir ./
	##保存的路径

**如何触发RDB快照**

- 配置文件中默认的快照配置
- 命令save或者是bgsave
	- Save：save时只管保存，其它不管，全部阻塞
	- BGSAVE：Redis会在后台异步进行快照操作，快照同时还可以响应客户端请求。可以通过lastsave命令获取最后一次成功执行快照的时间
- 执行flushall命令，也会产生dump.rdb文件，但里面是空的，无意义

**如何恢复**

- 将备份文件 (dump.rdb) 移动到 redis 安装目录并启动服务即可
- CONFIG GET dir获取目录

**优势**

- 适合大规模的数据恢复
- 对数据完整性和一致性要求不高

**劣势**

- 在一定间隔时间做一次备份，所以如果redis意外down掉的话，就会丢失最后一次快照后的所有修改
- Fork的时候，内存中的数据被克隆了一份，大致2倍的膨胀性需要考虑

**如何停止** 动态所有停止RDB保存规则的方法：redis-cli config set save ""

![](https://images2015.cnblogs.com/blog/1015818/201611/1015818-20161101224544721-403073878.png)

**AOF（Append Only File）**

以日志的形式来记录每个写操作，将Redis执行过的所有写指令记录下来(读操作不记录)，只许追加文件但不可以改写文件，redis启动之初会读取该文件重新构建数据，换言之，redis重启的话就根据日志文件的内容将写指令从前到后执行一次以完成数据的恢复工作

**Aof保存的是appendonly.aof文件**

**配置位置**

		############################## APPEND ONLY MODE ###############################	
		# By default Redis asynchronously dumps the dataset on disk. This mode is
		# good enough in many applications, but an issue with the Redis process or
		# a power outage may result into a few minutes of writes lost (depending on
		# the configured save points).
		#
		# The Append Only File is an alternative persistence mode that provides
		# much better durability. For instance using the default data fsync policy
		# (see later in the config file) Redis can lose just one second of writes in a
		# dramatic event like a server power outage, or a single write if something
		# wrong with the Redis process itself happens, but the operating system is
		# still running correctly.
		#
		# AOF and RDB persistence can be enabled at the same time without problems.
		# If the AOF is enabled on startup Redis will load the AOF, that is the file
		# with the better durability guarantees.
		#
		# Please check http://redis.io/topics/persistence for more information.
		
		appendonly no
		##开启AOF备份,默认是no
		
		# The name of the append only file (default: "appendonly.aof")
		
		appendfilename "appendonly.aof"
		##AOF备份文件名
		
		# The fsync() call tells the Operating System to actually write data on disk
		# instead of waiting for more data in the output buffer. Some OS will really flush
		# data on disk, some other OS will just try to do it ASAP.
		#
		# Redis supports three different modes:
		#
		# no: don't fsync, just let the OS flush the data when it wants. Faster.
		# always: fsync after every write to the append only log. Slow, Safest.
		# everysec: fsync only one time every second. Compromise.
		#
		# The default is "everysec", as that's usually the right compromise between
		# speed and data safety. It's up to you to understand if you can relax this to
		# "no" that will let the operating system flush the output buffer when
		# it wants, for better performances (but if you can live with the idea of
		# some data loss consider the default persistence mode that's snapshotting),
		# or on the contrary, use "always" that's very slow but a bit safer than
		# everysec.
		#
		# More details please check the following article:
		# http://antirez.com/post/redis-persistence-demystified.html
		#
		# If unsure, use "everysec".
		
		# appendfsync always
		appendfsync everysec
		# appendfsync no

		##Always：同步持久化 每次发生数据变更会被立即记录到磁盘  性能较差但数据完整性比较好
		##Everysec：出厂默认推荐，异步操作，每秒记录   如果一秒内宕机，有数据丢失
		##No
		
		# When the AOF fsync policy is set to always or everysec, and a background
		# saving process (a background save or AOF log background rewriting) is
		# performing a lot of I/O against the disk, in some Linux configurations
		# Redis may block too long on the fsync() call. Note that there is no fix for
		# this currently, as even performing fsync in a different thread will block
		# our synchronous write(2) call.
		#
		# In order to mitigate this problem it's possible to use the following option
		# that will prevent fsync() from being called in the main process while a
		# BGSAVE or BGREWRITEAOF is in progress.
		#
		# This means that while another child is saving, the durability of Redis is
		# the same as "appendfsync none". In practical terms, this means that it is
		# possible to lose up to 30 seconds of log in the worst scenario (with the
		# default Linux settings).
		#
		# If you have latency problems turn this to "yes". Otherwise leave it as
		# "no" that is the safest pick from the point of view of durability.
		
		no-appendfsync-on-rewrite no
		##No-appendfsync-on-rewrite：重写时是否可以运用Appendfsync，用默认no即可，保证数据安全性。
		
		# Automatic rewrite of the append only file.
		# Redis is able to automatically rewrite the log file implicitly calling
		# BGREWRITEAOF when the AOF log size grows by the specified percentage.
		#
		# This is how it works: Redis remembers the size of the AOF file after the
		# latest rewrite (if no rewrite has happened since the restart, the size of
		# the AOF at startup is used).
		#
		# This base size is compared to the current size. If the current size is
		# bigger than the specified percentage, the rewrite is triggered. Also
		# you need to specify a minimal size for the AOF file to be rewritten, this
		# is useful to avoid rewriting the AOF file even if the percentage increase
		# is reached but it is still pretty small.
		#
		# Specify a percentage of zero in order to disable the automatic AOF
		# rewrite feature.
		
		auto-aof-rewrite-percentage 100
		auto-aof-rewrite-min-size 64mb
		##Auto-aof-rewrite-min-size：设置允许重写的最小aof文件大小，避免了达到约定百分比但尺寸仍然很小的情况还要重写。
		##Auto-aof-rewrite-percentage：aof自动重写配置，当目前aof文件大小超过上一次重写的aof文件大小的百分之多少进行重写，即当aof文件增长到一定大小的时候，Redis能够调用bgrewriteaof对日志文件进行重写。当前AOF文件大小是上次日志重写得到AOF文件大小的二倍（设置为100）时，自动启动新的日志重写过程。
		
		# An AOF file may be found to be truncated at the end during the Redis
		# startup process, when the AOF data gets loaded back into memory.
		# This may happen when the system where Redis is running
		# crashes, especially when an ext4 filesystem is mounted without the
		# data=ordered option (however this can't happen when Redis itself
		# crashes or aborts but the operating system still works correctly).
		#
		# Redis can either exit with an error when this happens, or load as much
		# data as possible (the default now) and start if the AOF file is found
		# to be truncated at the end. The following option controls this behavior.
		#
		# If aof-load-truncated is set to yes, a truncated AOF file is loaded and
		# the Redis server starts emitting a log to inform the user of the event.
		# Otherwise if the option is set to no, the server aborts with an error
		# and refuses to start. When the option is set to no, the user requires
		# to fix the AOF file using the "redis-check-aof" utility before to restart
		# the server.
		#
		# Note that if the AOF file will be found to be corrupted in the middle
		# the server will still exit with an error. This option only applies when
		# Redis will try to read more data from the AOF file but not enough bytes
		# will be found.
		aof-load-truncated yes
		
		# When rewriting the AOF file, Redis is able to use an RDB preamble in the
		# AOF file for faster rewrites and recoveries. When this option is turned
		# on the rewritten AOF file is composed of two different stanzas:
		#
		#   [RDB file][AOF tail]
		#
		# When loading Redis recognizes that the AOF file starts with the "REDIS"
		# string and loads the prefixed RDB file, and continues loading the AOF
		# tail.
		aof-use-rdb-preamble yes

**AOF启动/修复/恢复**

- 正常恢复
	- 启动：设置Yes 修改默认的appendonly no，改为yes
	- 将有数据的aof文件复制一份保存到对应目录(config get dir)
	- 恢复：重启redis然后重新加载

- 异常恢复
	- 启动：设置Yes
	- 备份被写坏的AOF文件
	- 修复：Redis-check-aof --fix 文件名.aof 进行修复
	- 恢复：重启redis然后重新加载

**Rewrite**

AOF采用文件追加方式，文件会越来越大为避免出现此种情况，新增了重写机制,当AOF文件的大小超过所设定的阈值时，Redis就会启动AOF文件的内容压缩，只保留可以恢复数据的最小指令集.可以使用命令bgrewriteaof

重写原理

AOF文件持续增长而过大时，会fork出一条新进程来将文件重写(也是先写临时文件最后再rename)，遍历新进程的内存中数据，每条记录有一条的Set语句。重写aof文件的操作，并没有读取旧的aof文件，而是将整个内存中的数据库内容用命令的方式重写了一个新的aof文件，这点和快照有点类似

触发机制

**Redis会记录上次重写时的AOF大小，默认配置是当AOF文件大小是上次rewrite后大小的一倍且文件大于64M时触发(注意:实际应用生产中这里一般都是配几GB)**

**优势**

- 每秒同步：appendfsync always同步持久化 每次发生数据变更会被立即记录到磁盘  性能较差但数据完整性比较好
- 每修改同步：appendfsync everysec异步操作，每秒记录，如果一秒内宕机，有数据丢失
- 不同步：appendfsync no从不同步

**劣势**

- 相同数据集的数据而言aof文件要远大于rdb文件，恢复速度慢于rdb
- Aof运行效率要慢于rdb,每秒同步策略效率较好，不同步效率和rdb相同

![](https://static.oschina.net/uploads/space/2017/1125/164332_PeN7_1156339.png)

**总结**

- RDB持久化方式能够在指定的时间间隔能对你的数据进行快照存储
- AOF持久化方式记录每次对服务器写的操作,当服务器重启的时候会重新执行这些命令来恢复原始的数据,AOF命令以redis协议追加保存每次写的操作到文件末尾.Redis还能对AOF文件进行后台重写,使得AOF文件的体积不至于过大
- 只做缓存：如果你只希望你的数据在服务器运行的时候存在,你也可以不使用任何持久化方式.
- 同时开启两种持久化方式
	- 在这种情况下,**当redis重启的时候会优先载入AOF文件来恢复原始的数据**,因为在通常情况下AOF文件保存的数据集要比RDB文件保存的数据集要完整.
	- RDB的数据不实时，同时使用两者时服务器重启也只会找AOF文件。**那要不要只使用AOF呢？作者建议不要**，因为RDB更适合用于备份数据库(AOF在不断变化不好备份)，快速重启，而且不会有AOF可能潜在的bug，留着作为一个万一的手段。
- 性能建议

	因为RDB文件只用作后备用途，建议只在Slave上持久化RDB文件，而且只要15分钟备份一次就够了，只保留save 900 1这条规则。
	 
	如果Enalbe AOF，好处是在最恶劣情况下也只会丢失不超过两秒数据，启动脚本较简单只load自己的AOF文件就可以了。代价一是带来了持续的IO，二是AOF rewrite的最后将rewrite过程中产生的新数据写到新文件造成的阻塞几乎是不可避免的。只要硬盘许可，应该尽量减少AOF rewrite的频率，AOF重写的基础大小默认值64M太小了，可以设到5G以上。默认超过原大小100%大小时重写可以改到适当的数值。
	 
	如果不Enable AOF ，仅靠Master-Slave Replication 实现高可用性也可以。能省掉一大笔IO也减少了rewrite时带来的系统波动。代价是如果Master/Slave同时倒掉，会丢失十几分钟的数据，启动脚本也要比较两个Master/Slave中的RDB文件，载入较新的那个。新浪微博就选用了这种架构

# Redis的事务 #

可以一次执行多个命令，本质是一组命令的集合。一个事务中的所有命令都会序列化，**按顺序地串行化执行执行而不会被其它命令插入，不许加塞**

一个队列中，一次性、顺序性、排他性的执行一系列命令

常用命令

- **discard 取消事务,放弃执行事务块内的所有命令**
- **exec 执行所有事务块内的命令**
- **multi 标记一个事务块的开始**
- **unwatch 取消watch命令对所有Key的监视**
- **watch key 监视一个(或多个)key,如果事务执行之前这个(或这些)key被其他命令所改动,那么事务将被打断**

		##case正常执行
		127.0.0.1:9379> multi			##开启事务
		OK
		127.0.0.1:9379> set k1 v1		##设置的值没有提交成功，只是提交到队列里
		QUEUED
		127.0.0.1:9379> set k2 v2
		QUEUED
		127.0.0.1:9379> set k3 v3
		QUEUED
		127.0.0.1:9379> exec			##提交事务
		1) OK
		2) OK
		3) OK
		127.0.0.1:9379> keys *
		1) "k2"
		2) "k1"
		3) "k3"

		##case放弃事务
		127.0.0.1:9379> multi
		OK
		127.0.0.1:9379> set k4 v4
		QUEUED
		127.0.0.1:9379> set k5 v5
		QUEUED
		127.0.0.1:9379> discard		##放弃事务提交
		OK
		127.0.0.1:9379> keys *
		1) "k2"
		2) "k1"
		3) "k3"

		##冤头债主
		127.0.0.1:9379> multi
		OK
		127.0.0.1:9379> incr k1		##k1是string类型，无法累加，但因为没有提交事务无法判断,先暂存队列里
		QUEUED
		127.0.0.1:9379> set k4 v4
		QUEUED
		127.0.0.1:9379> set k5 v5
		QUEUED
		127.0.0.1:9379> exec		##事务提交发现其一有错误，不影响其它的修改
		1) (error) ERR value is not an integer or out of range
		2) OK
		3) OK
		127.0.0.1:9379> keys *
		1) "k4"
		2) "k5"
		3) "k1"
		4) "k2"
		5) "k3"

		##case全体连坐(事务未提交已报error致命错误，整个事务都提交失败)
		127.0.0.1:9379> multi
		OK
		127.0.0.1:9379> set k6
		(error) ERR wrong number of arguments for 'set' command
		127.0.0.1:9379> set k7 v7
		QUEUED
		127.0.0.1:9379> set k8 v8
		QUEUED
		127.0.0.1:9379> exec
		(error) EXECABORT Transaction discarded because of previous errors.
		127.0.0.1:9379> keys *
		1) "k4"
		2) "k5"
		3) "k1"
		4) "k2"
		5) "k3"

		##watch监控 无加塞，先监控再开启multi
		127.0.0.1:9379> set age 12
		OK
		127.0.0.1:9379> watch age
		OK
		127.0.0.1:9379> multi
		OK
		127.0.0.1:9379> set age 13
		QUEUED
		127.0.0.1:9379> exec
		1) OK
		127.0.0.1:9379> get age
		"13"

		#有加塞，监控了key，如果key被修改了，后面一个事务的执行失效
		127.0.0.1:9379> watch age
		OK
		127.0.0.1:9379> multi
		OK
		127.0.0.1:9379> set age 15
		QUEUED
		127.0.0.1:9379> exec
		(nil)

		##unwatch(在实际应用中,如果监测提交失败，)
		127.0.0.1:9379> watch age
		OK
		127.0.0.1:9379> set age 18
		OK
		127.0.0.1:9379> get age
		"18"
		127.0.0.1:9379> unwatch
		OK
		127.0.0.1:9379> multi
		OK
		127.0.0.1:9379> set age 19
		QUEUED
		127.0.0.1:9379> exec
		1) OK
		127.0.0.1:9379> get age
		"19"

		##注意:一旦执行了exec之前加的监控锁全部都会被取消掉了**
		##总结:Watch指令，类似乐观锁，事务提交时，如果Key的值已被别的客户端改变，比如某个list已被别的客户端push/pop过了，整个事务队列都不会被执行
		##通过WATCH命令在事务执行之前监控了多个Keys，倘若在WATCH之后有任何Key的值发生了变化，EXEC命令执行的事务都将被放弃，同时返回Nullmulti-bulk应答以通知调用者事务执行失败