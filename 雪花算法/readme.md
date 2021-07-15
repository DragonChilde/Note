# 雪花算法

**为什么需要分布式全局唯一ID以及分布式ID的业务需求？集群高并发情况下如何保证分布式唯一全局Id生成？**

在复杂分布式系统中，往往需要对大量的数据和消息进行唯一标识，如在美团点评的金融、支付、餐饮、酒店，猫眼电影等产品的系统中数据日渐增长，对数据分库分表后需要有一个唯一ID来标识一条数据或消息。特别一点的如订单、骑手、优惠券也都雷要有唯一ID做标识。此时一个能够生成全局唯一ID的系统是非常必要的。

ID生成规则部分硬性要求

- **全局唯一**：不能出现重复的ID号，既然是唯一-标识，这是最基本的要求
- **趋势递增**：在`MySQL`的`InnoDB`引擎中使用的是聚集索引，由于多数`RDBMS`使用`Btree`的数据结构来存储索引数据，在主键的选择上面我们应该尽量使用有序的主键保证写入性能。
- **单调递增**：保证下一个ID一定大于上一个ID，例如事务版本号、IM增量消息、排序等特殊需求
- **信息安全**：如果ID是连续的，恶意用户的扒取工作就非常容易做了，直接按照顺序下载指定URL即可。如果是订单号就更危险了，竞争对手可以直接知道我们一天的单量。所以在一些应用场景下，需要ID无规则不规则，让竞争对手否好猜。
- **含时间戳**：这样就能够在开发中快速了解这个分布式id的生成时间。

**ID号生成系统的可用性要求**

- *高可用*：发一个获取分布式ID的请求，服务器就要保证99.999%的情况下给我创建一个唯一分布式ID。
- *低延迟*：发一个获取分布式ID的请求，服务器就要快，极速。
- *高QPS*：假如并发一口气10万个创建分布式ID请求同时杀过来，服务器要顶的住且一下子成功创建10万个分布式ID。

