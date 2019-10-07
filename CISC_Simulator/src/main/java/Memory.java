
public class Memory 
{
    private Word memory[];
    private int memorySize = 2048;
    //construct function by default
    Memory()
    {
        memory = new Word[memorySize];
        for(int i = 0; i < memorySize; i++)
        {
            memory[i] = new Word();
        }
    }
    //expand memory
    Memory(int size)
    {
        memorySize = size;
        memory = new Word[memorySize];
    }
    //setter and getter
    public Word getMemory(int address) 
    {
        return memory[address];
    }

    public void setMemory(int address, String content) 
    {
        this.memory[address].setData(content);
    }
}
