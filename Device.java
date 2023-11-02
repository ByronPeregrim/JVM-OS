import java.io.FileNotFoundException;
import java.security.InvalidAlgorithmParameterException;

public interface Device {
    int Open(String s) throws InvalidAlgorithmParameterException, FileNotFoundException;
    void Close(int id);
    byte[] Read(int id,int size);
    void Seek(int id,int to);
    int Write(int id, byte[] data);
}
