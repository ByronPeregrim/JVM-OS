import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;

public class KernelandProcess {

    private int PID;
    private boolean thread_started = false;
    private boolean thread_stopped = false;
    private Thread thread;
    private int wakeUpTime;
    private OS.Priority priority;
    private boolean callsSleep = false;
    private int[] VFS_ID_Array = new int[10];
    private Random rand = new Random();
    private String name;
    private LinkedList<KernelMessage> messageQueue = new LinkedList<KernelMessage>();
    private VirtualToPhysicalMapping[] virtualToPhysicalPageMap = new VirtualToPhysicalMapping[100]; // Index is virtual page number, value is physical page number

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
        if (virtualToPhysicalPageMap[virtualPageNumber] != null) {
            if (virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber == -1) {
                // If mapping does not have an associated physical page number, look for unused page in kernel
                int freePage = OS.GetFreePage();
                if (freePage != -1) {
                    virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber = freePage;
                }
                else { // If no unused pages, write random page to disk to free up space
                    int victimsOldPageNumber = OS.PageSwap();
                    virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber = victimsOldPageNumber;
                }
                // If mapping with new physical page has been previously written to disk, read in data from disk and rewrite to memory
                if (virtualToPhysicalPageMap[virtualPageNumber].diskPageNumber != -1) {
                    byte[] data = OS.ReadFromDisk(virtualToPhysicalPageMap[virtualPageNumber].diskPageNumber);
                    OS.WriteToMemory(virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber, data);
                }
                else {
                // If mapping hasn't been written to disk before, fill memory with zeros
                    byte[] data = new byte[1024];
                    OS.WriteToMemory(virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber, data);
                }
            }
            OS.UpdateTLB(virtualPageNumber, virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber);
        }
        else {
            System.err.println("KernelandProcess: GetMapping: Virtual page number returns null value.");
            System.exit(0);
        }
    }

    public int AllocateMemory(int[] physicalPagesArray) {
        int counter = 0;
        /* Looks for a gap in the virtualToPhysicalPageMap array large enough to store all of the pages
           in the input array in a continuous fashion, then, once found, stores those pages. */
        for (int i = 0; i < virtualToPhysicalPageMap.length; i++) {
            if (virtualToPhysicalPageMap[i] == null) {
                counter += 1;
            }
            else {
                counter = 0;
            }
            if (counter >= physicalPagesArray.length) {
                counter = 0;
                // When a large enough gap is found, pointer goes to beginning of gap and begins initializing VirtualMappings
                for (int j = i - physicalPagesArray.length + 1; j < i+1; j++) {
                    virtualToPhysicalPageMap[j] = new VirtualToPhysicalMapping();
                    virtualToPhysicalPageMap[j].physicalPageNumber = physicalPagesArray[counter++];
                }
                return i - physicalPagesArray.length + 1;
            }
        }
        return -1;
    }

    public int[] FreeMemory(int virtualPage, int numberOfPages) {
        int[] physicalPageArray = new int[numberOfPages];
        int index = 0;
        // Free virtual pages, create and return array of corresponding physical pages to be freed
        for (int i = virtualPage; i < virtualPage + numberOfPages; i++) {
            if (virtualToPhysicalPageMap[i].physicalPageNumber != -1) {
                physicalPageArray[index++] = virtualToPhysicalPageMap[i].physicalPageNumber;
            }

        }
        return physicalPageArray;
    }

    public int[] FreeAllPages() {
        VirtualToPhysicalMapping[] tempArray = new VirtualToPhysicalMapping[100];
        int index = 0;
        for (int i = 0; i < virtualToPhysicalPageMap.length; i++) {
            if (virtualToPhysicalPageMap[i] != null) {
                // Store physical pages numbers in a temp array
                tempArray[index] = virtualToPhysicalPageMap[i];
                index += 1;
                virtualToPhysicalPageMap[i] = null;
            }
        }
        // Creates appropriately sized array containing physical pages corresponding to freed virtual pages
        int[] physicalPageArray = new int[index];
        for (int i = 0; i < index; i++) {
            physicalPageArray[i] = tempArray[i].physicalPageNumber;
        }
        return physicalPageArray;
    }

    public void KillProcess() {
        thread_stopped = true;
    }

    public VirtualToPhysicalMapping LookForVictimMapping() {
        ArrayList<Integer> array = new ArrayList<>();
        // Adds virtualPageNumbers to array
        for (int i = 0; i < virtualToPhysicalPageMap.length; i++) {
            if (virtualToPhysicalPageMap[i] != null && virtualToPhysicalPageMap[i].physicalPageNumber != -1) {
                array.add(i);
            }
        }
        if (array.size() <= 0) {
            return null;
        }
        else {
            // Returns random virtualPageNumber from array
            return virtualToPhysicalPageMap[array.get(rand.nextInt((array.size())))];
        }
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
