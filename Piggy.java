public class Piggy extends UserlandProcess {
    private int i;

    public Piggy(int i) {
        this.i = i;
    }
    
    public void run() {
        int start_address = OS.AllocateMemory(102400);
        Write(start_address+1000, (byte)i);
        while (true) {
            System.out.println("Piggy" + i + ": " + Read(start_address+1000));
            try {
                Thread.sleep(100); // sleep for 50 ms
            } catch (Exception e) {
                System.err.println("Piggy: run: Error while attempting to sleep.");
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
