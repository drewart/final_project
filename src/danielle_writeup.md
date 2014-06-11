//Danielle Jenkins

# FileSystem

## close
The close method has the requirement of 

## fsize
The fsize method has the requirement of returning the size in bytes of the file indicated by the fd. It takes in a file table entry and returns the length of the file table entry's inode.

## Delete
The delete method has the requirement of destroying the file specified by teh filename, but ensuring that it will not be destroyed until the last open is closed and all new attempts to open it should fail.

It gets the file table entry by calling an open with the mode "write". It then get's the inode's number from that file table entry and returns a close call on it with the file table entry and a call to ifree using that inode number. I chose to do it this way, because this way it meets the requirements of not being destroyed until the last open on it is closed (the 'close' part) and it calls the directory's ifree method to deallocate the inode number which will cause all new attempts to open it to fail.

