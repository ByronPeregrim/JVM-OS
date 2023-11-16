public class Pong extends UserlandProcess {

    public Pong() {

    }
    
    public void run() {
        while (true) {
            // Check message queue for message
            KernelMessage km = OS.WaitForMessage();
            // If message exists, read message.
            if (km != null) {
                KernelMessage newMessage = new KernelMessage(km);
                // Swap sender and target PID
                newMessage.SetSenderPID(km.GetTargetPID());
                newMessage.SetTargetPID(km.GetSenderPID());
                System.out.print("PONG ");
                newMessage.ToString();
                // Send new message back to sender
                OS.SendMessage(newMessage);
            }
            // Sleep for length of interrupt cycle, so process does not run more than once per cycle
            try {
                Thread.sleep(260); // sleep for 260 ms
            } catch (Exception e) {
                System.err.println("Pong: run: Error while attempting to sleep.");
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}