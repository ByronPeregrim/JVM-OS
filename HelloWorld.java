public class HelloWorld extends UserlandProcess {
    
    public HelloWorld() {
        
    }
    
    public  void run() {
        while (true) {
            //System.out.println("Hello world");
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) { }
        }
    }

}
