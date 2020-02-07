package ai.tools.threads;

public class PoolLock {

    private int counter = 0;
    private boolean locked;

    private int threads;

    public PoolLock(int threads) {
        this.threads = threads;
    }

    public synchronized void lock()  {
        if(this.counter >= threads) return;
        synchronized (this){
            try {
                this.locked = true;
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getCounter() {
        return counter;
    }

    public int getThreads() {
        return threads;
    }

    public synchronized void unlock() {
        synchronized (this){
            counter++;
            if(this.counter >= this.threads){
                this.notify();
                this.locked = false;
            }
        }

    }

}
