**Mybatis Plus**

applicationContext.xml

	<?xml version="1.0" encoding="UTF-8"?>
	<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:mybatis-spring="http://mybatis.org/schema/mybatis-spring"
       xsi:schemaLocation="http://mybatis.org/schema/mybatis-spring http://mybatis.org/schema/mybatis-spring-1.2.xsd
		http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd
		http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.0.xsd">


    <!-- 数据源 -->
    <context:property-placeholder location="classpath:db.properties"/>
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
        <property name="driverClass" value="${jdbc.driverClassName}"></property>
        <property name="jdbcUrl" value="${jdbc.url}"></property>
        <property name="user" value="${jdbc.username}"></property>
        <property name="password" value="${jdbc.password}"></property>
    </bean>

    <!-- 事务管理器 -->
    <bean id="dataSourceTransactionManager"
          class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"></property>
    </bean>
    <!-- 基于注解的事务管理 -->
    <tx:annotation-driven transaction-manager="dataSourceTransactionManager"/>


    <!--  配置SqlSessionFactoryBean
        Mybatis提供的: org.mybatis.spring.SqlSessionFactoryBean
        MP提供的:com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean
     -->
    <bean id="sqlSessionFactoryBean" class="com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean">
        <!-- 数据源 -->
        <property name="dataSource" ref="dataSource"></property>
        <property name="configLocation" value="classpath:mybatis-config.xml"></property>
        <!-- 别名处理 -->
        <property name="typeAliasesPackage" value="com.mp.bean"></property>

        <!-- 注入全局MP策略配置 -->
        <property name="globalConfig" ref="globalConfiguration"></property>
        <property name="plugins">

            <list>
                <!-- 注册分页插件 -->
                <bean class="com.baomidou.mybatisplus.plugins.PaginationInterceptor"/>

                <!-- 注册执行分析插件 -->
                <bean class="com.baomidou.mybatisplus.plugins.SqlExplainInterceptor">
                    <!--<property name="stopProceed" value="true"/>-->
                </bean>
                <!-- 注册性能分析插件 -->
                <bean class="com.baomidou.mybatisplus.plugins.PerformanceInterceptor">
                    <property name="format" value="true"/>

                </bean>

                <!-- 注册乐观锁插件 -->
                <bean class="com.baomidou.mybatisplus.plugins.OptimisticLockerInterceptor"/>
            </list>



        </property>
    </bean>

    <!-- 定义MybatisPlus的全局策略配置-->
    <bean id ="globalConfiguration" class="com.baomidou.mybatisplus.entity.GlobalConfiguration">
        <!-- 在2.3版本以后，dbColumnUnderline 默认值就是true 表字断驼峰命名，自动处理下划线-->
        <property name="dbColumnUnderline" value="true"></property>

        <!-- 全局的主键策略 -->
        <property name="idType" value="0"></property>

        <!-- 全局的表前缀策略配置 -->
        <property name="tablePrefix" value="tbl_"></property>

        <!--注入自定义全局操作 -->
        <!--<property name="sqlInjector" ref="myInjector"/>-->

        <!-- 注入逻辑删除 -->
        <property name="sqlInjector" ref="logicSqlInjector"/>
        <property name="logicDeleteValue" value="0"/>
        <property name="logicNotDeleteValue" value="1"/>

        <!-- 注入公共字段填充处理器 -->
        <property name="metaObjectHandler" ref="myMetaObjectHandler" />
    </bean>

    <!--
        配置mybatis 扫描mapper接口的路径
     -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.mp.mapper"></property>
    </bean>

    <!-- 定义自定义注入器 -->
    <bean id="myInjector" class="com.mp.injector.MyInjector"/>

    <!-- 逻辑删除 -->
    <bean id="logicSqlInjector" class="com.baomidou.mybatisplus.mapper.LogicSqlInjector"/>


    <!-- 公共字段填充 处理器 -->
    <bean id="myMetaObjectHandler" class="com.mp.metaObjectHandler.MyMetaObjectHandler"/>
	</beans>

