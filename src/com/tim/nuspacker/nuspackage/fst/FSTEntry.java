package com.tim.nuspacker.nuspackage.fst;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.tim.nuspacker.nuspackage.FST;
import com.tim.nuspacker.nuspackage.contents.Content;
import com.tim.nuspacker.nuspackage.interfaces.IHasData;
import com.tim.nuspacker.utils.HashUtil;

/**
 * Represents a Entry of a FST
 * @author Timogus
 *
 */
public class FSTEntry implements IHasData,Cloneable{
    public enum Types{
        DIR((byte)0x01),
        notInNUS((byte)0x80),
        WiiVC((byte)0x02);
        private final byte type;
        Types(byte type) { this.type = type; }
        public byte getValue() { return type; }
    }
    public enum Flags{
        NOBIGFILE((short)0x04),
        HASHED((short)0x400);
        private final short type;
        Flags(short type) { this.type = type; }
        public short getValue() { return type; }
    }
    
    private File file;
    
    /**
     * Attributes for all FSTEntries
     */
    private String filename = "";
    private FSTEntry parent = null;
    private List<FSTEntry> children = null;
    private int nameOffset = 0;
    private int entryOffset = 0x00;
    
    private short flags;
    
    /**
     * Attributes when FSTEntry is a DIR
     */
    private boolean isDir = false;
    private int parentOffset = 0;
    private int nextOffset = 0;
    
    /**
     * Attributes when FSTEntry is a DIR
     */
	private long filesize = 0;	
	private long fileoffset = 0; 
	/**
	 * Attributes when FSTEntry is the root of the FST
	 */
	private boolean isRoot = false;
	
	/**
	 * When this FSTEntry is the root, we need to know the total EntryCount of the FST. 
	 */	
	private int root_entryCount = 0;
	
	/**
	 * This FSTEntry belongs to content....
	 */
	private Content content = null; //We need the ID
	
	/**
	 * SHA1 hash of the decrypted file padded to the next full 32kb (0x8000 bytes) 
	 */
	private byte[] decryptedSHA1 = new byte[0x14];
	
	/**
	 * 
	 */
	private boolean bigFile = false; //TODO: Check it...
	
	private boolean hashedFile = false;
	
	private boolean notInPackage = false;

    public FSTEntry(File file){
	    this(file,false);
    }
	
	public FSTEntry(File file,boolean notInPackage){
	    if(file == null || !file.exists()){
	        throw new IllegalArgumentException("Couldn't create FSTEntry, file is NULL or doesn't exist");
	    }
	    this.file = new File(file.getAbsolutePath());
	    setDir(file.isDirectory());
        setFileName(file.getName());
        setFileSize(file.length());
        
        setNotInPackage(notInPackage);
        
        if(isFile()){
            decryptedSHA1 = null;//
        }
    }

	public FSTEntry(boolean root) {
		file = null;
        if(root){
            setIsRoot(true);
            setDir(true);
        }
    }

	//TODO: Make sure that the filename is unique for all children of this entry.
    public void addChildren(FSTEntry fstEntry) {        
	    getChildren().add(fstEntry);
	    fstEntry.setParent(this);
    }
    
    public boolean isNotInPackage() {
        return notInPackage;
    }

    public void setNotInPackage(boolean notInPackage) {
        this.notInPackage = notInPackage;
    }
    
    /**
     * Returns the parent of this FSTEntry
     * @return
     */
    public FSTEntry getParent() {
        return this.parent;
    }

    /**
     * Sets the parent of this FSTEntry
     * @param child
     */
    private void setParent(FSTEntry child) {
    	//TODO: check for null? if parent is a child/it self? idk. Need to think about it.
        this.parent = child;
    }
    
    /**
     * Return the content this FSTEntry will be saved in.
     * @return
     */
    public Content getContent() {
        return content;
    }
	
    /**
	 * Sets a ref to content this file is a part of and sets the flags.
	 * @param fstContent
	 */
	 public void setContent(Content content) {	   
	    setFlags(content.getEntriesFlags());
        this.content = content;
    }	 

	/**
	 * Sets a ref to content this file is a part of recursive.
	 * The same content will be set for ALL children.
	 * @param fstContent
	 */
	 public void setContentRecursive(Content content) {
	    setContent(content);
	    for(FSTEntry entry : getChildren()){
	        entry.setContentRecursive(content);
	    }
	} 
	
