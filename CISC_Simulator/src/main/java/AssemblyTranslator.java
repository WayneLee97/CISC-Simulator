
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jonathan Pritchett
 */
public class AssemblyTranslator
{
    
    private HashMap<String,Integer> instructionMap;
    private HashMap<String,Integer> complexMap;
    
    private HashMap<String,Integer> registerMap;
    private HashMap<String,Integer> indexMap;
    private HashMap<String,Integer> variableMap;
    private HashMap<String,Integer> pointerMap;
    private HashMap<String,Integer> jumpMap;
    private LinkedHashMap<Integer,Integer> jumpAddressMap;
    
    
    public AssemblyTranslator() 
    {
        instructionMap = new HashMap<String,Integer>();
        complexMap = new HashMap<String,Integer>();
        registerMap = new HashMap<String,Integer>();
        indexMap = new HashMap<String,Integer>();
        variableMap = new HashMap<String,Integer>();
        pointerMap = new HashMap<String,Integer>();
        jumpMap = new HashMap<String,Integer>();
        jumpAddressMap = new LinkedHashMap<Integer,Integer>();
        
        
        instructionMap.put("LDR", 1);
        instructionMap.put("STR", 2);
        instructionMap.put("LDA", 3);
        instructionMap.put("LDX", 41);
        instructionMap.put("STX", 42);
        instructionMap.put("JZ", 10);
        instructionMap.put("JNE", 11);
        instructionMap.put("JCC", 12);
        instructionMap.put("JMA", 13);
        instructionMap.put("JSR", 14);
        instructionMap.put("RFS", 15);
        instructionMap.put("SOB", 16);
        instructionMap.put("JGE", 17);
        instructionMap.put("AMR", 4);
        instructionMap.put("SMR", 5);
        instructionMap.put("AIR", 6);
        instructionMap.put("SIR", 7);
        instructionMap.put("MLT", 20);
        instructionMap.put("DVD", 21);
        instructionMap.put("TRR", 22);
        instructionMap.put("AND", 23);
        instructionMap.put("ORR", 24);
        instructionMap.put("NOT", 25);
        instructionMap.put("SRC", 31);
        instructionMap.put("RRC", 32);
        instructionMap.put("IN", 61);
        instructionMap.put("OUT", 62);
        
        //Custom instructions
        
        instructionMap.put("SET",43);
        
        complexMap.put("LDRSTR",1);
        
        registerMap.put("R0", 0);
        registerMap.put("R1", 1);
        registerMap.put("R2", 2);
        registerMap.put("R3", 3);
        
        indexMap.put("X0", 0);
        indexMap.put("X1", 1);
        indexMap.put("X2", 2); //X2 is reserved for jump table
        indexMap.put("X3", 3); //X3 is reserved for variable table
        
        for(char c = 'A'; c <= 'Z'; c++)
        {
            variableMap.put(String.valueOf(c),(int)(c - 'A'));
            pointerMap.put(String.valueOf(c) + "*",(int)(c - 'A'));
        }
        jumpMap.put("START", 0);
        jumpMap.put("END",1);
        
    }
    
    
    