mybatis-config.xml

	<?xml version="1.0" encoding="UTF-8" ?>
	<!DOCTYPE configuration
	        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
	        "http://mybatis.org/dtd/mybatis-3-config.dtd">
	<configuration>
	    <!--Log4j无法打印SQL语句,必须设置用LOG4J打印SQL才有效-->
	    <settings>
	        <setting name="logImpl" value="LOG4J" />
	    </settings>
	    <!--<plugins>-->
	        <!--<plugin interceptor="com.baomidou.mybatisplus.plugins.PaginationInterceptor"></plugin>-->
	    <!--</plugins>-->
	</configuration>


**通用CRUD**

1)  提出问题:
假设我们已存在一张employee表，且已有对应的实体类Employee，实现employee表的CRUD操作我们需要做什么呢？ 

2)  实现方式: 

**基于Mybatis**
    
需要编写EmployeeMapper 接口，并手动编写CRUD 方法 
提供EmployeeMapper.xml 映射文件，并手动编写每个方法对应的SQL 语句. 

基于MP 

只需要创建EmployeeMapper 接口,   并继承BaseMapper 接口.这就是使用MP需要完成的所有操作，甚至不需要创建SQL 映射文件

**插入操作** 

2)  @TableName :Bean类上指定表明属性//@TableName(value="tbl_employee")

3)  全局的MP 配置: 指定表前缀

	<property name="tablePrefix" value="tbl_"></property> 

4)  @TableField ：指定字段属性

5)  全局的MP 配置: 表字断驼峰命名，自动处理下划线

	<property name="dbColumnUnderline" value="true"></property> 

6)  @TableId ：value: 指定表中的主键列的列名，如果实体属性名与列名一致，可以省略不指定.type: 指定主键策略.

7)  全局的MP 配置:

	//数据库ID自增
	 <property name="idType" value="0"></property> 

8)  支持主键自增的数据库插入数据获取主键值 

Mybatis:  需要通过  useGeneratedKeys 以及keyProperty  来设置 

MP:   自动将主键值回写到实体类中 


	Integer insert(T entity) // insert方法在插入时， 会根据实体类的每个属性进行非空判断，只有非空的属性对应的字段才会出现到SQL语句中
	
	Integer insertAllColumn(T entity)   //insertAllColumn方法在插入时， 不管属性是否非空， 属性所对应的字段都会出现到SQL语句中.

**更新操作**

	Integer updateById(@Param("et") T entity); 
	Integer updateAllColumnById(@Param("et") T entity) 

**查询操作**

	T selectById(Serializable id); 
	T selectOne(@Param("ew") T entity); //selectOne只可以取数据唯一一条，获取到多条会报异常
	List<T> selectBatchIds(List<? extends Serializable> idList); //通过多个id进行查询<foreach>
	List<T> selectByMap(@Param("cm") Map<String, Object> columnMap); //通过Map封装条件查询
	List<T> selectPage(RowBounds rowBounds, @Param("ew") Wrapper<T> wrapper); //分页查询

**删除操作**

	Integer deleteById(Serializable id); //根据id进行删除
	Integer deleteByMap(@Param("cm") Map<String, Object> columnMap); 	//根据 条件进行删除
	Integer deleteBatchIds(List<? extends Serializable> idList); // 批量删除


详细操作可参考:https://mybatis.plus/guide/crud-interface.html#mapper-crud-%E6%8E%A5%E5%8F%A3

# 条件构造器  EntityWrapper  #

可通过以下两个实例创建条件语句

new EntityWrapper<T>和Condition.create()

