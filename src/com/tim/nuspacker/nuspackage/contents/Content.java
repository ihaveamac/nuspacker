package com.tim.nuspacker.nuspackage.contents;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.tim.nuspacker.Settings;
import com.tim.nuspacker.nuspackage.crypto.ContentHashes;
import com.tim.nuspacker.nuspackage.crypto.Encryption;
import com.tim.nuspacker.nuspackage.fst.FSTEntry;
import com.tim.nuspacker.nuspackage.interfaces.IHasData;
import com.tim.nuspacker.nuspackage.packaging.NUSPackage;
import com.tim.nuspacker.nuspackage.packaging.NUSPackageFactory;
import com.tim.nuspacker.utils.Pair;
import com.tim.nuspacker.utils.Utils;

/**
 * Represents a content used in a package. 
 * A content holds a number of files while are saves in a .app file. 
 * The ID also used as a filename(.app)
 * @author timogus
 *
 */
public class Content implements IHasData{
	/**
	 * Represents the different types a content can be of. The actual is the combination of these types
	 */
	public static final short TYPE_CONTENT = 0x2000;
	public static final short TYPE_ENCRYPTED = 0x0001;
	public static final short TYPE_HASHED = 0x0002;
	
	/**
	 * ID of this content. Unique this package
	 */
    private int ID = 0x00;
    /**
	 * Index of this content. Unique this package
	 */
	private short index = 0x00;
	
	/**
	 * Type of this content
	 */
	private short type = TYPE_CONTENT & TYPE_ENCRYPTED;
	
	/**
	 * Reprensents the size of this content when its packed to a .app file. (Size of the produced .app file)
	 * This can only be set after the file is packed.
	 */
	private long encryptedFileSize;
	
	/**
	 * If content has type TYPE_HASHED, the hash is the SHA1 hash of the corresponding .h3 file.
	 * Otherwise the hash is the SHA1 hash of the decrypted file (Filled with 0x00 until its aligned to 0x8000 (aka filesize multiple of 0x8000)).	
	 */
	private byte[] SHA2 = new byte[0x14];
	
	
	/**
     * Current fileoffset
     */
    private long curFileOffset = 0;
    
    public static final int ALIGNMENT_IN_CONTENT_FILE    = 0x20;
    public static final int CONTENT_FILE_PADDING         = 0x08000;
    
	/**
	 * FSTEntries that are in this content
	 */
    private List<FSTEntry> entries = new ArrayList<>();
    
    /**
     * GroupID of this Content
     */
    private int groupID = 0;
    
    /**
     * parentTitleID of this Content. Is the games when this package is an Update. But not for all contents ~
     */
    private long parentTitleID = 0;
    
    private short entriesFlags = 0x0000;
    
    private boolean isFSTContent;
    
	Content() {        
        
    }

    /**
	 * Returns the ID of the content
	 * @return
	 */
	public int getID(){
	    return this.ID;
	}
	
	/**
	 * Sets the ID
	 * @param id
	 */
    public void setID(int id){
	    this.ID = id;
	}
	
    /**
     * Returns the type
     * @return
     */
    public short getType() {
        return type;
    }
    
    /**
     * Adds a type
     * @param type
     */
    public void addType(short type) {
        this.type |= type;
    }
    /**
     * removes a type
     * @param type
     */
    public void removeType(short type) {
        this.type &= ~type;
    }

    /**
     * Sets the type
     * @param type
     */
    public void setType(short type) {
        this.type = type;
    }

    /**
     * Returns the Index
     * @return
     */
    public short getIndex() {
        return index;
    }

    /**
     * Sets the index
     * @param index
     */
	public void setIndex(short index) {
        this.index = index;
        
    }
	
    public long getParentTitleID() {
        return parentTitleID;
    }

    public void setParentTitleID(long parentTitleID) {
        this.parentTitleID = parentTitleID;
    }
    
    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

	private long getCurFileOffset() {
        return curFileOffset;
    }

    private void setCurFileOffset(long curFileOffset) {
        this.curFileOffset = curFileOffset;
    }

    public void setEncryptedFileSize(long size) {
		this.encryptedFileSize = size;
	}

	public long getEncryptedFileSize() {
        return this.encryptedFileSize;
    }

    public void setHash(byte[] hash) {
        this.SHA2 = hash;
    }
	
	public byte[] getHash() {
       return this.SHA2;
    }

    public boolean isHashed(){
        return (getType() & TYPE_HASHED) == TYPE_HASHED;
    }

    public boolean isFSTContent() {
        return isFSTContent;
    }

    public void setFSTContent(boolean isFSTContent) {
        this.isFSTContent = isFSTContent;
    }

    public void setEntriesFlags(short entriesFlag) {
       this.entriesFlags =  entriesFlag;
    }

    public short getEntriesFlags() {
        return entriesFlags;
    }

    public int getFSTContentHeaderDataSize() {
        return 0x20;
    }
    
