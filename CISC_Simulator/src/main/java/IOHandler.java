/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jonathan Pritchett
 */
public class IOHandler
{
    String inputBuffer;
    String outputBuffer;
    
    private static IOHandler inst = null;
    
    private IOHandler()
    {
        inputBuffer = "";
        outputBuffer = "";
    }
    
    public static IOHandler instance()
    {
        if(inst == null)
        {
            inst = new IOHandler();
        }
        
        return inst;
        
    }
    
    public String getNextInput()
    {
        String input = "";
        
        if(inputBuffer.length() > 0)
        {
            input = inputBuffer.substring(0,1);
            inputBuffer = inputBuffer.substring(1);
        }
                
        return input;
    }

    public String getNextOutput()
    {
        String output = "";
        
        if(outputBuffer.length() > 0)
        {
            output = outputBuffer.substring(0,1);
            outputBuffer = outputBuffer.substring(1);
        }
                
        return output;
    }
    
    public void pushInput(String input)
    {
        inputBuffer = inputBuffer.concat(input);
    }
    
    public void pushOutput(String output)
    {
       outputBuffer = outputBuffer.concat(output);
    }
    
    public boolean hasInput()
    {
        return inputBuffer.length() > 0;
    }
    
    public boolean hasOutput()
    {
        return outputBuffer.length() > 0;
    }
    
}
