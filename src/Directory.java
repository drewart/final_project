public class Directory {
 
  private static int maxChar = 30;  // max char of each file name
  
  // directory entries
  private int fsize[];      // each element stores a file string size.
  private char fnames[][];  // each element stores a file name in char.
  
  // directory constructor
  public Directory( int maxInumber ) {  
    fsize = new int[maxInumber];    // maxInumber = max files
    
    // init file size to 0
    for( int i = 0; i < maxInumber; i++ )
      fsize[i] = 0;
    
    fnames = new char[maxInumber][maxChar];
    String root = "/";                         // entry(inode) 0 is "/"
    fsize[0] = root.length( );                // fsize[0] is the size of "/".
    root.getChars( 0, fsize[0], fnames[0], 0 );  // fnames includes "/"
  }
  
  
  // initialize directory with byte[] from disk
  public void bytes2directory( byte data[] ) {
   // assumes data[] received directory information from disk
   // initializes the directory instance with the data[]
   int offset = 0;
   for ( int i = 0; i < fsize.length; i++,offset += 4 ) // 4 byte int
   {
     fsize[i] = SysLib.bytes2int( data, offset );
     
     System.out.println(i + " : " + fsize[i] + " offset: " + offset);
   }
   
   DirectoryTest.printBytes(data);
   
   for (int i = 0; i < fnames.length; i++, offset += maxChar * 2) // 2 byte per char 
   {
     String fname = new String(data, offset,maxChar*2);
     fname.getChars(0, fsize[i], fnames[i], 0 );
   }
   DirectoryTest.printBytes(data);
  }
  
  // converts directory information into byte[]
  public byte[] directory2bytes() {
    // converts and return Directory information into a plain byte array
    // this byte array will be writeen back to disk
    // note: only meaningfull directory information should be converted
    // into bytes
    int byteSize = fsize.length * 4 + fnames.length * maxChar * 2;
    byte[] data = new byte[byteSize];
    int offset = 0;
    for( int i = 0; i < fsize.length; i++, offset += 4 )
    {
      System.out.println(i + " : " + fsize[i] + " offset" + offset);
      SysLib.int2bytes(this.fsize[i], data, offset);
      
    }
    
    for( int i = 0; i < fnames.length; i++, offset += maxChar * 2)  
    { 
        String str = new String(this.fnames[i], 0, fsize[i]);
       byte[] strBytes = str.getBytes();
      System.arraycopy(strBytes, 0, data, i, strBytes.length);
    }
    return data;
  }
  
  // Allocate an iNumber
  public short ialloc( String filename ) {
    // filename taken
    if (namei(filename) != -1)
      return -1;
      
    for(int i = 1; i < fsize.length; i++)
    {

      // look for empty
      if (fsize[i] == 0)
      {
        this.fsize[i] = (filename.length() > maxChar) ? maxChar : filename.length();
        filename.getChars(0, filename.length() , fnames[i],0);
        return (short)i;
      }
    }
    return -1;
  }
  
  // deallocate the iNumber
  public boolean ifree( short iNumber ) {
    if (iNumber > fsize.length || iNumber < 0)
      return false;
    
    fsize[iNumber] = 0;
    return true;
  }
  
  // returns this file's iNumber
  public short namei( String filename )
  {
    for(int i = 0; i < fnames.length; i++)
    {
      if (filename.compareTo(new String(this.fnames[i], 0, this.fsize[i])) == 0)
        return (short)i;
    }
    return -1;
  }
  
  
  
  
}
