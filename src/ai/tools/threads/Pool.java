package ai.tools.threads;

public class Pool {




    private PoolThread[] threads;
    private int activeThreads;


    public Pool(int threads) {
        this.threads = new PoolThread[threads];
        PoolLock initialLock = new PoolLock(threads);
        for(int i = 0; i < threads; i++){
            this.threads[i] = new PoolThread(this, initialLock);
        }
        initialLock.lock();
        this.setActiveThreads(this.threads.length);
    }

    public boolean finished(){
        for(PoolThread t:threads){
            if(t.isExecuting()) return false;
        }
        return true;
    }

    public void execute(PoolFunction function, int range, boolean debug){
        int cores = Math.min(activeThreads,threads.length);
        PoolLock lock = new PoolLock(cores);
        for(int i = 0; i < cores; i++){
            int start = (int)((double)i / cores * range);
            int end = (int)((double)(i+1) / cores * range);

            this.threads[i].setDebug(debug);
            this.threads[i].execute(new PoolThreadRange(start, end), function, lock);
        }
        lock.lock();
    }

    public synchronized void printProgress() {
        System.out.print("\r");
        for(int i = 0; i < this.threads.length; i++){
            System.out.format("[%-2d: %-9s]  ",(i+1),
                    this.threads[i].isExecuting() ?
                    this.threads[i].getCurrentFunctionIndex():"FINISHED");
        }
    }

    public void stop() {
        for(Thread t:threads){
            t.interrupt();
        }
    }

    public void setActiveThreads(int limit){
        this.activeThreads = Math.min(limit, this.threads.length);
        this.activeThreads = limit;
    }

    public int getActiveThreads() {
        return activeThreads;
    }

    public int getAvailableThreads(){
        return this.threads.length;
    }

    public static int getAvailableProcessors(){
        return Runtime.getRuntime().availableProcessors();
    }

    public static void main(String[] args) throws InterruptedException {

        Pool pool = new Pool(16);
        PoolFunction function = index -> {
            long time = System.currentTimeMillis();

            int couter = 0;
            while(System.currentTimeMillis() - time < 4000){
                couter += 1;
            }
            System.out.println(couter);
        };

        pool.execute(function,32, false);
        System.out.println("FINISHED");
        pool.stop();
    }

}
