

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
    // directory test
    Directory dir = new Directory(10);
    System.out.println(bytesToHex(dir.directory2bytes()));
    
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
    
      
      System.out.println(bytesToHex(dirBytes));
  }
  
  public static void test2()
  {
    Directory dir = new Directory(4);
    dir.ialloc("foo");
    dir.ialloc("bar");
    dir.ialloc("tar");
    byte[] dirBytes = dir.directory2bytes();
    System.out.println(bytesToHex(dirBytes));
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
     
     dirBytes = dir.directory2bytes();
     System.out.println(bytesToHex(dirBytes));
    
    
    
  }
  
  public static void test3()
  {
    int myInt = 1;
    byte[] myBytes = new byte[16];
    SysLib.int2bytes(myInt, myBytes, 0);
    myInt = 3;
    SysLib.int2bytes(myInt, myBytes, 4);
    myInt = 3;
    SysLib.int2bytes(myInt, myBytes, 8);
    myInt = 3;
    SysLib.int2bytes(myInt, myBytes, 12);        
    printBytes(myBytes);
    
    int myInt2 = SysLib.bytes2int( myBytes, 0 );
    System.out.println(myInt2);
    myInt2 = SysLib.bytes2int( myBytes, 4 );
    System.out.println(myInt2);
    myInt2 = SysLib.bytes2int( myBytes, 8 );
    System.out.println(myInt2);
    myInt2 = SysLib.bytes2int( myBytes, 12 );
    System.out.println(myInt2);
  }
  
  public static void main(String args[])
  {
    //test1();
    //test2();
    test3();
  }
  
  
}
