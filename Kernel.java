import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

public class Kernel implements Device {

    private Scheduler scheduler;
    private VFS VFS = new VFS();
    private boolean[] activePhysicalPages = new boolean[1024];

    public Kernel() {
        scheduler = new Scheduler();
    }

    public int CreateProcess(UserlandProcess up, OS.Priority priority, boolean callSleep) {
        return scheduler.CreateProcess(up,priority,callSleep);
    }

    public void Sleep(int milliseconds) {
        scheduler.Sleep(milliseconds);
    }

    public int Open(String string) throws InvalidAlgorithmParameterException, IOException {
        KernelandProcess currentProcess = scheduler.getCurrentlyRunning();
        int[] VFS_ID_Array = currentProcess.Get_VFS_ID_Array();
        System.out.println(currentProcess + " " + currentProcess.GetPriority());
        for (int i = 0; i < VFS_ID_Array.length; i++) {
            if (VFS_ID_Array[i] == -1) {
                // If an empty spot is found, pass open call to VFS
                int VFS_ID = VFS.Open(string);
                // If VFS returns -1, fail
                if (VFS_ID == -1) {
                    return -1;
                }
                else {
                    VFS_ID_Array[i] = VFS_ID;
                    currentProcess.Set_VFS_ID_Array(VFS_ID_Array);
                    // Returns the location of the VFS_ID in the process' array
                    return i;
                }
            }
        }
        // If array is full, fail
        return -1;
    }

    public void Close(int id) {
        // Retrieve VFS_ID from Kerneland.
        KernelandProcess currentProcess = scheduler.getCurrentlyRunning();
        int[] VFS_ID_Array = currentProcess.Get_VFS_ID_Array();
        int VFS_ID = VFS_ID_Array[id];
        // Pass Kernel call through to VFS
        VFS.Close(VFS_ID);
        VFS_ID_Array[id] = -1;
        currentProcess.Set_VFS_ID_Array(VFS_ID_Array);
    }

    public byte[] Read(int id, int size) throws IOException {
        KernelandProcess currentProcess = scheduler.getCurrentlyRunning();
        int[] VFS_ID_Array = currentProcess.Get_VFS_ID_Array();
        int VFS_ID = VFS_ID_Array[id];
        return VFS.Read(VFS_ID, size);
    }

    public int Write(int id, byte[] data) {
        KernelandProcess currentProcess = scheduler.getCurrentlyRunning();
        int[] VFS_ID_Array = currentProcess.Get_VFS_ID_Array();
        int VFS_ID = VFS_ID_Array[id];
        return VFS.Write(VFS_ID, data);
    }

    public void Seek(int id, int to) throws IOException {
        KernelandProcess currentProcess = scheduler.getCurrentlyRunning();
        int[] VFS_ID_Array = currentProcess.Get_VFS_ID_Array();
        int VFS_ID = VFS_ID_Array[id];
        VFS.Seek(VFS_ID, to);
    }

    public int GetPID() {
        return scheduler.GetPID();
    }

    public int GetPIDByName(String s) {
        return scheduler.GetPIDByName(s);
    }

    public void SendMessage(KernelMessage km) {
        KernelMessage copy = new KernelMessage(km);
        // Ensure that SenderPID is the PID of current process.
        copy.setSenderPID(GetPID());
        KernelandProcess target = scheduler.GetProcessByPID(copy.getTargetPID());
        if (target == null) {
            System.out.println("ERROR: Message target not found.");
        }
        else {
            target.AddToMessageQueue(copy);
            scheduler.CheckIfWaitingAndRestore(target);
        }
    }

    public KernelMessage WaitForMessage() {
        KernelandProcess currentProcess = scheduler.getCurrentlyRunning();
        // Retrieve first message in queue, if it exists
        KernelMessage message = currentProcess.PopMessageQueue();
        if (message != null) {
            return message;
        }
        // Otherwise, switch status to waiting
        else {
            scheduler.RemoveFromProcessList(currentProcess);
            scheduler.AddToWaitingProcesses(currentProcess);
            return null;
        }
    }

    public void GetMapping(int virtualPageNumber) {
        scheduler.GetMapping(virtualPageNumber);
    }

    public int AllocateMemory(int size) {
        int num_of_pages = size / 1024;
        int[] physicalPageArray = new int[num_of_pages];
        int index = 0;
        for (int i = 0; i < activePhysicalPages.length; i++) {
            if (activePhysicalPages[i] == false) {
                physicalPageArray[index] = i;
                index += 1;
                activePhysicalPages[i] = true;
            }
            if (index >= num_of_pages) {
                break;
            }
        }
        int virtualPageNumber = scheduler.AllocateMemory(physicalPageArray);
        return virtualPageNumber * 1024;
    }

    public void FreeMemory(int pointer, int size) {
        int virtualPageNumber = pointer / 1024;
        int num_of_pages = size / 1024;
        int[] physicalPageArray = scheduler.FreeMemory(virtualPageNumber, num_of_pages);
        for (int i = 0; i < physicalPageArray.length; i++) {
            activePhysicalPages[physicalPageArray[i]] = false;
        }
    }

    public void FreeActivePages(int[] physicalPageArray) {
        for (int i = 0; i < physicalPageArray.length; i++) {
            activePhysicalPages[physicalPageArray[i]] = false;
        }
    }

    public void KillCurrentProcess() {
        scheduler.KillCurrentProcess();
    }
}
