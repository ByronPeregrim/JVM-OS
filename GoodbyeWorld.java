public class GoodbyeWorld extends UserlandProcess {

    public GoodbyeWorld() {

    }
    
    public  void run() {
        while (true) {
            //System.out.println("Goodbye world");
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) { }
        }
    }
    
}
