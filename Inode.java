
import java.util.*;
 
public class Inode {
    public final static int SIZE = 64;	// size in bytes
    public final static int INODE_POINTERS = 30;
    public final static int FILE_NAME_LENGTH = 16;
    public final static int PATH_LENGTH = 256;
    private int fileType;
    private int fileSize;
    private int inumber;
    private Set<Integer> inodeBlockNum = new HashSet<>();//stores block numbers
    
    //For all pairs, first value is integer blocknumber, second value is ArrayList<Integer> of blocknumber's pointers
    private LinkedHashMap<Integer, ArrayList<Integer>> fileName = new LinkedHashMap<>();//stores the pointernumbers specific to the filename
    private LinkedHashMap<Integer, ArrayList<Integer>> path = new LinkedHashMap<>();//stores the pointernumbers specific to the path
    private LinkedHashMap<Integer, ArrayList<Integer>> blockPairs = new LinkedHashMap<>();//stores all file data in order
    
    public Inode()//placeholder inode
    {
        this.fileType = -1;     
    }
    
    public void setInumber(int num)
    {
        this.inumber = num;
    }
    
    public void clear()
    {
        this.fileName.clear();
        this.fileSize = 0;
        this.fileType = -1;
        this.path.clear();
        this.blockPairs.clear();
        this.inodeBlockNum.clear();
    }
    
    public int getInumber()
    {
        return this.inumber;
    }

    public void setFileSize(int size)
    {
        this.fileSize = size;
    }

    public int getFileSize()
    {
        return this.fileSize;
    }
        
    //sets file data block pairs
    public void setBlockPairs(LinkedHashMap<Integer, ArrayList<Integer>> blockData)
    {
        this.blockPairs = blockData;
    }
    
    public LinkedHashMap<Integer, ArrayList<Integer>> getBlockPairs()
    {
        return this.blockPairs;
    }
    
    public int getBlockNumberCount()
    {
        return this.inodeBlockNum.size();
    }
    
    public int getBlockPointerCount()
    {
        int count = 0;
        for (Integer key : this.blockPairs.keySet())
        {
            count += blockPairs.get(key).size();
        }
        
        return count;
    }
    
    public void setFileNamePairs(LinkedHashMap<Integer, ArrayList<Integer>> blockData)
    {
        this.fileName = blockData;
    }
    
    public LinkedHashMap<Integer, ArrayList<Integer>> getFileNamePairs()
    {
        return this.fileName;
    }
    
    public void setPathPairs(LinkedHashMap<Integer, ArrayList<Integer>> blockData)
    {
        this.path = blockData;
    }
    
    public LinkedHashMap<Integer, ArrayList<Integer>> getPathPairs()
    {
        return this.path;
    }
    
    public void setType(int typeBit)
    {
        this.fileType = typeBit;
    }

    public String getType()
    {
        switch (this.fileType)
        {
            case 0:
                return "Directory";
            case 1:
                return "File";
            case -1:
                return "Empty";
        }
        return "fileType Failed /n";
    }

    @Override
    public String toString() {
        String s = "[Type: " + getType()
            + "  Size: " + this.fileSize + " KB ";
        for(int i : this.inodeBlockNum)
            s += "|" + i;
        return s + "]";
    }
}
