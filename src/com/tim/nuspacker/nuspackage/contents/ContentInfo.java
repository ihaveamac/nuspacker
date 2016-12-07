package com.tim.nuspacker.nuspackage.contents;

import java.nio.ByteBuffer;

import com.tim.nuspacker.nuspackage.interfaces.IHasData;

public class ContentInfo implements IHasData{
    private short indexOffset   = 0x00;
    private short commandCount  = 0x0B;
    private byte[] SHA2Hash = new byte[0x20]; //Will be patched anyway.
    
    public ContentInfo() {
        this((short) 0);
    }
 
    public ContentInfo(short contentCount) {
        this((short) 0,contentCount);
    }
    public ContentInfo(short indexOffset,short contentCount) {
        this.indexOffset = indexOffset;
        this.commandCount = contentCount;
    }    
   
    @Override
    public byte[] getAsData(){
        ByteBuffer buffer = ByteBuffer.allocate(0x24);
        buffer.putShort(getIndexOffset());
        buffer.putShort(getCommandCount());        
        buffer.put(getSHA2Hash());        
        return buffer.array();
    }
    
    public static int getDataSizeStatic() {        
        return 0x24;
    }    

    @Override
    public int getDataSize() {        
        return 0x24;
    }

    public short getCommandCount() {
        return commandCount;
    }

    public short getIndexOffset() {
        return indexOffset;
    }

    public void setIndexOffset(short indexOffset) {
        this.indexOffset = indexOffset;
    }

    public byte[] getSHA2Hash() {
        return SHA2Hash;
    }

    public void setSHA2Hash(byte[] SHA2Hash) {
        this.SHA2Hash = SHA2Hash;
    }

    public void setCommandCount(short commandCount) {
        this.commandCount = commandCount;
    }
    
    
}
