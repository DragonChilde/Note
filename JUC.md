# JUC #
内存可见性问题是，当多个线程操作共享数据时，彼此不可见

volatile关键字:当多个线程操作共享数据时，可以保证内存中的数据可见

相较于synchronized是一种较为轻量级的同步策略

注意：
	1.volatile不具备"互斥性"
	2.不能保证变量的"原子性"
					
i++原子性问题:i++的操作实际上分为三个步骤"读-改-写"
	int i = 10;
	i = i++;	//10
	
	int temp =i;
	i = i +1;
	i = temp;
原子变量:jdk1.5后java.util.concurrent.atomic包下提供了常用的原子变量:

1.volatile保证内存可见性

2.CAS(Compare-And-Swap)算法保证数据的原子性
	- CAS算法是硬件对于并发操作共享数据的支持
	- 
	- 	CAS包含了三个操作数：

	- 		内存值V
	- 		预付值A
	- 		更新值B
	- 		当且仅当V==A时，V=B.否则，将不做任何操作
				
**CopyOnWriteArrayList/CopyOnWriteArraySet:**

`CopyOnWrite`容器即写时复制的容器。通俗的理解是当我们往一个容器添加元素的时候，不直接往当前容器添加，而是先将当前容器进行Copy，复制出一个新的容器，然后新的容器里添加元素，添加完元素之后，再将原容器的引用指向新的容器。这样做的好处是我们可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素。所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器。
 “写入并复制”
注意：添加操作多时，效率低，因为每次添加时都会进行复制，内存开销非常的大。并发迭代操作多时可以选择。

CopyOnWrite并发容器用于读多写少的并发场景。比如白名单，黑名单，商品类目的访问和更新场景，假如我们有一个搜索网站，用户在这个网站的搜索框中，输入关键字搜索内容，但是某些关键字不允许被搜索。这些不能被搜索的关键字会被放在一个黑名单当中，黑名单每天晚上更新一次。当用户搜索时，会检查当前关键字在不在黑名单当中，如果在，则提示不能搜索

CopyOnWrite容器只能保证数据的最终一致性，不能保证数据的实时一致性。所以如果你希望写入的的数据，马上能读到，请不要使用CopyOnWrite容器。


**CountDownLatch:**闭锁，在完成某些运算时，只有其它所有线程的运算全部完成，当前运算才继续执行

构造器中的计数值（count）实际上就是闭锁需要等待的线程数量。这个值只能被设置一次，而且CountDownLatch没有提供任何机制去重新设置这个计数值。

与CountDownLatch的第一次交互是主线程等待其他线程。主线程必须在启动其他线程后立即调用CountDownLatch.await()方法。这样主线程的操作就会在这个方法上阻塞，直到其他线程完成各自的任务。

