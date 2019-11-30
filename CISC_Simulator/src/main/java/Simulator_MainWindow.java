
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JFormattedTextField;

import java.util.Timer;
import java.util.TimerTask;
import javafx.stage.FileChooser;
import javax.swing.JFileChooser;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Jonathan Pritchett
 */
class ClockCycler extends TimerTask
{

    private Simulator_MainWindow parent;

    ClockCycler(Simulator_MainWindow parent)
    {
        this.parent = parent;
    }

    public void run()
    {
        parent.cycle();
    }
}

class PipedInstruction
{
    public String instruction = "";
    public int address = -1;
    public int opCode = 0;
    public int register = -1;
    
    public boolean isJump = false;
    public int EA = -1;

    PipedInstruction(String instruct, int adr)
    {
        instruction = instruct;
        address = adr;
        initialize();
    }
    
    private void initialize()
    {
        opCode = Instructions.instance().binary_to_int(instruction.substring(0,6));
        register = Instructions.instance().binary_to_int(instruction.substring(6,8));
        if(opCode >= 10 && opCode <= 17 )
            isJump = true;
        EA = Instructions.instance().EA_calculator(instruction);
        
        
    }
    
    
    
}

public class Simulator_MainWindow extends javax.swing.JFrame
{
    
    static final String ZEROED_REGISTER = "0000000000000000";
    static final int CYCLE_TIME = 1;//made change here to get faster clock cycle

    private InputType inputType = InputType.BINARY;

    private ArrayList<JFormattedTextField> wordFields;

    private Registers registers;
    private Memory memory;
    private Instructions instructions;
    private IOHandler io;

    private int cycleCount = 0;
    private Boolean p1_active = false;
    private Boolean p1_firstTime = true;
    private Boolean p2_active = false;
    
    private PipedInstruction IF;
    private PipedInstruction ID;
    private PipedInstruction EX;
    private PipedInstruction MEM;
    
    
    
    //private Map<JFormattedTextField,Word> wordMap;
    public static int op_translator(String S_instruction, Instructions instruction)
    {
        String op_code = S_instruction.substring(0, 6);
        return instruction.binary_to_int(op_code);
    }
    

    /**
     * Creates new form Simulator_MainWindow
     */
    public Simulator_MainWindow()
    {
        initComponents();

        registers = Registers.instance();
        memory = Memory.instance();
        instructions = Instructions.instance();
        io = IOHandler.instance();

        for (int i = 0; i < memory.MEMORY_SIZE; i++)
        {
            memory.setMemory(i, ZEROED_REGISTER);
        }

        wordFields = new ArrayList<JFormattedTextField>();
        //Add fields to list and map
        wordFields.add(R0_Field);
        wordFields.add(R1_Field);
        wordFields.add(R2_Field);
        wordFields.add(R3_Field);

        wordFields.add(X1_Field);
        wordFields.add(X2_Field);
        wordFields.add(X3_Field);

        wordFields.add(PC_Field);
        wordFields.add(CC_Field);
        wordFields.add(IR_Field);
        wordFields.add(MAR_Field);
        wordFields.add(MBR_Field);
        wordFields.add(MFR_Field);

        wordFields.add(Address_Field);
        wordFields.add(Value_Field);

        for (JFormattedTextField field : wordFields)
        {
            field.setText(ZEROED_REGISTER);
            //field.setEnabled(false);
        }
        OverrideButtonActionPerformed(null);

        this.setInputType(inputType.BINARY);
        //this.cycle();
        Timer timer = new Timer();
        ClockCycler cycler = new ClockCycler(this);
        timer.schedule(cycler, CYCLE_TIME);

        IF = null;
        ID = null;
        EX = null;
        MEM = null;
        
    }

    private void setInputType(InputType type)
    {
        if (this.inputType != type)
        {
            String oldAddress = Address_Field.getText();
            String newAddress = "";
            if (type == InputType.BINARY)
            {
                newAddress = intToBinaryString(Integer.decode(oldAddress));
            }
            else if (type == InputType.DECIMAL)
            {
                newAddress = binaryToIntString(oldAddress);
            }
            Address_Field.setText(newAddress);
        }
        this.inputType = type;
        updateDisplay();
    }

    private String binaryToIntString(String binary)
    {
        return Instructions.binary_to_int(binary).toString();
    }

    private String intToBinaryString(int intval)
    {
        return intToBinaryString(intval,16);
    }
    private String intToBinaryString(int intval, int numBits)
    {
        return Instructions.int_to_binary_xbits(intval, numBits);
    }

    private String decimalStringtoBinaryString(String decimal)
    {

        String binary = intToBinaryString(Integer.decode(decimal));

        return binary;
    }

