public class HelloWorld extends UserlandProcess {
    
    public HelloWorld() {
        
    }
    
    public  void run() {
        int start_address = OS.AllocateMemory(1024);
        Write(start_address+1000, (byte)7);
        // Write(start_address+2000,(byte)9); // For testing writing memory that hasn't been allocated
        while (true) {
            System.out.println("Hello world. Read Result: " + Read(start_address+1000));
            try {
                Thread.sleep(100); // sleep for 50 ms
            } catch (Exception e) {
                System.err.println("HelloWorld: run: Error while attempting to sleep.");
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
