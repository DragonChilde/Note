**同步方法**:将操作共享数据的方法声明为synchronized。即此方法为同步方法，能够保证其中一个线程执行此方法时，其它线程在外等待直至线程执行完此方法
同步方法的锁:this

**sleep()不会释放锁，wait()会释放锁**

**线程的同步的弊端**：由于同一个时间只能有一个线程访问共享数据，效率变低
> 
> 关于懒汉式的线程安全问题：使用同步机制
> 对于一般的方法内，使用同步代码块，可以考虑使用this
> 对于静态方法而言，使用当前类本身充当锁

	class Test{
	    private Test(){}
	    public static Test instance;
	    public static Test getObject(){
	        if(instance instanceof Test){
	            return (Test) instance;
	        }else {
	            synchronized(Test.class) {
	                instance = new Test();
	            }
	        }
	        return instance;
	    }
	}

> 死锁的问题，处理线程同步时容易出现问题
> 	不同的线程分别占用对方需要的同步资源不放弃，都在等对方放弃自己需要的同步资源，就形成了线程的死锁


**线程的通信**：如下的三个关键字使用的话，都得在同步代码块或同步方法中

**wait()**：一旦一个线程执行到wait()，线程等待，就释放当前的锁，直至其它线程执行notify()的方法，将其唤醒，唤醒后继续其wait()后的代码

**notify()/notifyAll()**：在同步中，执行到此方法，则唤醒一个或所有的被wait()线程

**notify()**:唤醒在此对象监视器上等待的单个线程。
**notifyAll()**:唤醒在此对象监视器上等待的所有线程。 

方法一：同步代码块

	synchronized(同步监视器){
		//操作共享数据的代码
	}
**注：**
> 	1同步监视器：俗称锁，任何一个类的对象都可以充当锁，要想保证线程的安全，必须要求所有的线程共用同一把锁
> 	2使用实现Runnable接口的方式创建多线程的话，同步代码块中的锁，可以考虑是this,如果使用继承Thread类的方式，慎用this
> 	3共享数据：多个线程需要共同操作的变量。明确哪部分是操作共享数据的代码
	
**方法二：同步方法：将操作共享数据的方法声明为synchronized**
		
    public synchronized void show()
