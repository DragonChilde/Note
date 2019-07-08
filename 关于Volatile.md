# Volatile #

volatile是Java虚拟机提供的轻量级的同步机制

1. 保证可见性
2. 不保证原子性
3. 禁止指令重排

JVM(Java虚拟机)

JMM(Java内存模型)要求保证：

1. 可见性
1. 原子性
1. 有序性

JMM（Java内存模型 Java Memory Model，简称JMM）本身是一种抽象的概念并不真实存在
，它描述的是一组规则或规范，通过这组规范定义了程序中各个变量（包括实例字段，静态字段和构成数组对象的元素）的访问方式。

JMM关于同步的规定：
1.线程解锁前，必须把共享变量的值刷新回主内存。
2.线程加锁前，必须读取主内存的最新值到自己的工作内存。
3.加锁解锁是同一把锁。

由于JVM运行程序的实体是线程，而每个线程创建时JVM都会为其创建一个工作内存（有些地方称为栈空间），工作内存是每个线程的私有数据区域，而Java内存模型中规定所有变量都存储到
主内存（相当于硬件里的内存），主内存是共享内存区域，所有线程都可以访问，但线程对变量的操作（读取、复制等）必须在工作内存中进行，首先要将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存，不能直接操作主内存中的变量，各个线程中的工作内存中存储着主内存中的变量副本拷贝
，因此不同的线程间无法访问对方的工作内存，线程间的通信（传值）必须通过主内存来完成

**一关于可见性**

	/**当不使用关键字时volatile,main进程一直处在循环当中，因为aaa线程执行更新后是不会通知main主线程主内存的值已经修改,可见性也是指线程的通知，**/

	public class VolatileDemo {
    public static void main(String[] args) {
        Memory m = new Memory();
        new Thread(()->{
            System.out.println(Thread.currentThread().getName()+" start!");
            System.out.println(Thread.currentThread().getName()+" sleep 3sec");
            try {
                TimeUnit.MILLISECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            m.setNumber();
            System.out.println(Thread.currentThread().getName()+" set memory number is "+m.number);
        },"aaa").start();

        System.out.println("main thread start!");
        while (m.number ==0){

        }
        System.out.println("main thread is over: "+m.number);
    }
	}
	
	/**
	*       1 验证volatile的可见性
	*       1.1 加入int number=0，number变量之前根本没有添加volatile关键字修饰,没有可见性
	*       1.2 添加了volatile，可以解决可见性问题
	**/
	class Memory{
	    volatile int number =0;
	    public void setNumber(){
	        this.number = 30;
	    }
	}

	
**二 关于原子性的问题**

多个线获取其变量进行更新操作时，因为取到的值是在各自的工作内存里进行操作，再更新回主内存，因为会出现无序的情况，导致部份线程出现了提前的操作导致数值不一致

	/**
	 *
	 * 2 验证volatile不保证原子性
	 *
	 *     2.1 原子性是不可分割，完整性，也即某个线程正在做某个具体业务时，中间不可以被加塞或者分割。
	 *     需要整体完成，要么同时成功，要么同时失败。
	 *
	 *     2.2 volatile不可以保证原子性演示
	 *
	 *	3 如何保证原子性一致
			1加sync
			2使用我们的JUC下AtomicInteger
	 * */
	    public static void main(String[] args) {
	        Memory memory = new Memory();
	        for (int i = 1; i <= 20; i++) {
	            new Thread(()->{
	                for (int j = 1; j <= 1000; j++) {
	                    memory.addNumberPlus();
						memory.addAtomicInteger();
	                }
	            },String.valueOf(i)).start();
	        }
	
	        //活跃线程自少有2个，一个是main线程，一个是GC，判断当前活跃线程
	        while (Thread.activeCount()>2){
	            //如果线程数大于2时，主线程一直处于阻塞状态，调度其他线程执行
	            Thread.yield();
	        }
			/**最终number的值是不可能为20000*/
	        System.out.println(Thread.currentThread().getName()+" final get number is "+memory.number);
			/**最终atomicInteger的值是一直为20000*/
			System.out.println(Thread.currentThread().getName()+"atomicInteger type final get number is "+memory.atomicInteger);
	    }
	
	}
	
	class Memory{
	    volatile int number =0;
	    public void setNumber(){
	        this.number = 30;
	    }
	
	    //当加了volatile关键字后，验证原子性不一致的情况
	    public void addNumberPlus(){
	        this.number++;
	    }

		//使用java.util.concurrent.atomic.Atomic***保证原子性一致
		 AtomicInteger atomicInteger = new AtomicInteger();

	    public void addAtomicInteger(){
	        atomicInteger.getAndIncrement();
	    }
	}


