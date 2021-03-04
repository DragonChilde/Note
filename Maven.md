# Maven简介

## Maven概述

Maven是Apache软件基金会组织维护的一款自动化构建工具。主要有两个作用

1. maven工程对jar包的管理过程

2. 项目的一键构建

   1. maven工程对jar包的管理过程

      ![](http://120.77.237.175:9080/photos/maven/01.png)

   2. 项目的一键构建

      1. 先来说说什么是构建？指的是项目从编译、测试、运行、打包、安装 ，部署整个过程都交给 maven 进行管理，这个过程称为构建

      2. 一键构建指的是整个构建过程，使用 maven 一个命令可以轻松完成整个工作

         ![](http://120.77.237.175:9080/photos/maven/02.png)



# Maven核心概念

## Maven目录结构

1. maven中约定的目录结构图解

   ![](http://120.77.237.175:9080/photos/maven/03.png)

2. 各个目录结构详解

   ```
   Maven工程的目录结构
   (1). src/main/java —— 存放项目的.java 文件
   (2). src/main/resources —— 存放项目资源文件，如 spring, hibernate 配置文件
   (3). src/test/java —— 存放所有单元测试.java 文件，如 JUnit 测试类
   (4). src/test/resources —— 测试资源文件
   (5). target —— 项目输出位置，编译后的 class 文件会输出到此目录
   (6). pom.xml——maven 项目核心配置文件
   注意：如果是普通的 java 项目，那么就没有 webapp 目录
   ```

## pom.xml

1. modelVersion:Maven模型的版本,对于Maven2和Maven3来说,它只能是4.0.0
2. **`groupId:`组织id,一般是公司域名的倒写,格式可以为**
   1. 域名倒写,例如 com.baidu
   2. 域名倒写+项目名,例如com.baidu.appolo
3. **`artifactId:`项目名称,也是模块名称对应groupId中项目中的子项目**
4. **`version:`项目的版本号。如果项目还在开发中,是不稳定版本,通常在版本后带 -SNAPSHOT。version使用三位数字标识,例如1.1.0**
5. name:项目的名称
6. packaging:项目打包的类型,可以使 jar、war、rar、ear、pom,默认是 jar(一般父工程设置为pom,子工程设置为jar),web项目是war包
7. **`dependencies | dependency`：Maven 的一个重要作用就是管理jar包,为了一个项目可以构建或运行,项目中不可避免的,会依赖很多其他的jar包,在Maven中,这些 jar 就被称为依赖,使用标签dependency来配置。而这种依赖的配置正是通过坐标来定位的,由此我们也不难看出,maven 把所有的 jar包也都视为项目存在了**
8. properties:是用来定义一些配置属性的,例如project.build.sourceEncoding（项目构建源码编码方式）,可以设置为UTF-8,防止中文乱码,也可定义相关构建版本号,便于日后统一升级(配置属性)
9. build:表示与构建相关的配置,例如设置编译插件的jdk版本(构建)
10. **`parent:`在Maven中,如果多个模块都需要声明相同的配置,例如：groupId、version、有相同的依赖、或者相同的组件配置等,也有类似Java的继承机制,用parent声明要继承的父工程的pom配置(继承)**
    1. **`modules:`在Maven的多模块开发中,为了统一构建整个项目的所有模块,可以提供一个额外的模块,该模块打包方式为pom,并且在其中使用modules聚合的其它模块,这样通过本模块就可以一键自动识别模块间的依赖关系来构建所有模块,叫Maven的聚合**
    2. description:描述信息
    3. relativePath
11. 父项目的pom.xml文件的相对路径。默认值为…/pom.xml。maven首先从当前构建项目开始查找父项目的pom文件,然后从本地仓库,最后从远程仓库。RelativePath允许你选择一个不同的位置
12. 如果默认…/pom.xml没找到父元素的pom,不配置relativePath指向父项目的pom则会报错

## 仓库repository

1. 本地仓库 ：用来存储从远程仓库或中央仓库下载的插件和 jar 包，项目使用一些插件或 jar 包，优先从本地仓库查找

   ```
   <!--本地仓库配置mavne安装目录下的settings.xml文件里-->
   <localRepository>D:\Server\LocalRepository</localRepository>
   ```

2. 远程仓库

   1. 中央仓库：在maven软件中内置一个远程仓库地址http://repo1.maven.org/maven2,它是中央仓库,服务于整个互联网,它是由Maven团队自己维护,里面存储了非常全的jar包,它包含了世界上大部分流行的开源项目构件
   2. 私服:在局域网环境中部署的服务器,为当前局域网范围内的所有Maven工程服务。公司中常常使
   3. 中央仓库的镜像：架设在不同位置,欧洲,美洲,亚洲等每个洲都有若干的服务器,为中央仓库分担流量。减轻中央仓库的访问,下载的压力。所在洲的用户首先访问的是本洲的镜像服务器

![](http://120.77.237.175:9080/photos/maven/04.png)

## Maven的生命周期

maven 对项目构建过程分为三套相互独立的生命周期，请注意这里说的是“三套”，而且“相互独立”,这三套生命周期分别是

1. Clean Lifecycle 在进行真正的构建之前进行一些清理工作
2. Default Lifecycle 构建的核心部分，编译，测试，打包，部署等等
3. Site Lifecycle 生成项目报告，站点，发布站点

![](http://120.77.237.175:9080/photos/maven/05.png)

对于我们程序员而言,**无论我们要进行哪个阶段的构建,直接执行相应的命令即可,无需担心它前边阶段是否构建,Maven都会自动构建**。这也就是Maven这种自动化构建工具给我们带来的好处

## Maven常用命令

- mvn clean:将target目录删除,但是已经 install 到仓库里的包不会删除

- mvn compile:编译(执行的是主程序下的代码）

- mvn test:测试(不仅仅编译了src/main/java下的代码，也编译了java/test/java下的代码）

- mvn package:打包(执行的时候回打成war包，编译了src/main/java下的代码，也编译了java/test/java下的代码）

- mvn install:安装(执行的时候回打成war包，编译了src/main/java下的代码，也编译了java/test/java下的代码，将这个包安装到了本地仓库中）

- mvn deploy:部署(需要配置才能执行）

  > **`注意：`**运行Maven命令时一定要进入pom.xml文件所在的目录！

## 插件plugings

maven提供的功能,用来执行清理、编译、测试、报告、打包的程序

- **clean插件`maven-clean-plugin:2.5`**
  clean阶段是独立的一个阶段,功能就是清除工程目前下的target目录
- **resources插件`maven-resources-plugin:2.6`**
  resource插件的功能就是把项目需要的配置文件拷贝到指定的目当,默认是拷贝src\main\resources目录下的件到classes目录下
- **compile插件`maven-compiler-plugin`**
  compile插件执行时先调用resouces插件,功能就是把src\mainjava源码编译成字节码生成class文件,并把编译好的class文件输出到target\classes目录下
- **test测试插件**
  单元测试所用的compile和resources插件和主代码是相同的,但执行的目标不行,目标testCompile和testResources是把src\test\java下的代码编译成字节码输出到target\test-classes,同时把src\test\resources下的配置文件拷贝到target\test-classes
- **package打包插件`maven-jar-plugin`**
  这个插件是把class文件、配置文件打成一个jar(war或其它格式)包
- **deploy发布插件`maven-install-plugin`**
  发布插件的功能就是把构建好的artifact部署到本地仓库,还有一个deploy插件是将构建好的artifact部署到远程仓库

![](http://120.77.237.175:9080/photos/maven/06.png)

## 坐标gav

- Maven把任何一个插件都作为仓库中的一个项目进行管理,用一组(三个)向量组成的坐标来表示。坐标在仓库中可以唯一定位一个Maven项目
- groupId:组织名,通常是公司或组织域名倒序+项目名
- artifactId:模块名,通常是工程名
- version:版本号
  (需要特别指出的是,项目在仓库中的位置是由坐标来决定的:groupId、artifactId和version决定项目在仓库中
  的路径,artifactId和version决定jar包的名称)

## 依赖dependency

- 一个Maven项目正常运行需要其它项目的支持,Maven会根据坐标自动到本地仓库中进行查找。对于程序员自己的Maven项目需要进行安装,才能保存到仓库中
-  不用maven的时候所有的jar都不是你的,需要去各个地方下载拷贝,用了maven所有的jar包都是你的,想要谁,叫谁的名字就行。maven帮你下载

# Maven全局配置

在mavne安装目录下的settings.xml文件里配置如下,

```
 <!--指定JDK全局JDK版本-->
 <profiles>
     <profile>  
             <id>jdk-1.8</id> 
              <activation>  
                  <activeByDefault>true</activeByDefault>  
                  <jdk>1.8</jdk>  
              </activation>  
              <properties>  
              	<!--源码编译jdk版本-->	
                  <maven.compiler.source>1.8</maven.compiler.source>  
                  <!--运行代码的jdk版本-->
                  <maven.compiler.target>1.8</maven.compiler.target>  
                  <!--项目构建使用的编码,避免中文乱码-->
                  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                  <!--生成报告的编码-->
                  <maven.compiler.compilerVersion>1.8</maven.compiler.compilerVersion>  
              </properties>   
    </profile>  
<profiles>
```

# IDEA配置Maven

![](http://120.77.237.175:9080/photos/maven/07.jpg)

- -DarchetypeCatalog=internal
  这个参数的意思是:
  如果我们使用maven为我们提供好的骨架来创建maven工程,一般是要联网的.
  为了在不联网的情况下我们可以正常创建工程,配了这样一个参数,只要我们之前联网下载过之前相关创建工程的插件,它就会从本地仓库找到对应插件,而不会联网下载

## 使用骨架创建maven的java工程

![](http://120.77.237.175:9080/photos/maven/08.jpg)

## 创建一个maven的web工程

![](http://120.77.237.175:9080/photos/maven/09.jpg)

> 注：如果不想用模板，只想创建普通的maven项目， 在上图不用勾选从原型创建即可

## Servlet冲突问题

1. 开发过程中,web的工程的项目中,因为创建了Servlet,但报错,因为缺少了相应的jar包,要解决问题,就是要将`servlet-api-xxx.jar`包放进来,作为maven工程应当添加servlet的坐标,从而导入它的jar包

2. jar包冲突问题

   因为在开发

pom.xml中导入的jar包和maven自带的tomcat中的jar包发生了冲突