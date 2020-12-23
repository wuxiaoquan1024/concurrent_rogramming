package com.study.thread;

import java.util.concurrent.TimeUnit;

public class ThreadNoamal {

	private static Object lock = new Object();

	private static final int COUNT_BITS = Integer.SIZE - 3;

	private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

	private static final int RUNNING    = -1 << COUNT_BITS;

	private static final int SHUTDOWN   =  0 << COUNT_BITS;
	// 001 00000000000000000000000000000
	private static final int STOP       =  1 << COUNT_BITS;
	// 010 00000000000000000000000000000
	private static final int TIDYING    =  2 << COUNT_BITS;
	// 011 00000000000000000000000000000
	private static final int TERMINATED =  3 << COUNT_BITS;

	public static void main(String[] args) {
//		testJoin();
//		testYield();
//		testSleep();
		testWait();

		System.out.println("Running:" + RUNNING);
		System.out.println("SHUTDOWN:" + SHUTDOWN);
		System.out.println("STOP:" + STOP);
		System.out.println("TIDYING:" + TIDYING);
		System.out.println("CAPACITY:" + CAPACITY);
		System.out.println("TERMINATED:" + TERMINATED);

	}

	/**
	 * join方法，等待当前线程执行完成再执行。
	 *
	 * 下面例子要求3条线程按照ABC顺序依次输出
	 */
	public static void testJoin() {
		Thread a = new Thread(() ->  {
			System.out.println("线程A执行完成");
		});
		
		Thread b = new Thread(() ->  {
			try {
				a.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("线程B执行完成");
		});  
		
		Thread c = new Thread(() ->  {
			try {
				b.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("线程C执行完成");
		});  
		
		a.start();
		b.start();
		c.start();
	}

	/**
	 * yield放弃当前cpu资源进入就绪状态，将它让给其他的任务占用cpu执行时间。但放弃的时间不确定。
	 * 由于线程进入就绪状态，有可能刚刚放弃，马上又获得cpu时间片。
	 */
	public static void testYield() {

		/**
		 * 可以看到不管是校长、还是小蜜在运行到20的时候都会让出当前时间片的占有重新进入就绪状态。
		 *
		 * 小蜜-----20
		 * 校长-----5
		 * 小蜜-----21
		 */
		Runnable runnable = () -> {
			for (int i = 0; i <= 100; i++) {
				System.out.println(Thread.currentThread().getName() + "-----" + i);
				if (i % 20 == 0) {
					Thread.yield();
				}
			}
		};
		new Thread(runnable, "校长").start();
		new Thread(runnable, "小蜜").start();
	}

	/**
	 * sleep 让线程进入暂停状态，直到时间完成或者被intercept
	 * 在暂停状态下线程并不会释放当前占有的CPU资源
	 */
	public static void testSleep() {
		new Thread(() -> {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("Thread Sleep 执行完成");
		}).start();

		//使用TimeUtil 枚举提供sleep方法
		//比直接调用Thread sleep 来说，可以节省睡眠时间的单位量
		new Thread(() -> {
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("TimeUtil Sleep 执行完成");
		}).start();
	}

	/**
	 * wait/notify, notifyAll 然当前线程进入Wait状态，并且释放锁与占用的CPU资源。 在使用是必须在给代码块加锁
	 */
	public static void testWait() {

		boolean done = false;

		Thread threadA = new Thread() {
			@Override
			public void run() {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				synchronized (lock) {
					try {
						System.out.println("线程A进入Wait状态");
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				System.out.println("线程A执行完成");
			}
		};

		Thread threadB = new Thread() {
			@Override
			public void run() {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("线程B唤醒线程");
				synchronized (lock) {
					lock.notify();
				}

				System.out.println("线程B执行完成");
			}
		};
		threadA.start();
		threadB.start();


	}
}
