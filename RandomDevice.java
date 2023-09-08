import java.util.Random;

public class RandomDevice implements Device {
    private Random[] randomArray = new Random[10];

    public RandomDevice() {
        
    }

    public int Open(String string) {
        int i = 0;
        if (string != null && string != "") {
            int seed = Integer.parseInt(string);
            Random newRandom = new Random(seed);
            // Places new Random device into first open spot in the array
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
        Random rand = randomArray[id];
        // Fill array with random bytes
        rand.nextBytes(array);
        return array;
    }

    public int Write(int id, byte[] data) {
        return 0;
    }

    public void Seek(int id, int to) {
        for (int i = id; i < to; i++) {
            Random currentRandom = randomArray[i];
        }
    }
}
