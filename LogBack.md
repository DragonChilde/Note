# logback的介绍

logback是Java的开源框架，性能比log4j要好。是springboot自带的日志框架。该框架主要有3个模块：

- logback-core：核心代码块
- log back-classic：实现了slf4j的api，加入该依赖可以实现log4j的api。
- log back-access：访问模块与servlet容器集成提供通过http来访问日志的功能（也就是说不需要访问服务器，直接在网页上就可以访问日志文件）。

```java
<!-- springboot 的springboot-core已经依赖了logback-core和logback-classic -->
<dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.25</version>
</dependency>
<dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>log4j-over-slf4j</artifactId>
            <version>1.7.25</version>
</dependency>
```

**日志级别**
**从低到高分为TRACE < DEBUG < INFO < WARN < ERROR < FATAL，如果设置为WARN，则低于WARN的信息都不会输出。**

**自定义日志配置**
**由于日志服务一般都在ApplicationContext创建前就初始化了，它并不是必须通过Spring的配置文件控制。因此通过系统属性和传统的Spring Boot外部配置文件依然可以很好的支持日志控制和管理。**
**根据不同的日志系统，你可以按如下规则组织配置文件名，就能被正确加载：**

| 日志框架                | 配置文件名                                                   |
| ----------------------- | ------------------------------------------------------------ |
| Logback                 | logback-spring.xml, logback-spring.groovy, logback.xml, logback.groovy |
| Log4j                   | log4j-spring.properties, log4j-spring.xml, log4j.properties, log4j.xml |
| Log4j2                  | log4j2-spring.xml, log4j2.xml                                |
| JDK (Java Util Logging) | logging.properties                                           |

# logback的默认配置

 Spring Boot官方推荐优先使用带有-spring的文件名作为你的日志配置（如使用logback-spring.xml，而不是logback.xml），命名为logback-spring.xml的日志配置文件，spring boot可以为它添加一些spring boot特有的配置项（下面会提到）。
上面是默认的命名规则，并且放在src/main/resources下面即可。如果你即想完全掌控日志配置，但又不想用logback.xml作为Logback配置的名字，可以在application.properties配置文件里面通过logging.config属性指定自定义的名字：`logging.config=classpath:logback.xml`

```java
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="60 seconds" debug="false">
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <property name="LOG_FILE_NAME_PATTERN" value="logs/auth.%d{yyyy-MM-dd}.%i.log"/>
    <!-- 日志格式 -->
    <property name="CONSOLE_LOG_PATTERN"
              value="%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%c){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"/>
    <property name="FILE_LOG_PATTERN"
              value="%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}} ${LOG_LEVEL_PATTERN:-%5p} ${PID:- } --- [%t] %c : %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"/>

    <!--输出到控制台-->
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <!--输出到文件-->
    <appender name="file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE_NAME_PATTERN}</fileNamePattern>
            <!-- 日志保留天数 -->
            <maxHistory>366</maxHistory>
            <!-- 日志文件上限大小，达到指定大小后删除旧的日志文件 -->
            <totalSizeCap>2GB</totalSizeCap>

            <!-- 每个日志文件的最大值 -->
            <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>

        </rollingPolicy>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <!-- (多环境配置日志级别)根据不同的环境设置不同的日志输出级别 -->
    <springProfile name="default,local">
        <root level="info">
            <appender-ref ref="console"/>
        </root>
        <logger name="com.zhl" level="debug"/>
    </springProfile>

    <springProfile name="dev,test">
        <root level="info">
            <appender-ref ref="console"/>
            <appender-ref ref="file"/>
        </root>
        <logger name="com.zhl" level="debug"/>
    </springProfile>

    <springProfile name="product,pre">
        <root level="info">
            <appender-ref ref="console"/>
            <appender-ref ref="file"/>
        </root>
        <logger name="com.zhl" level="debug"/>
    </springProfile>
    
</configuration>
```

**不同日志信息输出到不同文件：** 

