# CountDownLatch/CyclicBarrier/Semaphore #

**CountDownLatch**

让一些线程阻塞直到另一个线程完成一系列操作后才被唤醒

CountDownLatch主要有两个方法，当一个或多个线程调用await方法时，调用线程会被阻塞。其他线程调用countDown方法会将计数器减1（调用CountDown方法的线程不会阻塞），当计数器的值变为0时，因调用await方法被阻塞的线程会被唤醒，继续执行。

**相当于倒计时，当子线程执行计数器为0时才执行主线程**

	/**枚举类相当于小型数据库,ONE相当于表，1为表字段，“韩国”为表值**/
	public enum CountryEnum {
	    ONE(1,"韩国"),TWO(2,"齐国"),THREE(3,"魏国"),FOUR(4,"楚国"),FIVE(5,"燕国"),SIX(6,"赵国"),SEVEN(7,"秦国");
	
	
	    public Integer returnCode;
	    public String MessageCode;
	
	
	    CountryEnum(Integer returnCode, String messageCode) {
	        this.returnCode = returnCode;
	        MessageCode = messageCode;
	    }
	
	    public Integer getReturnCode() {
	        return returnCode;
	    }
	
	    public String getMessageCode() {
	        return MessageCode;
	    }
	
	    public static CountryEnum getValue(int index){
	        CountryEnum[] values = CountryEnum.values();
	        for (CountryEnum value:values){
	            if (index == value.returnCode){
	                return value;
	            }
	        }
	        return null;
	    }
	}


	public class CountDownLatchDemo {
	    public static void main(String[] args) {
		 /**初始化倒计时数倒为6**/	
	        CountDownLatch countDownLatch = new CountDownLatch(6);
	        for (int i = 1; i <=6 ; i++) {
	            new Thread(()->{
	                System.out.println(Thread.currentThread().getName()+"灭");
					/**每执行一次递减**/
	                countDownLatch.countDown();	
	            },CountryEnum.getValue(i).getMessageCode()).start();
	        }
	
	        try {
				//线程阻塞,当倒计时为0时被唤醒继续执行下去
	            countDownLatch.await();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	
	        System.out.println(CountryEnum.SEVEN);
	        System.out.println(CountryEnum.SEVEN.returnCode);
	        System.out.println(CountryEnum.SEVEN.MessageCode+"统一天下");
	
			  /*    齐国灭
			        燕国灭
			        魏国灭
			        楚国灭
			        韩国灭
			        赵国灭
			        SEVEN
			        7
			        秦国统一天下*/
	    }
	}


**CyclicBarrier**

CyslicBarrier的字面意思是可循环（Cyclic）使用的屏障（Barrier）。它要做的事情是，让一组线程到达屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会打开门，所有被屏障拦截的线程才会继续干活，线程进入屏障通过CyclicBarrier的await（）方法。

**与CountDownLatch用法相反，当子线程执行完累加到固定值时才会执行主线程**

	public class CyclicBarrierDemo {
	    public static void main(String[] args) {
			/**一般初始化使用下面方法**/
			//CyclicBarrier(int parties, Runnable barrierAction)
	        CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
	            System.out.println("集齐7颗龙珠召唤神龙！");
	        });
	
	        for (int i = 1; i <= 7; i++) {
	            new Thread(()->{
	                System.out.println(Thread.currentThread().getName()+"颗龙珠集齐！");
	                try {
	                    cyclicBarrier.await();
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                } catch (BrokenBarrierException e) {
	                    e.printStackTrace();
	                }
	            },String.valueOf(i)).start();
	        }
			
			 /*   	1颗龙珠集齐！
			        3颗龙珠集齐！
			        6颗龙珠集齐！
			        2颗龙珠集齐！
			        4颗龙珠集齐！
			        5颗龙珠集齐！
			        7颗龙珠集齐！
			        集齐7颗龙珠召唤神龙！*/
	    }
	}


**Semaphore**

信号量主要用于两个目的，一个是用于多个共享资源的互斥使用，另一个用于并发线程数的控制。

这也相当于是一个锁(synchronized/lock)之类的，只是多个线程抢一个资源，Semaphore可以控制多个线程抢多个资源

控制线程要获得许可才可以进入（好像红绿灯，红灯进绿灯停），这里是多到多关系，多个线程在等待有限的许可证才可执行acquire()阻塞没有许可证的线程，执行完务必要release()释放许可让其它线程获取

	/****/
	public class SemaphoreDemo {
	
	    public static void main(String[] args) {
	        Semaphore semaphore = new Semaphore(3);//模拟3个车位
	
	        for (int i = 1; i <= 6; i++) {//模拟6部车
	            new Thread(()->{
	
	                try {
	                    semaphore.acquire();
	                    System.out.println(Thread.currentThread().getName()+"号抢到了车位");
	                    TimeUnit.MILLISECONDS.sleep(3);
	                    System.out.println(Thread.currentThread().getName()+"号停了3秒");
	
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                } finally {
	                    semaphore.release();
	                    System.out.println(Thread.currentThread().getName()+"号出来");
	                }
	            },String.valueOf(i)).start();
	        }

			 /*  2号抢到了车位
		        6号抢到了车位
		        1号抢到了车位
		        2号停了3秒
		        2号出来
		        5号抢到了车位
		        1号停了3秒
		        1号出来
		        4号抢到了车位
		        6号停了3秒
		        6号出来
		        3号抢到了车位
		        5号停了3秒
		        5号出来
		        4号停了3秒
		        3号停了3秒
		        3号出来
		        4号出来*/
	    }
	}