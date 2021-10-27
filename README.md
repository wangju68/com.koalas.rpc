 <p align="center">
  <img src="https://images.gitee.com/uploads/images/2019/0417/122151_abcd2032_536094.png" width="300">
  <br>
  <img src="https://images.gitee.com/uploads/images/2021/0111/210007_ac6e2b22_536094.png">
 <br>
  <a href="https://www.apache.org/licenses/LICENSE-2.0" align="center">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
  <a href="https://github.com/996icu/996.ICU/blob/master/LICENSE" align="center">
    <img alt="996icu" src="https://img.shields.io/badge/license-NPL%20(The%20996%20Prohibited%20License)-blue.svg">
  </a>
</p>

# 一：项目介绍
[开发者接入](https://gitee.com/a1234567891/koalas-rpc/wikis/%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E?sort_id=1424137)

koalas-RPC 个人作品，提供大家交流学习，有意见请私信，欢迎拍砖。客户端采用thrift协议，服务端支持netty和thrift的TThreadedSelectorServer半同步半异步线程模型，支持动态扩容，服务上下线，权重动态，可用性配置，页面流量统计等，QPS统计，TP90,TP99,TP95等丰富可视化数据，持续为个人以及中小型公司提供可靠的RPC框架技术方案。

##### 1：为什么要写这个RPC
市面上常见的RPC框架很多，grpc，motan，dubbo等，但是随着越来越多的元素加入，复杂的架构设计等因素似使得这些框架和spring一样，虽然号称是轻量级，但是用起来却是让我们很蹩脚，大量的配置，繁杂的API设计，其实，我们根本用不上这些东西！！！ 我也算得上是在很多个互联网企业厮杀过，见过很多很多的内部RPC框架，有些优秀的设计让我非常赞赏，有一天我突然想着，为什么不对这些设计原型进行聚合归类，自己搞一套【轻量级】RPC框架呢，碍于工作原因，一直没有时间倒腾出空，十一期间工作闲暇，说搞就搞吧，落地不易，很多细节性问题，比如tcp中怎么解决大量的wait-time,如何做到thrift和netty的兼容等等大量细节的优化，希望源码对大家对认识RPC框架起到推进的作用。东西越写越多，有各种问题欢迎随时拍砖

##### 2：为什么叫koalas
树袋熊英文翻译，希望考拉RPC给那些不太喜欢动手自己去造轮子的人提供可靠的RPC使用环境

##### 3：技术栈
- [x] thrift 0.8.0
- [x] spring-core-4.2.5，spring-context-4.2.5，spring-beans-4.2.5等spring源码
- [x] log4j，slf4j
- [x] org.apache.commons(v2.0+)
- [x] io.netty4
- [x] fastJson
- [x] zookeeper
- [x] 点评cat（V3.0.0+ 做数据大盘统计上报等使用，可不配置）
- [x] AOP，反射代理等

##### 4：关于技术选型
1. 序列化篇 考察了很多个序列化组件，其中包括jdk原生，kryo、hessian、protoStuff,thrift，json等，最终选择了Thrift，原因如下 原生JDK序列化反序列化效率堪忧，其序列化内容太过全面kryo和hessian，json相对来说比原生JDK强一些，但是对跨语言支持一般，所以舍弃了，最终想在protoBuf和Thrift协议里面选择一套框架，这俩框架很相通，支持跨语言，需要静态编译等等。但是protoBuf不带RPC服务，本着提供多套服务端模式（thrift rpc，netty）的情况下，最终选择了Thrift协议。
2. IO线程模型篇 原生socket可以模拟出简单的RPC框架，但是对于大规模并发，要求吞吐量的系统来说，也就算得上是一个demo级别的，所以BIO肯定是不考虑了，NIO的模型在序列化技术选型的时候已经说了，Thrift本身支持很多个io线程模型，同步，异步，半同步异步等（SimpleServer，TNonblockingServer，THsHaServer，TThreadedSelectorServer，TThreadPoolServer），其中吞吐量最高的肯定是半同步半异步的IO模TThreadedSelectorServer了，具体原因大家可自行google，这次不做多的阐述，选择好了模型之后，发现thrift简直就是神器一样的存在，再一想，对于服务端来说，IO模型怎么能少得了Netty啊，所以下决心也要支持Netty，但是很遗憾Netty目前没有对Thrift的序列化解析，拆包粘包的处理，但是有protoBuf，和http协议的封装，怎么办，自己在netty上写对thrift的支持呗，虽然工作量大了一些，但是一想netty不就是干这个事儿的嘛- -！
3. 服务发现 支持集群的RPC框架里面，像dubbo，或者是其他三方框架，对服务发现都进行的封装，那么自研RPC的话，服务发现就要自己来写了，那么简单小巧容易上手的zookeeper肯定是首选了。
![输入图片说明](https://gitee.com/uploads/images/2019/0425/175955_058b77a7_536094.png "屏幕截图.png")

##### 5：安装教程
考拉RPC确保精简，轻量的原则，只需要zk服务器进行服务发现（后续版本服务治理可能需要Datasource），对于zookeeper的各个环境安装教程请自行google，不在本安装教程内特意说明 如果需要cat的数据大盘功能，想更方便的查看服务的调用情况，需要安装cat服务，至于cat的安装就更简单了，就是war包扔在tomcat里面运行，然后配置一些参数即可，当然你也可以不接入cat，单独的作为RPC框架来使用。 CAT接入参考：https://github.com/dianping/cat
本地clean install有可能会本地报错 找不到maven依赖，这里推荐将cat下载到本地 clean  install一下即可

```
 <dependency>
        <groupId>com.dianping.cat</groupId>
        <artifactId>cat-client</artifactId>
        <version>3.0.0</version>
  </dependency>
```


# 二：使用说明
##### 1：前期准以及依赖
maven依赖

```
 <dependency>
        <groupId>koalas.rpc</groupId>
        <artifactId>com.Koalas.rpc</artifactId>
        <version>Koalas-1.0-SNAPSHOT</version>
    </dependency>
```
关于私服的引用问题，记得全局文件不要把全局的依赖都代理掉，因为这么做只能从aliyun的私服上下载项目，由于koalas-rpc中的Cat依赖只在美团点评的私有仓库中存在，这么做会下载依赖失败，所以不要暴力的设置下面的代理做法。

```
    <mirror>
        <id>nexus-aliyun</id>
        <mirrorOf>*</mirrorOf>
        <name>Nexus aliyun</name>
        <url>http://maven.aliyun.com/nexus/content/groups/public</url>
    </mirror>
```
正确的做法是将代理去掉，直接按照作者在pom.xml文件中给定的依赖仓库地址就可以了。


首先需要编写自己的thrift idl文件了，这里多说一句，在群里的小伙伴曾经说过idl文件编写不熟悉，有可能出错 这里顺带说一嘴，thrift的ldl文件和写java的请求体和service几乎没有任何区别，熟能生巧，上手之后非常简单，推荐大家去google一些相关的入门文章即可 下面截图为测试的thrift文件

```
namespace java thrift.service

include 'TestCreateAccountRequest.thrift'
include 'TestCreateAccountRespone.thrift'

service TestCreateAccountService {
      TestCreateAccountRespone.TestCreateAccountRespone getRPC(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest1(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest2(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest3(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest4(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest5(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest6(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest7(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest8(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest9(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest10(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest11(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
      TestCreateAccountRespone.TestCreateAccountRespone koaloasTest12(1:TestCreateAccountRequest.TestCreateAccountRequest TestCreateAccountRequest);
}
```


```
namespace java thrift.domain
/**
* 测试类
**/
struct TestCreateAccountRequest {

    1:i32 source,

    2:i32 accountType,

    3:i64 partnerId,

    4:i32 partnerType,

    5:string partnerName,

    6:i32 poiFlag,
}
```

```
namespace java thrift.domain
/**
* 测试类
**/
struct TestCreateAccountRespone {
    1:i32 code,
    2:string message,
}
```

编译器需要大家去下载对应的版本 windows和linux下不同的编译器，下载地址http://archive.apache.org/dist/thrift/0.8.0/ 下载0.8.0版本即可,0.8.0版本是很老的版本了，但是相对稳定，后续会把thirft版本升级。如果上面地址下载不下来或者失效，可以上作者的网盘上下载zip包，上面有win版本和mac，linux版本的0.8.0的thrift编译器，链接: https://pan.baidu.com/s/1JpLqVbmokTOe30nU_TznWw 提取码: ntye，
编译上面三个文件 thrift -gen java TestCreateAccountService.thrift, thrift -gen java TestCreateAccountRequest.thrift, thrift -gen java TestCreateAccountRespone.thrift 在当前目录下会生成3个java文件 这三个文件分别是请求体，返回体，和服务类，就这么简单 Ok作为开发者而言，所有的准备工作都结束了。下面就开始进入实际开发~

#### 2：xml配置方式
**1. 客户端同步调用**

---
首先在你的xml里面配置一下引用

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xmlns:koalas="http://www.koalas.com/schema/ch"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans
	                       http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
                           http://www.koalas.com/schema/ch
                           http://www.koalas.com/schema/ch.xsd">

	<koalas:client id="TestCreateAccountService1"
			   serviceInterface="thrift.service.TestCreateAccountService"
			   zkPath="127.0.0.1:2181"/>
</beans>

```
首先引用koalas的自定义schema,xmlns:koalas和xsi:schemaLocation，
其中serviceInterface为thrift自动生成的java类，zkPath为zk的服务地址，默认是同步调用，接下来就是在java里面的远程调用了。


```
package thrift.service;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import thrift.domain.TestCreateAccountRequest;
import thrift.domain.TestCreateAccountRespone;

@Service("testService")
public class TestService {

    @Autowired
    TestCreateAccountService.Iface TestCreateAccountService;

    public void getRemoteRpc() throws TException {

        TestCreateAccountRequest request= new TestCreateAccountRequest (  );
        //request.setSource ( 10 );
        request.setAccountType ( 1 );
        request.setPartnerId ( 1 );
        request.setPartnerType ( 1 );
        request.setPartnerName ( "你好" );
        request.setPoiFlag ( 1 );
        TestCreateAccountRespone respone = TestCreateAccountService.getRPC (  request);
        System.out.println (respone);
     }
}
```
就这么简单一个高性能的RPC框架就诞生了。TestCreateAccountService是thrift自动生成的，作为使用者而言不需要做任何事情，只需要在spring bean中注入xxx.Iface即可。


**2. 客户端异步调用**

---
刚刚我们看了客户端的同步调用方式，下面我们一起来看看异步的使用方式，
首先在你的xml里面配置一下引用

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xmlns:koalas="http://www.koalas.com/schema/ch"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
                           http://www.koalas.com/schema/ch
                           http://www.koalas.com/schema/ch.xsd">

	<koalas:client id="TestCreateAccountService2"
	       serviceInterface="thrift.service.TestCreateAccountService"
               zkPath="127.0.0.1:2181"
	       async="true"/>
</beans>

```
和同步的区别async=true，代表异步使用，接下来就是在java里面的异步远程调用了

```
package thrift.service;

import client.async.KoalasAsyncCallBack;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import thrift.domain.TestCreateAccountRequest;
import thrift.domain.TestCreateAccountRespone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
@Service("testService")
public class TestService2 {
    @Autowired
    TestCreateAccountService.AsyncIface TestCreateAccountService;
    public void getRemoteRpc() throws TException{
        KoalasAsyncCallBack<TestCreateAccountRespone, TestCreateAccountService.AsyncClient.getRPC_call> 
        koalasAsyncCallBack = new KoalasAsyncCallBack<> ();
        TestCreateAccountRequest request= new TestCreateAccountRequest (  );
        request.setAccountType ( 1 );
        request.setPartnerId ( 1 );
        request.setPartnerType ( 1 );
        request.setPartnerName ( "你好啊" );
        request.setPoiFlag ( 1 );
        TestCreateAccountService.getRPC ( request ,koalasAsyncCallBack);
        Future<TestCreateAccountRespone> future= koalasAsyncCallBack.getFuture ();
        try {
            //to get other things
            System.out.println (future.get ());
        } catch (InterruptedException e) {
            e.printStackTrace ();
        } catch (ExecutionException e) {
            e.printStackTrace ();
        }
    }

}
```
这次调用getRpc方法不会阻塞等待server同步结果了。而是可以去干一些自己的其他事情，然后在调用future.get ()来获得返回resopne，当然future.get ()支持最大等待时间的，超时之后会抛出TimeOutException,当然这仅仅是client超时而已不会影响server的执行结果。

**3. 服务端实现**

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:koalas="http://www.koalas.com/schema/ch"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
	   http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
	   http://www.springframework.org/schema/context
	   http://www.springframework.org/schema/context/spring-context-4.2.xsd
	   http://www.koalas.com/schema/ch
	   http://www.koalas.com/schema/ch.xsd">

    <koalas:server id="TestCreateAccountService"
                   serviceInterface="thrift.service.TestCreateAccountService"
                   serviceImpl="TestCreateAccountServiceImpl"
                   port="8001"
                   zkpath="127.0.0.1:2181"/>
</beans>
```
服务端只需要指定暴露的端口，zk服务地址和服务端实现即可。

```
@Service
public class TestCreateAccountServiceImpl implements TestCreateAccountService.Iface {
    @Override
    public TestCreateAccountRespone getRPC(TestCreateAccountRequest TestCreateAccountRequest) throws TException {
        TestCreateAccountRespone TestCreateAccountRespone = new TestCreateAccountRespone ();
        TestCreateAccountRespone.setCode ( 1 );
        TestCreateAccountRespone.setMessage ( "你好" );
        if(new Random (  ).nextInt ( 5 )>100){
            throw new RuntimeException ( "测试错误" );
        }
        System.out.println ( "getRPC  start ...." + TestCreateAccountRequest + "------" + atomicInteger.incrementAndGet () );

        return TestCreateAccountRespone;
    }
}
```
只需要实现xxxx.Iface即可

#### 3：注解配置方式
有的小伙伴会觉得配置xml有点麻烦，koalas-rpc也提供了纯注解的使用方式

**1. 客户端调用**

xml中的配置

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xmlns:koalas="http://www.koalas.com/schema/ch"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans
	                       http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
                           http://www.koalas.com/schema/ch
                           http://www.koalas.com/schema/ch.xsd">

	<koalas:annotation package="thrift.annotation.client.impl"/>
</beans>
```
一个扫描标签就行了，如果你在spring bean里想通过调用rpc远程服务，那么扫描一下就行了

java中使用

```
@Service("testServiceSync")
public class TestServiceSync {

    @KoalasClient(zkPath = "127.0.0.1:2181",readTimeout = 5000*1000)
    TestCreateAccountService.Iface TestCreateAccountService;

    public void getRemoteRpc() throws TException {
        TestCreateAccountRequest request= new TestCreateAccountRequest (  );
        //request.setSource ( 10 );
        request.setAccountType ( 1 );
        request.setPartnerId ( 1 );
        request.setPartnerType ( 1 );
        request.setPartnerName ( "你好啊-我是注解实现的" );
        request.setPoiFlag ( 1 );
        TestCreateAccountRespone respone = TestCreateAccountService.getRPC (  request);
        System.out.println (respone);
     }

}
```
只需要在你想远程调用的类上加一个@KoalasClient注解就可以了，远程调用就这么简单，当然异步使用方式也类似

```
@Service("testServiceAsync")
public class TestServiceAsync {
    @KoalasClient(zkPath = "127.0.0.1:2181",readTimeout = 5000*1000)
    TestCreateAccountService.AsyncIface TestCreateAccountService;
    public void getRemoteRpc() throws TException{
        KoalasAsyncCallBack<TestCreateAccountRespone, TestCreateAccountService.AsyncClient.getRPC_call> koalasAsyncCallBack = new KoalasAsyncCallBack<> ();
        TestCreateAccountRequest request= new TestCreateAccountRequest (  );
        //request.setSource ( 10 );
        request.setAccountType ( 1 );
        request.setPartnerId ( 1 );
        request.setPartnerType ( 1 );
        request.setPartnerName ( "你好啊-我是注解实现的" );
        request.setPoiFlag ( 1 );
        TestCreateAccountService.getRPC ( request ,koalasAsyncCallBack);
        Future<TestCreateAccountRespone> future= koalasAsyncCallBack.getFuture ();
        try {
            System.out.println (future.get ());
        } catch (InterruptedException e) {
            e.printStackTrace ();
        } catch (ExecutionException e) {
            e.printStackTrace ();
        }
    }

}
```
注意和同步调用不同的是自定义注解注入的接口是xxxx.AsyncIface，同步是xxxx.Iface。KoalasAsyncCallBack回调使用方式和上面的xml一样。有一点需要说明

```
<koalas:annotation package="thrift.annotation.client.impl"/>
```
如果package属性设置为空，那么所有的@KoalasClient都会生效，也就是说所有在spring bean中的自定义注解@KoalasClient都会自动注入。这里说另外一种用法

```
private TestCreateAccountService.Iface TestCreateAccountService;

@KoalasClient(zkPath = "127.0.0.1:2181",readTimeout = 5000*1000)
public void setTestCreateAccountService(TestCreateAccountService.Iface TestCreateAccountService){
    this.TestCreateAccountService = TestCreateAccountService;
}
```
直接注入方法的方式也是可以的。

**2. 服务端实现**

xml中的配置

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:koalas="http://www.koalas.com/schema/ch"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
	   http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
	   http://www.koalas.com/schema/ch
	   http://www.koalas.com/schema/ch.xsd">
    <koalas:annotation package="thrift.annotation.server.impl"/>
</beans>
```
配置和client中一样只需要配置一个自定义标签即可，java中的使用方式如下：


```
package thrift.annotation.server.impl;

import annotation.KoalasServer;
import org.apache.thrift.TException;
import thrift.domain.TestCreateAccountRequest;
import thrift.domain.TestCreateAccountRespone;
import thrift.service.TestCreateAccountService;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@KoalasServer ( port = 8801,zkpath="127.0.0.1:2181")
public class TestCreateAccountServiceNettyImpl implements TestCreateAccountService.Iface {
    private AtomicInteger atomicInteger = new AtomicInteger ( 0 );
    @Override
    public TestCreateAccountRespone getRPC(TestCreateAccountRequest TestCreateAccountRequest) throws TException {
        TestCreateAccountRespone TestCreateAccountRespone = new TestCreateAccountRespone ();
        TestCreateAccountRespone.setCode ( 1 );
        TestCreateAccountRespone.setMessage ( "你好啊" );
        if(new Random (  ).nextInt ( 5 )>100){
            try {
                Thread.sleep ( 5000 );
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
        System.out.println ( "getRPC  start ...." + TestCreateAccountRequest + "------" + atomicInteger.incrementAndGet () );

        return TestCreateAccountRespone;
    }
}
```
这样服务实现就会主从注册到zookeeper中提供给client端使用了。值得说明的是被扫描到并且类上有@KoalasServer的类会被加载到spring上下文中，可以当成一个普通的spring bean来处理，还有一点如果你不指定package，配置成如下情况

```
 <koalas:annotation package=""/>
```
这样配置会以spring的bean为基础实现，那么使用方式需要改成

```
package thrift.annotation.server.impl;

import annotation.KoalasServer;
import org.apache.thrift.TException;
import thrift.domain.TestCreateAccountRequest;
import thrift.domain.TestCreateAccountRespone;
import thrift.service.TestCreateAccountService;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


@KoalasServer ( port = 8801,zkpath="127.0.0.1:2181")
@Service
public class TestCreateAccountServiceNettyImpl implements TestCreateAccountService.Iface {
    private AtomicInteger atomicInteger = new AtomicInteger ( 0 );
    @Override
    public TestCreateAccountRespone getRPC(TestCreateAccountRequest TestCreateAccountRequest) throws TException {
        TestCreateAccountRespone TestCreateAccountRespone = new TestCreateAccountRespone ();
        TestCreateAccountRespone.setCode ( 1 );
        TestCreateAccountRespone.setMessage ( "你好啊" );
        if(new Random (  ).nextInt ( 5 )>100){
            try {
                Thread.sleep ( 5000 );
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
        System.out.println ( "getRPC  start ...." + TestCreateAccountRequest + "------" + atomicInteger.incrementAndGet () );

        return TestCreateAccountRespone;
    }
}
```
就这么简单即可。

**3. 泛化调用**

为什么需要泛化调用？
1：有一个通用压测平台，想去压测不同的server。那么现在就有一个问题了，不可能让压测平台服务端去依赖所有的下游服务，这样依赖会很繁杂，这时候如果说只配置serviceName,request模型和request请求json就可以进行远程调用，那么将大大的减少头疼的依赖。
2：假设php同事对java代码不熟悉，不可能让他们去依赖spring，一共一套简单的api来使用是很有必要的。
3：上游服务不想依赖下游服务的数据模型。

对于泛化调用来说，dubbo已经提供，soft-rpc也有提供。当然koalas-rpc也不会例外，并且支持xml，注解和java api的使用方式。下面几个例子来说明一下使用方式。更多demo去源码中查看，作者已经写好，开箱即用。

xml使用方式

```
<koalas:client id="TestCreateAccountService3"
				   serviceInterface="thrift.service.TestCreateAccountService"
				   zkPath="127.0.0.1:2181"
				   generic="true"
				   readTimeout="50000000"/>
```

```
@Autowired
@Qualifier("TestCreateAccountService3")
GenericService.Iface TestGenericService;

public void getGenericRpc() throws TException {
        GenericRequest request = new GenericRequest (  );
        request.setMethodName ( "getRPC" );

        request.setClassType ( new ArrayList<String> (  ){{
            add ( "thrift.domain.TestCreateAccountRequest");
        }} );

        request.setRequestObj ( new ArrayList<String> (  ){{
            add ( "{\"accountType\":1,\"partnerId\":1,\"partnerName\":\"你好\",\"partnerType\":1,\"poiFlag\":1,\"source\":0}");
        }} );

        String str = TestGenericService.invoke ( request );
        System.out.println (str);
    }
```
简单说明一下，GenericService.Iface是通用服务，有三个参数，第一个是方法名称，第二个是请求体类型集合，第三个是请求体内容。直接调用即可，返回值是server端的json类型，使用json工具为阿里巴巴的Fast-json

注解使用方式
```
 @KoalasClient(zkPath = "127.0.0.1:2181",readTimeout = 5000*1000,genericService = "thrift.service.TestCreateAccountService")
 GenericService.Iface genericService;
 
 public void getGenericRemoteRpc() throws TException {
        GenericRequest request = new GenericRequest (  );
        request.setMethodName ( "getRPC" );

        request.setClassType ( new ArrayList<String> (  ){{
            add ( "thrift.domain.TestCreateAccountRequest");
        }} );

        request.setRequestObj ( new ArrayList<String> (  ){{
            add ( "{\"accountType\":1,\"partnerId\":1,\"partnerName\":\"你好\",\"partnerType\":1,\"poiFlag\":1,\"setAccountType\":true,\"setPartnerId\":true,\"setPartnerName\":true,\"setPartnerType\":true,\"setPoiFlag\":true,\"setSource\":false,\"source\":0}");
        }} );

        String str = genericService.invoke ( request );
        System.out.println (str);
    }
    
```
唯一区别的是注解要指定genericService，当genericService不为空时，默认开启泛化调用
当然，java api方式也是支持的。

```
        KoalasClientProxy koalasClientProxy = new KoalasClientProxy();
        koalasClientProxy.setServiceInterface ( "thrift.service.TestCreateAccountService" );
        koalasClientProxy.setZkPath ("127.0.0.1:2181"  );
        koalasClientProxy.setGeneric ( true );
        koalasClientProxy.setReadTimeout ( 50000000 );
        koalasClientProxy.afterPropertiesSet ();
        GenericService.Iface genericService = (GenericService.Iface) koalasClientProxy.getObject ();
        GenericRequest request = new GenericRequest (  );
        request.setMethodName ( "getRPC" );

        request.setClassType ( new ArrayList<String> (  ){{
            add ( "thrift.domain.TestCreateAccountRequest");
        }} );

        request.setRequestObj ( new ArrayList<String> (  ){{
            add ( "{\"accountType\":1,\"partnerId\":1,\"partnerName\":\"你好\",\"partnerType\":1,\"poiFlag\":1,\"setAccountType\":true,\"setPartnerId\":true,\"setPartnerName\":true,\"setPartnerType\":true,\"setPoiFlag\":true,\"setSource\":false,\"source\":0}");
        }} );

        String str = genericService.invoke ( request );
        System.out.println (str);
        koalasClientProxy.destroy ();
```

特别注意的是KoalasClientProxy对象非常非常重，一定要在服务关闭的时候执行koalasClientProxy.destroy ();方法，并且需要带应用程序中缓存该对象，千万不要每次使用都要创建，这样会极大的浪费资源，每个服务对应一个KoalasClientProxy，同步和异步也是不同的对象，这些使用者需要注意。

**4. 原生调用支持**

koalas-rpc在原生基础上封装了自定义协议和特定的传输类型，看过源码的朋友一定觉得处理非常非常麻烦，但是在自定义协议的过程中koalas-rpc也同时支持原生的thrift请求，可以在本地做测试等等。请求调用demo：

```
package xml.client;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import thrift.domain.TestCreateAccountRequest;
import thrift.domain.TestCreateAccountRespone;
import thrift.service.TestCreateAccountService;

public class ThriftNative {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 8001;
    public static final int TIMEOUT = 3000000;

    public static void main(String[] args) throws TException {
        TTransport transport = new TFramedTransport (new TSocket (SERVER_IP, SERVER_PORT, TIMEOUT));
        TProtocol protocol = new TBinaryProtocol (transport);
        TestCreateAccountService.Client client = new TestCreateAccountService.Client(protocol);
        transport.open();

        TestCreateAccountRequest request= new TestCreateAccountRequest (  );
        //request.setSource ( 10 );
        request.setAccountType ( 1 );
        request.setPartnerId ( 1 );
        request.setPartnerType ( 1 );
        request.setPartnerName ( "你好啊-我是ThriftNative实现的服务端getRemoteRpc" );
        request.setPoiFlag ( 1 );

        TestCreateAccountRespone respone=client.getRPC (request  );
        System.out.println (respone);

    }
}
```

# 三：参数配置文档

**1：客户端**

参数名 | 说明 | 是否必须 |
---|---|---
serviceInterface | thrift生成的接口类| Y
zkPath | zk的服务地址，集群中间逗号分隔| Y
serverIpPorts | 不实用zk发现直接连接服务器server，格式ip:端口#权重。多个逗号分隔 | N
async | 是否异步 | N，默认false同步
generic | 是否泛化调用（xml配置中使用） | N，默认false
genericService | 泛化调用的serviceName（注解配置中使用）使用方法参照代码中demo | N，默认false
cat | 是否开启CAT数据大盘，需要配置CAT服务，即可查看详细调用情况） | N，默认false
connTimeout | 连接超时 | N，默认3000ms
readTimeout | 读取超时 | N，默认5000ms，按照服务端指定时间适当调整
localMockServiceImpl | 本地测试的实现 | N
retryRequest | 是否错误重试 | N,默认true
retryTimes | 重试次数 | N，默认3次
maxTotal | TCP长连接池，参照Apache Pool参数 | 100
maxIdle | TCP长连接池，参照Apache Pool参数 | 50
minIdle | TCP长连接池，参照Apache Pool参数 | 10
lifo | TCP长连接池，参照Apache Pool参数 | true
fairness | TCP长连接池，参照Apache Pool参数 | false
maxWaitMillis | TCP长连接池，参照Apache Pool参数 |30 * 1000
timeBetweenEvictionRunsMillis | TCP长连接池，参照Apache Pool参数 | 3 * 60 * 1000
minEvictableIdleTimeMillis | TCP长连接池，参照Apache Pool参数 | 5 * 60 * 1000
softMinEvictableIdleTimeMillis| TCP长连接池，参照Apache Pool参数 | 10 * 60 * 1000
numTestsPerEvictionRun | TCP长连接池，参照Apache Pool参数 | 20
testOnCreate | TCP长连接池，参照Apache Pool参数 | false
testOnBorrow | TCP长连接池，参照Apache Pool参数 | false
testOnReturn | TCP长连接池，参照Apache Pool参数 | false
testWhileIdle | TCP长连接池，参照Apache Pool参数 | true
iLoadBalancer | 负载略侧，默认随机 | N
env | 环境 | N,默认dev
removeAbandonedOnBorrow | TCP长连接池，参照Apache Pool参数 | true
removeAbandonedOnMaintenance | TCP长连接池，参照Apache Pool参数 | true
removeAbandonedTimeout| TCP长连接池，参照Apache Pool参数 | 30000ms
maxLength_ | 允许发送最大字节数 | N,10 * 1024 * 1024
cores | selecter核心数量 | N，默认当前cpu数量 
asyncSelectorThreadCount | 异步请求时线程数量 | N，默认当前CPU核心数量*2
privateKey | 私钥 | N
publicKey | 公钥 | N

**2：服务端**

参数 | 说明| 是否必须
---|---|---
serviceImpl | 服务端实现| Y
serviceInterface | thrift自动生成的类| Y
port | 暴露的服务端口| Y
zkpath | 服务端的zk路径| Y
cat | （是否开启CAT数据大盘，需要配置CAT服务，即可查看详细调用情况） | N，默认false
bossThreadCount | 处理连接线程| N,当前CPU核心数
workThreadCount | 读取线程|N，当前CPU核心数*2
koalasThreadCount | 业务线程数| 256
maxLength | 最大接收字节数| Integer.MAX_VALUE
env | 环境| N,dev
weight| 权重| N，10
serverType | 采用哪些服务端，可以选NETTY和THRIFT，默认NETTY| N
workQueue | 当server超载时，可以容纳等待任务的队列长度| 0
privateKey | 私钥|N
publicKey | 公钥| N

##### 1：客户端服务端RSA双向加密
源码中utils.KoalasRsaUtil的main方法已经为大家写好生成私钥和公钥的代码，执行即可 ，下面为核心源码展示

```
public static String sign(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64.decodeBase64 ( privateKey.getBytes ( "UTF-8" ) );
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec ( keyBytes );
        KeyFactory keyFactory = KeyFactory.getInstance ( KEY_ALGORITHM );
        PrivateKey privateK = keyFactory.generatePrivate ( pkcs8KeySpec );
        Signature signature = Signature.getInstance ( SIGNATURE_ALGORITHM );
        signature.initSign ( privateK );
        signature.update ( data );
        return new String ( Base64.encodeBase64 ( signature.sign () ), "UTF-8" );
    }
```

```
public static boolean verify(byte[] data, String publicKey, String sign)
            throws Exception {
        byte[] keyBytes = Base64.decodeBase64 ( publicKey.getBytes ("UTF-8") );
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec ( keyBytes );
        KeyFactory keyFactory = KeyFactory.getInstance ( KEY_ALGORITHM );
        PublicKey publicK = keyFactory.generatePublic ( keySpec );
        Signature signature = Signature.getInstance ( SIGNATURE_ALGORITHM );
        signature.initVerify ( publicK );
        signature.update ( data );
        return signature.verify ( Base64.decodeBase64 ( sign.getBytes ("UTF-8") ) );
    }
```
执行main方法之后，会得到4个长长的字符串

```
下面四个字符串为koalas-rpc中客户端和服务端使用的rsa非对称秘钥，复制使用即可
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
上面四个字符串为koalas-rpc中客户端和服务端使用的rsa非对称秘钥，复制使用即可
```

得到上面的四个长长的字符串，可以由server端给client端提供。其中字符串1，字符串2分别对应client的privateKey，和publicKey，字符串3和字符串4分别对应server端的privateKey，和publicKey，提供rsa双向加密的初衷是为了将非常重要的项目保护起来，不允许其他项目随意调用，但是RSA双向加密会对性能有所影响。当RSA验证失败的时候，client会抛RsaException。RSA对称加密适合给三方系统进行调用,对称加密会影响传输性能。

# 实际性能压测
8C 16G mac开发本，单机10000次请求耗时截图
![输入图片说明](https://images.gitee.com/uploads/images/2018/1010/174547_9325018d_536094.png "屏幕截图.png")

10w次请求，大约耗时12s，平均qps在8000左右，在集群环境下会有不错的性能表现

# 数据大盘展示
开启数据大盘，需要设置客户端或者服务端的cat参数为true，默认为false。
koalas2.0已经接入了cat服务，cat服务支持qps统计，可用率，tp90line,tp99line,丰富自定义监控报警等，接入效果图
![输入图片说明](https://images.gitee.com/uploads/images/2019/0401/144340_c34bf1e0_536094.png "屏幕截图.png")
丰富的可视参数，流量统计，日，周，月报表展示等。

# 链路跟踪
对RPC服务来说，系统间的调用和排查异常接口，确定耗时代码是非常重要的，只要接入了cat，koalsa-rpc天然的支持链路跟踪，一切尽在眼前！
![输入图片说明](https://images.gitee.com/uploads/images/2019/0418/154348_b4b6df22_536094.png "屏幕截图.png")


# 代码下载后如何测试
作者在src/test/java和resource下面有已经写好了的丰富的xml配置和注解配置，下载后直接运行测试即可，注意测试的时候需要安装zookeeper服务,如果不想通过zk做服务发现，那么客户端可以进行直连，指定的server列表，逗号分隔，#分隔权重,格式，192.168.3.253:6666#10,192.168.3.253:6667#10
详情见参数配置列表，但是这种办法作者是不推荐的，在生产环境下没有心跳和动态上下线功能。

CAT服务按需配置，不需要数据大盘不需要配置，不会影响RPC功能，CAT接入参考：https://github.com/dianping/cat

Cat的client包在代码中有可能报错,可以把cat-client 3.0这个依赖包下载到本地 然后放到本地私服中，或者直接放到自己项目的classpath下面即可
cat-client下载地址 http://unidal.org/nexus/content/repositories/releases/com/dianping/cat/
或者下载cat代码 本地clean install一下即可 https://github.com/dianping/cat

#### 开源协议 :
Apache License Version 2.0 see http://www.apache.org/licenses/LICENSE-2.0.html

#### 联系作者 :
高级java QQ群：825199617
博客地址:https://www.cnblogs.com/zyl2016/
![输入图片说明](https://images.gitee.com/uploads/images/2019/0417/122134_0bd7dd55_536094.png "在这里输入图片标题")