```java
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="60 seconds" debug="false">
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <!-- 应用名称-->
    <property name="appName" value="zhlrm-ppt-service"/>
    <!-- 日志的存放目录-->
    <!-- debug-->
    <property name="DEBUG_LOG_FILE_NAME_PATTERN" value="logs/${appName}-debug.%d{yyyy-MM-dd}.%i.log"/>
    <property name="INFO_LOG_FILE_NAME_PATTERN" value="logs/${appName}-info.%d{yyyy-MM-dd}.%i.log"/>
    <property name="WARN_LOG_FILE_NAME_PATTERN" value="errlogs/${appName}-warn.%d{yyyy-MM-dd}.%i.log"/>
    <property name="ERROR_LOG_FILE_NAME_PATTERN" value="errlogs/${appName}-error.%d{yyyy-MM-dd}.%i.log"/>
    <!-- 日志格式 -->
    <property name="CONSOLE_LOG_PATTERN"
              value="%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%c){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"/>
    <property name="FILE_LOG_PATTERN"
              value="%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}} ${LOG_LEVEL_PATTERN:-%5p} ${PID:- } --- [%t] %c : %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"/>
    <!--输出到控制台-->
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <!--输出到DEBUG文件-->
    <appender name="debug_file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${DEBUG_LOG_FILE_NAME_PATTERN}</fileNamePattern>
            <!-- 日志保留天数 -->
            <maxHistory>30</maxHistory>
            <!-- 日志文件上限大小，达到指定大小后删除旧的日志文件 -->
            <totalSizeCap>2GB</totalSizeCap>
            <!-- 每个日志文件的最大值 -->
            <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>50MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
        </encoder>
        <!-- 此日志文件只记录debug级别的 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>debug</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!--输出到INFO文件-->
    <appender name="info_file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${INFO_LOG_FILE_NAME_PATTERN}</fileNamePattern>
            <!-- 日志保留天数 -->
            <maxHistory>7</maxHistory>
            <!-- 日志文件上限大小，达到指定大小后删除旧的日志文件 -->
            <totalSizeCap>1GB</totalSizeCap>
            <!-- 每个日志文件的最大值 -->
            <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>50MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
        </encoder>
        <!-- 此日志文件只记录info级别的 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>info</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!--输出到WARN文件-->
    <appender name="warn_file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${WARN_LOG_FILE_NAME_PATTERN}</fileNamePattern>
            <!-- 日志保留天数 -->
            <maxHistory>30</maxHistory>
            <!-- 日志文件上限大小，达到指定大小后删除旧的日志文件 -->
            <totalSizeCap>1GB</totalSizeCap>
            <!-- 每个日志文件的最大值 -->
            <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
        </encoder>
        <!-- 此日志文件只记录warn级别的 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>warn</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!--输出到ERROR文件-->
    <appender name="error_file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${ERROR_LOG_FILE_NAME_PATTERN}</fileNamePattern>
            <!-- 日志保留天数 -->
            <maxHistory>30</maxHistory>
            <!-- 日志文件上限大小，达到指定大小后删除旧的日志文件 -->
            <totalSizeCap>1GB</totalSizeCap>
            <!-- 每个日志文件的最大值 -->
            <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
        </encoder>
        <!-- 此日志文件只记录error级别的 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>error</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>
    <!-- region 根据不同的环境设置不同的日志输出级别 -->
    <springProfile name="default,local,dev">
        <root level="info">
            <appender-ref ref="console"/>
        </root>
        <logger name="com.zhl.rm" level="debug"/>
    </springProfile>

    <springProfile name="prod,pre,test">
        <root level="info">
            <appender-ref ref="console"/>
            <appender-ref ref="debug_file"/>
            <appender-ref ref="info_file"/>
            <appender-ref ref="warn_file"/>
            <appender-ref ref="error_file"/>
        </root>
        <logger name="com.zhl.rm" level="debug"/>
    </springProfile>
    <!-- endregion -->

</configuration>
```

