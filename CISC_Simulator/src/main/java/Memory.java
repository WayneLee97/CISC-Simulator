import java.util.LinkedHashMap;
import java.util.Set;

public class Memory 
{
	private String memory[];
        private LinkedHashMap<Integer,String> cache;
        public final int MAX_CACHE = 16;
	//construct function by default
	Memory()
        {
		memory = new String[2048];
                cache = new LinkedHashMap<Integer,String>();
	}
	//expend memory
	Memory(int size)
        {
		memory = new String[size];
	}
	//setter and getter
	public String getMemory(int address) 
        {
            if(!cache.containsKey(address))
            {
                if(cache.size() == MAX_CACHE)
                {
                    cache.remove(cache.keySet().iterator().next());
                }
                
                cache.put(address, memory[address]);
            }
            return cache.get(address);

	}
	public void setMemory(int address, String content) 
        {
		this.memory[address] = content;
	}
        
        public String printCache()
        {

            
            return cache.toString();
        }
        
        public LinkedHashMap<Integer,String> getCache()
        {
            return cache;
        }
        
}
