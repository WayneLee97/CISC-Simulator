


import java.util.ArrayList;
import java.util.Map;
import javax.swing.JFormattedTextField;

import java.util.Timer;
import java.util.TimerTask;

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




public class Simulator_MainWindow extends javax.swing.JFrame
{
    static final String ZEROED_REGISTER = "0000000000000000";
    static final int CYCLE_TIME = 1000;
    
    
    
    private InputType inputType = InputType.BINARY;
    
    private ArrayList<JFormattedTextField> wordFields;
    
    private Registers registers;
    private Memory memory;
    private Instructions instructions;
    private IOHandler io;
    
    private int cycleCount = 0;
    
    //private Map<JFormattedTextField,Word> wordMap;

    public static int op_translator(String S_instruction, Instructions instruction){
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
        
        for(int i = 0; i < 2048; i++)
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
        
        
        for(JFormattedTextField field : wordFields)
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
        
    }
    
      
    private void setInputType(InputType type)
    {
        this.inputType = type;
        updateDisplay();
    }
    
    private String binaryToIntString(String binary)
    {
        return Instructions.binary_to_int(binary).toString();
    }
    private String intToBinaryString(int intval)
    {
        return Instructions.int_to_binary(intval);
    }
    
    public void updateDisplay()
    {
        String cache_text = "";
        
        for(int i = 0; i < memory.MAX_CACHE; i++)
        {
            cache_text = cache_text.concat( (i+1) + ":\t");
            if(i < memory.getCache().size())
            {
                if(inputType == InputType.BINARY)
                {
                    cache_text = cache_text.concat(intToBinaryString((Integer)memory.getCache().keySet().toArray()[i]) + "\t");
                    cache_text = cache_text.concat(memory.getCache().values().toArray()[i].toString());
                }
                else if(inputType == InputType.DECIMAL)
                {
                    cache_text = cache_text.concat(memory.getCache().keySet().toArray()[i] + "\t");
                    cache_text = cache_text.concat(binaryToIntString(memory.getCache().values().toArray()[i].toString()));
                }
            }
            cache_text = cache_text.concat("\n");
        }
            
            cache_area.setText(cache_text);
        
        if(inputType == InputType.BINARY)
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
            
            
            Value_Field.setText(memory.getMemory(instructions.binary_to_int(Address_Field.getText())));
            
            

            
        }
        else if(inputType == InputType.DECIMAL)
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
            
            
            Value_Field.setText(binaryToIntString(memory.getMemory(instructions.binary_to_int(Address_Field.getText()))));
        }
    }
    
    public void cycle()
    {

        //System.out.print(memory.printCache());
        

        //a variable keep the trace whether PC has been changed inside of instructions
    	boolean PC_changed = false;
        //implement the instruction for this step
        if(!this.memory.getMemory(this.instructions.binary_to_int(registers.getPC())).equals("0000000000000000"))
        {
            //execute the instruction,but first use the translator function to get the operation code
            int op_code = op_translator(memory.getMemory(instructions.binary_to_int(registers.getPC())), instructions);
            switch (op_code) 
            {
            //load register from the memory, LDR
            case 1:
                    registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
                    this.instructions.LDR(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
                    break;
            //store register from memory, STR
            case 2:
                    registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
                    this.instructions.STR(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
                    break;
            //load register with address, LDA
            case 3:
                    registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
                    this.instructions.LDA(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
                    break;
            //load index register from the memory, LDX
            case 41:
                    registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
                    this.instructions.LDX(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
                    break;
            //store index register to memory, STX
            case 42:
                    registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
                    this.instructions.STX(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
                    break;
            //jump if zero
            case 10:
            		registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
            		this.instructions.JZ(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
            		//the pc has changed inside, so we set PC_changed  = true
            		PC_changed = true;
            		break;
            //jump if not equal
            case 11:
            		registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
            		this.instructions.JNE(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
            		//the pc has changed inside, so we set PC_changed  = true
            		PC_changed = true;
            		break;
            //jump if condition code
            case 12:
        		registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
        		this.instructions.JCC(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
        		//the pc has changed inside, so we set PC_changed  = true
        		PC_changed = true;
        		break;
        	//unconditional jump to address
            case 13:
        		registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
        		this.instructions.JMA(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
        		//the pc has changed inside, so we set PC_changed  = true
        		PC_changed = true;
        		break;
        	//jump and save return address
            case 14:
        		registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
        		this.instructions.JSR(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
        		//the pc has changed inside, so we set PC_changed  = true
        		PC_changed = true;
        		break;
        	//return from subroutine with return code as immediate portion stored in the instructions' address field
            case 15:
        		registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
        		this.instructions.RFS(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
        		//the pc has changed inside, so we set PC_changed  = true
        		PC_changed = true;
        		break;
        	//subtract one and branch
            case 16:
        		registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
        		this.instructions.SOB(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
        		//the pc has changed inside, so we set PC_changed  = true
        		PC_changed = true;
        		break;
        	//jump greater than or equal to
            case 17:
        		registers.setIR(memory.getMemory(instructions.binary_to_int(registers.getPC())));
        		this.instructions.JGE(memory.getMemory(instructions.binary_to_int(registers.getPC())), registers, this.memory);
        		//the pc has changed inside, so we set PC_changed  = true
        		PC_changed = true;
        		break;
            default:
                    break;
            }
        }
        
        //go to the next instruction
        if(!PC_changed){
        	int next_address = this.instructions.binary_to_int(registers.getPC()) + 1;
            registers.setPC(this.instructions.int_to_binary(next_address));
        }
        //print for test
        System.out.println(registers.getPC());
        System.out.println(registers.getMAR());
        
        	
        if(RunCheckBox.isSelected())
        {
            Timer timer = new Timer();
            ClockCycler cycler = new ClockCycler(this);
            timer.schedule(cycler, CYCLE_TIME);
        }
        
        updateDisplay();
        
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
        jScrollPane2 = new javax.swing.JScrollPane();
        user_input_area = new javax.swing.JTextArea();
        load_p1_button = new javax.swing.JButton();
        load_p2_button = new javax.swing.JButton();

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
                .addContainerGap(68, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Registers", jPanel1);

        cache_area.setColumns(20);
        cache_area.setRows(5);
        cache_area.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane1.setViewportView(cache_area);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(300, 300, 300))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Cache", jPanel2);

        user_input_area.setBackground(new java.awt.Color(0, 0, 0));
        user_input_area.setColumns(20);
        user_input_area.setForeground(new java.awt.Color(255, 255, 0));
        user_input_area.setLineWrap(true);
        user_input_area.setRows(5);
        user_input_area.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyTyped(java.awt.event.KeyEvent evt)
            {
                user_input_areaKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(user_input_area);

        load_p1_button.setText("Load Program 1");
        load_p1_button.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                load_p1_buttonActionPerformed(evt);
            }
        });

        load_p2_button.setText("Load Program 2");
        load_p2_button.setEnabled(false);
        load_p2_button.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                load_p2_buttonActionPerformed(evt);
            }
        });

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
                        .addContainerGap()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(RunCheckBox)
                                    .addGap(18, 18, 18)
                                    .addComponent(StepButton)
                                    .addGap(6, 6, 6))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addComponent(OverrideButton))
                                    .addComponent(Override_Checkbox))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                                            .addComponent(Value_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(Address_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(LoadTest_Button)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(load_p2_button)
                                    .addComponent(load_p1_button)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(72, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(InputType_Box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 496, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Address_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AddressLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Value_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Value_Label))
                        .addGap(59, 59, 59)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(Override_Checkbox))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(OverrideButton)
                                .addGap(30, 30, 30)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(LoadTest_Button)
                            .addComponent(load_p1_button))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(load_p2_button)
                        .addGap(25, 25, 25)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(RunCheckBox)
                            .addComponent(StepButton))
                        .addGap(36, 36, 36)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(40, Short.MAX_VALUE))
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
        Value_Field.setText(memory.getMemory(instructions.binary_to_int(Address_Field.getText())));
        
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
        System.out.print(evt.getKeyChar());
        io.pushInput(Character.toString(evt.getKeyChar()));
        
        // "\nEnter a number : "
        // "\nNumber list = {"
        // "}"
        // "\nEnter a number to search the list for : "
        //
        
        
        
    }//GEN-LAST:event_user_input_areaKeyTyped

    private void load_p1_buttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_load_p1_buttonActionPerformed
    {//GEN-HEADEREND:event_load_p1_buttonActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_load_p1_buttonActionPerformed

    private void load_p2_buttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_load_p2_buttonActionPerformed
    {//GEN-HEADEREND:event_load_p2_buttonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_load_p2_buttonActionPerformed

  
    
    
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton load_p1_button;
    private javax.swing.JButton load_p2_button;
    private javax.swing.JTextArea user_input_area;
    // End of variables declaration//GEN-END:variables
}
