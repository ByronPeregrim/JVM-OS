public class KernelMessage {
    private int senderPID;
    private int targetPID;
    private int message;

    public KernelMessage() {
    
    }
    
    public KernelMessage(int senderPID, int targetPID, int message) {
        this.senderPID = senderPID;
        this.targetPID = targetPID;
        this.message = message;
    }
    // Copy constructor
    public KernelMessage(KernelMessage km) {
         this(km.senderPID,km.targetPID,km.message);
    }
    
    public int GetMessage() {
        return message;
    }

    public int GetSenderPID() {
        return senderPID;
    }

    public int GetTargetPID() {
        return targetPID;
    }

    public void SetMessage(int message) {
        this.message = message;
    }

    public void SetSenderPID(int senderPID) {
        this.senderPID = senderPID;
    }
    
    public void SetTargetPID(int targetPID) {
        this.targetPID = targetPID;
    }
   
    public void ToString() {
        System.out.print("from: " + senderPID);
        System.out.print(" to: " + targetPID);
        System.out.print(" what: " + message + "\n");
    }
}
    