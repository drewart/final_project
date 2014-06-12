
//File Inode.java
//Author: Timothy Virgillo
//Team Members: Danielle Jenkins, Drew Pierce

import java.util.*;
import java.io.*;


//Each Inode describes 1 file
//16 Inodes can be stored in 1 block
class Inode {

    private final static int iNodeSize = 32;
    private final static int directSize = 11;
    private final static int blockSize = 512;
    private final static int NO_ERROR = 0;

    private final static int INODE_BLOCK_STARTING_INDEX = 1;
    private final static int INODES_PER_BLOCK = 16;

    //file size in bytes
    public int length;
    //number of file-table entries pointing to this iNode
    public short count;
    //inode states
    //public Flag flag_;
    public short flag;
    //pointers to the direct blocks
    public short[] direct = new short[directSize];
    //pointer to the indirect block
    public short indirect;

    public Inode() {
        length = 0;
        count = 0;
        flag = 1;
        for (int i = 0; i < direct.length; ++i) {
            direct[i] = -1;
        }
        indirect = -1;
    }

    //retrieve inode from disk
    public Inode(short iNumber) {
        //bitwise operations, advancing through the bytes of the inode
        try {
            //reads the corresponding disk block to inumber
            //locates the corresponding inode information in that block
            //initializes a new inode with this information
            int seek = 1 + iNumber / INODES_PER_BLOCK;
            byte[] dataBuffer = new byte[blockSize]; //512
            SysLib.rawread( seek, dataBuffer );
            //inode number % 16 * 32
            int offset = ( iNumber % INODES_PER_BLOCK ) * iNodeSize;
            length = SysLib.bytes2int(dataBuffer, offset);
            offset += 4;
            count = SysLib.bytes2short(dataBuffer, offset);
            offset += 2;
            flag = SysLib.bytes2short(dataBuffer, offset);
            offset += 2;

            for (int i = 0; i < direct.length; ++i, offset += 2 ) {
                direct[i] = SysLib.bytes2short( dataBuffer, offset );
            }
            indirect = SysLib.bytes2short( dataBuffer, offset );
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int findIndexBlock() {
        return indirect;
    }

    //save to disk as the i-th node
    public void toDisk(short iNumber) {

        byte[] inodeBuffer = new byte[iNodeSize]; //32
        int offset = 0; //

        SysLib.int2bytes( length, inodeBuffer, offset );
        offset += 4;
        SysLib.short2bytes( count, inodeBuffer, offset );
        offset += 2;
        SysLib.short2bytes( flag, inodeBuffer, offset );
        offset += 2;
        for ( int i = 0; i < direct.length; ++i, offset += 2 ) {
            SysLib.short2bytes( direct[i], inodeBuffer, offset );
        }
        SysLib.short2bytes(indirect, inodeBuffer, offset);

        //assures the correct block and offset
        int seek = 1 + iNumber / INODES_PER_BLOCK;
        byte[] readBuffer = new byte[blockSize];
        SysLib.rawread( seek, readBuffer );

        offset = (iNumber % INODES_PER_BLOCK) * iNodeSize;
        System.arraycopy(inodeBuffer, 0, readBuffer, offset, iNodeSize);
        SysLib.rawwrite( seek, readBuffer);
    }

    public boolean setIndexBlock(short blockAddress)throws Exception {
        //sets the indirect pointer to the next available block Address
            indirect = blockAddress;
            return true;
    }

    public boolean registerIndexBlock(short iNumber) {
        //registering the index (indirect) block for use
        try {
            for (int i = 0; i < direct.length; ++i) {
                //if there are any direct blocks free, return; the block must
                //fill prior to linking to another block through the index block
                if (direct[i] == -1)
                    return false;
            }
            //if it's already assigned, we can't use it
            if (indirect != -1)
                return false;

            //the actual book-keeping and 'setting' logic
            indirect = iNumber;
            byte[] dataBuffer = new byte[blockSize];
            for (int i = 0; i < blockSize / 2; ++i) {
                SysLib.short2bytes((short)-1, dataBuffer, i * 2);
            }
            SysLib.rawwrite(iNumber, dataBuffer);
        } catch (Exception e) { e.printStackTrace(); }
        return true;
    }

    public int registerTargetBlock(int offset, short iNumber) {
        try {
            int targetIndex = offset / blockSize;
            if (targetIndex < direct.length) {
                //target lies within the direct blocks for this inode
                if (direct[targetIndex] >= 0) {
                    return -1;
                    //our target lands on a space that is in use
                }
                //the targetIndex skipped a direct block
                //if allowed, would encourage external fragmentation
                if ((targetIndex > 0) &&
                        direct[targetIndex - 1] == -1) {
                    return -2;
                }
                direct[targetIndex] = iNumber;
                return 0;
            }
            //indirect is unassigned
            if (indirect < 0) {
                return -3;
            }
            byte[] readBuffer = new byte[blockSize];
            SysLib.rawread( indirect, readBuffer );
            int seek = (targetIndex - direct.length);
            if ( SysLib.bytes2short( readBuffer, seek * 2) > 0 ) {
                return -1;
            }

            SysLib.short2bytes(iNumber, readBuffer, seek * 2);
            SysLib.rawwrite(indirect, readBuffer);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public int findTargetBlock(int offset){
        try {
            //index is found by dividing the offset by the block size
            int seek = offset / blockSize;
            //it lies within the direct pointers of this inode
            if (seek < direct.length) {
                return direct[seek];
            }

            if (indirect < 0) {
                return -1;
            }

            byte[] readBuffer = new byte[blockSize];
            SysLib.rawread(indirect, readBuffer);
            //calculate the readSeek offset into the indirect block
            int readSeek = (seek - direct.length);
            return SysLib.bytes2short(readBuffer, readSeek * 2);

        } catch (Exception e) { e.printStackTrace(); }
        return -1; // keep the compiler happy
    }

    //de-allocate block
    public byte[] unregisterIndexBlock() throws Exception {
        if (indirect >= 0) {
            byte[] dataBuffer = new byte[blockSize];
            SysLib.rawread( indirect, dataBuffer );
            indirect = -1;
            return dataBuffer;
        }
        return null;
    }

    //dump function for debugging
    public void dump( ){
        Dump d = new Dump();
        d.dumpInode();
    }


    class Dump{

        public void dumpInode(){
            try{
                System.err.println("[ERROR] Initiating Inode data dump...");
                dumpFlag();
                dumpFileSize();
                dumpCount();
                dumpDirect();
                dumpIndirect();

            }catch( Exception e ){
                System.err.println("[ERROR] Inode has a data fault...");
                SysLib.exit();
            }

        }
        private void dumpDirect()throws Exception{
            for( short directPointer : direct ) {
                System.err.println("[DIRECT]" + directPointer);
            }
        }
        private void dumpIndirect()throws Exception{
            System.err.println("[INDIRECT]" + indirect );
        }
        private void dumpFlag()throws Exception{
            System.err.println("[FLAG]" + flag );
        }
        private void dumpFileSize()throws Exception{
            System.err.println("[FILE_SIZE]" + length );
        }
        private void dumpCount()throws Exception{
            System.err.println("[REFERENCE_COUNT]" + count );
        }
    }
}