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
    
    public int getMessage() {
        return message;
    }
    
    public void setTargetPID(int targetPID) {
        this.targetPID = targetPID;
    }
    
    public int getTargetPID() {
        return targetPID;
    }
   
    public void setSenderPID(int senderPID) {
        this.senderPID = senderPID;
    }
    
    public int getSenderPID() {
        return senderPID;
    }
    
    public void setMessage(int message) {
        this.message = message;
    }
   
    public void ToString() {
        System.out.print("from: " + senderPID);
        System.out.print(" to: " + targetPID);
        System.out.print(" what: " + message + "\n");
    }
}
    