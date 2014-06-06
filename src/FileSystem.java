//FileSystem
public class FileSystem {
	private SuperBock superBlock;
	private Directory directory;
	private FileTable fileTable;
	
	public FileSystem( int diskBlocks )
	{
	  // create SuperBlock and format disk with 64 inodes in default
	  supperBlock = new SuperBlock( diskBlocks );
	  
	  // create direcotry and register "/" in directory entry 0
	  directory = new Directory ( superBlock.inodeBlocks );
	  
	  // file table is created, and store directory int he file table
	  fileTable = new FileTable( directory );
	  // direcotry reconstruction
	  FileTableEntry dirEnt = open( "/", "r" );
	  int dirSize = fsize( dirEnt );
	  if ( dirSize > 0 ) {
	    byte[] dirData = new byte[dirSize];
	    read( dirEnt, dirData );
	    direcotry.byte2directory( dirData );
	  }
	  close( dirEnt );
	  
	}
	
	void sync() { 
	  //TODO:
	}
	
	FileTableEntry open( String  fileName, String mode) {
	  //TODO:
	}
	
	boolean close( FileTableEntry ftEnt )	{
	  //TODO:
	}
	
	int fsize( FileTableEntry ftEnt ) {
	  // TODO:
	}
	
	int read( FileTableEntry ftEnt, byte[] buffer )	{
	    // TODO:
	}
	
	int write( FileTableEntry ftEnt, byte[] buffer ) {
	    // TODO:
	}
  
	private boolean deallocAllBlocks( FileTableEntry ftEnt ) {
     // TODO:
  }
  
  boolean delete( String finename ) {
    // TODO:
  }
  
  private final int SEEK_SET = 0;
  private final int SEEK_CUR = 1;
  private final int SEEK_END = 2;
  
  int seek( fileTableEntry ftEnt, int offset, int whence ) {
    // TODO:
  }
  
}
