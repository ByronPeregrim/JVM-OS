public class Main {
    public static void main(String[] args) {
        OS.Startup(new Piggy(0), OS.Priority.BACKGROUND);
        OS.AddDevice0();
        OS.CreateProcess(new HelloWorld(), OS.Priority.INTERACTIVE, false);
        OS.AddDevice1();
        OS.CreateProcess(new GoodbyeWorld(), OS.Priority.REALTIME, false);
        OS.AddDevice2();
        OS.CreateProcess(new IAmGoingToSleep(), OS.Priority.REALTIME, true);
        OS.AddDevice3();
        OS.CreateProcess(new Ping());
        OS.AddDevice4();
        OS.CreateProcess(new Pong());
        OS.CloseDevices();
        for (int i = 20; i > 0; i--) {
            OS.CreateProcess(new Piggy(i));
        }
    }
}
