文档参考:http://shiro.apache.org/reference.html

**整合SPRING**

**与Web集成**

• Shiro提供了与 Web 集成的支持，其通过一个 
   ShiroFilter 入口来拦截需要安全控制的URL ，然后 
   进行相应的控制

• ShiroFilter 类似于如 Strut2/SpringMVC 这种 
   web 框架的前端控制器 ，是安全控制的入口点 ，其 
   负责读取配置（如ini 配置文件），然后判断URL 
   是否需要登录/权限等工作。

WEB.xml

	1. 配置  Shiro 的 shiroFilter.
	2. DelegatingFilterProxy 实际上是 Filter 的一个代理对象. 默认情况下, Spring 会到 IOC 容器中查找和<filter-name> 对应的 filter bean. 也可以通过 targetBeanName 的初始化参数来配置 filter bean 的 id.
	3. DelegatingFilterProxy 作用是自动到 Spring 容器查找名字为 shiroFilter （filter-name ）的 bean 并把所有 Filter的操作委托给它。
	<filter>
		<filter-name>shiroFilter</filter-name>
		<filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
		<init-param>
			<param-name>targetFilterLifecycle</param-name>
			<param-value>true</param-value>
		</init-param>
	</filter>

	<filter-mapping>
		<filter-name>shiroFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

