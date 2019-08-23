# ActiveMQ #

问题产生背景-系统之间直接调用实际工程落地和存在的问题

- 系统之间接口耦合比较严重
- 面对大流量并发时，容易被冲垮
- 等待同步存在性能问题

作用:解耦 异步 削锋

# Java代码实现ActiveMQ通讯 #

流程图:

![](http://www.51cto.com/files/uploadimg/20120525/1543541.png)

**JMS开发的基本步骤**

1. 创建一个connection factory
2. 通过connection factory来创建JMS connection
3. 启动JMS connection
4. 通过connection创建JMS session
5. 创建JMS destination
6. 创建JMS producer或者创建JMS message并设置destination
7. 创建JMS consumer或者注册一个JMS message listener
8. 发送或者接收JMS message(s)
9. 关闭所有的JMS源(connection,session,producer,consumer等)

**QUEUE**

**Queue生产者Demo**

	/**
	 * @author Lee
	 * @create 2019/8/7 15:35
	 */
	public class JmsProduce {
	    //设置目的地址URL
	    private static final String ACTIVE_URL = "tcp://120.77.237.175:61616";
	    //设置队列名称
	    private static final String QUEUE = "queue01";
	
	
	    public static void main(String[] args) throws Exception {
	        //1创建连接工厂,按照给定的url地址,采用默认用户名和密码
	        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_URL);
	        //2通过连接工厂,获得连接connection并启动访问
	        Connection connection = activeMQConnectionFactory.createConnection();
	        connection.start();
	        //3创建会话session
	        //两个参数，第一个叫事务/第二个叫签收
	        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
	        //4创建目的地，(具体是队列还是主题topic)
	        Queue queue = session.createQueue(QUEUE);
	        //5创建消息生产者
	        MessageProducer messageProducer = session.createProducer(queue);
	        //6通过使用messageProducer生产3条消息发送到MQ的队列里面
	        for (int i = 1; i <= 3; i++) {
	            //7创建消息,
	            TextMessage textMessage = session.createTextMessage("msg is " + i);//可以理解发送字符串
	            messageProducer.send(textMessage);
	        }
	        //9.关闭资源
	        messageProducer.close();
	        session.close();
	        connection.close();
	
	        System.out.println("消息发送完毕===============");
	    }
	}

**Queue消费者Demo**

	/**
	 * @author Lee
	 * @create 2019/8/7 16:31
	 */
	public class JmsConsumer {
	    //设置目的地址URL
	    private static final String ACTIVE_URL = "tcp://120.77.237.175:61616";
	    //设置队列名称
	    private static final String QUEUE = "queue01";
	
	    public static void main(String[] args) throws Exception {
	        //1创建连接工厂,按照给定的url地址,采用默认用户名和密码
	        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_URL);
	        //2通过连接工厂,获得连接connection并启动访问
	        Connection connection = activeMQConnectionFactory.createConnection();
	        connection.start();
	        //3创建会话session
	        //两个参数，第一个叫事务/第二个叫签收
	        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
	        //4创建目的地，(具体是队列还是主题topic)
	        Queue queue = session.createQueue(QUEUE);
	        //5创建消息消费者
	        MessageConsumer messageConsumer = session.createConsumer(queue);

	        /**接收方式一**/
	        /**同步阻塞receive()订阅者或接收者调用MessageConsumer的receive()方法来接收消息,receive方法在能够接收到消息之前(或超时之前)将一直阻塞**/
	        /*   while (true){
	            TextMessage message = (TextMessage)messageConsumer.receive();
	            //TextMessage message = (TextMessage)messageConsumer.receive(4000L);    //等待4S后退出
	            if (message!=null){
	                System.out.println("从中间件接收到的信息是 "+message.getText());
	            } else {
	                break;
	            }
	       messageConsumer.close();
	        session.close();
	        connection.close();
	
	        }*/
	
	        /**接收方式二**/
	        /**通过监听的方式来接收消息**/
	        /**异步非阴塞方法监听器onMessage()**/
	        /**订阅者或者接收者通过MessageConsumer的setMessageListener(MessageListener listener)注册一个消息监听器,当消息到达之后,系统自动调用
	         监听器MessageListener的 onMessage(Message message)方法**/
	      messageConsumer.setMessageListener(new MessageListener() {
	          @Override
	          public void onMessage(Message message){
	              if(message!=null && message instanceof TextMessage){
	                  TextMessage textMessage = (TextMessage)message;
	                  try {
	                      System.out.println("通过Listener接收到的信息是： "+textMessage.getText());
	                  } catch (JMSException e) {
	                      e.printStackTrace();
	                  }
	              }
	          }
	      });
	        System.in.read();
	        messageConsumer.close();
	        session.close();
	        connection.close();
	
	        System.out.println("消息接收完毕=====================");
	        
	
	    }
	}

两种消费方式

1. 同步阻塞方式：receive()方法 订阅者或接收者调用MessagConsumer的receive()方法，在接收到消息之前一直阻塞。
2. 异步非阻塞方式：onMessage()监听器订阅者或接收者通过MessagConsumer的setMessageListener()注册一个消息监听器，当消息到达之后，系统自动调用监听器的的onMessage()方法。





三种情况？

1先生产，只启动一号消费者，问题:一号消费者能消费吗?Y

2先生产,先启动一号消费者再启动二号消费者，问题:二号消费者还能消费信息吗?

一号可以消费？Y

二号可以消费？N

3先启动2个消费者,再生产6条消息,请问,消费情况如何?

两个消费者都有6条 N

先到先得,6条全给一个 N

一人一半 Y

**TOPIC**

注意:必须先启动消费者再开启生产者，要先订阅才能发送成功，不然发送的就是废消息

**Topic生产者Demo**

	public class JmsProduce {
	    //设置目的地址URL
	    private static final String ACTIVE_URL = "tcp://120.77.237.175:61616";
	    //设置队列名称
	    private static final String TOPIC = "topic01";
	
	
	    public static void main(String[] args) throws Exception {
	        //1创建连接工厂,按照给定的url地址,采用默认用户名和密码
	        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_URL);
	        //2通过连接工厂,获得连接connection并启动访问
	        Connection connection = activeMQConnectionFactory.createConnection();
	        connection.start();
	        //3创建会话session
	        //两个参数，第一个叫事务/第二个叫签收
	        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
	        //4创建目的地，(具体是队列还是主题topic)
	        Topic topic = session.createTopic(TOPIC);
	        //5创建消息生产者
	        MessageProducer messageProducer = session.createProducer(topic);
	        //6通过使用messageProducer生产3条消息发送到MQ的队列里面
	        for (int i = 1; i <= 3; i++) {
	            //7创建消息,
	            TextMessage textMessage = session.createTextMessage("topic msg is " + i);//可以理解发送字符串
	            messageProducer.send(textMessage);
	        }
	        //9.关闭资源
	        messageProducer.close();
	        session.close();
	        connection.close();
	
	        System.out.println("消息发送完毕===============");
	    }
	}

