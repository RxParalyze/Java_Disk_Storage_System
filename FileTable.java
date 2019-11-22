import java.util.*;
import java.io.*;

public class FileTable {
    public static final int MAX_OPEN_FILES = 256; 
    private final ArrayList<String> errors = new ArrayList<>();
    private String e;
    private int[] inodeBitMap;//what inodes are free
    private int[] blockBitMap;//what blocks are free
    private LinkedHashMap<Integer, String> openFiles = new LinkedHashMap<>(MAX_OPEN_FILES);//a is fileNumber, b is a string path
    private LinkedHashMap<Integer, String> fileNumKey = new LinkedHashMap<>(Disk.MAX_FILES);//a is fileNumber, b is a string path
    private LinkedHashMap<String, Integer> fileNameKey = new LinkedHashMap<>(Disk.MAX_FILES);
    private LinkedHashMap<Integer, FileDescriptor> fdArray = new LinkedHashMap<>(Disk.MAX_FILES);//the array of file Descriptors
    private LinkedHashMap<Integer, String> fdNums = new LinkedHashMap<>(Disk.MAX_FILES);//map of fd numbers and string file names
    private final InodeBlock[] inodeSector;
    private final Block[] blockSector;
    
    
    public FileTable(InodeBlock[] iSector, Block[] bSector){        
        this.inodeSector = iSector;
        this.blockSector = bSector;

        //this begins at 1 so that the first file will be file 1 not file 0
        inodeBitMap =  new int[Disk.MAX_FILES + 1];
        blockBitMap = new int[Disk.NUM_DATA_BLOCKS];
        inodeBitMap[0] = -1;//First file is file 1, file "0" will never change/exist
        blockBitMap[0] = 1;//First block is technically SuperBlock, will never become available
        
	for(int i = 1; i <= inodeBitMap.length; i++){
	    inodeBitMap[i] = 0;//0 is free, 1 is occupied
            fdArray.put(i, new FileDescriptor(new Inode(), i));//initializes blank file descriptors for max amount of files as placeholders
	}
        
        for(int b = 1; b < blockBitMap.length; b++)
        {
            blockBitMap[b] = 0;//0 is free, 1 is full
        }
    }
    
    //exports errors to Disk or JFS
    public ArrayList<String> exportErrors()
    {
        return this.errors;
    }
    
    //this function initializes all inode blocks
    public void initInodeBlocks()
    {
        for (int x = 1; x <= this.inodeSector.length; x++)
        {
            this.inodeSector[x] = new InodeBlock(x);
        }
    }
    
    //for returning inodeSector and blockSector to disk for saving
    public InodeBlock[] getInodeSector()
    {
        return this.inodeSector;
    }    
    public Block[] getBlockSector()
    {
        return this.blockSector;
    }
    
    //sets bitmaps, don't need get bitmaps because FileTable is only class that needs it
    //***should not be used except if the disk is manually configured externally***
    public void setInodeBitMap(int[] iBitMap)
    {
        this.inodeBitMap = iBitMap;
    }
    public void setBlockBitMap(int[] bBitMap)
    {
        this.blockBitMap = bBitMap;
    }
    
    //assign a file
    public int createNewFile(String filePath)
    {
        int outcome;
        if(this.fileNameKey.containsKey(filePath))
        {
            this.e = "create file failed, already exists";
            this.errors.add(e);
            return -1;
        }
        File newFile = new File(filePath);        
        int fd = getNextFD();
        
        if(fd > 0)
        {
            this.fdArray.get(fd).setFileName(newFile.getName());
            this.fdNums.put(fd, newFile.getName());
            this.fdArray.get(fd).getInode().setType(1);//1 is file, 0 is directory
            this.fileNumKey.put(fd, filePath);
            this.fileNameKey.put(filePath, fd);
            outcome = 0;
        }
        else
        {
            this.e= "File Creation Failed: Max File Limit Reached.";
             outcome = -1;
            this.errors.add(e);
        }
        return outcome;        
    }
    
    public int openFile(String fileName)
    {
        if(this.fileNameKey.containsKey(fileName))
        {
            int fd = fileNameKey.get(fileName);
            if(this.openFiles.size() < MAX_OPEN_FILES)
            {
                this.openFiles.putIfAbsent(fd, fileName);
                return fd;
            }
            else if(this.openFiles.containsKey(fd))
            {
                this.e = "Max files open, but this file was already open.";
                this.errors.add(e);
                return fd;
            }
            else
            {
                this.e = "File open failure: too many files open";
                this.errors.add(e);
                return -1;
            }
        }
        this.e = "File open failure: File Doesn't exist";
        this.errors.add(e);
        return -1;
    }
    
