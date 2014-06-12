/**
 * Created with IntelliJ IDEA.
 * User: drpier
 * Date: 6/12/14
 * Time: 1:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileTableTest {

    public static void test1()
    {
        Directory dir = new Directory(4);

        short id = dir.ialloc("foo");

        FileTable table = new FileTable(dir);

        FileTableEntry bar = table.falloc("bar", "r");

        if (bar.iNumber != 2)
            System.out.println("bar iNumber wrong");

    }


    public static void main(String args[])
    {
        test1();

    }
}
