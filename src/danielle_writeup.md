//Danielle Jenkins

# FileSystem

## constructor
The file system constructor initializes three main objects: it creates a super-block along with formatting the disk with 64 inodes by default, creates a directory object and registers '/' in directory entry 0, and it creates a file-table and stores the directory in the file table. It also reads the contents of the root directory into the directory entry and copies all that data into the directory.

## sync
The sync method writes the directory information to disk. Our method accomplishes this by opening the root in write mode as a dentry, loading the directory data into a temporary buffer. Writing that buffer the to root file in the directory entry and finally calling super-block's sync method to write the contents of directory to disk.

## format
The format method has the requirement of formatting the disk by taking in the number of files to be created as a parameter. My method does this by first checking validating if there are any files before calling the super-block's format method. and returning true. If the file parameter is invalid however, it returns false.

## open
The open method has the requirement of opening the file specified by the filename passed in in the parameter and is supposed create a file if it doesn't exist and the write mode or append mode is used. It accomplishes this by getting the file table entry corresponding to the filename in the file table. It then checks if the mode is write and if not all the blocks have been deallocated yet. If they have it returns null, otherwise though it returns the opened file.

## close
The close method has the requirement of closing the file corresponding to the file descriptor that is passed in as a parameter. The close method is then supposed to commit all file transactions and unregister the file descriptor from the user file descriptor in the calling thread's TCB. It accomplishes this by decrementing the file table entry's thread counter each time a close is called. While it is, it returns true, however, if the count ever is zero, it calls the file table's free which saves the corresponding inode, frees the file table entry from the TCB and returns true if it is in the table.

## fsize
The fsize method has the requirement of returning the size in bytes of the file indicated by the fd. It takes in a file table entry and returns the length of the file table entry's inode.

## read
The read method has the requirement of being able to read the buffer length in bytes from the buffer that is passed in as a parameter by starting at the position pointed to by the seek pointer. Also, if any bytes are remain being between the current seek pointer and the end of the file are less than the buffer length then it needs to read as many bytes as possible and add those to the beginning of the buffer. Finally, it needs to increment the seek pointer by the number of bytes it's read and return the number of bytes read.

Our read method accomplishes this with the following algorithm. With two variables, readBytes and lengthRead to hold the number of bytes that have been read and the length of bytes left to read, we then validates that we're in fact in read mode before entering the critical section (the file-table entry) via the Java synchronized command. Within the critical section, we loop while we still have bytes left to read. If we've gone beyond the file table entry, we break out. Otherwise, we get the current block that the pointer is pointing to and create a temporary data block before reading the contents of the block the seek pointer was pointing to into the temporary block. Next, we get the offset and determine the remaining bytes in the the block and in the file table entry as a whole. After that, we use the min method in the math library to ensure that remaining bytes we have left to read and the remaining bytes left in the file table entry are not greater than the bytes available. We then use this to increment the seek pointer and readBytes variables. Finally, we copy all the data to the buffer, update the seek pointer, readBytes, and lengthRead variables before returning the number of bytes read.

## write
The write method has the requirements of writing the contents of it's buffer parameter to the inode in the file table entry by starting at the position of the seek pointer. The operation should be able to overwrite existing data in the file or append to the end of it. Afterwards, it should return the number of bytes that have been written, unless there's an error, in which case it should return -1.

My method accomplishes this through the following algorithm. First, it keeps track of the number of bytes that have been read and the number of bytes left to read. Next, validates that the entry is not in read mode before calling synchronized on the file table entry parameter. While there are still bytes left to read, it gets the current block that the pointer pointed to and checks if it is valid. If it is, it gets the next free block, and registers that block with the inode. If the space was in use or if a direct block was skipped it returns an error, if the indirect blocks were unassigned it attempts to assign and register it. After that it updates the current block by setting it to the next free block.

A temporary block is created and the contents of the current block are copied into it. Next, we determine the offset by performing mod 512 on the seek pointer. Then we subtract that value from the size of the disk in order to get the number of bytes available. We use the math min method again to ensure that the length we have left to read is not greater than the number of bytes available before we copy the data from the buffer that was passed in into the temporary block. Finally, we write the contents of the temporary block to the current block, update the seek pointer and bytes read and bytes left to read variables, and save the inode's contents to disk.

## deallocAllBlocks
The deallocateAllBlocks method has the requirements of being able to empty the inode and delete any freed blocks. It accomplishes this with the following algorithm. 

First, we check if the inode is being used, if it is we return false since we cannot deallocate a used inode. Otherwise, we iterate through each direct block in the inode and deallocate it if it hasn't been deallocated. During this process we also set it's pointer to invalid (-1) to mark it as having been deallocated. Next, we have to deallocate the indirect blocks. We deallocate the index block and get the data. Then we loop over the blocks in the data, converting each block to a short before passing it into super-block's returnBlock method which frees the block. Finally, if all has gone well, we write the inode's contents to disk and return true.

## delete
The delete method has the requirement of destroying the file specified by the filename, but ensuring that it will not be destroyed until the last open is closed and all new attempts to open it should fail. It gets the file table entry by calling an open with the mode "write". It then gets the inode's number from that file table entry and returns a close call on it with the file table entry and a call to ifree using that inode number. I chose to do it this way, because this way it meets the requirements of not being destroyed until the last open on it is closed (the 'close' part) and it calls the directory's ifree method to deallocate the inode number which will cause all new attempts to open it to fail.

## seek
The seek method has the requirements of being able to update the seek pointer according to the file descriptor that's passed in and it's offset and whence values.

It accomplishes this with the following algorithm. It locks the file entry by calling Java's synchronized method. Then it updates the seek pointer differently depending the value of whence. If whence is 0, then the seek pointer is set to the offset bytes from the beginning of the file. If whence is 1, then the seek pointer is set to it's current value plus the offset. If whence is 2, then the seek pointer is set to the size of the file plus the offset. Finally, we return the updated seek pointer.

## Performance Consideration
I would maybe look into using something like Java's "mappedbytebuffer" which can replace the read and write methods and is more efficient at writing to disk. However, I'd have to research a bit more to determine whether it can be properly integrated into ThreadOS to the extent that we need.

## Extending Functionality

