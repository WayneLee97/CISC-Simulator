

public class Instructions {
    public static final int OVERFLOW_MAX = 32767;
    public static final int UNDERFLOW_MIN = -32768;
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
        String binary_number = Integer.toBinaryString(number);
        if (binary_number.length() > 16) {

            return binary_number.substring(16, 32);
        } else if (binary_number.length() < 16) {
            int numberOfZero = 16 - binary_number.length();
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
        registers.setMAR(int_to_binary(EA));
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
    public void LDA(String instruction, Registers registers, Memory mm){
        //still, get the EA
        int EA = EA_calculator(instruction, registers, mm);
        //find out which register to store the address, and format the address to make become a 16-bits string
        if(instruction.substring(6, 8).equals("00")){//R0
            registers.setR0(int_to_binary(EA));
        }else if(instruction.substring(6, 8).equals("01")){//R1
            registers.setR1(int_to_binary(EA));
        }else if(instruction.substring(6, 8).equals("10")){//R2
            registers.setR2(int_to_binary(EA));
        }else if(instruction.substring(6, 8).equals("11")){//R3
            registers.setR3(int_to_binary(EA));
        }
    }

    //load index register from the memory
    public void LDX(String instruction, Registers registers, Memory mm){
        //still, get the EA
        int EA = EA_calculator(instruction, registers, mm);
        //set MAR
        registers.setMAR(int_to_binary(EA));
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
        registers.setMAR(int_to_binary(EA));
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

    //Add momory to register (r <- c(r) + c(EA))

    /**
     *
     * @param instruction
     * @param registers
     * @param mm
     *        todo: handle overflow.
     */
    public void AMR(String instruction, Registers registers, Memory mm) {
        //still, get the EA
        int EA = EA_calculator(instruction, registers, mm);
        //set MAR
        registers.setMAR(int_to_binary(EA));
        registers.setMBR(mm.getMemory(EA));
        //find out which register to store the address
        if(instruction.substring(6, 8).equals("00")){//R0
            String address = int_to_binary(binary_to_int(registers.getR0()) + binary_to_int(mm.getMemory(EA)));
            registers.setR0(address);
        }else if(instruction.substring(6, 8).equals("01")){//R1
            String address = int_to_binary(binary_to_int(registers.getR1()) + binary_to_int(mm.getMemory(EA)));
            registers.setR1(address);
        }else if(instruction.substring(6, 8).equals("10")){//R2
            String address = int_to_binary(binary_to_int(registers.getR2()) + binary_to_int(mm.getMemory(EA)));
            registers.setR2(address);
        }else if(instruction.substring(6, 8).equals("11")){//R3
            String address = int_to_binary(binary_to_int(registers.getR3()) + binary_to_int(mm.getMemory(EA)));
            registers.setR3(address);
        }
    }

    //Subtract memory from register (r <- c(r) - c(EA))
    public void SMR(String instruction, Registers registers, Memory mm) {
        //still, get the EA
        int EA = EA_calculator(instruction, registers, mm);
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
     * @param immed 5 bits String
     */
    public void AIR(String instruction, Registers registers, String immed) {
        int immedValue = binary_to_int(immed);
        if (immedValue != 0) {
            //find out which register to store the address
            if (instruction.substring(6, 8).equals("00")) {//R0
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR0()) + immedValue);
                registers.setR0(address);
            } else if(instruction.substring(6, 8).equals("01")){//R1
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR1()) + immedValue);
                registers.setR0(address);
            }else if(instruction.substring(6, 8).equals("10")){//R2
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR2()) + immedValue);
                registers.setR0(address);
            }else if(instruction.substring(6, 8).equals("11")){//R3
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR3()) + immedValue);
                registers.setR0(address);
            }
        }
    }

    //Subtract immediate from register
    /**
     *
     * @param instruction
     * @param registers
     * @param immed 5 bits String
     */
    public void SIR(String instruction, Registers registers, String immed) {
        int immedValue = binary_to_int(immed);
        if (immedValue != 0) {
            //find out which register to store the address
            if (instruction.substring(6, 8).equals("00")) {//R0
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR0()) - immedValue);
                registers.setR0(address);
            } else if(instruction.substring(6, 8).equals("01")){//R1
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR1()) - immedValue);
                registers.setR0(address);
            }else if(instruction.substring(6, 8).equals("10")){//R2
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR2()) - immedValue);
                registers.setR0(address);
            }else if(instruction.substring(6, 8).equals("11")){//R3
                String address = int_to_binary_16bits(binary_to_int_16bits(registers.getR3()) - immedValue);
                registers.setR0(address);
            }
        }
    }

    /**
     *
     * @param instruction
     * @param registers
     *      Multiply Register by Register
     */
    public void MLT(String instruction, Registers registers) {
        String rx = instruction.substring(6, 8);
        String ry = instruction.substring(8, 10);
        if (rx == "00") {
            if (ry == "00") {
                int result = binary_to_int_16bits(registers.getR0()) * binary_to_int_16bits(registers.getR0());
                registers.setR0(int_to_binary_16bits(result));
                if (checkOverUnderFlow(registers, result)) {
                    registers.setR1(getHighOrderBits(result));
                }
            } else if (ry == "10") {
                int result = binary_to_int_16bits(registers.getR0()) * binary_to_int_16bits(registers.getR2());
                registers.setR0(int_to_binary_16bits(result));
                if (checkOverUnderFlow(registers, result)) {
                    registers.setR1(getHighOrderBits(result));
                }
            }
        } else if (rx == "10") {
            if (ry == "00") {
                int result = binary_to_int_16bits(registers.getR2()) * binary_to_int_16bits(registers.getR0());
                registers.setR2(int_to_binary_16bits(result));
                if (checkOverUnderFlow(registers, result)) {
                    registers.setR3(getHighOrderBits(result));
                }
            } else if (ry == "10") {
                int result = binary_to_int_16bits(registers.getR2()) * binary_to_int_16bits(registers.getR2());
                registers.setR2(int_to_binary_16bits(result));
                if (checkOverUnderFlow(registers, result)) {
                    registers.setR3(getHighOrderBits(result));
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
    public void DVD(String instruction, Registers registers) {
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
    //Logical And of register and register c(rx) <- c(rx) AND c(ry)
    /**
     *
     * @param instruction (6-8): Rx
     *                    (8-10) Ry
     */
    public void AND(String instruction, Registers registers) {
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
    public void ORR(String instruction, Registers registers) {
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
    public void NOT(String instruction, Registers registers) {
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
}
