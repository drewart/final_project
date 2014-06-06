public class Inode {
  
  private final static int iNodeSize = 32;   // fix to 32 bytes
  private final static int directSize = 11;  // # direct pointers
  
  public int length;                                // file size in bytes
  public short count;                               // # file-table entries pointing to this
  public short flag;                                // 0 = unused, 1 = used, ...
  public short direct[] = new short[directSize];
  public short indirect;                            // a indirect pointer
  
  Inode() {
      length = 0;
      count = 0;
      flag = 1;
      for ( int i = 0; i < directSize; i++ )
        direct[i] = -1;
      indirect = -1;
      
  }
  
  // retrieving inode from disk
  Inode( short iNumber ) {
    // TODO: design
  }
  
  // save to disk as the i-th inode
  int toDist( short iNumber ) {
    
  }

}
