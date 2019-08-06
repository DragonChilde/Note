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
