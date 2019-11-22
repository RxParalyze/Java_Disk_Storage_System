public class SuperBlock{
    public final int magicNum;

    public SuperBlock(int magNum)
    {
        this.magicNum = magNum;
    }
    
    @Override
    public String toString () {
        return
            "SUPERBLOCK MAGIC NUMBER: " + this.magicNum + "/n";
    }
}
