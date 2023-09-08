public class KernelandProcess {

    private static int nextpid;
    private int pid;
    private boolean thread_started = false;
    private Thread thread;
    private int wake_up_time;
    private OS.Priority priority;
    private boolean callsSleep = false;
    private int[] VFS_ID_Array = new int[10];

    public KernelandProcess(UserlandProcess up, OS.Priority input_priority, boolean callSleep) {
        thread = new Thread(up);
        pid = (int)thread.getId();
        priority = input_priority;
        callsSleep = callSleep;
        for (int i = 0; i < VFS_ID_Array.length; i++) {
            VFS_ID_Array[i] = -1;
        }
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

    public int getWakeUpTime() {
        return wake_up_time;
    }

    public void setWakeUpTime(int input_wake_up_time) {
        wake_up_time = input_wake_up_time;
    }

    public OS.Priority getPriority() {
        return priority;
    }

    public void setPriority(OS.Priority input_priority) {
        priority = input_priority;
    }

    public boolean callsSleep() {
        return callsSleep;
    }

    public int[] get_VFS_ID_Array() {
        return VFS_ID_Array;
    }

    public void set_VFS_ID_Array(int[] inputArray) {
        VFS_ID_Array = inputArray;
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