    public int closeFile(int fd)
    {
        if(this.fileNumKey.containsKey(fd))
        {
            if(this.openFiles.containsKey(fd))
            {
                this.openFiles.remove(fd);
                return 0;
            }
            else
            {
                this.e = "File close failure: file was already closed";
                this.errors.add(e);
                return -1;
            }
        }
        this.e = "File close failure: File Doesn't exist";
        this.errors.add(e);
        return -1;
    }
    
    //actually writes data to blocks
    public int writeFile(int fd, FileInputStream buffer, int size) throws IOException
    {
        FileDescriptor fdWrite = this.fdArray.get(fd);
        int pointerLocation = fdWrite.getSeekPointer();
        Inode fdInode = fdWrite.getInode();
        
        //do all checks before anything else
        if(!this.openFiles.containsKey(fd))
        {
            this.e = "File Write failure: File Closed.";
            this.errors.add(e);
            return -1;
        }        
        if((size - fdInode.getBlockPointerCount() - fdWrite.getSeekPointer()) > this.checkMemoryStorage())
        {
            this.e = "File Write failure: Memory full.";
            this.errors.add(e);
            return -1;
        }
        if((size + fdInode.getBlockPointerCount() - fdWrite.getSeekPointer()) > Disk.MAX_FILE_SIZE)
        {
            this.e = "File Write failure: File too large.";
            this.errors.add(e);
            return -1;
        }
        
        ArrayList<Integer> overWriteData = new ArrayList<>();      
        ArrayList<Integer> newData = new ArrayList<>(); 
        
        LinkedHashMap<Integer, ArrayList<Integer>> blockPairs = fdInode.getBlockPairs();
        LinkedHashMap<Integer, ArrayList<Integer>> newBlockPairs = new LinkedHashMap<>();
        ArrayList<Integer> inodeBlockNums = new ArrayList<>(blockPairs.keySet());
        ArrayList<ArrayList<Integer>> inodePointerNums = new ArrayList<>(blockPairs.values());//arrays of the inode's block's pointers
        for(Integer key : blockPairs.keySet())
        {
            newBlockPairs.put(key, blockPairs.get(key));
        }
        
        for(int b = 0; b < size; b++)
        {
            if(b < (fdInode.getBlockPointerCount() - pointerLocation))
                overWriteData.add(buffer.read());
            else
                newData.add(buffer.read());
        }
        
        //overwrite old data
        for(int x = pointerLocation; x < inodeBlockNums.size(); x++)
        {
            this.blockSector[inodeBlockNums.get(x)].writeSpecificPointer(inodePointerNums.get(x), overWriteData);
            pointerLocation++;
        }
        
        //write new data, returns as map of (BlockNumber, ArrayList<Integer>)        
        newBlockPairs.putAll(this.getFreeBlocks(newData));
        
        this.fdArray.get(fd).getInode().setBlockPairs(newBlockPairs);
        
        return 0;
    }
    
    public FileOutputStream readFile(int fd, FileOutputStream output, int size) throws IOException
    {
        if(this.openFiles.containsKey(fd))
        {
            FileDescriptor fileRead = this.fdArray.get(fd);
            Inode fileInode = fileRead.getInode();
            LinkedHashMap<Integer, ArrayList<Integer>> fileInodeBlocks = fileInode.getBlockPairs();
            ArrayList<Integer> outputInts = new ArrayList<>();
            
            for(Integer x : fileInodeBlocks.keySet())
            {
                outputInts.addAll(this.blockSector[x].readPointers(fileInodeBlocks.get(x)));
            }
            for(int i = fileRead.getSeekPointer(); i < outputInts.size(); i++)
            {
                output.write(outputInts.get(i));
            }
        }
        
        return output;
    }
    
    public int fileSeek(int fd, int offset)
    {
        if(this.openFiles.containsKey(fd))
        {
            
            if(offset <= this.fdArray.get(fd).getInode().getFileSize() && offset >= 0)
            {
                this.fdArray.get(fd).setSeekPointer(offset);
                return 0;
            }
            else
            {
                this.e = "fileSeek failure: offset out of bounds";
                this.errors.add(e);
                return -1;
            }
        }
        this.e = "Seek failure. File doesn't exist.";
        this.errors.add(e);
        return -1;
    }
    
    //delete a file
    public int unlinkFile(String delFile)
    {
        if(this.fileNameKey.containsKey(delFile) && !this.openFiles.containsValue(delFile))
        {
            int fd = this.fileNameKey.get(delFile);
            Inode fdInode = this.fdArray.get(fd).getInode();
            LinkedHashMap<Integer, ArrayList<Integer>> blocksToClear = fdInode.getBlockPairs();
            for(Integer x : blocksToClear.keySet())
            {
                this.blockSector[x].removePointers(blocksToClear.get(x));
            }
            fdInode.clear();//call after blocks are reset
            this.fdArray.get(fd).clear();
            return 0;
        }
        this.e = "File Unlink failure: file doesn't exist.";
        this.errors.add(e);
        return -1;
    }
    