详细条件说明:https://baomidou.gitee.io/mybatis-plus-doc/#/wrapper

	
	/**
	 * <p>
	 * 根据 whereEntity 条件，更新记录
	 * </p>
	 *
	 * @param entity        实体对象 (set 条件值,可为 null)
	 * @param updateWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
	 * @return 修改成功记录数
	 */
	int update(@Param(Constants.ENTITY) T entity, @Param(Constants.WRAPPER) Wrapper<T> updateWrapper);
	
	/**
	 * <p>
	 * 根据 entity 条件，删除记录
	 * </p>
	 *
	 * @param wrapper 实体对象封装操作类（可以为 null）
	 * @return 删除成功记录数
	 */
	int delete(@Param(Constants.WRAPPER) Wrapper<T> wrapper);

	/**
	 * <p>
	 * 根据 entity 条件，查询全部记录
	 * </p>
	 *
	 * @param queryWrapper 实体对象封装操作类（可以为 null）
	 * @return 实体集合
	 */
	List<T> selectList(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

		//使用案例
	   List page = employeeMapper.selectPage(new Page<Employee>(2, 2),
                  new EntityWrapper()
                  .between("age", 20, 50)
                  .eq("last_name","Tome")
                 .eq("gender",1)
          );

		// 查询tbl_employee表中， 性别为女并且名字中带有"老师" 或者  邮箱中带有"a"
         List<Employee> employees = employeeMapper.selectList(
	                 new EntityWrapper<Employee>()
	                          .eq("gender", 1)
	                          .like("last_name", "老师")
	                         //.or() //(gender = ? AND last_name LIKE ? OR email LIKE ?)
	                         .orNew()  //(gender = ? AND last_name LIKE ?) OR (email LIKE ?)
	                          .like("email", "a")
	         );

		//使用Condition 的方式
		  List list = employeeMapper.selectList(
                  Condition.create()
                          .eq("last_name", "B")
          );

小结：

**MP: EntityWrapper    Condition    条件构造器**

**MyBatis MBG :  xxxExample→Criteria : QBC( Query    By    Criteria)**


# ActiveRecord(活动记录)  #

如何使用AR模式

仅仅需要让实体类继承Model 类且实现主键指定方法，即可开启AR之旅. 

    @TableName("tbl_student") 
    public  class  Student  extends   Model<Student>{ 
        // .. fields 
        // .. getter and setter 

     /**
     ** 指定当前实体类的主键属性
     **/
    @Override
    protected Serializable pkVal() {
        return id;
    }

基本CRUD

	1)  插入操作 
    public boolean insert() 
	2)  修改操作 
    public boolean updateById() 
	3)  查询操作 
    public T selectById() 
    public T selectById(Serializable id) 
    public List<T> selectAll() 
    public List<T> selectList(Wrapper wrapper) 
    public int selectCount(Wrapper wrapper)
	4)  删除操作 
    public boolean deleteById() 
    public boolean deleteById(Serializable id)  
    public boolean delete(Wrapper wrapper) 
	5)  分页复杂操作 
    public Page<T> selectPage(Page<T> page, Wrapper<T> wrapper) 

# 代码生成器 #

MP 的代码生成器默认使用的是Apache  的Velocity 模板，当然也可以更换为别的模板技术，例如freemarker

引入依赖

	   <dependency>
            <groupId>org.apache.velocity</groupId>
            <artifactId>velocity-engine-core</artifactId>
            <version>2.0</version>
        </dependency>
加入slf4j ,查看日志输出信息 

		 <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.7</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.7</version>
        </dependency>

