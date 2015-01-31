package com.a4server.util.network;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by arksu on 03.01.2015.
 */
public final class AsyncPacketReader<T extends NetClient> implements Runnable
{
    private final BlockingQueue<T> _clientQueue;
    private final ExecutorService _threadPool;
    private int _threadPoolSize;

    public AsyncPacketReader(int threadPoolSize, String pool_name)
    {
        _threadPoolSize = threadPoolSize;
        _threadPool = Executors.newFixedThreadPool(threadPoolSize, new PktReaderThreadFactory(pool_name));
        _clientQueue = new LinkedBlockingQueue<>();

        initThreadPool();
    }

    class PktReaderThreadFactory implements ThreadFactory
    {
        private final String _name;
        private final AtomicInteger _threadNumber = new AtomicInteger(1);

        public PktReaderThreadFactory(String name)
        {
            _name = name;
        }

        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r, _name + "-" + _threadNumber.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }

    private void initThreadPool()
    {
        for (int i = 0; i < this._threadPoolSize; i++)
        {
            this._threadPool.execute(this);
        }
    }

    public void shutdown() throws InterruptedException
    {
        _threadPool.shutdown();
        if (!_threadPool.awaitTermination(60, TimeUnit.SECONDS))
        {
            _threadPool.shutdownNow(); // Cancel currently executing tasks
            // Wait a while for tasks to respond to being cancelled
            if (!_threadPool.awaitTermination(60, TimeUnit.SECONDS))
            {
                System.err.println("AsyncPacketReader thread pool did not terminate");
            }
        }
    }

    // добавление клиента в очередь на обработку
    public void addClientToProcess(T client)
    {
        if (client != null)
        {
            this._clientQueue.add(client);
        }
    }

    @Override
    public void run()
    {
        boolean isActive = true;
        while (isActive)
        {
            try
            {
                // Получаем следующего клиента для обработки
                T client = _clientQueue.take();

                // Здесь происходит обработка игровых сообщений
                client.ProcessPacket();
            }
            catch (InterruptedException e)
            {
                isActive = false;
            }

        }
    }


}
