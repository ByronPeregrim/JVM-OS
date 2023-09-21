import java.util.LinkedList;

public class KernelandProcess {

    private static int nextpid;
    private int PID;
    private boolean thread_started = false;
    private Thread thread;
    private int wakeUpTime;
    private OS.Priority priority;
    private boolean callsSleep = false;
    private int[] VFS_ID_Array = new int[10];
    private String name;
    private LinkedList<KernelMessage> messageQueue = new LinkedList<KernelMessage>();

    public KernelandProcess(UserlandProcess up, OS.Priority input_priority, boolean callSleep) {
        thread = new Thread(up);
        PID = (int)thread.getId();
        priority = input_priority;
        callsSleep = callSleep; //if true, Process calls sleep function
        for (int i = 0; i < VFS_ID_Array.length; i++) {
            VFS_ID_Array[i] = -1;
        }
        name = up.getClass().getSimpleName();
    }

    public void Stop() {
        if (thread_started) {
            thread.suspend();
        }
    }

    public boolean IsDone() {
        if (thread_started == true && !thread.isAlive()) {
            return true;
        }
        else {
            return false;
        }
    }

    public int GetPID() {
        return PID;
    }

    public String GetName() {
        return name;
    }

    public boolean IsRunning() {
        return thread.isAlive();
    }

    public int GetWakeUpTime() {
        return wakeUpTime;
    }

    public void SetWakeUpTime(int wakeUpTime) {
        this.wakeUpTime = wakeUpTime;
    }

    public OS.Priority GetPriority() {
        return priority;
    }

    public void SetPriority(OS.Priority priority) {
        this.priority = priority;
    }

    public boolean CallsSleep() {
        return callsSleep;
    }

    public int[] Get_VFS_ID_Array() {
        return VFS_ID_Array;
    }

    public void Set_VFS_ID_Array(int[] inputArray) {
        VFS_ID_Array = inputArray;
    }

    public void AddToMessageQueue(KernelMessage km) {
        messageQueue.addLast(km);
    }

    public KernelMessage PopMessageQueue() {
        if (!messageQueue.isEmpty()) {
            return messageQueue.pop();
        }
        else {
            return null;
        }
    }

    public void run() {
        // If running for the first time, start thread, otherwise, resume suspended thread.
        if (thread_started) {
            thread.resume();
        }
        else {
            thread_started = true;
            thread.start();
        }
    }
}
