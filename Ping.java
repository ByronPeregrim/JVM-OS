public class Ping extends UserlandProcess {

    private boolean initialized = false;

    public Ping() {
    }
    
    public  void run() {
        while (true) {
            if (initialized == false) {
                // Initial message
                initialized = true;
                int ping = OS.GetPID();
                int pong = OS.GetPIDByName("Pong");
                System.out.println("I am Ping, Ping = " + ping);
                System.out.println("I am Pong, Pong = " + pong);
                // Pack info into kernel message, print out message, and send to pong
                KernelMessage km = new KernelMessage(ping,pong,0);
                System.out.print("PING ");
                km.ToString();
                OS.SendMessage(km);
            }
            else {
                KernelMessage km = OS.WaitForMessage();
                if (km != null) {
                    KernelMessage copy = new KernelMessage(km);
                    // Increase message value by 1
                    copy.setMessage(km.getMessage()+1);
                    // Swap sender and target PID
                    copy.setSenderPID(km.getTargetPID());
                    copy.setTargetPID(km.getSenderPID());
                    System.out.print("PING ");
                    copy.ToString();
                    // Send new message back to sender
                    OS.SendMessage(copy);
                }
            }
            // Sleep for length of interrupt cycle, so process does not run more than once per cycle
            try {
                Thread.sleep(250); // sleep for 250 ms
            } catch (Exception e) { }
        }
    }
}
