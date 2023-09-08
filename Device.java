import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

public interface Device {
    int Open(String s) throws IOException, InvalidAlgorithmParameterException;
    void Close(int id) throws IOException;
    byte[] Read(int id,int size) throws IOException;
    void Seek(int id,int to) throws IOException;
    int Write(int id, byte[] data);
}
