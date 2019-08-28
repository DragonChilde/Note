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

**- String（字符串）**

string是redis最基本的类型，你可以理解成与Memcached一模一样的类型，一个key对应一个value。
 
string类型是二进制安全的。意思是redis的string可以包含任何数据。比如jpg图片或者序列化的对象 。
 
string类型是Redis最基本的数据类型，一个redis中字符串value最多可以是512M

**- Hash（哈希，类似java里的Map）**

Redis hash 是一个键值对集合。

Redis hash是一个string类型的field和value的映射表，hash特别适合用于存储对象。
 
类似Java里面的Map<String,Object>

**- List（列表）**

Redis 列表是简单的字符串列表，按照插入顺序排序。你可以添加一个元素导列表的头部（左边）或者尾部（右边）。

它的底层实际是个链表

**- Set（集合）**

Redis的Set是string类型的无序集合。它是通过HashTable实现实现的

**- Zset(sorted set：有序集合)**

Redis zset 和 set 一样也是string类型元素的集合,且不允许重复的成员。

不同的是每个元素都会关联一个double类型的分数。

redis正是通过分数来为集合中的成员进行从小到大的排序。
zset的成员是唯一的,但分数(score)却可以重复。

**redis常见数据类型操作命令:http://redisdoc.com/**

**Redis键(key)**

-  keys *
-  exists key的名字，判断某个key是否存在
-  move key db   --->当前库就没有了，被移除了
-  expire key 秒钟：为给定的key设置过期时间
-  ttl key 查看还有多少秒过期，-1表示永不过期，-2表示已过期(过期就代表消失了)
-  type key 查看你的key是什么类型

**Redis字符串(String)**

**- set/get/del/append/strlen**

**- Incr/decr/incrby/decrby,一定要是数字才能进行加减**

**- getrange/setrange**

getrange:获取指定区间范围内的值，类似between......and的关系
从零到负一表示全部

setrange设置指定区间范围内的值，格式是setrange key值 具体值

**-  setex(set with expire)/setnx(set if not exist)**

setex:设置带过期时间的key，动态设置。

setex 键 秒值 真实值

setnx:只有在 key 不存在时设置 key 的值。

**- mset/mget/msetnx(如果有一个失败，全部都设置失败)**

mset:同时设置一个或多个 key-value 对。

mget:获取所有(一个或多个)给定 key 的值。

**-  getset(先get再set)**

getset:将给定 key 的值设为 value ，并返回 key 的旧值(old value)。

简单一句话，先get然后立即set
