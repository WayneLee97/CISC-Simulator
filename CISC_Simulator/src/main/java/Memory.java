
public class Memory {
	private String memory[];
	//construct function by default
	Memory(){
		memory = new String[2048];
	}
	//expend memory
	Memory(int size){
		memory = new String[size];
	}
	//setter and getter
	public String getMemory(int address) {
		return memory[address];
	}
	public void setMemory(int address, String content) {
		this.memory[address] = content;
	}
}
