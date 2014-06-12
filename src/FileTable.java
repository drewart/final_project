import java.util.*;
/**
* FileTable class used to store File usage
*/
public class FileTable {

  private final static short FLAG_UNUSED = 0;
  private final static short FLAG_USED   = 1;
  
  private Vector table;
  private Directory dir;
  
  // constructor
  public FileTable( Directory inDir ) {
    table = new Vector();   // instantiate a file (structure) table
    dir = inDir;         // receive a feference to the Direcotry
    
  }
  
  // major public methods
  /**
  * @param filename file name to create/read/write
  * @param mode "r", "w", "w+", or "a"
  */
  public synchronized FileTableEntry falloc( String filename, String mode ) {
    // allocate a new file (structure) table entry for this file name
    // allocate/retrieve and register the corresponding inode using dir
    // increament this inode's count
    // imediately write back this inode to the disk
    /// return a reference to theis file (structure) table entry
    Inode node = null;
    short fileIndex = dir.namei(filename);
    char modeChar = mode.charAt(0);
    
    // file not created
    if (fileIndex == -1)
      fileIndex = dir.ialloc(filename);
    
    if (fileIndex == -1)
      return null;
    
    if (modeChar == 'r') {
      
      node = new Inode(fileIndex);
      if (node.flag == FLAG_UNUSED)
        node.flag = FLAG_USED;
      
    } else if ( modeChar == 'w' || modeChar == 'a') {
        
        node = new Inode();
        node.flag = 2;
    }
      
    node.toDisk(fileIndex);
    FileTableEntry entry = new FileTableEntry(node,fileIndex,mode);
    entry.count += 1;
    table.add(entry);
    
    return entry;
  }
  
  /**
  * @param entry 
  */
  public synchronized boolean ffree( FileTableEntry entry ) {
    // receive a file table entry reference
    // save the corresponding inode to the disk
    // free this file table entry.
    // return true if this file table entry found in my table
    
    if (table.removeElement(entry))
    {
      entry.inode.count -= 1;
      if (entry.inode.flag <= 2)
        entry.inode.flag = 0;
      else if (entry.inode.flag > 2)
        entry.inode.flag = 3;
      
      // write to disk
      entry.inode.toDisk(entry.iNumber);
      
      entry = null; // clear entry
      notify();
      return true;
    }
    return false;
  }
  
  public synchronized boolean fempty() {
    return table.isEmpty();
  }
}
