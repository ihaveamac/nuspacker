package com.tim.nuspacker.nuspackage.contents;

import java.nio.ByteBuffer;

import com.tim.nuspacker.nuspackage.interfaces.IHasData;

public class ContentInfos implements IHasData{
    private static final int contentInfoCount = 0x40;
            
    private ContentInfo[] contentinfos = new ContentInfo[contentInfoCount];
    
    public ContentInfos(){
        
    }
    
    public void setContentInfo(int index, ContentInfo contentInfo) {
        if(index < 0 && index > (contentinfos.length-1)){
            throw new IllegalArgumentException("Error on setting ContentInfo, index " + index + " invalid");
        }
        if(contentInfo == null){
            throw new IllegalArgumentException("Error on setting ContentInfo, ContentInfo is null");
        }
        contentinfos[index] = contentInfo;
    }
    
    public ContentInfo getContentInfo(int index) {
        if(index < 0 && index > (contentinfos.length-1)){
            throw new IllegalArgumentException("Error on getting ContentInfo, index " + index + " invalid");
        }
        if(contentinfos[index] == null){
            contentinfos[index] =  new ContentInfo();
        }
        return contentinfos[index];
    }
    
    public byte[] getAsData(){
        ByteBuffer buffer = ByteBuffer.allocate(ContentInfo.getDataSizeStatic() * contentinfos.length);
        for(int i = 0;i<contentinfos.length-1;i++){
            if(contentinfos[i] == null) contentinfos[i] = new ContentInfo();
            buffer.put(contentinfos[i].getAsData());
        }
        return buffer.array();
    }
    
    @Override
    public int getDataSize() {        
        return contentinfos.length * ContentInfo.getDataSizeStatic();
    }
}
