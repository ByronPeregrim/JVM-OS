public class Kernel {

    private Scheduler scheduler;

    public Kernel() {
        scheduler = new Scheduler();
    }

    public int CreateProcess(UserlandProcess up, OS.Priority priority, boolean callSleep) {
        return scheduler.CreateProcess(up,priority,callSleep);
    }

    public void Sleep(int milliseconds) {
        scheduler.Sleep(milliseconds);
    }
}
