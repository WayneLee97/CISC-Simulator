package edu.gwu.seas.csci;

public class Instructions {
	//a useful function to convert binary string to integer
	public int binary_to_int(String binary_number){
		//initialize the return value
		int number = 0;
		for (int i = binary_number.length() - 1; i > 0; i--) {
			//calculate the weight on every digit
			number = number + ((int)binary_number.charAt(i) - (int)('0')) * (int)(Math.pow(2, binary_number.length() - 1 - i));
		}
		return number;
	}
	
	//another useful function to convert integer into binary string
	public String int_to_binary(int number){
		String binary_number = Integer.toBinaryString(number);
		return binary_number;
	}
	
	//function to calculate the effective address
	public int EA_calculator(String instruction, Registers registers, Memory mm){
		//initialize the return number
		int EA = 0;
		/*
		 * there will be 4 branches, which depends on the values of I and IX. Just a reminder here:
		 * The index range of IX is(8, 10). The index range of R is(6, 8). The index of I is(10). The
		 * index range of address is (11,16).
		 * */
		//no indirect addressing
		if(instruction.charAt(10) == '0'){
			//and no indexing. Then the EA is exactly the address in the instruction
			if(instruction.substring(8, 10).equals("00")){
				EA = binary_to_int(instruction.substring(11, 16));
			}else{//no indirect addressing but indexing, EA = address + c(IX)
				//3 branches to get the value of IX
				if(instruction.substring(8, 10).equals("01")){//X1
					EA = binary_to_int(instruction.substring(11, 16)) + binary_to_int(registers.getX1());
				}else if(instruction.substring(8, 10).equals("10")){//X2
					EA = binary_to_int(instruction.substring(11, 16)) + binary_to_int(registers.getX2());
				}else if(instruction.substring(8, 10).equals("11")){//X3
					EA = binary_to_int(instruction.substring(11, 16)) + binary_to_int(registers.getX3());
				}
			}
		}else if(instruction.charAt(10) == '1'){//indirect addressing
			if(instruction.substring(8, 10).equals("00")){//but no indexing. EA = c(address)
				EA = binary_to_int(instruction.substring(11, 16));
				EA = binary_to_int(mm.getMemory(EA));
			}else{//indirect addressing and indexing. EA = c(address + c(IX))
				//still, 3 branches to get value of IX and add it to address
				if(instruction.substring(8, 10).equals("01")){//X1
					EA = binary_to_int(instruction.substring(11, 16)) + binary_to_int(registers.getX1());
					EA = binary_to_int(mm.getMemory(EA));
				}else if(instruction.substring(8, 10).equals("10")){//X2
					EA = binary_to_int(instruction.substring(11, 16)) + binary_to_int(registers.getX2());
					EA = binary_to_int(mm.getMemory(EA));
				}else if(instruction.substring(8, 10).equals("11")){//X3
					EA = binary_to_int(instruction.substring(11, 16)) + binary_to_int(registers.getX3());
					EA = binary_to_int(mm.getMemory(EA));
				}
			}
		}
		return EA;
	}
	
	//load register from memory
	public void LDR(String instruction, Registers registers, Memory mm){
		//first of all, get the EA
		int EA = EA_calculator(instruction, registers, mm);
		//set the MAR
		registers.setMAR(String.format("%016d", int_to_binary(EA)));
		//find out which register to be loaded from the memory
		if(instruction.substring(6, 8).equals("00")){//R0
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			registers.setR0(mm.getMemory(EA));
		}else if(instruction.substring(6, 8).equals("01")){//R1
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			registers.setR1(mm.getMemory(EA));
		}else if(instruction.substring(6, 8).equals("10")){//R2
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			registers.setR2(mm.getMemory(EA));
		}else if(instruction.substring(6, 8).equals("11")){//R3
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			registers.setR3(mm.getMemory(EA));
		}
	}
	
	//store register to memory
	public void STR(String instruction, Registers registers, Memory mm){
		//first of all, get the EA
		int EA = EA_calculator(instruction, registers, mm);
		//set MAR
		registers.setMAR(String.format("%016d", int_to_binary(EA)));
		//find out which register to be stored into the memory
		if(instruction.substring(6, 8).equals("00")){//R0
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, String.format("%016d", registers.getR0()));
		}else if(instruction.substring(6, 8).equals("01")){//R1
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, String.format("%016d", registers.getR0()));
		}else if(instruction.substring(6, 8).equals("10")){//R2
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, String.format("%016d", registers.getR0()));
		}else if(instruction.substring(6, 8).equals("11")){//R3
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, String.format("%016d", registers.getR0()));
		}
	}
	
	//load register with address
	public void LDA(String instruction, Registers registers, Memory mm){
		//still, get the EA
		int EA = EA_calculator(instruction, registers, mm);
		//find out which register to store the address, and format the address to make become a 16-bits string
		if(instruction.substring(6, 8).equals("00")){//R0
			registers.setR0(String.format("%016d", int_to_binary(EA)));
		}else if(instruction.substring(6, 8).equals("01")){//R1
			registers.setR1(String.format("%016d", int_to_binary(EA)));
		}else if(instruction.substring(6, 8).equals("10")){//R2
			registers.setR2(String.format("%016d", int_to_binary(EA)));
		}else if(instruction.substring(6, 8).equals("11")){//R3
			registers.setR3(String.format("%016d", int_to_binary(EA)));
		}
	}
	
	//load index register from the memory
	public void LDX(String instruction, Registers registers, Memory mm){
		//still, get the EA
		int EA = EA_calculator(instruction, registers, mm);
		//set MAR
		registers.setMAR(String.format("%016d", int_to_binary(EA)));
		//find out which register to store the address
		if(instruction.substring(8, 10).equals("01")){//X1
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			registers.setX1(mm.getMemory(EA));
		}else if(instruction.substring(8, 10).equals("10")){//X2
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			registers.setX2(mm.getMemory(EA));
		}else if(instruction.substring(8, 10).equals("11")){//X3
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			registers.setX3(mm.getMemory(EA));
		}
	}
	
	//store index register to memory
	public void STX(String instruction, Registers registers, Memory mm){
		//still, get the EA
		int EA = EA_calculator(instruction, registers, mm);
		//set MAR
		registers.setMAR(String.format("%016d", int_to_binary(EA)));
		//find out which register to store the address
		if(instruction.substring(8, 10).equals("01")){//X1
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, registers.getX1());
		}else if(instruction.substring(8, 10).equals("10")){//X2
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, registers.getX2());
		}else if(instruction.substring(8, 10).equals("11")){//X3
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, registers.getX3());
		}
	}
}
