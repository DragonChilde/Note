# 关于ArrayList线程不安全问题 #

	/**ArrayList.add()因为并发的效率问题，是没有加锁的**/
	public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }

	/*
	* 集合类不安全问题:原因是为了保证并发性，add操作没有加锁
	* ArrayList
	* */
	public class ContainerNotSafeDemo {
	    public static void main(String[] args){
	//        List<String> list = new ArrayList<>();	//ArrayList底层初始化是一个长度为10的空数组
	//        List<String> list = Collections.synchronizedList(new ArrayList<>());
	        List<String> list = new CopyOnWriteArrayList<>();
	        for(int i=1;i<=3;i++){
	            new Thread(()->{
	                list.add(UUID.randomUUID().toString().substring(0,8));
	                System.out.println(list);
	            },String.valueOf(i)).start();
	        }
			//   java.util.ConcurrentModificationException
	        /*
	        * 1 故障现象
	        *   java.util.ConcurrentModificationException
	        *
	        * 2 导致原因
	        *   并发争抢修改导致，参考我们的花名册签名情况。
	        *   一个人正在写入，另一个同学过来抢夺，导致数据不一致异常。并发修改异常。
	        * 3 解决方案
	        *   3.1 new Vector<>();
	        *   3.2 Collections.synchronizedList(new ArrayList<>());
	        *   3.3 new CopyOnWriteArrayList<>()
	        * 4 优化建议
	        * */
	    }
	}


**关于CopyOnWriteArrayList写时复制**

CopyOnWrite容器即写时复制的容器，往一个容器添加元素的时候，不直接往当前容器Object[]添加，而是先将当前容器Object[]进行Copy,复制出一个新的容器Object[] newElements,然后新的容器Object[] new Elements里添加元素,添加完元素之后，再将原容器的引用指向新的容器setArray(newElements).这样做的好处是可以对CopyOnWrite容器进行并发的读，而不需要加锁。因为当前容器不会添加任何元素.所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器

	 public boolean add(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            newElements[len] = e;
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

**HashSet同样是线程不安全的,要线程安全可以用:1. Collections.synchronizedSet() 2. new CopyOnWriteArraySet<>();**

注意：HastSet底层是HashMap的

	/**e作为key值,value设置为常量PRESENT是个new Object()**/
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }

**同理HashMap也是线程不安全的，要线程安全可以用:1.Collections.synchronizedMap() 2. new ConcurrentHashMap<>()**