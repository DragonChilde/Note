# 关于CAS #
CAS的全称是Compare-And-Swap(比较并交换)，它是一条CPU并发原语。

它的功能是判断内存某个位置的值是否为预期值，如果是则更改为新的值，这个过程是原子的。

CAS并发原语体现在JAVA语言中就是sun.misc.Unsafe类中的各个方法。调用UnSafe类中CAS方法，JVM会帮我们实现
CAS汇编指令。这是一种完全依赖于硬件的功能，通过它实现原子小左。再次强调，由于CAS是一种系统原语，原语属于操作系统用语范畴，是由若干指令组成的，用语完成某个功能的一个过程，并且原语的执行必须是连续的，在执行过程中不允许被中断，也就是说CAS是一条CPU的原子指令，不会造成所谓的数据不一致性问题。

	public class CASDemo {
	    public static void main(String[] args) {
	        AtomicInteger atomicInteger = new AtomicInteger(5);
	        System.out.println(atomicInteger.compareAndSet(5, 2019)+" current data :"+atomicInteger.get());
	        System.out.println(atomicInteger.compareAndSet(5, 9102)+" current data :"+atomicInteger.get());
			/**
			**true current data :2019
			**false current data :2019
			**/
	    }
	}


**CAS原理:调用了Unsafe类（这是JAVA底层的执行类）**

1. Unsafe是CAS的核心类，由于Java方法无法直接访问底层系统，需要通过本地（native）方法来访问，Unsafe相当于一个后门，基于该类可以直接操作特定内存的数据。
Unsafe类存在于sun.misc包中，其内部方法操作可以像C的指针一样直接操作内存，因为Java中CAS操作的执行依赖于Unsafe类的方法。

注意Unsafe类中的所有方法都是native修饰的，也就是说Unsafe类中的方法都直接调用操作系统底层资源执行相应任务。

2. 变量valueOffset，表示该变量在内存中的偏移地址，因为Unsafe就是根据内存偏移地址获取数据的。

3. 变量value用volatile修饰，保证了多线程之间的内存可见性。

	
		/**java.util.concurrent.atomic.AtomicInteger源码分析**/
		 private volatile int value;	//注意这里用了volatile关键字，可见性
		  /**
	     * Atomically increments by one the current value.
	     *
	     * @return the previous value
	     */
	    public final int getAndIncrement() {
			/**
			**	this 指当前对象
			**	valueOffset 内存偏移量
			**	初始值1
			**/
	        return unsafe.getAndAddInt(this, valueOffset, 1);
	    }
		
		/**sun.misc.Unsafe源码分析***/
		/**
		 **var1 类名
		 **var2 内存偏移量
		 **var4 默认值1
		 **var5 主内存中真实的值
		 ** 首先根据getIntVolatile(类名,内存偏移量)方法查找出对应的值
		 ** 比较并交换判断是刚查出的值是否有修改过，如果不同返回FALSE继续根据类名和内存偏移量重新查找新的值，相同话更新操作返回TRUE退出循环
		 ************************/
		 public final int getAndAddInt(Object var1, long var2, int var4) {
	        int var5;
	        do {
	            var5 = this.getIntVolatile(var1, var2);
	        } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));
	
	        return var5;
	    }
实例情况：

假设线程A和线程B两个线程同时执行getAndAddInt操作（分别泡在不同CPU上）

1、 AtomicInteger里面的value原始值为3，即主内存中AtomicInteger的value为3，根据JMM模型，线程A和线程B各自持有一份值为3的value的副本分别到各自的工作内存。

2、线程A通过getIntVolatile（var1，var2）拿到value值3，这是线程A被挂起。

3、线程B也通过getIntVolatile（var1，var2）方法获得value值3，此时刚好线程B没有被挂起并执行compareAndSwap方法比较内存值也为3，成功修改内存值为4，线程B打完收工，一切OK。

4、这是线程A回复，执行compareAndSwapInt方法比较，发现手里的值3与内存值4不一致，说明该值已经被其他线程抢险异步修改过了，那A线程本次修改失败，只能重新读取重新来一遍了。

5、线程A重新获取value值，因为变量value被volatile修饰，所以其他线程对它的修改，线程A总是能够看到，线程A继续执行compareAndSwapInt进行比较替换，知道成功。

