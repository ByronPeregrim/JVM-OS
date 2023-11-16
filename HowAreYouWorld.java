public class HowAreYouWorld extends UserlandProcess {
    
    public HowAreYouWorld() {
        
    }
    
    public  void run() {
        while (true) {
            System.out.println("How are you world");
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) {
                System.err.println("HowAreYouWorld: run: Error while attempting to sleep.");
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

}