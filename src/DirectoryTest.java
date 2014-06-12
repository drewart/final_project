

public class DirectoryTest {

  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for ( int j = 0; j < bytes.length; j++ ) {
        int v = bytes[j] & 0xFF;
        hexChars[j * 2] = hexArray[v >>> 4];
        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }
  
  
  public static void printBytes(byte[] bytes) {
    for(int i = 0; i < bytes.length; i++) {
      System.out.print(bytes[i]);
      if (((i+1) % 4) == 0)
        System.out.println();
    }
    System.out.println();
  }
  
  
  
  public static void test1() {
   System.out.println("test1");
    Directory dir = new Directory(10);
    //System.out.println(bytesToHex(dir.directory2bytes()));
    
    short idx = dir.ialloc("foo");

    if (idx == -1)
      System.out.println("bad index");

    if (idx != 1)
      System.out.println("root not set");

    if (dir.namei("foo") != idx)
      System.out.println("namei failure");
    
    if (dir.ifree(idx) == false)
      System.out.println("failure ifree 1");
    
    short id = dir.namei("foo");
    if (id == idx)
      System.out.println("failure ifree 2");
    
    if (id != -1)
      System.out.println("failure ifree 3");
      
    byte[] dirBytes = dir.directory2bytes();
    
    if (dirBytes.length < 1)
      System.out.println("failure ifree 3");
    
      
      //System.out.println(bytesToHex(dirBytes));
  }
  
  public static void test2()
  {
    System.out.println("test2");
    Directory dir = new Directory(4);
    dir.ialloc("foo");
    dir.ialloc("bar");
    dir.ialloc("tar");
    byte[] dirBytes = dir.directory2bytes();
    //System.out.println(bytesToHex(dirBytes));
    dir.bytes2directory(dirBytes);
    
    short id = dir.namei("foo");
    if (id == -1)
       System.out.println("failure bytes2directory 1");
     id = dir.namei("bar");
     if (id == -1)
       System.out.println("failure bytes2directory 2");
     id = dir.namei("tar");
     if (id == -1)
       System.out.println("failure bytes2directory 3");    
     
     byte[] dirBytes2 = dir.directory2bytes();
     if (dirBytes.length != dirBytes2.length)
       System.out.println("directory2bytes issue");
     //System.out.println(bytesToHex(dirBytes));
     
     String a = bytesToHex(dirBytes);
     String b = bytesToHex(dirBytes2);
     if (a.compareTo(b) != 0)
       System.out.println("dirBytes != dirBytes2 issue");
    
    
    
  }
  
  public static void test3()
  {
    System.out.println("test3");
    int myInt = 1;
    byte[] myBytes = new byte[16];
    SysLib.int2bytes(myInt, myBytes, 0);
    myInt = 3;
    SysLib.int2bytes(myInt, myBytes, 4);
    myInt = 4;
    SysLib.int2bytes(myInt, myBytes, 8);
    myInt = 5;
    SysLib.int2bytes(myInt, myBytes, 12);        
    //printBytes(myBytes);
    
    int myInt2 = SysLib.bytes2int( myBytes, 0 );
    if (myInt2 != 1)
      System.out.println("test3 issue 1");
    
    myInt2 = SysLib.bytes2int( myBytes, 4 );
    if (myInt2 != 3)
      System.out.println("test3 issue 2");
    
    myInt2 = SysLib.bytes2int( myBytes, 8 );
    if (myInt2 != 4)
      System.out.println("test3 issue 3");
    
    myInt2 = SysLib.bytes2int( myBytes, 12 );
    if (myInt2 != 5)
      System.out.println("test3 issue 4");
    //System.out.println(myInt2);
  }
  
  public static void main(String args[])
  {
    test1();
    test2();
    test3();
  }
  
  
}
