import java.util.*;
import java.io.*;

/*
 * Contains data about a file.  The data it contains is:
 * Inode, inumber, current seek pointer.
 */
public class FileDescriptor
{    
    private final Inode inode;
    private final int inumber;//file number 
    private int seekptr;//where in the file the system is currently looking at
    private String fileName;
    private String parentDirectory;
    
    public FileDescriptor(Inode inode, int inum)
    {
        this.inode = inode;
        this.inumber = inum;
        this.inode.setInumber(inum);
    }
    
    public void clear()
    {
        this.fileName = "";
        this.seekptr = 0;
    }
    
    public void setFileName(String name)
    {
        this.fileName = name;
    }
    public String getFileName()
    {
        return this.fileName;
    }
    
    public int getSize()
    {
        return (this.fileName.length() * 4);
    }
    
    public void setParentDir(String name)
    {
        this.parentDirectory = name;
    }
    public String getParentDir()
    {
        return this.parentDirectory;
    }
    
    public Inode getInode(){
	return inode;
    }
    
    public int getInumber(){
	return inumber;
    }
    
    public void setSeekPointer(int i){
	seekptr = i;
    }
    public int getSeekPointer(){
	return seekptr;
    }	
}

