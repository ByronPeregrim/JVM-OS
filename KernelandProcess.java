public class KernelandProcess {

    private static int nextpid;
    private int pid;
    private boolean thread_started = false;
    private Thread thread;

    public KernelandProcess(UserlandProcess up) {
        thread = new Thread(up);
        pid = (int)thread.getId();
    }

    public void stop() {
        if (thread_started) {
            thread.suspend();
        }
    }

    public boolean isDone() {
        if (thread_started == true && !thread.isAlive()) {
            return true;
        }
        else {
            return false;
        }
    }

    public int getPid() {
        return pid;
    }

    public boolean isRunning() {
        return thread.isAlive();
    }

    public void run() {
        // If running for the first time, start thread, otherwise, resume suspended thread.
        if (thread_started) {
            thread.resume();
        }
        else {
            thread.start();
            thread_started = true;
        }
    }
}
