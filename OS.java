public class OS {

    private static Kernel kernel;
    
    public static void Startup(UserlandProcess init) {
        kernel = new Kernel();
        CreateProcess(init);
    }

    public static int CreateProcess(UserlandProcess up) {
        return kernel.CreateProcess(up);
    }
}
