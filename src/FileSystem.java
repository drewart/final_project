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
        // filetable is created, and store directory in the filetable
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
     
    // Formats the disk, (i.e., Disk.java's data contents). The parameter files 
    // specifies the maximum number of files to be created, (i.e., the number 
    // of inodes to be allocated) in your file system. The return value is 0 on 
    // success, otherwise -1.
    boolean format(int files)
    { 
        // Validate input
        if (files > 0)
        {
            // Call the superblock's format
            this.superblock.format(files);
            return true;
        }
        return false;
    }

    // Opens the file specified by the fileName string in the given mode (where
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
            // return null since we have to write
            return null; 
        }
        // Otherwise return opened file
        return ftEnt;  
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
     
    // Reads up to buffer.length bytes from the file indicated by fd, starting 
    // at the position currently pointed to by the seek pointer. If bytes 
    // remaining between the current seek pointer and the end of file are less 
    // than buffer.length, SysLib.read reads as many bytes as possible, putting 
    // them into the beginning of buffer. It increments the seek pointer by the 
    // number of bytes to have been read. The return value is the number of 
    // bytes that have been read, or a negative value upon an error.
    int read(FileTableEntry ftEnt, byte[] buffer)
    {
        // number of bytes read
        int readBytes = 0;
        // number of bytes to read
        int lengthRead = buffer.length;
        // Validate input
        if (ftEnt.mode.equals("w") || ftEnt.mode.equals("a")) 
        {
            return -1;
        }

        // enter the CS
        synchronized (ftEnt) 
        {
            while (lengthRead > 0)
            {
                // break out if gone beyond the filetable entry
                if (ftEnt.seekPtr >= fsize(ftEnt))
                {
                    break;
                }
                // Get the current block that the pointer is pointing to 
                int currBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                if (currBlock == -1) 
                {
                    return -1;
                }

                // Create a temporary block to copy into
                byte[] tmpBlock = new byte[Disk.blockSize];

                // Read the contents of the current block into the temp block
                int readStatus = SysLib.rawread(currBlock, tmpBlock);
                if (readStatus == -1)
                {
                    return -1;
                }

                // Get the current pointer offset
                int offset = ftEnt.seekPtr % 512;

                // Find the remaining bytes in the block
                int bytesInBlock = 512 - offset;

                // Find the remaining bytes  in the filetable entry
                int bytesInFtEnt = fsize(ftEnt) - ftEnt.seekPtr;

                // Get the shortest remaining bytes
                int rBytes = Math.min(Math.min(bytesInBlock, lengthRead), bytesInFtEnt);

                // Copy the data to the buffer
                System.arraycopy(tmpBlock, offset, buffer, readBytes, rBytes);

                // Update seek pointer
                ftEnt.seekPtr += rBytes;

                // Update bytes read thusfar
                readBytes += rBytes;

                // Update bytes left to read
                lengthRead -= rBytes;
            }
            return readBytes;
        }
    }
}