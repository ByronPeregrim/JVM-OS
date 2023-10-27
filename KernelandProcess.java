import java.util.LinkedList;

public class KernelandProcess {

    private static int nextpid;
    private int PID;
    private boolean thread_started = false;
    private boolean thread_stopped = false;
    private Thread thread;
    private int wakeUpTime;
    private OS.Priority priority;
    private boolean callsSleep = false;
    private int[] VFS_ID_Array = new int[10];
    private String name;
    private LinkedList<KernelMessage> messageQueue = new LinkedList<KernelMessage>();
    private int[] virtualToPhysicalPageMap = new int[100]; // Index is virtual page number, value is physical page number

    public KernelandProcess(UserlandProcess up, OS.Priority input_priority, boolean callSleep) {
        thread = new Thread(up);
        PID = (int)thread.getId();
        priority = input_priority;
        callsSleep = callSleep; //if true, Process calls sleep function
        for (int i = 0; i < VFS_ID_Array.length; i++) {
            VFS_ID_Array[i] = -1;
        }
        name = up.getClass().getSimpleName();
        for (int i = 0; i < virtualToPhysicalPageMap.length; i++) {
            virtualToPhysicalPageMap[i] = -1;
        }
    }

    public void Stop() {
        if (thread_started) {
            thread.suspend();
        }
    }

    public boolean IsDone() {
        if (thread_started == true && thread_stopped == true) {
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

    public void GetMapping(int virtualPageNumber) {
        if (virtualToPhysicalPageMap[virtualPageNumber] != -1) {
            OS.UpdateTLB(virtualPageNumber, virtualToPhysicalPageMap[virtualPageNumber]);
        }
    }

    public int AllocateMemory(int[] physicalPagesArray) {
        int counter = 0;
        for (int i = 0; i < virtualToPhysicalPageMap.length; i++) {
            if (virtualToPhysicalPageMap[i] == -1) {
                counter += 1;
            }
            else {
                counter = 0;
            }
            if (counter >= physicalPagesArray.length) {
                counter = 0;
                for (int j = i - physicalPagesArray.length + 1; j < i+1; j++) {
                    virtualToPhysicalPageMap[j] = physicalPagesArray[counter++];
                }
                return i - physicalPagesArray.length + 1;
            }
        }
        return -1;
    }

    public int[] FreeMemory(int virtualPage, int numberOfPages) {
        int[] physicalPageArray = new int[numberOfPages];
        for (int i = virtualPage; i < virtualPage + numberOfPages; i++) {
            physicalPageArray[i - virtualPage] = virtualToPhysicalPageMap[i];
            virtualToPhysicalPageMap[i] = -1;
        }
        return physicalPageArray;
    }

    public int[] FreeAllPages() {
        int[] tempArray = new int[100];
        int index = 0;
        for (int i = 0; i < virtualToPhysicalPageMap.length; i++) {
            if (virtualToPhysicalPageMap[i] != -1) {
                tempArray[index] = virtualToPhysicalPageMap[i];
                index += 1;
                virtualToPhysicalPageMap[i] = -1;
            }
        }
        int[] physicalPageArray = new int[index];
        for (int i = 0; i < index; i++) {
            physicalPageArray[i] = tempArray[i];
        }
        return physicalPageArray;
    }

    public void KillProcess() {
        thread_stopped = true;
    }

    public void run() {
        if (thread_stopped == false) {
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
}
