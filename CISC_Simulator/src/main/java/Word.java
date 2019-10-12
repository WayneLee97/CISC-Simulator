
import java.util.Objects;
import java.util.Arrays;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jonathan Pritchett
 */
public class Word
{

        //16bit memory block
    //private String data = "0000000000000000";
    private char data[];
    private int word_length = 16;
    
    public Word()
    {
        data = new char[word_length];
        Arrays.fill(data,'0');
    }
    
    public Word(String data)
    {
        if(data.length() == word_length)
        {
            this.data = data.toCharArray();
        }
    }

    public String getData()
    {
        return String.valueOf(data);
    }

    public void setData(String data)
    {
        if(data.length() == word_length)
        {
            this.data = data.toCharArray();
        }
    }
    
    public char charAt(int bitNum)
    {
        if(bitNum < word_length && bitNum >= 0)
        {
            return data[bitNum];
        }
        else
        {
            return data[bitNum];
        }
    }
    
    public void setBit(int bitNum, String bitVal)
    {
        if(bitNum < word_length && bitNum >= 0 && bitVal.length() > 0)
        {
            data[bitNum] = bitVal.charAt(0);
        }
    }
    
    public String substring(int startIndex, int endIndex)
    {
        String sub = "";
        
        if(startIndex >= 0  && endIndex >= startIndex && endIndex < word_length)
        {
            for(int i = startIndex; i < endIndex; i++)
            {
                sub = sub.concat(String.valueOf(data[i]));
            }
        }
        
        return sub;
    }
    
    public String toString()
    {
        return getData();
    }
    
    
    

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Word other = (Word) obj;
        if (!Objects.equals(this.data, other.data))
        {
            return false;
        }
        return true;
    }
    
    
    
}
