import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class FakeFileSystem implements Device {

    RandomAccessFile[] array = new RandomAccessFile[10];
    
    FakeFileSystem () {
        
    }

    public int Open(String string) {
        if (string == null || string == "") {
            try {
                throw new FileNotFoundException(string);
            } catch (FileNotFoundException e) {
                System.exit(0);
                e.printStackTrace();
            }
        }
        int i = 0;
        RandomAccessFile newRandomAccessFile;
        try {
            newRandomAccessFile = new RandomAccessFile(string, "rw");
        } catch (FileNotFoundException e) {
            System.err.println("FakeFileSystem: Open: File: " + string + " not found.");
            newRandomAccessFile = null;
            e.printStackTrace();
            System.exit(1);
        }
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
            e.printStackTrace();
            System.err.println("FakeFileSystem: Close: IOException.");
            System.exit(2);
        }
        array[id] = null;
    }

    public byte[] Read(int id, int size) {
        byte[] byteArray = new byte[size];
        RandomAccessFile current = array[id];
        try {
            current.read(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("FakeFileSystem: Read: IOException.");
            System.exit(3);
        }
        return byteArray;
    }

    public int Write(int id, byte[] data) {
        RandomAccessFile current = array[id];
        // If write successful, return 1. Otherwise, return 0.
        try {
            current.write(data);
            return 0;
        } catch (IOException e) {
            return -1;
        }
    }

    public void Seek(int id, int to) {
        // Open up file located at index, id. Offset file-pointer to second argument
        RandomAccessFile current = array[id];
        try {
            current.seek((long)to);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("FakeFileSystem: Seek: IOException.");
            System.exit(4);
        }
    }
}
