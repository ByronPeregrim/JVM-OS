public class Kernel {

    private Scheduler scheduler;

    public Kernel() {
        scheduler = new Scheduler();
    }

    public int CreateProcess(UserlandProcess up) {
        return scheduler.CreateProcess(up);
    }
}
