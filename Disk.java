import java.util.*;
import java.io.*;

public class Disk implements Serializable{
    
    public final static int NUM_BLOCKS = 10000; // the number of disk blocks in the system    
    public final static int MAX_FILES = 1000;//maximum allowable number of files and/or directories on the disk
    public final static int BLOCK_SIZE = 512; // the size in bytes of each disk block    
    public final static int INODES_PER_BLOCK = BLOCK_SIZE / Inode.SIZE;
    public final static int MAX_FILE_SIZE = (Block.POINTERS_PER_BLOCK * 30);
    public final static int NUM_INODE_BLOCKS = MAX_FILES / INODES_PER_BLOCK;//total number of InodeBlocks, not number of Inodes
    public final static int NUM_DATA_BLOCKS = NUM_BLOCKS - NUM_INODE_BLOCKS;//
    
    private String diskErrno = "";
    
    //Objects owned by Disk
    private InodeBlock[] inodeSector = new InodeBlock[NUM_INODE_BLOCKS];
    private Block[] blockSector = new Block[NUM_DATA_BLOCKS];
    public final FileTable fileTable = new FileTable(inodeSector, blockSector);
    public SuperBlock superBlock;
    
    // the number of reads and writes to the file system
    private int readCount = 0;
    private int writeCount = 0;

    // the file representing the simulated  disk
    private File fileName;
    private RandomAccessFile disk;
    private String pathName = null;
    
    //Error Code Reference Files
    File diskError = new File("Q:\\c17ph\\CompleteFinalProject\\test\\Errors");
    Properties osError = new Properties();

    // read in the file representing the simulated disk
    public Disk() {
        try {
            fileName = new File("DISK");
            disk = new RandomAccessFile(fileName, "rw");
        }
        catch (IOException e) {
            System.err.println ("Unable to start the disk");
            this.diskErrno = "disk start error";
            System.exit(1);
        }
        superBlock = new SuperBlock(83);
    }
    
    public Disk(File path)
    {
        try {
            fileName = path;
            disk = new RandomAccessFile(fileName, "rw");
        }
        catch (IOException e) {
            System.err.println ("Unable to start the disk");
            this.diskErrno = "disk start error";
            System.exit(1);
        }
        superBlock = new SuperBlock(83);
    }
    
//    public void exportErrorFiles() throws FileNotFoundException
//    {
//        FileOutputStream export = new FileOutputStream("Q:\\c17ph\\CompleteFinalProject\\test\\Errors");
//        export.write(this.diskError);
//    }
    
    public String getdiskErrno()
    {
        return this.diskErrno;
    }
    
    //path must be RandomAccessFile
    public static Disk diskLoad(String path)
    {
        Disk disk = null;
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        
        try {

            fin = new FileInputStream(path);
            ois = new ObjectInputStream(fin);
            disk = (Disk) ois.readObject();

            } 
        catch (Exception ex) {
                ex.printStackTrace();
            }
        finally {
            if (fin != null) {
                try {
                    fin.close();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }   

            if (ois != null) {
                try {
                    ois.close();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return disk;
    }
    
    //not sure if this will work
    public static Disk diskLoad(File file)
    {
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        Disk disk = null;
        
        try {

            fin = new FileInputStream(file);
            ois = new ObjectInputStream(fin);
            disk = (Disk) ois.readObject();

            } 
        catch (Exception ex) {
                ex.printStackTrace();
            }
        finally {
            if (fin != null) {
                try {
                    fin.close();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }   

            if (ois != null) {
                try {
                    ois.close();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return disk;
    }
    
    public void diskSave(String file)//attempts to save at a new path name
    {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        write();
        
        try 
        {
            this.setPathName(file);
            fout = new FileOutputStream(this.fileName);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(disk);

            System.out.println("Successful Disk Save");
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        } 
        finally 
        {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void diskSave()//attempts to save file at current path name
    {
        if(this.pathName == null)
        {
            System.out.println("Save failed. Disk pathName is blank.");
            return;
        }
        
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        write();
        
        try 
        {
            fout = new FileOutputStream(this.fileName);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(fout);
            oos.writeObject(disk);

            System.out.println("Successful Disk Save");
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        } 
        finally 
        {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void setPathName(String path)
    {
        this.pathName = path;
        this.fileName = new File(this.pathName);
    }
    
    public String getPathName()
    {
        return this.pathName;
    }

    /**
     * read from blockNum into a byte buffer 	
     * Really not that useful from JFS standpoint as it will be totally random gibberish
     * Unless you know exactly what block pointer you're looking at
     */
    public void read(int blocknum, byte[] buffer) {
        if (buffer.length != BLOCK_SIZE) 
            throw new RuntimeException(
                "Read: bad buffer size " + buffer.length);
        try {
            disk.seek(blocknum);
            disk.read(buffer);
        }
        catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
        readCount++;
    } 

    /** 
     * Write from the buffer to blockNum 	
     * Really not that useful from JFS standpoint as it will corrupt all of the data inside
     * Unless you know exactly what block pointer you're looking at
     */
    public void write(int blocknum, byte[] buffer) {
        if (buffer.length != BLOCK_SIZE) 
            throw new RuntimeException(
                "Write: bad buffer size " + buffer.length);
        try {
            disk.seek(blocknum);
            disk.write(buffer);
        }
        catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
        writeCount++;
    }
    
    //Only for storing data before save
    private void write()
    {
        this.blockSector = this.fileTable.getBlockSector();
        this.inodeSector = this.fileTable.getInodeSector();
    }

    /**
     * Outputs statistics from the file system.
     */
    //public String toString() {
    public String generateStats() {
        return ("DISK: Read count: " + readCount + " Write count: " + 
            writeCount);
    }
}
