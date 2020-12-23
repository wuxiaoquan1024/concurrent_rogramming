并发编程
一、CPU核心数、线程、CPU时间片轮转机制
    1.1 CPU核心数
        1.1.1 CPU核心数： 是逻辑上的，简单理解为逻辑上模拟出的核心数。
              多核CPU对于单核CPU的优势在于吞吐量跟大。

        1.1.2 CPU 线程数： 是同一时刻设备能并行执行的程序个数，线程数 = cpu个数 * 核数。
               充分利用CPU的算法利用率，提高CPU的效率

    1.2 线程
        操作系统执行任务的基本单位，生存必须依赖进程。

    1.3 CPU 时间片轮转机制
        操作系统调度程序将CPU的使用切成一段一段的小片（10ns~100ns）,并且以FIFO 队列维护所有请求CPU资源的线程。
     调度时， 将CPU使用分配给FIFO队列对头，对头线程开始执行任务。当前系统时间计数器发出终端请求时，线程将被Wait
     并且加入到FIFO对尾。调度程序继续将CPU使用分配给FIFO对头。


二、线程同步的几种方法与实现原理
	1、Synchronized
		1.1 使用方法
			1.1.1 同步块
			
				synchronized(lock) {
					<!--同步代码-->
				}
				同步块在线程访问时，只能锁住代码块中的共享元素。如果同时间内有多线程访问同步块，没有获得同步块锁的线程将被挂起，直到同步块执行完成被唤醒。
				线程的唤醒已FIFO
			
			1.1.2 同步方法
			
				public synchronized void doSomthing() {
					<!--方法代码-->
				}
				同步方法的作用域为整个方法的作用域。在线程获取同步方法锁后，其它线程将被挂起，并且加入到Wait Queue中。
			
			1.1.3 对象锁与类锁
				对象锁：以类实例对象作为锁，只对当前实例对象起作用。对于多实例访问同一方法的情况下不起作用
				类锁： 以类的字节码信息作为锁。由于Java的字节码信息保存在JVM中，而方法信息与静态修饰的方法与变量也保存在JVM中的方法区中。所以，类锁对于所有
					  线程来说是共享的。在多线程并发中，同一时间点只能有一个线程访问。
				
			
		1.2 实现原理
			
			1.2.1 Java 对象在内存中的组成部分
			    Java 对象在内存中的组成由对象头， 实例变量和对齐填充三部分。
			    实例变量：存放对象的相关属性信息，包括父类的属性信息。如果对象为数组，包括数组的长度。这部分有4个字节保存
			    对齐填充：由于虚拟记要求要求对象地址空间必须是8个字节的整数倍，因此填充数据可能不存在。对齐填充仅仅是为了满足虚拟机的规范要求
			    对象头：
			        MarkWorld:         保存对象的相关锁状态， hash地址， 分代年龄等相关信息
			        ClassMetaAddress：  类型元数据指针地址， JVM根据地址确定对象是那个类型的实例对象

			        MarkWorld 结构信息
			        |      锁状态     |     对象hash地址(25Bit)   |     分代年龄(4Bit)     |    是否偏向锁标识位(1Bit)    |      锁标识位(1Bit)      |

                    MarkWork在不同的状态下存放的数据信息
                    ---------------------------------------------------------------------------------------------------------------------------
                    |      锁状态     |     对象hash地址(25Bit)   |     分代年龄(4Bit)     |    是否偏向锁标识位(1Bit)    |      锁标识位(1Bit)      |
                    ---------------------------------------------------------------------------------------------------------------------------
                    |   无所状态      |      对象HashCode地址     |     分代年龄           |            0               |           01            |
                    ---------------------------------------------------------------------------------------------------------------------------
                    |   轻量锁        |                   指向锁记录的指针                 |            0               |           00            |
                    ---------------------------------------------------------------------------------------------------------------------------
                    |   重量锁        |                   指向重量锁的指针                 |            0               |           10            |
                    ----------------------------------------------------------------------------------------------------------------------------
                    |   GC标记        |                   空，不需要标记                  |            0               |           11            |
                    ----------------------------------------------------------------------------------------------------------------------------
                    |   偏向锁        |      线程ID/Epoch        |      对象分代年龄       |            1               |           01            |
                    ---------------------------------------------------------------------------------------------------------------------------
                    可以看到，不同的状态下MarkWork存放的信息不同，从而提高了JVM的空间利用效率


			1.2.2 Synchronized 同步的实现原理
			      Synchronized 同步实现已内存对象对象头的MarkWork信息，monitorenter和monitorexit 结合实现(不管是显示同步，还是隐式同步)。
			      而同步方法的方式与同步块在字节码的表现上确实不同的，同步块在字节码表现表现上会已monitorenter和monitoerexit指令声明进入锁和退出锁。同步方法确实在方法常量池中
                通过ACC_SYNCHRONIZED flags表示同步

                   根据1.2.1 中的介绍我们知道，MarkWork在锁状态为重量锁时，锁标识位的值为10， 对象的Hashcode地址会指向一个Monitor的对象。 Monitor 对象在JVM 中的最终实现是ObjectMonitor.
                对象的具体数据结构如下：
                    ObjectMonitor() {
                        _header       = NULL;
                        _count        = 0; // 记录个数
                        _waiters      = 0, // 等待锁的数量
                        _recursions   = 0;
                        _object       = NULL;
                        _owner        = NULL; // 线程指针
                        _WaitSet      = NULL; // 处于wait状态的线程，会被加入到_WaitSet
                        _WaitSetLock  = 0 ;
                        _Responsible  = NULL ;
                        _succ         = NULL ;
                        _cxq          = NULL ;
                        FreeNext      = NULL ;
                        _EntryList    = NULL ; // 处于等待锁block状态的线程，会被加入到该列表
                        _SpinFreq     = 0 ;
                        _SpinClock    = 0 ;
                        OwnerIsThread = 0 ;
                      }



				1.2.2.1 同步块
				    源码：
                        public void sychBlock() {
                            synchronized (this) {

                            }
                        }

                    字节码：
                    public void sychBlock();
                        descriptor: ()V
                        flags: ACC_PUBLIC
                        Code:123
                        123
                        123

                        stack=2, locals=3, args_size=1
                        0: aload_0
                        1: dup
                        2: astore_1
                        3: monitorenter     // 进入同步方法
                        4: aload_1
                        5: monitorexit      //退出同步方法
                        .
                        .
                        .
                    }
                    在字节码中可以看到同步代码块中加入了monitorenter与monitorexit进入退出同步块。
                    在多线程并发中，线程在访问同步块代码时JVM 会检测Monitor对象中的owner与count对象。如果owner对象为空，表示代码块没有线程访问，当前线程获得代码块的锁，并且
                    将线程的指针给monitor的owner， count = 1.
                    如果owner 的线程指针不为空，表示代码块已有线程获得锁，这时检测当前请求锁的线程与owner指向的线程相等。如果相等，monitor中的count+1.(synchronized 支持重入性)
                    如果与owner指向的线程不相等，当前线程进入Wait状态，并且加入到_WaitSet队列。

                    count在monitorenter每次进入时+1，在monitorexit每次-1.当count==0 的时候，会将owner置空。

				1.2.2.2 同步方法

			        源代码：
			        public synchronized void synchMethod() {

                    }

			        字节码：
			        public synchronized void synchMethod();
                        descriptor: ()V
                        flags: ACC_PUBLIC, ACC_SYNCHRONIZED   // 常量池中的ACC_SYNCHRONIZED声明成同步方法
                        Code:
                        stack=0, locals=1, args_size=1
                        0: return
                        LineNumberTable:
                        line 17: 0
                        LocalVariableTable:
                        Start  Length  Slot  Name   Signature
                        0       1     0  this   Lcom/study/thread/SyncCalzz;
                    }

                    同步方法是隐式的，无需通过JVM指令来表示进入与退出锁，已方法进入与方法返回表示整个锁状态。
                    在线程访问方法时，JVM从方法常量池中的方法表结构中检测ACC_SYNCHRONIZED标志。 ACC_SYNCHRONIZED访问标志被设置，执行线程将先持有monitor（虚拟机规范中用的是管程一词）
                    ， 然后再执行方法，最后在方法完成（无论是正常完成还是非正常完成）时释放monitor。在方法执行期间，执行线程持有了monitor，
                    其他任何线程都无法再获得同一个monitor。如果一个同步方法执行期间抛出了异常，并且在方法内部无法处理此异常，
                    那这个同步方法所持有的monitor将在异常抛到同步方法之外时自动释放

		1.3 优缺点
			1.3.1 优点
			    满足了同步原子性、 一致性、 有序性的三大特性要求。 已独占方式访问共享数据。
			
			1.3.2 缺点
			    由于是独占锁，多线程并发时存在线程调度上开销大，吞吐量底的问题。
			    因此在JDK1.6 对synchronized 进行了优化。根据不同的使用情况，JVM会自动对synchronized进行优化。

			    偏向锁：在线程并发时，大多数情况下，同步锁并不存在锁竞争，而且都只是一个现在在请求锁。因此，为了减少锁的申请。
			          偏向锁的核心思想，在一个线程获得时，会进入偏向锁，MarkWork进入偏向锁数据结构。当这个线程的其它方法或者操作再此对锁进行申请时，无需
			          再做任何的同步操作，从而提高性能。
			          所在没有竞争的情况下，偏向锁的性能是最高的。如果锁存在竞争，偏向锁将会失效，但是并不会立即进入重量锁，而是进入轻量锁。

			    轻量锁：在同步周期内，锁并不存在竞争关系，在偏向锁失败后，进入轻量锁。

			    轮训锁： 在线程申请锁失败是，JVM并不会立即将线程挂起，而是轮训N此检测在这N此内能否获得锁。 如果获得锁成功，线程则持有锁。失败，线程进入Wait状态

		
	2、Volatile
		2.1 Volatile解决了线程同步中的
		    一致性： 使用Volatile修饰的变量，在修改了变量后会立即将线程工作内存中的值写回到主内存中的共享数据。从而达到多线程并发共享数据的可见性
		    编译指令重拍： JVM在运行时会将指令错乱进行优化重排，从而造成执行顺序发生变化。 Volatile修饰的变量禁止编译重排

            在单例模式中的懒加载和双重检测方法中，给单例变量加volatile 关键字修饰，能更加完美。

	    2.2 synchronized与volatile的区别
	        2.2.1 修饰对象
	            synchronized 修饰方法，代码块
	            volatile 只能修饰修饰变量

	        2.2.2 是否阻塞
	            synchronized 锁被线程持有，再有线程请求锁，将会被阻塞
	            volatile 不会阻塞线程

	        2.2.3 编译优化
	            synchronized 会被编译器，CPU优化，进行指令重排
	            volatile  禁止编译器，CPU指令重排

	        2.2.4 可见性与原子性
	            synchronized 保证原子性与可见性。（通过阻塞线程达到目的）
	            volatile    保证可见性

	参考：详述 synchronized 和 volatile 的实现原理以及两者的区别：https://blog.csdn.net/qq_35246620/article/details/106311989
	
	3、Lock
		3.1 乐观锁与悲观锁
		
		3.2 公平锁与非公平锁
		
		3.3 独占锁与共享锁
		
		3.4 CAS 算法
			3.4.1 原理
			3.4.2 ABA问题

		3.5 AQS(Abstract Queued Sychronizer 抽象队列同步器)
			3.5.1 介绍
			3.5.2 实现原理



	
	参考： https://tech.meituan.com/2018/11/15/java-lock.html
		AQS 详解：https://www.cnblogs.com/chengxiao/p/7141160.html
		https://blog.csdn.net/qq_35190492/article/details/104691668
	
	4.ThreadLocal实现原理
		ThreadLocal 为线程提供本地的局部变量，每个线程都有自己的独立的变量副本。
		4.1 原理
		    4.1.1 理解原理前先了解一下ThreadLocal，ThreadLocalMap和Thread三者之间的关系
                ThreadLocal通过当前线程获取ThreadLocalMap
                Thread 中定义名称threadLocals的ThreadLocalMap变量
                ThreadLocalMap中已ThreadLocal为Key值保存Value

                ThreadLocal通过setter与getter保存Value。 在调用setter或getter时，获得当前线程的threadLocals变量，如果threadLocals变量为空，
              以当前ThreadLocal为key, value为空的值对齐赋值。 如果不为空，则通过ThreadLocal为Key获取值。

            4.1.2 使用场景
                a. 希望每个线程都有单独实例，并不会因为多线程并发而改变当前线程的实例对象
                b. 保存连接池，Session回话等

                ThreadLocal 一般都使用static 静态类型声明

        4.2 InheritableThreadLocal
            InheritableThreadLocal 在线程创建是共享父线程的ThreadLocal值。从而达到并发数据共享。
            多线程并发时，不同线程修改InheritableThreadLocal 中的值互不干扰，也不会将值写会父线程。

            4.2.2 InheritableThreadLocal 与ThreadLocal的区别
             InheritableThreadLocal 与ThreadLocal的作用一样，区别在于InheritableThreadLocal 会在线程创建是将父线程中的inheritableThreadLocals值Copy
            到当前线程。 所以，InheritableThreadLocal 在子线程中有默认值，而ThreadLocal 的默认值为空

		参考： https://droidyue.com/blog/2016/03/13/learning-threadlocal-in-java/



	5.JMM(Java Memory Model)
	    Java定义的一套内存规范，用于在不同的硬件与操作系统间读取内存不一致，已实现Java程序运行的一致性。


	参考：https://zhuanlan.zhihu.com/p/29881777
	
	
三、线程
	1、Runnable
	
	2、Thread
	
	3、Callable
	
	4.线程的生命周期
		
	
四、线程池使用与实现


参考：https://g.yuque.com/xiewenlin/develop-sikll/fyr5bp?language=zh-cn
    https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html

五、进程与线程的关系
    进程： 一个进程表示操作系统中一个可运行的程序，有独立的内存地址与系统资源。进程之间不能够直接访问必须的资源，可以通过IPC机制（Socket, PIP, FILE, SINGLE, AIDL）访问
    线程： 操作系统中执行任务的基本单位，必须依赖与进程存在。一个进程必须有至少一个或者多个线程。

六、线程协同
	
	1. CountDownLatch
	
	2. CyclicBarrier
	
	3. Semaphore
	
	4. Pharser
	
	5. Exchanger

七、实现wait/notify 相同功能的几种方式

    1.1 wait/notify

	1.2 ReentrantLock 的Condition
	
	1.3 LockSupport
	
	参考： 两条线程交叉输出实现的几种方式-> https://cloud.tencent.com/developer/article/1698570






















参考文献：


https://www.jianshu.com/p/7921c21f0664

