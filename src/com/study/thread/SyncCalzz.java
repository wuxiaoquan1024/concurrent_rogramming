package com.study.thread;


//class
//Last modified 2020-12-23; size 656 bytes
//        MD5 checksum c584b1c76e93ff6bae3a318344e7ba26
//        Compiled from "SyncCalzz.java"
//public class com.study.thread.SyncCalzz
//        minor version: 0
//        major version: 52
//        flags: ACC_PUBLIC, ACC_SUPER
//        Constant pool:
//        #1 = Methodref          #3.#23         // java/lang/Object."<init>":()V
//        #2 = Class              #24            // com/study/thread/SyncCalzz
//        #3 = Class              #25            // java/lang/Object
//        #4 = Utf8               <init>
//   #5 = Utf8               ()V
//           #6 = Utf8               Code
//           #7 = Utf8               LineNumberTable
//           #8 = Utf8               LocalVariableTable
//           #9 = Utf8               this
//           #10 = Utf8               Lcom/study/thread/SyncCalzz;
//           #11 = Utf8               main
//           #12 = Utf8               ([Ljava/lang/String;)V
//           #13 = Utf8               args
//           #14 = Utf8               [Ljava/lang/String;
//           #15 = Utf8               sychBlock
//           #16 = Utf8               StackMapTable
//           #17 = Class              #24            // com/study/thread/SyncCalzz
//           #18 = Class              #25            // java/lang/Object
//           #19 = Class              #26            // java/lang/Throwable
//           #20 = Utf8               synchMethod
//           #21 = Utf8               SourceFile
//           #22 = Utf8               SyncCalzz.java
//           #23 = NameAndType        #4:#5          // "<init>":()V
//           #24 = Utf8               com/study/thread/SyncCalzz
//           #25 = Utf8               java/lang/Object
//           #26 = Utf8               java/lang/Throwable
//           {
//public com.study.thread.SyncCalzz();
//        descriptor: ()V
//        flags: ACC_PUBLIC
//        Code:
//        stack=1, locals=1, args_size=1
//        0: aload_0
//        1: invokespecial #1                  // Method java/lang/Object."<init>":()V
//        4: return
//        LineNumberTable:
//        line 3: 0
//        LocalVariableTable:
//        Start  Length  Slot  Name   Signature
//        0       5     0  this   Lcom/study/thread/SyncCalzz;
//
//public static void main(java.lang.String[]);
//        descriptor: ([Ljava/lang/String;)V
//        flags: ACC_PUBLIC, ACC_STATIC
//        Code:
//        stack=0, locals=1, args_size=1
//        0: return
//        LineNumberTable:
//        line 7: 0
//        LocalVariableTable:
//        Start  Length  Slot  Name   Signature
//        0       1     0  args   [Ljava/lang/String;
//
//public void sychBlock();
//        descriptor: ()V
//        flags: ACC_PUBLIC
//        Code:
//        stack=2, locals=3, args_size=1
//        0: aload_0
//        1: dup
//        2: astore_1
//        3: monitorenter
//        4: aload_1
//        5: monitorexit
//        6: goto          14
//        9: astore_2
//        10: aload_1
//        11: monitorexit
//        12: aload_2
//        13: athrow
//        14: return
//        Exception table:
//        from    to  target type
//        4     6     9   any
//        9    12     9   any
//        LineNumberTable:
//        line 10: 0
//        line 12: 4
//        line 13: 14
//        LocalVariableTable:
//        Start  Length  Slot  Name   Signature
//        0      15     0  this   Lcom/study/thread/SyncCalzz;
//        StackMapTable: number_of_entries = 2
//        frame_type = 255 /* full_frame */
//        offset_delta = 9
//        locals = [ class com/study/thread/SyncCalzz, class java/lang/Object ]
//        stack = [ class java/lang/Throwable ]
//        frame_type = 250 /* chop */
//        offset_delta = 4
//
//public synchronized void synchMethod();
//        descriptor: ()V
//        flags: ACC_PUBLIC, ACC_SYNCHRONIZED
//        Code:
//        stack=0, locals=1, args_size=1
//        0: return
//        LineNumberTable:
//        line 17: 0
//        LocalVariableTable:
//        Start  Length  Slot  Name   Signature
//        0       1     0  this   Lcom/study/thread/SyncCalzz;
//        }
//        SourceFile: "SyncCalzz.java"

public class SyncCalzz {


    public static void main(String[] args) {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("abc");
        for (int i = 0; i < 2; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String s = threadLocal.get();
                    System.out.println("ThreadId:" + Thread.currentThread().getName() + "\t threadLocal value:" + s);
                }
            });
            thread.setName("thread-" + i);
            thread.start();
        }
        System.out.println("ThreadId:" + Thread.currentThread().getName() + "\t threadLocal value:" + threadLocal.get());


        // InheritableThreadLocal 的使用
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("efg");

        for (int i = 0; i < 2; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String s = itl.get();
                    System.out.println("ThreadId:" + Thread.currentThread().getName() + "\t ----before-----threadLocal value:" + s);
                    itl.set(Thread.currentThread().getName());
                    System.out.println("ThreadId:" + Thread.currentThread().getName() + "\t ----after-----threadLocal value:" + itl.get());

                }
            });
            thread.setName("thread-" + i);
            thread.start();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("ThreadId:" + Thread.currentThread().getName() + "\t threadLocal value:" + itl.get());
    }

    public void sychBlock() {
        synchronized (this) {
            System.out.println("");
        }
    }

    public synchronized void synchMethod() {

    }

}