**Topic消费者Demo**

	public class JmsConsumer {
	    //设置目的地址URL
	    private static final String ACTIVE_URL = "tcp://120.77.237.175:61616";
	    //设置队列名称
	    private static final String TOPIC = "topic01";
	
	    public static void main(String[] args) throws Exception {
	
	        System.out.println("Topic 消费者02");
	        //1创建连接工厂,按照给定的url地址,采用默认用户名和密码
	        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_URL);
	        //2通过连接工厂,获得连接connection并启动访问
	        Connection connection = activeMQConnectionFactory.createConnection();
	        connection.start();
	        //3创建会话session
	        //两个参数，第一个叫事务/第二个叫签收
	        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
	        //4创建目的地，(具体是队列还是主题topic)
	        Topic topic = session.createTopic(TOPIC);
	        //5创建消息消费者
	        MessageConsumer messageConsumer = session.createConsumer(topic);
	
	        /**通过监听的方式来接收消息**/
	        /**异步非阴塞方法监听器onMessage()**/
	        /**订阅者或者接收者通过MessageConsumer的setMessageListener(MessageListener listener)注册一个消息监听器,当消息到达之后,系统自动调用
	         监听器MessageListener的 onMessage(Message message)方法**/
	        messageConsumer.setMessageListener(new MessageListener() {
	            @Override
	            public void onMessage(Message message){
	                if(message!=null && message instanceof TextMessage){
	                    TextMessage textMessage = (TextMessage)message;
	                    try {
	                        System.out.println("通过Topic Listener接收到的信息是： "+textMessage.getText());
	                    } catch (JMSException e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	        });
	        System.in.read();
	        messageConsumer.close();
	        session.close();
	        connection.close();
	
	        System.out.println("消息接收完毕=====================");
	
	
	    }
	}

**Queue和Topic比较**

![](https://cn.bing.com/th?id=OIP.elqLNQr2i1I8jP7Hs4VcuAHaDY&pid=Api&rs=1)

**Topic模式队列**

工作模式:"订阅-发布"模式,如果当前没有订阅者，消息将会被丢弃,如果有多个订阅者,那么这些订阅者都会收到消息

有无状态:无状态

传递完整性:如果没有订阅者,消息会被丢弃

处理效率:由于消息要按照订阅者的数量进行复制,所以处理性能会随着订阅者的增加而明显降低,并且还要结合不同消息协议自身的性能差异

**Queue模式队列**

工作模式:"负载均衡"模式,如果当前没有消费者,消息也不会丢弃;如果有多个消费者，那么一条消息也只会发送给其中一个消费者,并且要求消费者ack信息

有无状态:Queue数据默认会在mq服务器上以文件 形式保存,比如Active MQ一般保存在$AMQ_HOME\data\kr-store\data下面.也可以配置成DB存储

传递完整性:消息不会丢弃

处理效率:由于一条消息只发送给一个消费者,所以就算消费者再多,性能也不会有明显降低.当然不同消息协议的具体性能也是有差异的


# JMS #

![](https://img-blog.csdn.net/20170816171523564?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvb01hdmVyaWNrMQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

**JMS组成结构和特点**

**JMS Message**

- 消息头

**JMSDestination** 消息发送的目的地,主要是指Queue和Topic

 	 textMessage.setJMSDestination();

**JMSDeliveryMode** 持久模式和非持久模式,一条持久性的消息:应该被传送"一次仅仅一次",这就意味如果JMS提供者出现故障,该消息并不会丢失,它会在服务器之后再传递.一条非持久的消息:最多会传送一次,这意味这服务器出现故障,该消息永远丢失

	textMessage.setJMSDeliveryMode();
	
**JMSExpiration** 可以设置消息在一定时间后过期,默认是永不过期

消息过期时间,等于Destination的send方法中的timeToLive值加上发送时刻的GMD时间值。

如果timeToLive值等于零,则JMSExpiration被设为零,表示该消息永不过期.

如果发送后，在消息过期时间之后消息还没有被发送到目的地,则该消息被清除

	 textMessage.setJMSExpiration();

**JMSPriority** 消息优先级，从0-9 十个级别，0到4是普通消息，5-9是加急信息。JMS不要求MQ严格按照这十个优先级发送消息,但必须保证加急消息要先于普通消息到达.默认是4级

	textMessage.setJMSPriority();

**JMSMessageId** 唯一识别每个消息的标识由MQ产生

	textMessage.setJMSMessageID();

- 消息体 封装具体的消息数据(5种消息格式)

**TextMessage** 普通字符串消息,包含一个string

**Mapmessage** 一个Map类型的消息,key为String类型,而值为Java的基本类型

	   MapMessage mapMessage = session.createMapMessage();
        mapMessage.setString("key","value");
        messageProducer.send(mapMessage);

**BytesMessage** 二进制数组消息,包含一个byte[]

**StreamMessage** Java数据流消息,用标准操作来顺序的填充和读取

**ObjectMessage** 对象消息,包含一个可序列化的Java对象

- 消息属性 

如果需要消除消息头字段以外的值,那么可以使用消息属性,识别、去重、重点标注等非常有用的方法

	textMessage.setStringProperty()

**JMS的可靠性**

**持久的Queue**

非持久化:当服务器宕机,消息不存在

 	messageProducer.setDeliverMode(DeliverMode.NON_PERSISTENT) 

持久化:当服务器宕机,消息依然存在

	messageProducer.setDeliverMode(DeliverMode.PERSISTENT)

**持久化消息是队列的默认传送模式**

点对点模式是基于队列的，生产者发消息到队列，消费者从队列接收消息，队列的存在使得消息的异步传输成为可能。可以想象成发短信的情景。

1. 如果session关闭时有部分消息已经收到但还没有签收，那当消费者下次连接到相同的队列时，这些消息还会被再次接收。
2. 队列可以长久保存消息，直到消费者收到消息。消费者不需要因为担心消息会丢失而时刻和队列保持激活的连接状态，充分体现了异步传输模式的优势。


**持久的Topic(默认就非持久化的)**

	public class JmsConsumerPersist {
	    //设置目的地址URL
	    private static final String ACTIVE_URL = "tcp://120.77.237.175:61616";
	    //设置队列名称
	    private static final String TOPIC = "topic01";
	
	    public static void main(String[] args) throws Exception {
	
	        System.out.println("Topic 消费者02");
	        //1创建连接工厂,按照给定的url地址,采用默认用户名和密码
	        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_URL);
	        //2通过连接工厂,获得连接connection并启动访问
	        Connection connection = activeMQConnectionFactory.createConnection();
	        connection.setClientID("23");
	        //3创建会话session
	        //两个参数，第一个叫事务/第二个叫签收
	        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
	        //4创建目的地，(具体是队列还是主题topic)
	        Topic topic = session.createTopic(TOPIC);
	        //5创建持久化消息消费者
	        TopicSubscriber subscriber = session.createDurableSubscriber(topic, "remark....");
	        connection.start();
	        Message message = subscriber.receive();
	        while (message!=null){
	            TextMessage textMessage = (TextMessage)message;
	            System.out.println("收到的持久他topic是:"+ textMessage.getText());
	            message = subscriber.receive(4000L);
	        }
	        subscriber.close();
	        session.close();
	        connection.close();
	
	        System.out.println("持久化消息接收完毕=====================");
	
	
	    }
	}

	public class JmsProducePersist {
	    //设置目的地址URL
	    private static final String ACTIVE_URL = "tcp://120.77.237.175:61616";
	    //设置队列名称
	    private static final String TOPIC = "topic01";
	
	
	    public static void main(String[] args) throws Exception {
	        //1创建连接工厂,按照给定的url地址,采用默认用户名和密码
	        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_URL);
	        //2通过连接工厂,获得连接connection并启动访问
	        Connection connection = activeMQConnectionFactory.createConnection();
	        //3创建会话session
	        //两个参数，第一个叫事务/第二个叫签收
	        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
	        //4创建目的地，(具体是队列还是主题topic)
	        Topic topic = session.createTopic(TOPIC);
	        //5创建消息生产者
	        MessageProducer messageProducer = session.createProducer(topic);
	        //设置持久化模式
	        messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
	        connection.start();
	        //6通过使用messageProducer生产3条消息发送到MQ的队列里面
	        for (int i = 1; i <= 3; i++) {
	            //7创建消息,
	            TextMessage textMessage = session.createTextMessage("topic msg is " + i);//可以理解发送字符串
	            messageProducer.send(textMessage);
	        }
	        //9.关闭资源
	        messageProducer.close();
	        session.close();
	        connection.close();
	
	        System.out.println("持久化消息发送完毕===============");
	
	
	
	    }
	}



1. 一定要先运行一次消费者,等于向MQ注册,类似我订阅了这处主题
2. 然后再运行生产者发送信息,此时
3. 无论消费者是否在线，都会接收到,不在线的话,下次连接时候,会把没有收过的消息都接收下来
4. JMS Pub/Sub模式定义**了如何向一个内容节点发布和订阅消息，这些节点被称为topic。主题可以被认为是消息的**传输介质，发布者发布消息到主题，订阅者从主题订阅消息。主题使得消息订阅者和消息发布者保持相互独立。
5. 非持久订阅：非持久订阅只有当客户端处于激活状态，也就是和MQ保持连接状态才能收到发送到某个主题的消息。如果消费者处于离线状态，生产者发布的消息将会被作废，消费者永远不会收到。总结就是：先要订阅注册才能接收到发布，只给订阅者发布消息。
6. 持久订阅：客户端首先向MQ注册一个自己的clientID，当这个客户端处于离线时，生产者会为这个id保存所有发送到主题的消息，当客户端再次连接到MQ时，会根据客户端的id得到消息。
7. 那平时用持久订阅还是非持久订阅呢？如果消息必须被接收，那就用持久化订阅，当丢失消息能够被容忍，则用非持久订阅。

**事务**

事务偏生产者/签收偏接收者

**false** 只要执行send,就进入队列中。 关闭事务，那第二个签收参数设置需要有效

**true** 先执行send再执行commit,消息才被真正的提交到队列中.消息需要批量发送,需要缓冲区处理

		//生产者
        try {
            Session session = connection.createSession(true,Session.AUTO_ACKNOWLEDGE);
			.........
            session.commit();
        } catch (JMSException e) {
            session.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

		//消费者
		//注意消费者虽然设置了true，但没有commit，是可以接收到消息，但ActiveMQ后台一直会显示信息没有成功接收，一直可以接收到队列推送到的消息

事务开启的意义在于，如果对于多条必须同批次传输的消息，可以使用事务，如果一条传输失败，可以将事务回滚，再次传输，保证数据的完整性。

对于消息消费者来说，开启事务的话，可以避免消息被多次消费，以及后台和服务器数据的不一致性。举个栗子：
如果消息消费的createSession设置为ture，但是没有commit，此时就会造成非常严重的后果，那就是在后台看来消息已经被消费，但是对于服务器来说并没有接收到消息被消费，此时就有可能被多次消费。

**签收(俗称ack)**

Session.AUTO_ACKNOWLEDGE 自动签收(默认)

Session.CLIENT_ACKNOWLEDGE 手动签收

	message.acknowledge();	//设置了手动签收,必须手动ack

在事务性会话中，当一个事务被成功提交则消息被自动签收.如果事务回滚,则消息会被再次传送
非事务会话中,消息何时被确认取决于创建会话时的应答模式

对于开启事务时，设置手动签收和自动签收没有多大的意义，都默认自动签收，也就是说事务的优先级更高一些。

注意:如开启了事务,但没有commit,而手动签收acknowledge()了，消息是可以重复消费,因为事务没提交保证不了一致性

对于非事务，设置了手动签收,必须手动ack acknowledge()

# ActiveMQ的Broker #

不同的conf配置文件模拟不同的实例

	//使用多个配置文件启动activemq 
	// 以active02 启动mq 服务器
	./activemq start xbean:file:/opt/AcitveMQ/conf/activemq02.xml 

**嵌入式Broker**

把ActiveMQ嵌入到java代码中作为微小的服务器，不使用Linux上的服务
	
pom文件引入

	 <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.9.5</version>
    </dependency>

	public class EmbedBroker {
	    public static void main(String[] args) throws Exception {
	        BrokerService brokerService = new BrokerService();
	        brokerService.setUseJmx(true);
	        brokerService.addConnector("tcp://localhost:61616");
	        brokerService.start();
	    }
	}

一般实际应用中都是以使用以Linux上的配置服务,Broker的嵌入式可以只作为了解

# Spring整合ActiveMQ #

**pom文件**

  	<!--activemq对JMS的支持,整合Spring和Activemq-->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jms</artifactId>
            <version>4.3.23.RELEASE</version>
        </dependency>
        <!--  pool 池化包相关的支持  -->
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-pool</artifactId>
            <version>5.15.9</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>4.3.23.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-orm</artifactId>
            <version>4.3.23.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>4.3.23.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aop</artifactId>
            <version>4.3.23.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>1.6.1</version>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.5.4</version>
        </dependency>
        <dependency>
            <groupId>cglib</groupId>
            <artifactId>cglib</artifactId>
            <version>2.1_2</version>
        </dependency>

**applicationContext.xml**

	<?xml version="1.0" encoding="UTF-8"?>
	<beans xmlns="http://www.springframework.org/schema/beans"
	       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	       xmlns:context="http://www.springframework.org/schema/context"
	
	       xsi:schemaLocation="http://www.springframework.org/schema/beans
	       http://www.springframework.org/schema/beans/spring-beans.xsd
	          http://www.springframework.org/schema/context
	         http://www.springframework.org/schema/context/spring-context.xsd
	        http://www.springframework.org/schema/aop
	     http://www.springframework.org/schema/aop/spring-aop.xsd
	     http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd">
	
	    <context:component-scan base-package="com.my.activemq"/>
	
	    <!--配置生产者-->
	    <bean id="jmsFactory" class="org.apache.activemq.pool.PooledConnectionFactory"  destroy-method="stop">
	        <property name="connectionFactory">
	            <!--真正可以产生Connection的ConnectionFactory，由对应的JMS服务厂商提供-->
	            <bean class="org.apache.activemq.ActiveMQConnectionFactory">
	                <property name="brokerURL" value="tcp://120.77.237.175:61616"></property>
	            </bean>
	        </property>
	        <!--配置最大连接数-->
	        <property name="maxConnections" value="100"></property>
	    </bean>
	
	    <!--这个是队列目的地，点对点的-->
	    <bean id="destinationQueue" class="org.apache.activemq.command.ActiveMQQueue">
	        <constructor-arg index="0" value="spring-active-queue" />
	    </bean>
	
	    <!--这个是订阅主题-->
	    <bean id="destinationTopic" class="org.apache.activemq.command.ActiveMQTopic">
	        <constructor-arg index="0" value="spring-active-topic" />
	    </bean>
	
	    <!--Spring提供JMS工具类,它可以进行消息发送、接收等-->
	    <bean id="jmsTemplate" class="org.springframework.jms.core.JmsTemplate">
	        <property name="connectionFactory" ref="jmsFactory"/>
	        <property name="defaultDestination" ref="destinationTopic"/>
	        <property name="messageConverter">
	            <bean class="org.springframework.jms.support.converter.SimpleMessageConverter"/>
	        </property>
	    </bean>
	
	    <!--配置监听器程序-->
	    <bean id="jmsContainer" class="org.springframework.jms.listener.DefaultMessageListenerContainer">
	        <property name="connectionFactory" ref="jmsFactory"/>
	        <property name="destination" ref="destinationTopic"/>
	        <!--实现 MyMessageListener-->
	        <property name="messageListener" ref="myMessageListener"/>
	    </bean>
	</beans>

**生产者**

	@Service
	public class SpringMQProducer {
	
	    @Autowired
	    private JmsTemplate jmsTemplate;
	
	    public static void main(String[] args) {
	
	        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
	        SpringMQProducer producer = (SpringMQProducer)ctx.getBean("springMQProducer");
	        producer.jmsTemplate.send((session) -> {
	
	            TextMessage textMessage = session.createTextMessage("Spring整合ActiveMQ发送消案例3for listener");
	            return textMessage;
	        });
	
	        System.out.println("生产者信息发送成功!");
	    }
	}

**消费者**
	
	@Service
	public class SpringMQConsumer {
	
	    @Autowired
	    private JmsTemplate jmsTemplate;
	
	    public static void main(String[] args) {
	
	        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
	        SpringMQConsumer producer = (SpringMQConsumer)ctx.getBean("springMQConsumer");
	        String message = (String) producer.jmsTemplate.receiveAndConvert();
	        System.out.println(message);
	
	        System.out.println("消费者接收成功!");
	    }
	}


**实现监听在不需要启动消费者就可以接收到信息**

	@Component
	public class MyMessageListener implements MessageListener {
	    @Override
	    public void onMessage(Message message) {
	        if (message != null && message instanceof TextMessage){
	            TextMessage textMessage = (TextMessage)message;
	            try {
	                String text = textMessage.getText();
	                System.out.println("监听器接收到的消息是："+text);
	            } catch (JMSException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}


# SpringBoot整合ActiveMQ #

pom

	<properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-activemq</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>


**生产者Queue**

**application.yml**

	server:
	  port:8888
	spring:
	  activemq:
	    broker-url: tcp://120.77.237.175:61616
	    user: admin
	    password: admin
	  jms:
	    pub-sub-domain: false # false = queue    true =  topic
	
	myqueue:
	  boot-activemq-queue


**com.my.springboot.springbootproducer.Config**

	@Component   // 让spring 管理的注解，相当于spring 中在xml 中写了个bean
	@EnableJms  // 开启jms 适配
	public class MyBean {
	
	    @Value("${myqueue}")
	    private String myQueue;     // 注入配置文件中的 myqueue
	
		/**配置一个Bean,相当于<bean id="" class />**/
	    @Bean
	    public ActiveMQQueue queue(){
	        return new ActiveMQQueue(myQueue);
	    }
	}

**com.my.springboot.springbootproducer.Produce**

	@Component
	public class QueueProduce {
	
		/**把yml配置的JMS注入进来**
	    @Autowired
	    private JmsMessagingTemplate jmsMessagingTemplate;
	
		/**把自定义配置里的Bean注入进来**/
	    @Autowired
	    private Queue queue;
	
	
	    public void produceMessage(){
	        jmsMessagingTemplate.convertAndSend(queue,UUID.randomUUID().toString().substring(0,6));
	    }

		  /*设置定时发送*/
		/**启动main()方法自动发送**/
		/***main()方法必须开启@EnableScheduling才能定时发送**/
	    @Scheduled(fixedDelay = 3000L)  /*每3秒发送一次*/
	    public void produceMessageSchedule(){
	        jmsMessagingTemplate.convertAndSend(queue,"*********Schedule:"+UUID.randomUUID().toString().substring(0,6));
	        System.out.println("定时发送完成!");
	    }
	}


**com.my.springboot.springbootproducer.SpringbootProducerApplicationTests**

	 @Test
    public void testProduceSendMsg(){
        queueProduce.produceMessage();
    }

**消费者Queue**(配置相同)

**com.my.springboot.consumer.springbootconsumer.Consume**

	@Component
	public class QueueConsume {
		/**启动监听器自动接收**/
		/**main()方法自启接收**/
	    @JmsListener(destination = "${myqueue}")
	    public void receiveMsg(TextMessage textMessage) throws Exception{
	        System.out.println("消费者接收到的消息是："+textMessage.getText());
	    }
	}

**Topic消费者和生产者基本同上，只是配置文件和队列改下**

application.yml

	server:
	  port: 6666
	spring:
	  activemq:
	    broker-url: tcp://120.77.237.175:61616
	    user: admin
	    password: admin
	  jms:
	    pub-sub-domain: true # false = queue    true =  topic
	
	#自定义队列名称
	mytopic:
	  boot-activemq-topic

**com.springboot.consumer.topic.Config**

	@Component
	@EnableJms
	public class MyBean {
	    @Value("${mytopic}")
	    private String myTopic;
	
	    @Bean
	    public Topic topic(){
	        return new ActiveMQTopic(myTopic);
	    }
	}


# ActiveMQ的传输协议 #

ActiveMQ支持的协议有:

- TCP 默认的协议,性能相对可以
- UDP 性能比TCP更好，但是不具有可靠性
- NIO 基于TCP协议之上的,进行了扩展和优化,具有更好的扩展性
- SSL 安全链接
- HTTP(S) 基于HTTP或者HTTPS
- VM VM本身不是协议，当客户端和代理在同一个Java虚拟机(VM)中运行进，他们之间需要通信,但不想占用网络通道,而是直接通信,可以使用该方式

TCP和NIO是重点

各种协议对比 : http://activemq.apache.org/configuring-version-5-transports.html

 	 	<transportConnectors>
            <!-- DOS protection, limit concurrent connections to 1000 and frame size to 100MB -->
            <transportConnector name="openwire" uri="tcp://0.0.0.0:61616?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
            <transportConnector name="amqp" uri="amqp://0.0.0.0:5672?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
            <transportConnector name="stomp" uri="stomp://0.0.0.0:61613?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
            <transportConnector name="mqtt" uri="mqtt://0.0.0.0:1883?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
            <transportConnector name="ws" uri="ws://0.0.0.0:61614?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
        </transportConnectors>

默认是使用 openwire 也就是 tcp 连接  
默认的Broker 配置，TCP 的Client 监听端口 61616 ，在网络上传输数据，必须序列化数据，消息是通过一个 write protocol 来序列化为字节流。默认情况 ActiveMQ 会把 wire protocol 叫做 Open Wire ，它的目的是促使网络上的效率和数据快速交互 。

使用tcp 的一些优化方案：tcp://hostname:port?key=value

参数详情参考：http://activemq.apache.org/tcp-transport-reference


**NIO协议为ActiveMQ提供更好的性能**

NIO协议和TCP协议类似但NIO更侧重于底层的访问操作。它允许开发人员对同一资源可有更多的client调用和服务端有更多的负载。

适合NIO 使用的场景：

1. 当有大量的Client 连接到Broker 上 ， 使用NIO 比使用 tcp 需要更少的线程数量，所以使用 NIO 
2. 可能对于 Broker 有一个很迟钝的网络传输， NIO 的性能高于 TCP 
3. NIO连接的uri形式： nio://hostname:port?key=value



在/opt/AcitveMQ/conf/activemq.xml添加NIO协议

 		<transportConnectors>
            <!-- DOS protection, limit concurrent connections to 1000 and frame size to 100MB -->
            <transportConnector name="openwire" uri="tcp://0.0.0.0:61616?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
            <transportConnector name="amqp" uri="amqp://0.0.0.0:5672?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
            <transportConnector name="stomp" uri="stomp://0.0.0.0:61613?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
            <transportConnector name="mqtt" uri="mqtt://0.0.0.0:1883?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
            <transportConnector name="ws" uri="ws://0.0.0.0:61614?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
            <transportConnector name="nio" uri="nio://0.0.0.0:61618?trace=true"/> <!--新增的NIO--->
         </transportConnectors>

而使用 NIO 协议，代码修改量极小，只需同时将消息生产者和消费者的URL修改即可：

	//private static final String ACTIVE_URL = "tcp://120.77.237.175:61616";
	private static final String ACTIVE_URL = "nio://120.77.237.175:61618";


**NIO增强**

URI格式以nio开头，表示这个端口使用tcp协议为基础的NIO网络IO模型，但这样设置让它只支持 tcp 、nio的连接协议。如何让它支持多种协议？

除了NIO这个高效的协议外，activeMQ还对所有协议进行了优化，提供了增强型NIO。这个模式，让这个端口既支持NIO网络模型，又支持多个协议。其实就是使用auto关键字，相当于把所有协议封装起来，只暴露一个端口，使得使用起来更加灵活，用起来也很简单

开启auto ，地址:http://activemq.apache.org/auto

使用auto的方式就相当于四合一协议：STOMP  AMQP  MQTT  TCP  NIO    

auto就像是一个网络协议的适配器，可以自动检测协议的类型，并作出匹配

	<transportConnector name="auto" uri="auto://localhost:5671?auto.protocols=default,stomp"/>

增加auto+nio协议

	<transportConnector name="auto+nio" uri="auto+nio://0.0.0.0:61619?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600&amp;org.apache.activemq.transport.nio.SelectorManager.corelPoolSize=20&amp;org.apache.activemq.transport.nio.SelectorManager.maximumPoolSize=50"/>

现在把代码改成tcp或者nio都会自动切换

	//private static final String ACTIVE_URL = "tcp://120.77.237.175:61619";
	private static final String ACTIVE_URL = "nio://120.77.237.175:61619";

对于NIO和tcp的代码相同，但不代表使用其他协议代码相同，因为底层配置不同，其他协议如果使用需要去修改代码  


# ActiveMQ的消息储存和可持久化 #

为了避免意外宕机以后丢失信息，需要做到重启后可以恢复消息队列，消息系统一般都会采用持久化机制。

ActiveMQ的消息持久化机制有JDBC，AMQ（了解），KahaDB(默认)和LevelDB（了解），无论使用哪种持久化方式，消息的存储逻辑都是一致的。

官网:http://activemq.apache.org/persistence  

就是发送者将消息发送出去后，消息中心首先将消息存储到本地数据文件、内存数据库或者远程数据库等，然后试图将消息发送给接收者，发送成功则将消息从存储中删除，失败则继续尝试。

消息中心启动以后首先要检查指定的存储位置，如果有未发送成功的消息，则需要把消息发送出去。

**AMQ方式(了解)**

基于文件的存储方式,是以前的默认消息存储,现在不用了（ActiveMQ5.3以前用的）

**KahaDB方式**

KahaDB是从ActiveMQ 5.4开始默认的持久化插件，是目前默认的存储方式,可用于任何场景,提高了性能和恢复能力

消息存储使用一个事务日志和仅仅用一个索引文件来存储它所有的地址

kahaDB是一个专门针对消息持久化的解决方案,它对典型的消息使用模式进行了优化

数据追加到data logs中。当不再需要log文件中的数据的时候,log文件会被丢弃

	<!--/opt/AcitveMQ/conf/activemq.xml-->
	<!--默认的配置使用是就是KahaDb方式-->
	 <persistenceAdapter>
            <kahaDB directory="${activemq.data}/kahadb"/>
   	 </persistenceAdapter>


KahaDB主要特性

1. 日志形式存储消息；
2. 消息索引以B-Tree结构存储，可以快速更新；
3. 完全支持JMS事务；
4. 支持多种恢复机制；

KahaDB的结构

消息存储在基于文件的数据日志中。如果消息发送成功，变标记为可删除的。系统会周期性的清除或者归档日志文件。

消息文件的位置索引存储在内存中，这样能快速定位到。定期将内存中的消息索引保存到metadata store中，避免大量消息未发送时，消息索引占用过多内存空间。

		-rw-r--r-- 1 root root 33554432 Aug 19 16:54 db-1.log
		-rw-r--r-- 1 root root   106496 Aug 20 11:11 db.data
		-rw-r--r-- 1 root root    49240 Aug 20 11:11 db.redo
		-rw-r--r-- 1 root root        8 Aug 19 16:51 lock

**db-<number>.log(存数据)**:KahaDB存储消息到预定义大小的数据记录中,文件命名为db-<number>.log 。当数据文件已满时,一个新的文件会随之创建,number数值也会随之递增,它随着消息数量的增多,如每32M一个文件,文件名按照数字进行编号,如db-1.log db-2.log db-3.log.....当不再有引用到数据文件中的任何消息时，文件会被删除或归档

![](https://www.eyesmoons.com/attachment/20190801/a4cbad9ef8fd4f34ad02e4bba8d2d491.png)

**db.data(存索引)**:该文件包含了持久化的BTree索引，索引了消息数据记录中的消息，他是消息的索引文件，本质上是B-Tree(B树)，使用BTree作为索引指向db-[number].log文件存储的消息。

db.free：当前db.data文件里面哪些页面是空的，文件具体内容是所有空闲页的ID

db.redo：用来进行消息恢复，如果KahaDB消息存储再强制退出后启动，用于恢复BTree索引。

lock:文件锁，表示当前获得KahaDB读写权限的Broker

四类文件+一把锁   ==》  KahaDB 

**LeavelDB方式(了解)** 

从ActiveMQ 5.6版本之后，又推出了LevelDB的持久化引擎。
目前默认的持久化方式仍然是KahaDB，不过LevelDB持久化性能高于KahaDB，它不使用BTree索引，而是使用基于LevalDB的索引，可能是以后的趋势。

在ActiveMQ 5.9版本提供了基于LevelDB和Zookeeper的数据复制方式，用于Master-slave方式的首选数据复制方案。

**JDBC方式**

MQ+MYSQL

![](https://www.eyesmoons.com/attachment/20190801/8e8f6117b5904fddb621135ad6c4dbae.png)

一、首先把Mysql驱动包放到ActiveMQ的lib文件夹下

二、修改activemq.xml配置
	
	<!--修改前KahaDB-->
	<persistenceAdapter>
    	<KahaDB directory = "${activemq.data}/kahadb"/>
	</persistenceAdapter>

	<!--修改后jdbcPersistenceAdapter-->
	<persistenceAdapter> 
	  <jdbcPersistenceAdapter dataSource="#mysql-ds" createTableOnStartup=true /> 
	</persistenceAdapter>
	<!--datasource：指定将要引用的持久化数据库的bean名称-->
	<!--createTableOnStartup：是否在启动的时候创建数据表，默认是true，这样每次启动MQ时都会创建数据表，一般是第一次启动设置为true之后改成false-->

三、在activemq.xml增加数据库连接池配置(需要在broker标签外设置bean)

	<!--如果不需要默认的apache连接池，要把相应的jar包配进AcitveMQ的lib后修改-->
	<bean id="mysql-ds" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close"> 
	    <property name="driverClassName" value="com.mysql.jdbc.Driver"/> 
	    <property name="url" value="jdbc:mysql://localhost:3306/activemq?relaxAutoCommit=true"/> 
	    <property name="username" value="*****"/> 
	    <property name="password" value="*****"/> 
	    <property name="poolPreparedStatements" value="true"/> 
	</bean> 

四、建一个名为activemq的数据库,重启MQ服务器，就会自动在数据库中生成以下三张表

**activemq_msgs**

- ID:自增的数据库主键
- CONTAINER:消息的Destination
- MSGID_PROD:消息发送者的主键
- MSG_SEQ:是发送消息的顺序,MSGID_PROD+MSG_SEQ可以组成JMS的MessageID
- EXPIRATION:消息的过期时间,存储的是从1970-01-01到现在的毫秒数
- MSG:消息本体的Java序列化对象的二进制数据
- PRIORITY:优先级，从0-9，数值越大优先级越高

**activemq_acks**用于存储订阅关系.如果是持久化Topic，订阅者和服务器的订阅关系在这个表保存

- CONTAINER:消息的Destination
- SUB_DEST:如果是使用Static集群,这个字段会有集群其他系统的信息
- CLIENT_ID:每个订阅者都必须一个唯一的客户端ID用以区分
- SUB_NAME:订阅者名称
- SELECTOR:选择器，可以选择只消息满足条件的消息,条件可以用自定义属性实现,可支持多属性AND和OR操作
- LAST_ACKED_ID:记录消费过的消息的ID

**activemq_lock**

在集群环境下才有用，只有一个Broker可以获得消息，成为master broker，其他的只能作为备份等待master broker不可用时，才可能成为下一个master broker。这个表用来记录那个Broker是当前的master broker

**注意，一定要开启持久化或者spring配置持久化**

  	messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT); 

在点对点类型中：

- a.当DeliveryMode设置为NON_PERSISTENCE时，消息保存在内存中；
- b.当DeliveryMode设置为PERSISTENCE时，消息保存在broker相应的文件或者数据库中；
- c.而且点对点类型的消息一旦被消费就从broker中删除。

在发布订阅类型中：要先运行消费者，再运行生产者

数据库情况：

- 如果是queue类型的消息： 在没有消费者消费的情况下会将消息保存到activemq_msgs表中，只要有任意一个消费者消费了，这些消息将会立即被删除
- 如果是topic类型的消息： 先启动消费者，会把订阅者根据client_id保存在activemq_acks中，然后消费者消息保存在activemq_msgs表中

**开发中的坑**

在配置关系型数据库作为ActiveMQ的持久化存储方案时，要注意以下三点：

- a，记得将需要使用到的相关jar包放到MQ安装目录的lib下，默认连接池使用的是dbcp，如果要使用c3p0等其他连接池，也要加入相关jar包
- b，createTablesOnStartup属性默认为true，当第一次启动MQ时，会自动在数据库中创建表。启动完成之后可以设置为false
- c，下划线坑爹，启动时报异常，则可能是操作系统名字中有“_”。请更改机器名并且重启。

**JDBC加强版消息存储(加入高速缓存机制Journal)**

这种策略叫带有高速缓存的JDBC解决方案，相当于在mysql之前加了一个高速缓存。这种方法克服了JDBC store的不足，JDBC每次请求过来，都需要去写库和读库。

ActiveMQ Journey使用高速缓存写入技术，大大提高了性能。

当消费者的消费速度能够及时跟上生产者生产速度时，journey文件能够大大减少写入DB消息的需要。

举个例子：

生产者生产1000条消息，这1000条消息会保存到journey文件中，如果消费者消费速度很快，在journey文件还没有同步到DB之前，消费者已经消费了90%的消息，那么这个时候只需要同步剩下的10%消息到DB。如果消费者消费的速度很慢，这时journey文件可以使消息以批量方式写入DB。

配置

	<!--修改前PersistenceAdapter-->
	<persistenceAdapter> 
	  <jdbcPersistenceAdapter dataSource="#mysql-ds"/> 
	</persistenceAdapter>


	<!--修改后jdbcPersistenceAdapter-->
	<persistenceFactory>
      <journalPersistenceAdapterFactory journalLogFiles="5" dataDirectory="activemq-data"  dataSource="#mysql-ds" useJournel="true" useQuickJournel="true"/>
	</persistenceFactory>

其它的就跟JDBC存储方式操作一致

JDBC Store和JDBC Message Store with ActiveMQ Journal的区别：

1. Jdbc with journal的性能优于jdbc
2. Jdbc用于master/slave模式的数据库分享
3. Jdbc with journal不能用于master/slave模式 
4. 一般情况下,推荐使用jdbc with journal

**总结**

持久化消息主要是指：MQ所在的服务器宕机了，消息不会丢失的机制。

持久化机制演化过程：最初的AMQ方案到V4版本推出的高性能事务支持插件，并且同步推出了关于关系型数据库的方案。V5.3版本又推出了对KahaDB的支持，后来V5.8又支持levelDB，到现在提供了标准版的Zookeeper+LevelDB集群化方案。

ActiveMQ持久化的机制有：

- AMQ：基于日志文件
- KahaDB：基于日志文件，V5.4以后默认支持的持久化插件
- JDBC：基于第三方数据库
- LevelDB：基于文件的本地数据库存储
- Replicated LevelDB Store：基于Zookeeper+LevelDB的数据复制方式，用于master-slave方式的首选数据复制方案。


无论使用哪一种持久化方式，消息的存储逻辑都是一致的。

# AvtiveMQ多节点集群 #

暂略

# AvtiveMQ高级特性 #

**引入消息队列后 如何保证高可用性**

持久化、事务、签收、 以及带复制的 Leavel DB + zookeeper 主从集群搭建等等

**异步投递Async Sends**

ActiveMQ支持同步和异步两种发送模式将消息发送到Broker，模式的选择对发送延时有巨大的影响。使用异步发送能显著提高发送的性能。

ActiveMQ默认使用异步发送模式：除非明确指定使用同步发送的方式或者在未使用事务的前提下发送持久化消息，这两种情况都是同步发送的。

异步发送带来了性能上的提升，但是会消耗较多的client端内存也会导致Broker端性能增加，消息可能会有概率丢失。

	/**三种开启异步投递的方法**/
	/**第一种**/
	cf = new ActiveMQConnectionFactory("tcp://locahost:61616?jms.useAsyncSend=true");
	/**第二种**/
	((ActiveMQConnectionFactory)connectionFactory).setUseAsyncSend(true);
	/**第三种**/
	((ActiveMQConnection)connection).setUseAsyncSend(true);

**异步发送如何确定消息发送成功**

生产者设置useAsyncSend=true，使用producer.send(msg)持续发送消息。由于消息不阻塞，生产者会认为所有的消息均被成功发送至MQ，如果MQ突然宕机，此时生产者内存中尚未被发送的消息都将会丢失。所以正确的异步发送方式是需要接收回调的。所以，同步发送和异步发送的区别就在这里：

**同步发送等send不阻塞了就表示发送成功了，异步发送需要接受回执并由客户端再判断一次是否发送成功。**

	public class JmsProduceAsync {
	
	    //设置目的地址URL
	    //开启异步投递第一种方式
	    private static final String ACTIVE_URL = "tcp://120.77.237.175:61619?jms.useAsyncSend=true";
	
	    //设置队列名称
	    private static final String QUEUE = "queue_async";
	
	    public static void main(String[] args) throws Exception {
	        //1创建连接工厂,按照给定的url地址,采用默认用户名和密码
	        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_URL);
	        //开启异步投递第二种方式
	       // activeMQConnectionFactory.setUseAsyncSend(true);
	
	        //2通过连接工厂,获得连接connection并启动访问
	        Connection connection = activeMQConnectionFactory.createConnection();
	        //开启异步投递第三种方式
	        //ActiveMQConnection activeMQConnection = (ActiveMQConnection) connection;
	        //activeMQConnection.setUseAsyncSend(true);
	        connection.start();
	        //3创建会话session
	        //两个参数，第一个叫事务/第二个叫签收
	        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
	        //4创建目的地，(具体是队列还是主题topic)
	        Queue queue = session.createQueue(QUEUE);
	        //5创建消息生产者
	        ActiveMQMessageProducer activeMQMessageProducer = (ActiveMQMessageProducer) session.createProducer(queue);
	
	        //6通过使用messageProducer生产3条消息发送到MQ的队列里面
	        for (int i = 1; i <= 3; i++) {
	            TextMessage textMessage = session.createTextMessage("async msg is " + i);//可以理解发送字符串
	            textMessage.setJMSMessageID(UUID.randomUUID().toString()+"----order id");
	            String messageID = textMessage.getJMSMessageID();
	            activeMQMessageProducer.send(textMessage, new AsyncCallback() {
	                @OverrideJmsConsumerTx
	                public void onSuccess() {
	                    System.out.println(messageID);
	                    System.out.println("async send success!");
	                }
	
	                @Override
	                public void onException(JMSException e) {
	                    /**如果异步发送失败可以人工处理重发**/
	                    System.out.println(messageID);
	                    System.out.println("async send fail!");
	                }
	            });
	
	        }
	
	        //9.关闭资源
	        activeMQMessageProducer.close();
	        session.close();
	        connection.close();
	
	        System.out.println("消息发送完毕===============");
	
	
	    }
	}


**延迟投递和定时投递**

- AMQ_SCHEDULED_DELAY long 延迟投递的时间

- AMQ_SCHEDULED_PERIOD long 重复投递的时间间隔

- AMQ_SCHEDULED_REPEAT int 重复投递次数

- AMQ_SCHEDULED_CRON String Cron表达式

		<!--activemq.xml配置文件增加schedulerSupport="true"-->
		<broker xmlns="http://activemq.apache.org/schema/core" brokerName="localhost" dataDirectory="${activemq.data}" schedulerSupport="true" >
Demo

		public class JmsProduceSchedule {
		
		    //设置目的地址URL
		    private static final String ACTIVE_URL = "nio://120.77.237.175:61619";
		    //private static final String ACTIVE_URL = "nio://120.77.237.175:61619";
		    //设置队列名称
		    private static final String QUEUE = "queue_schedule";
		
		    public static void main(String[] args) throws Exception {
		        //1创建连接工厂,按照给定的url地址,采用默认用户名和密码
		        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_URL);
		        //2通过连接工厂,获得连接connection并启动访问
		        Connection connection = activeMQConnectionFactory.createConnection();
		        connection.start();
		        //3创建会话session
		        //两个参数，第一个叫事务/第二个叫签收
		        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
		        //4创建目的地，(具体是队列还是主题topic)
		        Queue queue = session.createQueue(QUEUE);
		        //5创建消息生产者
		        MessageProducer messageProducer = session.createProducer(queue);
		
		        long delay = 3 * 1000;
		        long period = 4 * 1000;
		        int repeat = 5;
		
		        messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
		        //6通过使用messageProducer生产3条消息发送到MQ的队列里面
		        for (int i = 1; i <= 3; i++) {
		            TextMessage textMessage = session.createTextMessage("msg is " + i);//可以理解发送字符串
		            textMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delay);
		            textMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD,period);
		            textMessage.setIntProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT,repeat);
		
		            messageProducer.send(textMessage);
		        }
		
		        //9.关闭资源
		        messageProducer.close();
		        session.close();
		        connection.close();
		
		        System.out.println("消息发送完毕===============");
		    }
		}


**ActiveMQ消费重试机制**

具体哪些情况会引发消息重发呢？

1. client用了transactions且在session中调用了rollback()
2. client用了transactions且在调用commit()之前关闭或者没有commit()
3. client在签收模式下调用了recover()

消息重发的间隔和次数：间隔：1s；次数：6次

有毒消息Poison ACK的理解(默认最大重发6次，第7次进入死信队列)

一个消息被重发6次，消费端会给MQ发送一个“Poison ack”表示这个消息有毒，告诉Broker不要再发了。这个时候broker会把这个消息放到DLQ（死信队列）。

因此我们自己可以设置消息重发的间隔和次数,分别为：**maximumRedeliveries**和**initialRedeliveryDelay**

	public class JmsConsumerRedelivery {
	
	    //设置目的地址URL
	    private static final String ACTIVE_URL = "nio://120.77.237.175:61619";
	    //private static final String ACTIVE_URL = "nio://120.77.237.175:61619";
	    //设置队列名称
	    private static final String QUEUE = "queue01";
	
	    public static void main(String[] args) throws Exception {
	        //1创建连接工厂,按照给定的url地址,采用默认用户名和密码
	        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVE_URL);
	
	        /*设置消息重发的次数*/
	        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
	        redeliveryPolicy.setMaximumRedeliveries(3);
	        activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
	
	        //2通过连接工厂,获得连接connection并启动访问
	        Connection connection = activeMQConnectionFactory.createConnection();
	        connection.start();
	        //3创建会话session
	        //两个参数，第一个叫事务/第二个叫签收
	        Session session = connection.createSession(true,Session.AUTO_ACKNOWLEDGE);
	        //4创建目的地，(具体是队列还是主题topic)
	        Queue queue = session.createQueue(QUEUE);
	        //5创建消息消费者
	        MessageConsumer messageConsumer = session.createConsumer(queue);
	
	        /**接收方式二**/
	        /**通过监听的方式来接收消息**/
	        /**异步非阴塞方法监听器onMessage()**/
	        /**订阅者或者接收者通过MessageConsumer的setMessageListener(MessageListener listener)注册一个消息监听器,当消息到达之后,系统自动调用
	         监听器MessageListener的 onMessage(Message message)方法**/
	        messageConsumer.setMessageListener(new MessageListener() {
	            @Override
	            public void onMessage(Message message){
	                if(message!=null && message instanceof TextMessage){
	                    TextMessage textMessage = (TextMessage)message;
	                    try {
	
	                        System.out.println("通过Listener接收到的信息是： "+textMessage.getText());
	                    } catch (JMSException e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	        });
	        System.in.read();
	        messageConsumer.close();
	        session.close();
	        connection.close();
	
	        System.out.println("消息接收完毕=====================");
	
	
	    }
	}


属性说明:

- **collisionAvoidanceFactor** 设置防止冲突范围的正负百分比，只有启用useCollisionAvoidance参数时才生效。也就是在延迟时间上再加一个时间波动范围。**默认值为:0.15**
- **maximumRedeliveries** 最大重传次数，达到最大重连次数后抛出异常。为-1时不限制次数，为0时表示不进行重传。**默认值为:6**
- **maximumRedeliveryDelay** 最大传送延迟，只在useExponentialBackOff为true时有效（V5.5），假设首次重连间隔为10ms，倍数为2，那么第二次重连时间间隔为 20ms，第三次重连时间间隔为40ms，当重连时间间隔大的最大重连时间间隔时，以后每次重连时间间隔都为最大重连时间间隔。**默认值为:-1**
- initialRedeliveryDelay  初始重发延迟时间 **默认值为:1000L**
- redeliveryDelay 重发延迟时间，当initialRedeliveryDelay=0时生效（v5.4） **默认值为:1000L**
- **useCollisionAvoidance** 启用防止冲突功能，因为消息接收时是可以使用多线程并发处理的，应该是为了重发的安全性，避开所有并发线程都在同一个时间点进行消息接收处理。所有线程在同一个时间点处理时会发生什么问题呢？应该没有问题，只是为了平衡broker处理性能，不会有时很忙，有时很空闲。**默认值为:false**
- **useExponentialBackOff**  启用指数倍数递增的方式增加延迟时间。**默认值为:false**
- **backOffMultiplier**  重连时间间隔递增倍数，只有值大于1和启用useExponentialBackOff参数时才生效。**默认值为:5**

**整合Spring后使用**

    <!--定义Redelivery（重发机制）重发机制-->
     <bean id="activeMQRedeliveryPolicy" class="org.apache.activemq.RedeliveryPolicy">
		<!--是否在每次尝试重新发送失败后,增长这个等待时间-->
         <property name="useExponentialBackOff" value="true"/>
		<!--重发次数,默认为6次,这里设置为3次-->
         <property name="maximumRedeliveries" value="3"/>
		<!--重发时间间隔，默认为1次-->
         <property name="initialRedeliveryDelay" value="1000"/>
		<!--第一次失败后重新发送之间等待500毫秒,第二次失败再等待500*2毫秒这里的2就是value-->
         <property name="backOffMultiplier" value="2"/>
		<!--最大传送延迟，只在useExponentialBackOff为true时有效（V5.5），假设首次重连间隔为10ms，倍数为2，那么第二次重连时间间隔为 20ms，第三次重连时间间隔为40ms，当重连时间间隔大的最大重连时间间隔时，以后每次重连时间间隔都为最大重连时间间隔-->
         <property name="maximumRedeliveryDelay" value="1000"/>
     </bean>

**死信队列**

ActiveMQ中引入了死信队列这个概念，即一条消息被重发了多次后，将会被MQ移入到DLQ中，开发人员可以在这个队列中查看出错的消息，进行人工处理。

死信队列的使用

![](https://www.eyesmoons.com/attachment/20190807/781038264b4f4299b876f774eadbf675.png)

一般生产环境在使用MQ的时候设计两个队列：一个核心业务队列，一个是死信队列

核心业务队列：比如上图用来让订单系统发送订单消息的，另一个死信队列就是用来存储异常情况的

死信队列的配置

1. **SharedDeadLetterStrategy**

将所有的DeadLetter保存在一个共享队列中，这是AciveMQ broker端默认策略，共享队列默认为“ActiveMQ.DLQ”，可通过“deadLetterQueue”属性来设置。

	<deadLetterstrategy>
	    <sharedDeadLetterStrategy deadLetterQueue="DLQ-QUEUE" />
	</deadLetterstrategy>

2. **IndividualDeadLetterStrategy**

把DeadLetter放入各自的死信通道中
对于Queue而言，死信通道的前缀默认为"ActiveMQ.DLQ.Queue"
对于Topic而言，死信通道的前缀默认为"ActiveMQ.DLQ.Topic"

比如队列Order,那么它对应的死信通道为"ActiveMQ.DLQ.Queue.Order"
我们使用"queuePrefix" "topicPrefix"来指定上述前缀

默认情况下,无论是Topic还是Queue,broker将使用Queue来保存DeadLeader,即死信通道通常为Queue;不过开发者也可以指定为Topic

	<policyEntry queue="order">
		<deadLetterStrategy>
			<individualDeadLetterStrategy queuePrefix="DLQ." useQueueForQueueMessages="false"/>
		</deadLetterStrategy>
	</policyEntry>

将队列Order中出现的DeadLetter保存在DLQ.Order中，不过DLQ.Order为Topic

自动删除过期消息

有时需要直接删除过期的消息而不需要发送到死队列中,"processExpired"表示是否将过期消息放入死信队列，默认为true;

	<policyEntry queue=">">
		<deadLetterStrategy>
			<shareDeadLetterStrategy processExpired="false"/>
		</deadLetterStrategy>
	</policyEntry>

存放非持久消息到死队列中

默认情况下,Activemq不会把非持久的死消息发送到死信队列中

processNonPersistent表示是否将"非持久化"消息放入死信队列,默认为false

非持久性如果你想把非持久的消息发送到死队列中,需要设置属性processNonPersistent="true"

	<policyEntry queue=">">
		<deadLetterStrategy>
			<shareDeadLetterStrategy processNonPersistent="true"/>
		</deadLetterStrategy>
	</policyEntry>

**如何保证消息不被重复消费，以及幂等性问题**

网络延迟传输中，会造成进行MQ重试中，在重试过程中，可能会造成重复消费。

如果消息是做数据库的插入操作，给这个消息做一个唯一主键，就算重复消费也会出现主键冲突，避免数据库出现脏数据。

或者准备一个第三方服务来做消费记录。以redis为例，给消息分配一个全局id，只要消费过该消息，将<id,message>以K-V形式写入redis。消费者消费前先去redis查询有没有消费即可。