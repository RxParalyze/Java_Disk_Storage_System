import java.util.*;
import java.io.*;

public class InodeBlock{
    private final int[] inodeMap = new int[Disk.INODES_PER_BLOCK];//this is a list of what inodes are assigned here, in order
    private final int inodeBlockNum;
    
    public InodeBlock(int number) {
        this.inodeBlockNum = number;
        for (int x = 1; x <= Disk.INODES_PER_BLOCK; x++)
        {
            this.inodeMap[x - 1] = (x * this.inodeBlockNum);
        }
    }
    
    public int getInodeBlockNum()
    {
        return this.inodeBlockNum;
    }
    
    public int[] getInodeNumbers()
    {
        return this.inodeMap;
    }

    @Override
    public String toString() {
        String s = "INODEBLOCK:" + inodeMap.toString();
        return s;
    }
}

