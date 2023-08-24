import java.util.Timer;
import java.util.LinkedList;
import java.util.TimerTask;

public class Scheduler {

    private LinkedList<KernelandProcess> kernelList = new LinkedList<>();
    private Timer timer = new Timer();
    private KernelandProcess currentProcess;

    private class Interrupt extends TimerTask {
        public Interrupt() {
            
        }

        public void run() {
            SwitchProcess();
        }
    }

    public Scheduler() {
        // Set timer to switch processes every 250ms
        timer.schedule(new Interrupt(),250,250);
    }

    public int CreateProcess(UserlandProcess up) {
        // Create new process and add to end of kernel list.
        KernelandProcess newProcess = new KernelandProcess(up);
        kernelList.push(newProcess);
        boolean processRunning = false;
        // If nothing else is running, switch processes
        for (int i = 0; i < kernelList.size(); i++) {
            if (kernelList.get(i).isRunning()) {
                processRunning = true;
            }
        }
        if (processRunning == false) {
            SwitchProcess();
        }
        return newProcess.getPid();
        
    }

    public void SwitchProcess() {
        // Suspend any running processes. If process isn't finished, add to end of list.
        for (int i = 0; i < kernelList.size(); i++) {
                if (kernelList.get(i).isRunning()) {
                    kernelList.get(i).stop();
                    if (!kernelList.get(i).isDone()) {
                        kernelList.add(kernelList.size(),kernelList.get(i));
                        kernelList.remove(i);
                    }
                }
            }
        // Get first process from list and run it.
        KernelandProcess firstProcess = kernelList.get(0);
        firstProcess.run();
        currentProcess = firstProcess;
    }
}
