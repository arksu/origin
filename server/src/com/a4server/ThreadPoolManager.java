package com.a4server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by arksu on 03.01.2015.
 */
public class ThreadPoolManager
{
    private static final Logger _log = LoggerFactory.getLogger(ThreadPoolManager.class.getName());

    /**
     * основной пул потоков для обычных тасков
     */
    protected ScheduledThreadPoolExecutor _generalScheduledThreadPool;

    /**
     * temp workaround for VM issue
     */
    private static final long MAX_DELAY = Long.MAX_VALUE / 1000000 / 2;

    public static ThreadPoolManager getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final ThreadPoolManager _instance = new ThreadPoolManager();
    }

    protected ThreadPoolManager()
    {
        _generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_GENERAL, new PriorityThreadFactory("GeneralSTPool", Thread.NORM_PRIORITY));
    }

    /**
     * проверить задержку на выход за пределы
     *
     * @param delay
     * @return
     */
    public static long validateDelay(long delay)
    {
        if (delay < 0)
        {
            delay = 0;
        }
        else
        {
            if (delay > MAX_DELAY)
            {
                delay = MAX_DELAY;
            }
        }
        return delay;
    }

    /**
     * запустить задание через указанный промежуток времени
     *
     * @param r     задание
     * @param delay задержка перед выполнением
     * @return будущее
     */
    public ScheduledFuture<?> scheduleGeneral(Runnable r, long delay)
    {
        try
        {
            delay = ThreadPoolManager.validateDelay(delay);
            return _generalScheduledThreadPool.schedule(new RunnableWrapper(r), delay, TimeUnit.MILLISECONDS);
        }
        catch (RejectedExecutionException e)
        {
            return null; /* shutdown, ignore */
        }
    }

    /**
     * создание потоков с указанным приоритетом
     */
    private static class PriorityThreadFactory implements ThreadFactory
    {
        private final int _prio;
        private final String _name;
        private final AtomicInteger _threadNumber = new AtomicInteger(1);
        private final ThreadGroup _group;

        public PriorityThreadFactory(String name, int prio)
        {
            _prio = prio;
            _name = name;
            _group = new ThreadGroup(_name);
        }

        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(_group, r, _name + "-" + _threadNumber.getAndIncrement());
            t.setPriority(_prio);
            return t;
        }

        public ThreadGroup getGroup()
        {
            return _group;
        }
    }

    /**
     * враппер для выполнения заданий
     */
    private static final class RunnableWrapper implements Runnable
    {
        private final Runnable _r;

        public RunnableWrapper(final Runnable r)
        {
            _r = r;
        }

        @Override
        public final void run()
        {
            try
            {
                _r.run();
            }
            catch (final Throwable e)
            {
                final Thread t = Thread.currentThread();
                final Thread.UncaughtExceptionHandler h = t.getUncaughtExceptionHandler();
                if (h != null)
                {
                    h.uncaughtException(t, e);
                }
            }
        }
    }
}