	/**
	 * Returns the file object corresponding to this FSTEntry
	 * @return
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns the name of this entry
	 * @return name of this entry
	 */
	public String getFilename() {
		return filename;
	}

	/**
     * Sets the name of this entry
     * @param filename new filename
     */
	public void setFileName(String filename) {
		this.filename = filename;
	}
	
    /**
     * Returns the filesize of this entry. 
     * @return filesize in bytes, 0 if this not a file
     */
	public long getFilesize() {
	    if(!isFile())return 0;
		return filesize;
	}

	/**
	 * Sets the new filesize
	 * @param filesize new filesize
	 */
	public void setFileSize(long filesize) { 
		this.filesize = filesize;
	}
	
	/**
     * Returns the file fset of this entry in the {@link Content} file. 
     * @return file offset in bytes, 0 if this not a file
     */
    public long getFileOffset() {       
        return this.fileoffset;
    }

    /**
     * Sets the new file offset
     * @param new offset
     */
    public void setFileOffset(long fileOffset) {
        this.fileoffset = fileOffset;
    }

    /**
     * Sets this {@link FSTEntry} to root.
     * @param isRoot
     */
	private void setIsRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }
    
	/**
	 * Returns true if this {@link FSTEntry} is the root of the {@link FST}
	 * @return
	 */
    public boolean isRoot() {
        return isRoot;
    }	 
	 
	/**
     * Sets if the directory is a entry
     * @param isDir true if entry is a dir
     */
    public void setDir(boolean isDir) {
        this.isDir = isDir;
    }
    
	/**
	 * Returns if this entry is a dir
	 * @return true if entry is a dir
	 */
	public boolean isDir() {
		return isDir;
	}
	
	 /**
     * Returns if this entry is a file
     * @return true if entry is a file
     */
	public boolean isFile() {
        return !(isDir() || isNotInPackage());
    }

    public boolean isBigFile() {
        return bigFile;
    }

    public void setBigFile(boolean bigFile) {
        this.bigFile = bigFile;
    }

    public boolean isHashedFile() {
        return hashedFile;
    }

    public void setHashedFile(boolean hashedFile) {
        this.hashedFile = hashedFile;
    }

    /**
     * Returns the type of this {@link FSTEntry}
     * @return
     */
    public byte getType(){
        byte type = 0;
        if(isDir()) type |= Types.DIR.getValue();
        if(isNotInPackage()) type |= Types.notInNUS.getValue();
        if(getFilename().endsWith("nfs")) type |= Types.WiiVC.getValue();
        return type;
    }
    
    /**
     * Returns the flags of this {@link FSTEntry}
     * @return
     */
    public short getFlags(){
        return flags;
    }
        
    /**
     * Returns the child of this {@link FSTEntry} that has the given name.
	 * Result is null if the {@link FSTEntry} doesn't contain a child with the given name.
     */
    public FSTEntry getEntryByName(String name) {
        FSTEntry result = null;
        for(FSTEntry f:getChildren()){            
            if(f.getFilename().equals(name)){
                result = f;
                break;
            }
        }
        return result;
    } 
    
    @Override
    public byte[] getAsData() {        
        ByteBuffer buffer = ByteBuffer.allocate(getDataSize());
        if(isRoot()){
            buffer.put((byte)1);
            buffer.put(new byte[0x07]);
            buffer.putInt(root_entryCount);
            buffer.put(new byte[0x04]);
        }else{        
            buffer.put(getType());
            buffer.put((byte) ((nameOffset>>16) & 0xFF));       //We need to write a 24bit int..
            buffer.putShort((short) ((nameOffset) & 0xFFFF));
            if(isDir()){
                buffer.putInt(parentOffset);
                buffer.putInt(nextOffset);
            }else if(isFile()){
                buffer.putInt((int) (fileoffset >> 5));
                buffer.putInt((int) filesize);
            }else if(isNotInPackage()){
                buffer.putInt(0);
                buffer.putInt((int) filesize);
            }
            buffer.putShort(getFlags());
            buffer.putShort((short) content.getID());
        }
        
        //Let's call this recursive
        if(children != null){
            for(FSTEntry entry : getChildren()){
                buffer.put(entry.getAsData());
            }
        }
        return buffer.array();
    }

    @Override
    public int getDataSize() {
        int size = 0x10;
        for(FSTEntry entry : getChildren()){
           size += entry.getDataSize();
        }
        return size;
    }

    /**
     * Returns the SHA1 hash of the decypted data filled up to the next 32KB (0x8000 bytes)
     * @return
     */
    public byte[] getDecryptedHash() {
        if(decryptedSHA1 == null){  // Calculate this only when we really need it...
            calculateDecryptedHash();
        }
        return decryptedSHA1;
    }

    /**
     * 
     * @param offset
     */
    public void setNameOffset(int offset) {
        if(offset > 0xFFFFFF){
            System.out.println("Warning, filename offset is too big. Maximum is " + 0xFFFFFF + " tried to set to" + offset);
        }
        this.nameOffset = offset;        
    }

    /**
     * Updates the entryOffset,name section etc.
     * Dir connections are not updated
     */
    public void update(){
        //Adds the current filename to the string section
        setNameOffset(FST.getStringPos());
        FST.addString(filename);
        setEntryOffset(FST.curEntryOffset);
        FST.curEntryOffset++;
        
        //TODO: check if obsolete (should be as the UpdateDirRefs is implemented) it should do the same.
        if(isDir() && !isRoot()){
            setParentOffset(getParent().getEntryOffset());
        }
        
        if(getContent() != null && isFile()){
            long fileoffset = getContent().getOffsetForFileAndIncrease(this);
            setFileOffset(fileoffset);
        }
        
        //Update recursive!!!
        for(FSTEntry entry : getChildren()){
            entry.update();
        } 
    }
    
    /**
     * Updates the directory refs
     * 
     * Returns a FSTEntry when the directory has no files. The result need have the NextOffset set to the next dir.
     */
    public FSTEntry updateDirRefs(){
        if(!(isDir() || isRoot()))return null;
        if(parent != null){
            setParentOffset(getParent().getEntryOffset());
        }
        
       
        FSTEntry result = null;
        
        for(int i = 0;i<getDirChildren().size();i++){
            FSTEntry cur_dir = getDirChildren().get(i);
        
            if(i+1 < getDirChildren().size()){
                cur_dir.setNextOffset(getDirChildren().get(i+1).entryOffset);
            }
            
            FSTEntry cur_result = cur_dir.updateDirRefs();
            
            if(cur_result != null){                
                FSTEntry cur_foo = cur_result.getParent();
                while(cur_foo.getNextOffset() == 0){
                    cur_foo = cur_foo.getParent();
                }
                cur_result.setNextOffset(cur_foo.getNextOffset());
               
            }
            
            if(!(i+1 < getDirChildren().size())){
               
                /*if(!getFileChildren().isEmpty()){
                    System.out.println("ffffffff");
                    System.out.println(cur_dir);
                    FSTEntry child_file = getFileChildren().get(0);
                    cur_dir.setNextOffset(child_file.getEntryOffset());
                }else{*/
                    result = cur_dir;
                //}
            }       
        }
       return result;
    }
    
    private int getNextOffset() {
        return this.nextOffset;
    }

    /**
     * Sets the entryoffset of this {@link FSTEntry} in the {@link FST}.
     * Offset in terms of entry number X.
     * @param entryOffset
     */
    public void setEntryOffset(int entryOffset) {
        this.entryOffset = entryOffset;
    }

    /**
     * Returns the entryoffset of this {@link FSTEntry} in the {@link FST}.
     * Offset in terms of entry number X.
     * @return
     */
    public int getEntryOffset() {
        return entryOffset;
    }
  
    /**
     * 
     * @param nextOffset
     */
    public void setNextOffset(int nextOffset) {
       this.nextOffset =  nextOffset;
    }

    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        if(isDir())sb.append("DIR: ").append("\n");
        if(isDir())sb.append("Filename: ").append(getFilename()).append("\n");
        if(isDir())sb.append("       ID:").append(getEntryOffset()).append("\n");
        if(isDir())sb.append(" ParentID:").append(parentOffset).append("\n");
        if(isDir())sb.append("   NextID:").append(nextOffset).append("\n");
        /*
        if(isFile())sb.append("FILE: ").append("\n");        
        if(isFile())sb.append("Filename: ").append(getFilename()).append("\n");
        if(isFile())sb.append("       ID:").append(getEntryOffset()).append("\n");*/
        /*if(isFile())sb.append("     size:").append(getFilesize()).append("\n");
        if(isFile())sb.append("   offset:").append(String.format("%08X", getFileOffset())).append("\n");
                    sb.append("  content:").append(String.format("%08X", getContent().getID())).append("\n");
                    sb.append("     hash:").append(Utils.ByteArrayToString(getContent().getHash())).append("\n");*/
       // sb.append("--------\n");
        for(FSTEntry e : getChildren()){
            sb.append(e.toString());
        }
        
        return sb.toString();
    }
    
    public void printRecursive(int space){
        for(int i = 0;i<space;i++){
            System.out.print(" ");
        }
        System.out.print(getFilename());
        if(isNotInPackage()){
            System.out.print(" (not in package)");
        }
        System.out.println();
        for(FSTEntry child : getDirChildren(true)){
            child.printRecursive(space + 1);
        }
        for(FSTEntry child : getFileChildren(true)){
            child.printRecursive(space + 1);
        }
    }

	public List<FSTEntry> getFSTEntriesByContent(Content content) {
		List<FSTEntry> entries = new ArrayList<>();
		if(this.content == null){
		    if(isDir){
		        System.err.println("The folder \"" + getFilename() + "\" is emtpy. Please add a dummy file to it.");
		        
		    }else{
    		    System.err.println("The file \"" + getFilename() + "\" is not assigned to any content (.app).");
    		    System.err.println("Please delete it or write a corresponding content rule");
		    }
		    System.exit(0);
		}else{
    		if(this.content.equals(content)){
    			entries.add(this);
    		}
		}
		for(FSTEntry child : getChildren()){
			entries.addAll(child.getFSTEntriesByContent(content));
		}
		return entries;
	}

	public List<FSTEntry> getChildren() {
		if(children == null){
			children = new ArrayList<>();
		}
		return children;
	}

    public int getEntryCount() {
        int count = 1;
        for(FSTEntry entry : getChildren()){
            count += entry.getEntryCount();
        }
        return count;
    }
    
    public void setParentOffset(int i) {
        this.parentOffset = i;
    }

    public void setEntryCount(int fstEntryCount) {
        this.root_entryCount = fstEntryCount;
    }

    /* replaced by the ContentRules
    public void setContentPerFile() {
        boolean first = true;
        
        for(FSTEntry entry : getDirChildren()){
            entry.setContentPerFile();
        }
        
        int size_file = 0;
        for(FSTEntry entry : getFileChildren()){
            size_file++;
            Content new_content = FST.getInstance().getContents().getNewContent();
            if(first){
                this.setContent(new_content);
                first = false;
            }
            entry.setContent(new_content);
        }
        if(size_file == 0){        	
            for(FSTEntry entry : getDirChildren()){
                this.setContent(entry.getContent());
                break;
            }
        }
    }*/
    
   /**
     * Returns all children that are directories. Returns an empty list if the FSTEntry is not a directory or doesn't have directories as children
     * @return
     */
    public List<FSTEntry> getDirChildren(){
        return getDirChildren(false);
    }    
    public List<FSTEntry> getDirChildren(boolean all){
    	List<FSTEntry> result = new ArrayList<>();
    	for(FSTEntry child : getChildren()){
    		if(child.isDir() && (all || !child.isNotInPackage())){
    			result.add(child);
    		}
    	}
    	return result;
    }
    
    /**
     * Returns all children that are files. Returns an empty list if the FSTEntry is not a directory or doesn't have files as children
     * @return
     */
    public List<FSTEntry> getFileChildren(){
        return getFileChildren(false);
    }   
    public List<FSTEntry> getFileChildren(boolean all){
    	List<FSTEntry> result = new ArrayList<>();
    	for(FSTEntry child : getChildren()){
    		if(child.isFile() || (all && !child.isDir())){
    			result.add(child);
    		}
    	}
    	return result;
    }

    public void calculateDecryptedHash() {
        decryptedSHA1 = HashUtil.hashSHA1(file, 0x8000);
    }

    
    public void setFlags(short flags) {
        this.flags = flags;
    }
    
  
    @Override
    public FSTEntry clone()
    {
      try
      {
        return (FSTEntry) super.clone();
      }
      catch ( CloneNotSupportedException e ) {
        // Kann eigentlich nicht passieren, da Cloneable
        throw new InternalError();
      }
    }

    
}
