import java.util.*;
import java.io.*;
import java.nio.file.*;

public class JavaFileSystem {
    private String osErrno;
    private Disk disk;
        
    public JavaFileSystem(String path)
    {
        try
        {
            disk = new Disk();
            disk.setPathName(path);
            disk.superBlock = new SuperBlock(17);
            
        }
        catch(IllegalArgumentException e)
        {
            System.err.println(e);
            osErrno = "Disk Boot Error";
        }
    }
    
    //Loads a disk
    public void FS_Disk_Load(File path) 
    {
        try
        {
            disk = Disk.diskLoad(path);
            System.out.println(disk.superBlock.toString());
        }
        catch(IllegalArgumentException e)
        {
            System.err.println(e);
            osErrno = "Disk Boot Error";
        }
    }
    
    public void FS_Disk_Save(String file)
    {
        disk.diskSave(file);
    }
    
    // Close all files and shut down the simulated disk.    
    public int FS_Disk_Shutdown() {
        disk.diskSave();
        System.out.println(disk.superBlock.toString());
        return 0;
    } // shutdown

    // Create a new (empty) file and return a file descriptor.    
    public int fileCreate(String fileName) {
        int output = disk.fileTable.createNewFile(fileName);
        return output;
    } 

    // Open an existing file    
    public int fileOpen(String file) {
        int output = disk.fileTable.openFile(file);
        return output;
    } 
    
    public FileOutputStream fileRead(int fd, FileOutputStream buffer, int size) throws IOException
    {
        buffer = disk.fileTable.readFile(fd, buffer, size);
        return buffer;
    } // read

    // Transfer buffer.length bytes from the buffer to the file, starting
    // at the current seek pointer, and add buffer.length to the seek pointer.
    
    public int fileWrite(int fd, FileInputStream buffer, int size) throws IOException
    {
        int output = disk.fileTable.writeFile(fd, buffer, size);
        return output;
    } // write

    public int fileSeek(int fd, int offset) 
    {
        int output = disk.fileTable.fileSeek(fd, offset);
        return output;
    }
    
    public int fileClose(int fd) 
    {
        int output = disk.fileTable.closeFile(fd);
        return output;
    } // close

    // Delete the file with the given inumber, freeing all of its blocks.
    
    public int fileUnlink(String file) 
    {
        int output = disk.fileTable.unlinkFile(file);
        return output;
    } // delete
    
    public FileOutputStream directoryRead(String fd, FileOutputStream buffer, int size) throws IOException
    {
        buffer = disk.fileTable.readDir(fd, buffer, size);
        return buffer;
    }
    
    // Create a new (empty) file and return a file descriptor.    
    public int directoryCreate(String path) {
        int output = disk.fileTable.createDir(path);
        return output;
    }   
    
    public int directorySize(String path)
    {
        return disk.fileTable.sizeDir(path);
    }
    
    public int directoryUnlink(String path)
    {
        return disk.fileTable.unlinkDir(path);
    }

    public void setosErrno(String error)
    {
        this.osErrno = error;
    }
    
    public String getosErrno()
    {
        return this.osErrno;
    }
    
    @Override
    public String toString() {
        throw new RuntimeException("not implemented");
    }
}
