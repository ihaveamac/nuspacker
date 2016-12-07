package com.tim.nuspacker.nuspackage.fst;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.tim.nuspacker.nuspackage.FST;
import com.tim.nuspacker.nuspackage.contents.Content;
import com.tim.nuspacker.nuspackage.interfaces.IHasData;

public class FSTEntries implements IHasData {
	private List<FSTEntry> entries = new ArrayList<>();
	
	public FSTEntries(){
	    FSTEntry root = new FSTEntry(true);
	    entries.add(root);
	}
	
	public List<FSTEntry> getEntries() {
        if(entries == null){
            entries =  new ArrayList<>();
        }
        return entries;
    }
	
	public boolean isEmtpy() {
        return entries.isEmpty();
    }
	
	public boolean addEntry(FSTEntry entry){
	    if(!entry.isDir()){
	        System.out.println("FSTEntries in root need to be directories.");
	        return false;
	    }
	    getEntries().add(entry);
	    return true;
	}	

	public void update() {
	    for(FSTEntry entry : getEntries()){
           entry.update();
        }
	    updateDirRefs();
	}
	
	public List<FSTEntry> getFSTEntriesByContent(Content content){
		List<FSTEntry> result = new ArrayList<>();
		for(FSTEntry curEntry : getEntries()){
		    if(!curEntry.isNotInPackage()){
		        result.addAll(curEntry.getFSTEntriesByContent(content));
		    }
		}
		return result;
		
	}
	
	public int getFSTEntryCount(){
	    int count = 0; 
        for(FSTEntry entry : getEntries()){
            count += entry.getEntryCount();
        }
        return count;
	}
	
	public byte[] getAsData() {
        ByteBuffer buffer = ByteBuffer.allocate(getDataSize());
        for(FSTEntry entry : getEntries()){
            buffer.put(entry.getAsData());
        }
        return buffer.array();
    }
	
	@Override
    public int getDataSize() {
        return getFSTEntryCount() * 0x10;
    }
	
	public FSTEntry getRootEntry(){
	    List<FSTEntry> entries = getEntries();
        if(entries.size() == 0) return null;
        return entries.get(0);
	}

    public void updateDirRefs() {
        List<FSTEntry> entries = getEntries();
        if(entries.size() == 0) return;
        
        FSTEntry root = entries.get(0);
        root.setParentOffset(0);
        root.setNextOffset(FST.curEntryOffset);
        FSTEntry lastdir = root.updateDirRefs();
        if(lastdir != null){
            lastdir.setNextOffset(FST.curEntryOffset);
        }
    }   
}
