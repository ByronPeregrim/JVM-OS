public class HowAreYouWorld extends UserlandProcess {
    
    public HowAreYouWorld() {
        
    }
    
    public  void run() {
        while (true) {
            //System.out.println("How are you world");
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) { }
        }
    }

}