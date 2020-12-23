package com.study.thread;

import java.util.concurrent.*;

/**
 * 为什么要用线程池
 * 1.提供线程利用率，减少系统开销（新建线程，线程调度等相关开销）
 * 2.维护线程状态
 * 3.控制并发力度，减少同一时间系统资源调度
 */
public class ThreadPoolNormal {

    public static void main(String[] args) {

        ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(2, 4, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
        for (int i = 0; i < 14; i++) {

            /**
             * submit 提交的任务，小于线程次的coresize的直接运行。大于coresize 的任务，添加到workQueue中.
             * 如果workQueue 队列已满，且运行的线程数小于maxNum 数， 将新建线程执行任务。
             * 如果运行线程数 > maxNum， 执行reject
             *
             */
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Thread :" + Thread.currentThread().getName());
                }
            });
        }

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < noThreads; i++) {
            System.out.println("Thread No:" + i + " = " + lstThreads[i].getName());
        }

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         * 线程池在没有调用shutdown或者shutdownNow方法JVM虚拟机是不会退出的。因为在coresize 线程执行完成后，会调用workQueue.take中获取下一个任务，
         * take 方法是阻塞式，知道有新的元素才会返回。所有JVM无法退出
         *
         * 解决方法：
         *  一、 线程池设置超时时间
         *      mExecutor.setKeepAliveTime(20, TimeUnit.SECONDS);
         *      mExecutor.allowCoreThreadTimeOut(true);
         *
         *  二、 调用shutdown或者shutdownNow方法退出线程池
         *
         * 只有调用shutdown 或者shutdownNew才会让JVM 退出
         *
         */
        mExecutor.setKeepAliveTime(2, TimeUnit.SECONDS);
        mExecutor.allowCoreThreadTimeOut(true);

//        mExecutor.shutdown();

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Game Over");
        Future<?> submit = mExecutor.submit(() -> {
            System.out.println("Thread bab:" + Thread.currentThread().getName());
        });

        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "abc";
            }
        };


        /**
         * 使用Runnable 提交的任务，调用Future.get()方法返回null.
         * 使用Callable 提交的任务，调用Future.get()方法返回泛型的类型.
         */
        Future<String> fc = mExecutor.submit(callable);
        try {
            System.out.println("return result:" + fc.get() + "\t " + submit.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Callable<String> callableS = new Callable<String>() {
            @Override
            public String call() throws Exception {
                int count = 1;
                do {
                    System.out.println("callable:" + count);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                    count++;
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                }while (count < 5);
                return "abc";
            }
        };
        Future<String> fss = mExecutor.submit(callableS);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("callable cancel");

        /**
         * cancel 任务是配合式的. 在ThreadPool 中的实现是通过intercept 方式实现。如果没有配合intercept 方法的任务，在Future调用cancel方法后
         * 将回继续运行。只会影响到get方法的放回
         */
        fss.cancel(true);

        try {
            System.out.println("canceled return result:" + fss.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

}
