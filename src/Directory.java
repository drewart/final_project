public class Directory {
 
  private static int maxChar = 30;  // max char of each file name
  
  // directory entries
  private int fsize[];      // each element stores a differen file size.
  private char fnames[][];  // each element stores a different file name.
  
  // directory constructor
  public Directory( int maxInumber ) {  
    fsize = new int[maxInumber];    // maxInumber = max files
    
    // init file size to 0
    for( int i = 0; i < maxInumber; i++ )
      fsize[i] = 0;
    
    fnames = new char[maxInumber]maxChar];
    String root = "/"                         // entry(inode) 0 is "/"
    fsize[0] = root.length( );                // fsize[0] is the size of "/".
    root.getChars( 0, fsize[0], fnames[0], 0 );  // fnames includes "/"
  }
  
  public int bytes2directory( byte data[] ) {
   // assumes data[] received directory information from disk
   // initializes the directory instance with the data[]
  }
  
  public byte[] directory2bytes() {
    // converts and return Directory information into a plain byte array
    // this byte array will be writeen back to disk
    // note: only meaningfull directory information should be converted
    // into bytes
  }
  
  public short ialloc( string filename ) {
    
  }
  
  
  
  
}
