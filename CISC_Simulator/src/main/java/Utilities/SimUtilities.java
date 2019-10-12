package Utilities;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jonathan Pritchett
 */
public class SimUtilities
{
    public static String binaryToIntString(String binary)
    {
        return binary_to_int(binary).toString();
    }
    public static String intToBinaryString(int intval)
    {
        return int_to_binary(intval);
    }
    
    public static Integer binary_to_int(String binary_number)
    {
            return Integer.parseInt(binary_number, 2);
    }

    //another useful function to convert integer into binary string
    public static String int_to_binary(Integer number)
    {
            String binary_number = Integer.toBinaryString(number);
            return binary_number;
    }
    
}
