Phillip Jorgensen
I did everything.

To be frank, I just flat ran out of time. I had begun coding the assignment a couple weeks ago and realized 
that the files were in C. I don't know how to code C or C++. This class has been first and only times I've 
even seen raw C source code. The problem is that by the time the java files came out, I didn't have enough
time left. I took off half of last week from work to do this project, and all three days this week, and I 
coded about 14 hours each day over the weekend and still didn't finish. I tried calling you and emailed
you earlier this week because I had some questions but I did the best I could in terms of converting the
C assignment into Java terms. This is as far as I got, and as of
10:12 PM I finally got it to show no compilation errors for the first time but I have no way of testing 
anything because the testfs.java file that came with the assignment is incredibly archaic and I don't have
time to rewrite it to actually test my code. I've done literally zero debugging. That being said, I'm hoping
for the best. 

The structure is a little unorthodox. The fileTable class runs everything inside the disk and just transports data
inside and outside of it. The JFS is the front end of the disk. It's essentially a UI for the Disk's fileTable.
I added a lot of code that was intended to be for debugging purposes but isn't actually accessible from the Java
File System class. DiskSave actually saves two different kind of files, primarily because I haven't been able to
experiment with the usability of either file type. It saves the "disk" to a RandomAccessFile and a regular File. 
You can provide the JFS with an absolute path and it'll save the files there.

I implemented all of the file, directory, and disk methods listed on the Final Project instruction page. 
I allocated memory based on first-fit guidelines. The fileTable should take the file, divide it into
four-byte pointers, and store them in the first blocks that have open pointers. The overwriting portion was a bit tricky
and made me rewrite half the Inode class. The inode class actually saves both the block numbers and the block's pointer
numbers. That made it easier to figure out what pointers need to be written over and what pointers need new allocated 
space. 

If your testing algorithm is going to be tailored to this, then you can do everything out of the JFS. Method names are
slightly different to better meet Java naming conventions but they accept the same inputs as the C files.

