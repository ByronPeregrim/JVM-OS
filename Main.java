import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

public class Main {
    public static void main(String[] args) throws InvalidAlgorithmParameterException, IOException {
        OS.Startup(new HelloWorld(), OS.Priority.BACKGROUND);
        OS.CreateProcess(new HowAreYouWorld(), OS.Priority.INTERACTIVE);
        // AddDevice functions add devices to the current process and provide printouts for program testing.
        OS.AddDevice0();
        OS.AddDevice1();
        OS.CreateProcess(new GoodbyeWorld(), OS.Priority.REALTIME, false);
        OS.AddDevice2();
        OS.AddDevice3();
        OS.CreateProcess(new IAmGoingToSleep(), OS.Priority.REALTIME, true);
        try {
            Thread.sleep(3000); // sleep for 3s
        } catch (Exception e) { }
        OS.AddDevice4();
        OS.CloseDevices();
    }
}
