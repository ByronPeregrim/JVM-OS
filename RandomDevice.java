import java.util.Random;

public class RandomDevice implements Device {

    Random[] randomArray = new Random[10];
    RandomDevice() {

    }

    public int Open(String string) {
        int i = 0;
        if (string != null && string != "") {
            int seed = Integer.parseInt(string);
            Random newRandom = new Random(seed);
            for (i = 0; i < randomArray.length; i++) {
                if (randomArray[i] == null) {
                    randomArray[i] = newRandom;
                    break;
                }
            }
        }
        return i;
    }

    public void Close(int id) {
        randomArray[id] = null;
    }

    public byte[] Read(int id, int size) {
        byte[] array = new byte[size];
        Random rand = new Random();
        rand.nextBytes(array);
        return array;
    }

    public int Write(int id, byte[] data) {
        return 0;
    }

    public void Seek(int id, int to) {
        Random currentRandom = randomArray[to];
    }
}
