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
}
