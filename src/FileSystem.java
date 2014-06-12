// Danielle Jenkins
// Team Members: Timothy Virgillo, Drew Pierce
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
    
    // Writes the directory information to disk. 
    void sync()
    {
        // Open the root in write mode as the dentry
        FileTableEntry dirEnt = open("/", "w");

        // Load the directory data into a buffer
        byte[] dirData = this.directory.directory2bytes();

        // Write that data to the root file
        write(dirEnt, dirData);

        // Close it since we're done
        close(dirEnt);

        // Write it all to disk
        this.superblock.sync();
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
        // Validate that we're in read mode
        if (ftEnt.mode.equals("w") || ftEnt.mode.equals("a")) 
        {
            return -1;
        }

        // enter the CS
        synchronized (ftEnt) 
        {
            // While we still have bytes left to read...
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

                // Get the current pointer offset (if 0, it's a new block)
                int offset = ftEnt.seekPtr % 512;

                // Find the bytes available in the block
                int bytesAvail = 512 - offset;

                // Find the remaining bytes in the file table
                int bytesInFtEnt = fsize(ftEnt) - ftEnt.seekPtr;

                // Get the remaining number of bytes that need to be read.
                int rBytes = Math.min(bytesAvail, lengthRead);
                rBytes = Math.min(rBytes, bytesInFtEnt);

                // Copy the data to the buffer from the block
                System.arraycopy(tmpBlock, offset, buffer, 
                        readBytes, rBytes);

                // Update seek pointer
                ftEnt.seekPtr += rBytes;

                // Update bytes read thusfar
                readBytes += rBytes;

                // Update bytes left to read
                lengthRead -= rBytes;
            }
            // Return the number of bytes reads
            return readBytes;
        }
    }

    // Write the contents of it's buffer parameter to the inode in the 
    // filetable entry by starting at the position of the seek pointer. 
    // The operation should be able to overwrite existing datwa in the file or 
    // append to the end of it. Afterwards, it should return the number of 
    // bytes that have been written, unless there's an eror, in which case it 
    // should return -1.
    int write(FileTableEntry ftEnt, byte[] buffer)
    {
        // number of bytes read
        int readBytes = 0;
        // number of bytes to read
        int lengthRead = buffer.length;
        // Validate that we're not in read mode
        if (ftEnt.mode.equals("r"))
        {
            return -1;
        }
        // enter the CS
        synchronized (ftEnt) 
        {
            Inode inode = ftEnt.inode;

            // While we still have bytes left to read...
            while (lengthRead > 0)
            {
                // Get the current block that the pointer is pointing to 
                int currBlock = inode.findTargetBlock(ftEnt.seekPtr);

                // if the current block is invalid
                if (currBlock == -1) 
                {
                    // Get the next free block
                    short block = (short)this.superblock.getFreeBlock();

                    // Register that block with the inode
                    int stat = inode.registerTargetBlock(ftEnt.seekPtr, block);
                    if (stat == -1)
                    {
                        return -1;
                    }
                    if (stat == -2)
                    {
                        // A direct block was skipped. This could cause
                        // external fragmentation.
                        return -1;
                    }
                    if (stat == -3) 
                    {
                        // Indirect is unassigned, attempt to assign it
                        short blk = (short)this.superblock.getFreeBlock();
                        if(!inode.registerIndexBlock(blk))
                        {
                            return -1;
                        }
                        // Attempt to register it
                        if(inode.registerTargetBlock(ftEnt.seekPtr, block) != 0)
                        {
                            return -1;
                        }
                    }
                    // Set the current block to the next free block
                    currBlock = block;
                }

                // Create a temporary block to copy into
                byte[] tmpBlock = new byte[512];

                // Read the contents of the current block into the temp block
                int readStatus = SysLib.rawread(currBlock, tmpBlock);
                if (readStatus == -1) 
                {
                    return -1;
                }

                // Get the current pointer offset (if 0, it's a new block)
                int offset = ftEnt.seekPtr % 512;

                // Find the number of bytes available in the block
                int bytesAvail = 512 - offset;

                // The bytes left to read cannot be greater than the available
                int rBytes = Math.min(bytesAvail, lengthRead);

                // Copy the data from the buffer to the block
                System.arraycopy(buffer, readBytes, tmpBlock, 
                        offset, rBytes);

                // Write the contents of the temp block to the target block
                SysLib.rawwrite(currBlock, tmpBlock);

                // Update seek pointer
                ftEnt.seekPtr += rBytes;

                // Update bytes read thusfar
                readBytes += rBytes;

                // Update bytes left to read
                lengthRead -= rBytes;

                // Once we reach the end of the inode...
                if (ftEnt.seekPtr > inode.length) 
                {
                    // Set the pointer to the end of the inode
                    inode.length = ftEnt.seekPtr;
                }
            }
            // Save the inode to disk
            inode.toDisk(ftEnt.iNumber);

            // Return the number of bytes written
            return readBytes;
        }
    }
 
    //Empty the inode, delete any freed blocks
    private boolean deallocAllBlocks(FileTableEntry ftEnt)
    {
        // check if inode is being used
        if (ftEnt.inode.count != 1) 
        {
            // it is being used
            return false;
        }

        // For each direct pointer
        for (int i = 0; i < 11; i++)
        {
            // If the block hasn't been deallocated yet
            if (ftEnt.inode.direct[i] != -1) 
            {
                // deallocate direct blocks
                this.superblock.returnBlock(ftEnt.inode.direct[i]);
                // set the block to deallocated
                ftEnt.inode.direct[i] = -1;
            }
        }

        // Deallocate the indirect blocks,
        // deallocate index block and get data
        byte[] bData = ftEnt.inode.unregisterIndexBlock();
        
        // First check if there was data
        if (bData != null) 
        {
            // loop over the blocks
            while (true) 
            {
                // convert the data to a short
                short sData = SysLib.bytes2short(bData, 0);
                if (sData == -1){
                    break;
                }
                // free the block
                this.superblock.returnBlock(sData);
            }
        }
        // Write the inode to disk
        ftEnt.inode.toDisk(ftEnt.iNumber);

        // Great success!
        return true;
    }

    // Destroys the file specified by fileName. If the file is currently open, 
    // it is not destroyed until the last open on it is closed, but new 
    // attempts to open it will fail.
    boolean delete(String filename)
    {
        // get the FT entry
        FileTableEntry ftEnt = open(filename, "w");
        if (ftEnt == null)
        {
            System.out.println("NULL!!!@!!0");
        }
        // Get the entry's inode number
        short iNum = ftEnt.iNumber; 

        // only if completely closed and deallocate the inode number so it will
        // fail upon any new attempts to open it.
        return close(ftEnt) && this.directory.ifree(iNum);
    }

    // Updates the seek pointer corresponding to the file descriptor. 
    int seek(FileTableEntry ftEnt, int offset, int whence)
    {
        // verify that the filetable entry isn't null
        if (ftEnt == null){
            return -1;
        }
        // Enter the CS
        synchronized (ftEnt)
        {
            switch (whence) 
            {
                case SEEK_SET:
                    // The file's seek pointer is set to offset bytes from the 
                    // beginning of the file.
                    if ((offset >= 0) && (offset <= fsize(ftEnt)))
                    {
                        ftEnt.seekPtr = offset;
                    }
                    else
                    {
                        return -1;
                    }
                    break;
                case SEEK_CUR:
                    // The file's seek pointer is set to its current value plus 
                    // the offset. The offset can be positive or negative.
                    if ((ftEnt.seekPtr + offset >= 0) && 
                            (ftEnt.seekPtr + offset <= fsize(ftEnt)))
                    {
                        ftEnt.seekPtr += offset;
                    }
                    else
                    { 
                        return -1;
                    }
                    break;
                case SEEK_END:
                    // The file's seek pointer is set to the size of the file 
                    // plus the offset. The offset can be positive or negative.
                    if ((fsize(ftEnt) + offset >= 0) && 
                            (fsize(ftEnt) + offset <= fsize(ftEnt)))
                    {
                        ftEnt.seekPtr = (fsize(ftEnt) + offset);
                    }
                    else
                    {
                        return -1;
                    }
                    break;
            }
            // return the updated seek pointer
            return ftEnt.seekPtr;
        }
    }
}