    public String makeSimpleInstruction(String assembly)
    {
        String output = "";
        
        String[] args = assembly.split(" ");
        
        if(!(instructionMap.containsKey(args[0])))
        {
                System.out.println("ERROR: bad instruction");
                return "0000000000000000";
        }
        

        
        String instruction = Instructions.int_to_binary_xbits(instructionMap.get(args[0]),6);
        String register = "";
        if(registerMap.containsKey(args[1]))
        {
            register = Instructions.int_to_binary_xbits(registerMap.get(args[1]), 2);
        }
        else if(indexMap.containsKey(args[1]))
        {
            register = Instructions.int_to_binary_xbits(indexMap.get(args[1]), 2);
        }
        else
        {
                System.out.println("ERROR: bad register");
                return "0000000000000000";
        }
        String index = "00";
        String indirect = "0";
        String address = "00000";
        
        if(args.length > 2)
        {
            if(args[0].equals("AIR"))
            {
                //handle immediates
                address = Instructions.int_to_binary_xbits(Integer.decode(args[2]),5);
            }
            else if(args[0].equals("SIR"))
            {
                //handle immediates
                address = Instructions.int_to_binary_xbits(Integer.decode(args[2]),5);
            }
            else if(args[0].equals("SET"))
            {
                //index = "";
                //indirect = "";
                address = Instructions.int_to_binary_xbits(Integer.decode(args[2]),5);
            }
            else if(registerMap.containsKey(args[2]))
            {
                index = Instructions.int_to_binary_xbits(registerMap.get(args[2]), 2);
            }
            else if(variableMap.containsKey(args[2])) //got a variable, infer indexing
            {
                index = Instructions.int_to_binary_xbits(indexMap.get("X3"),2);
                indirect = "0";
                address = Instructions.int_to_binary_xbits(variableMap.get(args[2]),5);
            }
            else if(pointerMap.containsKey(args[2]))
            {
                index = Instructions.int_to_binary_xbits(indexMap.get("X3"),2);
                indirect = "1";
                address = Instructions.int_to_binary_xbits(pointerMap.get(args[2]),5);
            }
            else if(indexMap.containsKey(args[2]))
            {
                index = Instructions.int_to_binary_xbits(indexMap.get(args[2]), 2);
                indirect = args[3];
                address = Instructions.int_to_binary_xbits(Integer.decode(args[4]),5);
            }
            else if(jumpMap.containsKey(args[2]))
            {
                index = Instructions.int_to_binary_xbits(indexMap.get("X3"),2);
                indirect = "1";
                address = Instructions.int_to_binary_xbits(jumpMap.get(args[2]),5);
            }

            else
            {
                //ERROR: bad input?
                System.out.println("ERROR");
                return "0000000000000000";
            }
        }
        
        output = instruction + register + index + indirect + address;
        System.out.println("Good output: " + output);
        
        return output;
    }
    
    public void loadAssembly(String filepath)
    {
        Memory memory = Memory.instance();
        Registers registers = Registers.instance();
        int currentAddress = memory.PROGRAM_START_ADDRESS;
        
        Scanner in;
        try
        {
            in = new Scanner(new File(filepath));
        }
        catch(FileNotFoundException err)
        {
            System.out.println("FILE NOT FOUND: " + filepath);
            return;
        }
        
        while(in.hasNextLine())
        {
            String nextLine = in.nextLine();
            System.out.println(nextLine);
            String[] args = nextLine.split(" ");
            if(args[0].charAt(0) == '#')
                continue;
            if(args.length > 2)
            {
                if(!variableMap.containsKey(args[2]) && !indexMap.containsKey(args[2]) && !pointerMap.containsKey(args[2]));
                {

                    if(!jumpMap.containsKey(args[2]))
                    {
                        int index = jumpAddressMap.size();
                        jumpMap.put(args[2],index);
                        jumpAddressMap.put(index, -1);
                    }
                }
            }
            
            if(complexMap.containsKey(args[0]))
            {
                switch(args[0])
                {
                    case("LDRSTR"):
                    {
                        // OPCODE - StartAddress - String

                        //

                    }
                    break;
                }
            }
            else if(instructionMap.containsKey(args[0]))
            {
                String preWrite = "";
                if(args.length > 2)
                {
                    if(variableMap.containsKey(args[2]))
                    {
                        preWrite = makeSimpleInstruction("LDX X3 X0 0 7");
                    }
                    else if(pointerMap.containsKey(args[2]))
                    {
                        preWrite = makeSimpleInstruction("LDX X3 X0 0 7");
                    }
                    else if(jumpMap.containsKey(args[2]))
                    {
                        preWrite = makeSimpleInstruction("LDX X3 X0 0 8");
                    }
                }
                if(!preWrite.equals(""))
                {
                    memory.setMemory(currentAddress++, preWrite);
                }
                String instruction = makeSimpleInstruction(nextLine);
                memory.setMemory(currentAddress++,instruction);
            }
            else
            {
                if(!jumpMap.containsKey(args[0]))
                {
                    int index = jumpAddressMap.size();
                    jumpMap.put(args[0],index);

                }
                
                jumpAddressMap.put(jumpMap.get(args[0]), currentAddress++);
                
            }
        
        } 
        for(int i = 0; i < jumpAddressMap.size();i++)
        {
            memory.setMemory(i+memory.JUMP_TABLE_ADDRESS,Instructions.int_to_binary_16bits(jumpAddressMap.get(i)));
        }
    }
}
