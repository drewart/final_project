//Danielle Jenkins

# FileSystem

## constructor
The filesystem constructor initializes three main objects: it creates a superblock along with formatting the disk with 64 inodes by default, creats a directory object and registers '/' in directory entry 0, and it creates a filetable and stores the directory in the filetable. It also reads the contents of the root directory into the directory entry and copies all that data into the directory.

## format
The format method has the requirement of formatting the disk by taking in the number of files to be created as a parameter. My method does this by first checking validating if there are any files before calling the superblock's format method. and returning true. If the file parameter is invalid however, it returns false.

## open
The open method has the requirement of opening the file specified by the filename passed in in the parameter and is supposed create a file if it doesn't exist and the write mode or append mode is used. It accomplishes this by getting the file table entry corresponding to the filename in the filetable. It then checks if the mode is write and if not all the blocks have been deallocated yet. If they have it returns null, otherwise though it returns the opened file.

## close
The close method has the requirement of closing the file corresponding to the file descriptor that is passed in as a parameter. The close method is then supposed to commit all file transactions and unregister the file descriptor from the user file descriptor in the calling thread's TCB. It accomplishes this by decrementing the filetable entry's thread counter each time a close is called. While it is, it returns true, however, if the count ever is zero, it calls the filetable's ffree which saves the corresponding inode, free's the filetable entry from the TCB and returns true if it is in the table.

## fsize
The fsize method has the requirement of returning the size in bytes of the file indicated by the fd. It takes in a file table entry and returns the length of the file table entry's inode.

## delete
The delete method has the requirement of destroying the file specified by teh filename, but ensuring that it will not be destroyed until the last open is closed and all new attempts to open it should fail. It gets the file table entry by calling an open with the mode "write". It then get's the inode's number from that file table entry and returns a close call on it with the file table entry and a call to ifree using that inode number. I chose to do it this way, because this way it meets the requirements of not being destroyed until the last open on it is closed (the 'close' part) and it calls the directory's ifree method to deallocate the inode number which will cause all new attempts to open it to fail.

## read
The read method has the requirement of being able to read the buffer length in bytes from the buffer that is passed in as a parameter by starting at the position pointed to by the seek pointer. Also, if any bytes are remainbeing between the curernt seek pointer and the end of the file are less than the buffer length then it needs to read as many bytes as possible and add those to the beginning of the buffer. Finally, it needs to increment the seek pointer by the number of bytes it's read and return the number of bytes read.

Our read method accomplishes this by using two variables, readBytes and lengthRead to hold the number of bytes that have been read and the length of bytes left to read. It then validates that we're in fact in read mode before entering the critical section (the filetable entry) via the javascript synchronized command. Within the critical section, we loop while we still have bytes left to read. If we've gone beyond the filetable entry, we break out. Otherwise, we get the current block that the pointer is pointing to and create a temporary data block before reading the contents of the block the seek pointer was pointing to into the temporary block. Next, we get the offset and determine the remaining bytes in the the block and in the filetable entry as a whole. After that, we use the min method in the math library to get the shortest of the three remaining variables in order to determine the degree we increment the seek pointer and readBytes variable by. Finally, we copy all the data to the buffer, update the seek pointer, readBytes, and lengthRead variables before returning the number of bytes read.



