
public class Computer {

	//the translator function which gets the operation code
	public static int op_translator(String S_instruction, Instructions instruction){
		String op_code = S_instruction.substring(0, 6);
		return instruction.binary_to_int(op_code);
	}
	
	public static void main(String[] args) {
		//initialization of registers and memory
		Registers registers = new Registers();
		Memory mm = new Memory();//remember the first 6 addresses are preserved
		Instructions instruction = new Instructions();
		
		//some tests
		registers.setX1("0000000000000101");
		mm.setMemory(6, "0000000000010010");
		mm.setMemory(18, "0000000000001111");
		int address = instruction.EA_calculator("0000010100100110", registers, mm);
		System.out.println(address);
                
                Simulator_MainWindow frontPanel = new Simulator_MainWindow(registers, mm, instruction);
                frontPanel.setVisible(true);
		
		/*
		 * boot program where everything is initialized again and start, then we go to auto-run or single step
		 * */
		
		
		//auto-run, just get next PC and find out whether it is HALT operation
		while(!registers.getPC().equals("0000000000000000")){
			//execute the instruction,but first use the translator function to get the operation code
			int op_code = Computer.op_translator(registers.getPC(), instruction);
			switch (op_code) {
			//load register from the memory, LDR
			case 1:
				registers.setIR(registers.getPC());
				instruction.LDR(registers.getPC(), registers, mm);
				break;
			//store register from memory, STR
			case 2:
				registers.setIR(registers.getPC());
				instruction.STR(registers.getPC(), registers, mm);
				break;
			//load register with address, LDA
			case 3:
				registers.setIR(registers.getPC());
				instruction.LDA(registers.getPC(), registers, mm);
				break;
			//load index register from the memory, LDX
			case 41:
				registers.setIR(registers.getPC());
				instruction.LDX(registers.getPC(), registers, mm);
				break;
			//store index register to memory, STX
			case 42:
				registers.setIR(registers.getPC());
				instruction.STX(registers.getPC(), registers, mm);
				break;
			default:
				break;
			}
			//go to the next instruction
			int next_address = instruction.binary_to_int(registers.getPC()) + 1;
			registers.setPC(instruction.int_to_binary(next_address));
		}
		
		//single step mode
		
	}

}