    public void updateDisplay()
    {
        
        
        //update pipelinedisplay
        String pipeline_text = "STAGE\tINSTRUCTION\tADDRESS\n";

        if(IF != null)
        {
            if(inputType == inputType.BINARY)
            {
                pipeline_text = pipeline_text.concat(" IF:\t" + IF.instruction + "\t" + IF.address + "\n");
            }
            else
            {
                pipeline_text = pipeline_text.concat(" IF:\t" + binaryToIntString(IF.instruction) + "\t" + IF.address + "\n");
            }
        }
        else
        {
            pipeline_text = pipeline_text.concat(" IF:\t" + "NULL" + "\t" + "NULL" + "\n");
        }
        
        if(ID != null)
        {
            if(inputType == inputType.BINARY)
            {
                pipeline_text = pipeline_text.concat(" ID:\t" + ID.instruction + "\t" + ID.address + "\n");
            }
            else
            {
                pipeline_text = pipeline_text.concat(" ID:\t" + binaryToIntString(ID.instruction) + "\t" + ID.address + "\n");
            }
        }
        else
        {
            pipeline_text = pipeline_text.concat(" ID:\t" + "NULL" + "\t" + "NULL" + "\n");
        }

        if(EX != null)
        {
            if(inputType == inputType.BINARY)
            {
                pipeline_text = pipeline_text.concat(" EX:\t" + EX.instruction + "\t" + EX.address + "\n");
            }
            else
            {
                pipeline_text = pipeline_text.concat(" EX:\t" + binaryToIntString(EX.instruction) + "\t" + EX.address + "\n");
            }
        }
        else
        {
            pipeline_text = pipeline_text.concat(" EX:\t" + "NULL" + "\t" + "NULL" + "\n");
        } 
        
        if(MEM != null)
        {
            if(inputType == inputType.BINARY)
            {
                pipeline_text = pipeline_text.concat("MEM:\t" + MEM.instruction + "\t" + MEM.address + "\n");
            }
            else
            {
                pipeline_text = pipeline_text.concat("MEM:\t" + binaryToIntString(MEM.instruction) + "\t" + MEM.address + "\n");
            }
        }
        else
        {
            pipeline_text = pipeline_text.concat("MEM:\t" + "NULL" + "\t" + "NULL" + "\n");
        }
        pipeline_area.setText(pipeline_text);

                
        
        //update memory cache display
        String cache_text = "";
        for (int i = 0; i < memory.MAX_CACHE; i++)
        {
            cache_text = cache_text.concat((i + 1) + ":\t");
            if (i < memory.getCache().size())
            {
                if (inputType == InputType.BINARY)
                {
                    cache_text = cache_text.concat(intToBinaryString((Integer) memory.getCache().keySet().toArray()[i]) + "\t");
                    cache_text = cache_text.concat(memory.getCache().values().toArray()[i].toString());
                }
                else if (inputType == InputType.DECIMAL)
                {
                    cache_text = cache_text.concat(memory.getCache().keySet().toArray()[i] + "\t");
                    cache_text = cache_text.concat(binaryToIntString(memory.getCache().values().toArray()[i].toString()));
                }
            }
            cache_text = cache_text.concat("\n");
        }
        cache_area.setText(cache_text);

        //update register display
        if (inputType == InputType.BINARY)
        {
            R0_Field.setText(registers.getR0());
            R1_Field.setText(registers.getR1());
            R2_Field.setText(registers.getR2());
            R3_Field.setText(registers.getR3());

            X1_Field.setText(registers.getX1());
            X2_Field.setText(registers.getX2());
            X3_Field.setText(registers.getX3());

            PC_Field.setText(registers.getPC());
            //CC_Field.setText(registers.getCC());
            IR_Field.setText(registers.getIR());
            MBR_Field.setText(registers.getMBR());
            MAR_Field.setText(registers.getMAR());
            MFR_Field.setText(registers.getMFR());

            int memoryAddress = instructions.binary_to_int(Address_Field.getText());
            Value_Field.setText(memory.getMemory(memoryAddress));

        }
        else if (inputType == InputType.DECIMAL)
        {
            R0_Field.setText(binaryToIntString(registers.getR0()));
            R1_Field.setText(binaryToIntString(registers.getR1()));
            R2_Field.setText(binaryToIntString(registers.getR2()));
            R3_Field.setText(binaryToIntString(registers.getR3()));

            X1_Field.setText(binaryToIntString(registers.getX1()));
            X2_Field.setText(binaryToIntString(registers.getX2()));
            X3_Field.setText(binaryToIntString(registers.getX3()));

            PC_Field.setText(binaryToIntString(registers.getPC()));
            //CC_Field.setText(binaryToIntString(registers.getCC());
            IR_Field.setText(binaryToIntString(registers.getIR()));
            MBR_Field.setText(binaryToIntString(registers.getMBR()));
            MAR_Field.setText(binaryToIntString(registers.getMAR()));
            MFR_Field.setText(binaryToIntString(registers.getMFR()));

            int memoryAddress = Integer.decode(Address_Field.getText());
            Value_Field.setText(binaryToIntString(memory.getMemory(memoryAddress)));
        }
        
        
        
    }

