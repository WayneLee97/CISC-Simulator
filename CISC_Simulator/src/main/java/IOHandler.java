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
    
    //singleton instance
    private static IOHandler inst = null;
    
    //singleton constructor
    private IOHandler()
    {
        inputBuffer = "";
        outputBuffer = "";
    }
    
    //returns the singleton instance
    public static IOHandler instance()
    {
        if(inst == null)
        {
            inst = new IOHandler();
        }
        
        return inst;
        
    }
    
    //returns the next character in the input buffer as a single char string,
    //or "" if the buffer is empty
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

    //returns the next character in the output buffer as a single char string,
    //or "" if the buffer is empty
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
    
    //pushes a string into the input buffer 
    public void pushInput(String input)
    {
        inputBuffer = inputBuffer.concat(input);
    }
    
    //pushes a string into the output buffer 
    public void pushOutput(String output)
    {
       outputBuffer = outputBuffer.concat(output);
    }
    
    //returns true if the input buffer has data, false otherwise
    public boolean hasInput()
    {
        return inputBuffer.length() > 0;
    }
    
    //returns true if the output buffer has data, false otherwise
    public boolean hasOutput()
    {
        return outputBuffer.length() > 0;
    }
    
}
