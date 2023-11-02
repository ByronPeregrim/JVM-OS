import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class FakeFileSystem implements Device {

    RandomAccessFile[] array = new RandomAccessFile[10];
    
    FakeFileSystem () {

    }

    public int Open(String string) throws FileNotFoundException {
        if (string == null || string == "") {
            System.err.println("FakeFileSystem: Open: File not found.");
            System.exit(0);
        }
        int i = 0;
        RandomAccessFile newRandomAccessFile = new RandomAccessFile(string, "rw");
        // Places RandomAccessFile into first open index in array
        for (i = 0; i < array.length; i++) {
            if (array[i] == null) {
                array[i] = newRandomAccessFile;
                break;
            }
        }
        return i;
    }

    public void Close(int id) {
        try {
            array[id].close();
        } catch (IOException e) {
            System.err.println("FakeFileSystem: Close: Error while attempting to close file, id: " + id);
            e.printStackTrace();
            System.exit(0);
        }
        array[id] = null;
    }

    public byte[] Read(int id, int size) {
        byte[] byteArray = new byte[size];
        RandomAccessFile current = array[id];
        try {
            current.read(byteArray);
        } catch (IOException e) {
            System.err.println("FakeFileSystem: Read: Error while attempting to read file, id: " + id);
            e.printStackTrace();
            System.exit(1);
        }
        return byteArray;
    }

    public int Write(int id, byte[] data) {
        RandomAccessFile current = array[id];
        // If write successful, return disk page number. Otherwise, return 0.
        try {
            current.write(data);
            return ((int) current.getFilePointer() - data.length) / 1024;
        } catch (IOException e) {
            System.err.println("FakeFileSystem: Write: Error while attempting to write file, id: " + id);
            e.printStackTrace();
            System.exit(2);
            return -1;
        }
    }

    public void Seek(int id, int to) {
        // Open up file located at index, id. Offset file-pointer to second argument
        RandomAccessFile current = array[id];
        // to == -1024 indicates a new block of the swap file needs to be assigned and fp needs to be moved to end of file
        if (to == -1024) {
            try {
                current.seek(current.length());
            } catch (IOException e) {
                System.err.println("FakeFileSystem: Seek: Error while attempting to seek file, id: " + id);
                e.printStackTrace();
                System.exit(3);
            }
        }
        else {
            try {
                current.seek((long)to);
            } catch (IOException e) {
                System.err.println("FakeFileSystem: Seek: Error while attempting to move pointer to end of file, id: " + id);
                e.printStackTrace();
                System.exit(4);
            }
        }
    }
}
