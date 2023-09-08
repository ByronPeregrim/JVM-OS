import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class FakeFileSystem implements Device {

    RandomAccessFile[] array = new RandomAccessFile[10];
    
    FakeFileSystem () {

    }

    public int Open(String string) throws IOException {
        if (string == null || string == "") {
            throw new FileNotFoundException(string);
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

    public void Close(int id) throws IOException {
        array[id].close();
        array[id] = null;
    }

    public byte[] Read(int id, int size) throws IOException {
        byte[] byteArray = new byte[size];
        RandomAccessFile current = array[id];
        current.read(byteArray);
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

    public void Seek(int id, int to) throws IOException {
        // Open up file located at index, id. Offset file-pointer to second argument
        RandomAccessFile current = array[id];
        current.seek((long)to);
    }
}