**注：**
> 1对于非静态的方法而言，使用同步的话，默认锁为：this，如果使用在继承的方式实现多线程的话，慎用
> 2对于非静态的方法，如果使用同步，默认的锁为：当前类本身，以单例的懒汉式为例。
   ``Class clazz = Singleton.class`
		
**释放锁**：`wait() `

**不释放锁**：

`sleep()	 yield()` : 暂停当前正在执行的线程对象，并执行其他线程。

`suspend()`(过时，可能导致死锁)

`join()`等待该线程终止

`wait(),notify(),notifyAll()`只能在同步控制方法或同步控制块里调用

`sleep()`可以在非同步控制方法里调用

`setPriority()`：设置线程的优先级（优先级是在run开头部分设定的,在构造器设置是没有任何好处的，因为Executor还没有执行任务）
只使用MAX_PRIORITY NORM_PRIORITY MIN_PRIORITY

`setDaemon()`:设置是否守护线程,如果主线程main退出，守护线程也会自动退出

**String:代表不可变的字符序列，底层使用char[]存放，是final的**

	public char charAt(int index)	//返回指定index位置的字符，index从0开始
	public bollean equals(Object anObject)	//比较2个字符串是否相等，相等返回true,否则返回false
	public int compareTo(String anotherString)
	public int indexOf(String s):返回s字符在当前字符串首次出现的位置，若没有，返回-1
	public int indexOf(String s,int startpoint)返回s字符在当前字符串startpoint位置首次出现的位置，若没有，返回-1
	public int lastIndexOf(String s):返回s字符在当前字符串最后出现的位置，若没有，返回-1
	public int lastIndexOf(String s,int startpoint)
	public bollean startWith(String prefix):判断当前字符串是否以prefix开始
	public boolean endWith(String suffix):判断当前字符串是否以suffix结束
	public boolean regionMatches(int firstStart,String other,int otherStart,int length):判断当前字符串从firstStart开始的子串与另一个字符串other从otherStart开始,length长度的字串是否equals
	public String substring(int startponit):
	public String substring(int start,int end):返回从start开始到end结束的一个左闭右开的子串。start可以从0开始
	public String replace(char oldChar,char newChar)
	public String replaceAll(String old,String new)
	public String trim()
	public String concat(String str)连接当前字符串与str
	public String[] split(String regex)按照regex将当前字符串拆分，拆分为多个字符串，整体返回值为String[]
	
	
# 1字符串与基本数据类型、包装类之间的转换 #
	1. 字符串--》基本数据类型、包装类：调用相应的包装类parseXXX(String Str);
	2. 基本数据类型、包装类-->字符串：调用字符串的重载的valueOf()方法
## 2字符串与字节数组间的转换**** ##
	1. 字符串--》字节数组：调用字符串的getBytes()
	2. 字节数组-->字符串：调用字符串的构造器new String();
### 3字符串与字符数组间的转换 ###
	1. 字符串--》字符数组：调用字符串的toCharArray()
	2. 字符数组--》字符串：调用字符串的构造器
#### 4String与StringBuffer的转换 ####
	1. 	1String-->StringBuffer:使用StringBuffer的构造器:new StringBuffer(String str);
	2. 	2StringBuffer-->String:使用StringBuffer的toString()方法
	
`java.lang.StringBuffer`:代表可变以字符序列，可以对字符串内容进行增删

`public StringBuffer reverse()`:反转此StringBuffer

**总结：**
添加:`append()`

删除:`delete(int startIndex,int endIndex)`

修改:`setCharAt(int index,char ch)	`

查:`charAt(Int n)`

插入:`insert(int index,String str)`

反转:`reverse()`

长度:`length()`


`java.lang.StringBuilder`:可变的字符序列,是jdk5.0新加入的,线程不安全。效率要高于StringBuffer

**效率从高到低：StringBuilder>StringBuffer>String**


# 与时间相关的类： #
1,System类下的`currentTimeMillis()`:返回当前时间的long型值，此方法常用来计算时间差

2,Date类:`java.util.Date`

    Date date = new Date();//返回当前时间的Date:Mon May 12 15:17:01 CST 2014
    Date d1 = new Date(1234566789L)://返回形参外此long型值对应的日期

如何创建其实例：
其下的方法`toString() getTime()`:返回当前日期对应的long型值
以及子类`java.sql.Date`以及子类java

3.**SimpleDateFormat类**

`java.lang.SimpleDateFormat`类易于国际化

格式化：日期--->文本 使用`SimpleDateFormat`的`format()`方法

解析：文本--->日期	使用SimpleDateFormat的parse()方法

**4.Calendar日历类**

获取实例:

    Calendar c = Calendar.getInstance();
    get()/set()/add()/date getTime/setTime()
    
`java.lang.Class `是反射的源头(涉及到反射的类都在java.lang.reflect子包下,如:Field,Method,Constructor,Type,Package...)
过程:源文件通过编译(javac.exe)以后，生成对应的.class文件。之后我们使用java.exe加载（JVM的类加载器完成的)
此.class文件，此.class文件加载到内存以后，就是一个运行时类，存在缓存区。那么这个运行时类本身就是一个Class的实例

1. 每一个运行时类只加载一次

2. 有了Class的实例以后，我们可以进行如下的操作:
	1. 创建对应的运行时类的对象
	2. 	获取对应的运行时的完整结构(属性、方法、构造器、父类、所在的包、异常、注解)
	3. 	调用对应的运行的类的指定的结构(属性、方法、构造器)
	4. 	反射的应用：动态代理
3. 当通过Class的实例调用getMethods()-->Method,getConstructors()-->Constructor
	
	
**如何获取Class的实例（3种）**

1. 调用运行时类本身的.class属性
	Class clazz = Person.class
2. 通过运行时类的对象获取
	Person p = new Person()
	Class clazz = p.getClass()
3. 通过Class的静态方法获取.通过此方式，体会反射的动态性
	String className = "com.java.Person";
	Class clazz = Class.forName(className)
4. 通过类的加载器
	ClassLoader classLoader = this.getClass().getClassLoader();
	Class clazz = classLoader.loadClass(className);
	
关于类的加载器：**ClassLoader 掌握**

     ClassLoader loader = this.getClass.getClassLoader();
     InputStream is = loader.getResourceAsStream("com\\java\\jdbc.properties");
     Properties pro = new Properties();
     pro.load(is)
     String user = pros.getProperty("user");

  
创建类时，尽量保留一个空参的构造器 创建对应的运行时类的对象。

使用newInstance()实际上就是调用了运行时类的空参构造器

要想能够创建成功：
	1. 要求对应的运行时类要有空参的构造器
	2. 构造器的权限修饰符的权限要足够


    getFields():只能获取到运行时类中及其父类中声明为public的属性
    getDeclaredFields():获取运行时类本身声明的所有的属性
    getModfiers():获取每个属性的权限修饰符
    getType():获取属性的变量类型
    getMethods():获取运行时类及其父类中所有的声明为public的方法
    getDeclaredMethods():获取运行时类本身声明的所有的方法
    getAnnotations():获取注解
    getReturnType():返回值类型
    getParameterTypes():形参列表
    getExceptionTypes():异常类型
    getDeclaredConstructors():获取运行时类本声的构造器
    getSuperClass():获取运行时类的父类
    getGenericSuperclass():获取带泛型的父类
    getActualTypeArguments():获取泛型的类型
    getInterfaces():获取实现的接口
    getPackage():获取所在的包
    getAnnotations():获取注解:获取运行时类中声明为public的指定属性名为public的指定属性名为fieldName的属性
	getDeclaredField(String fieldName):获取运行时类中指定的名为fieldName的属性
	由于属性权限修饰符的限制，为了保证可以给属性赋值，需要在操作前使得此属性可被操作
	get()调用静态属性
	
	
调用运行时类中指定的方法

	getMethod(String methodName,Class...params):获取运行时类中声明为public的方法
	Object invoke(Object obj,Object...obj):调用指定的方法
对于运行时静态方法的调用

	Method m3 = clazz.getMethod("info");
	m3.invoke(Person.class);

	getDeclaredMethod(String methodName,Class...params):获取运行时类中声明了的指定的方法
	Method m = Clazz.getDeclaredMethod("display",String.class,Integer.class)
	m.setAccessible(true);//更改方法的权限
	m4.invoke(p,"CHN",4);

调用指定的构造器

    Constructor cons = clazz.getDeclaredConstructor(String.class,int.class);
    cons.setAccessible(true);
    Person p = (Person)cons.newInstance("test",20);


有了Class实例以后

	1.  可以创建对应的运行时类的对象
	2. 	获取对应的运行时类的完整的结构：属性、方法、构造器、包、父类、接口、泛型、注解、异常、内部类....
			如：Method[] m1 = clazz.getMethods():获取到对应的运行时类中声明的权限为public的方法(包含其父类的声明的public)
			Method[] m2 = clazz.getDeclaredMethods():获取对对应的运行时类中声明的所有的方法(1任何权限修饰符修饰的都能获取2不含父类中的)
	3. 	3调用对应的运行时类中指定的结构(某个指定的属性、方法、构造器)(重点)

代理模式的原理:使用一个代理将对象包装起来,然后用该代理对象取代原对象，任何对原始对象的调用都要通过代理.代理对象决定是否以及何时将方法调到转到原始对象上

静态代理:要求被代理类和代理类同时实现相应的一套接口;通过被代理类的对象调用重写接口方法时,实际上执行的是被代理类的同样的方法调用

动态代理:在运行时，根据代理类及其实现的接口，动态的创建一个代理类。当调用代理类的实现的抽象方法时，就发起对被代理类同样方法的调用.

涉及到的技术点:
	提供一个实现了InvocationHandler接口实现类，并重写期invoke()方法	2Proxy.newInstance(obj.getClass().getClassLoader(),obj.getClass().getInterfaces(),h);//注:obj:被代理类对象,h:实现了1提供一个实现了InvocationHandler接口实现类，并重写期invoke接口的实现类的对象

	public class TestProxy {
	    public static void main(String[] args) {
	        CustomerProxy customerProxy = new CustomerProxy();
	        Object o = customerProxy.setObject(new Customer());
	        Person person = (Person) o;
	        person.readBook();
	    }
	}

	class Customer implements Person{
	    public void readBook(){
	        System.out.println("customer read book");
	    }
	}
	interface Person{
	    public void readBook();
	}
	class CustomerProxy implements InvocationHandler {
	    Object object;
	
	    public Object setObject(Object object){
	        this.object = object;
	       return Proxy.newProxyInstance(object.getClass().getClassLoader(),object.getClass().getInterfaces(),this);
	    }
	    @Override
	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	        System.out.println("-------------");
	        method.invoke(object);
	        System.out.println("!!!!!!!!!!!!!!");
	      return null;
	    }
	}	
	

# InetAddress:位于java.net包下 #

	1. InetAddress用来代表IP地址。一个InetAddress的对象代表着一个IP地址
	2. 	如何创建InetAddress的对象：getByName(String host)
	 	如：InetAddress inet = InetAddress.getByName("192.168.0.1");
	3. 	获取本机的一个InetAddress的对象:getLocalHost()
	4. 	getHostName():获取IP地址对应的域名
		getHostAddress():获取IP地址

**TCP的编程：Socket ServerSocket
UDP的编程:DatagramSocket DatagramPacket**

	shutdownOutput():执行此方法，显式的告诉服务端发送完毕
	
**URL:统一资源定位符**

	getProtocol():获取该URL的协议名
	getHost():获取该URL的主机名
	getPort():获取该URL的端口号
	getPath():获取该URL的文件路径
	getFile():获取该URL的文件名
	getRef()：获取该URL在文件中的相对位置
	getQuery():获取该URL的查询名

如何将服务 端的资源读取进来:`openStream()`

	InputStream is = url.openStream()
	byte[] b = new byte[20];
	int len;
	while(len = is.read(b)!=-1){
		String str  = new String(b,0,len);
		System.out.print(str)
	}
	is.close();
	
如果既有数据的输入，又有数据的输出，则考虑使用URLConnection

	URLConnection urlConn = url.openConnection();
	InputStream is1 = urlConn.getInputStream();
	FileOutputStream fos = new FileOutputStream(new File("abc.txt"));
	byte[] b1 = new byte[20];
	int len1;
	while((len1 = is1.read(b1)!=-1){
		fos.write(b1,0,len1);
	}
	fos.close();
	is1.close();

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


**NIO**

NIO的核心在于:**通道(channel)**和**缓冲区(buffer)**,Channel负责传输,Buffer负责存储
缓冲区(Buffer):在Java NIO中负责数据的存取.缓冲区就是数组.用于存储不同数据类型的数据
根据数据类型不同(boolean除外),提供了相应类型的缓冲区:

- ByteBuffer
- CharBuffer
- ShortBuffer
- IntBuffer
- LongBuffer
- FloatBuffer
- DoubleBuffer

上述缓冲区的管理方式几乎一致，通过static xxxBuffer.allocate(int capacity)获取缓冲区

缓冲区存取数据的两个核心方法:
    
    put():存入数据到缓冲区中
    get():获取缓冲区中的数据

缓冲区中的四个核心属性:

	capactiy:容量,表示缓冲区中最大存储数据的容量.一旦声明不能改变
	limit:界限,表示缓冲区中可以操作数据的大小.(limit 后数据不能进行读写)
	position:位置，表示缓冲区中正在操作数据的位置
	mark:标记，表示记录当前postion的位置。可以通过reset()恢复到mark位置0<=mark<=position<=limit<=capacity
	flip():切换读取数据模式	读->写		写->读
			使缓冲区为新的通道写入或相对获取操作序列做好准备：它将limit设置为当前位置，然后将位置设置为零。
	rewind():可重复读数据
	clear():清空缓冲区,但是缓冲区中的数据依然存在，但是处于"被遗忘"状态
	hasRemaining:判断缓冲区中是否还有剩余数据
	remaining:获取缓冲区中可以操作的数量

	ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
	System.out.println(byteBuffer.capacity());		//1024
	System.out.println(byteBuffer.limit());			//1024
	System.out.println(byteBuffer.position());		//0
	
	String str = "abcde";
	byteBuffer.put(str.getBytes());					
	System.out.println(byteBuffer.capacity());		//1024
	System.out.println(byteBuffer.limit());			//1024
	System.out.println(byteBuffer.position());		//5
	
	byteBuffer.flip();
	System.out.println(byteBuffer.capacity());		//1024
	System.out.println(byteBuffer.limit());			//5
	System.out.println(byteBuffer.position());		//0
	
	byte[] bytes = new byte[byteBuffer.limit()];
	byteBuffer.get(bytes);
	System.out.println(new String(bytes,0,byteBuffer.limit()));	//abcde
	System.out.println(byteBuffer.capacity());			//1024
	System.out.println(byteBuffer.limit());				//5
	System.out.println(byteBuffer.position());			//5
	
	byteBuffer.rewind();
	System.out.println(byteBuffer.capacity());			//1024
	System.out.println(byteBuffer.limit());				//5
	System.out.println(byteBuffer.position());			//0
	
	byteBuffer.clear();
	System.out.println(byteBuffer.capacity());			//1024
	System.out.println(byteBuffer.limit());				//1024
	System.out.println(byteBuffer.position());			//0
	System.out.println((char) byteBuffer.get());		//a
	
	String str = "abcd";
	ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
	byteBuffer.put(str.getBytes());
	
	byteBuffer.flip();
	byte[] bytes = new byte[byteBuffer.limit()];
	byteBuffer.get(bytes,0,2);
	System.out.println(new String(bytes,0,2));			//ab
	System.out.println(byteBuffer.position());			//2
	
	byteBuffer.mark();
	byteBuffer.get(bytes,2,2);
	System.out.println(new String(bytes,2,2));			//cd
	System.out.println(byteBuffer.position());			//4
	
	byteBuffer.reset();
	System.out.println(byteBuffer.position());			//2
	
	if(byteBuffer.hasRemaining()){
		System.out.println(byteBuffer.remaining());		//2
	}

**直接缓冲区与非直接缓冲区别:**

- 非直接缓冲区:通过allocate()方法分配缓冲区,将缓冲区建立在JVM的内存中(在内存中进行复制搬运,因此速度不及直接缓冲区)
- 直接缓冲区:通过allocateDirect()方法分配直接缓冲区，将缓冲区建立在物理内存中，可以提高效率(在物理内存建立映射文件,速度快,但不能及时反映出处理结果,会有延时)

**通道(channel)**:用于源节点与目标节点的连接.在Java NIO中负责缓冲区中数据的传输Channel本身不储存数据，因此需要配合缓冲区进行传输

**二通道的主要实现类:**

	java.nio.channels.Channel接口：
		FileChannel
		SocketChannel
		ServerSocketChannel
		DatagramChannel

**三获取通道**

1Java针对支持通道的类提供了getChannel()方法

	本地IO:
		FileInputStream/FileOutputStream
		RandomAccessFile
	网络IO:
		Socket
		ServerSocket
		DatagramSocket
	
 	public static void test01(){
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        FileChannel inpuChannel = null;
        FileChannel outChannel = null;

        try {
            fileInputStream = new FileInputStream("C:\\Users\\FYkf\\Desktop\\20160726165633.png");
            fileOutputStream = new FileOutputStream("C:\\Users\\FYkf\\Desktop\\test.png");

            inpuChannel = fileInputStream.getChannel();
            outChannel = fileOutputStream.getChannel();

            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            while (inpuChannel.read(byteBuffer)!=-1){
                byteBuffer.flip();
                outChannel.write(byteBuffer);
                byteBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(outChannel!=null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inpuChannel!=null) {
                try {
                    inpuChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileOutputStream!=null)
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (fileInputStream!=null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
2在JDK1.7中的NIO.2针对各个通道提供了静态方法open()

	//使用直接缓冲区完成文件的复制(内存映射文件)
	 public static void test02(){
        try {
            FileChannel inChannel = FileChannel.open(Paths.get("C:\\Users\\FYkf\\Desktop\\20160726165633.png"), StandardOpenOption.READ);
            FileChannel outChannel = FileChannel.open(Paths.get("C:\\Users\\FYkf\\Desktop\\test.png"), StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.READ);

            MappedByteBuffer inMap = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            MappedByteBuffer outMap = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

            byte[] bytes = new byte[inMap.limit()];
            inMap.get(bytes);
            outMap.put(bytes);

            outChannel.close();
            inChannel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

3在JDK1.7中的NIO.2的Files工具类的newByteChannel()

**四通道之间的数据传输(直接缓冲区)**

transferForm()

transferTo()

	public static void test03() {
		try {
			FileChannel inChannel = (FileChannel) Files.newByteChannel(Paths.get("C:\\Users\\FYkf\\Desktop\\20160726165633.png"), StandardOpenOption.READ);
			FileChannel outChannel = (FileChannel) Files.newByteChannel(Paths.get("C:\\Users\\FYkf\\Desktop\\test.png"), StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.READ);
			outChannel.transferFrom(inChannel,0,inChannel.size());
			outChannel.close();
			inChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

**五、分散(Scatter)与聚集(Gather)**

- 分散读取（Scattering Reads）：将通道中的数据分散到多个缓冲区中
- 聚集写入（Gathering Writes）：将多个缓冲区中的数据聚集到通道中

	    public static void test04(){
	    	try {
	    		RandomAccessFile f = new RandomAccessFile("C:\\Users\\FYkf\\Desktop\\Git.txt", "rw");
	    		FileChannel channel = f.getChannel();
	    		ByteBuffer allocate1 = ByteBuffer.allocate(100);
	    		ByteBuffer allocate2 = ByteBuffer.allocate(1024);
	    		ByteBuffer[] byteBuffers = {allocate1,allocate2};
	    		channel.read(byteBuffers);
	    		for (ByteBuffer b :byteBuffers){
	    			b.flip();
	    		}
	    		System.out.println(new String(byteBuffers[0].array(),0,byteBuffers[0].limit()));
	    		System.out.println("===============");
	    		System.out.println(new String(byteBuffers[1].array(),0,byteBuffers[1].limit()));
	    		RandomAccessFile fw = new RandomAccessFile("C:\\Users\\FYkf\\Desktop\\Git2.txt", "rw");
	    		FileChannel wChannel = fw.getChannel();
	    		wChannel.write(byteBuffers);
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }

**字符集：Charset**

- 编码：字符串 -> 字节数组  encode(CharBuffer)
- 解码：字节数组  -> 字符串 decode(ByteBuffer)

		public static void test06(){
			 SortedMap<String, Charset> stringCharsetSortedMap = Charset.availableCharsets();
			Set<Map.Entry<String, Charset>> entries = stringCharsetSortedMap.entrySet();
			for (Map.Entry<String,Charset> entry:entries){
				System.out.println(entry.getKey() +":"+entry.getValue());
			}
		
			Charset charset = Charset.forName("UTF-8");
			CharsetEncoder charsetEncoder = charset.newEncoder();
			CharsetDecoder charsetDecoder = charset.newDecoder();
			String str="测试是没有错的";
			CharBuffer ch = CharBuffer.allocate(1024);
			ch.put(str);
			ch.flip();
		
			try {
				ByteBuffer encode = charsetEncoder.encode(ch);
				for (int i = 0; i < 21; i++) {
					System.out.println(encode.get());
				}
				encode.flip();
				CharBuffer decode = charsetDecoder.decode(encode);
			   System.out.println(decode.toString());
		
		
			} catch (CharacterCodingException e) {
				e.printStackTrace();
			}
		}

**使用 NIO 完成网络通信的三个核心：**

	1. 通道（Channel）：负责连接
	 java.nio.channels.Channel 接口：
	 		|--SelectableChannel
	 			|--SocketChannel
	 			|--ServerSocketChannel
	 			|--DatagramChannel
	 		Pipe管道:是2个线程之间的单向数据连接,Pipe有一个source通道和一个sink通道，数据会被写到sink通道,从source通道读取
	 			|--Pipe.SinkChannel
	 			|--Pipe.SourceChannel
	2. 缓冲区（Buffer）：负责数据的存取
	3. 选择器（Selector）：是 SelectableChannel 的多路复用器。用于监控 SelectableChannel 的 IO 状况
 
**FileChannel无论怎样都是阻塞的**

    // SocketChannel/ServerSocketChanne
    //客户端
    public void client() throws IOException{
    	//1. 获取通道
    	SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
    	//2. 切换非阻塞模式
    	sChannel.configureBlocking(false);
    	//3. 分配指定大小的缓冲区
    	ByteBuffer buf = ByteBuffer.allocate(1024);
    	//4. 发送数据给服务端
    	Scanner scan = new Scanner(System.in);
    	while(scan.hasNext()){
    		String str = scan.next();
    		buf.put((new Date().toString() + "\n" + str).getBytes());
    		buf.flip();
    		sChannel.write(buf);
    		buf.clear();
    	}
    	//5. 关闭通道
    	sChannel.close();
    }
    
    
    //服务端
    @Test
    public void server() throws IOException{
    	//1. 获取通道
    	ServerSocketChannel ssChannel = ServerSocketChannel.open();
    	//2. 切换非阻塞模式
    	ssChannel.configureBlocking(false);
    	//3. 绑定连接
    	ssChannel.bind(new InetSocketAddress(9898));
    	//4. 获取选择器
    	Selector selector = Selector.open();
    	//5. 将通道注册到选择器上, 并且指定“监听接收事件”
    	ssChannel.register(selector, SelectionKey.OP_ACCEPT);
    	//6. 轮询式的获取选择器上已经“准备就绪”的事件
    	while(selector.select() > 0){	
    		//7. 获取当前选择器中所有注册的“选择键(已就绪的监听事件)”
    		Iterator<SelectionKey> it = selector.selectedKeys().iterator();	
    		while(it.hasNext()){
    			//8. 获取准备“就绪”的是事件
    			SelectionKey sk = it.next();	
    			//9. 判断具体是什么事件准备就绪
    			if(sk.isAcceptable()){
    				//10. 若“接收就绪”，获取客户端连接
    				SocketChannel sChannel = ssChannel.accept();		
    				//11. 切换非阻塞模式
    				sChannel.configureBlocking(false);		
    				//12. 将该通道注册到选择器上
    				sChannel.register(selector, SelectionKey.OP_READ);
    			}else if(sk.isReadable()){
    				//13. 获取当前选择器上“读就绪”状态的通道
    				SocketChannel sChannel = (SocketChannel) sk.channel();		
    				//14. 读取数据
    				ByteBuffer buf = ByteBuffer.allocate(1024);		
    				int len = 0;
    				while((len = sChannel.read(buf)) > 0 ){
    					buf.flip();
    					System.out.println(new String(buf.array(), 0, len));
    					buf.clear();
    				}
    			}	
    			//15. 取消选择键 SelectionKey
    			it.remove();
    		}
    	}
    }
    
    
    //DatagramChannel
    public void send() throws IOException{
    	DatagramChannel dc = DatagramChannel.open();
    	dc.configureBlocking(false);
    	ByteBuffer buf = ByteBuffer.allocate(1024);
    	Scanner scan = new Scanner(System.in);
    	while(scan.hasNext()){
    		String str = scan.next();
    		buf.put((new Date().toString() + ":\n" + str).getBytes());
    		buf.flip();
    		dc.send(buf, new InetSocketAddress("127.0.0.1", 9898));
    		buf.clear();
    	}
    	dc.close();
    }
    
    
    public void receive() throws IOException{
    	DatagramChannel dc = DatagramChannel.open();
    	dc.configureBlocking(false);
    	dc.bind(new InetSocketAddress(9898));
    	Selector selector = Selector.open();
    	dc.register(selector, SelectionKey.OP_READ);
    	while(selector.select() > 0){
    		Iterator<SelectionKey> it = selector.selectedKeys().iterator();
    		while(it.hasNext()){
    			SelectionKey sk = it.next();	
    			if(sk.isReadable()){
    				ByteBuffer buf = ByteBuffer.allocate(1024);
    				dc.receive(buf);
    				buf.flip();
    				System.out.println(new String(buf.array(), 0, buf.limit()));
    				buf.clear();
    			}
    		}
    		it.remove();
    	}
    }
    
    
    //Pipe
    public void test1() throws IOException{
    	//1. 获取管道
    	Pipe pipe = Pipe.open();
    	//2. 将缓冲区中的数据写入管道
    	ByteBuffer buf = ByteBuffer.allocate(1024);
    	Pipe.SinkChannel sinkChannel = pipe.sink();
    	buf.put("通过单向管道发送数据".getBytes());
    	buf.flip();
    	sinkChannel.write(buf);
    	//3. 读取缓冲区中的数据
    	Pipe.SourceChannel sourceChannel = pipe.source();
    	buf.flip();
    	int len = sourceChannel.read(buf);
    	System.out.println(new String(buf.array(), 0, len));
    	sourceChannel.close();
    	sinkChannel.close();
}