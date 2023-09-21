import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

public class Main {
    public static void main(String[] args) throws InvalidAlgorithmParameterException, IOException {
        OS.Startup(new HelloWorld(), OS.Priority.BACKGROUND);
        OS.AddDevice0();
        OS.AddDevice1();
        OS.CreateProcess(new GoodbyeWorld(), OS.Priority.REALTIME, false);
        OS.AddDevice2();
        OS.CreateProcess(new IAmGoingToSleep(), OS.Priority.REALTIME, true);
        OS.AddDevice3();
        OS.CreateProcess(new Ping());
        OS.AddDevice4();
        OS.CreateProcess(new Pong());
        OS.CloseDevices();
    }
}
