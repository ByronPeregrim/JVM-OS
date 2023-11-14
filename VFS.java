import java.io.FileNotFoundException;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;

public class VFS implements Device {

    private int VFS_ID_generator = 0;
    private RandomDevice randomDevice = new RandomDevice();
    private FakeFileSystem fakeFileSystem = new FakeFileSystem();

    private HashMap<Integer,Device_ID_Combination> VFS_devices = new HashMap<Integer,Device_ID_Combination> ();

    public VFS() {
        
    }

    public class Device_ID_Combination {
        private Device device;
        private int id;

        public Device_ID_Combination(Device device_arg, int id_arg) {
            device = device_arg;
            id = id_arg;
        }

        protected Device GetDevice() {
            return device;
        }

        protected int GetID() {
            return id;
        }
    }

    public int Open(String s) {
        // String argument should contain 2 word
        String[] args = s.split(" ");
        if (args.length != 2) {
            try {
                throw new InvalidAlgorithmParameterException("Incorrect number of arguments provided to VFS open method.");
            } catch (InvalidAlgorithmParameterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // Check first word of argument for device type
        String first_arg = args[0];
        if (first_arg.equals("random")) {
            // Second word of argument contains argument for devices Open method
            int newID = randomDevice.Open(args[1]);
            Device_ID_Combination newDeviceCombo = new Device_ID_Combination(randomDevice,newID);
            // Map VFS_ID to new instance of Device_ID_Combination
            int VFS_ID = VFS_ID_generator++;
            VFS_devices.put(VFS_ID,newDeviceCombo);
            return VFS_ID;
        }
        else if (first_arg.equals("file")) {
            int newID;
            newID = fakeFileSystem.Open(args[1]);
            Device_ID_Combination newDeviceCombo = new Device_ID_Combination(fakeFileSystem,newID);
            int VFS_ID = VFS_ID_generator++;
            VFS_devices.put(VFS_ID,newDeviceCombo);
            return VFS_ID;
        }
        // If first_arg doesn't match device type, fail
        else {
            return -1;
        }
    }

    public void Close(int id) {
        VFS_devices.remove(id);
    }

    public byte[] Read(int id, int size) {
        // Feed arguments into similar method within appropriate device
        Device_ID_Combination current_Combination = VFS_devices.get(id);
        Device current_Device = current_Combination.GetDevice();
        return current_Device.Read(current_Combination.GetID(), size);
    }

    public int Write(int id, byte[] data) {
        Device_ID_Combination current_Combination = VFS_devices.get(id);
        Device current_Device = current_Combination.GetDevice();
        return current_Device.Write(current_Combination.GetID(), data);
    }

    public void Seek(int id, int to) {
        Device_ID_Combination current_Combination = VFS_devices.get(id);
        Device current_Device = current_Combination.GetDevice();
        current_Device.Seek(current_Combination.GetID(), to);
    }

}
