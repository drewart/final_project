/**
 * User/Author: Drew Pierce
 * Team: Danielle Jenkins, Drew Pierce,Timothy Virgillo
 * Date: 6/12/14
 *
 * Description:
 *  FileTable class used to handle the working File read and writes in the ThreadOS
 */
import java.util.*;
/**
* FileTable class used to store File usage
*/
public class FileTable {

  private final static short FLAG_UNUSED = 0;
  private final static short FLAG_USED   = 1;
  private final static short FLAG_USED_WRITE = 2;
  
  private Vector table;
  private Directory dir;
  
  // constructor
  public FileTable( Directory inDir ) {
    table = new Vector();   // instantiate a file (structure) table
    dir = inDir;         // receive a feference to the Direcotry
    
  }
  
  // major public methods
  /**
   * method to add or create a file in the FileTable
  * @param filename file name to create/read/write
  * @param mode "r", "w", "w+", or "a"
  */
  public synchronized FileTableEntry falloc( String filename, String mode ) {
    // allocate a new file (structure) table entry for this file name
    // allocate/retrieve and register the corresponding inode using dir
    // increment this inode's count
    // immediately write back this inode to the disk
    /// return a reference to this file (structure) table entry
    Inode node = null;
    short fileIndex;
    char modeChar = mode.charAt(0);

    // look up file
    fileIndex = dir.namei(filename);

    // file not created
    if (modeChar == 'r') {

      // if no file to read return null object
      if (fileIndex == -1)
          return null;

      node = new Inode(fileIndex);
      if (node.flag == FLAG_UNUSED)
        node.flag = FLAG_USED;
      
    } else if ( modeChar == 'w' || modeChar == 'a') {

        // if file does not exit create for write and append
        if (fileIndex == -1)
            fileIndex = dir.ialloc(filename);

        node = new Inode();
        node.flag = FLAG_USED_WRITE;
    }
      
    node.toDisk(fileIndex);
    FileTableEntry entry = new FileTableEntry(node,fileIndex,mode);
    entry.count += 1;
    table.add(entry);
    
    return entry;
  }
  
  /**
  * Method to remove a FileEntry from the FileTable
  * @param entry File entry to remove from the FileTable
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

    /**
     * method to check if the file table is empty
     * @return if the table is empty
     */
  public synchronized boolean fempty() {
    return table.isEmpty();
  }
}
