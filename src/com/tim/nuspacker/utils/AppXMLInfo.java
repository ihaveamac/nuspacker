package com.tim.nuspacker.utils;

import java.util.Arrays;

public class AppXMLInfo {
    private int version = 0;
    private long OSVersion = 0x0L;
    private long titleID = 0x0L;
    private short titleVersion = 0;
    private int SDKVersion = 0;    
    private int appType = 0x0;
    private short groupID = 0;
    private byte[] OSMask = new byte[32];
    private long common_id = 0x0L;
    
    public AppXMLInfo(){
        
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public long getOSVersion() {
        return OSVersion;
    }
    
    public void setOSVersion(long oSVersion) {
        OSVersion = oSVersion;
    }
    
    public long getTitleID() {
        return titleID;
    }
    
    public void setTitleID(long titleID) {
        this.titleID = titleID;
    } 
    
    public short getTitleVersion() {
        return titleVersion;
    }
    
    public void setTitleVersion(short titleVersion) {
        this.titleVersion = titleVersion;
    }

    public int getSDKVersion() {
        return SDKVersion;
    }
    
    public void setSDKVersion(int sDKVersion) {
        SDKVersion = sDKVersion;
    }
    
    public int getAppType() {
        return appType;
    }
    
    public void setAppType(int appType) {
        this.appType = appType;
    }
    
    public short getGroupID() {
        return groupID;
    }
    
    public void setGroupID(short groupID) {
        this.groupID = groupID;
    }
    
    public byte[] getOSMask() {
        return OSMask;
    }
    
    public void setOSMask(byte[] oSMask) {
        OSMask = oSMask;
    }
    
    public long getCommon_id() {
        return common_id;
    }
    
    public void setCommon_id(long common_id) {
        this.common_id = common_id;
    }

    @Override
    public String toString() {
        return "AppXMLInfo [version=" + version + ", OSVersion=" + OSVersion + ", titleID=" + titleID
                + ", titleVersion=" + titleVersion + ", SDKVersion=" + SDKVersion + ", appType=" + appType
                + ", groupID=" + groupID + ", OSMask=" + Arrays.toString(OSMask) + ", common_id=" + common_id + "]";
    }
    
    
}
