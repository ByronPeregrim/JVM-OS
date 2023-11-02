import java.util.LinkedList;

public class KernelandProcess {

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
                int unusedPageNumber = OS.GetUnusedPage();
                // If GetUnusedPage does not return -1, use unused page
                if (unusedPageNumber != -1) {
                    virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber = unusedPageNumber;
                }
                // If -1, then all physical pages are in use, must use swap file
                else {
                    virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber = OS.SwapFile();
                }
                if (virtualToPhysicalPageMap[virtualPageNumber].diskPageNumber != -1) {
                    // Read Data from disk page
                    byte[] diskPageData = OS.ReadFromDisk(virtualToPhysicalPageMap[virtualPageNumber].diskPageNumber);
                    // Write data to physical page
                    OS.WriteToPhysicalPage(virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber, diskPageData);
                }
                else {
                    // Populate memory with 0's
                    byte[] diskPageData = new byte[1024];
                    OS.WriteToPhysicalPage(virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber, diskPageData);
                }
            }
            if (virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber != -1) {
                OS.UpdateTLB(virtualPageNumber, virtualToPhysicalPageMap[virtualPageNumber].physicalPageNumber);
            }
            else {
                System.err.println("Error: KernelandProcess: GetMapping: Attempted to update TLB with physical page number = -1");
            }
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
        // Free virtual pages, create and return array of corresponding physical pages to be freed
        for (int i = virtualPage; i < virtualPage + numberOfPages; i++) {
            physicalPageArray[i - virtualPage] = virtualToPhysicalPageMap[i].physicalPageNumber;
            virtualToPhysicalPageMap[i] = null;
            // NEED TO FREE DISCPAGENUMBER TOO???
        }
        // Counts the number of VirtualToPhysicalMappings with mapping to physical page
        int counter = 0;
        for (int i = 0; i < physicalPageArray.length; i++) {
            if (physicalPageArray[i] != -1)
                counter++; 
        }
        // Create appropriate sized array and add physical pages that aren't -1
        int[] trimmedPhysicalPageArray = new int[counter];
        int index = 0;
        for (int i = 0; i < physicalPageArray.length; i++) {
            if (physicalPageArray[i] != -1) {
                trimmedPhysicalPageArray[index] = physicalPageArray[i];
                index++;
            }
        }
        return trimmedPhysicalPageArray;
    }

    public int[] FreeAllPages() {
        int[] tempArray = new int[100];
        int index = 0;
        for (int i = 0; i < virtualToPhysicalPageMap.length; i++) {
            if (virtualToPhysicalPageMap[i] == null && virtualToPhysicalPageMap[i].physicalPageNumber != -1) {
                // Store physical pages numbers in a temp array
                tempArray[index] = virtualToPhysicalPageMap[i].physicalPageNumber;
                index += 1;
                virtualToPhysicalPageMap[i] = null;
                // NEED TO FREE DISCPAGENUMBER TOO???
            }
        }
        // Creates appropriately sized array containing physical pages corresponding to freed virtual pages
        int[] physicalPageArray = new int[index];
        for (int i = 0; i < index; i++) {
            physicalPageArray[i] = tempArray[i];
        }
        return physicalPageArray;
    }

    public void KillProcess() {
        thread_stopped = true;
    }

    public int SwapActivePage() {
        for (int i = 0; i < virtualToPhysicalPageMap.length; i++) {
            if (virtualToPhysicalPageMap[i] != null) {
                if (virtualToPhysicalPageMap[i].physicalPageNumber != -1) {
                    // Read victim page in 
                    byte[] writeToDiskData = OS.ReadPhysicalPage(virtualToPhysicalPageMap[i].physicalPageNumber);
                    // Write victim page to disk
                    virtualToPhysicalPageMap[i].diskPageNumber = OS.WriteToDisk(writeToDiskData, virtualToPhysicalPageMap[i].diskPageNumber);
                    int physicalPageNumber = virtualToPhysicalPageMap[i].physicalPageNumber;
                   virtualToPhysicalPageMap[i].physicalPageNumber = -1;
                   return physicalPageNumber;
                }
            }
        }
        return -1;
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
