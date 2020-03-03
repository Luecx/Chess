package ai.tools.threads;

public class Pool {



    private PoolThread[] threads;
    private int activeThreads;

    private int _requestCounter = 0;        //only used for sequential
    private int _requestRange = 0;          //only used for sequential

    public Pool(int threads) {
        this.threads = new PoolThread[threads];
        PoolLock initialLock = new PoolLock(threads);
        for(int i = 0; i < threads; i++){
            this.threads[i] = new PoolThread(this, initialLock);
        }
        initialLock.lock();
        //initial lock is used to wait for the threads to be generated before calling anything else.
        this.setActiveThreads(this.threads.length);
    }

    public boolean finished(){
        for(PoolThread t:threads){
            if(t.isExecuting()) return false;
        }
        return true;
    }

    protected synchronized int requestTask(){
        return _requestCounter >= _requestRange ? -1:_requestCounter++;
    }

    public void executeSequential(PoolFunction function, int range, boolean debug){
        int cores = Math.min(activeThreads, threads.length);
        PoolLock lock = new PoolLock(cores);
        this._requestRange = range;
        this._requestCounter = 0;
        for (int i = 0; i < cores; i++) {
            this.threads[i].setSequential(true);
            this.threads[i].setDebug(debug);
            this.threads[i].execute(null, function, lock, i);
        }
        lock.lock();
    }

    public void executeTotal(PoolFunction function, int range, boolean debug) {
        int cores = Math.min(activeThreads, threads.length);
        PoolLock lock = new PoolLock(cores);
        for (int i = 0; i < cores; i++) {
            int start = (int) ((double) i / cores * range);
            int end = (int) ((double) (i + 1) / cores * range);
            this.threads[i].setSequential(false);
            this.threads[i].setDebug(debug);
            this.threads[i].execute(new PoolThreadRange(start, end), function, lock, i);
        }
        lock.lock();

    }

    public synchronized void printProgress() {
        System.out.print("\r");
        for(int i = 0; i < this.threads.length; i++){
            System.out.format("[%-2d: %-9s]  ",(i+1),
                    this.threads[i].isExecuting() && this.threads[i].getCurrentFunctionIndex()>=0 ?
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

        Pool pool = new Pool(4);
        PoolFunction function = new PoolFunction() {
            @Override
            public void execute(int index, int core) {
                long time = System.currentTimeMillis();
                int t = (int) (Math.random() * 100);
                int couter = 0;
                while (System.currentTimeMillis() - time < t) {
                    couter += 1;
                }
                //System.out.println(couter);
            }
        };

        pool.executeSequential(function, 128, true);
        System.out.println("FINISHED");
        pool.stop();
    }

}