ApplicationContext.xml

	<?xml version="1.0" encoding="UTF-8"?>
	<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <!-- 1. 配置 SecurityManager!-->
    <bean id="securityManager" class="org.apache.shiro.web.mgt.DefaultWebSecurityManager">
        <property name="cacheManager" ref="cacheManager"/>
        <property name="authenticator" ref="authenticator"></property>
        <property name="realms">
            <list>
                <ref bean="jdbcRealm"/>
                <ref bean="secondRealm"/>
            </list>
        </property>
		 <!-- 配置rememberMeManager cookie属性的生存时间-->
         <!--<property name="rememberMeManager.cookie.maxAge" value="10"/>
        <property name="rememberMeManager.cookie.name" value="shiroRememberme"/>-->
    </bean>

		<bean id="rememberMeManager" class="org.apache.shiro.web.mgt.CookieRememberMeManager">
	        <property name="cookie" ref="rememberCookie"/>
	    </bean>
	    <bean id="rememberCookie" class="org.apache.shiro.web.servlet.SimpleCookie">
	        <constructor-arg value="shiroRememberMe"/>
	        <property name="maxAge" value="3600000"/>
	        <property name="httpOnly" value="true"/>
	    </bean>
    <!--
    2. 配置 CacheManager.
    2.1 需要加入 ehcache 的 jar 包及配置文件.
    -->
    <bean id="cacheManager" class="org.apache.shiro.cache.ehcache.EhCacheManager">
        <property name="cacheManagerConfigFile" value="classpath:ehcache.xml"/>
    </bean>

	<!--配置身份验证为多验证-->
    <bean id="authenticator"
          class="org.apache.shiro.authc.pam.ModularRealmAuthenticator">
        <property name="authenticationStrategy">
			<!--配置认证策略，默认为：AtLeastOneSuccessfulStrategy 策略 -->
            <bean class="org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy"></bean>
        </property>
    </bean>
    <!--
    	3. 配置 Realm
    	3.1 直接配置实现了 org.apache.shiro.realm.Realm 接口的 bean
		配置密码用MD5加密，并且加密1024次
    -->
    <bean id="jdbcRealm" class="com.maven.ssm.shiro.realms.ShiroRealm">
        <property name="credentialsMatcher">
            <bean class="org.apache.shiro.authc.credential.HashedCredentialsMatcher">
                <property name="hashAlgorithmName" value="MD5"></property>
                <property name="hashIterations" value="1024"></property>
            </bean>
        </property>

    </bean>

	<!--启用二次认验证，例:开发时移值用ORACLE数据库用了SHA1加密方式-->
    <bean id="secondRealm" class="com.maven.ssm.shiro.realms.SecondShiroRealm">
        <property name="credentialsMatcher">
            <bean class="org.apache.shiro.authc.credential.HashedCredentialsMatcher">
                <property name="hashAlgorithmName" value="SHA1"></property>
                <property name="hashIterations" value="1024"></property>
            </bean>
        </property>
    </bean>

    <!--4. 配置 LifecycleBeanPostProcessor. 可以自定的来调用配置在 Spring IOC 容器中 shiro bean 的生命周期方法.-->
    <bean id="lifecycleBeanPostProcessor" class="org.apache.shiro.spring.LifecycleBeanPostProcessor"/>

    <!--5. 启用 IOC 容器中使用 shiro 的注解. 但必须在配置了 LifecycleBeanPostProcessor 之后才可以使用.-->
    <bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"
          depends-on="lifecycleBeanPostProcessor"/>
    <bean class="org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor">
        <property name="securityManager" ref="securityManager"/>
    </bean>

    <!--
	6. 配置 ShiroFilter.
	6.1 id 必须和 web.xml 文件中配置的 DelegatingFilterProxy 的 <filter-name> 一致.
	                      若不一致, 则会抛出: NoSuchBeanDefinitionException. 因为 Shiro 会来 IOC 容器中查找和 <filter-name> 名字对应的 filter bean. 
	-->
    <bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
        <property name="securityManager" ref="securityManager"/>
        <property name="loginUrl" value="/login"/> <!--登录页-->
        <property name="successUrl" value="/success"/><!--成功页-->
        <property name="unauthorizedUrl" value="/unauthorized"/><!--没授权页-->
        <property name="filterChainDefinitionMap" ref="filterChainDefinitionMap"></property> <!--启用工厂类进行配置-->

        <!--
        	配置哪些页面需要受保护.
        	以及访问这些页面需要的权限.
        	1). anon 可以被匿名访问
        	2). authc 必须认证(即登录)后才可能访问的页面.
        	3). logout 登出.
        	4). roles 角色过滤器
        -->
		<!--配置文件进行静态配置，可用上面的工厂类进行动态配置-->
        <!--<property name="filterChainDefinitions">-->
            <!--<value>-->
                <!--/login.jsp = anon-->
               <!--/login = anon-->
                <!--/checklogin = anon-->
                <!--/logout = logout-->
                <!--/user = roles[test]-->
                <!--/admin = roles[admin]-->

                <!--/shiro/logout = logout-->

                <!--/user.jsp = roles[user]-->
                <!--/admin.jsp = roles[admin]-->

                <!--# everything else requires authentication:-->
                <!--/** = authc-->
            <!--</value>-->
        <!--</property>-->

    </bean>

    <!-- 配置一个 bean, 该 bean 实际上是一个 Map. 通过实例工厂方法的方式 -->
    <bean id="filterChainDefinitionMap"
          factory-bean="filterChainDefinitionMapBuilder" factory-method="buildFilterChainDefinitionMap"></bean>
    <bean id="filterChainDefinitionMapBuilder"
          class="com.maven.ssm.factory.FilterChainDefinitionMapBuilder"></bean>
	</beans>

缓存ehcache.xml

**URL 匹配顺序**

• URL 权限采取第一次匹配优先的方式，即从头开始使用第一个匹配的 url 模式对应的拦截器链

    –/bb/**=filter1

    –/bb/aa=filter2

    –/**=filter3

    – 如果请求的url是“/bb/aa”，因为按照声明顺序进行匹 
       配，那么将使用 filter1 进行拦截。 

**认证**

登录代码可参考LoginController.checkLogin()
 AuthenticationException 
• 如果身份验证失败请捕获 AuthenticationException 或其子类 
• 最好使用如“用户名/密码错误”而不是“用户名错误”/“密码错误”，防止一些恶意用户非法扫描帐号库； 

身份认证流程 

• 1、首先调用 Subject.login(token) 进行登录，其会自动委托给 
   SecurityManager

• 2、SecurityManager 负责真正的身份验证逻辑；它会委托给 
   Authenticator 进行身份验证；

• 3、Authenticator 才是真正的身份验证者，Shiro API 中核心的身份 
   认证入口点，此处可以自定义插入自己的实现；

• 4、Authenticator 可能会委托给相应的 AuthenticationStrategy 进 
   行多 Realm 身份验证，默认 ModularRealmAuthenticator 会调用 
   AuthenticationStrategy 进行多 Realm 身份验证；

• 5、Authenticator 会把相应的 token 传入 Realm ，从 Realm 获取 
   身份验证信息，如果没有返回/抛出异常表示身份验证失败了。此处 
   可以配置多个Realm ，将按照相应的顺序及策略进行访问。 

**认证流程实例：**

1. 获取当前的 Subject. 调用 SecurityUtils.getSubject();
2. 测试当前的用户是否已经被认证. 即是否已经登录. 调用 Subject 的 isAuthenticated() 
3. 若没有被认证, 则把用户名和密码封装为 UsernamePasswordToken 对象

	1). 创建一个表单页面

	2). 把请求提交到 SpringMVC 的 Handler

	3). 获取用户名和密码. 
4. 执行登录: 调用 Subject 的 login(AuthenticationToken) 方法. 
5. 自定义 Realm 的方法, 从数据库中获取对应的记录, 返回给 Shiro.

	1). 实际上需要继承 org.apache.shiro.realm.AuthenticatingRealm 类

	2). 实现 doGetAuthenticationInfo(AuthenticationToken) 方法. 
6. 由 shiro 完成对密码的比对. 

**密码的比对:**
通过 AuthenticatingRealm 的 credentialsMatcher 属性来进行的密码的比对!

**如何把一个字符串加密为 MD5**

替换当前 Realm 的 credentialsMatcher 属性. 直接使用 HashedCredentialsMatcher 对象, 并设置加密算法即可. 

**为什么使用 MD5 盐值加密:**

1). 在 doGetAuthenticationInfo 方法返回值创建 SimpleAuthenticationInfo 对象的时候, 需要使用SimpleAuthenticationInfo(principal, credentials, credentialsSalt, realmName) 构造器

2). 使用 ByteSource.Util.bytes() 来计算盐值. 

3). 盐值需要唯一: 一般使用随机字符串或 user id

4). 使用 new SimpleHash(hashAlgorithmName, credentials, salt, hashIterations); 来计算盐值加密后的密码的值. 

**Realm**

一般继承AuthorizingRealm （授权）即可；其继承了AuthenticatingRealm （即身份验证），而且也间接继承了CachingRealm （带有缓存实现）

**Authenticator**

• Authenticator 的职责是验证用户帐号，是 Shiro API 中身份验 
   证核心的入口点：如果验证成功，将返回AuthenticationInfo 验 
   证信息；此信息中包含了身份及凭证；如果验证失败将抛出相应 
   的 AuthenticationException异常 

• SecurityManager 接口继承了 Authenticator ，另外还有一个 
   ModularRealmAuthenticator实现 ，其委托给多个Realm 进行 
   验证，验证规则通过AuthenticationStrategy接口指定 

**AuthenticationStrategy**

• AuthenticationStrategy 接口的默认实现 ：

• FirstSuccessfulStrategy ：只要有一个 Realm 验证成功即可，只返回第 
   一个 Realm 身份验证成功的认证信息，其他的忽略；

• AtLeastOneSuccessfulStrategy ：只要有一个Realm验证成功即可，和 
   FirstSuccessfulStrategy 不同 ，将返回所有Realm身份验证成功的认证信 
   息；

• AllSuccessfulStrategy ：所有Realm验证成功才算成功，且返回所有 
   Realm身份验证成功的认证信息，如果有一个失败就失败了。

• ModularRealmAuthenticator 默认是AtLeastOneSuccessfulStrategy 
   策略 




# 授权 #

**授权方式** 

Shiro 支持三种方式的授权：

– 编程式：通过写if/else 授权代码块完成

– 注解式：通过在执行的Java方法上放置相应的注解完成，没有权限将抛出相 
   应的异常

– JSP/GSP 标签：在JSP/GSP 页面通过相应的标签完成 

**默认拦截器 **

Shiro 内置了很多默认的拦截器，比如身份验证、授权等相关的。默认拦截器可以参考org.apache.shiro.web.filter.mgt.DefaultFilter中的枚举拦截器： 

**身份验证相关的：**

**authc:**

	org.apache.shiro.web.filter.authc.FormAuthenticationFilter
基于表单的拦截器;如“/**=authc”,如果没有登录会跳到相应的登录页面登录;

主要属性:

	usernameParam:表单提交的用户名参数名（username）
	passwordParam:表单提交的密码参数名（password）
	rememberMeParam:表单提交的记住我参数名(rememberMe)
	loginUrl:登录页面地址(/login.jsp)
	successUrl:登录成功后的默认重定向地址;
	failureKeyAttribute:登录失败后错误信息存储key(shiroLoginFailure)
	
**authcBasic:**
	
	org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
BasicHttp身份验证拦截器，主要属性:
	applicationName:弹出登录框显示的信息(application)

**logout:**

	org.apache.shiro.web.filter.authc.LogoutFilter
退出拦截器，主要属性：
	redirectUrl:退出成功后重定向的地址（/）;如“/logout=logout”

**user:**

	org.apache.shiro.web.filter.authc.UserFilter
用户拦截器，用户已经身份验证或记住我登录的都可;如"/**=user"

**anon:**

	org.apache.shiro.web.filter.authc.AnonymousFilter
匿名拦截器，即不需要登录即可访问;一般用于静态资源过滤，如“/static/**=anon”


**授权相关的**
**roles**
	org.apache.shiro.web.filter.authz.RolesAuthorizationFilter
角色授权拦截器,验证用户是否拥有所有角色;
主要属性:
	loginUrl:登录页面地址（/login.jsp）
	unauthorizedUrl:未授权后重定向的地址;如：“/admin/**=roles[admin]”

**perms**

	org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter
权限授权拦截器，验证用户是否拥有所有权限;属性和roles一样;如“/user/**=perms["user:create"]”

**port**

	org.apache.shiro.web.filter.authz.PortFilter
端口拦截器,主要属性：port(80)：可以通过的端口;如“/test=port[80]”,如果用户访问该页面是非80，将自动将请求端口改为80并重定向到该80端口，其它路径或参数等都一样

**rest**

	org.apache.shiro.web.filter.authz.HttpMethodPermissionFilter
rest风格拦截器，自动根据请求方法构建权限字符串
（GET=read,Post=create,PUT=update,DELETE=delete,HEAD=head,TRACE=read,OPTIONS=read,MKCOL=read）构建权限字符串：如：“/users=rest[user]”,会自动拼出"user:read,user:create,user:update,user:delete"权限字符串进行权限匹配(所有都得匹配,isPermittedAll)
**ssl**
	org.apache.shiro.web.filter.authz.SslFilter
SSL拦截器，只有请求协议是https才能通过;否则自动跳转https端口（443）;其它和port拦截器一样

其它

**noSessionCreation**

	org.apache.shiro.web.filter.session.NoSessionCreationFilter
不创建会话拦截器，调用subject.getSession(false)不会有什么问题,但是如果subject.getSession(true)将抛出

**授权流程**

1、首先调用 Subject.isPermitted*/hasRole* 接口，其会委托给SecurityManager ，而 SecurityManager 接着会委托给 Authorizer；

