
import java.util.LinkedHashMap;
import java.util.Set;
//import static Utilities.*;

public class Memory
{

    private String memory[];
    private LinkedHashMap<Integer, String> cache;
    public final int MAX_CACHE = 16;
    public final int MEMORY_SIZE = 2048;

    private static Memory inst = null;

    private Memory()
    {
        memory = new String[MEMORY_SIZE];
        cache = new LinkedHashMap<Integer, String>();
    }

    public static Memory instance()
    {
        if (inst == null)
        {
            inst = new Memory();
        }

        return inst;
    }

    //setter and getter
    public String getMemory(int address)
    {
        if (!cache.containsKey(address))
        {
            if (cache.size() == MAX_CACHE)
            {
                cache.remove(cache.keySet().iterator().next());
            }

            cache.put(address, memory[address]);
        }
        return cache.get(address);

    }

    public void setMemory(int address, String content)
    {
        if (cache.containsKey(address))
        {
            cache.put(address, content);
        }
        this.memory[address] = content;
    }

    public String printCache()
    {
        return cache.toString();
    }

    public LinkedHashMap<Integer, String> getCache()
    {
        return cache;
    }

    public void writeStringToMemory(int startAddress, String str)
    {
        int size = str.length();

        int currentAddress = startAddress;
        setMemory(currentAddress, Instructions.int_to_binary(size));
        currentAddress++;
        for (int i = 0; i < size; i++)
        {
            setMemory(currentAddress, Instructions.int_to_binary((int) str.charAt(i)));
            currentAddress++;
        }
    }

}
