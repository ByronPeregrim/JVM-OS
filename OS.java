public class OS {

    private static Kernel kernel;

    public static enum Priority {
        REALTIME,
        INTERACTIVE,
        BACKGROUND
    }
    
    public static void Startup(UserlandProcess init) {
        kernel = new Kernel();
        CreateProcess(init);
    }

    public static void Startup(UserlandProcess init, Priority priority) {
        kernel = new Kernel();
        CreateProcess(init,priority);
    }

    public static void Startup(UserlandProcess init, Priority priority, boolean callSleep) {
        kernel = new Kernel();
        CreateProcess(init,priority,callSleep);
    }

    public static int CreateProcess(UserlandProcess up) {
        return kernel.CreateProcess(up, Priority.INTERACTIVE,false);
    }

    public static int CreateProcess(UserlandProcess up, Priority priority) {
        return kernel.CreateProcess(up,priority,false);
    }

    public static int CreateProcess(UserlandProcess up, Priority priority, boolean callSleep) {
        return kernel.CreateProcess(up,priority,callSleep);
    }

    public static void Sleep(int milliseconds) {
        kernel.Sleep(milliseconds);
    }

    public static int GetPID() {
        return kernel.GetPID();
    }

    public static int GetPIDByName(String s) {
        return kernel.GetPIDByName(s);
    }

    public static void SendMessage(KernelMessage km) {
        kernel.SendMessage(km);
    }

    public static KernelMessage WaitForMessage() {
        return kernel.WaitForMessage();
    }

    public static void UpdateTLB(int virtualPageNumber, int physicalPageNumber) {
        int rand_int = (int)Math.round(Math.random());
        UserlandProcess.TLB[rand_int][0] = virtualPageNumber;
        UserlandProcess.TLB[rand_int][1] = physicalPageNumber;
    }

    public static void GetMapping(int virtualPageNumber) {
        kernel.GetMapping(virtualPageNumber);
    }

    public static int AllocateMemory(int size) {
        if (size % 1024 == 0) {
            return kernel.AllocateMemory(size);
        }
        else {
            System.err.println("OS: AllocateMemory: Failed to allocate memory.");
            System.exit(0);
            return -1; // Failure
        }
    }

    public static boolean FreeMemory(int pointer, int size) {
        if (size % 1024 == 0 && pointer % 1024 == 0) {
            kernel.FreeMemory(pointer,size);
            return true;
        }
        else {
            return false; // Failure
        }
    }

    public static void FreeActivePages(int[] physicalPageArray) {
        kernel.FreeActivePages(physicalPageArray);
    }

    public static void ClearTLB() {
        for (int i = 0; i < UserlandProcess.TLB.length; i++) {
            UserlandProcess.TLB[i][0] = -1;
            UserlandProcess.TLB[i][1] = -1;
        }
    }

    public static void KillCurrentProcess() {
        kernel.KillCurrentProcess();
    }

    public static int PageSwap() {
        return kernel.PageSwap();
    }

    public static byte[] ReadFromMemory(int victimsPhysicalPage) {
        byte[] data = new byte[1024];
        int index = 0;
        for (int i = victimsPhysicalPage*1024; i < (victimsPhysicalPage*1024) + 1024; i++) {
            data[index] = UserlandProcess.memory[i];
            index++;
        }
        return data;
    }

    public static void WriteToMemory(int physicalPageNumber, byte[] data) {
        int index = 0;
        for (int i = physicalPageNumber*1024; i < (physicalPageNumber*1024) + 1024; i++) {
            UserlandProcess.memory[i] = data[index];
            index++;
        }
    }

    public static void WriteToDisk(int diskPageNumber, byte[] data) {
        kernel.WriteToDisk(diskPageNumber, data);
    }

    public static int WriteToDisk(byte[] data) {
        return kernel.WriteToDisk(data);
    }

    public static byte[] ReadFromDisk(int diskPageNumber) {
        return kernel.ReadFromDisk(diskPageNumber);
    }

    public static int GetFreePage() {
        return kernel.GetFreePage();
    }
    

    // Following variables are for testing purposes  
    private static int device_id0;
    private static int device_id1;
    private static int device_id2;
    private static int device_id3;
    private static int device_id4;

    // Following methods are for testing purposes
    public static void AddDevice0() {
        device_id0 = kernel.Open("random 100");
        byte[] array = kernel.Read(device_id0, 10);
        System.out.println("Device ID: " + device_id0);
        System.out.println("Device Type: Random");
        System.out.println("Read method printout:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }
        System.out.println();
    }

    public static void AddDevice1() {
        device_id1 = kernel.Open("file data.dat");
        byte[] array = kernel.Read(device_id1, 10);
        System.out.println("Device ID: " + device_id1);
        System.out.println("Device Type: File");
        System.out.println("Read method printout:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }
        System.out.println();
    }

    public static void AddDevice2() {
        device_id2 = kernel.Open("random 200");
        byte[] array = kernel.Read(device_id2, 10);
        System.out.println("Device ID: " + device_id2);
        System.out.println("Device Type: Random");
        System.out.println("Read method printout:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }
        System.out.println();
    }

    public static void AddDevice3() {
        device_id3 = kernel.Open("random 500");
        byte[] array = kernel.Read(device_id3, 15);
        System.out.println("Device ID: " + device_id3);
        System.out.println("Device Type: Random");
        System.out.println("Read method printout:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }
        System.out.println();
    }

    public static void AddDevice4() {
        device_id4 = kernel.Open("file data1.dat");
        byte[] array = kernel.Read(device_id4, 15);
        System.out.println("Device ID: " + device_id4);
        System.out.println("Device Type: File");
        System.out.println("Read method printout:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }
        System.out.println();
    }

    public static void CloseDevices() {
        kernel.Close(device_id0);
        kernel.Close(device_id1);
        kernel.Close(device_id2);
        kernel.Close(device_id3);
        kernel.Close(device_id4);
    }
    
}