2、Authorizer是真正的授权者，如果调用如isPermitted(“user:view”) ，其首先会通过PermissionResolver 把字符串转换成相应的 Permission 实例；

3、在进行授权之前，其会调用相应的 Realm 获取 Subject 相应的角色/权限用于匹配传入的角色/权限；

4、Authorizer 会判断 Realm 的角色/权限是否和传入的匹配，如果有多个Realm ，会委托给 ModularRealmAuthorizer 进行循环判断，如果匹配如 isPermitted*/hasRole* 会返回true ，否则返回false表示授权失败。


**ModularRealmAuthorizer**

ModularRealmAuthorizer 进行多 Realm 匹配流程：

1、首先检查相应的 Realm 是否实现了实现了Authorizer ；

2、如果实现了 Authorizer ，那么接着调用其相应的isPermitted*/hasRole* 接口进行匹配；

3、如果有一个Realm匹配那么将返回 true ，否则返回 false。 

# Shiro标签 #
**guest 标签**：用户没有身份验证时显示相应信息，即游客访问信息：

	<shiro:guest>
		欢迎游客访问，<a href="login.jsp">登录</a>
	</shiro:guest>

**user 标签**：用户已经经过认证/记住我登录后显示相应的信息。
	
	<shiro:user>
		[<shiro:principal/>]登录，<a href="logout">退出</a>
	</shiro:user>