# logback.xml格式详解

- 根节点`<configuration>`包含的属性

  - scan：当此属性设置为true时，配置文件如果发生改变，将会被重新加载，默认值为true。
  - scanPeriod：设置监测配置文件是否有修改的时间间隔，如果没有给出时间单位，默认单位是毫秒。当scan为true时，此属性生效。默认的时间间隔为1分钟。
  - debug：当此属性设置为true时，将打印出logback内部日志信息，实时查看logback运行状态。默认值为false。

  ```java
  <configuration scan="true" scanPeriod="60 seconds" debug="false"> 
  　　  <!--其他配置省略--> 
  </configuration>　
  ```

- 根节点`<configuration>`的子节点：`<configuration>`下面一共有2个属性，3个子节点，分别是：

  - 设置上下文名称`<contextName>`

    每个logger都关联到logger上下文，默认上下文名称为“default”。但可以使用设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改,可以通过%contextName来打印日志上下文名称。

    ```java
    <configuration scan="true" scanPeriod="60 seconds" debug="false"> 
        <contextName>logback</contextName>
    　　  <!--其他配置省略-->
    </configuration>  
    ```

  - 设置变量`<property>` 用来定义变量值的标签，有两个属性，name和value；其中name的值是变量的名称，value的值时变量定义的值。通过定义的值会被插入到logger上下文中。定义变量后，可以使“${}”来使用变量。

    ```java
    <configuration scan="true" scanPeriod="60 seconds" debug="false"> 
    　　　<property name="log.path" value="log" />
    　　　<contextName>${APP_Name}</contextName> 
    　　　<!--其他配置省略--> 
    </configuration>
    ```

  -  子节点<timestamp>：获取时间戳字符串，他有两个属性key和datePattern

    - key: 标识此<timestamp> 的名字；
    - datePattern: 设置将当前时间（解析配置文件的时间）转换为字符串的模式，遵循java.txt.SimpleDateFormat的格式。

    ```java
    <configuration scan="true" scanPeriod="60 seconds" debug="false"> 
    　　<timestamp key="bySecond" datePattern="yyyyMMdd'T'HHmmss"/> 
    　　<contextName>${bySecond}</contextName> 
    　　<!-- 其他配置省略--> 
    </configuration>
    ```

  - 子节点`<appender>`：负责写日志的组件，它有两个必要属性name和class。name指定appender名称，class指定appender的全限定名

    - ConsoleAppender 把日志输出到控制台，有以下子节点:

      - <encoder>：对日志进行格式化
      - <target>：字符串System.out(默认)或者System.err（区别不多说了）

      ```java
      <configuration> 
      　　　<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender"> 
      　　　　　 <encoder> 
      　　　　　　　　　<pattern>%-4relative [%thread] %-5level %logger{35} - %msg %n</pattern> 
      　　　　　 </encoder> 
      　　　</appender> 
      
      　　　<root level="DEBUG"> 
      　　　　　　<appender-ref ref="STDOUT" /> 
      　　　</root> 
      </configuration>
      ```

      > 上述配置表示把>=DEBUG级别的日志都输出到控制台

    - **FileAppender**：把日志添加到文件，有以下子节点：

      - <file>：被写入的文件名，可以是相对目录，也可以是绝对目录，如果上级目录不存在会自动创建，没有默认值。
      - <append>：如果是 true，日志被追加到文件结尾，如果是 false，清空现存文件，默认是true。
      - <encoder>：对记录事件进行格式化。（具体参数稍后讲解 ）
      - <prudent>：如果是 true，日志会被安全的写入文件，即使其他的FileAppender也在向此文件做写入操作，效率低，默认是 false。

      ```java
      <configuration> 
      　　<appender name="FILE" class="ch.qos.logback.core.FileAppender"> 
      　　　　<file>testFile.log</file> 
      　　　　<append>true</append> 
      　　　　<encoder> 
      　　　　　　<pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</pattern> 
      　　　　</encoder> 
      　　</appender> 
      
      　　<root level="DEBUG"> 
      　　　　<appender-ref ref="FILE" /> 
      　　</root> 
      </configuration>
      ```

      > 上述配置表示把>=DEBUG级别的日志都输出到testFile.log

    - **RollingFileAppender**：滚动记录文件，先将日志记录到指定文件，当符合某个条件时，将日志记录到其他文件。有以下子节点：

      - <file>：被写入的文件名，可以是相对目录，也可以是绝对目录，如果上级目录不存在会自动创建，没有默认值。

      - <append>：如果是 true，日志被追加到文件结尾，如果是 false，清空现存文件，默认是true。

      - <rollingPolicy>:当发生滚动时，决定RollingFileAppender的行为，涉及文件移动和重命名。属性class定义具体的滚动策略类

        - class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy，是最受欢迎的滚动政策，例如按天或按月。 TimeBasedRollingPolicy承担翻滚责任以及触发所述翻转的责任。TimeBasedRollingPolicy支持自动文件压缩。
          - <fileNamePattern>：必要节点，包含文件名及“%d”转换符，“%d”可以包含一个java.text.SimpleDateFormat指定的时间格式，如：%d{yyyy-MM}。
            如果直接使用 %d，默认格式是 yyyy-MM-dd。RollingFileAppender的file字节点可有可无，通过设置file，可以为活动文件和归档文件指定不同位置，当前日志总是记录到file指定的文件（活动文件），活动文件的名字不会改变；
            如果没设置file，活动文件的名字会根据fileNamePattern 的值，每隔一段时间改变一次。“/”或者“\”会被当做目录分隔符。
          - <maxHistory>:
            可选节点，控制保留的归档文件的最大数量，超出数量就删除旧文件。假设设置每个月滚动，且<maxHistory>是6，则只保存最近6个月的文件，删除之前的旧文件。注意，删除旧文件是，那些为了归档而创建的目录也会被删除。
        - class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy"： 查看当前活动文件的大小，如果超过指定大小会告知RollingFileAppender 触发当前活动文件滚动。只有一个节点:
          - <maxFileSize>:这是活动文件的大小，默认值是10MB。
          - <prudent>：当为true时，不支持FixedWindowRollingPolicy。支持TimeBasedRollingPolicy，但是有两个限制，1不支持也不允许文件压缩，2不能设置file属性，必须留空。
        - 　class="ch.qos.logback.core.rolling.FixedWindowRollingPolicy" 根据固定窗口算法重命名文件的滚动策略。有以下子节点：
          - <minIndex>:窗口索引最小值
          - <maxIndex>:窗口索引最大值，当用户指定的窗口过大时，会自动将窗口设置为12。
          - <fileNamePattern>:必须包含“%i”例如，假设最小值和最大值分别为1和2，命名模式为 mylog%i.log,会产生归档文件mylog1.log和mylog2.log。还可以指定文件压缩选项，例如，mylog%i.log.gz 或者 没有log%i.log.zip
        - <encoder>：对记录事件进行格式化。负责两件事，一是把日志信息转换成字节数组，二是把字节数组写入到输出流。
        - PatternLayoutEncoder 是唯一有用的且默认的encoder ，有一个<pattern>节点，用来设置日志的输入格式。使用“%”加“转换符”方式，如果要输出“%”，则必须用“\”对“\%”进行转义。

        ```java
        <configuration> 
        　　　<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender"> 
        　　　　　　<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy"> 
        　　　　　　　　　<fileNamePattern>logFile.%d{yyyy-MM-dd}.log</fileNamePattern> 
        　　　　　　　　　<maxHistory>30</maxHistory> 
        　　　　　　</rollingPolicy> 
        　　　　　　<encoder> 
        　　　　　　　　　<pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</pattern> 
        　　　　　　</encoder> 
        　　　</appender> 
        
        　　　<root level="DEBUG"> 
        　　　　　　<appender-ref ref="FILE" /> 
        　　　</root> 
        </configuration>
        ```

        > 备注：上述配置表示每天生成一个日志文件，保存30天的日志文件。

        ```java
        <configuration> 
        　　　<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender"> 
        　　　　　　<file>test.log</file> 
        
        　　　　　　<rollingPolicy class="ch.qos.logback.core.rolling.FixedWindowRollingPolicy"> 
        　　　　　　　　　　<fileNamePattern>tests.%i.log.zip</fileNamePattern> 
        　　　　　　　　　　<minIndex>1</minIndex> 
        　　　　　　　　　　<maxIndex>3</maxIndex> 
        　　　　　　</rollingPolicy> 
        
        　　　　　　<triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy"> 
        　　　　　　　　　　<maxFileSize>5MB</maxFileSize> 
        　　　　　　</triggeringPolicy> 
        　　　　　　<encoder> 
        　　　　　　　　　<pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</pattern> 
        　　　　　　</encoder> 
        　　　</appender> 
        
        　　　<root level="DEBUG"> 
        　　　　　　<appender-ref ref="FILE" /> 
        　　　</root> 
        </configuration>
        ```

        > 备注：上述配置表示按照固定窗口模式生成日志文件，当文件大于5MB时，生成新的日志文件。窗口大小是1到3，当保存了3个归档文件后，将覆盖最早的日志。

      - 还有SocketAppender、SMTPAppender、DBAppender、SyslogAppender、SiftingAppender，并不常用，这里就不详解了。大家可以参考官方文档（http://logback.qos.ch/documentation.html），还可以编写自己的Appender。

    - 子节点<logger>：用来设置某一个包或具体的某一个类的日志打印级别、以及指定<appender>

      - <logger>仅有一个name属性，一个可选的level和一个可选的additivity属性。
      - 可以包含零个或多个<appender-ref>元素，标识这个appender将会添加到这个logger
      - 属性name: 用来指定受此logger约束的某一个包或者具体的某一个类。
      - 属性level: 用来设置打印级别，大小写无关：TRACE, DEBUG, INFO, WARN, ERROR, ALL和OFF，还有一个特俗值INHERITED或者同义词NULL，代表强制执行上级的级别。 如果未设置此属性，那么当前logger将会继承上级的级别。
      - 属性additivity: 是否向上级logger传递打印信息。默认是true。同<logger>一样，可以包含零个或多个<appender-ref>元素，标识这个appender将会添加到这个logger。

      ```java
      <!-- show parameters for hibernate sql 专为 Hibernate 定制 -->
      <logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE" />
      <logger name="org.hibernate.type.descriptor.sql.BasicExtractor" level="DEBUG" />
      <logger name="org.hibernate.SQL" level="DEBUG" />
      <logger name="org.hibernate.engine.QueryParameters" level="DEBUG" />
      <logger name="org.hibernate.engine.query.HQLQueryPlan" level="DEBUG" />
      
      <!--myibatis log configure-->
      <logger name="com.apache.ibatis" level="TRACE"/>
      <logger name="java.sql.Connection" level="DEBUG"/>
      <logger name="java.sql.Statement" level="DEBUG"/>
      <logger name="java.sql.PreparedStatement" level="DEBUG"/>
      ```

    - 子节点<root>:它也是<logger>元素，但是它是根logger,是所有<logger>的上级。只有一个level属性，因为name已经被命名为"root",且已经是最上级了。

      - level: 用来设置打印级别，大小写无关：TRACE, DEBUG, INFO, WARN, ERROR, ALL和OFF，不能设置为INHERITED或者同义词NULL。 默认是DEBUG。

# 总结

logback的配置，需要配置输出源appender，打日志的logger（子节点）和root（根节点）。实际上，它输出日志是从子节点开始，子节点如果有输出源直接输出，如果无，判断配置的additivity，是否向上级传递，即是否向root传递，传递则采用root的输出源，否则不输出日志。