1. 一般通用方案(UUID)

   `UUID(Universally Unique ldentifer)`的标准型式包含32个16进制数字，以连了号分为五段，形式为8-4-4-4-12的36个字符， 示例：`550e8400-e29b-41d4-a716-446655440000`

   - 性能非常高：本地生成，没有网络消耗
   - 如果只是考虑唯一性，那就选用它吧
   - 但是，入数据库性能差

   > **为什么无序的UUID会导致入库性能变差呢？**
   >
   > 1. 无序，无法预测他的生成顺序，不能生成递增有序的数字。首先分布式ID一般都会作为主键， 但是安装MySQL官方推荐主键要尽量越短越好，UUID每一个都很长，所以不是很推荐。
   > 2. 主键，ID作为主键时在特定的环境会存在一些问题。比如做DB主键的场景下，UUID就非常不适用MySQL官方有明确的建议主键要尽量越短越好36个字符长度的UUID不符合要求。
   > 3. 索引，既然分布式ID是主键，然后主键是包含索引的，然后MySQL的索引是通过B+树来实现的，每一次新的UUID数据的插入，为了查询的优化，都会对索引底层的B+树进行修改，因为UUID数据是无序的，所以每一次UUID数据的插入都会对主键地械的B+树进行很大的修改，这一点很不好。 插入完全无序，不但会导致一-些中间节点产生分裂，也会白白创造出很多不饱和的节点，这样大大降低了数据库插入的性能。
   >
   > 
   >
   > All indexes other than the clustered index are known as secondary indexes. In InnoDB, each record in a secondary index contains the primary key columns for the row, as well as the columns specified for the secondary index. InnoDB uses this primary key value to search for the row in the clustered index.
   >
   > If the primary key is long, the secondary indexes use more space, so it is advantageous to have a short primary key.
   >
   > [link](https://dev.mysql.com/doc/refman/8.0/en/innodb-index-types.html)

2. **数据库自增主键**

   在单机里面，数据库的自增ID机制的主要原理是：数据库自增ID和MySQL数据库的replace into实现的。

   `REPLACE INTO`的含义是插入一条记录，如果表中唯一索引的值遇到冲突，则替换老数据。

   这里的`replace into`跟inset功能类似，不同点在于：`replace into`首先尝试插入数据列表中，如果发现表中已经有此行数据（根据主键或唯一索引判断）则先删除，再插入。否则直接插入新数据。

   ```mysql
   CREATE TABLE t_test(
   	id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
   	stub CHAR(1) NOT NULL DEFAULT '',
   	UNIQUE KEY stub(stub)
   )
   
   SELECT * FROMt_ test;
   
   REPLACE INTO t_test (stub) VALUES('b');
   
   SELECT LAST_INSERT_ID();
   ```

3. **集群分布式**

   那数据库自增ID机制适合作分布式ID吗？答案是不太适合

   1. 系统水平扩展比较困难，比如定义好了步长和机器台数之后，如果要添加机器该怎么做？假设现在只有一台机器发号是1，2，3，4，5（步长是1），这
      个时候需要扩容机器一台。可以这样做：把第二台机器的初始值设置得比第一台超过很多，貌似还好，现在想象一下如果我们线上有100台机器，这
      个时候要扩容该怎么做？简直是噩梦，所以系统水平扩展方案复杂难以实现。
   2. 数据库压力还是很大，每次获取ID都得读写一次数据库， 非常影响性能，不符合分布式ID里面的延迟低和要高QPS的规则（在高并发下，如果都去数据库里面获取id，那是非常影响性能的）

   **基于Redis生成全局ID策略**

   因为Redis是单线的天生保证原子性，可以使用原子操作INCR和INCRBY来实现

   注意：在Redis集群情况下，同样和MySQL一样需要设置不同的增长步长，同时key一定要设置有效期可以使用Redis集群来获取更高的吞吐量。

   假如一个集群中有5台Redis。可以初始化每台Redis的值分别是1,2,3,4,5，然后步长都是5。

   各个Redis生成的ID为:

   ```
   A：1, 6, 11, 16, 21
   B：2, 7 , 12, 17, 22
   C：3, 8, 13, 18, 23
   D：4, 9, 14, 19, 24
   E：5, 10, 15, 20, 25
   ```

## 概述

`Twitter`的分布式自增ID算法`snowflake`

`Twitter`的`snowflake`解决了这种需求，最初`Twitter`把存储系统从`MySQL`迁移到`Cassandra`（由Facebook开发一套开源分布式NoSQL数据库系统）。因为`Cassandra`没有顺序ID生成机制，所以开发了这样一套全局唯一生成服务。

`Twitter`的分布式雪花算法`SnowFlake` ，经测试`snowflake`每秒能够产生26万个自增可排序的ID

1. `Twitter`的`SnowFlake`生成ID能够按照时间有序生成。
2. `SnowFlak`e算法生成ID的结果是一个64bit大小的整数， 为一个Long型（转换成字符串后长度最多19）。
3. 分布式系统内不会产生ID碰撞（由`datacenter`和`workerld`作区分）并且效率较高。

分布式系统中，有一些需要使用全局唯一ID的场景， 生成ID的基本要求：

1. 在分布式的环境下必须全局且唯一 。
2. 一般都需要单调递增，因为一般唯一ID都会存到数据库，而`Innodb`的特性就是将内容存储在主键索引树上的叶子节点而且是从左往右，递增的，所以考虑到数据库性能，一般生成的ID也最好是单调递增。 为了防止ID冲突可以使用36位的UUID，但是UUID有一些缺点， 首先他相对比较长， 另外UUID一般是无序的。
3. 可能还会需要无规则，因为如果使用唯一ID作为订单号这种，为了不然别人知道一天的订单量是多少，就需要这个规则。

## 结构

雪花算法的几个核心组成部分：

![](http://120.77.237.175:9080/photos/interview3/01.jpg)

> 号段解析
>
> 1bit：不用，因为二进制中最高位是符号位，1表示负数，0表示正数。生成的id一般都是用整数，所以最高位固定为0。
>
> 41bit - 时间戳，用来记录时间戳，毫秒级：
>
> - 41位可以表示2^41 - 1 个数字
> - 如果只用来表示正整数（计算机中正数包含0），可以表示的数值范围是：0至2^41 − 1， 减1是因为可表示的数值范围是从0开始算的，而不是1。
> - 也就是说41位可以表示 2^41 - 1个毫秒的值，转化成单位年则是( 2^41 − 1 ) / ( 1000 ∗ 60 ∗ 60 ∗ 24 ∗ 365 ) =69年。
> - **简单说就,是当41位都为1都时候根据long支持的最大值最长可以支持到2035年**
>
> 10bit - 工作机器ID，用来记录工作机器ID：
>
> - 可以部署在2^10 = 1024个节点，包括5位`DataCenterId`和5位`Workerld`。
> - 5位(bit) 可以表示的最大正整数是2^5 − 1 = 31 ,即可以用0、1、2、3、…31这32个数字，来表示不同的`DataCenterld`或`Workerld`。
> - **简单说就是,当10位都为1的时候,最多可支持31台服务器,每台服务器最多1024个服务节点,就是最大支持31*1024(前五位是节点ID,后五位是服务器ID)**
>
> 12bit - 序列号，用来记录同毫秒内产生的不同id。
>
> - 12位(bit) 可以表示的最大正整数是2^12 − 1 = 4095， 即可以用0、1、2、 3、…4094这4095个数字，来表示同一机器同一时间截(毫秒)内产生的4095个ID序号。
> - **简单说就是,因1毫秒内可生成4095个ID,当全部都为1的时候,1秒内就可以生成1024*4095个ID**

`SnowFlake`可以保证：

- 所有生成的ID按时间趋势递增。
- 整个分布式系统内不会产生重复id（因为有`DataCenterId`和`Workerld`来做区分)

## 源码

```java
/**
 * Twitter_Snowflake
 * SnowFlake的结构如下(每部分用-分开):
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
 * 41位时间戳(毫秒级)，注意，41位时间戳不是存储当前时间的时间戳，而是存储时间戳的差值（当前时间戳 - 开始时间戳)
 * 得到的值），这里的的开始时间戳，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下面程序SnowflakeIdWorker类的startTime属性）。41位的时间戳，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间戳)产生4096个ID序号
 * 加起来刚好64位，为一个Long型。
 */
public class SnowflakeIdWorker {
    /** 开始时间戳 (2015-01-01) */
    private final long twepoch = 1420041600000L;

    /** 机器id所占的位数 */
    private final long workerIdBits = 5L;

    /** 数据标识id所占的位数 */
    private final long datacenterIdBits = 5L;

    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /** 支持的最大数据标识id，结果是31 */
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /** 序列在id中占的位数 */
    private final long sequenceBits = 12L;

    /** 机器ID向左移12位 */
    private final long workerIdShift = sequenceBits;

    /** 数据标识id向左移17位(12+5) */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /** 时间戳向左移22位(5+5+12) */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /** 工作机器ID(0~31) */
    private long workerId;

    /** 数据中心ID(0~31) */
    private long datacenterId;

    /** 毫秒内序列(0~4095) */
    private long sequence = 0L;

    /** 上次生成ID的时间戳 */
    private long lastTimestamp = -1L;

    //==============================Constructors=====================================
    /**
     * 构造函数
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // ==============================Methods==========================================
    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间戳
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift) //
                | (datacenterId << datacenterIdShift) //
                | (workerId << workerIdShift) //
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /** 测试 */
    public static void main(String[] args) {
        System.out.println("开始："+System.currentTimeMillis());
        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);
        for (int i = 0; i < 50; i++) {
            long id = idWorker.nextId();
            System.out.println(id);
//            System.out.println(Long.toBinaryString(id));
        }
        System.out.println("结束："+System.currentTimeMillis());
    }
}
```

## 实践

`hutool`工具包已经封装了该雪花算法,下面引入最小的依赖就可以进行实现

官网链接:`https://hutool.cn/docs/#/core/%E5%B7%A5%E5%85%B7%E7%B1%BB/%E5%94%AF%E4%B8%80ID%E5%B7%A5%E5%85%B7-IdUtil`

1. `pom`

   ```xml
   <!--不需要全部工具都引入,只需要引入验证码的包就可以实现-->
   <dependency>
       <groupId>cn.hutool</groupId>
       <artifactId>hutool-captcha</artifactId>
       <version>5.7.4</version>
   </dependency>
   ```

2. 开启线程池生成唯一ID

   ```java
   @Component
   @Slf4j
   public class IdGeneratorSnowflake {
   
       private long workerId = 0;
       private long datacenterId = 1;
       private Snowflake snowflake = IdUtil.getSnowflake();
   
       public synchronized long snowflakeId() {
   
           return snowflake.nextId();
       }
   
       public synchronized long snowflakeId(long workerId, long datacenterId) {
           Snowflake snowflake = IdUtil.getSnowflake(workerId, datacenterId);
           return snowflake.nextId();
       }
   
       public static void main(String[] args) {
           IdGeneratorSnowflake snowflake = new IdGeneratorSnowflake();
           ExecutorService service = Executors.newFixedThreadPool(5);
           for (int i = 0; i < 20; i++) {
               service.execute(
                       () -> {
                           System.out.println(snowflake.snowflakeId());
   
                       });
           }
           service.shutdown();
       }
   
   }
   ```

3. 生成结果如下,可以看到如下ID都是根据时间戳递增生成并且是唯一无序的(因为是开启线程,所以顺序有可能不是升序)

   ```
   1415507992074903552
   1415507992074903554
   1415507992074903553
   1415507992079097859
   1415507992079097858
   1415507992079097857
   1415507992079097856
   1415507992079097863
   1415507992079097862
   1415507992079097861
   1415507992079097860
   1415507992079097867
   1415507992079097866
   1415507992079097865
   1415507992079097864
   1415507992079097871
   1415507992079097870
   1415507992079097869
   1415507992079097868
   1415507992079097872
   ```

## 优缺点

优点

- 毫秒数在高位，自增序列在低位，整个ID都是趋势递增的
- 不依赖数据库等第三方系统，以服务的方式部署，稳定性更高，生成ID的性能也是非常高的。
- 可以根据自身业务特性分配bit位，非常灵活。

缺点

- 依赖机器时钟，如果机器时钟回拨，会导致重复ID生成。
- 在单机上是递增的，但是由于设计到分布式环境，每台机器上的时钟不可能完全同步，有时候会出现不是全局递增的情况。
- （此缺点可以认为无所谓，一般分布式ID只要求趋势递增，并不会严格要求递增，90%的需求都只要求趋势递增,基本可以解决90%分布式ID递增的问题,如果非要解决时间回拨,可以使用下面两个）

> 补充:关于时间回拨,下面两个已经解决了这个问题
>
> - 百度开源的分布式唯一ID生成器`UidGenerator`
> - 美团点评分布式ID生成系统`Leaf`