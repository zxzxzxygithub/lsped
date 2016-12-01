package co.allconnected.libspeedtest.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by allconnected on 3/4/16.
 */
public class TaskManager {

    private final ExecutorService pools;
    private static TaskManager instance;

    private TaskManager() {
        this.pools = new ThreadPoolExecutor(10, 10, 30000L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue());
    }

    public static synchronized TaskManager getInstance() {
        if(instance == null) {
            instance = new TaskManager();
        }

        return instance;
    }

    public void submit(Runnable r) {
        this.pools.execute(r);
    }

    public void shutDown() {
        this.pools.shutdown();
    }
}