    public void cycle()
    {
        


        //System.out.print(memory.printCache());
        //a variable keep the trace whether PC has been changed inside of instructions
        boolean PC_changed = false;
        //implement the instruction for this step
        //if (!this.memory.getMemory(instructions.binary_to_int(registers.getPC())).equals("0000000000000000"))
        {
            //execute the instruction,but first use the translator function to get the operation code
            //int op_code = op_translator(memory.getMemory(instructions.binary_to_int(registers.getPC())), instructions);
            int op_code = 0;
            //advance pipeline
            if(MEM != null)
            {
                if(MEM.instruction.equals(ZEROED_REGISTER))
                    return;
                op_code = MEM.opCode;
                
                if(MEM.isJump)
                {
                    registers.setPC(instructions.int_to_binary_16bits(MEM.address+1));
                    PC_changed = true;
                    EX = null;
                    ID = null;
                    IF = null;
                }
                else
                {
                    if(EX != null && (MEM.register == EX.register || MEM.EA == EX.EA))
                    {
                        registers.setPC(instructions.int_to_binary_16bits(MEM.address+1));
                        PC_changed = true;
                        EX = null;
                        ID = null;
                        IF = null;
                        
                    }
                }
                switch (op_code)
                {
                    //load register from the memory, LDR
                    case 1:
                        registers.setIR(MEM.instruction);
                        instructions.LDR(MEM.instruction);
                        break;
                    //store register from memory, STR
                    case 2:
                        registers.setIR(MEM.instruction);
                        instructions.STR(MEM.instruction);
                        break;
                    //load register with address, LDA
                    case 3:
                        registers.setIR(MEM.instruction);
                        instructions.LDA(MEM.instruction);
                        break;
                    //load index register from the memory, LDX
                    case 41:
                        registers.setIR(MEM.instruction);
                        instructions.LDX(MEM.instruction);
                        break;
                    //store index register to memory, STX
                    case 42:
                        registers.setIR(MEM.instruction);
                        instructions.STX(MEM.instruction);
                        break;
                    //jump if zero
                    case 10:
                        registers.setIR(MEM.instruction);
                        instructions.JZ(MEM.instruction);
                        //the pc has changed inside, so we set PC_changed  = true
                        PC_changed = true;
                        break;
                    //jump if not equal
                    case 11:
                        registers.setIR(MEM.instruction);
                        instructions.JNE(MEM.instruction);
                        //the pc has changed inside, so we set PC_changed  = true
                        PC_changed = true;
                        break;
                    //jump if condition code
                    case 12:
                        registers.setIR(MEM.instruction);
                        instructions.JCC(MEM.instruction);
                        //the pc has changed inside, so we set PC_changed  = true
                        PC_changed = true;
                        break;
                    //unconditional jump to address
                    case 13:
                        registers.setIR(MEM.instruction);
                        instructions.JMA(MEM.instruction);
                        //the pc has changed inside, so we set PC_changed  = true
                        PC_changed = true;
                        break;
                    //jump and save return address
                    case 14:
                        registers.setIR(MEM.instruction);
                        instructions.JSR(MEM.instruction);
                        //the pc has changed inside, so we set PC_changed  = true
                        PC_changed = true;
                        break;
                    //return from subroutine with return code as immediate portion stored in the instructions' address field
                    case 15:
                        registers.setIR(MEM.instruction);
                        instructions.RFS(MEM.instruction);
                        //the pc has changed inside, so we set PC_changed  = true
                        PC_changed = true;
                        break;
                    //subtract one and branch
                    case 16:
                        registers.setIR(MEM.instruction);
                        instructions.SOB(MEM.instruction);
                        //the pc has changed inside, so we set PC_changed  = true
                        PC_changed = true;
                        break;
                    //jump greater than or equal to
                    case 17:
                        registers.setIR(MEM.instruction);
                        instructions.JGE(MEM.instruction);
                        //the pc has changed inside, so we set PC_changed  = true
                        PC_changed = true;
                        break;
                    //Add Memory To Register
                    case 4:
                        registers.setIR(MEM.instruction);
                        instructions.AMR(MEM.instruction);
                        break;
                    //Subtract Memory From Register
                    case 5:
                        registers.setIR(MEM.instruction);
                        instructions.SMR(MEM.instruction);
                        break;
                    //Add  Immediate to Register
                    case 6:
                        registers.setIR(MEM.instruction);
                        instructions.AIR(MEM.instruction);
                        break;
                    //Subtract  Immediate  from Register
                    case 7:
                        registers.setIR(MEM.instruction);
                        instructions.SIR(MEM.instruction);
                        break;
                    //Multiply Register by Register
                    case 20:
                        registers.setIR(MEM.instruction);
                        instructions.MLT(MEM.instruction);
                        break;
                    //Divide Register by Register
                    case 21:
                        registers.setIR(MEM.instruction);
                        instructions.DVD(MEM.instruction);
                        break;
                    //Test the Equality of Register and Register
                    case 22:
                        registers.setIR(MEM.instruction);
                        instructions.TRR(MEM.instruction);
                        break;
                    //Logical And of Register and Register
                    case 23:
                        registers.setIR(MEM.instruction);
                        instructions.AND(MEM.instruction);
                        break;
                    //Logical Or of Register and Register
                    case 24:
                        registers.setIR(MEM.instruction);
                        instructions.ORR(MEM.instruction);
                        break;
                    //Logical Not of Register and Register
                    case 25:
                        registers.setIR(MEM.instruction);
                        instructions.NOT(MEM.instruction);
                        break;
                    //Shift Register by Count
                    case 31:
                        registers.setIR(MEM.instruction);
                        instructions.SRC(MEM.instruction);
                        break;
                    //Rotate Register by Count
                    case 32:
                        registers.setIR(MEM.instruction);
                        instructions.RRC(MEM.instruction);
                        break;
                    //Input Character To Register from Device
                    case 61:
                        registers.setIR(MEM.instruction);
                        instructions.IN(MEM.instruction);
                        break;
                    //Output Character To Register from Device
                    case 62:
                        registers.setIR(MEM.instruction);
                        instructions.OUT(MEM.instruction);
                        break;
                    case 43:
                        registers.setIR(MEM.instruction);
                        instructions.SET(MEM.instruction);
                        break;
                    default:
                        break;
                }
                
                MEM = null;
            }
            //if(EX != null)
            {
                MEM = EX;
            }
            //if(ID != null)
            {
                EX = ID;
            }
            //if(IF != null)
            {
                ID = IF;
            }
            
                //go to the next instruction
            if (!PC_changed)
            {
                int next_address = instructions.binary_to_int(registers.getPC()) + 1;
                registers.setPC(instructions.int_to_binary_16bits(next_address));
            }
            int address = instructions.binary_to_int(registers.getPC());
            IF = new PipedInstruction(memory.getMemory(address),address);
            
            
            
        }

    

        String output = "";
        while (io.hasOutput())
        {
            output += io.getNextOutput();
        }
        io_area.append(output);
        updateDisplay();
        
        if (RunCheckBox.isSelected())
        {
        	if(!registers.getPC().equals("0000000000001110")){
        		Timer timer = new Timer();
        		ClockCycler cycler = new ClockCycler(this);
        		timer.schedule(cycler, CYCLE_TIME);
        	}
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        RunCheckBox = new javax.swing.JCheckBox();
        StepButton = new javax.swing.JButton();
        AddressLabel = new javax.swing.JLabel();
        Value_Label = new javax.swing.JLabel();
        Address_Field = new javax.swing.JFormattedTextField();
        Value_Field = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        InputType_Box = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        OverrideButton = new javax.swing.JButton();
        Override_Checkbox = new javax.swing.JCheckBox();
        LoadTest_Button = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        X3_Label = new javax.swing.JLabel();
        X2_Label = new javax.swing.JLabel();
        X2_Field = new javax.swing.JFormattedTextField();
        X3_Field = new javax.swing.JFormattedTextField();
        X1_Field = new javax.swing.JFormattedTextField();
        PC_Label = new javax.swing.JLabel();
        CC_Label = new javax.swing.JLabel();
        PC_Field = new javax.swing.JFormattedTextField();
        CC_Field = new javax.swing.JFormattedTextField();
        MBR_FIeld = new javax.swing.JLabel();
        R3_Label = new javax.swing.JLabel();
        R2_Label = new javax.swing.JLabel();
        MBR_Field = new javax.swing.JFormattedTextField();
        R2_Field = new javax.swing.JFormattedTextField();
        R3_Field = new javax.swing.JFormattedTextField();
        IR_Label = new javax.swing.JLabel();
        MAR_Label = new javax.swing.JLabel();
        IR_Field = new javax.swing.JFormattedTextField();
        MAR_Field = new javax.swing.JFormattedTextField();
        R0_Label = new javax.swing.JLabel();
        R0_Field = new javax.swing.JFormattedTextField();
        R1_Field = new javax.swing.JFormattedTextField();
        R1_Label = new javax.swing.JLabel();
        X1_Label = new javax.swing.JLabel();
        MFR_Label = new javax.swing.JLabel();
        MFR_Field = new javax.swing.JFormattedTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cache_area = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        pipeline_area = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        io_area = new javax.swing.JTextArea();
        load_program1_button = new javax.swing.JButton();
        load_program2_button = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        RunCheckBox.setText("AutoRun?");
        RunCheckBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                RunCheckBoxActionPerformed(evt);
            }
        });

        StepButton.setText("Step");
        StepButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                StepButtonActionPerformed(evt);
            }
        });

        AddressLabel.setText("Address");

        Value_Label.setText("Value");

        Address_Field.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        Address_Field.setText("1234567890123456");
        Address_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                Address_FieldActionPerformed(evt);
            }
        });

        Value_Field.setText("1234567890123456");
        Value_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                Value_FieldActionPerformed(evt);
            }
        });

        jLabel3.setText("Memory");

        InputType_Box.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "binary", "decimal" }));
        InputType_Box.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                InputType_BoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Input Mode");

        OverrideButton.setText("Override All Values");
        OverrideButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                OverrideButtonActionPerformed(evt);
            }
        });

        Override_Checkbox.setText("Lock Override");
        Override_Checkbox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                Override_CheckboxActionPerformed(evt);
            }
        });

        LoadTest_Button.setText("Load test program");
        LoadTest_Button.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                LoadTest_ButtonActionPerformed(evt);
            }
        });

        X3_Label.setText("X3");

        X2_Label.setText("X2");

        X2_Field.setText("1234567890123456");
        X2_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                X2_FieldActionPerformed(evt);
            }
        });

        X3_Field.setText("1234567890123456");
        X3_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                X3_FieldActionPerformed(evt);
            }
        });

        X1_Field.setText("1234567890123456");
        X1_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                X1_FieldActionPerformed(evt);
            }
        });

        PC_Label.setText("PC");

        CC_Label.setText("CC");

        PC_Field.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        PC_Field.setText("1234567890123456");
        PC_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                PC_FieldActionPerformed(evt);
            }
        });

        CC_Field.setText("1234567890123456");
        CC_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                CC_FieldActionPerformed(evt);
            }
        });

        MBR_FIeld.setText("MBR");

        R3_Label.setText("R3");

        R2_Label.setText("R2");

        MBR_Field.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        MBR_Field.setText("1234567890123456");
        MBR_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                MBR_FieldActionPerformed(evt);
            }
        });

        R2_Field.setText("1234567890123456");
        R2_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                R2_FieldActionPerformed(evt);
            }
        });

        R3_Field.setText("1234567890123456");
        R3_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                R3_FieldActionPerformed(evt);
            }
        });

        IR_Label.setText("IR");

        MAR_Label.setText("MAR");

        IR_Field.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0"))));
        IR_Field.setText("1234567890123456");
        IR_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                IR_FieldActionPerformed(evt);
            }
        });

        MAR_Field.setText("1234567890123456");
        MAR_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                MAR_FieldActionPerformed(evt);
            }
        });

        R0_Label.setText("R0");

        R0_Field.setText("1234567890123456");
        R0_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                R0_FieldActionPerformed(evt);
            }
        });

        R1_Field.setText("1234567890123456");
        R1_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                R1_FieldActionPerformed(evt);
            }
        });

        R1_Label.setText("R1");

        X1_Label.setText("X1");

        MFR_Label.setText("MFR");

        MFR_Field.setText("1234567890123456");
        MFR_Field.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                MFR_FieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(R0_Label)
                    .addComponent(R1_Label)
                    .addComponent(R2_Label)
                    .addComponent(R3_Label)
                    .addComponent(X1_Label)
                    .addComponent(X2_Label)
                    .addComponent(X3_Label)
                    .addComponent(PC_Label)
                    .addComponent(CC_Label)
                    .addComponent(IR_Label)
                    .addComponent(MAR_Label)
                    .addComponent(MBR_FIeld)
                    .addComponent(MFR_Label))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(R0_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(R1_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(R2_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(R3_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(X1_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(X2_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(X3_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PC_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CC_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(IR_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MAR_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MBR_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MFR_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(295, 295, 295))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {CC_Field, IR_Field, MAR_Field, MBR_Field, MFR_Field, PC_Field, R0_Field, R1_Field, R2_Field, R3_Field, X1_Field, X2_Field, X3_Field});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(R0_Label)
                    .addComponent(R0_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(R1_Label)
                    .addComponent(R1_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(R2_Label)
                    .addComponent(R2_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(R3_Label)
                    .addComponent(R3_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(X1_Label)
                    .addComponent(X1_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(X2_Label)
                    .addComponent(X2_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(X3_Label)
                    .addComponent(X3_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PC_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PC_Label))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CC_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CC_Label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(IR_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(IR_Label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MAR_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MAR_Label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MBR_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MBR_FIeld))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MFR_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MFR_Label))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Registers", jPanel1);

        cache_area.setColumns(20);
        cache_area.setRows(5);
        jScrollPane1.setViewportView(cache_area);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(319, 319, 319))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Cache", jPanel2);

        pipeline_area.setColumns(20);
        pipeline_area.setRows(5);
        jScrollPane3.setViewportView(pipeline_area);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(328, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Pipeline", jPanel3);

        jScrollPane2.setBackground(new java.awt.Color(0, 0, 0));

        io_area.setBackground(new java.awt.Color(0, 0, 0));
        io_area.setColumns(20);
        io_area.setForeground(new java.awt.Color(255, 255, 0));
        io_area.setLineWrap(true);
        io_area.setRows(5);
        io_area.setWrapStyleWord(true);
        io_area.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyTyped(java.awt.event.KeyEvent evt)
            {
                io_areaKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(io_area);

        load_program1_button.setText("Load Program 1");
        load_program1_button.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                load_program1_buttonActionPerformed(evt);
            }
        });

        load_program2_button.setText("Load Program 2");
        load_program2_button.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                load_program2_buttonActionPerformed(evt);
            }
        });

        jLabel2.setText("Operator Console");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(InputType_Box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addComponent(OverrideButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Override_Checkbox)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addGap(65, 65, 65))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(Value_Label)
                                                .addGap(12, 12, 12))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(AddressLabel)
                                                .addGap(18, 18, 18)))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(Value_Field)
                                            .addComponent(Address_Field))))
                                .addGap(145, 145, 145))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(LoadTest_Button)
                                        .addGap(51, 51, 51)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(load_program2_button)
                                            .addComponent(load_program1_button)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(RunCheckBox)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(StepButton))))
                                .addGap(16, 16, 16)))
                        .addGap(27, 27, 27))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(InputType_Box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(OverrideButton)
                            .addComponent(Override_Checkbox)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Address_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AddressLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Value_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Value_Label))
                        .addGap(121, 121, 121)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(LoadTest_Button)
                            .addComponent(load_program1_button))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(load_program2_button)
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(RunCheckBox)
                            .addComponent(StepButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(67, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void RunCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_RunCheckBoxActionPerformed
    {//GEN-HEADEREND:event_RunCheckBoxActionPerformed
        // TODO add your handling code here:
        if(RunCheckBox.isSelected())
        {
            StepButton.setEnabled(false);
            cycle();
        }
        else
        {
            StepButton.setEnabled(true);
        }
        
        
    }//GEN-LAST:event_RunCheckBoxActionPerformed

    private void StepButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_StepButtonActionPerformed
    {//GEN-HEADEREND:event_StepButtonActionPerformed
        // TODO add your handling code here:
        if(StepButton.isEnabled())
        {
            this.cycle();
        }
    }//GEN-LAST:event_StepButtonActionPerformed

    private void R0_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_R0_FieldActionPerformed
    {//GEN-HEADEREND:event_R0_FieldActionPerformed
        // TODO add your handling code here:
       
    }//GEN-LAST:event_R0_FieldActionPerformed

    private void R1_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_R1_FieldActionPerformed
    {//GEN-HEADEREND:event_R1_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_R1_FieldActionPerformed

    private void R2_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_R2_FieldActionPerformed
    {//GEN-HEADEREND:event_R2_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_R2_FieldActionPerformed

    private void R3_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_R3_FieldActionPerformed
    {//GEN-HEADEREND:event_R3_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_R3_FieldActionPerformed

    private void Address_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_Address_FieldActionPerformed
    {//GEN-HEADEREND:event_Address_FieldActionPerformed
        // TODO add your handling code here:
        updateDisplay();
        
    }//GEN-LAST:event_Address_FieldActionPerformed

    private void Value_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_Value_FieldActionPerformed
    {//GEN-HEADEREND:event_Value_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Value_FieldActionPerformed

    private void InputType_BoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_InputType_BoxActionPerformed
    {//GEN-HEADEREND:event_InputType_BoxActionPerformed
        // TODO add your handling code here:
        if(InputType_Box.getSelectedIndex() == 0)//binary
        {
            setInputType(inputType.BINARY);
        }
        else if(InputType_Box.getSelectedIndex() == 1)//decimal
        {
            setInputType(inputType.DECIMAL);
        }
    }//GEN-LAST:event_InputType_BoxActionPerformed

    private void X2_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_X2_FieldActionPerformed
    {//GEN-HEADEREND:event_X2_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_X2_FieldActionPerformed

    private void X3_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_X3_FieldActionPerformed
    {//GEN-HEADEREND:event_X3_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_X3_FieldActionPerformed

    private void X1_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_X1_FieldActionPerformed
    {//GEN-HEADEREND:event_X1_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_X1_FieldActionPerformed

    private void PC_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_PC_FieldActionPerformed
    {//GEN-HEADEREND:event_PC_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PC_FieldActionPerformed

    private void CC_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_CC_FieldActionPerformed
    {//GEN-HEADEREND:event_CC_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CC_FieldActionPerformed

    private void MBR_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_MBR_FieldActionPerformed
    {//GEN-HEADEREND:event_MBR_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MBR_FieldActionPerformed

    private void MFR_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_MFR_FieldActionPerformed
    {//GEN-HEADEREND:event_MFR_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MFR_FieldActionPerformed

    private void IR_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_IR_FieldActionPerformed
    {//GEN-HEADEREND:event_IR_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_IR_FieldActionPerformed

    private void MAR_FieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_MAR_FieldActionPerformed
    {//GEN-HEADEREND:event_MAR_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MAR_FieldActionPerformed

    private void OverrideButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_OverrideButtonActionPerformed
    {//GEN-HEADEREND:event_OverrideButtonActionPerformed
        // TODO add your handling code here:
        

        
        if(OverrideButton.isEnabled())
        {
            if(inputType == InputType.BINARY)
            {
                registers.setR0(R0_Field.getText());
                registers.setR1(R1_Field.getText());
                registers.setR2(R2_Field.getText());
                registers.setR3(R3_Field.getText());

                registers.setX1(X1_Field.getText());
                registers.setX2(X2_Field.getText());
                registers.setX3(X3_Field.getText());

                registers.setPC(PC_Field.getText());
                //registers.setCC(CC_Field.getText());
                registers.setIR(IR_Field.getText());
                registers.setMBR(MBR_Field.getText());
                registers.setMAR(MAR_Field.getText());
                registers.setMFR(MFR_Field.getText());
                



                memory.setMemory(instructions.binary_to_int(Address_Field.getText()), Value_Field.getText());

            }
             else if(inputType == InputType.DECIMAL)
            {
                registers.setR0(decimalStringtoBinaryString(R0_Field.getText()));
                registers.setR1(decimalStringtoBinaryString(R1_Field.getText()));
                registers.setR2(decimalStringtoBinaryString(R2_Field.getText()));
                registers.setR3(decimalStringtoBinaryString(R3_Field.getText()));

                registers.setX1(decimalStringtoBinaryString(X1_Field.getText()));
                registers.setX2(decimalStringtoBinaryString(X2_Field.getText()));
                registers.setX3(decimalStringtoBinaryString(X3_Field.getText()));

                registers.setPC(decimalStringtoBinaryString(PC_Field.getText()));
                //registers.setCC(decimalStringtoBinaryString(CC_Field.getText()));
                registers.setIR(decimalStringtoBinaryString(IR_Field.getText()));
                registers.setMBR(decimalStringtoBinaryString(MBR_Field.getText()));
                registers.setMAR(decimalStringtoBinaryString(MAR_Field.getText()));
                registers.setMFR(decimalStringtoBinaryString(MFR_Field.getText()));

                int memoryAddress = Integer.decode(Address_Field.getText());
                String memoryValue = intToBinaryString(Integer.decode(Value_Field.getText()));
                memory.setMemory(memoryAddress, memoryValue);
            }
        }
       
        
    }//GEN-LAST:event_OverrideButtonActionPerformed

    private void Override_CheckboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_Override_CheckboxActionPerformed
    {//GEN-HEADEREND:event_Override_CheckboxActionPerformed
        // TODO add your handling code here:
        if(Override_Checkbox.isSelected())
        {
            OverrideButton.setEnabled(false);
            for(JFormattedTextField field : wordFields)
            {
                field.setEnabled(false);
            }
        }
        else
        {
            OverrideButton.setEnabled(true);
            for(JFormattedTextField field : wordFields)
            {
                field.setEnabled(true);
            }
        }
    }//GEN-LAST:event_Override_CheckboxActionPerformed

    private void LoadTest_ButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_LoadTest_ButtonActionPerformed
    {//GEN-HEADEREND:event_LoadTest_ButtonActionPerformed
        // TODO add your handling code here:
        
        //load instructions into memory starting after address 6
        //set PC to 10
        
    	//this is the test program for this project, including all addressing modes. 
    	//initialization
    	/*
    	 * mem[31] = 29, mem[30] = 31, mem[29] = 30, mem[28] = 30, IX[1] = 1, IX[2] = 2
    	 * */
        IF = null;
        ID = null;
        EX = null;
        MEM = null;
        
    	memory.setMemory(31, "0000000000011101");
    	memory.setMemory(30, "0000000000011111");
    	memory.setMemory(29, "0000000000011110");
    	memory.setMemory(28, "0000000000011110");
    	registers.setX1("0000000000000001");
    	registers.setX2("0000000000000010");
    	//first, the LDR, R0 = 30
    	memory.setMemory(10, "0000010001111110");
    	//then, the STR, mem[28] = 30
    	memory.setMemory(11, "0000100000011100");
    	//next, the LDA, R1 = 29
    	memory.setMemory(12, "0000110101111101");
    	//next, the LDX, X2 = 29
    	memory.setMemory(13, "1010010010111100");
    	//last, the STX, mem[29] = 29
    	memory.setMemory(14, "1010100010000000");
    	registers.setPC("0000000000001010");
    }//GEN-LAST:event_LoadTest_ButtonActionPerformed


    
    private void user_input_areaKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_user_input_areaKeyTyped
    {//GEN-HEADEREND:event_user_input_areaKeyTyped
        // TODO add your handling code here:
        //System.out.print(evt.getKeyChar());
        io.pushInput(Character.toString(evt.getKeyChar()));
        
        // "\nEnter a number : "
        // "\nNumber list = {"
        // "}"
        // "\nEnter a number to search the list for : "
        //
        
        
        
    }//GEN-LAST:event_user_input_areaKeyTyped

    private void io_areaKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_io_areaKeyTyped
    {//GEN-HEADEREND:event_io_areaKeyTyped
        // TODO add your handling code here:
        if(!p2_active)
        {
            if(p1_active)
            {           
                p1_active = false;
            }
            if(evt.getKeyChar() == '\n')
            {
                p1_active = true;

                io_area.append("\nType a number press enter: ");

            }
        }
        //System.out.print(evt.getKeyChar());
        io.pushInput(Character.toString(evt.getKeyChar()));
        
    }//GEN-LAST:event_io_areaKeyTyped

    private void load_program2_buttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_load_program2_buttonActionPerformed
    {//GEN-HEADEREND:event_load_program2_buttonActionPerformed

        IF = null;
        ID = null;
        EX = null;
        MEM = null;
        for (int i = 0; i < memory.MEMORY_SIZE; i++)
        {
            memory.setMemory(i, ZEROED_REGISTER);
        }

        for (JFormattedTextField field : wordFields)
        {
            field.setText(ZEROED_REGISTER);
            //field.setEnabled(false);
        }
        
        io_area.setText("");
        
        
        p1_active = false;
        p1_firstTime = false;
        p2_active = true;
        
        
//        AssemblyTranslator translator = new AssemblyTranslator();
//        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
//        int retVal = chooser.showDialog(this,"Open Program file");
//        String filepath = chooser.getSelectedFile().getPath();
        
        JFileChooser chooser2 = new JFileChooser(System.getProperty("user.dir"));
        int retVal2 = chooser2.showDialog(this,"Open Paragraph file");
        String filepath2 = chooser2.getSelectedFile().getPath();
        
//        System.out.println(filepath);
        System.out.println(filepath2);
//        translator.loadAssembly(filepath);
        
//        registers.setPC(Instructions.int_to_binary_16bits(255));
        registers.setPC("0000011010100100");//Set PC to 1700
    	registers.setX1("0000011101100010");//X1=1890
    	registers.setX2("0000010111011100");//X2=1500
    	registers.setX3("0000011010100100");//X3=1700
    	memory.setMemory(11, instructions.int_to_binary_16bits(1611));//sentence compare
    	memory.setMemory(12, instructions.int_to_binary_16bits(1616));//compare 1
    	memory.setMemory(13, instructions.int_to_binary_16bits(1626));//end of sentence
    	memory.setMemory(14, instructions.int_to_binary_16bits(1811));//word compare
    	memory.setMemory(15, instructions.int_to_binary_16bits(1816));//compare 2
    	memory.setMemory(16, instructions.int_to_binary_16bits(1826));//end of word
    	memory.setMemory(1899, instructions.int_to_binary_16bits(1025));
    	memory.setMemory(1897, instructions.int_to_binary_16bits(1));
    	memory.setMemory(1896, instructions.int_to_binary_16bits(1800));
    	memory.setMemory(1895, instructions.int_to_binary_16bits(1900));
    	memory.setMemory(1894, instructions.int_to_binary_16bits(1951));
    	memory.setMemory(1893, instructions.int_to_binary_16bits(2000));
    	memory.setMemory(1892, instructions.int_to_binary_16bits(1900));
    	memory.setMemory(1891, instructions.int_to_binary_16bits(1951));
    	memory.setMemory(1890, instructions.int_to_binary_16bits(1600));
    	memory.setMemory(1951, instructions.int_to_binary_16bits(116));//t
    	memory.setMemory(1952, instructions.int_to_binary_16bits(111));//o
    	memory.setMemory(1953, instructions.int_to_binary_16bits(119));//w
    	memory.setMemory(1954, instructions.int_to_binary_16bits(110));//n
    	memory.setMemory(2000, instructions.int_to_binary_16bits(4));
        memory.setMemory(7,Instructions.int_to_binary_16bits(32));
        memory.setMemory(8,Instructions.int_to_binary_16bits(64));
        
        //test program
        int start = 1700;
        memory.setMemory(start++, "0000010001101001");
        memory.setMemory(start++, "0001110000011111");
        memory.setMemory(start++, "0001110000000001");
        memory.setMemory(start++, "0010100001100110");//jz word
        memory.setMemory(start++, "0000010001101001");
        memory.setMemory(start++, "0001110000011111");
        memory.setMemory(start++, "0001110000011111");
        memory.setMemory(start++, "0001110000000001");
        memory.setMemory(start++, "0010100001100000");// jz sentence
        memory.setMemory(start++, "0000010001101001");
        memory.setMemory(start++, "0001110000011111");
        memory.setMemory(start++, "0001110000001111");
        memory.setMemory(start++, "0010100001100000");//jz sentence
        memory.setMemory(start++, "0000010001101001");
        memory.setMemory(start++, "0000100001100010");
        memory.setMemory(start++, "0000010001000010");
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001000010");
        memory.setMemory(start++, "0000010001001001");
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001001001");
        memory.setMemory(start++, "0001101000000001");
        memory.setMemory(start++, "0011010011000000");//jma main

        start = 1600;
        memory.setMemory(start++, "0000010001000101");//sentence 1600
        memory.setMemory(start++, "0000100001000010");
        memory.setMemory(start++, "0000010001000100");
        memory.setMemory(start++, "0000100001000001");
        memory.setMemory(start++, "0001011001100011");
        memory.setMemory(start++, "0010111000101101");//JNE R2 END_OF_SENTENCE
        memory.setMemory(start++, "0000011001100011");
        memory.setMemory(start++, "0001101000000001");
        memory.setMemory(start++, "0000010001001000");
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001001000");
        memory.setMemory(start++, "0100001000101100");//SOB R2 COMPARE1 "sentence compare" 1611
        memory.setMemory(start++, "0000010101001000");
        memory.setMemory(start++, "0000011001000111");
        memory.setMemory(start++, "1111100100000001");
        memory.setMemory(start++, "1111101000000001");
        memory.setMemory(start++, "0000010001100010");//compare1 1616
        memory.setMemory(start++, "0001010001100001");
        memory.setMemory(start++, "0010110000101101");//jne r0 END_OF_SENTENCE
        memory.setMemory(start++, "0000010001000001");
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001000001");
        memory.setMemory(start++, "0000010001000010");
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001000010");
        memory.setMemory(start++, "0011010000101011");//jma sentence compare
        memory.setMemory(start++, "0000010001000111");//"END_OF_SENTENCE" 1626
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001000111");
        memory.setMemory(start++, "0000010001001000");
        memory.setMemory(start++, "0001010001001000");
        memory.setMemory(start++, "0000100001001000");
        memory.setMemory(start++, "1010111000000000");//set r2 = 0
        memory.setMemory(start++, "0000010001001001");
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001001001");
        memory.setMemory(start++, "0000010001000101");//set 1892 to 1900
        memory.setMemory(start++, "0000100001000010");
        memory.setMemory(start++, "0011010011000000");//jma main

        start = 1800;
        memory.setMemory(start++, "0000010001000101");//word 1800
        memory.setMemory(start++, "0000100001000010");
        memory.setMemory(start++, "0000010001000100");
        memory.setMemory(start++, "0000100001000001");
        memory.setMemory(start++, "0001011001100011");
        memory.setMemory(start++, "0010111000110000");//JNE R2 END_OF_WORD
        memory.setMemory(start++, "0000011001100011");
        memory.setMemory(start++, "0001101000000001");
        memory.setMemory(start++, "0000010001001000");//#WORD PLUS ONE
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001001000");
        memory.setMemory(start++, "0100001000101111");//SOB R2 COMPARE2 "word compare" 1811
        memory.setMemory(start++, "0000010101001000");
        memory.setMemory(start++, "0000011001000111");
        memory.setMemory(start++, "1111100100000001");
        memory.setMemory(start++, "1111101000000001");
        memory.setMemory(start++, "0000010001100010");//COMPARE2 1816
        memory.setMemory(start++, "0001010001100001");
        memory.setMemory(start++, "0010110000110000");//JNE R0 END_OF_WORD
        memory.setMemory(start++, "0000010001000001");
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001000001");
        memory.setMemory(start++, "0000010001000010");
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001000010");
        memory.setMemory(start++, "0010110000101110");//jma word compare
        memory.setMemory(start++, "1010111000000000");//set r2 = 0 END_OF_WORD  1826
        memory.setMemory(start++, "0000010001001000");
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001001000");
        memory.setMemory(start++, "0000010001001001");
        memory.setMemory(start++, "0001100000000001");
        memory.setMemory(start++, "0000100001001001");
        memory.setMemory(start++, "0000010001000101");//set 1892 to 1900
        memory.setMemory(start++, "0000100001000010");
        memory.setMemory(start++, "0011010011000000");//jma main
        
        Scanner in;
        int charCount = 0;
        try
        {
            in = new Scanner(new File(filepath2));
        }
        catch(FileNotFoundException err)
        {
            System.out.println("ERROR READING FILE: " + filepath2);
            return;
        }
        while(in.hasNext())
        {
        	String next = in.nextLine();
            for (int i = 0; i < next.length(); i++)
            {
                char c = next.charAt(i);
                charCount++;
                String binary = Instructions.characterToBinary(String.valueOf(c));
                int ASCII = Instructions.binary_to_int(binary);
                memory.setMemory(memory.HEAP_START_ADDRESS + charCount, Instructions.int_to_binary_16bits(ASCII));
                //io.pushOutput(next);
                io_area.append(String.valueOf(c));
            }
        }
        
//        memory.setMemory(memory.HEAP_START_ADDRESS, Instructions.int_to_binary_16bits(charCount));
//        io_area.append("\nEnter a word to find and press <enter>: ");
        updateDisplay();

    }//GEN-LAST:event_load_program2_buttonActionPerformed

    private void load_program1_buttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_load_program1_buttonActionPerformed
    {//GEN-HEADEREND:event_load_program1_buttonActionPerformed

        IF = null;
        ID = null;
        EX = null;
        MEM = null;
        for (int i = 0; i < memory.MEMORY_SIZE; i++)
        {
            memory.setMemory(i, ZEROED_REGISTER);
        }

        for (JFormattedTextField field : wordFields)
        {
            field.setText(ZEROED_REGISTER);
            //field.setEnabled(false);
        }
        io_area.setText("");

        io_area.append("Type a number press enter: ");
        p1_active = true;
        p1_firstTime = true;
        p2_active = false;
        
    	registers.setPC("0000010000011010");
    	registers.setX1("0000001111110110");//X1=1014
    	registers.setX2("0000000000011111");//X2=32
        memory.setMemory(7,Instructions.int_to_binary_16bits(32));
        memory.setMemory(8,Instructions.int_to_binary_16bits(64));
    	memory.setMemory(1021, "0000001111101100");//1004
    	memory.setMemory(1023, "0111111111111111");
        memory.setMemory(1050, "1111010000000000");
        memory.setMemory(1051, "0000100001001010");
        memory.setMemory(1052, "1111010000000000");
        memory.setMemory(1053, "0000100001001011");
        memory.setMemory(1054, "1111010000000000");
        memory.setMemory(1055, "0000100001001100");
        memory.setMemory(1056, "1111010000000000");
        memory.setMemory(1057, "0000100001001101");
        memory.setMemory(1058, "1111010000000000");
        memory.setMemory(1059, "0000100001001110");
        memory.setMemory(1060, "1111010000000000");
        memory.setMemory(1061, "0000100001001111");
        memory.setMemory(1062, "1111010000000000");
        memory.setMemory(1063, "0000100001010000");
        memory.setMemory(1064, "1111010000000000");
        memory.setMemory(1065, "0000100001010001");
        memory.setMemory(1066, "1111010000000000");
        memory.setMemory(1067, "0000100001010010");
        memory.setMemory(1068, "1111010000000000");
        memory.setMemory(1069, "0000100001010011");
        memory.setMemory(1070, "1111010000000000");
        memory.setMemory(1071, "0000100001010100");
        memory.setMemory(1072, "1111010000000000");
        memory.setMemory(1073, "0000100001010101");
        memory.setMemory(1074, "1111010000000000");
        memory.setMemory(1075, "0000100001010110");
        memory.setMemory(1076, "1111010000000000");
        memory.setMemory(1077, "0000100001010111");
        memory.setMemory(1078, "1111010000000000");
        memory.setMemory(1079, "0000100001011000");
        memory.setMemory(1080, "1111010000000000");
        memory.setMemory(1081, "0000100001011001");
        memory.setMemory(1082, "1111010000000000");
        memory.setMemory(1083, "0000100001011010");
        memory.setMemory(1084, "1111010000000000");
        memory.setMemory(1085, "0000100001011011");
        memory.setMemory(1086, "1111010000000000");
        memory.setMemory(1087, "0000100001011100");
        memory.setMemory(1088, "1111010000000000");
        memory.setMemory(1089, "0000100001011101");
        memory.setMemory(1090, "1111010000000000");
        memory.setMemory(1091, "0000100001001000");//store the request from user to address 1022
        memory.setMemory(1092, "0011010000001010");//jump back to address 10
        memory.setMemory(10, "0001101100010101");//AIR R3 21
        memory.setMemory(10, "0001101100010101");//AIR R3 21
        memory.setMemory(11, "0000101101000110");//STR R3 1020
        memory.setMemory(12, "0000011101000110");//LDR R3 1020
        memory.setMemory(13, "0100001100001111");//SOB R3 15
        memory.setMemory(14, "1111100000000001");//output the closest number
        memory.setMemory(15, "0000011001001000");//LDR R2 0 0 1022
        memory.setMemory(16, "0001001101000111");//AMR R3 1021
        memory.setMemory(17, "0000101101000100");//STR R3 1018
        memory.setMemory(18, "0001011001100100");//SMR R2 0 1 1018
        memory.setMemory(19, "0101001010000000");//MLT R2 R2
        memory.setMemory(20, "0000101101000010");//STR R3 1016
        memory.setMemory(21, "0000011001000010");//LDR R2 1016(R2 store the current calculation result)
        memory.setMemory(22, "0001011101001001");//SMR R3 0 0 1023
        memory.setMemory(23, "0100011110000010");//JGE R3 0 0 34
        memory.setMemory(24, "0000101001001001");//STR R2 0 0 1023
        memory.setMemory(25, "0000010001100100");//LDR R0 0 1 1018
        memory.setMemory(26, "0000100001000011");//STR R0 0 0 1017
        memory.setMemory(27, "0000011101000111");//LDR R3 1021
        memory.setMemory(28, "0001101100000010");//AIR R3 2
        memory.setMemory(29, "0000101101000111");//STR R3 1021
        memory.setMemory(30, "0000011101000110");//LDR R3 1020
        memory.setMemory(31, "0001111100000001");//SIR R3 1
        memory.setMemory(32, "0000101101000110");//STR R3 1020
        memory.setMemory(33, "0011010000001100");//JMA 12
        memory.setMemory(34, "0000011101000111");//LDR R3 1021
        memory.setMemory(35, "0001101100000010");//AIR R3 2
        memory.setMemory(36, "0000101101000111");//STR R3 1021
        memory.setMemory(37, "0000011101000110");//LDR R3 1020
        memory.setMemory(38, "0001111100000001");//SIR R3 1
        memory.setMemory(39, "0000101101000110");//STR R3 1020
        memory.setMemory(40, "0011010000001100");//JMA 12
        
        updateDisplay();
    }//GEN-LAST:event_load_program1_buttonActionPerformed
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(Simulator_MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(Simulator_MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(Simulator_MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(Simulator_MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {

                new Simulator_MainWindow().setVisible(true);
               
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AddressLabel;
    private javax.swing.JFormattedTextField Address_Field;
    private javax.swing.JFormattedTextField CC_Field;
    private javax.swing.JLabel CC_Label;
    private javax.swing.JFormattedTextField IR_Field;
    private javax.swing.JLabel IR_Label;
    private javax.swing.JComboBox<String> InputType_Box;
    private javax.swing.JButton LoadTest_Button;
    private javax.swing.JFormattedTextField MAR_Field;
    private javax.swing.JLabel MAR_Label;
    private javax.swing.JLabel MBR_FIeld;
    private javax.swing.JFormattedTextField MBR_Field;
    private javax.swing.JFormattedTextField MFR_Field;
    private javax.swing.JLabel MFR_Label;
    private javax.swing.JButton OverrideButton;
    private javax.swing.JCheckBox Override_Checkbox;
    private javax.swing.JFormattedTextField PC_Field;
    private javax.swing.JLabel PC_Label;
    private javax.swing.JFormattedTextField R0_Field;
    private javax.swing.JLabel R0_Label;
    private javax.swing.JFormattedTextField R1_Field;
    private javax.swing.JLabel R1_Label;
    private javax.swing.JFormattedTextField R2_Field;
    private javax.swing.JLabel R2_Label;
    private javax.swing.JFormattedTextField R3_Field;
    private javax.swing.JLabel R3_Label;
    private javax.swing.JCheckBox RunCheckBox;
    private javax.swing.JButton StepButton;
    private javax.swing.JFormattedTextField Value_Field;
    private javax.swing.JLabel Value_Label;
    private javax.swing.JFormattedTextField X1_Field;
    private javax.swing.JLabel X1_Label;
    private javax.swing.JFormattedTextField X2_Field;
    private javax.swing.JLabel X2_Label;
    private javax.swing.JFormattedTextField X3_Field;
    private javax.swing.JLabel X3_Label;
    private javax.swing.JTextArea cache_area;
    private javax.swing.JTextArea io_area;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton load_program1_button;
    private javax.swing.JButton load_program2_button;
    private javax.swing.JTextArea pipeline_area;
    // End of variables declaration//GEN-END:variables
}
