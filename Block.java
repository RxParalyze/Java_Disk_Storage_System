import java.io.*;
import java.util.*;

public class Block {
    public static final int POINTERS_PER_BLOCK = Disk.BLOCK_SIZE / 4;
    private final int[] blockPointers = new int[POINTERS_PER_BLOCK];//number of bytes per integer
    public final int blockNum;
    private boolean hasSpace = true;

    public Block(int number)
    {
        this.blockNum = number;
    }

    public void clear() 
    {
        this.hasSpace = true;
        for (int i = 0; i < Disk.BLOCK_SIZE / 4; i++) {
            this.blockPointers[i] = 0;
        }
    }
    
    public boolean hasFreeSpace()
    {
        if (this.getFreePointerCount() == 0)
            this.hasSpace = false;
        
        return this.hasSpace;
    }
    
    //frees up pointers for storage
    public void removePointers(ArrayList<Integer> pointers)
    {        
        for(int x = 0; x < pointers.size(); x++)
        {
            this.blockPointers[x] = 0;
        }
        
        if(!this.hasSpace)
            this.hasSpace = true;        
    }
    
    public int getFreePointerCount()
    {
        int freePointers = 0;        
        for(int x : this.blockPointers)
        {
            if (this.blockPointers[x] == 0)
                freePointers++;
        }        
        if(freePointers == 0)
            this.hasSpace = false;
        
        return freePointers;
    }
    
    //writes first availabe pointers, no overwriting
    public ArrayList<Integer> writeFreePointers(ArrayList<Integer> pointerData)
    {
        ArrayList<Integer> pointerNums = new ArrayList<>();
        
        for(int c = 0; c < pointerData.size(); c++)
        {
            for(int p = 0; p < this.blockPointers.length; p++)
            {
                if (this.blockPointers[p] == 0)
                {
                    this.blockPointers[p] = pointerData.get(c);
                    pointerNums.add(p);
                    break;
                }
                
            }
            if (pointerNums.size() == pointerData.size())
                break;
        }
        
        return pointerNums;
    }
    
    //writes specific pointers, will overwrite
    public void writeSpecificPointer(ArrayList<Integer> pointerNums, ArrayList<Integer> dataPointers)
    {
        for(int x = 0; x < pointerNums.size(); x++)
        {
            this.blockPointers[pointerNums.get(x)] = dataPointers.get(x);
        }
    }
    
    public ArrayList<Integer> readPointers(ArrayList<Integer> pointerNums)
    {
        ArrayList<Integer> output = new ArrayList<>();
        for(Integer x : pointerNums)
        {
            output.add(this.blockPointers[x]);
        }
        return output;
    }
    
    public String toString() {
        String s = new String();
        s += "BLOCK:\n";
        for (int i = 0; i < this.blockPointers.length; i++)
                s += this.blockPointers[i] + "|";
        return s;
    }
}
