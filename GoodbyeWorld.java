public class GoodbyeWorld extends UserlandProcess {

    public GoodbyeWorld() {

    }
    
    public  void run() {
        int start_address = OS.AllocateMemory(2048);
        Write(start_address+2000, (byte)3);
        Write(start_address+1000, (byte)'z');
        while (true) {
            System.out.println("Goodbye world. Read Result1: " + Read(start_address+2000) + " Read Result2: " + Read(start_address+1000));
            //System.out.println("Goodbye world. Read Result: " + Read(start_address+3000)); // Test reading from memory that has not been allocated
            //System.out.println("Goodbye world. Read Result: " + Read(30)); // Test reading from memory that has not been written to
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) {
                System.err.println("GoodbyeWorld: run: Error while attempting to sleep.");
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
