//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package top.maxim.im.common.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskDispatcher {
    private static final Handler sMainHandler;

    private static final Handler sThreadHandler;

    private static final Handler sIOHandler;

    private static final ThreadPoolExecutor sExecutor;

    private TaskDispatcher() {
        throw new RuntimeException("Can't new MgDispatcher");
    }

    public static void exec(Runnable task) {
        if (null != task) {
            sExecutor.execute(task);
        }

    }

    public static void execDelayed(final TaskDispatcher.DelayedExec task, long delayMillis) {
        if (null != task) {
            sThreadHandler.postDelayed(task.delayed = new Runnable() {
                public void run() {
                    TaskDispatcher.sExecutor.execute(task);
                }
            }, delayMillis);
        }

    }

    public static void removeExec(TaskDispatcher.DelayedExec task) {
        if (null != task) {
            sThreadHandler.removeCallbacks(task.delayed);
        }

    }

    public static Executor getExecutor() {
        return sExecutor;
    }

    public static Looper getMainLooper() {
        return sMainHandler.getLooper();
    }

    public static void postMain(Runnable task) {
        if (null != task) {
            sMainHandler.post(task);
        }

    }

    public static void postMainDelayed(Runnable task, long delayMillis) {
        if (null != task) {
            sMainHandler.postDelayed(task, delayMillis);
        }

    }

    public static void postMainAtFrontOfQueue(Runnable task) {
        if (null != task) {
            sMainHandler.postAtFrontOfQueue(task);
        }

    }

    public static void removeMain(Runnable task) {
        if (null != task) {
            sMainHandler.removeCallbacks(task);
        }

    }

    public static Looper getThreadLooper() {
        return sThreadHandler.getLooper();
    }

    public static void postThread(Runnable task) {
        if (null != task) {
            sThreadHandler.post(task);
        }

    }

    public static void postThreadDelayed(Runnable task, long delayMillis) {
        if (null != task) {
            sThreadHandler.postDelayed(task, delayMillis);
        }

    }

    public static void postThreadAtFrontOfQueue(Runnable task) {
        if (null != task) {
            sThreadHandler.postAtFrontOfQueue(task);
        }

    }

    public static void removeThread(Runnable task) {
        if (null != task) {
            sThreadHandler.removeCallbacks(task);
        }

    }

    public static Looper getIOThreadLooper() {
        return sIOHandler.getLooper();
    }

    public static void postIOThread(Runnable task) {
        if (null != task) {
            sIOHandler.post(task);
        }

    }

    public static void postIOThreadDelayed(Runnable task, long delayMillis) {
        if (null != task) {
            sIOHandler.postDelayed(task, delayMillis);
        }

    }

    public static void postIOThreadAtFrontOfQueue(Runnable task) {
        if (null != task) {
            sIOHandler.postAtFrontOfQueue(task);
        }

    }

    public static void removeIOThread(Runnable task) {
        if (null != task) {
            sIOHandler.removeCallbacks(task);
        }

    }

    static {
        int coreThreadCount = Runtime.getRuntime().availableProcessors();
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue();
        HandlerThread handlerThread = new HandlerThread("TaskDispatcher-Computation", 10);
        HandlerThread ioThread = new HandlerThread("TaskDispatcher-IO", 19);
        handlerThread.start();
        ioThread.start();
        sThreadHandler = new Handler(handlerThread.getLooper());
        sIOHandler = new Handler(ioThread.getLooper());
        sMainHandler = new Handler(Looper.getMainLooper());
        sExecutor = new ThreadPoolExecutor(coreThreadCount, coreThreadCount + 1, 60L,
                TimeUnit.SECONDS, workQueue, new TaskDispatcher.TaskDispatcherThreadFactory());
    }

    private static class TaskDispatcherThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup group;

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final String namePrefix;

        TaskDispatcherThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = "TaskDispatcher-Pool-" + poolNumber.getAndIncrement() + "-Thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.group, r,
                    this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }

            if (t.getPriority() != 5) {
                t.setPriority(5);
            }

            return t;
        }
    }

    public abstract static class DelayedExec implements Runnable {
        private Runnable delayed;

        public DelayedExec() {
        }
    }
}
