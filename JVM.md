**线程的生命周期**

**NEW->RUNNABLE->BLOCKED->WAITTING->TIMED_WAITING->TERMINATED**

**程序计数器 Program Counter Register**

在程序中 有多个线程轮流切换实现 main()->test1()->test2()方法，为保证方法执行完能恢复到正确的位置，每条线程都需要有一个独立的程序计数器，各条线程之间计数器互影响，独立存储


**Java虚拟机栈 Java Virtual Machine Stacks**

栈也叫栈内存，主管Java程序的运行，是在线程创建时创建,它的生命周期是跟随线程的生命期，线程结束栈内存也就释放,**对于栈来说不存在垃圾回收的问题**,只要线程一结束该栈就Over，生命周期和线程一致，是线程私有的.**基本类型的变量和对象的引用变量都是在函数的栈内存中分配**

本时所说的栈就是指**Java虚拟机栈**（可以理解成是一个子弹夹，按照先进后出，后进先出的原则）

Java的方法执行内存模型都是放在这里，称为**栈帧**（Stack Frame）

main()->test1()->test2()

- test2()方法被调用产生栈帧F3压入栈
- test1()方法被调用产生栈帧F2压入栈
- main()方法被调用产生栈帧F1压入栈底


执行完毕后，先弹出F3,再弹出F2,最后是F1

顶部栈就是当前的方法,该方法执行完毕后会自动将此栈帧出栈

栈主要保存三类数据:

本地变量（Local Variables）输入参数和输出参数以及方法内的变量

		/**以下都是存储在栈内存里**/
		//栈顶
	 	public static int add(int x,int y){
	        int result = -1;
	        return result = x + y;
	    }
	
		//栈底
	    public static void main(String[] args) {
	        int i = 1;
	        add(1,2);
	    }

栈操作（Operand Stack） 记录出栈、入栈的操作


栈帧数据（Frame Data）包括类文件、方法等等

当子弹夹里已经满了不允许继续放更多的子弹时会抛出异常**java.lang.StackOverflowError**

可以通过设置-Xss参数来制定这块空间的大小。

**本地方法栈（Native Method Stack）**

使用Native方法调用系统服务级别的服务

**Java堆（Java Heap）**

Java堆是垃圾收集器管理的主要区域，也称为**GC堆**

NEW一个对象是在堆内存的新生区的伊甸区（Eden space）
幸存者区（Survivor spaces）分为：1，幸存0区 2，幸存1区（这里的幸存区是from和to交换使用的）
养老区
永久存储区

Minor GC轻量级GC（新生代），这种GC算法采用的是复制算法（Copying）

新生区里伊甸区和from,to的存储比例是8：1：1

- 优点：不会产生碎片，幸存者可以整个复制
- 缺点：会消耗一定的内存，如果幸存下来的对象太大会造成严重损耗

FullGC（老年代），老年代一般是由标记清除或者是标记清除与标记整理的混合实现

**一个对象从创建到消亡的过程如下：**

1. 我们用new关键字创建的对象，首先出生在伊甸园，即图中的绿色部分(eden)，由于他刚出生，所以他属于新生代(Young Generation)。

2. 随着程序的运行，伊甸园中的对象将会急速增长，很快这块区域将被填满，这时要保证程序继续运行，就必须清除不再使用的对象。
也就是要进行一次新生代的垃圾回收(Young GC)，首先计算那些对象(假设eden和to区)不再被使用（常用的有标记清除法，火车算法等），然后还在使用中的对象我们成为幸存者。
在新生代提供了两块幸存区域(survior spaces)供幸存者使用：from和to，假设首先这部分幸存者被安排在from区，然后eden和to区的所有对象被无情的清除，伊甸园开始继续接收新生对象。

3. 马上又到了不得不YoungGC的时间，此次检查eden和from区，幸存的对象被放入to区。

4. 不断重复2,3的过程期间一些对象经历了多次幸存游戏，我们称这些对象已经成熟，部分对象会在from和to区域中复制来复制去，如此交换15次（由JVM参数MaxTenuringThreshold决定，这个参数默认是15），将会被移动到老年区(Old)