代码生成器示例
	
	        //1. 全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setIdType(IdType.AUTO)// 主键策略
                    .setAuthor("Lee")// 作者
                    .setBaseColumnList(true)
                    .setActiveRecord(true)// 是否支持AR模式
                    .setOutputDir("D:\\Code\\MyBatisPlus\\src\\main\\java")// 生成路径
                    .setServiceName("%sService")// 设置生成的service接口的名字的首字母是否为I   // IEmployeeService
                    .setFileOverride(true)// 文件覆盖
                    .setBaseResultMap(true);


        //2. 数据源配置
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.MYSQL)// 设置数据库类型
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setUrl("jdbc:mysql://127.0.0.1:3306/mp?serverTimezone=GMT&useSSL=false&characterEncoding=utf-8")
                        .setUsername("root")
                        .setPassword("123456");

        //3. 策略配置
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setCapitalMode(true)//全局大写命名
                        .setDbColumnUnderline(true)// 指定表名 字段名是否使用下划线
                        .setTablePrefix("tbl")
                        .setNaming(NamingStrategy.underline_to_camel)// 数据库表映射到实体的命名策略
                        .setInclude("tbl_teacher");// 生成的表

        //4. 包名策略配置
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setController("controller")
                        .setParent("com.mp")
                        .setMapper("mapper")
                        .setEntity("bean")
                        .setController("controller")
                        .setXml("mapper")
                        .setService("service");

        //5. 整合配置
        AutoGenerator autoGenerator = new AutoGenerator();
        autoGenerator.setGlobalConfig(globalConfig)
                     .setDataSource(dataSourceConfig)
                     .setPackageInfo(packageConfig)
                     .setStrategy(strategyConfig);
        //6. 执行
        autoGenerator.execute();

官网详细讲解:https://baomidou.gitee.io/mybatis-plus-doc/#/generate-code



# 插件扩展 #

1)  插件机制:

 Mybatis通过插件(Interceptor)可以做到拦截四大对象相关方法的执行,根据需求，完成相关数据的动态改变。

    Executor 
    StatementHandler 
    ParameterHandler 
    ResultSetHandler 
2)  插件原理 

四大对象的每个对象在创建时，都会执行interceptorChain.pluginAll()，会经过每个插件对象的plugin()方法，目的是为当前的四大对象创建代理。代理对象就可以拦截到四大对象相关方法的执行，因为要执行四大对象的方法需要经过代理. 

**注意:插件的使用要在applicationContext.XML配置**

**分页插件**

	com.baomidou.mybatisplus.plugins.PaginationInterceptor 
	//使用示例：
	   Page<Employee> page = new Page<Employee>(0, 1);

        List list = employeeMapper.selectPage(page, null);
        System.out.println(list);

        System.out.println("=======================");
        System.out.println("总条数:" +page.getTotal());
        System.out.println("当前页码: "+  page.getCurrent());
        System.out.println("总页码:" + page.getPages());
        System.out.println("每页显示的条数:" + page.getSize());
        System.out.println("是否有上一页: " + page.hasPrevious());
        System.out.println("是否有下一页: " + page.hasNext());

        //将查询的结果封装到page对象中
        page.setRecords(list);

**执行分析插件**

1)  com.baomidou.mybatisplus.plugins.SqlExplainInterceptor

2)  SQL 执行分析拦截器，只支持MySQL5.6.3  以上版本 

3)  该插件的作用是分析  DELETE    UPDATE 语句,防止小白或者恶意进行DELETE    UPDATE 全表操作 

4)  只建议在开发环境中使用，不建议在生产环境使用

5)  在插件的底层  通过SQL 语句分析命令:Explain  分析当前的SQL 语句，根据结果集中的Extra 列来断定当前是否全表操作

**性能分析插件**

1) com.baomidou.mybatisplus.plugins.PerformanceInterceptor 

2)  性能分析拦截器，用于输出每条  SQL  语句及其执行时间 

3)  SQL 性能执行分析,开发环境使用，超过指定时间，停止运行。有助于发现问题

**乐观锁插件**

1)  com.baomidou.mybatisplus.plugins.OptimisticLockerInterceptor 

2)  如果想实现如下需求:   当要更新一条记录的时候，希望这条记录没有被别人更新 

3)  乐观锁的实现原理: 

取出记录时，获取当前version      2  

更新时，带上这个version    2  

执行更新时，  set version = yourVersion+1 where version = yourVersion 

如果version 不对，就更新失败 

4)  @Version  用于注解实体字段，必须要有。 (Employee.version)

# 自定义全局操作 #

