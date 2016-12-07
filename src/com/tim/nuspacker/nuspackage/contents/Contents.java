package com.tim.nuspacker.nuspackage.contents;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.tim.nuspacker.nuspackage.crypto.Encryption;
import com.tim.nuspacker.nuspackage.fst.FSTEntries;
import com.tim.nuspacker.nuspackage.interfaces.IHasData;
import com.tim.nuspacker.nuspackage.packaging.ContentDetails;
import com.tim.nuspacker.nuspackage.packaging.NUSPackage;
import com.tim.nuspacker.nuspackage.packaging.NUSPackageFactory;
import com.tim.nuspacker.utils.Pair;

/**
 * Represents a content (one .app file) of a package
 * @author timogus
 *
 */
public class Contents implements IHasData{
	/**
	 * List of the containing "Content" elements
	 */
	private List<Content> contents = new ArrayList<>();
    private Content fstContent;
	
	public Contents() {
       setFSTContent(getNewContent()); //first is  always the FST.
    }
	
	public void setFSTContent(Content content) {
        this.fstContent = content;
        content.setFSTContent(true);
    }
	
	public Content getFSTContent() {
        return this.fstContent;
    }
	
	public Content getNewContent(){
	     return getNewContent(false);
	}
    
	/**
	 * Creates and a return a new Content element. The ID and Index will be set automatically
	 * (simply but counting up from 0)
	 * @return the new created Content instance
	 */
    public Content getNewContent(boolean isHashed){
    	ContentDetails details = new ContentDetails(isHashed,(short) 0x0000,0x0,(short)0x0000);    	
        return getNewContent(details);
    }
    
  public Content getNewContent(ContentDetails details) {      
      Content content = new Content();
      content.setID(contents.size());
      content.setIndex((short) contents.size());
        
      if(details.isContent()){
          content.addType(Content.TYPE_CONTENT);
      }
      if(details.isEncrypted()){
          content.addType(Content.TYPE_ENCRYPTED);
      }
      if(details.isHashed()){
          content.addType(Content.TYPE_HASHED);
      }
      
      content.setEntriesFlags(details.getEntriesFlag());
      
      //Extra infos for FST
      content.setGroupID(details.getGroupID());
      content.setParentTitleID(details.getParentTitleID());
      
      getContents().add(content);
      return content;
    }
    
    /**
     * Returns the number of contents this collection contains
     * @return number of contents
     */
    public short getContentCount() {        
        return (short) getContents().size();
    }

    
    @Override
    /**
     * Returns the content info in form of a byte[]. The expected size is getDataSize().
     */
    public byte[] getAsData() {
        ByteBuffer buffer = ByteBuffer.allocate(getDataSize());
        for(Content c : getContents()){           
            buffer.put(c.getAsData());
        }
        return buffer.array();
    }

    /**
     * Returns the size (in bytes) the information about the contents will take in the Title Meta Data
     */
    @Override
    public int getDataSize() {
        int size = 0x00;
        for(Content c : getContents()){
            size += c.getDataSize();
        }
        return size;
    }

    /**
     * Returns the content info needed in the FST as a byte[]. The expected size is getFSTContentHeaderDataSize().  
     * @return
     */
	public byte[] getFSTContentHeaderAsData() {
	    long content_offset = 0;
		ByteBuffer buffer = ByteBuffer.allocate(getFSTContentHeaderDataSize());
		for(Content c: getContents()){
		    Pair<byte[], Long> result = c.getFSTContentHeaderAsData(content_offset);
			buffer.put(result.getKey());
			content_offset = result.getValue();
		}
		return buffer.array();
	}

	/**
	 * Size (in bytes) the content info will take in the FST
	 * @return
	 */
	public int getFSTContentHeaderDataSize() {
		int size = 0;
		for(Content c: getContents()){
			size += c.getFSTContentHeaderDataSize();
		}
		return size;
	}

	/**
	 * Returns a List containing all contents this collection holds.
	 * @return
	 */
	public List<Content> getContents() {
		if(contents == null){
			contents = new ArrayList<>();
		}
		return contents;
	}
	
	/**
	 * Resets the 
	 */
	public void resetFileOffsets() {
        for(Content c : getContents()){
            c.resetFileOffsets();
        }        
    }

	/**
	 * Updates the contents. Currently updateing the file offsets.
	 */
    public void update(FSTEntries fileEntries) {
        
        for(Content c : getContents()){
            c.update(fileEntries.getFSTEntriesByContent(c));
        }        
    }
    
    /**
     * Creates all content and hash files (.app & .h3). Run this BEFORE creating the tmd. Some sizes, offsets and values are changed.
     * @throws IOException 
     *
     */
    public void packContents(String outputDir) throws IOException{
    	//At first pack all non FST contents.
        for(Content c : getContents()){
            if(!c.equals(getFSTContent())){
                c.packContentToFile(outputDir);                
            }
        }
       
        NUSPackage nuspackage = NUSPackageFactory.getPackageByContents(this);
        Encryption encryption = nuspackage.getEncryption();
        //Then pack the FST
        System.out.println("Packing the FST into " +  String.format("%08X", fstContent.getID()) + ".");
       
        String fst_path = outputDir + "/" + String.format("%08X.app", fstContent.getID());
        encryption.encryptFileWithPadding(nuspackage.getFST(),fst_path,(short) getFSTContent().getID(),Content.CONTENT_FILE_PADDING);
       
        System.out.println("-------------");
        System.out.println("Packed all contents.\n\n");
    }

    public void deleteContent(Content cur_content) {
       contents.remove(cur_content);
    }
}   
