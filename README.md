以前在做后台服务开发的时候，SpringBoot每次改动代码都需要手动重启才能生效，感觉贼麻烦，后来使用Spring提供的一款热部署插件，它只是部分重启，相当于重新加载了我们自己写的代码，效率提高很多。后来遇到了Jrebel，它只重新加载我们修改的那个类，比Springboot热部署插件重启速度更快，连改mybatis的xml文件都能热部署，太方便了有不有！（顺便安利一下同一家公司的另一个软件XRebel，实时监控服务请求）后来又接触了BTrace，它可以线上调试代码而不需要重启项目，也是很吊的一个东西。通过了解，上面所说的几个东西都是通过Java Agent来实现的，那么Java Agent到底是啥，为啥这么吊？

简介

先说一下它的用途，在JDK1.5以后，我们可以使用agent技术构建一个独立于应用程序的代理程序（即为Agent），用来协助监测、运行甚至替换其他JVM上的程序。使用它可以实现虚拟机级别的AOP功能。

Agent实例

Agent分为两种，一种是在主程序之前运行的Agent，一种是在主程序之后运行的Agent（前者的升级版，1.6以后提供），这两种我们都会举个例子。

一、在主程序运行之前的代理程序

1、首先写一个agent程序


代码很简单，只有一个premain方法，顾名思义它代表着他将在主程序的main方法之前运行，agentArgs代表传递过来的参数，inst则是agent技术主要使用的API，我们可以使用它来改变和重新定义类的行为，这里我们简单的进行一下参数打印。

2、编写MANIFEST.MF文件

MANIFEST.MF文件用于描述Jar包的信息，例如指定入口函数等。我们需要在该文件中加入如下配置，指定我们编写的含有premain方法类的全路径，然后将agent类打成Jar包。


如果你是使用Maven来构建的项目，在构建的时候加入如下代码，否则Maven会生成自己的MANIFEST.MF覆盖掉你的。


3、编写我们的主程序

这里的程序就是我们要代理的程序，我们在主程序的VM options添加上启动参数

-javaagent: 你的路径/test-1.0-SNAPSHOT.jar=hah

其中hah为上文中传入permain方法的agentArgs参数。运行我们的主程序


可以看到，我们Jar包中premain方法中的的代码在主函数运行之前就已经成功运行了！

二、在主程序运行之后的代理程序

在主程序运行之前的agent模式有一些缺陷，例如需要在主程序运行前就指定javaagent参数，premain方法中代码出现异常会导致主程序启动失败等，为了解决这些问题，JDK1.6以后提供了在程序运行之后改变程序的能力。它的实现步骤和之前的模式类似

1、编写agent类

我们复用上面的类，将premain方法修改为agentmain方法，由于是在主程序运行后再执行，意味着我们可以获取主程序运行时的信息，这里我们打印出来主程序中加载的类名。


2、修改MANIFEST.MF文件

添加Agent-Class参数，打成Jar包


3、启动主程序，编写加载agent类的程序

在程序运行后加载，我们不可能在主程序中编写加载的代码，只能另写程序，那么另写程序如何与主程序进行通信？这里用到的机制就是attach机制，它可以将JVM A连接至JVM B，并发送指令给JVM B执行，JDK自带常用工具如jstack，jps等就是使用该机制来实现的。这里我们先用tomcat启动一个程序用作主程序B，再来写A程序代码


我们使用VirtualMachine attach到目标进程，其中78256为tomcat进程的PID，可以使用jps命令获得，也可以使用VirtualMachine.list方法获取本机上所有的Java进程，再来判断tomcat进程，loadAgent方法第一个参数为Jar包在本机中的路径，第二个参数为传入agentmain的args参数，此处为null，运行程序


然而什么都没有打印啊！是不是什么地方写错了呢？仔细想想就会发现，我们是将进程attach到了tomcat进程上，agent其实是在主程序B中运行的，所以程序A中自然就不会进行打印，我们跳回tomcat程序的控制台，查看结果。

由于是真实公司项目，项目名打码了
可以看到，agentmain方法中的代码已经在主程序中顺利运行了，并且打印出了程序中加载的类！

总结

以上就是Java Agent的俩个简单小栗子了，Java Agent十分强大，它能做到的不仅仅是打印几个监控数值而已，还包括使用Transformer（推荐观看）等高级功能进行类替换，方法修改等，要使用Instrumentation的相关API则需要对字节码等技术有较深的认识。

https://www.jianshu.com/p/b72f66da679f
https://www.jianshu.com/p/5065656a12ad
TProfiler：性能分析工具，代码比较简单，可以作为初步学习参考。
arthas：java问题诊断神器，功能强大丰富。
ASM官网：https://asm.ow2.io/