    public int createDir(String path)
    {
        File newDir = new File(path);

        if (!newDir.exists()) 
        {
            newDir.mkdir();
            int dirInt = this.getNextFD();
            this.fdArray.get(dirInt).setFileName(path);
            return 0;
        }
        return -1;
    }
    
    public int sizeDir(String fd)
    {
        int output = this.fdArray.get(this.fileNameKey.get(fd)).getSize();
        return output;
    }
    public FileOutputStream readDir(String fd, FileOutputStream output, int size) throws IOException
    {
        if(this.openFiles.containsValue(fd))
        {
            FileDescriptor fileRead = this.fdArray.get(this.fileNameKey.get(fd));
            Inode fileInode = fileRead.getInode();
            LinkedHashMap<Integer, ArrayList<Integer>> fileInodeBlocks = fileInode.getBlockPairs();
            ArrayList<Integer> outputInts = new ArrayList<>();
            
            for(Integer x : fileInodeBlocks.keySet())
            {
                outputInts.addAll(this.blockSector[x].readPointers(fileInodeBlocks.get(x)));
            }
            for(int i = fileRead.getSeekPointer(); i < outputInts.size(); i++)
            {
                output.write(outputInts.get(i));
            }
        }
        
        return output;
    }
    public int unlinkDir(String delFile)
    {
        if(this.fileNameKey.containsKey(delFile) && !this.openFiles.containsValue(delFile))
        {
            int fd = this.fileNameKey.get(delFile);
            Inode fdInode = this.fdArray.get(fd).getInode();
            LinkedHashMap<Integer, ArrayList<Integer>> blocksToClear = fdInode.getBlockPairs();
            for(Integer x : blocksToClear.keySet())
            {
                this.blockSector[x].removePointers(blocksToClear.get(x));
            }
            fdInode.clear();//call after blocks are reset
            this.fdArray.get(fd).clear();
            return 0;
        }
        this.e = "File Unlink failure: file doesn't exist.";
        this.errors.add(e);
        return -1;
    }
    
    //provides next fileDescriptor number
    private int getNextFD()
    {
        int x = -1;
        for(int bit : this.inodeBitMap)
        {
            if(this.inodeBitMap[bit] == 0)
            {
                x = bit;
                this.inodeBitMap[bit] = 1;
                break;
            }
            else if(bit == this.inodeBitMap.length && this.inodeBitMap[bit] == 1)
            {
                this.e = "Get FileDescriptor error. Max File Limit. ";
                this.errors.add(e);
                return x;
            }
        }
        return x;
    }
    
    private int checkMemoryStorage()
    {
        int counter = 0;
    
        for(int blockNum = 1; blockNum <= this.blockBitMap.length; blockNum++)
        {
            if(this.blockBitMap[blockNum] == 0)
            {
                counter += this.blockSector[blockNum].getFreePointerCount();
            }
        }
        
        return counter;
    }
    
    //allocates next free block's pointers on list
    private LinkedHashMap<Integer, ArrayList<Integer>> getFreeBlocks(ArrayList<Integer> newData)//0 is free, 1 is full
    {        
        LinkedHashMap<Integer, ArrayList<Integer>> freeBlocks = new LinkedHashMap<>(); 
        int counter = 0;
        
        for(int blockNum = 1; blockNum <= this.blockBitMap.length; blockNum++)
        {
            if(this.blockBitMap[blockNum] == 0)
            {
                ArrayList<Integer> pointerData = new ArrayList<>();
                for(int x = 0; x < this.blockSector[blockNum].getFreePointerCount(); x++)
                {
                    pointerData.add(newData.get(counter));
                    counter++;
                }
                
                freeBlocks.put(blockNum, this.blockSector[blockNum].writeFreePointers(pointerData));
                
                if(!this.blockSector[blockNum].hasFreeSpace())
                {
                    this.blockBitMap[blockNum] = 1;
                }
            }
            
            if(blockNum == this.blockBitMap.length && this.blockBitMap[blockNum] == 1)
            {
                this.e = "Get Free Blocks error. Memory Full. ";
                this.errors.add(e);
                for(Integer key : freeBlocks.keySet())
                {
                    this.blockSector[key].removePointers(freeBlocks.get(key));
                    freeBlocks.remove(key);
                }
            }
        }
        return freeBlocks;
    }

    
}


