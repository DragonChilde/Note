类的加载器有几种？？？？双亲委派机制

引导类加载器、自定义类加载器 、扩展类加载器、应用类加载器


GC的三和种收集方法的优缺点？

引用计数

缺点：每次对对象赋值的均要维护引用计数器，且计数器本身也有一定的消耗
较难处理循环引用

复制（复制之后有交换，谁空谁是TO）
优点：不会产生内存碎片
缺点：浪费空间，大对象复制会耗时

标记清除

优点：大面积标记不会浪费时间

缺点：会产生内存碎片

标记整理

优点：不会产生碎片
缺点：耗时间

**JVM垃圾回收的时候如何确定垃圾？是否知道什么是GC Roots**

什么是垃圾？**内存中已经不再被使用到的空间就是垃圾**

要进行垃圾回收，如何判断一个对象是否可以被回收？

**1引用计数法（可以忽略）**

Java中，引用和对象是有关联的。如果要操作对象则必须引用进行。

因此，简单的办法是通过引用计数来判断一个对象是否可以回收。简单的说，给对象中添加一个引用计数，每当有一个引用失效时，计数器值减1.

任何时刻计数器值为0的对象就是不可能再被利用的，那么这个对象就是可回收对象。

**那么为什么主流的Java虚拟机里面都没有选择这种算法呢？主要的原因是它很难解决对象之间相互循环引用的问题。**

什么是循环引用？
就是多个对象，互相引用对方作为属性，下面就是A依赖B，B依赖C，C依赖A的循环引用。