**authenticated 标签**：用户已经身份验证通过，即Subject.login登录成功，不是记住我登录的

	<shiro:authenticated>
		用户[<shiro:principal/>]身份验证通过
	</shiro:authenticated>

**notAuthenticated 标签**：用户未进行身份验证，即没有调用Subject.login进行登录，包括记住我自动登录的也属于未进行身份验证。

	<shiro:notAuthenticated>
		未通过身份验证（包括记住我）
	</shiro:notAuthenticated>
 

**pincipal 标签**：显示用户身份信息 ，默认调用Subject.getPrincipal() 获取，即           Primary Principal。 
		
	<shiro:principal property="username" />

**hasRole 标签**：如果当前 Subject 有角色将显示 body 体内容：

	<shiro:hasRole name="admin">
		用户[<shiro:principal/>]拥有角色admin
	</shiro:hasRole>

**hasAnyRoles 标签**：如果当前Subject有任意一个角色（或的关系）将显示body体内容。 

	<shiro:hasAnyRoles name="admin,user">
		用户[<shiro:principal/>]拥有角色admin或user
	</shiro:hasAnyRoles>

**lacksRole** ：如果当前 Subject 没有角色将显示 body 体内容

	<shiro:lacksRole name="admin">
		用户[<shiro:principal/>]拥有没有角色admin
	</shiro:lacksRole>

