package com.tim.nuspacker.nuspackage;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.tim.nuspacker.nuspackage.contents.Contents;
import com.tim.nuspacker.nuspackage.fst.FSTEntries;
import com.tim.nuspacker.nuspackage.interfaces.IHasData;
import com.tim.nuspacker.utils.Utils;

public class FST implements IHasData{
	private byte[] magicbytes = new byte[] {0x46,0x53,0x54,0x00};
	private int unknown = 0x20;
	private int contentCount = 0;
	
	private byte[] padding = new byte[0x14];	
	
	private Contents contents = null;
	private FSTEntries fileEntries = null;
	
	private static ByteBuffer strings = ByteBuffer.allocate(0x300000); // 3MB should be more than enough.
		
	/**
	 * Helper variables to build the FST
	 */
    public static int curEntryOffset = 0x00;
	
	private byte[] alignment = null;
	
	public FST(Contents contents) {
       this.contents = contents;
    }

    public void update(){
	    strings.clear();	    
	    FST.curEntryOffset = 0;
	    
	    contents.resetFileOffsets();
	    fileEntries.update();
	    contents.update(fileEntries);
	    fileEntries.getRootEntry().setEntryCount(fileEntries.getFSTEntryCount());
	    
	    contentCount = contents.getContentCount();
	}
    
    public static int getStringPos() {
        return strings.position();
    }

    public static void addString(String filename) {
        strings.put(filename.getBytes());
        strings.put((byte) 0x00);
    }

    @Override
    public byte[] getAsData() {
        ByteBuffer buffer = ByteBuffer.allocate(getDataSize());
        buffer.put(magicbytes);
        buffer.putInt(unknown);
        buffer.putInt(contentCount);
        buffer.put(padding);
        buffer.put(contents.getFSTContentHeaderAsData());
        buffer.put(fileEntries.getAsData());
        buffer.put(Arrays.copyOfRange(strings.array(), 0, strings.position()));
        buffer.put(alignment);
        return buffer.array();
    }

    @Override
    public int getDataSize() {
        int size = 0;
        size += magicbytes.length;
        size += 0x04; // unknown
        size += 0x04; // contentCount
        size += padding.length;
        size += contents.getFSTContentHeaderDataSize();
        size += fileEntries.getDataSize();
        size += strings.position();
        int newsize = (int) Utils.align(size, 0x8000);
        alignment = new byte[newsize - size];
        return newsize;
    }

    public FSTEntries getFSTEntries() {
        if(fileEntries == null){
            fileEntries =  new FSTEntries();
        }
        return this.fileEntries;
    }

    public Contents getContents() {        
        return contents;
    }
	
}
