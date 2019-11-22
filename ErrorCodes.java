import java.util.*;
import java.io.*;

public class ErrorCodes extends Exception{
    private final Properties diskErrors = new Properties();
    private final Properties osErrors = new Properties();   
    private FileOutputStream diskErrOutputStream;
    private FileOutputStream osErrOutputStream;
    private final ArrayList<String> diskErr = new ArrayList<>();
    private final ArrayList<String> osErr = new ArrayList<>();
    
    public ErrorCodes(File diskFile, File osFile) throws IOException
    {
        diskErrOutputStream = new FileOutputStream(diskFile, true);
        osErrOutputStream = new FileOutputStream(osFile, true);
    }
    
    public int getDiskOutFile() throws IOException
    {
        return this.diskErrOutputStream.getFD().hashCode();
    }
    
    public int getOSOutFile() throws IOException
    {
        return this.osErrOutputStream.getFD().hashCode();
    }
    
    public void finalizeErrors() throws IOException
    {
        diskErrors.store(diskErrOutputStream, diskErr.toString());
        osErrors.store(osErrOutputStream, osErr.toString());
    }
    
    public void setDiskErrOut(File file) throws IOException
    {
        this.diskErrOutputStream = new FileOutputStream(file, true);
    }
    
    public Properties getDiskErrOut()
    {
        return this.diskErrors;
    }
    
    public void setOSErrOut(File file) throws IOException
    {
        this.osErrOutputStream = new FileOutputStream(file, true);
    }
    
    public Properties getOsErrOut()
    {
        return this.osErrors;
    }
    
    public void newDiskErr(String err)
    {
        this.diskErr.add(err);
    }
    
    public void newOsErr(String err)
    {
        this.osErr.add(err);
    }
}