**hasPermission** ：如果当前 Subject 有权限将显示 body 体内容 
	
	<shiro:hasPermission name="user:create">
		用户[<shiro:principal/>]拥有权限user:create
	</shiro:hasPermission>

**lacksPermission**：如果当前Subject没有权限将显示body体内容。

	<shiro:lacksPermission name="org:create">
		用户[<shiro:principal/>]没有权限org:create
	</shiro:lacksPermission>

**权限注解**

• @RequiresAuthentication ：表示当前Subject已经通过login进行了身份验证；即 Subject. isAuthenticated() 返回true

• @RequiresUser ：表示当前 Subject 已经身份验证或者通过记住我登录的

• @RequiresGuest ：表示当前Subject没有身份验证或通过记住我登录过，即是游客身份。

• @RequiresRoles(value={“admin”, “user”}, logical=Logical.AND) ：表示当前 Subject 需要角色admin 和user

• @RequiresPermissions (value={“user:a”, “user:b”}, logical= Logical.OR) ：表示当前 Subject 需要权限 user:a 或 user:b。 

可参考ShiroService（可把权限注解到Service层上控制Controller层的访问）
 
**这里有一个问题要注意：**

在Service方法上使用注解 @Transactional 即在方法开始的时候会有事务，这个时候这个Service已经是一个代理对象，

这个时候把权限注解加到Service上是不好用的，会发生类型转换异常。需要加到Controller上，因为不能够让Service是代理的代理。

**自定义拦截器**

通过自定义拦截器可以扩展功能，例如：动态url-角色/权限访问控制的实现、根据 Subject 身份信息获取用户信息绑定到 Request （即设置通用数据）、验证码验证、在线用户信息的保存等 

com.maven.ssm.factory.FilterChainDefinitionMapBuilder自定义动态配置权限访问权限

#会话管理 #

**相关API：**

	Subject.getSession() ：即可获取会话；其等价于Subject.getSession(true) ，即如果当前没有创建 Session 对象会创建一个 ；Subject.getSession(false) ，如果当前没有创建 Session 则返回null
	
	session.getId() ：获取当前会话的唯一标识
	
	session.getHost() ：获取当前Subject的主机地址
	
	session.getTimeout() & session.setTimeout(毫秒) ：获取/设置当 
	   前Session的过期时间
	
	session.getStartTimestamp() & session.getLastAccessTime() ：获取会话的启动时间及最后访问时间；如果是 JavaSE 应用需要自己定期调用 session.touch() 去更新最后访问时间；如果是 Web 应用 ，每次进入 ShiroFilter 都会自动调用 session.touch() 来更新最后访问时间。 
	
	session.touch() & session.stop() ：更新会话最后访问时间及销毁会话；当Subject.logout()时会自动调用 stop 方法来销毁会话。如果在web中，调用 HttpSession. invalidate()也会自动调用Shiro Session.stop 方法进行销毁Shiro 的会话 
	
	session.setAttribute(key, val) & session.getAttribute(key) & session.removeAttribute(key) ：设置/获取/删除会话属性；在整个会话范围内都可以对这些属性进行操作 

**会话监听器**

会话监听器用于监听会话创建、过期及停止事件 
		
	public interface SessionListener {
	    void onStart(Session var1);
	
	    void onStop(Session var1);
	
	    void onExpiration(Session var1);
	}

**SessionDao**

• AbstractSessionDAO 提供了 SessionDAO 的基础实现 ，如生成会话ID等

• CachingSessionDAO 提供了对开发者透明的会话缓存的功能 ，需要设置相应的 CacheManager

