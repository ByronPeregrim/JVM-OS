import java.util.Timer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TimerTask;
import java.time.Clock;
import java.util.Random;
import java.util.List;

public class Scheduler {

    private List<KernelandProcess> realtimeProcessList = Collections.synchronizedList(new LinkedList<KernelandProcess>());
    private List<KernelandProcess> interactiveProcessList = Collections.synchronizedList(new LinkedList<KernelandProcess>());
    private List<KernelandProcess> backgroundProcessList = Collections.synchronizedList(new LinkedList<KernelandProcess>());
    private List<KernelandProcess> sleepingProcessList = Collections.synchronizedList(new LinkedList<KernelandProcess>());
    private Timer timer = new Timer();
    private KernelandProcess currentProcess;
    private Clock clock = Clock.systemDefaultZone();
    private boolean processRunning = false;
    private Random rand = new Random();
    private int demotionCounter = 0;
    private Kernel kernel;

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

    public int CreateProcess(UserlandProcess up,OS.Priority priority, boolean callSleep) {
        // Create new process and add to end of kernel list.
        KernelandProcess newProcess = new KernelandProcess(up,priority,callSleep);
        switch (priority) {
            case REALTIME:
                realtimeProcessList.add(0,newProcess);
                for (int i = 0; i < realtimeProcessList.size(); i++) {
                    if (realtimeProcessList.get(i).isRunning()) {
                        processRunning = true;
                    }
                }
                break;
            case INTERACTIVE:
                interactiveProcessList.add(0,newProcess);
                for (int i = 0; i < interactiveProcessList.size(); i++) {
                    if (interactiveProcessList.get(i).isRunning()) {
                        processRunning = true;
                    }
                }
                break;
            case BACKGROUND:
                backgroundProcessList.add(0,newProcess);
                for (int i = 0; i < backgroundProcessList.size(); i++) {
                    if (backgroundProcessList.get(i).isRunning()) {
                        processRunning = true;
                    }
                }
                break;
        }
        // If nothing else is running, switch processes
        if (processRunning == false) {
            SwitchProcess();
        }
        return newProcess.getPid();
        
    }

    public void SwitchProcess() {
        Stop_Processes(backgroundProcessList);
        Stop_Processes(realtimeProcessList);
        Stop_Processes(interactiveProcessList);
        // Get first process from list and run it.
        Start_Next_Process();
    }

    public void Stop_Processes(List<KernelandProcess> processList) {
        // Suspend any running processes. If process isn't finished, add to end of list.
        for (int i = 0; i < processList.size(); i++) {
            if (processList.get(i).isRunning()) {
                processList.get(i).stop();
                if (!processList.get(i).isDone()) {
                    processList.add(processList.size(),processList.get(i));
                    processList.remove(i);
                }
                else {
                    int[] open_device_IDs = processList.get(i).get_VFS_ID_Array();
                    for (int j = 0; j < open_device_IDs.length; j++) {
                        if (open_device_IDs[j] != -1) {
                            kernel.Close(open_device_IDs[j]);
                        }
                    }
                }
            }
        }
    }

    public void Start_Next_Process() {
        Wake_Up_Sleeping_Processes();
        int rand_int;
        KernelandProcess nextProcess = null;
        // Use random number to determine which queue to pull next process from
        if (realtimeProcessList.size() > 0) {
            rand_int = rand.nextInt(10);
            if (rand_int <= 5) {
                // 60% chance to run realtime
                nextProcess = realtimeProcessList.get(0);
            }
            else if (rand_int > 5 && rand_int < 9) {
                // 30% chance to run interactive
                if (interactiveProcessList.size() > 0) {
                    nextProcess = interactiveProcessList.get(0);
                }
                else {
                    Start_Next_Process();
                }
            }
            else {
                // 10% chance to run background
                if (backgroundProcessList.size() > 0) {
                    nextProcess = backgroundProcessList.get(0);
                }
                else {
                    Start_Next_Process();
                }
            }
        }
        else if (interactiveProcessList.size() > 0) {
            rand_int = rand.nextInt(4);
            // 75% chance to run interactive
            if (rand_int > 0) {
                    nextProcess = interactiveProcessList.get(0);
                }
            else {
                // 25% chance to run background
                if (backgroundProcessList.size() > 0) {
                    nextProcess = backgroundProcessList.get(0);
                }
                else {
                    Start_Next_Process();
                }
            }
        }
        else {
            if (backgroundProcessList.size() > 0) {
                    nextProcess = backgroundProcessList.get(0);
                }
        }
        if (nextProcess != null) {
            Check_For_Demotion(nextProcess);
            Sleep_or_Run();
        }
        
    }

