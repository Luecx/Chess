package ai.tools.threads;

public class PoolThread extends Thread {

    private Object object = new Object();

    private PoolThreadRange counter;
    private PoolFunction function;

    private PoolLock lock;
    private PoolLock initialLock;

    private boolean executing = false;

    private Pool pool;
    private boolean debug = false;
    private int functionIndex;
    private boolean sequential = false;             //true if request data after finish
    private int threadIndex;

    public PoolThread(Pool pool, PoolLock initialLock) {
        this.initialLock = initialLock;
        this.pool = pool;
        this.start();
    }

    public boolean isExecuting() {
        return executing;
    }

    public void execute(PoolThreadRange counter, PoolFunction function, PoolLock lock, int threadIndex) {
        if(this.isExecuting()) throw new RuntimeException("Thread is already executing");
        this.lock = lock;
        this.threadIndex = threadIndex;
        this.counter = counter;
        this.function = function;
        synchronized (object) {
            object.notify();
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public int getCurrentFunctionIndex(){
        return functionIndex;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setSequential(boolean sequential){
        this.sequential = sequential;
    }

    @Override
    public void run() {
        synchronized (object) {
            this.initialLock.unlock();
            while (!this.isInterrupted()) {
                try {
                    object.wait();
                    this.executing = true;

                    if(this.sequential){
                        functionIndex = pool.requestTask();
                        while (functionIndex != -1){
                            if(debug){
                                pool.printProgress();
                            }
                            this.function.execute(functionIndex, threadIndex);
                            functionIndex = pool.requestTask();

                            if(debug){
                                pool.printProgress();
                            }
                        }

                        this.executing = false;
                        this.lock.unlock();

                    }
                    else{
                        for(int i = counter.start; i < counter.end; i++){

                            functionIndex = i;
                            if(debug){
                                pool.printProgress();
                            }

                            this.function.execute(i, threadIndex);

                            if(debug){
                                pool.printProgress();
                            }
                        }
                        this.executing = false;
                        this.lock.unlock();
                    }
                } catch (InterruptedException e) {
                    this.interrupt();
                }
            }
            this.interrupt();
        }

    }

}