• MemorySessionDAO 直接在内存中进行会话维护

• EnterpriseCacheSessionDAO 提供了缓存功能的会话维护，默认情况下使用 MapCache 实现 ，内部使用ConcurrentHashMap 保存缓存的会话。 

可用作数据持久化

	//配置
	<bean id="sessionManager" class="org.apache.shiro.web.session.mgt.DefaultWebSessionManager" >
        <property name="sessionDAO" ref="sessionDao"/>
        <property name="globalSessionTimeout" value="360000"/>
        <property name="deleteInvalidSessions" value="true"/>
        <property name="sessionValidationSchedulerEnabled" value="true"/>

    </bean>
	//继承EnterpriseCacheSessionDAO方法重写doCreate(),doReadSession(),doUpdate(),doDelete()，使用SerializableUtils序列化Session进行保存
    <bean id="sessionDao" class="com.maven.ssm.dao.SessionDAO">
        <property name="activeSessionsCacheName" value="shiro-activeSessionCache"/>
        <property name="sessionIdGenerator" ref="sessionIdGenerator"/>
    </bean>
    <bean id="sessionIdGenerator" class="org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator"/>
	
ehcache.xml

	 <cache name="shiro-activeSessionCache"
           eternal="false"
           timeToIdleSeconds="3600"
           timeToLiveSeconds="0"
           overflowToDisk="false"
           statistics="true">
    </cache>

com.maven.ssm.utils.SerializableUtils
使用序列化就运用了“对象输出流”，包装了“字节组输出流”来为Session进行序列化；
而反序列化运用了“对象输入流”，包装了“字节组输入流”，来对Session的序列字符串进行实体类转换。

**会话验证**

• Shiro 提供了会话验证调度器 ，用于定期的验证会话是否已过期，如果过期将停止会话

• 出于性能考虑，一般情况下都是获取会话时来验证会话是否过期并停止会话的；但是如在 web 环境中，如果用户不主动退出是不知道会话是否过期的，因此需要定期的检测会话是否过期 ，Shiro 提供了会话验证调度器SessionValidationScheduler 

• Shiro 也提供了使用Quartz会话验证调度器 ：QuartzSessionValidationScheduler


# 缓存 #

**CacheManagerAware 接口**

• Shiro 内部相应的组件（DefaultSecurityManager ）会自动检测相应的对象（如Realm ）是否实现了CacheManagerAware 并自动注入相应的CacheManager。

**Realm 缓存**

• Shiro 提供了 CachingRealm ，其实现了CacheManagerAware 接口 ，提供了缓存的一些基础实现； 
• AuthenticatingRealm 及AuthorizingRealm 也分别提供了对AuthenticationInfo 和 AuthorizationInfo 信息的缓存。 

**Session 缓存**

• 如 SecurityManager 实现了 SessionSecurityManager ，其会判断 SessionManager 是否实现了CacheManagerAware 接口 ，如果实现了会把CacheManager 设置给它。

• SessionManager 也会判断相应的 SessionDAO （如继承自CachingSessionDAO ）是否实现了CacheManagerAware ，如果实现了会把 CacheManager设置给它 

• 设置了缓存的 SessionManager ，查询时会先查缓存，如果找不到才查数据库。 

# RememberMe #

**认证和记住我** 

• subject.isAuthenticated() 表示用户进行了身份验证登录的，即使有 Subject.login 进行了登录； 

• subject.isRemembered() ：表示用户是通过记住我登录的，此时可能并不是真正的你（如你的朋友使用你的电脑，或者你的cookie 被窃取）在访问的 

• 两者二选一 ，即 subject.isAuthenticated()==true ，则subject.isRemembered()==false ；反之一样。 

**建议** 

• 访问一般网页 ：如个人在主页之类的，我们使用user 拦截器即可，user 拦截器只要用户登录(isRemembered() || isAuthenticated())过即可访问成功；

• 访问特殊网页 ：如我的订单，提交订单页面，我们使用authc 拦截器即可，authc 拦截器会判断用户是否是通过Subject.login （isAuthenticated()==true ）登录的，如 果是才放行，否则会跳转到登录页面叫你重新登录。 

**实现** 

如果要自己做RememeberMe ，需要在登录之前这样创建Token：UsernamePasswordToken(用户名 ，密码，是否记住我) ，且调用UsernamePasswordToken的：token.setRememberMe(true); 方法 

