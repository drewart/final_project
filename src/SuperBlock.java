//File SuperBlock.java
//Author: Timothy Virgillo
//Team Members: Danielle Jenkins, Drew Pierce

import java.util.*;

/*
 *From Specification
 *Superblock is maintained as the 0 block of the disk. It describes:
 *--the disk block capacity
 *--the number of inodes
 *--the block number of the head block of the free list
 *
 *The OS manages the SuperBlock; No other information must be recorded in, and
 *	no user threads must be able to get access to it *
 */


public class SuperBlock{
	
	private static final int DEFAULT_BLOCK_NUMBER = 1000;
	private static final int DEFAULT_INODE_BLOCKS = 64;
	private static final int BLOCK_BYTE_CAPACITY = 512; //byte capacity of each block
	public int totalBlocks;			// the total number of blocks on the disk
	public int inodeBlocks;			// the number of inodes on the disk
	public int freeList;				// the block number of the free list's head
	
	public SuperBlock(){
		//no arguments. Default to Default_inode_blocks
		try{
			  byte[] superBlock = new byte[BLOCK_BYTE_CAPACITY];
			  SysLib.rawread( 0, superBlock );
			  totalBlocks = SysLib.bytes2int( superBlock, 0 );
			  inodeBlocks = SysLib.bytes2int( superBlock, 4 );
			  freeList = SysLib.bytes2int( superBlock, 8 );
	      
			  //validation check; the disk is already formatted
			  if (( totalBlocks == DEFAULT_BLOCK_NUMBER) && ( inodeBlocks > 0 ) 
					  && ( freeList >= 2 )) 
				  return;
			  else{//format the disk
				  totalBlocks = DEFAULT_BLOCK_NUMBER;    	  	 
				  format();    	  
			  }
		  }catch( Exception e ){ e.printStackTrace(); }	  
	}
  
  public SuperBlock( int diskCapacityInBlocks ){
	  try{
		  byte[] superBlock = new byte[BLOCK_BYTE_CAPACITY];
		  SysLib.rawread( 0, superBlock );
		  totalBlocks = SysLib.bytes2int( superBlock, 0 );
		  inodeBlocks = SysLib.bytes2int( superBlock, 4 );
		  freeList = SysLib.bytes2int( superBlock, 8 );
      
		  //validation check; the disk is already formatted
		  if (( totalBlocks == diskCapacityInBlocks) && ( inodeBlocks > 0 ) 
				  && ( freeList >= 2 )) 
			  return;
		  else{//format the disk
			  totalBlocks = diskCapacityInBlocks;    	  	 
			  format();    	  
		  }
	  }catch( Exception e ){ e.printStackTrace(); }	  
  }

  //write back totalBlocks, inodeBlocks, and freeList to disk
  public void sync(){
      byte[] superBlockDataSyncBuffer = new byte[BLOCK_BYTE_CAPACITY];
      SysLib.int2bytes( totalBlocks, superBlockDataSyncBuffer, 0 );
      SysLib.int2bytes( inodeBlocks, superBlockDataSyncBuffer, 4 );
      SysLib.int2bytes( freeList, superBlockDataSyncBuffer, 8 );
      SysLib.rawwrite( 0, superBlockDataSyncBuffer );     
  }

  private void format(){	  
      try{
    	  format( DEFAULT_INODE_BLOCKS );
      }catch( Exception e ){ e.printStackTrace(); }
  }

  //formats the disk space for the given number of files
  public synchronized void format( int numFilesForFormat )
  {
	  //segments the bytes of the disk to represent 
	  //--disk 0 (the superblock)
	  //--the individual disk blocks for the inodes
	  //--the free list to take the first 2 bytes for each disk block
	  //--initialize the inodes of the blocks
	  
	 
	  byte[] dataBuffer = null;	  
	  inodeBlocks = numFilesForFormat;
	  short iNodeCount;
      //initially, all the space is free space
      //set this way, I can programmically calculate the totalBlocks
      //32 is the byte size per Inode
      //The minimum size of the freelist is 2 elements
      //inodeBlocks, represented as bytes, divided by the byte capacity of the
      //disk
      freeList = 2 + ( inodeBlocks * 32 )/ BLOCK_BYTE_CAPACITY;      
      
      
      /*
       * could either format the blocks first, then the inodes within it,
       * or format the bytes of the disk with the inodes, then segment the
       * blocks.
       * this takes the latter method
       */
      Inode iNode;
      for ( iNodeCount = 0; (int)iNodeCount < inodeBlocks; ++iNodeCount ){
          iNode = new Inode();
          iNode.flag = 0;
          iNode.toDisk( iNodeCount );          
      }
      
      //with the iNodes segmented and initialized
      //we leverage that the freeList to segment the inodes into unique blocks
      for (int freeListHead = freeList; freeListHead < totalBlocks; ++freeListHead){
    	  
          dataBuffer = new byte[BLOCK_BYTE_CAPACITY];
          for ( int i = 0; i < BLOCK_BYTE_CAPACITY; ++i ) {
        	  //zeros the elements of the block
        	  dataBuffer[i] = 0;
          }
          
          //bookkeeeping and writing it to disk
          SysLib.int2bytes( (freeListHead + 1), dataBuffer, 0);
          SysLib.rawwrite( freeListHead, dataBuffer );
      }
      sync();
  }

  //Dequeue the top block from the free list
  public synchronized int getFreeBlock()
  {
      int nextFreeBlock = freeList;
      //validation check
      if (nextFreeBlock != -1){
          byte[] readBuffer = new byte[BLOCK_BYTE_CAPACITY];

          //get the existing block for this index
          SysLib.rawread( nextFreeBlock, readBuffer );
          //reset the index for the freeList
          freeList = SysLib.bytes2int( readBuffer, 0 );
          
          //bookkeeeping and writing it to disk
          SysLib.int2bytes( 0, readBuffer, 0 );
          SysLib.rawwrite( nextFreeBlock, readBuffer );
      }
      return nextFreeBlock; //the index of the block that was just freed
  }

  //enqueu the block to the end of the free list
  public synchronized boolean returnBlock(int indexOfBlock ){
	  
      if ( indexOfBlock >= 0 ){
          byte[] dataBuffer = new byte[BLOCK_BYTE_CAPACITY];
          for (int i = 0; i < BLOCK_BYTE_CAPACITY; ++i){
              //zeros all of the bytes
        	  dataBuffer[i] = 0; 
          }
          
          SysLib.int2bytes( freeList, dataBuffer, 0 );
          //write the block to disk
          SysLib.rawwrite( indexOfBlock, dataBuffer );
          //set the freeList value to the index of the block
          freeList = indexOfBlock;
          return true;
      }
      return false;
  }
}
  
  