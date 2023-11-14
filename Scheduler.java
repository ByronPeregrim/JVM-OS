import java.util.Timer;
import java.util.Collections;
import java.util.HashMap;
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
    private HashMap<Integer,KernelandProcess> waitingProcesses = new HashMap<Integer,KernelandProcess>();
    private Timer timer = new Timer();
    private KernelandProcess currentProcess;
    private Clock clock = Clock.systemDefaultZone();
    private boolean processRunning = false;
    private Random rand = new Random();
    private int demotionCounter = 0;
    private Kernel kernel;
    private HashMap<Integer,KernelandProcess> PIDToProcessMap = new HashMap<Integer,KernelandProcess>();
    private HashMap<String,Integer> nameToPIDMap = new HashMap<String,Integer>();

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
        // Create new process and add to end of process list corresponding to process priority.
        KernelandProcess newProcess = new KernelandProcess(up,priority,callSleep);
        PIDToProcessMap.put(newProcess.GetPID(), newProcess);
        nameToPIDMap.put(newProcess.GetName(),newProcess.GetPID());
        switch (priority) {
            case REALTIME:
                realtimeProcessList.add(0,newProcess);
                break;
            case INTERACTIVE:
                interactiveProcessList.add(0,newProcess);
                break;
            case BACKGROUND:
                backgroundProcessList.add(0,newProcess);
                break;
        }
        // Check if any processes are running
        for (int i = 0; i < realtimeProcessList.size(); i++) {
            if (realtimeProcessList.get(i).IsRunning()) {
                processRunning = true;
            }
        }
        for (int i = 0; i < interactiveProcessList.size(); i++) {
            if (interactiveProcessList.get(i).IsRunning()) {
                processRunning = true;
            }
        }
        for (int i = 0; i < backgroundProcessList.size(); i++) {
            if (backgroundProcessList.get(i).IsRunning()) {
                processRunning = true;
            }
        }
        // If nothing else is running, switch processes
        if (processRunning == false) {
            SwitchProcess();
        }
        return newProcess.GetPID();
    }

    public void SwitchProcess() {
        // Stop any running processes
        StopProcess();
        StopWaitingProcesses();
        WakeUpSleepingProcesses();
        OS.ClearTLB();
        // Next process determined by priority queue
        KernelandProcess nextProcess = GetNextProcess();
        if (nextProcess != null) {
            CheckForDemotion(nextProcess);
            CheckForSleepAndRun(nextProcess);
        }
    }

    public void StopProcess() {
        // Suspend currently running process. If process isn't finished, add to end of process list.
        if (currentProcess != null && currentProcess.IsRunning()) {
            currentProcess.Stop();
            if (!currentProcess.IsDone()) {
                RemoveFromProcessList(currentProcess);
                switch (currentProcess.GetPriority()) {
                    case REALTIME:
                        realtimeProcessList.add(realtimeProcessList.size(),currentProcess);
                        break;
                    case BACKGROUND:
                        backgroundProcessList.add(backgroundProcessList.size(),currentProcess);
                        break;
                    case INTERACTIVE:
                        interactiveProcessList.add(interactiveProcessList.size(),currentProcess);
                        break;
                }
            }
            else {
                // If process is done, close all devices and remove from maps and process list
                int[] open_device_IDs = currentProcess.Get_VFS_ID_Array();
                for (int j = 0; j < open_device_IDs.length; j++) {
                    if (open_device_IDs[j] != -1) {
                        kernel.Close(open_device_IDs[j]);
                    }
                }
                PIDToProcessMap.remove(currentProcess.GetPID());
                nameToPIDMap.remove(currentProcess.GetName());
                RemoveFromProcessList(currentProcess);
            }
        }
    }

    public void StopWaitingProcesses() {
        waitingProcesses.forEach((k,v) -> {
            if (v.IsRunning()) {
                v.Stop();
            }
        });
    }

    public KernelandProcess GetNextProcess() {
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
                    nextProcess = GetNextProcess();
                }
            }
            else {
                // 10% chance to run background
                if (backgroundProcessList.size() > 0) {
                    nextProcess = backgroundProcessList.get(0);
                }
                else {
                    nextProcess = GetNextProcess();
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
                    nextProcess = GetNextProcess();
                }
            }
        }
        else {
            if (backgroundProcessList.size() > 0) {
                nextProcess = backgroundProcessList.get(0);
            }
            else {
                nextProcess = GetNextProcess();
            }
        }
        return nextProcess;
    }

    public void CheckForSleepAndRun(KernelandProcess nextProcess) { // Calls sleep if process was told, during initiation, to call sleep
        currentProcess = nextProcess;
        if (nextProcess.CallsSleep()) {
            nextProcess.run();
            currentProcess = nextProcess;
            try {
                Thread.sleep(250); // sleep for 250 ms
            } catch (Exception e) { }
            Sleep(3000); // Sleeps for 3 seconds
        }
        else {
            nextProcess.run();
            currentProcess = nextProcess;
        }
    }

    public void Sleep(int milliseconds) {
        currentProcess.SetWakeUpTime(milliseconds + (int)clock.millis());
        sleepingProcessList.add(currentProcess);
        RemoveFromProcessList(currentProcess);
        // Save copy of currently running process and stop it
        KernelandProcess tmp = currentProcess;
        currentProcess = null;
        tmp.Stop();
        SwitchProcess();
    }

    public void RemoveFromProcessList(KernelandProcess inputCurrentProcess) {
        switch (inputCurrentProcess.GetPriority()) {
            case REALTIME:
                for (int i = 0; i < realtimeProcessList.size(); i++) {
                    if (realtimeProcessList.get(i).GetPID() == inputCurrentProcess.GetPID()) {
                        realtimeProcessList.remove(i);
                        break;
                    }
                }
                break;
            case INTERACTIVE:
                for (int i = 0; i < interactiveProcessList.size(); i++) {
                    if (interactiveProcessList.get(i).GetPID() == inputCurrentProcess.GetPID()) {
                        interactiveProcessList.remove(i);
                        break;
                    }
                }
                break;
            case BACKGROUND:
                for (int i = 0; i < backgroundProcessList.size(); i++) {
                    if (backgroundProcessList.get(i).GetPID() == inputCurrentProcess.GetPID()) {
                        backgroundProcessList.remove(i);
                        break;
                    }
                }
                break;
        }
    }

    public void CheckForDemotion(KernelandProcess nextProcess) {
        // If same process is called 5 times in a row, demote process priority.
        if (nextProcess == currentProcess) {
            demotionCounter += 1;
            if (demotionCounter >= 5) {
                RemoveFromProcessList(nextProcess);
                switch (nextProcess.GetPriority()) {
                    case REALTIME:
                        nextProcess.SetPriority(OS.Priority.INTERACTIVE);
                        interactiveProcessList.add(interactiveProcessList.size(), nextProcess);
                        System.out.println("~~~~Demoted REALTIME process " + nextProcess.GetName() + " to INTERACTIVE~~~~");
                        break;
                    case INTERACTIVE:
                        nextProcess.SetPriority(OS.Priority.BACKGROUND);
                        backgroundProcessList.add(backgroundProcessList.size(), nextProcess);
                        System.out.println("~~~~Demoted INTERACTIVE process " + nextProcess.GetName() + " to BACKGROUND~~~~");
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
    }

    public void WakeUpSleepingProcesses() {
        if (sleepingProcessList.size() > 0) {
            for (int i = 0; i < sleepingProcessList.size(); i++) {
                // Wake up sleeping processes that have surpassed the minimum wake up time.
                if ((int)clock.millis() >= sleepingProcessList.get(i).GetWakeUpTime()) {
                    KernelandProcess sleepingProcess = sleepingProcessList.get(i);
                    sleepingProcessList.remove(i);
                    switch (sleepingProcess.GetPriority()) {
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

    public int GetPID() {
        return currentProcess.GetPID();
    }

    public int GetPIDByName(String s) {
        if (nameToPIDMap.get(s) != null) {
            return nameToPIDMap.get(s);
        }
        return -1;
    }

    public KernelandProcess GetProcessByPID(int input_PID) {
        return PIDToProcessMap.get(input_PID);
    }    

    public void AddToWaitingProcesses(KernelandProcess inputProcess) {
        waitingProcesses.put(inputProcess.GetPID(), inputProcess);
    }

    public void CheckIfWaitingAndRestore(KernelandProcess inputProcess) {
        // If process is waiting, remove from waiting list and restore to process list corresponding to priority
        if (waitingProcesses.containsKey(inputProcess.GetPID())) {
            KernelandProcess restoredProcess = waitingProcesses.get(inputProcess.GetPID());
            waitingProcesses.remove(inputProcess.GetPID());
            switch (restoredProcess.GetPriority()) {
                case REALTIME:
                    realtimeProcessList.add(restoredProcess);
                    break;
                case INTERACTIVE:
                    interactiveProcessList.add(restoredProcess);
                    break;
                case BACKGROUND:
                    backgroundProcessList.add(restoredProcess);
                    break;
            }
        }
    }

    public void GetMapping(int virtualPageNumber) {
        currentProcess.GetMapping(virtualPageNumber);
    }

    public int AllocateMemory(int[] physicalPagesArray) {
        return currentProcess.AllocateMemory(physicalPagesArray);
    }

    public int[] FreeMemory(int virtualPageNumber, int numberOfPages) {
        return currentProcess.FreeMemory(virtualPageNumber, numberOfPages);
    }

    public void KillCurrentProcess() {
        currentProcess.KillProcess();
    }

    public KernelandProcess GetRandomProcess() {
        Object[] processes = PIDToProcessMap.values().toArray();
        return (KernelandProcess) processes[rand.nextInt(processes.length)];
    }

    public int PageSwap() {
        int virtualIndex = -1;
        KernelandProcess randomProcess = null;
        while (virtualIndex == -1) {
            randomProcess = GetRandomProcess();
            virtualIndex = randomProcess.LookForActivePhysicalPage();
        }
        VirtualToPhysicalMapping victim = randomProcess.GetMappingObject(virtualIndex);
        byte[] data = OS.ReadFromMemory(victim.physicalPageNumber);
        if (victim.diskPageNumber != -1) {
            OS.WriteToDisk(victim.diskPageNumber, data);
        }
        else {
            int newDiskPageNumber = OS.WriteToDisk(data);
            victim.diskPageNumber = newDiskPageNumber;
        }
        int victimsPhysicalPage = victim.physicalPageNumber;
        victim.physicalPageNumber = -1;
        randomProcess.SetMappingObject(virtualIndex, victim);
        return victimsPhysicalPage;
    }

}
