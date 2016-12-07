package com.tim.nuspacker.nuspackage.packaging;

/**
 * This class represents the attributes of each content file. Type, flags and permission can be set here.
 * @author timogus
 *
 */
public class ContentDetails {
    /*
     * Should be always true. TODO: remove it?
     */
	private boolean isContent = true;
	 /*
     * Should be always true. TODO: remove it?
     */
    private boolean isEncrypted = true;
    /*
     * Defines if the content will be hashed or not.
     */
    private boolean isHashed = false;
    
    private short groupID = 0x0000;
    private long parentTitleID = 0x0;
    
    /**
     * The flag that will be set in each FSTEntry thats in this content.
     */
    private short entriesFlag = 0x0000;
        
    public ContentDetails(boolean isHashed,short groupID,long parentTitleID,short entriesFlags){
        setHashed(isHashed);
        setGroupID(groupID);
        setParentTitleID(parentTitleID);
        setEntriesFlag(entriesFlags);
    }
    
    //Typical getter and setter. No need for documentation.. (yet?)
    public boolean isHashed() {
        return isHashed;
    }
    
    public void setHashed(boolean isHashed) {
        this.isHashed = isHashed;
    }
    
    public short getGroupID() {
        return groupID;
    }
    
    public void setGroupID(short groupID) {
        this.groupID = groupID;
    }
    
    public long getParentTitleID() {
        return parentTitleID;
    }
    
    public void setParentTitleID(long parentTitleID) {
        this.parentTitleID = parentTitleID;
    }
    
    public boolean isContent() {
        return isContent;
    }
    
    public void setContent(boolean isContent) {
        this.isContent = isContent;
    }
    
    public boolean isEncrypted() {
        return isEncrypted;
    }
    
    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public short getEntriesFlag() {
        return entriesFlag;
    }

    public void setEntriesFlag(short entriesFlag) {
        this.entriesFlag = entriesFlag;
    }

    @Override
    public String toString() {
        return "ContentDetails [isContent=" + isContent + ", isEncrypted=" + isEncrypted + ", isHashed=" + isHashed
                + ", groupID=" + groupID + ", parentTitleID=" + parentTitleID + ", entriesFlag=" + entriesFlag + "]";
    }
    
    
}
