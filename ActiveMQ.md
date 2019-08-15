# ActiveMQ #

问题产生背景-系统之间直接调用实际工程落地和存在的问题

- 系统之间接口耦合比较严重
- 面对大流量并发时，容易被冲垮
- 等待同步存在性能问题

作用:解耦 削锋 异步

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
	}


**com.my.springboot.springbootproducer.SpringbootProducerApplicationTests**

	 @Test
    public void testProduceSendMsg(){
        queueProduce.produceMessage();
    }