![](http://upload-images.jianshu.io/upload_images/1112615-ac9f5b3e247bc4a9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**2枚举根节点做可达性分析（根搜索路径）**

为了解决引用计数法的循环引用问题，Java使用了可达性分析的方法。

所谓“GC roots”或者tracing GC的“根集合”就是一组必须活跃的引用。

![蓝色表示可达对象灰色表示不可达对象](http://upload-images.jianshu.io/upload_images/1112615-70117103618610c2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
(蓝色表示可达对象灰色表示不可达对象)

基本思路就是通过一系列名为“GC Roots”的对象作为起点，从这个被称为GC Roots的对象开始向下搜索，如果一个对象到GC Roots没有任何引用链相连时，则说明此对象不可用。即给定一个集合的引用作为根出发，通过引用关系遍历对象图，能被遍历到的（可达性的）对象就被判定为存活，没有被遍历到的就被判断为死亡。

在图上可以看到，从GCRoot开始，蓝色部分的对象都可以被遍历到，灰色部分，即使 Object A 可以遍历到 Object B 和Object C，但是却没有了GCRoot 引用，所以就属于不可达的死亡对象了

**注意这里的集合是指GC Root Set**

Java中可以作为GC Roots的对象

1. 虚拟机栈（栈帧中的局部变量区，也叫做局部变量表）中引用的对象。
2. 方法区中的类静态属性引用的对象。
3. 方法区中常量引用的对象
4. 本地方法栈中JNI（Native方法）引用的对象。

		public class GCRootDemo {
		    private byte[] byteArray = new byte[100*1024*1024];
			//private static GCRootDemo gcRootDemo = new GCRootDemo();//2
			//private static final GCRootDemo gcRootDemo = new GCRootDemo();//3
		
		    public static void m1(){
		        GCRootDemo t1 = new GCRootDemo();
		        System.gc();
		        System.out.println("第一次GC完成");
		    }
		
		    public static void main(String[] args){
		        m1();
		    }
		}

**JVM调优和参数配置**

JVM的参数类型有三种：

1. 标配参数 -version|-help|java -showversion
2. x参数（了解）-Xint：解释执行|-Xcomp：第一次使用就编译成本地代码|-Xmixed：混合模式
3. xx参数

		Boolean类型
		-XX：+或者-某个属性值 +表示开启  -表示关闭

		是否打印GC收集细节 
		-XX：-PrintGCDetails
		-XX:+PrintGCDetails
		是否使用串行垃圾回收器
		-XX:-UseSerialGC
		-XX:UseSerialGC

		KV设值类型
		-XX:属性key=属性值value
		-XX:MetaspaceSize=128m
		-XX:MaxTenuringThreadhold=15

		//jinfo举例，如何查看当前运行程序的配置
		//jinfo -flag 配置项 java进程编号
		//jinfo -flags java进程编号

		D:\Code\interview2>jps -l
		8448 TestPlus
		6532
		8932 jdk.jcmd/sun.tools.jps.Jps
		8040
		4332 org.jetbrains.jps.cmdline.Launcher
		
		D:\Code\interview2>jinfo -flag PrintGCDetails 8448
		-XX:-PrintGCDetails

		D:\Code\interview2>jps -l
		8208 TestPlus
		3492 org.jetbrains.jps.cmdline.Launcher
		6532
		1048 jdk.jcmd/sun.tools.jps.Jps
		8040
		
		D:\Code\interview2>jinfo -flag MetaspaceSize 8208
		-XX:MetaspaceSize=134217728


		D:\Code\interview2>jps -l
		8208 TestPlus
		8864 jdk.jcmd/sun.tools.jps.Jps
		3492 org.jetbrains.jps.cmdline.Launcher
		6532
		8040
		
		D:\Code\interview2>jinfo -flags 8208 //查看当前线程下设置的所有参数
		VM Flags:
		-XX:CICompilerCount=3 -XX:InitialHeapSize=67108864 -XX:MaxHeapSize=1065353216 -XX:MaxNewSize=354942976 -XX:MetaspaceSize=134217728 -XX:MinHeapDeltaBytes=524288 -XX:NewSize=22020096 -XX:OldSize=45088768 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+Use
		FastUnorderedTimeStamps -XX:-UseLargePagesIndividualAllocation -XX:+UseParallelGC

**注意：-Xms：等价于-XX:InitialHeapSize  -Xmx：等价于-XX：MaxHeapSize**
**这两个参数必须配成一致**


查看JVM默认值

**java -XX：+PrintFlagsInitial -version主要查看初始默认**

**java -XX:+PrintFlagsFinal -version主要查看修改更新**

**java -XX:+PrintCommandLineFlags -version主要查看JVM用的哪种垃圾回收器**

**注意 = 和 ：=的区别是： =是没有修改的值 ：=是人为或者JVM加载不一样修改过的**

		D:\Code\interview2\src>java -XX:+PrintCommandLineFlags -version
		-XX:G1ConcRefinementThreads=4 -XX:InitialHeapSize=66513344 -XX:MaxHeapSize=1064213504 -XX:+PrintCommandLineFlags -XX:ReservedCodeCacheSize=251658240 -XX:+SegmentedCodeCache -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseG1GC -XX:-UseLargePagesIndivi
		dualAllocation
		java version "9.0.4"
		Java(TM) SE Runtime Environment (build 9.0.4+11)
		Java HotSpot(TM) 64-Bit Server VM (build 9.0.4+11, mixed mode)
		//注意这个命令最主要的作用是看JVM用的哪种垃圾回收器（-XX:+UseG1GC）

**JVM常用基本配置参数有哪些？**

**-Xms**	初始大小内存，默认为物理内存1/64 等价于-XX:InitialHeapSize

**-Xmx**	最大分配内存,默认为物理内存1/4	等价于-XX:MaxHeapSize

**-Xss**	设置单个线程栈的大小，一般默认为512K~1024K 等价于-XX:ThreadStackSize

	显示为0是因为默认值跟平台系统有关（一般LINUX为1024K）

**-Xmn**	设置年轻代大小（一般用默认可以了，不需要调整）

**-XX:MetaspaceSize**	设置元空间大小 元空间的本质和永久代类似，都是对JVM规范中方法区的实现。不过元空间与永久代之间最大的区别在于：


**元空间并不在虚拟机中，而是使用本地内存(java.lang.ref.Reference)**


因此，默认情况下，元空间的大小仅受本地内存限制，默认配置为21M左右，但为了避免OOM，可以设置为1024M

**-XX:+PrintGCDetails**	显示垃圾回收的详细情况

**-XX:SurvivorRatio**	设置新生代中eden和S0/S1空间的比例

	默认 -XX:SurvivorRatio=8 Eden:S0:S1=8:1:1
	假如	-XX:SurvivorRatio=4 Eden:S0:S1=4:1:1
	SurvivorRatio值就是设置eden区的比例占多少，S0/S1相同

**-XX:NewRatio**配置年轻代与老年代在堆结构的占比
	默认
	-XX:NewRatio=2新生代占1，老年代2，年轻代占整个堆的1/3
	假如
	-XX:NewRatio=4新生代占1，老年代4，年轻代占整个堆的1/5 NewRatio值就是设置老年代的占比，剩下的1给新生代

**-XX:MaxTenuringThreshold** 设置垃圾最大年龄，就是年轻区到老年区经历的最大次数（默认最大只能设置15，只能设置0-15）

## 强引用、软引用、弱引用、虚引用分别是什么？ ##
**一般赋值的引用都是强引用(java.lang.ref.Reference）默认支持**

当内存不足，JVM开始垃圾回收，**对于强引用的对象，就算是出现了OOM也不会对该对象进行回收，死都不收**。

强引用是最常见的普通对象引用，只要还有强用指向一个对象，就能表明对象还“活着”，垃圾收集器不会碰到这种对象。在Java中最常见的就是强引用，把一个对象赋给一个引用变量，这个引用变量就是一个强引用。当一个对象被强引用变量引用时，它处于可达状态，它是不可能被垃圾机制回收的，即使该对象以后永远都不能被用到，JVM也不会回收。因此强引用是造成Java内存泄露的主要原因之一。

对于一个普通的对象，如果没有其他的引用关系，只要超过了引用的作用域或者显示地将相应（强）引用赋值为null，一般认为就是可以被垃圾收集的了（当然具体回收时还要看垃圾收集策略）。

    Object obj1 = new Object();//这样定义默认的就是强引用
    Object obj2 = obj1;//obj2引用赋值
    obj1 = null;
    System.gc();
    System.out.println(obj2);	//可以正常打印出来不会被垃圾回收java.lang.Object@1b6d3586

软引用（java.lang.ref.SoftReference）

软引用是一种相对强化了一些的引用，需要用java.lang.ref.SoftReference类来实现，可以让对象豁免一些垃圾收集。

对于只有软引用的对象来说，
**当系统内存充足时，它不会被回收**

**当系统内存不足时，它会被回收**

软引用通常用在对内存敏感的程序中，比如高速缓存就有用到软引用，内存够用的时候就保留，不够用就回收。

	 /*
    * 内存够用的时候就保留，不够用就回收
    * */
    public static void softRef_Memory_Enough(){
        Object o1 = new Object();
        SoftReference<Object> softReference = new SoftReference<>(o1);
        System.out.println(o1);
        System.out.println(softReference.get());

        o1 = null;
        System.gc();
        System.out.println(o1);
        System.out.println(softReference.get());

		//可以打印
		//java.lang.Object@1b6d3586
		//java.lang.Object@1b6d3586
		//null
		//java.lang.Object@1b6d3586
		
    }

    public static void softRef_Memory_NotEnough(){
        Object o1 = new Object();
        SoftReference<Object> softReference = new SoftReference<>(o1);
        System.out.println(o1);
        System.out.println(softReference.get());

        o1 = null;

        try{
            byte[] bytes = new byte[30*1024*1024];
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            System.out.println(o1);
            System.out.println(softReference.get());
        }
		
		/**JVM配置，故意产生大对象并配置小的内存，让它内存不够用了导致OOM,看软引用的加收情况，看出软引用为null***/
		//-Xms1m -Xmx1m -XX:+PrintGCDetails
		/*
		[GC (Allocation Failure) [PSYoungGen: 508K->472K(1024K)] 508K->472K(1536K), 0.0206076 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
		[GC (Allocation Failure) [PSYoungGen: 976K->488K(1024K)] 976K->560K(1536K), 0.0031252 secs] [Times: user=0.02 sys=0.02, real=0.00 secs] 
		java.lang.Object@1b6d3586
		java.lang.Object@1b6d3586
		[GC (Allocation Failure) [PSYoungGen: 922K->504K(1024K)] 994K->608K(1536K), 0.0533493 secs] [Times: user=0.13 sys=0.00, real=0.05 secs] 
		[GC (Allocation Failure) [PSYoungGen: 504K->504K(1024K)] 608K->616K(1536K), 0.0166666 secs] [Times: user=0.03 sys=0.00, real=0.02 secs] 
		[Full GC (Allocation Failure) [PSYoungGen: 504K->495K(1024K)] [ParOldGen: 112K->80K(512K)] 616K->576K(1536K), [Metaspace: 3032K->3032K(1056768K)], 0.0479199 secs] [Times: user=0.02 sys=0.00, real=0.05 secs] 
		[GC (Allocation Failure) [PSYoungGen: 495K->504K(1024K)] 576K->600K(1536K), 0.0211873 secs] [Times: user=0.00 sys=0.00, real=0.02 secs] 
		[Full GC (Allocation Failure) [PSYoungGen: 504K->473K(1024K)] [ParOldGen: 96K->85K(512K)] 600K->558K(1536K), [Metaspace: 3032K->3032K(1056768K)], 0.0249160 secs] [Times: user=0.02 sys=0.00, real=0.03 secs] 
		null
		null
		Heap
		 PSYoungGen      total 1024K, used 512K [0x00000000ffe80000, 0x0000000100000000, 0x0000000100000000)
		  eden space 512K, 7% used [0x00000000ffe80000,0x00000000ffe89ab8,0x00000000fff00000)
		  from space 512K, 92% used [0x00000000fff00000,0x00000000fff76620,0x00000000fff80000)
		  to   space 512K, 0% used [0x00000000fff80000,0x00000000fff80000,0x0000000100000000)
		 ParOldGen       total 512K, used 85K [0x00000000ffe00000, 0x00000000ffe80000, 0x00000000ffe80000)
		  object space 512K, 16% used [0x00000000ffe00000,0x00000000ffe15520,0x00000000ffe80000)
		 Metaspace       used 3063K, capacity 4496K, committed 4864K, reserved 1056768K
		  class space    used 333K, capacity 388K, committed 512K, reserved 1048576K
		Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
			at SoftReferenceDemo.softRef_Memory_NotEnough(SoftReferenceDemo.java:34)
			at SoftReferenceDemo.main(SoftReferenceDemo.java:45)
		*/
    }

    public static void main(String[] args){
        //softRef_Memory_Enough();
        softRef_Memory_NotEnough();
    }

**弱引用(java.lang.ref.WeakReference<T>)**

弱引用需要用java.lang.ref.WeakReference类来实现，它比软引用的生存期更短。

对于软引用对象来说，只要垃圾回收机制一运行，**不管JVM的内存空间是否足够，都会回收该对象占用的内存**。

	   Object o1 = new Object();
	    WeakReference<Object> weakReference = new WeakReference<>(o1);
	    System.out.println(o1);
	    System.out.println(weakReference.get());
	
	    o1 = null;
	    System.gc();
	    System.out.println("...............");
	
	    System.out.println(o1);
	    System.out.println(weakReference.get());

		//弱引用只要GC就为null
		/*
		java.lang.Object@1b6d3586
		java.lang.ref.WeakReference@4554617c
		...............
		null
		null
		*/

**软引用和所引用的使用场景**

假如有一个应用需要读取大量的本地图片：

如果每次读取图片都从硬盘读取则会严重影响性能。如果一次性全部加载到内存中有可能造成内存泄露。此时使用软引用可以解决这个问题。

设计思路：用一个HashMap来保存图片的路径和相应图片对象关联的软引用之间的映射关系，在内存不足时，JVM会自动回收这些缓存图片对象所占用的空间，从而有效地避免OOM的问题。

Map<String,SoftReference<Bitmap>> imageCache = new HashMap<String,SoftReference<Bitmap>>();

**你知道所引用的话，谈谈weakHashMap？**

	public class WeakHashMapDemo {
	    public static void main(String[] args){
	        myHashMap();
	        System.out.println("========");
	        myWeakHashMap();
	    }
	
	    private static void myHashMap(){
	        HashMap<Integer,String> map = new HashMap<>();
	        Integer key = new Integer(1);
	        String value = "HashMap";
	
	        map.put(key,value);
	        System.out.println(map);
	
	        key = null;
	        System.out.println(map);
	
	        System.gc();
	        System.out.println(map);

			/*
			{1=myHashMap}
			{1=myHashMap}
			{1=myHashMap}
			*/
	    }
	
	    private static void myWeakHashMap(){
	        WeakHashMap<Integer,String> map = new WeakHashMap<>();
	        Integer key = new Integer(2);
	        String value = "WeakHashMap";
	
	        map.put(key,value);
	        System.out.println(map);
	
	        key = null;
	        System.out.println(map);
	
	        System.gc();
	        System.out.println(map);

			//垃圾加收
			/*
			{2=myWeakHashMap}
			{2=myWeakHashMap}
			{}
			*/
	    }
	}


#### 虚引用（大多数情况下很少用到）(java.lang.refPhantonReference) ####

虚引用需要java.lang.refPhantonReference类来实现。

顾名思义，就是形同虚设，与其他几种引用都不同，虚引用并不会决定对象的生命周期。
如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收器回收，**它不能单独使用也不能通过它访问对象，虚引用必须和队列（ReferenceQueue）联合使用。**

虚引用的主要作用是跟踪对象被垃圾回收的状态。仅仅是提供了一种确保对象被finalize以后，做某些事情的机制。

PhantomReference的get方法总是返回null，因此无法访问对应的引用对象。其意义在于说明一个对象那个已经进入finalization阶段，可以被gc回收，用来实现比finalization机制更灵活的回收操作。

换句话说，设置虚引用关联的唯一目的，就是在这个对象被收集器回收的时候收到一个系统通知或者后续添加进一步的处理。

Java技术允许使用finalize（）方法在垃圾收集器将对象从内存中清楚之前做必要的清理工作。
	

**引用队列（java.lang.ref.ReferenceQueue<T>）**

被回收前需要被引用队列保存下。一般强引用不会用到，另外的3个的可以用，虚引用必须结合使用


		/*Java提供了4种引用类型,在垃圾回收的时候，都有自己各自的特点。
		ReferenceQueue是用来配合引用工作的，没有ReferenceQueue一样可以运行。
		创建引用的时候可以指定关联的队列，当GC释放对象内存的时候，会将引用加入到引用队列，如果程序发现某个虚引用已经被加入到引用队列，那么就可以在所引用的对象的内存被回收之前采取必要的行动
		当关联的引用队列中有数据的时候，意味着引用指向的堆内存中的对象被回收。通过这种方式，JVM允许我们在对象被销毁后，做一些我们自己想做的事情。*/

	   ·Object o1 = new Object();
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
        PhantomReference<Object> phantomReference = new PhantomReference<>(o1,referenceQueue);

        System.out.println(o1);
        System.out.println(phantomReference.get());
        System.out.println(referenceQueue.poll());

        System.out.println("=================");
        o1 = null;
        System.gc();
        Thread.sleep(500);

        System.out.println(o1);
        System.out.println(phantomReference.get());
        System.out.println(referenceQueue.poll());


# 谈谈对OOM的认识？ #

**java.lang.StackOverFlowError**

		/*   java.lang.Object
			     java.lang.Throwable
			         java.lang.Error
			             java.lang.VirtualMachineError
			                 java.lang.StackOverflowError*/
  		public static void main(String[] args) {
	        StackOverflowError();
	    }
	
	    public static void StackOverflowError(){
	        StackOverflowError();
	    }
		/**
		Exception in thread "main" java.lang.StackOverflowError
		at StackOverflowErrorDemo.StackOverflowError(StackOverflowErrorDemo.java:11)
		*/

**java.lang.OutOfMemoryError:Java heap space**

		//-Xms1m -Xmx1m -XX:+PrintGCDetails
		String str = null;
        while (true){
            str+=new Random().nextInt(888888888)+new Random().nextInt(999999999);
        }

**java.lang.OutOfMemoryError:GC overhead limit exceeded**

GC回收时间长时会抛出OutOfMemoryError。过长的定义是，超过98%的时间用来做GC并且回收了不到2%的堆内存，连续多次GC都只回收了不到2%的极端情况下才会抛出。

假设不抛出GC overhead limit错误会发生什么情况呢？

那就是GC清理的这么点内存很快会再次填满，迫使GC再次执行，这样就形成恶性循环，CPU使用率一直是100%，而GC缺没有任何成果。

		/**-Xmx1m Xms1m -XX:MaxDirectMemorySize=5m**/
		int i = 0;
        List<String> list = new ArrayList<>();

        try{
            while(true){
                list.add(String.valueOf(++i).intern());
            }
        }catch (Throwable e){
            System.out.println("***************i:"+i);
            e.printStackTrace();
            throw e;
        }
	

**java.lang.OutOfMemoryError:Direct buffer memory**

导致原因：

写NIO程序经常使用ByteBuffer来读取或者写入数据，这是一种基于通道（Channel）与缓冲区（Buffer）的I/O方式，它可以使用Native函数库直接分配堆外内存，然后通过一个存储在Java堆里面的DirectByteBuffer对象作为这块内存的引用进行操作。这样能在一些场景中显著提高性能，因为避免了在java堆和Native堆中来回复制数据。

ByteBuffer.allocate(capability)第一种方式是分配JVM堆内存，属于GC管辖范围，由于需要拷贝所以速度相对较慢。

ByteBuffer.allocateDirect(capability)第二种方式是分配OS本地内存，不属于GC管辖范围，由于不需要内存拷贝，所以速度相对较快。

但如果不断分配内存，堆内存很少使用，那么JVM就不需要执行GC，DirectByteBuffer对象们就不会被回收，这时候堆内存充足，但本地内存可能已经使用光了，再次尝试分配本地内存就会出现OutOfMemoryError，那程序就直接崩溃了。
	  
**注意这种情况多数都是在写NIO的情况下出现**
		
		//-Xms1m -Xmx1m -XX:+PrintGCDetails -XX:MaxDirectMemorySize=1m
	   System.out.println("配置的DirectMaxMemorySize:"+sun.misc.VM.maxDirectMemory()/1024/1024+"MB");//显示使用的最大本地内存
       ByteBuffer allocate = ByteBuffer.allocateDirect(6 * 1024 * 1024);	//分配使用NIO的本地内存

		/*
		[GC (Allocation Failure) [PSYoungGen: 508K->488K(1024K)] 508K->488K(1536K), 0.0006103 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
		[GC (Allocation Failure) [PSYoungGen: 1000K->504K(1024K)] 1000K->584K(1536K), 0.0008472 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
		[GC (Allocation Failure) [PSYoungGen: 998K->504K(1024K)] 1078K->648K(1536K), 0.0007133 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
		配置的DirectMaxMemorySize:1MB
		[GC (System.gc()) [PSYoungGen: 614K->504K(1024K)] 758K->700K(1536K), 0.0006156 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
		[Full GC (System.gc()) [PSYoungGen: 504K->480K(1024K)] [ParOldGen: 196K->133K(512K)] 700K->614K(1536K), [Metaspace: 3205K->3205K(1056768K)], 0.0032241 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
		Exception in thread "main" java.lang.OutOfMemoryError: Direct buffer memory
			at java.nio.Bits.reserveMemory(Bits.java:694)
			at java.nio.DirectByteBuffer.<init>(DirectByteBuffer.java:123)
			at java.nio.ByteBuffer.allocateDirect(ByteBuffer.java:311)
			at DirctBufferMemoryDemo.main(DirctBufferMemoryDemo.java:12)
		Heap
		 PSYoungGen      total 1024K, used 533K [0x00000000ffe80000, 0x0000000100000000, 0x0000000100000000)
		  eden space 512K, 10% used [0x00000000ffe80000,0x00000000ffe8d390,0x00000000fff00000)
		  from space 512K, 93% used [0x00000000fff80000,0x00000000ffff8340,0x0000000100000000)
		  to   space 512K, 0% used [0x00000000fff00000,0x00000000fff00000,0x00000000fff80000)
		 ParOldGen       total 512K, used 133K [0x00000000ffe00000, 0x00000000ffe80000, 0x00000000ffe80000)
		  object space 512K, 26% used [0x00000000ffe00000,0x00000000ffe215a8,0x00000000ffe80000)
		 Metaspace       used 3237K, capacity 4496K, committed 4864K, reserved 1056768K
		  class space    used 352K, capacity 388K, committed 512K, reserved 1048576K
		*/



**java.lang.OutOfMemoryError:unable to create new native thread**

高并发请求服务器时，经常出现如下异常：java.lang.OutOfMemoryError:unbale to create new native thread
准确的将该native thread异常与对应的平台有关。

导致原因：

应用创建了太对线程，一个应用进程创建多个线程，超过系统承载极限。

服务器并不允许应用程序创建那么多线程，linux系统默认允许单个进程可以创建的线程数是1024个，如果应用创建超过这个数量，就会报java.lang.OutOfMemoryError:unable to create new native thread

解决办法：

想办法降低应用程序创建线程的数量，分析应用是否真的需要创建那么多线程，如果不是，改代码将线程数降到最低。(一般线上服务器最多1024个线程并发的情况下，取2/3大概800多已经够用)

对于有的应用，确实需要创建多个线程，远超过linux系统默认的1024个线程的限制，可以通过修改linux服务器配置，扩大linux默认限制。

**注意：这种情况大多数是发生在生产环境服务器高并发的的环境下的，如果有人问遇到的最深刻的故障或者问题，从中学到什么可以答这个**


	   for (int i = 1;  ; i++) {
            System.out.println(i);
            new Thread(()->{
                try {
                    TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            },String.valueOf(i)).start();
        }
		/*
		Exception in thread "main" java.lang.OutOfMemoryError: unable to create new native thread
		at java.lang.Thread.start0(Native Method)
		at java.lang.Thread.start(Thread.java:717)
		at UnableCrateThreadDemo.main(UnableCrateThreadDemo.java:18)
		*/

Linux系统下命令：
**ulimit -u**	查看当前系统用户的线程数

**vim/etc/security/limits.d/90-nproc.conf**		修改线程配置文件

**java.langOutOfMemoryError:Metaspace**
	
使用java -XX:+PrintFlagsInitial命令查看本机的初始化参数，-XX:Metaspacesize为218103768（大约为20.8M）

	/*
	* Java8之后的版本使用Metaspace来替代永久代
	* Metaspace是方法区在HotSpot中的实现，它与持久带最大的区别在于：Metaspace并不在虚拟机内存中而是使用
	* 本地内存，也即在java8中，class metaspace（the virtual machines internal presentation of java class）
	* ,被存储在叫做Metaspace的native memory
	*
	* 永久代（Metaspace）存放以下信息：
	* 虚拟机加载的类信息
	* 常量池
	* 静态变量
	* 即时编译后的代码
	* */
	
	public class MetaspaceOOMT {
	    static class OOMTest{
	
	    }
	    public static void main(String[] args){
	        int i=0;//模拟多少次后发生异常
	
	        try{
	            while (true){
	                i++;
	                
	            }
	        }catch (Throwable e){
	            System.out.println("********多少次后发生了异常："+i);
	            e.printStackTrace();
	        }
	    }
	}


# GC回收算法和垃圾收集器的关系？ #

GC算法（引用计数/复制/标清/标整）是内存回收的方法论（相当于接口），垃圾收集器就是算法落地实现（相当于实现）。

因为目前为止还没有完美的收集器出现，更加没有万能的收集器，只是针对具体应用最合适的收集器，进行分代收集。

**4种主要垃圾收集器**

**串行垃圾回收器（Serial）**

它为单线程环境设计且只使用一个线程进行垃圾回收，会暂停所有的用户线程。所以不适合服务器环境。

**并行垃圾回收器（Parallel）**

多个垃圾收集线程并行工作，此时用户线程是暂停的，适用于科学计算/大数据处理平台处理等弱交互场景。

**并发垃圾回收器（CMS）**

用户线程和垃圾收集线程同时执行（不一定是并行，可能交替执行），不需要停顿用户线程，互联网公司多用它，适用对响应时间有要求的场景。

**GI垃圾回收器**

G1垃圾回收器将堆内存分割成不同的区域然后并发的对其进行垃圾回收。

# 怎么查看服务器默认的垃圾收集器是哪个？生产上你是如何配置垃圾收集器的？ #

JVM参数：

**java -XX:+PrintCommandLineFlags -version**	
	
	-XX:G1ConcRefinementThreads=4 -XX:InitialHeapSize=66513344 -XX:MaxHeapSize=1064213504 -XX:+PrintCommandLineFlags -XX:ReservedCodeCacheSize=251658240 -XX:+SegmentedCodeCache -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseG1GC -XX:-UseLargePagesIndivi
	dualAllocation
	java version "9.0.4"
	Java(TM) SE Runtime Environment (build 9.0.4+11)
	Java HotSpot(TM) 64-Bit Server VM (build 9.0.4+11, mixed mode)

	//-XX:+UseG1GC	G1GC是Java9开始使用的


**参数预先说明**

**DefNew Default New Generation**

**Tenured Old**

**ParNew Parallel New Generation**

**PSYoungGen Parallel Scavenge**

**ParOldGen Parallel Old Generation**

	//示例代码
	 public static void main(String[] args) {
        String str = "123456789";
        while (true){

           str+=  str+new Random().nextInt(888888888) + new Random().nextInt(999999999);
           str.intern();
        }
    }


![](https://c1.staticflickr.com/5/4613/40093687372_3b03521a7c.jpg)

**注意图中Serial->CMS已废弃**

**java的GC回收的类型主要有七种，实际只有六种（Serial Old已经废弃）：**


**UseSerialOldGC（OLD）(已废弃只了解可以)**

Serial Old是Serial垃圾收集器老年代版本，它同样是个单线程的收集器，使用标记-整理算法，这个收集器也主要是运行在Client默认的Java虚拟机默认的老年代垃圾收集器

在Server模式下，主要有两个用途（了解，版本已经到8及以后）：

1，在JDK1.5之前版本中与新生代的Paraller Scavenge收集器搭配使用。（Parallel Scavenge + Serial Old）

2，作为老年代版本中使用CMS收集器的后备垃圾收集方案。

**UseSerialGC（YOUNG）**

**串行收集器：Serial收集器**

一个单线程的收集器，在进行垃圾收集时候，必须暂停其他所有的工作线程知道它收集结束。

对应JVM参数是：**-XX:+UseSerialGC**

开启后会使用：Serial（Young区用）+Serial Old（Old区用）的收集器组合。

表示：新生代、老年代都会使用串行回收收集器，新生代使用复制算法，老年代使用标记-整理算法。


![](https://c1.staticflickr.com/5/4603/28345836579_8dff90eb76_z.jpg)

从图中可以看出从新生代到老年代垃圾回收都是通过串行收售器进行（STW: Stop The World）

	//-Xmx1m -Xms1m -XX:+PrintGCDetails -XX:+PrintCommandLineFlags -XX:+UseSerialGC
	-XX:InitialHeapSize=1048576 -XX:MaxHeapSize=1048576 -XX:+PrintCommandLineFlags -XX:+PrintGCDetails -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:-UseLargePagesIndividualAllocation -XX:+UseSerialGC 
	[GC (Allocation Failure) [DefNew: 508K->64K(576K), 0.0010028 secs] 508K->430K(1984K), 0.0010349 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [DefNew: 568K->64K(576K), 0.0008207 secs] 934K->538K(1984K), 0.0008485 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [DefNew: 576K->64K(576K), 0.0009137 secs] 1050K->612K(1984K), 0.0009422 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [DefNew: 527K->55K(576K), 0.0005228 secs] 1075K->754K(1984K), 0.0005406 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [DefNew: 510K->54K(576K), 0.0004211 secs] 1209K->1024K(1984K), 0.0004370 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [DefNew: 507K->0K(576K), 0.0003056 secs] 1476K->1348K(1984K), 0.0003218 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [DefNew: 443K->443K(576K), 0.0000073 secs][Tenured: 1347K->1132K(1408K), 0.0020172 secs] 1791K->1132K(1984K), [Metaspace: 3184K->3184K(1056768K)], 0.0020589 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [DefNew: 451K->451K(576K), 0.0000103 secs][Tenured: 1132K->1027K(1408K), 0.0020142 secs] 1584K->1027K(1984K), [Metaspace: 3198K->3198K(1056768K)], 0.0020619 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [DefNew: 435K->435K(576K), 0.0000113 secs][Tenured: 1027K->1242K(1408K), 0.0017368 secs] 1462K->1242K(1984K), [Metaspace: 3202K->3202K(1056768K)], 0.0017785 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[Full GC (Allocation Failure) [Tenured: 1242K->1217K(1408K), 0.0018583 secs] 1242K->1217K(1984K), [Metaspace: 3202K->3202K(1056768K)], 0.0018818 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
		at java.util.Arrays.copyOf(Arrays.java:3332)
		at java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:124)
		at java.lang.AbstractStringBuilder.append(AbstractStringBuilder.java:674)
		at java.lang.StringBuilder.append(StringBuilder.java:208)
		at GCRootDemo.main(GCRootDemo.java:12)
	Heap
	 def new generation   total 576K, used 80K [0x00000000ffe00000, 0x00000000ffea0000, 0x00000000ffea0000)
	  eden space 512K,  15% used [0x00000000ffe00000, 0x00000000ffe14020, 0x00000000ffe80000)
	  from space 64K,   0% used [0x00000000ffe80000, 0x00000000ffe80000, 0x00000000ffe90000)
	  to   space 64K,   0% used [0x00000000ffe90000, 0x00000000ffe90000, 0x00000000ffea0000)
	 tenured generation   total 1408K, used 1217K [0x00000000ffea0000, 0x0000000100000000, 0x0000000100000000)
	   the space 1408K,  86% used [0x00000000ffea0000, 0x00000000fffd0658, 0x00000000fffd0800, 0x0000000100000000)
	 Metaspace       used 3249K, capacity 4496K, committed 4864K, reserved 1056768K
	  class space    used 352K, capacity 388K, committed 512K, reserved 1048576K

	//打印日志可以看到堆的垃圾回收 def new generation tenured generation

**UseParallelGC(JAVA8默认是这种模式)(YOUNG)**
	
**使用Parallel Scanvenge收集器**

![](https://c1.staticflickr.com/5/4662/28345836389_55e8402324_z.jpg)

Parallel Scavenge收集器类似ParNew也是一个新生代垃圾收集器，使用复制算法，也是一个并行的多线程的垃圾收集器，俗称吞吐量有限收集器。一句话：串行收集器在新生代和老年代的并行化。

它重点关注的是：

可控制的吞吐量（Thoughput=运行用户代码时间/(运行用户代码时间+垃圾收集时间)，也即比如程序运行100分钟，垃圾收集时间1分钟，吞吐量就是99%）。高吞吐量意味着高效利用CPU的时间，**它多用于在后台运算而不需要太多交互的任务**。

自适应调节策略也是ParallelScavenge收集器与ParNew收集器的一个重要区别。（自使用调节策略：虚拟机会根据当前系统的运行情况收集性能监控信息，动态调整这些参数以提供最合适的停顿时间（-XX:MaxGCPauseMillis）或最大的吞吐量。）

常用JVM参数：

**-XX:+UseParallelGC或-XX:+UseParallelOldGC（可互相激活）**

**并行收集器组合 Parallel Scavenge + Parallel Old**

	-XX:InitialHeapSize=1048576 -XX:MaxHeapSize=1048576 -XX:+PrintCommandLineFlags -XX:+PrintGCDetails -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:-UseLargePagesIndividualAllocation -XX:+UseParallelGC 
	[GC (Allocation Failure) [PSYoungGen: 508K->504K(1024K)] 508K->504K(1536K), 0.1306971 secs] [Times: user=0.00 sys=0.00, real=0.23 secs] 
	[GC (Allocation Failure) [PSYoungGen: 1008K->488K(1024K)] 1008K->576K(1536K), 0.0010061 secs] [Times: user=0.05 sys=0.02, real=0.00 secs] 
	[GC (Allocation Failure) [PSYoungGen: 981K->488K(1024K)] 1069K->640K(1536K), 0.0046515 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
	[GC (Allocation Failure) [PSYoungGen: 903K->488K(1024K)] 1055K->744K(1536K), 0.0006360 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [PSYoungGen: 820K->504K(1024K)] 1076K->921K(1536K), 0.0003883 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[Full GC (Ergonomics) [PSYoungGen: 504K->394K(1024K)] [ParOldGen: 417K->347K(512K)] 921K->741K(1536K), [Metaspace: 3203K->3203K(1056768K)], 0.0036715 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[Full GC (Ergonomics) [PSYoungGen: 844K->394K(1024K)] [ParOldGen: 347K->401K(512K)] 1192K->795K(1536K), [Metaspace: 3204K->3204K(1056768K)], 0.0037798 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[Full GC (Ergonomics) [PSYoungGen: 618K->394K(1024K)] [ParOldGen: 401K->508K(512K)] 1019K->903K(1536K), [Metaspace: 3204K->3204K(1056768K)], 0.0033713 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[Full GC (Ergonomics) [PSYoungGen: 833K->825K(1024K)] [ParOldGen: 508K->293K(512K)] 1342K->1118K(1536K), [Metaspace: 3204K->3204K(1056768K)], 0.0035305 secs] [Times: user=0.06 sys=0.00, real=0.00 secs] 
	[Full GC (Ergonomics) [PSYoungGen: 834K->385K(1024K)] [ParOldGen: 508K->399K(512K)] 1343K->785K(1536K), [Metaspace: 3204K->3204K(1056768K)], 0.0035742 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[Full GC (Ergonomics) [PSYoungGen: 609K->601K(1024K)] [ParOldGen: 399K->399K(512K)] 1008K->1001K(1536K), [Metaspace: 3204K->3204K(1056768K)], 0.0036374 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[Full GC (Allocation Failure) [PSYoungGen: 601K->601K(1024K)] [ParOldGen: 399K->399K(512K)] 1001K->1001K(1536K), [Metaspace: 3204K->3204K(1056768K)], 0.0032610 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	Heap
	 PSYoungGen      total 1024K, used 671K [0x00000000ffe80000, 0x0000000100000000, 0x0000000100000000)
	  eden space 512K, 55% used [0x00000000ffe80000,0x00000000ffec75e8,0x00000000fff00000)
	  from space 512K, 75% used [0x00000000fff00000,0x00000000fff60760,0x00000000fff80000)
	  to   space 512K, 0% used [0x00000000fff80000,0x00000000fff80000,0x0000000100000000)
	 ParOldGen       total 512K, used 399K [0x00000000ffe00000, 0x00000000ffe80000, 0x00000000ffe80000)
	  object space 512K, 78% used [0x00000000ffe00000,0x00000000ffe63f88,0x00000000ffe80000)
	 Metaspace       used 3249K, capacity 4496K, committed 4864K, reserved 1056768K
	  class space    used 352K, capacity 388K, committed 512K, reserved 1048576K
	Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
		at java.util.Arrays.copyOf(Arrays.java:3332)
		at java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:124)
		at java.lang.AbstractStringBuilder.append(AbstractStringBuilder.java:448)
		at java.lang.StringBuilder.append(StringBuilder.java:136)
		at GCRootDemo.main(GCRootDemo.java:12)


**UseConcMarkSweepGC(OLD)(重点)**

![](https://c1.staticflickr.com/5/4740/40093687062_7383cd1b49_z.jpg)

**CMS收集器（Concurrent Mark Sweep：并发标记清除）是一种以获取最短回收停顿时间为目标的收集器。**

**适合应用在互联网站或者B/S系统的服务器上，这类应用尤其重视服务器的响应速度，希望系统停顿时间最短。**

CMS非常适合堆内存大、CPU核数多的服务器端应用，也是G1出现之前大型应用的首选收集器。

Concurrent Mark Sweep并发标记清除，并发收集低停顿，并发指的是与用户线程一起执行。

开启该收集器的JVM参数：-XX:+UseConcMarkSweepGC 开启该参数后会自动将-XX:+UseParNewGC打开

开启该参数后，**使用ParNew（Young区用）+CMS（Old区用）+Serial Old的收集器组合，Serial Old将作为CMS出错的后备收集器。**

**四大过程**
	
**初始标记（CMS initial mark）**
只是标记一下GC Roots能直接关联的对象，速度很快，仍然需要暂停所有的工作线程。

**并发标记（CMS concurrent mark）和用户线程一起** 进行GC Roots跟踪的过程，和用户线程一起工作，不需要暂停工作线程。主要标记过程，标记全部对象。

**重新标记（CMS remark）** 为了修正在并发标记期间，因用户程序继续运行而导致标记产生变动的那一部分对象的标记记录，仍然需要暂停所有的工作线程。

由于并发标记时，用户线程依然运行，因此在正式清理前，再做修正。

**并发清除（CMS concurrent sweep）和用户线程一起** 清理GC Roots不可达对象，和用户线程一起工作，不需要暂停工作线程。基于标记结果，直接清理对象。

由于耗时最长的并发标记和并发清除过程中，垃圾收集线程可以和用户现在一起并发工作，所以总体上来看CMS收集器的内存回收和用户线程是一起并发执行。

**优点：并发收集低停顿**

**缺点：并发执行，对CPU资源压力大**

由于并发进行，CMS在收集与应用线程会同时增加对堆内存的占用，也就是说，**CMS必须要在老年代堆内存用尽之前完成垃圾回收，否则CMS回收失败**时，将触发担保机制，串行老年代收集器会以STW的方式进行一次GC，从而造成较大停顿时间。

**采用的标记清除算法会导致大量碎片**

标记清除算法无法整理空间碎片，老年代空间会随着应用时长被逐步耗尽，最后将不得不通过担保机制对堆内存进行压缩。CMS也提供了参数-XX:CMSFullGCsBeForeCompaction（默认0，即每次都进行内存整理）来指定多少次CMS收集之后，进行一次压缩的Full GC。


	-XX:InitialHeapSize=1048576 -XX:MaxHeapSize=1048576 -XX:MaxNewSize=0 -XX:MaxTenuringThreshold=6 -XX:OldPLABSize=16 -XX:+PrintCommandLineFlags -XX:+PrintGCDetails -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:-UseLargePagesIndividualAllocation -XX:+UseParNewGC 
	[GC (Allocation Failure) [ParNew (promotion failed): 1024K->1152K(1152K), 0.0159197 secs][CMS: 389K->544K(768K), 0.0020602 secs] 1024K->544K(1920K), [Metaspace: 2596K->2596K(1056768K)], 0.0180432 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
	[GC (Allocation Failure) [ParNew: 1024K->1024K(1152K), 0.0000159 secs][CMS: 544K->648K(768K), 0.0026485 secs] 1568K->648K(1920K), [Metaspace: 3196K->3196K(1056768K)], 0.0027148 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (CMS Initial Mark) [1 CMS-initial-mark: 648K(768K)] 660K(1920K), 0.0000795 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[CMS-concurrent-mark-start]
	[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[CMS-concurrent-preclean-start]
	[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (CMS Final Remark) [YG occupancy: 104 K (1152 K)][Rescan (parallel) , 0.0001996 secs][weak refs processing, 0.0000066 secs][class unloading, 0.0002625 secs][scrub symbol table, 0.0004360 secs][scrub string table, 0.0001198 secs][1 CMS-remark: 648K(768K)] 752K(1920K), 0.0010717 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[CMS-concurrent-sweep-start]
	[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[CMS-concurrent-reset-start]
	[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [ParNew: 859K->859K(1152K), 0.0000129 secs][CMS: 648K->679K(768K), 0.0021152 secs] 1507K->889K(1920K), [Metaspace: 3205K->3205K(1056768K)], 0.0021642 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (CMS Initial Mark) [1 CMS-initial-mark: 679K(768K)] 1309K(1920K), 0.0000735 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[CMS-concurrent-mark-start]
	[Full GC (Allocation Failure) [CMS[CMS-concurrent-mark: 0.000/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	 (concurrent mode failure): 679K->574K(768K), 0.0021473 secs] 1570K->809K(1920K), [Metaspace: 3217K->3217K(1056768K)], 0.0021764 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [ParNew (promotion failed): 887K->1009K(1152K), 0.0076821 secs][CMS: 577K->574K(768K), 0.0022248 secs] 1462K->1229K(1920K), [Metaspace: 3217K->3217K(1056768K)], 0.0099483 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
	[Full GC (Allocation Failure) [CMS: 574K->574K(768K), 0.0016514 secs] 1229K->1229K(1920K), [Metaspace: 3217K->3217K(1056768K)], 0.0016782 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
		at java.util.Arrays.copyOf(Arrays.java:3332)
		at java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:124)
		at java.lang.AbstractStringBuilder.append(AbstractStringBuilder.java:674)
		at java.lang.StringBuilder.append(StringBuilder.java:208)
		at GCRootDemo.main(GCRootDemo.java:12)
	Heap
	 par new generation   total 1152K, used 697K [0x00000000ffe00000, 0x00000000fff40000, 0x00000000fff40000)
	  eden space 1024K,  68% used [0x00000000ffe00000, 0x00000000ffeae668, 0x00000000fff00000)
	  from space 128K,   0% used [0x00000000fff00000, 0x00000000fff00000, 0x00000000fff20000)
	  to   space 128K,   0% used [0x00000000fff20000, 0x00000000fff20000, 0x00000000fff40000)
	 concurrent mark-sweep generation total 768K, used 574K [0x00000000fff40000, 0x0000000100000000, 0x0000000100000000)
	 Metaspace       used 3249K, capacity 4496K, committed 4864K, reserved 1056768K
	  class space    used 352K, capacity 388K, committed 512K, reserved 1048576K


**UseParNewGC（YOUNG）**

![](http://static.oschina.net/uploads/space/2015/0811/220617_gHTm_2431292.png)

**ParNew（并行）收集器**

一句话：使用多线程进行垃圾回收，在垃圾收集时，会Stop-the-World暂停其他所有的工作线程直到它收集结束。

ParNew收集器其实就是Serial收集器新生代的并行多线程版本，最常见的应用场景是配合老年代的CMS GC工作，其余的行为和Serial收集器完全一样，ParNew垃圾收集器在垃圾收集过程中同样也要暂停所有其他的工作线程。它是很多java虚拟机运行在Server模式下新生代的默认垃圾收集器。


常用对应JVM参数：

-XX:+UseParNewGC 启用ParNew收集器，只影响新生代的收集，不影响老年代。

开启上述参数后，会使用：ParNew（Young区用）+Serial Old的收集器组合，新生代使用复制算法，老年代采用标记-整理算法。（注意JAVA8开始JVM会提示warning老年区已废弃用Serial old，老年区应该配合使用CMS模式）


	//-Xmx1m -Xms1m -XX:+PrintGCDetails -XX:+PrintCommandLineFlags -XX:+UseParNewGC
	-XX:InitialHeapSize=1048576 -XX:MaxHeapSize=1048576 -XX:+PrintCommandLineFlags -XX:+PrintGCDetails -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:-UseLargePagesIndividualAllocation -XX:+UseParNewGC 
	[GC (Allocation Failure) [ParNew: 508K->64K(576K), 0.0312197 secs] 508K->510K(1984K), 0.1000292 secs] [Times: user=0.00 sys=0.00, real=0.10 secs] 
	[GC (Allocation Failure) [ParNew: 568K->64K(576K), 0.0290853 secs] 1014K->674K(1984K), 0.0291227 secs] [Times: user=0.03 sys=0.00, real=0.03 secs] 
	Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
		at java.util.Arrays.copyOf(Arrays.java:3332)
		at java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:124)
		at java.lang.AbstractStringBuilder.append(AbstractStringBuilder.java:674)
		at java.lang.StringBuilder.append(StringBuilder.java:208)
		at GCRootDemo.main(GCRootDemo.java:12)
	Java HotSpot(TM) 64-Bit Server VM warning: Using the ParNew young collector with the Serial old collector is deprecated and will likely be removed in a future release
	[GC (Allocation Failure) [ParNew: 576K->64K(576K), 0.0006078 secs] 1186K->740K(1984K), 0.0006357 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [ParNew: 541K->62K(576K), 0.0003254 secs] 1218K->869K(1984K), 0.0003532 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [ParNew: 543K->3K(576K), 0.0004125 secs] 1351K->986K(1984K), 0.0004344 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [ParNew: 327K->4K(576K), 0.0003595 secs] 1311K->1197K(1984K), 0.0003744 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [ParNew: 433K->433K(576K), 0.0000238 secs][Tenured: 1193K->1191K(1408K), 0.0473848 secs] 1627K->1191K(1984K), [Metaspace: 3131K->3131K(1056768K)], 0.0474629 secs] [Times: user=0.00 sys=0.00, real=0.05 secs] 
	[GC (Allocation Failure) [ParNew: 429K->429K(576K), 0.0000099 secs][Tenured: 1191K->1076K(1408K), 0.0016947 secs] 1621K->1076K(1984K), [Metaspace: 3131K->3131K(1056768K)], 0.0017447 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
	[GC (Allocation Failure) [ParNew: 440K->440K(576K), 0.0000079 secs][Tenured: 1076K->1286K(1408K), 0.0015666 secs] 1516K->1286K(1984K), [Metaspace: 3131K->3131K(1056768K)], 0.0015991 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	[Full GC (Allocation Failure) [Tenured: 1286K->1216K(1408K), 0.0017067 secs] 1286K->1216K(1984K), [Metaspace: 3131K->3131K(1056768K)], 0.0017255 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
	Heap
	 par new generation   total 576K, used 95K [0x00000000ffe00000, 0x00000000ffea0000, 0x00000000ffea0000)
	  eden space 512K,  18% used [0x00000000ffe00000, 0x00000000ffe17ec0, 0x00000000ffe80000)
	  from space 64K,   0% used [0x00000000ffe80000, 0x00000000ffe80000, 0x00000000ffe90000)
	  to   space 64K,   0% used [0x00000000ffe90000, 0x00000000ffe90000, 0x00000000ffea0000)
	 tenured generation   total 1408K, used 1216K [0x00000000ffea0000, 0x0000000100000000, 0x0000000100000000)
	   the space 1408K,  86% used [0x00000000ffea0000, 0x00000000fffd0158, 0x00000000fffd0200, 0x0000000100000000)
	 Metaspace       used 3229K, capacity 4496K, committed 4864K, reserved 1056768K
	  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K

**UseParallelOldGC(OLD)**

Parallel Old收集器是Parallel Scavenge的老年代版本，使用多线程的标记-整理算法，Parallel Old收集器在JDK1.6才开始提供。

在JDK1.6之前，新生代使用ParallelScavenge收集器只能搭配老年代的Serial Old收集器，只能保证新生代的吞吐量优先，无法保证整体的吞吐量。在JDK1.6之前（Parallel Scavenge+Serial Old）

Parallel Old正是为了在老年代同样是吞吐量优先的垃圾收集器，如果系统对吞吐量要求比较高，JDK1.8后可以优先考虑新生代Parallel Scavenge和老年代Parallel Old收集器的搭配策略。在JDK1.8及后（Parallel Scavenge+Parallel Old）

JVM常用参数：

-XX:+UseParallel Old 使用Parallel Old收集器，设置该参数后，新生代Parallel+老年代Parallel Old

（可参考UseParallelGC）

**UseG1GC**



**如何选择垃圾收集器**

**组合的选择**

**单CPU或小内存，单机程序 -XX:UseSerialGC**

**多CPU,需要最大吞吐量，如后台计算型应用 -XX:+UseParallelGC或者-XX:+UseParallelOldGC**

**多CPU,追求停顿时间，需快速响应如互联网应用 -XX:+UseConsMarkSweepGC -XX:+ParNewGC**


	参数 							新生代垃圾收集器 		新生代算法 	老年代垃圾回收器 	老年代算法
	-XX:+UseSerialGC					SerialGC		复制			SerialOldGC			标整
	-XX:+UseParNewGC					ParNew			复制			SerialOldGC			标整
	-XX:+UseParallelGC					Parallel[Scavenge] 复制		Parallel Old		标整
	/-XX:+UseParallelOldGC
	-XX:+UseConcMarkSweepGC				ParNew	复制			CMS+Serial Old的收集器组合（Serial Old作为CMS出错的后备收集器	标清
	-XX:+UseG1GC				G1整体上采用标记-整理算法	局部是通过复制算法，不会产生内存碎片



# G1垃圾收集器 #

以前收集器特点

- 年轻代和老年代是各自独立且连续的内存块
- 年轻代收集使用单eden+s0+s1进行复制算法
- 老年代收集必须扫描整个老年代区域
- 都是以尽可能少而快速地执行GC为设计原则。

**G1是什么**

G1（Garbage-First）收集器，是一款面向服务端应用的收集器，应用在多处理器和大容量内存环境中，在实现高吞吐量的同时，尽可能的满足垃圾收集暂停时间的要求。另外还具有以下特性：

- 像CMS收集器一样，能与应用程序线程并发执行。
- 整理空间更快
- 需要更多的时间来预测GC停顿时间
- 不希望牺牲大量的吞吐性能
- 不需要更大的Java Heap


G1收集器的设计目标是取代CMS收集器，它同CMS相比，在以下方面表现的更出色：

- G1是一个有整理内存过程的垃圾收集器，不会产生很多内存碎片。
- G1的Stop The World（STW）更可控，G1在停顿时间上添加了预测机制，用户可以指定期望停顿时间。


**G1特点**

1. G1能充分利用多CPU、多核环境硬件优势，尽量缩短STW
2. G1整体上采用标记-整理算法，局部是通过复制算法，不会产生内存碎片
3. 宏观上看G1之中不再区分年轻代和老年代。**把内存划分成多个独立的子区域（Region）**，可以近似理解为一个围棋的棋盘。
4. G1收集器里面整个的内存区都混合在一起了，**但其本身依然在小范围内要进行年轻代和老年代的区分**，保留了新生代和老年代。但它们不再是物理隔离的，而是一部分Region的集合且不需要Region是连续的，也就是说依然会采用不同的GC方式来处理不同的区域
5. G1虽然也是分代收集器，但整个内存分区**不存在物理上的**年轻代与老年代的区别，也不需要完全独立的survivor(to space)堆做复制准备。**G1只有逻辑上的分代概念**，或者说每个分区都可能随G1的运行在不同代之间前后切换

**底层原理**

**Region区域化垃圾收集器**

最大好处是化整为零，避免全内存扫描，只需要按照区域来进行扫描即可。

区域化内存划片Region，整体编为一系列不连续的内存区域，避免了全内存的GC操作。

核心思想

是将整个堆内存区域分成大小相同的子区域（Region），在JVM启动时会自动设置这些子区域的大小。

在堆的使用上，G1并不要求对象的存储一定是物理上连续的只要逻辑上连续即可，每个分区也不会固定地为某个代服务，可以按需在年轻代和老年代之间切换。启动时可以通过参数-XX:G1HeapRegionSize=n可指定分区大小（1MB-32MB，且必须是2的幂），默认将整个堆划分为2048个分区。
大小范围在1MB-32MB，最多能设置2048个区域。也即能够支持的最大内存为：32MB*2048=65536MB=64G内存。

G1将新生代、老年代的物理空间取消了。

![](https://pic4.zhimg.com/80/v2-ee61e14749bea8da918e1070148cdbc3_hd.jpg)

G1算法将堆划分为若干个区域（Region），它仍然属于分代收集器

![](https://pic2.zhimg.com/80/v2-27dcb4e34d844af8361672275d9d5d1d_hd.jpg)

这些Region的一部分包含新生代，新生代的垃圾收集依然采用暂停所有应用线程的方式，将存活对象拷贝到老年代或者Survivor空间。

这些Region的一部分包含老年代，G1收集器通过将对象从一个区域复制到另一个区域，完成了清理工作。这就意味着，在正常的处理过程中，G1完成了堆的压缩（至少是部分堆的压缩），这样也就不会有CMS内存碎片问题的存在了。

在G1中，还有一种特殊的区域，叫Humongous（巨大的）区域。如果一个对象占用的空间超过了分区容量的50%以上，，G1收集器就认为这是一个巨型对象。这些巨型对象默认直接会被分配在老年代，但是如果它是一个短期存在的巨型对象，就会对垃圾收集器造成负面影响。为了解决这个问题，G1划分了一个Humongous区，它用来专门存放巨型对象。如果一个H区装不下一个巨型对象，那么G1会寻找连续的H分区来存储。为了能找到连续的H区，有时候不得不启动Full GC。

**回收步骤**

G1收集器下的Young GC

![](https://pic2.zhimg.com/80/v2-7c45eb6296e39ecb56a706a3723f2ccd_hd.jpg)
![](https://pic3.zhimg.com/80/v2-d43ba280b215143b38c1339c457827fa_hd.jpg)

- 针对Eden区进行收集，Eden区耗尽后会被触发，主要是小区域收集+形成连续的内存块，避免内存碎片。
- Eden区的数据移动到新的Survivor区，部分数据晋升到Old区。
- Survivor区的数据移动到新的Survivor区，部分数据晋升到Old区。
- 最后Eden区收集干净了，GC结束，用户的应用程序继续执行。

**4步过程：**

![](https://images2018.cnblogs.com/blog/563799/201805/563799-20180510163645191-1911681386.png)

1. 初始标记：只标记GC Roots能直接关联到的对象
2. 并发标记：进行GC Roots Tracing的过程
3. 最终标记：修正并发标记期间，因程序运行导致标记发生变化的那一部分对象
4. 筛选回收：根据时间来进行价值最大化的回收。

**常用配置参数（了解）**

开发人员仅仅声明一下参数即可：
三步归纳：开始G1+设置最大内存+设置最大停顿时间

	-XX:+UseG1GC -Xmx32g  -XX:MaxGCPauseMillis=100

**注意：一般常用的都是前三个，后面三个基本用默认就可以了**

**-XX:+UseG1GC**

**-XX:G1HeapRegionSize=n**:设置的G1区域的大小。值是2的幂，范围是1MB到32MB。目标是根据最小的Java堆大小划分出约2048个区域。

**-XX:MaxGCPauseMillis=n**：最大GC停顿时间（单位毫秒），这是个软目标，JVM将尽可能（但不保证）停顿小于这个时间。

**-XX:InitiatingHeapOccupancyPercent=n**：堆占用了多少的时候就触发GC，默认为45

**-XX:ConcGCThreads=n**：并发GC使用的线程数。

**-XX:G1ReservePercent=n**：设置作为空闲的预留内存百分比，以降低目标空间溢出的风险，默认值是10%

**和CMS相比的优势**

1. G1不会产生内存碎片
2. 是可以精确控制停顿。该收集器是把整个堆（新生代、老生代）划分成多个固定大小的区域，每次根据允许停顿时间去收集垃圾最多的区域。


**最后关于结合实际应用SpringBoot微服务的调优**

**公式：java -server jvm各种参数 -jar jar/war包名**

**例：java -server -Xmx1024m -Xms1024m -XX:+UseG1GC -jar 包名**