根据MybatisPlus  的AutoSqlInjector 可以自定义各种你想要的sql  ,注入到全局中，相当于自 
定义Mybatisplus   自动注入的方法。  

之前需要在xml  中进行配置的SQL 语句，现在通过扩展AutoSqlInjector  在加载mybatis 环境 
时就注入。 

**AutoSqlInjector**

1)  在Mapper 接口中定义相关的CRUD 方法 

2)  扩展AutoSqlInjector      inject  方法，实现Mapper 接口中方法要注入的SQL
 
3)  在MP 全局策略中，配置           自定义注入器 

	public class MyInjector extends AutoSqlInjector {
    @Override
    public void inject(Configuration configuration, MapperBuilderAssistant builderAssistant, Class<?> mapperClass, Class<?> modelClass, TableInfo table) {
       // super.inject(configuration, builderAssistant, mapperClass, modelClass, table);

        teacherDeleteAll(configuration, builderAssistant, mapperClass, modelClass, table);

    }

    private void teacherDeleteAll(Configuration configuration, MapperBuilderAssistant builderAssistant, Class<?> mapperClass, Class<?> modelClass, TableInfo table){

        String sql ="delete from "+table.getTableName();
        String method = "deleteAll";
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

        //this.addMappedStatement(mapperClass, method, sqlSource, SqlCommandType.DELETE, Integer.class);
        this.addDeleteMappedStatement(mapperClass,method,sqlSource);
    }
	}

**自定义注入器的应用之  逻辑删除**

假删除、逻辑删除:  并不会真正的从数据库中将数据删除掉，而是将当前被删除的这条数据中的一个逻辑删除字段置为删除状态. 

user       logic_flag = 1       →    -1  

1)  com.baomidou.mybatisplus.mapper.LogicSqlInjector 

2)  logicDeleteValue      逻辑删除全局值 

3)  logicNotDeleteValue  逻辑未删除全局值 


4)  在POJO 的逻辑删除字段  添加   @TableLogic 注解 

5)  会在mp  自带查询和更新方法的sql 后面，追加『逻辑删除字段』= 『LogicNotDeleteValue默认值』  删除方法: deleteById()和其他delete 方法,  底层SQL 调用的是update tbl_xxx set  『逻辑删除字段』=       『logicDeleteValue 默认值』 

# 公共字段自动填充 #

元数据处理器接口
	
	com.baomidou.mybatisplus.mapper.MetaObjectHandler 
	
	insertFill(MetaObject metaObject)   
	   
	updateFill(MetaObject metaObject) 

metaobject: 元对象.是Mybatis提供的一个用于更加方便，更加优雅的访问对象的属性, 给对象的属性设置值的一个对象.还会用于包装对象.支持对 Object、Map、Collection 等对象进行包装 

本质上metaObject 获取对象的属性值或者是给对象的属性设置值，最终是要通过Reflector  获取到属性的对应方法的Invoker,  最终invoke. 

开发步骤 

1)  注解填充字段 @TableFile(fill = FieldFill.INSERT)查看FieldFill
 
2)  自定义公共字段填充处理器
	
	public class MyMetaObjectHandler extends MetaObjectHandler {
	    /**
	     * 插入操作 自动填充
	     */
	    @Override
	    public void insertFill(MetaObject metaObject) {
	        Object fieldValByName = getFieldValByName("last_name", metaObject);
	        if (fieldValByName == null){
	            System.out.println("=========meta object handler insert fill===========");
	            setFieldValByName("last_name","test1",metaObject);
	        }
	
	    }
	
	    /**
	     * 修改操作 自动填充
	     */
	    @Override
	    public void updateFill(MetaObject metaObject) {
	
	        Object fieldValByName = getFieldValByName("last_name", metaObject);
	        if (fieldValByName == null){
	            System.out.println("=========meta object handler update fill===========");
	            setFieldValByName("last_name","test2",metaObject);
	        }
	    }
	}


3)  MP 全局注入自定义公共字段填充处理器 