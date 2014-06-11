// Danielle Jenkins
// CSS430
// 06/07/14

public class FileSystem extends Thread
{
    private FileTable filetable;
    private SuperBlock superblock;
    private Directory directory;
    private static final int SEEK_SET = 0;
    private static final int SEEK_CUR = 1;
    private static final int SEEK_END = 2;
 
    // Constructor for the FileSystem
    public FileSystem(int diskBlocks)
    {
        // Create superblock, and format disk with 64 inodes in default
        this.superblock = new SuperBlock(diskBlocks);
        // create directory, and register "/" in directory entry 0
        this.directory = new Directory(this.superblock.inodeBlocks);
        // file table is created, and store directory in the file table
        this.filetable = new FileTable(this.directory);

        // directory reconstruction
        FileTableEntry dirEnt = open("/", "r");
        // Get the size of the FileTableEntry
        int dirSize = fsize(dirEnt);
        if (dirSize > 0) 
        {    
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData); // Copy directory entry into dirData
            this.directory.bytes2directory(dirData);
        }
        close(dirEnt);
    }
     
    // opens the file specified by the fileName string in the given mode (where
    // "r" = ready only, "w" = write only, "w+" = read/write, "a" = append), 
    // and allocates a new file descriptor, fd to this file. The file is 
    // created if it does not exist in the mode "w", "w+" or "a". SysLib.open 
    // must return a negative number as an error value if the file does not 
    // exist in the mode "r". Note that the file descriptors 0, 1, and 2 are 
    // reserved as the standard input, output, and error, and therefore a newly 
    // opened file must receive a new descriptor numbered in the range between 
    // 3 and 31. If the calling thread's user file descriptor table is full, 
    // SysLib.open should return an error value. The seek pointer is 
    // initialized to zero in the mode "r", "w", and "w+", whereas initialized 
    // at the end of the file in the mode "a".
    FileTableEntry open(String filename, String mode) 
    {
        // Get the entry corresponding to the filename
        FileTableEntry ftEnt = this.filetable.falloc(filename, mode);
        // If the mode is write and not all the blocks have been deallocated...
        if (mode.equals("w") && !deallocAllBlocks(ftEnt))
        {
            return null; // return null since we have to write
        }
        return ftEnt;  // Otherwise return opened file
    }
 
    // Closes the file corresponding to fd, commits all file transactions on 
    // this file, and unregisters fd from the user file descriptor table of the
    // calling thread's TCB. The return value is 0 in success, otherwise -1.
    boolean close(FileTableEntry ftEnt)
    {
        synchronized (ftEnt)
        {
            // decrement since the thread's no longer using it
            ftEnt.count -= 1;
            // So long as there are threads are still using it
            if (ftEnt.count > 0)
            {
                return true;
            }
        } // once we run out
        // save the inode, free the FT entry, and return true if in the table 
        return this.filetable.ffree(ftEnt);
    }
 
    // returns the size in bytes of the file indicated by fd.
    int fsize(FileTableEntry ftEnt)
    {
        synchronized (ftEnt) 
        {
            return ftEnt.inode.length;
        }
    }
     
    // Destroys the file specified by fileName. If the file is currently open, 
    // it is not destroyed until the last open on it is closed, but new 
    // attempts to open it will fail.
    boolean delete(String filename)
    {
        // get the FT entry
        FileTableEntry ftEnt = open(filename, "w");

        // Get the entry's inode number
        short iNum = ftEnt.iNumber; 

        // only if completely closed and deallocate the inode number so it will
        // fail upon any new attempts to open it.
        return close(ftEnt) && this.directory.ifree(iNum);
    }

}
