import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

public class OS {

    private static Kernel kernel;
    
    // Following variables are for testing purposes  
    private static int device_id0;
    private static int device_id1;
    private static int device_id2;
    private static int device_id3;
    private static int device_id4;
    

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

    // Following methods are for testing purposes
    public static void AddDevice0() throws InvalidAlgorithmParameterException, IOException {
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

    public static void AddDevice1() throws InvalidAlgorithmParameterException, IOException {
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

    public static void AddDevice2() throws InvalidAlgorithmParameterException, IOException {
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

    public static void AddDevice3() throws InvalidAlgorithmParameterException, IOException {
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

    public static void AddDevice4() throws InvalidAlgorithmParameterException, IOException {
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
