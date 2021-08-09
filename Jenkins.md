

# 配置

## SVN版本控制器

安装过程略,只记录一下版本库的访问账号和密码

```
test = 123456
```

> 注意:`svnserve.conf`文件中1anon-acces1s一定要打开注释并设置为`none`
>
> ![](http://120.77.237.175:9080/photos/jenkins/01.jpg)

## Tomcat

同装详细过程略,仅记录Tomcat服务器的账号密码,配置文件位置:/opt/tomcat/conf/tomcat-users.xml

![](http://120.77.237.175:9080/photos/jenkins/02.jpg)

如果是远程服务器,需要修改配置`/opt/tomcat/webapps/manager/META-INF/context.xml`文件

```xml
<Context antiResourceLocking="false" privileged="true" >
      <CookieProcessor className="org.apache.tomcat.util.http.Rfc6265CookieProcessor"
                       sameSiteCookies="strict" />
      <Valve className="org.apache.catalina.valves.RemoteAddrValve"
             allow="127\.\d+\.\d+\.\d+|::1|0:0:0:0:0:0:0:1|\d+\.\d+\.\d+\.\d+" />	 ##此处要允许宿主机的ip访问
      <Manager sessionAttributeValueClassNameFilter="java\.lang\.(?:Boolean|Integer|Long|Number|String)|org\.apache\.catalina\.filters\.CsrfPreventionFilter\$LruCache(?:\$1)?|java\.util\.(?:Linked)?HashMap"/>
</Context>
```

## Jenkins

- 把`jenkins.war`放在`Tomcat`解压目录`/webapps`目录下

- 打开Tomcat解压目录`/server.xml`修改`url`地址的编码解码字符集

  ```sh
  vim /opt/tomcat/conf/server.xml
  ```

  ```xml
   <Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" URIEncoding="UTF-8"/>
  ```

- 启动Tomcat并通过浏览器访问 `http://127.0.0.1:9090/jenkins`