其他N 个线程必须引用闭锁对象，因为他们需要通知CountDownLatch对象，他们已经完成了各自的任务。这种通知机制是通过 CountDownLatch.countDown()方法来完成的；每调用一次这个方法，在构造函数中初始化的count值就减1。所以当N个线程都调 用了这个方法，count的值等于0，然后主线程就能通过await()方法，恢复执行自己的任务。


	public class TestCountDownLatch {
	    public static void main(String[] args) {
	        CountDownLatch countDownLatch = new CountDownLatch(10);
	        PrintNum printNum = new PrintNum(countDownLatch);
	        for (int i = 0; i < 10; i++) {
	            new Thread(printNum).start();
	        }
	        try {
	            countDownLatch.await();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        System.out.println("ok");
	    }
	}
	
	class PrintNum implements Runnable{
	    private CountDownLatch countDownLatch;
	    PrintNum(CountDownLatch count){
	        countDownLatch = count;
	    }
	
	    @Override
	    public void run() {
	        try {
	            for (int i = 0; i < 50000; i++) {
	                if (i%2==0) System.out.println(i);
	            }
	        } finally {
	            countDownLatch.countDown();
	        }
	    }
	}

在实时系统中的使用场景：

1. 实现最大的并行性：有时我们想同时启动多个线程，实现最大程度的并行性。例如，我们想测试一个单例类。如果我们创建一个初始计数为1的CountDownLatch，并让所有线程都在这个锁上等待，那么我们可以很轻松地完成测试。我们只需调用 一次countDown()方法就可以让所有的等待线程同时恢复执行。
2. 开始执行前等待n个线程完成各自任务：例如应用程序启动类要确保在处理用户请求前，所有N个外部系统已经启动和运行了。
3. 死锁检测：一个非常方便的使用场景是，你可以使用n个线程访问共享资源，在每次测试阶段的线程数目是不同的，并尝试产生死锁。


CyclicBarrier 相较于CountDownLatch,可以多次重用

1. CountDownLatch和CyclicBarrier都能够实现线程之间的等待，只不过它们侧重点不同：
　　 　CountDownLatch一般用于某个线程A等待若干个其他线程执行完任务之后，它才执行；而CyclicBarrier一般用于一组线程互相等待至某个状态，然后这一组线程再同时执行；另外，CountDownLatch是不能够重用的，而CyclicBarrier是可以重用的。

2. Semaphore其实和锁有点类似，它一般用于控制对某组资源的访问权限。

		public class TestSemaphore {
		    private final static int worker = 5;
		
		    public static void main(String[] args) {
		
		        ExecutorService executorService = Executors.newCachedThreadPool();
		        Work work = new Work();
		        for (int i = 0; i < 8; i++) {
		            executorService.execute(work);
		        }
		        executorService.shutdown();
		    }
		}
		
		class Work implements Runnable{
		
		    private Semaphore semaphore = new Semaphore(5);
		
		
		    @Override
		    public void run() {
		        if (!Thread.interrupted()){
		            try {
		                semaphore.acquire();//获取许可
		                System.out.println(Thread.currentThread().getName()+" start work!");
		                TimeUnit.MILLISECONDS.sleep(2000);
		                System.out.println(Thread.currentThread().getName()+" finsh work!");
		                semaphore.release();//释放一个许可
		            } catch (InterruptedException e) {
		                e.printStackTrace();
		            }
		
		        }
		    }
		}
- pool-1-thread-1 start work!
- pool-1-thread-4 start work!
- pool-1-thread-2 start work!
- pool-1-thread-5 start work!
- pool-1-thread-6 start work!
- pool-1-thread-4 finsh work!
- pool-1-thread-1 finsh work!
- pool-1-thread-3 start work!
- pool-1-thread-8 start work!
- pool-1-thread-2 finsh work!
- pool-1-thread-5 finsh work!
- pool-1-thread-6 finsh work!
- pool-1-thread-7 start work!
- pool-1-thread-3 finsh work!
	
**DelayQueue 延时队列**

	public class Wangming implements Delayed {
	 
		private String name;
		//身份证
		private String id;
		//截止时间
		private long endTime;
		
		public Wangming(String name,String id,long endTime){
			this.name=name;
			this.id=id;
			this.endTime=endTime;
		}
		
		public String getName(){
			return this.name;
		}
		
		public String getId(){
			return this.id;
		}
		
		/**
		 * 用来判断是否到了截止时间
		 */
		@Override
		public long getDelay(TimeUnit unit) {
			// TODO Auto-generated method stub
			return endTime-System.currentTimeMillis();
		}
	 
		/**
		 * 相互批较排序用
		 */
		@Override
		public int compareTo(Delayed o) {
			// TODO Auto-generated method stub
			Wangming jia = (Wangming)o;
			return endTime-jia.endTime>0?1:0;
		}
	 
	}
	
	public class WangBa implements Runnable {
	 
		private DelayQueue<Wangming> queue = new DelayQueue<Wangming>();
		public boolean yinye =true;
		
		public void shangji(String name,String id,int money){
			Wangming man = new Wangming(name,id,1000*60*money+System.currentTimeMillis());		
			System.out.println("网名"+man.getName()+" 身份证"+man.getId()+"交钱"+money+"块,开始上机...");
			this.queue.add(man);
		}
		
		public void xiaji(Wangming man){
			System.out.println("网名"+man.getName()+" 身份证"+man.getId()+"时间到下机...");
		}
	 
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(yinye){
				try {
					System.out.println("检查ing");
					Wangming man = queue.take();
					xiaji(man);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public static void main(String args[]){
			try{
				System.out.println("网吧开始营业");
				WangBa siyu = new WangBa();
				Thread shangwang = new Thread(siyu);
				shangwang.start();
				
				siyu.shangji("路人甲", "123", 1);
				siyu.shangji("路人乙", "234", 2);
				siyu.shangji("路人丙", "345", 1);
			}
			catch(Exception ex){
				
			}
	 
		}
	}


**LinkedBlockingQueue和PriorityBlockingQueue区别**

LinkedBlockingQueue是直接遵循先进先出的顺序，元素一路从上往下被取出，而PriorityBlockingQueue则是优先级最高的元素最先被取出.

	public class PriorityTask implements Runnable,Comparable{
	    private int priority;
	
	    public PriorityTask(int priority) {
	        this.priority = priority;
	    }
	
	    @Override
	    public int compareTo(Object o) {
	        PriorityTask task = (PriorityTask)o;
	        if (task.priority == this.priority)return 0;
	        return this.priority>task.priority ? 1:-1;
	    }
	
	    @Override
	    public void run() {
	        System.out.println("priority take "+this.priority);
	    }
	
	    private static void BlockQuee(final BlockingQueue<PriorityTask> queue) throws InterruptedException{
	        Random random = new Random();
	        for (int i = 0; i < 10; i++) {
	            int nextInt = random.nextInt(1000);
	            System.out.println("random is "+nextInt);
	            queue.put(new PriorityTask(nextInt));
	        }
	
	        ExecutorService executorService = Executors.newCachedThreadPool();
	        executorService.execute(new Runnable() {
	                    @Override
	                    public void run() {
	                        if (!Thread.interrupted()){
	                            while (queue.size() > 0) {
	                                try {
	
	                                    queue.take().run();
	
	                                } catch (InterruptedException e) {
	                                    e.printStackTrace();
	                                }
	                            }
	
	                        }
	                    }
	                }
	        );
	        executorService.shutdown();
	
	
	    }
	
	    public static void main(String[] args) throws InterruptedException {
	        System.out.println("start test LinkBlockingQeque!!!!!!!!!!!");
	        BlockQuee(new LinkedBlockingQueue<PriorityTask>());
	        TimeUnit.MILLISECONDS.sleep(1000);
	        System.out.println("start test PriorityBlockingQueue");
	        BlockQuee(new PriorityBlockingQueue<PriorityTask>());
	
	    }
	}

**Exchanger**

当线程A调用Exchange对象的exchange()方法后，他会陷入阻塞状态，直到线程B也调用了exchange()方法，然后以线程安全的方式交换数据，之后线程A和B继续运行

	public class TestExchanger {
	    public static void main(String[] args) {
	        Exchanger<List<Integer>> exchanger = new Exchanger<>();
	        new Producer(exchanger).start();
	        new Consumer(exchanger).start();
	
	        System.out.println("main is end!");
	    }
	}
	
	class Producer extends Thread{
	    List<Integer> list = new ArrayList<>();
	    Exchanger<List<Integer>> exchanger = null;
	
	    public Producer(Exchanger<List<Integer>> exchanger){
	        super();
	        this.exchanger = exchanger;
	    }
	
	    @Override
	    public void run() {
	        Random random = new Random();
	
	        for (int i = 0; i < 10; i++) {
	            int nextInt = random.nextInt(100);
	            list.add(nextInt);
	            System.out.println("Producer: "+nextInt);
	        }
	        try {
	          list = exchanger.exchange(list);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	class Consumer extends Thread{
	    List<Integer> list = new ArrayList<>();
	    Exchanger<List<Integer>> exchanger = null;
	
	    public Consumer(Exchanger exchanger){
	        super();
	        this.exchanger = exchanger;
	    }
	
	    @Override
	    public void run() {
	        try {
	            list = exchanger.exchange(list);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        for (int i = 0; i < 10; i++) {
	
	            Integer integer = list.get(i);
	            System.out.println("Consumer: "+integer);
	        }
	    }
	}

- main is end!
- Producer: 76
- Producer: 30
- Producer: 12
- Producer: 18
- Producer: 60
- Producer: 12
- Producer: 1
- Producer: 6
- Producer: 44
- Producer: 58
- Consumer: 76
- Consumer: 30
- Consumer: 12
- Consumer: 18
- Consumer: 60
- Consumer: 12
- Consumer: 1
- Consumer: 6
- Consumer: 44
- Consumer: 58



**创建线程有4种方式**

- Thread

- Runnable

- Callable<V>

- 线程池

创建执行线程的方式三:

- 实现Callable接口.相较于实现Runnable接口的方式 方法可以有返回值,并且可以抛出异常
- 执行Callable方式，需要FutureTask实现类的支持,用于接收运算结果。
- FutureTask是Future接口的实现类

FutureTask可用于闭锁

	public class TestCallable {
	    public static void main(String[] args) {
	        getThreaCallable getThreaCallable = new getThreaCallable();
	        FutureTask futureTask = new FutureTask<>(getThreaCallable);
	        futureTask.run();
	        try {
	            Object o = futureTask.get();
	            System.out.println(o);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        } catch (ExecutionException e) {
	            e.printStackTrace();
	        }
	
	    }
	}
	class getThreaCallable implements Callable<Integer>{
	    private int a=0;
	    @Override
	    public Integer call() throws Exception {
	        for (int i = 0; i < 100; i++) {
	            if(i%2==0){
	                System.out.println(i);
	                a+=i;
	            }
	        }
	        return a;
	    }
	}


用于解决多线程安全问题的方式:

synchronized:隐式锁

1. 同步代码块
2. 同步方法
jdk1.5后：
3. 同步锁

注意:是一个显式锁，需要通过lock()方法上锁,必须通过unlock()方法进行释放锁

	Lock lock = new ReentrantLock();//初此化显式锁
	Condition condition = lock.newCondition();//Condition与Object访问监视器类像，其对应的方法（wait\notify\notifyAll）为:
	condition.await();
	condition.signal();
	condition.signalAll();
	

生产者和消费者案件

为了避免虚假唤醒问题，`this.wait()`应该总是放在循环中


**ReadWriteLock:读写锁**

	ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.readLock().lock();
	lock.readLock().unlock();
	lock.writeLock().lock();
	lock.writeLock().unlock();
写写/读写需要"互斥"
读读 不需要“互斥”

[https://www.jianshu.com/p/9cd5212c8841](https://www.jianshu.com/p/9cd5212c8841 "读写锁详细讲解")

**线程八锁**

1. 两个普通同步请求 ，两个线程，标准打印,打印one two
2. 新增Thread.sleep()给getOne()，打印one two
3. 新增普通方法getThree(),打印three one two
4. 两个普通同步方法,两个Number对象，打印two one
5. 修改getOne()为静态同步方法,一个Number对象,打印two one   1个Object,非静态同步>静态同步
6. 修改两个方法均为静态同步方法,一个Number对象，打印one two
7. 一个静态同步方法,一个非静态同步方法,两个Number对象 two one   2个Object,非静态同步>静态同步
8. 两个静态同步方法,两个Number对象 one two

**线程八锁关键:**

	1. 非静态方法的锁默认为this,静态方法的锁为对应的Class实例
	2. 某一个时刻内，只能有一个线程持有锁,无论几个方法
	
一个对象里面如果有多个synchronized方法，某一个时刻内，只要一个线程去调用其中的一个synchronized方法了，其它的线程都只能等待，换句话说，某一个时刻内，只能有唯一一个线程去访问这些synchronized方法 
锁的是当前对象this，被锁定后，其它的线程都不能进入到当前对象的其它的 

**synchronized方法**

- 加个普通方法后发现和同步锁无关 
- 换成两个对象后，不是同一把锁了，情况立刻变化。 
- 都换成静态同步方法后，情况又变化 
- 所有的非静态同步方法用的都是同一把锁——实例对象本身，也就是说如果一个实例对象的非静态同步方法获取锁后，该实例对象的其他非静态同步方法必须等待获取锁的方法释放锁后才能获取锁，可是别的实例对象的非静态同步方法因为跟该实例对象的非静态同步方法用的是不同的锁，所以毋须等待该实例对象已获取锁的非静态同步方法释放锁就可以获取他们自己的锁。 
- 所有的静态同步方法用的也是同一把锁——类对象本身，这两把锁是两个不同的对象，所以静态同步方法与非静态同步方法之间是不会有竞态条件的。但是一旦一个静态同步方法获取锁后，其他的静态同步方法都必须等待该方法释放锁后才能获取锁，而不管是同一个实例对象的静态同步方法之间，还是不同的实例对象的静态同步方法之间，只要它们同一个类的实例对象！ 
	
**线程池**:提供了一个线程队列,队列中保存着所有等待状态的线程.避免了创建与销毁额外开销,提高了响应的速度。

ThreadPoolExecutor:

	java.util.concurrent.Executor:负责线程的使用与调度的根接口
		ExecutorService:子接口:线程池的主要接口
			ThreadPoolExecutor:线程的实现类
			ScheduledExecutorService子接口:负责线程的调度
				ScheduledThreadPoolExecutor: 继承了ThreadPoolExecutor,实现了ScheduledExecutorService子接口
**工具类:Executors**

    ExecutorService Executors.newFixedThreadPool():创建固定大小的线程池
    ExecutorService Executors.newCachedThreadPool():缓存线程池,线程池的数量不固定,可以根据需求自动的更改数量(首选)
					Executors.newCachedThreadPool(ThreadFactory threadFactory) 
	ExecutorService Executors.newSingleThreadExecutor():创建单个线程池。线程池中只有一个线程（适合使用在文件系统，日志更新事件分发等.如果创建多个线程的情况下，会进行任务排队，只有在当前任务执行完才会执行下个线程）
	
	ScheduledExecutorService newScheduledThreadPool():创建固定大小的线程,可以延迟或定时的执行任务
	void	execute(Runnable command) :执行线程任务
	
	executors.submit(new ThreadPoolDemo());执行线程任务（Callable必须此方法来调用）
	executors.shutdown();关闭线程 启动一次顺序关闭，执行以前提交的任务，但不接受新任务。

**接口 ThreadFactory**

根据需要创建新线程的对象。使用线程工厂就无需再手工编写对 new Thread 的调用了，从而允许应用程序使用特殊的线程子类、属性等等。

此接口最简单的实现就是：

	 class SimpleThreadFactory implements ThreadFactory {
	   public Thread newThread(Runnable r) {
	     return new Thread(r);
	   }
	 }

Fork/Join框架(JDK1.7)将大任务拆分成若干小任务分 开开执行 再join合并

	ForkJoinPool extends AbstractExecutorService implements ExecutorService ForkJoinPool
	ForkJoinPool forkJoinPool = new ForkJoinPool();	
	forkJoinPool.execute(ForkJoinTask)//没返回值
	forkJoinPool.invoke(ForkJoinTask)//获取结果(泛型)
	forkJoinPool.submit(ForkJoinTask)//获取返回结果类型ForkJoinTask<T>
	
	public abstract class RecursiveTask<V> extends ForkJoinTask<V>
	ForkJoinTask.fork()//排列以異步執行此任務。
	ForkJoinTask.join()//合并

**Fork/Join采用 “工作窃取”模式（work-stealing）**： 

- 当执行新的任务时它可以将其拆分分成更小的任务执行，并将小任务加到线程队列中，然后再从一个随机线程的队列中偷一个并把它放在自己的队列中。 
- 相对于一般的线程池实现，fork/join框架的优势体现在对其中包含的任务的处理方式上.在一般的线程池中，如果一个线程正在执行的任务由于某些原因无法继续运行，那么该线程会处于等待状态。而在fork/join框架实现中，如果某个子问题由于等待另外一个子问题的完成而无法继续运行。那么处理该子问题的线程会主动寻找其他尚未运行的子问题来执行.这种方式减少了线程的等待时间，提高了性能
