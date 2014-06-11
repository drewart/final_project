public class FileTable {

  private Vector table;
  private Directory dir;
  
  // constructor
  public FileTable( Direcotry directory ) {
    table = new Vectory();   // instantiate a file (structure) table
    dir = directory;         // receive a feference to the Direcotry
    
  }
  
  // major public methods
  public synchronized FileTableEntry falloc( String filename, string mode ) {
    // allocate a new file (structure) table entry for this file name
    // allocate/retrieve and register the corresponding inode using dir
    // increament this inode's count
    // imediately write back this inode to the disk
    /// return a reference to theis file (structure) table entry
  }
  
  public synchronized boolean ffree( FileTableEntry e ) {
    // receive a file table entry reference
    // save the corresponding inode to the disk
    // free this file table entry.
    // return true if this file table entry found in my table
  }
  
  public synchronized boolean fempty() {
    return table.isEmpty();
  }
}