5. 逐渐老年区的对象也被填满，我们系统将对整个堆内存进行一次垃圾回收(full GC),这个时间明显比YoungGC的时间要长，发生的频率要小。

**抛出异常：**

OOM:java.long.OutOfMemoryError:Java heap space
OOM只会发生在养老区，发生OOM时上一行必定会发生FullGC

**方法区(Method Area)**

方法区与Java堆一样，是各个线程共享的内存区域，它用于存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据。虽然Java虚拟机规范把方法区描述为堆的一个逻辑部分，但是它却有一个别名叫做Non-Heap(非堆)，目地应该是与Java堆区分开来

方法区可以理解为永久代的接口（Interface） 永久代就是方法区的实现

构造方法+接口的定义（所有定义方法的信息，**此区域是共享区间**）

永久代没有垃圾回收

Java7永久代 Java8 元空间

**静态变量+常量+类信息+运行时常量池存在方法区中 实例变量存在堆内存中**


**字符串常量池JDK1.6是在方法区**
**字符串常量池JDK1.7是在堆里**
**字符串常量池JDK1.8是在元空间**

**运行时常量池(Runtime Constant Pool)**

是方法区的一部分，Class文件中除了有类的版本、字段、方法、接口等描述信息外，还有一项信息是常量池（Constant Pool Table）,用于存放编译期生成的各种字面量和符号引用，这部分内容将在类加载后进入方法区的运行是常量池中存放

既然运行时常量池是方法区的一部分，自然受到方法区内存的限制，当常量池无法再申请到内存时会抛出OutOfMemoryError异常

**直接内存**



**栈管运行，堆管存储**


**Java优化主要是优化堆**

-Xms	设置初始分配大小，默认物理内存的1/64

-Xmx	最大分配内存，默认为物理内存的1/4