    public void Sleep_or_Run() { // Calls sleep if process was told, during initiation, to call sleep
        if (currentProcess.callsSleep()) {
            currentProcess.run();
            try {
                Thread.sleep(250); // sleep for 50 ms
            } catch (Exception e) { }
            Sleep(3000); // Sleeps for 3 seconds
        }
        else {
            currentProcess.run();
        }
    }

    public void Sleep(int milliseconds) {
        currentProcess.setWakeUpTime(milliseconds + (int)clock.millis());
        sleepingProcessList.add(currentProcess);
        removeFromProcessList(currentProcess);
        // Save copy of currently running process and stop it
        KernelandProcess tmp = currentProcess;
        currentProcess = null;
        tmp.stop();
        SwitchProcess();
    }

    public void removeFromProcessList(KernelandProcess inputCurrentProcess) {
        switch (inputCurrentProcess.getPriority()) {
            case REALTIME:
                realtimeProcessList.remove(0);
                break;
            case INTERACTIVE:
                interactiveProcessList.remove(0);
                break;
            case BACKGROUND:
                backgroundProcessList.remove(0);
                break;
        }
    }

    public void Check_For_Demotion(KernelandProcess nextProcess) {
        // If same process is called 5 times in a row, demote process priority.
        if (nextProcess == currentProcess) {
            demotionCounter += 1;
            if (demotionCounter >= 5) {
                removeFromProcessList(nextProcess);
                switch (nextProcess.getPriority()) {
                    case REALTIME:
                        nextProcess.setPriority(OS.Priority.INTERACTIVE);
                        interactiveProcessList.add(realtimeProcessList.size(), nextProcess);
                        System.out.println("~~~~Demoted REALTIME process to INTERACTIVE~~~~");
                        break;
                    case INTERACTIVE:
                        nextProcess.setPriority(OS.Priority.BACKGROUND);
                        backgroundProcessList.add(backgroundProcessList.size(), nextProcess);
                        System.out.println("~~~~Demoted INTERACTIVE process to BACKGROUND~~~~");
                        break;
                    case BACKGROUND:
                        break;
                }
                demotionCounter = 0;
            }
        }
        else {
            demotionCounter = 0;
        }
        currentProcess = nextProcess;
    }

    public void Wake_Up_Sleeping_Processes() {
        // Wake up processes that have exceeded the minimum wake up time.
        if (sleepingProcessList.size() > 0) {
            for (int i = 0; i < sleepingProcessList.size(); i++) {
                if ((int)clock.millis() >= sleepingProcessList.get(i).getWakeUpTime()) {
                    KernelandProcess sleepingProcess = sleepingProcessList.get(i);
                    sleepingProcessList.remove(i);
                    switch (sleepingProcess.getPriority()) {
                        case REALTIME:
                            realtimeProcessList.add(realtimeProcessList.size(), sleepingProcess);
                            break;
                        case INTERACTIVE:
                            interactiveProcessList.add(interactiveProcessList.size(), sleepingProcess);
                            break;
                        case BACKGROUND:
                            backgroundProcessList.add(backgroundProcessList.size(), sleepingProcess);
                            break;
                    }
                }
            }
        }
    }

    public KernelandProcess getCurrentlyRunning() {
        return currentProcess;
    }
}
