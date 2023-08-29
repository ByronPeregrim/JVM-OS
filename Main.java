public class Main {
    public static void main(String[] args) {
        OS.Startup(new HelloWorld(), OS.Priority.BACKGROUND);
        OS.CreateProcess(new HowAreYouWorld(), OS.Priority.INTERACTIVE);
        OS.CreateProcess(new GoodbyeWorld(), OS.Priority.REALTIME, false);
        OS.CreateProcess(new IAmGoingToSleep(), OS.Priority.REALTIME, true);
    }
}