-XX:+PrintGCDetails	输出详细的GC处理日志

	通过设置VM参数-Xms1024m -Xmx1024m -XX:+PrintGCDetails
	
	 System.out.println(Runtime.getRuntime().maxMemory());		//1029177344
     System.out.println(Runtime.getRuntime().totalMemory());	//1029177344
	
	/**年轻代+老年代=总内存**/
	Heap
	 PSYoungGen      total 305664K, used 15729K [0x00000000eab00000, 0x0000000100000000, 0x0000000100000000)
	  eden space 262144K, 6% used [0x00000000eab00000,0x00000000eba5c420,0x00000000fab00000)
	  from space 43520K, 0% used [0x00000000fd580000,0x00000000fd580000,0x0000000100000000)
	  to   space 43520K, 0% used [0x00000000fab00000,0x00000000fab00000,0x00000000fd580000)
	 ParOldGen       total 699392K, used 0K [0x00000000c0000000, 0x00000000eab00000, 0x00000000eab00000)
	  object space 699392K, 0% used [0x00000000c0000000,0x00000000c0000000,0x00000000eab00000)
	 Metaspace       used 3125K, capacity 4496K, committed 4864K, reserved 1056768K
	  class space    used 342K, capacity 388K, committed 512K, reserved 1048576K

	/**配置最小内存为1M可以看出详细的OOM异常*/
	 String str ="www.test123456789.com";
	    while (true){
	        str+= new Random().nextInt(888888888) + new Random().nextInt(999999999);
	    }
	
	[Full GC (Ergonomics) [PSYoungGen: 512K->496K(1024K)] [ParOldGen: 462K->458K(512K)] 974K->954K(1536K), [Metaspace: 3746K->3746K(1056768K)], 0.0096152 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
	java.lang.OutOfMemoryError: Java heap space
		at java.util.Arrays.copyOfRange(Arrays.java:3664)
		at java.lang.String.<init>(String.java:207)
		at java.lang.StringBuilder.toString(StringBuilder.java:407)
		at TestPlus.main(TestPlus.java:26)
	[Full GC (Ergonomics) [PSYoungGen: 512K->467K(1024K)] [ParOldGen: 458K->457K(512K)] 970K->925K(1536K), [Metaspace: 3755K->3755K(1056768K)], 0.0052077 secs] [Times: user=0.06 sys=0.00, real=0.01 secs] 
	Heap
	 PSYoungGen      total 1024K, used 479K [0x00000000ffe80000, 0x0000000100000000, 0x0000000100000000)
	  eden space 512K, 93% used [0x00000000ffe80000,0x00000000ffef7ee8,0x00000000fff00000)
	  from space 512K, 0% used [0x00000000fff80000,0x00000000fff80000,0x0000000100000000)
	  to   space 512K, 0% used [0x00000000fff00000,0x00000000fff00000,0x00000000fff80000)
	 ParOldGen       total 512K, used 457K [0x00000000ffe00000, 0x00000000ffe80000, 0x00000000ffe80000)
	  object space 512K, 89% used [0x00000000ffe00000,0x00000000ffe727c8,0x00000000ffe80000)
	 Metaspace       used 3757K, capacity 4540K, committed 4864K, reserved 1056768K
	  class space    used 413K, capacity 428K, committed 512K, reserved 1048576K



1StackOverflowError和OutOfMemoryError,谈谈你的理解

2一般什么时候会发生GC?如何处理

答：Java中的GC会有两种回收：年轻代的MinorGC,另外一个就是老年代的FullGC;新对象创建时如果伊甸区空间不足会触发MinorGC，如果此时老年代的内存空间不足会触发FullGC，如果空间都不足抛出OutOfMemoryError

3GC回收策略，谈谈你的理解

答：年轻代（伊甸区+两个幸存区），GC加收策略为"复制"，老年代的保存空间一般较大，GC回收策略为“整理-压缩”

**GC是什么：**

- 频繁收集YOUNG区
- 较少收集OLD区
- 基本不动Perm区

**复制算法**

为了解决效率问题，有了复制算法，这种算法将内存分成相同大小的两块

回收前
![](http://upload-images.jianshu.io/upload_images/1112615-ffaf64836f7d71d5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

回收后
![](http://upload-images.jianshu.io/upload_images/1112615-81fc977d72535b59.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![](http://upload-images.jianshu.io/upload_images/1112615-bbd542e94ead259d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**标记-整理算法**

综合复制和标记算法，整理算法会把有用的存活对象向y，一端移动，这样避免了复制算法浪费那么多内存，也不会像普通标记回收算法一样导致内存碎片过于严重。

回收前

![](http://upload-images.jianshu.io/upload_images/1112615-141b22c5c8f8efce.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

回收后

![](http://upload-images.jianshu.io/upload_images/1112615-821ce46430e3de22.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**新生代一般是用复制算法**

老年代一般是由标记清除或者是标记清除与标记整理的混合实现

**分代收集算法**

将java堆内存分成老年代，新生代。根据经验，新生代死亡比较快，老年代比较持久。所以一般新生代区域使用复制方法，只需要复制几个就可以了，老年代比较持久，所以一般用标记清除，或标记整理来回收。

**注意没有最好的方法，只有最适合的方法**

1JVM内存模型以及分区，需要详细到每个区放什么？

2堆里面的分区：Eden,survival from to,老年代，各自的特点

3GC的三种收集方法：标记清除、标记整理、复制算法的原理与特点

4Minor GC与Full GC分别在什么时候发生


![](https://raw.githubusercontent.com/DragonChilde/MarkdownPhotos/master/photos/1.png)


在Java8中，永久代已经被移除，被一个称为元空间的区域所取代。元空间的本质和永久代类似。

在元空间（Java8）与永久代（Java7）之间最大的区别在于：
永久带使用的JVM的堆内存，但是Java8以后的元空间并不在虚拟机中而是使用本机物理内存

因此，默认情况下，元空间的大小仅受本地内存限制。类的元数据放入native memory,字符串池和类的静态变量放入java堆中，这样可以加载多少类的元数据就不再由MaxPermSize控制，而由系统的实际可用空间来控制

