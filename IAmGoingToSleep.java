public class IAmGoingToSleep extends UserlandProcess {
    
    public IAmGoingToSleep() {
        
    }
    
    public  void run() {
        while (true) {
            //System.out.println("I Am Going To Sleep");
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) { }
        }
    }

}
