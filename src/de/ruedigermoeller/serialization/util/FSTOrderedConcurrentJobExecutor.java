package de.ruedigermoeller.serialization.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 17.12.12
 * Time: 23:27
 * To change this template use File | Settings | File Templates.
 */
public class FSTOrderedConcurrentJobExecutor {

    ExecutorService pool;
    Future futures[];
    AtomicInteger curIdx = new AtomicInteger(0), endIdx = new AtomicInteger(0);
    Semaphore lock;
    private int threads;

    public FSTOrderedConcurrentJobExecutor(int threads) {
        this.pool = Executors.newFixedThreadPool(threads);
        this.threads = threads;
        futures = new Future[threads];
        lock = new Semaphore(threads);
    }

    public int addCall(Callable toRun) {
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println(lock.availablePermits());
        int idx = curIdx.getAndIncrement() % threads;
        futures[idx] = pool.submit(toRun);
        return idx;
    }

    public Object getResult() throws ExecutionException {
        int idx = endIdx.getAndIncrement() % threads;
        Object res = null;
        try {
            res = futures[idx].get();
        } catch (InterruptedException e) {
            return null;
        }
        lock.release();
        return res;
    }

}
