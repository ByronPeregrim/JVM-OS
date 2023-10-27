public abstract class UserlandProcess implements Runnable {
    
    public static int[][] TLB = new int[2][2];
    public static byte[] memory = new byte[1048576];

    public byte Read(int virtualAddress) {
        int physicalAddress = ConvertVirtualAddressToPhysical(virtualAddress);
        if (physicalAddress != -1) {
            return memory[physicalAddress];
        }
        else {
            System.err.println("Could not READ from physical address: " + virtualAddress);
            return -1;
        }
    }

    public void Write(int virtualAddress, byte value) {
        int physicalAddress = ConvertVirtualAddressToPhysical(virtualAddress);
        if (physicalAddress != -1) {
            memory[physicalAddress] = value;
        }
        else {
            System.err.println("Could not WRITE byte value: " + value + " to physical address: " + virtualAddress);
        }
    }

    public int ConvertVirtualAddressToPhysical(int virtualAddress) {
        int virtualPageNumber = virtualAddress / 1024;
        int pageOffset = virtualAddress % 1024;
        int physicalPageNumber = -1;
        int physicalAddress = -1;
        for (int i = 0; i < TLB.length; i++) {
            if (TLB[i][0] == virtualPageNumber) {
                physicalPageNumber = TLB[i][1];
                break;
            }
        }
        if (physicalPageNumber != -1) { // Page was found
            physicalAddress = (physicalPageNumber * 1024) + pageOffset;
        }
        else {
            OS.GetMapping(virtualPageNumber);
            for (int i = 0; i < TLB.length; i++) {
                if (TLB[i][0] == virtualPageNumber) {
                    physicalPageNumber = TLB[i][1];
                    break;
                }
            }
            if (physicalPageNumber != -1) { // Page was found
                physicalAddress = (physicalPageNumber * 1024) + pageOffset;
            }
            else {
                System.err.println("Could not locate physical address corresponding to given virtual address.");
                OS.KillCurrentProcess();
            }
        }
        return physicalAddress;
    }
}