**CAS有3个操作数，内存值V，旧的预期值A，要修改的更新值B。
当且仅当预期值A和内存值V相同时，将内存值V修改为B，否则什么都不做。**


**CAS缺点**

1.循环时间长开销大

如果CAS失败，会一直进行尝试。如果CAS长时间一直不成功，可能会给CPU带来很大的开销。

2.只能保证一个共享变量的原子操作

当对一个共享变量执行操作时，我们只能使用循环CAS的方式来保证原子操作，但是
，对多个共享变量操作时，循环CAS就无法保证操作的原子性，这个时候就可以用锁来保证原子性。

3.ABA问题

CAS算法实现一个重要前提需要取出内存中某时刻的数据并在当下时刻比较并替换，那么在这个
时间差
类会导致数据的变化。

线程A执行需要10s,线程B执行才需要2s,当线程B执行多次BBBBA的更新主内存的变量操作，线程A才执行更新A的操作，发现变量值没有变更是A，对比没错可以更新，但其实线程B已经在其过程中执行了多次更新操作，这是有问题的

**解决ABA问题**(原子引用+时间戳的方式)

	/**原子引用**/
	public class AtomicReferenceDemo {

    public static void main(String[] args) {
        AtomicReference<User> atomicReference = new AtomicReference<>();
        User user = new User(10,"tom");
        User jack = new User(20, "jack");
        atomicReference.set(user);

        System.out.println(atomicReference.compareAndSet(user, jack)+" user data :"+atomicReference.get().toString());

		System.out.println(atomicReference.compareAndSet(user, jack)+" user data :"+atomicReference.get().toString());
		
		/**
		*true user data :User@4554617c
		*false user data :User@4554617c
		**/
    }
	}

	class User{
	    int age;
	    String name;
	
	    public User(int age, String name) {
	        this.age = age;
	        this.name = name;
	    }
	}




	/****时间戳的原子引用*******/
	/** t1线程执行了2次更新操作100->101->100 t2 过了5秒才执行发现原值没变可能执行更新操作100->2019，这就是ABA的问题
	 ** t3线程每次更新都会引入一个版本号的概念（相当于乐观锁，每次更新都会更新版本号）,进行了2次操作，版本号变为3
	 ** t4线和初始化后过了5秒才进行更新操作以为版本号还是1，当进行更新时会更新失败
	 **/
	public class ABADemo {
	    static AtomicReference<Integer> atomicReference =  new AtomicReference<>(100);
	    static AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(100,1);
	
	    public static void main(String[] args) {
	        new Thread(()->{
	            atomicReference.compareAndSet(100,101);
	            atomicReference.compareAndSet(101,100);
	        },"t1").start();
	
	        new Thread(()->{
	            try {
	                TimeUnit.MILLISECONDS.sleep(5);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	            System.out.println(atomicReference.compareAndSet(100, 2019) + " t2 update is "+atomicReference.get());
	        },"t2").start();
			/**true t2 update is 2019**/

	        System.out.println("====================解决ABA===================");
	
	        new Thread(()->{
	
	            int stamp = atomicStampedReference.getStamp();
	
	            System.out.println(Thread.currentThread().getName()+"第一次版本号为:"+stamp);
	            atomicStampedReference.compareAndSet(100,101,stamp,stamp+1);
	            System.out.println(Thread.currentThread().getName()+" 第二次版本号为:"+atomicStampedReference.getStamp());
	
	            atomicStampedReference.compareAndSet(101,100,atomicStampedReference.getStamp(),atomicStampedReference.getStamp()+1);
	            System.out.println(Thread.currentThread().getName()+" 第三次版本号为:"+atomicStampedReference.getStamp());
	
	
	        },"t3").start();
	
	        new Thread(()->{
	
	            int stamp = atomicStampedReference.getStamp();
	
	            System.out.println(Thread.currentThread().getName()+"第一次版本号为:"+stamp);
	
	            try {
	                TimeUnit.MILLISECONDS.sleep(5);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	
	            boolean result = atomicStampedReference.compareAndSet(100, 2019, stamp, stamp + 1);
	              System.out.println(Thread.currentThread().getName()+"修改版本号为:"+(stamp + 1)+result);
	        },"t4").start();

			/**
			**	t3第一次版本号为:1
			**	t4第一次版本号为:1
			**	t3 第二次版本号为:2
			**	t3 第三次版本号为:3
			**	t4修改版本号为:2 false
			**/
	    }
	}
