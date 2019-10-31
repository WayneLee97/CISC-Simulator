public class Instructions {
	public static final int OVERFLOW_MAX = 32767;
    public static final int UNDERFLOW_MIN = -32768;
    
    private static Instructions inst = null;
    private Registers registers;
    private Memory mm;
    private IOHandler io;
    
  //singleton constructor
    private Instructions()
    {
        registers = Registers.instance();
        mm = Memory.instance();
        io = IOHandler.instance();
    }
    
    //returns the singelton instance
    public static Instructions instance()
    {
        if(inst == null)
        {
            inst = new Instructions();
        }

        return inst;
    }
    
	//a useful function to convert binary string to integer
	static public Integer binary_to_int(String binary_number)
        {
		return Integer.parseInt(binary_number, 2);
	}
	
	 //convert binary string to 16 bits Integer number
    static public Integer binary_to_int_16bits(String binary_number)
    {
        if ((binary_number.charAt(0) - '0') == 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                String cur = binary_number.charAt(i) =='1' ? "0" : "1";
                sb.append(cur);
            }
            int temp = Integer.parseInt(sb.toString(), 2) + 1;
            return -1 * temp;
        } else {
           return Integer.parseInt(binary_number, 2);
        }
    }
    
  //convert a char to a 16bit binary number
    public String characterToBinary(String character)
    {
        int decimal = character.charAt(0);
        return int_to_binary(decimal);
    }
    
    //convert a 16bit binary number to a char
    public String binaryToCharacter(String binary)
    {
        int val = binary_to_int(binary);
        return Character.toString((char)val);
    }
    
    //check overflow or underflow
    private boolean checkOverUnderFlow(Registers registers, int number) {
        if (number > OVERFLOW_MAX) {
            registers.setCC0(1);
            return true;
        } else if (number < UNDERFLOW_MIN) {
            registers.setCC1(1);
            return true;
        }
        return false;
    }
	
	//another useful function to convert integer into binary string
	static public String int_to_binary(Integer number)
        {
		String binary_number = Integer.toBinaryString(number);
		return binary_number;
	}
	
	//get high order bits when overflow/underflow.
    private String getHighOrderBits(Integer number) {
        String binary_number = Integer.toBinaryString(number);
        return binary_number.substring(0, 16);
    }

    //convert integer to 16 bits binary string
    static public String int_to_binary_16bits(Integer number)
    {
        return int_to_binary_xbits(number,16);
    }
    static public String int_to_binary_xbits(Integer number, int numBits)
    {
        String binary_number = Integer.toBinaryString(number);
        if (binary_number.length() > numBits) {

            return binary_number.substring(numBits, 32);
        } else if (binary_number.length() < numBits) {
            int numberOfZero = numBits - binary_number.length();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < numberOfZero; i++) {
                sb.append("0");
            }
            return sb.toString() + binary_number;
        }
        return binary_number;
    }
    //ADD operation
    private String addOperation(String one, String two) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < one.length(); i++) {
            if (one.charAt(i) == '0' || two.charAt(i) == '0') {
                sb.append("0");
            } else {
                sb.append("1");
            }
        }
        return sb.toString();
    }

    //OR operation
    private String orOperation(String one, String two) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < one.length(); i++) {
            if (one.charAt(i) == '1' || two.charAt(i) == '1') {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }
        return sb.toString();
    }

    //NOT operation
    private String notOperation(String address) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < address.length(); i++) {
            String cur = address.charAt(i) == '1' ? "0" : "1";
            sb.append(cur);
        }
        return sb.toString();
    }
	
	//function to calculate the effective address
	public int EA_calculator(String instruction){
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
	public void LDR(String instruction){
		//first of all, get the EA
		int EA = EA_calculator(instruction);
		//set the MAR
		registers.setMAR(int_to_binary_16bits(EA));
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
	public void STR(String instruction){
		//first of all, get the EA
		int EA = EA_calculator(instruction);
		//set MAR
		registers.setMAR(int_to_binary_16bits(EA));
		//find out which register to be stored into the memory
		if(instruction.substring(6, 8).equals("00")){//R0
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, registers.getR0());
		}else if(instruction.substring(6, 8).equals("01")){//R1
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, registers.getR1());
		}else if(instruction.substring(6, 8).equals("10")){//R2
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, registers.getR2());
		}else if(instruction.substring(6, 8).equals("11")){//R3
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, registers.getR3());
		}
	}
	
	//load register with address
	public void LDA(String instruction){
		//still, get the EA
		int EA = EA_calculator(instruction);
		//find out which register to store the address, and format the address to make become a 16-bits string
		if(instruction.substring(6, 8).equals("00")){//R0
			registers.setR0(int_to_binary_16bits(EA));
		}else if(instruction.substring(6, 8).equals("01")){//R1
			registers.setR1(int_to_binary_16bits(EA));
		}else if(instruction.substring(6, 8).equals("10")){//R2
			registers.setR2(int_to_binary_16bits(EA));
		}else if(instruction.substring(6, 8).equals("11")){//R3
			registers.setR3(int_to_binary_16bits(EA));
		}
	}
	
	//load index register from the memory
	public void LDX(String instruction){
            System.out.println("LDX");
		//still, get the EA
		int EA = EA_calculator(instruction);
		//set MAR
		registers.setMAR(int_to_binary_16bits(EA));
		//find out which register to store the address
		if(instruction.substring(6, 8).equals("01")){//X1
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			registers.setX1(mm.getMemory(EA));
		}else if(instruction.substring(6, 8).equals("10")){//X2
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			registers.setX2(mm.getMemory(EA));
		}else if(instruction.substring(6, 8).equals("11")){//X3
                    System.out.println("    x3" + String.valueOf(EA));
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			registers.setX3(mm.getMemory(EA));
		}
	}
	
	//store index register to memory
	public void STX(String instruction){
		//still, get the EA
		int EA = EA_calculator(instruction);
		//set MAR
		registers.setMAR(int_to_binary(EA));
		//find out which register to store the address
		if(instruction.substring(6, 8).equals("01")){//X1
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, registers.getX1());
		}else if(instruction.substring(6, 8).equals("10")){//X2
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, registers.getX2());
		}else if(instruction.substring(6, 8).equals("11")){//X3
			//set MBR
			registers.setMBR(mm.getMemory(EA));
			mm.setMemory(EA, registers.getX3());
		}
	}
	
	//jump if zero
	public void JZ(String instruction){
		int EA = EA_calculator(instruction);
		if(instruction.substring(6, 8).equals("00")){//R0
			if(binary_to_int(registers.getR0()) == 0){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("01")){//R1
			if(binary_to_int(registers.getR1()) == 0){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("10")){//R2
			if(binary_to_int(registers.getR2()) == 0){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("11")){//R3
			if(binary_to_int(registers.getR3()) == 0){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}
	}
	
	//jump if not equal
	public void JNE(String instruction){
		int EA = EA_calculator(instruction);
		if(instruction.substring(6, 8).equals("00")){
			if(binary_to_int(registers.getR0()) != 0){//R0
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("01")){//R1
			if(binary_to_int(registers.getR1()) != 0){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("10")){//R2
			if(binary_to_int(registers.getR2()) != 0){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("11")){//R3
			if(binary_to_int(registers.getR3()) != 0){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}
	}
	
	//jump if condition code
	public void JCC(String instruction){
		int EA = EA_calculator(instruction);
		if(instruction.substring(6, 8).equals("00")){
			if(registers.getCC0() == 1){//CC0
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("01")){//CC1
			if(registers.getCC1() == 1){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("10")){//CC2
			if(registers.getCC2() == 1){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("11")){//CC3
			if(registers.getCC3() == 1){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}
	}
	
	//unconditional jump to address
	public void JMA(String instruction){
		int EA = EA_calculator(instruction);
		registers.setPC(int_to_binary_16bits(EA));
	}
	
	//jump and save return address
	public void JSR(String instruction){
		int EA = EA_calculator(instruction);
		int next_address = binary_to_int(registers.getPC()) + 1;
		//R3 = PC + 1
		registers.setR3(int_to_binary_16bits(next_address));
		registers.setPC(int_to_binary_16bits(EA));
		//R0 should contain pointer to arguments
	}
	
	//return from subroutine with return code as immediate portion stored in the instructions' address field
	public void RFS(String instruction){
		//R0 = Immed
		registers.setR0("00000000000" + instruction.substring(11, 16));
		registers.setPC(registers.getR3());
	}
	
	//subtract one and branch
	public void SOB(String instruction){
		int EA = EA_calculator(instruction);
		if(instruction.substring(6, 8).equals("00")){//R0
			registers.setR0(int_to_binary_16bits(binary_to_int(registers.getR0()) - 1));
			if(binary_to_int(registers.getR0()) > 0){
				//PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("01")){//R1
			registers.setR1(int_to_binary_16bits(binary_to_int(registers.getR1()) - 1));
			if(binary_to_int(registers.getR1()) > 0){
				//PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("10")){//R2
			registers.setR2(int_to_binary_16bits(binary_to_int(registers.getR2()) - 1));
			if(binary_to_int(registers.getR2()) > 0){
				//PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("11")){//R3
			registers.setR3(int_to_binary_16bits(binary_to_int(registers.getR3()) - 1));
			if(binary_to_int(registers.getR3()) > 0){
				//PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}
	}
	
	//jump greater than or equal
	public void JGE(String instruction){
		int EA = EA_calculator(instruction);
		if(instruction.substring(6, 8).equals("00")){
			if(binary_to_int_16bits(registers.getR0()) >= 0){//R0
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("01")){//R1
			if(binary_to_int_16bits(registers.getR1()) >= 0){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("10")){//R2
			if(binary_to_int_16bits(registers.getR2()) >= 0){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}else if(instruction.substring(6, 8).equals("11")){//R3
			if(binary_to_int_16bits(registers.getR3()) >= 0){
				//R0 = 0, PC = EA
				registers.setPC(int_to_binary_16bits(EA));
			}else{
				//PC++ inside of this instruction
				int next_address = binary_to_int(registers.getPC()) + 1;
	            registers.setPC(int_to_binary_16bits(next_address));
			}
		}
	}
	
	public void AMR(String instruction) {
        //still, get the EA
        int EA = EA_calculator(instruction);
        //set MAR
        registers.setMAR(int_to_binary_16bits(EA));
        registers.setMBR(mm.getMemory(EA));
        //find out which register to store the address
        if(instruction.substring(6, 8).equals("00")){//R0
            String address = int_to_binary_16bits(binary_to_int(registers.getR0()) + binary_to_int(mm.getMemory(EA)));
            registers.setR0(address);
        }else if(instruction.substring(6, 8).equals("01")){//R1
            String address = int_to_binary_16bits(binary_to_int(registers.getR1()) + binary_to_int(mm.getMemory(EA)));
            registers.setR1(address);
        }else if(instruction.substring(6, 8).equals("10")){//R2
            String address = int_to_binary_16bits(binary_to_int(registers.getR2()) + binary_to_int(mm.getMemory(EA)));
            registers.setR2(address);
        }else if(instruction.substring(6, 8).equals("11")){//R3
            String address = int_to_binary_16bits(binary_to_int(registers.getR3()) + binary_to_int(mm.getMemory(EA)));
            registers.setR3(address);
        }
    }

    //Subtract memory from register (r <- c(r) - c(EA))
    public void SMR(String instruction) {
        //still, get the EA
        int EA = EA_calculator(instruction);
        //set MAR
        registers.setMAR(int_to_binary_16bits(EA));
        registers.setMBR(mm.getMemory(EA));
        //find out which register to store the address
        if(instruction.substring(6, 8).equals("00")){//R0
            String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR0()) - binary_to_int_16bits(mm.getMemory(EA)));
            registers.setR0(address);
        }else if(instruction.substring(6, 8).equals("01")){//R1
            String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR1()) - binary_to_int_16bits(mm.getMemory(EA)));
            registers.setR1(address);
        }else if(instruction.substring(6, 8).equals("10")){//R2
            String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR2()) - binary_to_int_16bits(mm.getMemory(EA)));
            registers.setR2(address);
        }else if(instruction.substring(6, 8).equals("11")){//R3
            String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR3()) - binary_to_int_16bits(mm.getMemory(EA)));
            registers.setR3(address);
        }
    }

    //Add immediate to register
    /**
     *
     * @param instruction
     * @param registers
     * @param  5 bits String
     */
    public void AIR(String instruction) {
    	String immed = instruction.substring(11, 16);
    	int immedValue = binary_to_int(immed);
        if (immedValue != 0) {
            //find out which register to store the address
            if (instruction.substring(6, 8).equals("00")) {//R0
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR0()) + immedValue);
                registers.setR0(address);
            } else if(instruction.substring(6, 8).equals("01")){//R1
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR1()) + immedValue);
                registers.setR1(address);
            }else if(instruction.substring(6, 8).equals("10")){//R2
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR2()) + immedValue);
                registers.setR2(address);
            }else if(instruction.substring(6, 8).equals("11")){//R3
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR3()) + immedValue);
                registers.setR3(address);
            }
        }
    }

    //Subtract immediate from register
    /**
     *
     * @param instruction
     * @param registers
     * @param  5 bits String
     */
    public void SIR(String instruction) {
    	String immed = instruction.substring(11, 16);
    	int immedValue = binary_to_int(immed);
        if (immedValue != 0) {
            //find out which register to store the address
            if (instruction.substring(6, 8).equals("00")) {//R0
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR0()) - immedValue);
                registers.setR0(address);
            } else if(instruction.substring(6, 8).equals("01")){//R1
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR1()) - immedValue);
                registers.setR1(address);
            }else if(instruction.substring(6, 8).equals("10")){//R2
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR2()) - immedValue);
                registers.setR2(address);
            }else if(instruction.substring(6, 8).equals("11")){//R3
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR3()) - immedValue);
                registers.setR3(address);
            }
        }
    }

    /**
     *
     * @param instruction
     * @param registers
     *      Multiply Register by Register
     */
    public void MLT(String instruction) {
        String rx = instruction.substring(6, 8);
        String ry = instruction.substring(8, 10);
        if (rx.equals("00")) {
            if (ry.equals("00")) {
                int result = binary_to_int_16bits(registers.getR0()) * binary_to_int_16bits(registers.getR0());
                registers.setR1(int_to_binary_16bits(result));
                if (checkOverUnderFlow(registers, result)) {
                    registers.setR0(getHighOrderBits(result));
                }
            } else if (ry.equals("10")) {
                int result = binary_to_int_16bits(registers.getR0()) * binary_to_int_16bits(registers.getR2());
                registers.setR1(int_to_binary_16bits(result));
                if (checkOverUnderFlow(registers, result)) {
                    registers.setR0(getHighOrderBits(result));
                }
            }
        } else if (rx.equals("10")) {
            if (ry.equals("00")) {
                int result = binary_to_int_16bits(registers.getR2()) * binary_to_int_16bits(registers.getR0());
                registers.setR3(int_to_binary_16bits(result));
                if (checkOverUnderFlow(registers, result)) {
                    registers.setR2(getHighOrderBits(result));
                }
            } else if (ry.equals("10")) {
                int result = binary_to_int_16bits(registers.getR2()) * binary_to_int_16bits(registers.getR2());
                registers.setR3(int_to_binary_16bits(result));
                if (checkOverUnderFlow(registers, result)) {
                    registers.setR2(getHighOrderBits(result));
                }
            }
        }
    }

    /**
     *
     * @param instruction
     * @param registers
     *      Divide Register by Register
     */
    public void DVD(String instruction) {
        String rx = instruction.substring(6, 8);
        String ry = instruction.substring(8, 10);
        int R0 = binary_to_int_16bits(registers.getR0());
        int R2 = binary_to_int_16bits(registers.getR2());
        if (rx == "00") {
            if (ry == "00") {
                if (R0 == 0) {
                    registers.setCC3(1);
                } else {
                    int quotient = R0 / R0;
                    int remainder = R0 % R0;
                    registers.setR0(int_to_binary_16bits(quotient));
                    registers.setR1(int_to_binary_16bits(remainder));
                }
            } else if (ry == "10") {
                if (R2 == 0) {
                    registers.setCC3(1);
                } else {
                    int quotient = R0 / R2;
                    int remainder = R0 % R2;
                    registers.setR0(int_to_binary_16bits(quotient));
                    registers.setR1(int_to_binary_16bits(remainder));
                }
            }
        } else if (rx == "10") {
            if (ry == "00") {
                if (R0 == 0) {
                    registers.setCC3(1);
                } else {
                    int quotient = R2 / R0;
                    int remainder = R2 % R0;
                    registers.setR2(int_to_binary_16bits(quotient));
                    registers.setR3(int_to_binary_16bits(remainder));
                }
            } else if (ry == "10") {
                if (R2 == 0) {
                    registers.setCC3(1);
                } else {
                    int quotient = R2 / R2;
                    int remainder = R2 % R2;
                    registers.setR2(int_to_binary_16bits(quotient));
                    registers.setR3(int_to_binary_16bits(remainder));
                }
            }
        }
    }
    
    //Test the Equality of Register and Register
    public void TRR(String instruction){
    	int rx = binary_to_int(instruction.substring(6, 8));
        int ry = binary_to_int(instruction.substring(8, 10));
        if (rx == 0) {
            if (ry == 1) {
                if(registers.getR0().equals(registers.getR1())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            } else if (ry == 2) {
            	if(registers.getR0().equals(registers.getR2())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            } else if (ry == 3) {
            	if(registers.getR0().equals(registers.getR3())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            }
        } else if (rx == 1) {
        	if (ry == 1) {
                if(registers.getR1().equals(registers.getR1())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            } else if (ry == 2) {
            	if(registers.getR1().equals(registers.getR2())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            } else if (ry == 3) {
            	if(registers.getR1().equals(registers.getR3())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            }
        } else if (rx == 2) {
        	if (ry == 1) {
                if(registers.getR2().equals(registers.getR1())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            } else if (ry == 2) {
            	if(registers.getR2().equals(registers.getR2())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            } else if (ry == 3) {
            	if(registers.getR2().equals(registers.getR3())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            }
        } else if (rx == 3) {
        	if (ry == 1) {
                if(registers.getR3().equals(registers.getR1())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            } else if (ry == 2) {
            	if(registers.getR3().equals(registers.getR2())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            } else if (ry == 3) {
            	if(registers.getR3().equals(registers.getR3())){
                	registers.setCC3(1);
                }else{
                	registers.setCC3(0);
                }
            }
        }
    }
    
    //Logical And of register and register c(rx) <- c(rx) AND c(ry)
    /**
     *
     * @param instruction (6-8): Rx
     *                    (8-10) Ry
     */
    public void AND(String instruction) {
        int rx = binary_to_int(instruction.substring(6, 8));
        int ry = binary_to_int(instruction.substring(8, 10));
        if (rx == 0) {
            if (ry == 1) {
                registers.setR0(addOperation(registers.getR0(), registers.getR1()));
            } else if (ry == 2) {
                registers.setR0(addOperation(registers.getR0(), registers.getR2()));
            } else if (ry == 3) {
                registers.setR0(addOperation(registers.getR0(), registers.getR3()));
            }
        } else if (rx == 1) {
            if (ry == 0) {
                registers.setR1(addOperation(registers.getR1(), registers.getR0()));
            } else if (ry == 2) {
                registers.setR1(addOperation(registers.getR1(), registers.getR2()));
            } else if (ry == 3) {
                registers.setR1(addOperation(registers.getR1(), registers.getR3()));
            }
        } else if (rx == 2) {
            if (ry == 0) {
                registers.setR2(addOperation(registers.getR2(), registers.getR0()));
            } else if (ry == 1) {
                registers.setR2(addOperation(registers.getR2(), registers.getR1()));
            } else if (ry == 3) {
                registers.setR2(addOperation(registers.getR2(), registers.getR3()));
            }
        } else if (rx == 3) {
            if (ry == 0) {
                registers.setR3(addOperation(registers.getR3(), registers.getR0()));
            } else if (ry == 1) {
                registers.setR3(addOperation(registers.getR3(), registers.getR1()));
            } else if (ry == 2) {
                registers.setR3(addOperation(registers.getR3(), registers.getR2()));
            }
        }
    }
    //Logical Or of register and register
    public void ORR(String instruction) {
        int rx = binary_to_int(instruction.substring(6, 8));
        int ry = binary_to_int(instruction.substring(8, 10));
        if (rx == 0) {
            if (ry == 1) {
                registers.setR0(orOperation(registers.getR0(), registers.getR1()));
            } else if (ry == 2) {
                registers.setR0(orOperation(registers.getR0(), registers.getR2()));
            } else if (ry == 3) {
                registers.setR0(orOperation(registers.getR0(), registers.getR3()));
            }
        } else if (rx == 1) {
            if (ry == 0) {
                registers.setR1(orOperation(registers.getR1(), registers.getR0()));
            } else if (ry == 2) {
                registers.setR1(orOperation(registers.getR1(), registers.getR2()));
            } else if (ry == 3) {
                registers.setR1(orOperation(registers.getR1(), registers.getR3()));
            }
        } else if (rx == 2) {
            if (ry == 0) {
                registers.setR2(orOperation(registers.getR2(), registers.getR0()));
            } else if (ry == 1) {
                registers.setR2(orOperation(registers.getR2(), registers.getR1()));
            } else if (ry == 3) {
                registers.setR2(orOperation(registers.getR2(), registers.getR3()));
            }
        } else if (rx == 3) {
            if (ry == 0) {
                registers.setR3(orOperation(registers.getR3(), registers.getR0()));
            } else if (ry == 1) {
                registers.setR3(orOperation(registers.getR3(), registers.getR1()));
            } else if (ry == 2) {
                registers.setR3(orOperation(registers.getR3(), registers.getR2()));
            }
        }
    }

    //Logical Not of register to register
    public void NOT(String instruction) {
        int rx = binary_to_int(instruction.substring(6, 8));
        if (rx == 0) {
            registers.setR0(notOperation(registers.getR0()));
        } else if (rx == 1) {
            registers.setR1(notOperation(registers.getR1()));
        } else if (rx == 2) {
            registers.setR2(notOperation(registers.getR2()));
        } else if (rx == 3) {
            registers.setR3(notOperation(registers.getR3()));
        }
    }
    
    //shift register by count, A/L = 8, L/R = 9, count = 12-15
    public void SRC(String instruction){
    	String result_string;
    	String temp_string;
    	StringBuilder shift_string = new StringBuilder();
    	int temp_number;
    	if(instruction.substring(6,8).equals("00")){
    		temp_string = registers.getR0();
    	}else if(instruction.substring(6,8).equals("01")){
    		temp_string = registers.getR1();
    	}else if(instruction.substring(6,8).equals("10")){
    		temp_string = registers.getR2();
    	}else{
    		temp_string = registers.getR3();
    	}
    	//left or right
    	if(instruction.charAt(9) == '0'){//right
    		//A or L
    		if(instruction.charAt(8) == '0'){//arithmetically
    			temp_number = binary_to_int_16bits(temp_string);
    			temp_number = (int)(temp_number/(int)(Math.pow(2, binary_to_int(instruction.substring(12, 16)))));
    			result_string = int_to_binary_16bits(temp_number);
    		}else{//logically
    			for(int i = 0; i < binary_to_int(instruction.substring(12, 16)); i++){
    				shift_string.append('0');
    			}
    			result_string = shift_string.toString();
    			result_string = result_string + temp_string.substring(binary_to_int(instruction.substring(12, 16)), 16);
    		}
    	}else{//left
    		if(instruction.charAt(8) == '0'){//arithmetically
    			temp_number = binary_to_int_16bits(temp_string);
    			temp_number = (int)(temp_number * (int)(Math.pow(2, binary_to_int(instruction.substring(12, 16)))));
    			//check overflow or underflow
    			if (temp_number > OVERFLOW_MAX) {
    	            registers.setCC0(1);
    	        } else if (temp_number < UNDERFLOW_MIN) {
    	            registers.setCC1(1);
    	        }
    			result_string = int_to_binary_16bits(temp_number);
    		}else{//logically
    			for(int i = 0; i < binary_to_int(instruction.substring(12, 16)); i++){
    				shift_string.append('0');
    			}
    			result_string = shift_string.toString();
    			result_string = temp_string.substring(0, 16 - binary_to_int(instruction.substring(12, 16))) + result_string;
    		}
    	}
    	if(instruction.substring(6,8).equals("00")){
    		registers.setR0(result_string);
    	}else if(instruction.substring(6,8).equals("01")){
    		registers.setR1(result_string);
    	}else if(instruction.substring(6,8).equals("10")){
    		registers.setR2(result_string);
    	}else{
    		registers.setR3(result_string);
    	}
    }
    
    //Rotate register by count
    public void RRC(String instruction){
    	String result_string;
    	String temp_string;
    	StringBuilder shift_string = new StringBuilder();
    	if(instruction.substring(6,8).equals("00")){
    		temp_string = registers.getR0();
    	}else if(instruction.substring(6,8).equals("01")){
    		temp_string = registers.getR1();
    	}else if(instruction.substring(6,8).equals("10")){
    		temp_string = registers.getR2();
    	}else{
    		temp_string = registers.getR3();
    	}
    	if(instruction.charAt(9) == '0'){//right
    		if(instruction.charAt(8) == '0'){//arithmetically
    			result_string = temp_string.substring(0, 1) + temp_string.substring(16 - binary_to_int(instruction.substring(12, 16)), 16) + temp_string.substring(1, 16 - binary_to_int(instruction.substring(12, 16)));
    		}else{//logically
    			result_string = temp_string.substring(16 - binary_to_int(instruction.substring(12, 16)), 16) + temp_string.substring(0, 16 - binary_to_int(instruction.substring(12, 16)));
    		}
    	}else{//left
    		if(instruction.charAt(8) == '0'){//arithmetically
    			result_string = temp_string.substring(0, 1) + temp_string.substring(binary_to_int(instruction.substring(12, 16)), 16) + temp_string.substring(1, binary_to_int(instruction.substring(12, 16)));
    		}else{//logically
    			result_string = temp_string.substring(binary_to_int(instruction.substring(12, 16)), 16) + temp_string.substring(0, binary_to_int(instruction.substring(12, 16)));
    		}
    	}
    	if(instruction.substring(6,8).equals("00")){
    		registers.setR0(result_string);
    	}else if(instruction.substring(6,8).equals("01")){
    		registers.setR1(result_string);
    	}else if(instruction.substring(6,8).equals("10")){
    		registers.setR2(result_string);
    	}else{
    		registers.setR3(result_string);
    	}
    }
    
    public void IN(String instruction)
    {
        
        int devID = binary_to_int(instruction.substring(11,16));
        if(devID == 0 && io.hasInput())
        {
            String input = characterToBinary(io.getNextInput());

//            int intval = 0;
//            //while(io.hasInput())
//            while(true) // go until newline is recieved
//            {
//                char input = io.getNextInput().charAt(0);
//                if(input == '\n')
//                {
//                    break;
//                }
//                else
//                {
//                    intval = intval * 10;
//                    intval += (input - '0');
//                }
//            }
//            String input = int_to_binary_16bits(intval);
            
            if(instruction.substring(6, 8).equals("00"))
            {//R0
                registers.setR0(input);
	}
            else if(instruction.substring(6, 8).equals("01"))
            {//R1
                registers.setR1(input);
	}
            else if(instruction.substring(6, 8).equals("10"))
            {//R2
                registers.setR2(input);
	}
            else if(instruction.substring(6, 8).equals("11"))
            {//R3
                registers.setR3(input);
	}
        }
        else
        {
            //@TODO: Error?
        }
    }
    
    //loads a single character into the output buffer into the indicated
    //register
    public void OUT(String instruction)
    {
                    
        int devID = binary_to_int(instruction.substring(11,16));
        if(devID == 1)
        {
            String output = "";
            String binary = "";

            if(instruction.substring(6, 8).equals("00"))
            {//R0
                binary = registers.getR0();
            }
            else if(instruction.substring(6, 8).equals("01"))
            {//R1
                binary = registers.getR1();
            }
            else if(instruction.substring(6, 8).equals("10"))
            {//R2
                binary = registers.getR2();
            }
            else if(instruction.substring(6, 8).equals("11"))
            {//R3
                    binary = registers.getR3();
            }
            
            output = binaryToCharacter(binary);
            //output = Integer.toString(binary_to_int_16bits(binary));
            io.pushOutput(output);

        }
        else
        {
            //@TODO: Error?
        }
    }
    
    
    //CUSTOM INSTRUCTIONS!!!
    
    //Load a literal into a register
    public void SET(String instruction)
    {
        String immed = instruction.substring(11, 16);

    	int immedValue = binary_to_int(immed);
        System.out.println(instruction);
        System.out.println(immed);
        System.out.println(String.valueOf(immedValue));
        if (immedValue != 0) {
            //find out which register to store the address
            if (instruction.substring(6, 8).equals("00")) {//R0
                String value = int_to_binary_16bits(immedValue);
                registers.setR0(value);
            } else if(instruction.substring(6, 8).equals("01")){//R1
                String value = int_to_binary_16bits(immedValue);
                registers.setR1(value);
            }else if(instruction.substring(6, 8).equals("10")){//R2
                String value = int_to_binary_16bits(immedValue);
                registers.setR2(value);
            }else if(instruction.substring(6, 8).equals("11")){//R3
                String value = int_to_binary_16bits(immedValue);
                registers.setR3(value);
            }
        }
    }
    
}