**三 关于指令重排**（计算机在执行程序时，为了提高性能，编译器和处理器常常会对指令做重排，一般分一下3种：源代码->
编译器优化的重排->指令并行的重排->内存系统的重排->最终执行的指令）

单线程环境里面确保程序最终执行结果和代码顺序执行的结果一致。（单线程不会出现指令重排）

处理器在进行重排序时必须考虑指令之间的数据依赖性。（数据之前没有依赖关系会进行重排）

多线程环境中线程交替执行，由于编译器优化重排的存在，两个线程中使用的变量能否保证一致性是无法确定的，结果无法预测。

	/**情况一**/
	int x=y=a=b=0;
	//线程1
	a=x;
	x=1;
	//线程2
	b=y;
	y=2
	//在禁止指令重排的清况下,按顺序执行 a,b=0
	//但当在多线程的执行情况下，指令重排，有机会先执行x=1,y=2,结果a=1,b=2造成数据混乱不一致


	/**情况二**/
	class Rearrangement{
	    int num =0;
	    boolean flag = false;
	    
	    public void method1(){
	        num = 1;            //语句1
	        flag = true;        //语句2
	    }
	    public void method2(){
	        if (flag = true){
	            num +=5;         //语句3
	            System.out.println("num is "+num);
	        }
	    }
	}
	//在单线程的情况下按顺序执行最终num为6
	//但多线程执行时有可能语句2->语句3->语句1,最终numy为5（语句1和语句2必没有依赖关系）

**线程安全获得保证**

1.工作内存和主内存同步延迟现象导致的可见性问题可以使用synchronized或volatile关键字解决，他们都可以使一个线程
修改后的变量立即对其他线程可见。

2.对于指令重排导致的可见性问题和有序性问题可以利用volatile关键字解决，因为volatile的另外一个作用就是禁止重排序优化。

**在哪些地方用过volatile？（单例模式DCL代码）**

DCL（双端检锁）机制不一定线程安全，原因是有指令重排序的存在，加入volatile可以禁止指令重排。

原因在于某一个线程执行到第一个检测，读取到的instance不为null时，instance的引用对象
可能没有完成初始化。

指令重排只会保证串行语义的执行一致性（单线程不需考虑重排问题），但并不会关心多线程间的语义一致性。

实例实现过程:

1. 先分配内存空间
2. 初始化instance=null
3. 把instance指向新new出来的内存地址

上面是正常的执行顺序，但多线程会指令重排1->3->2
所以当一条线程访问instance不为null时，由于instance实例未必已初始化完成，也就造成了线程安全问题。

		/**单例模式DCL代码**/
		public class SingleDemo {
		    protected static volatile SingleDemo instance = null;
		
		    protected SingleDemo() {
		        System.out.println(Thread.currentThread().getName()+" SingleDemo is consturct");
		    }
		
		  	 /**
		    * DCL(Double Check Lock)双端检锁机制
		    **/
		    public static SingleDemo getInstance(){
		        if (instance ==null){
		            synchronized (SingleDemo.class) {
		                if (instance==null) {
		                    instance = new SingleDemo();
		                }
		
		            }
		        }
		        return instance;
		    }
		}
		
		class Single{
		    public static void main(String[] args) {
		
		        for (int i = 1; i < 10; i++) {
		            new Thread(()->{
		                SingleDemo singleDemo = SingleDemo.getInstance();
		            },String.valueOf(i)).start();
		
		        }
		    }
		}