    // I don't really understand this part. Most parts are just guessed. Has something to do with the
    // Sections/Sectors on the disc. But we don't have a disc :D
	public Pair<byte[],Long> getFSTContentHeaderAsData(long old_content_offset) {
	    ByteBuffer buffer = ByteBuffer.allocate(getFSTContentHeaderDataSize());
      
        byte unkwn = 0;
        long content_offset = old_content_offset;
        
        long fst_content_size = (getEncryptedFileSize() / Content.CONTENT_FILE_PADDING);
        long fst_content_size_written = fst_content_size;
       
        if(isHashed()){
            unkwn = 2;

            fst_content_size_written -= ((fst_content_size / 64)+1)*2; //Hopefully this is right
            if(fst_content_size_written < 0) fst_content_size_written = 0;
        }else{
            unkwn = 1;
        }
        
        if(isFSTContent()){
            unkwn = 0;
            /**
             * Totally guessing here.
             */
            if(fst_content_size == 1){
                fst_content_size = 0;
            }
            content_offset += fst_content_size + 2;
            fst_content_size = 0;
        }else{
            content_offset += fst_content_size;
        }
          
        buffer.putInt((int) old_content_offset);
        buffer.putInt((int)fst_content_size_written);
        buffer.putLong(getParentTitleID());
        buffer.putInt(0x10,getGroupID());
        buffer.put(0x14,(byte) unkwn);      //Seems be if this content is Hashed (2) or not(1). But always 0 for the FST
        
        return new Pair<byte[],Long>(buffer.array(), content_offset);
	}

    public long getOffsetForFileAndIncrease(FSTEntry fstEntry) {
        long old_fileoffset = getCurFileOffset();
        setCurFileOffset(old_fileoffset + Utils.align(fstEntry.getFilesize(), ALIGNMENT_IN_CONTENT_FILE));
        return old_fileoffset;
    }

    public void resetFileOffsets() {
        curFileOffset = 0;
    }
    
    private List<FSTEntry> getFSTEntries() {
        return entries;
    }

    public int getFSTEntryNumber() {
       return entries.size();
    }
    
    @Override
    public byte[] getAsData() {
        ByteBuffer buffer = ByteBuffer.allocate(getDataSize());
        buffer.putInt(getID());
        buffer.putShort(getIndex());
        buffer.putShort(getType());
        buffer.putLong(getEncryptedFileSize());
        buffer.put(getHash());
        return buffer.array();
    }

    @Override
    public int getDataSize() {        
        return 0x30;
    }

    public void packContentToFile(String outputDir) throws IOException{
        System.out.println("Packing Content " + String.format("%08X", getID()));
        System.out.println();
        
        NUSPackage nusPackage = NUSPackageFactory.getPackageByContent(this);
        Encryption encryption = nusPackage.getEncryption();
        System.out.println("Packing files into one file:");
        //At first we need to create the decrypted file.
        File decryptedFile = packDecrypted();
        
        System.out.println();
        System.out.println("Generate hashes:");
        //Calculates the hashes for the decrypted content. If the content is not hashed,
        //only the hash of the decrypted file will be calculated
        ContentHashes contentHashes = new ContentHashes(decryptedFile, isHashed());
        String h3_path = outputDir + "/" + String.format("%08X", getID()) +".h3";
        contentHashes.saveH3ToFile(h3_path);
        setHash(contentHashes.getTMDHash());
        System.out.println();
        System.out.println("Encrypt content (" + String.format("%08X", getID()) + ")");                
        File encryptedFile = packEncrypted(outputDir,decryptedFile,contentHashes,encryption);
        
    	setEncryptedFileSize(encryptedFile.length());
    	
    	System.out.println();
        System.out.println("Content " + String.format("%08X", getID()) + " packed!");
        System.out.println("-------------");
    }

    private File packEncrypted(String outputDir,File decryptedFile,ContentHashes hashes,Encryption encryption) throws IOException {
        String outputFilePath = String.format("%s/%08X.app",outputDir, getID());        
        if((getType() & TYPE_HASHED) == TYPE_HASHED){
            encryption.encryptFileHashed(decryptedFile,this,outputFilePath,hashes);    
        }else{
            encryption.encryptFileWithPadding(decryptedFile,this,outputFilePath,CONTENT_FILE_PADDING);
        }
        System.out.println("Saved encrypted file to: " + outputFilePath);
        return new File(outputFilePath);
    }

    private File packDecrypted() throws IOException { //TODO: Proper error handling.
        String tmp_path = String.format("%s/%08X.dec",Settings.tmpDir, getID());
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(tmp_path);
            int totalCount  = getFSTEntryNumber();
            int cnt_file = 1;
            long cur_offset = 0;
            for(FSTEntry entry : getFSTEntries()){
                if(!entry.isNotInPackage()){
                    if(entry.isFile()){
                        if(cur_offset != entry.getFileOffset()){
                           System.out.println("FAILED"); //TODO: proper error message
                        }
                        long old_offset = cur_offset;
                       
                        cur_offset += Utils.align(entry.getFilesize(), ALIGNMENT_IN_CONTENT_FILE);
                       
                        String output = String.format("[%05d/%05d] Writing at %08x | FileSize: %08x | %s",cnt_file,totalCount,old_offset,entry.getFilesize(),entry.getFilename());
                        
                        Utils.copyFileInto(entry.getFile(),fos,output);
                        
                        int padding = (int) (cur_offset - (old_offset + entry.getFilesize()));   
                        fos.write(new byte[padding]);
                    }else{
                        System.out.println(String.format("[%05d/%05d] Wrote folder: \"%s\"",cnt_file,totalCount,entry.getFilename()));
                    }
                }
                cnt_file++;
            }
        } finally{
            fos.close();
        }
        return new File(tmp_path);
    }

    /**
     * 
     * @param entries flat list of all FSTEntry contained in this content
     */
    public void update(List<FSTEntry> entries){
    	if(entries != null){
    		this.entries = entries;
    	}
    }

    @Override
    public boolean equals(Object other){
        boolean result;
        if((other == null) || (getClass() != other.getClass())){
            result = false;
        }else{
            Content other_ = (Content)other;
            result = ID == other_.ID;
        }
        return result;
    }
}