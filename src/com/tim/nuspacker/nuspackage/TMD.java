package com.tim.nuspacker.nuspackage;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

import com.tim.nuspacker.nuspackage.contents.ContentInfo;
import com.tim.nuspacker.nuspackage.contents.ContentInfos;
import com.tim.nuspacker.nuspackage.contents.Contents;
import com.tim.nuspacker.nuspackage.crypto.Encryption;
import com.tim.nuspacker.nuspackage.crypto.IV;
import com.tim.nuspacker.nuspackage.crypto.Key;
import com.tim.nuspacker.nuspackage.interfaces.IHasData;
import com.tim.nuspacker.utils.AppXMLInfo;
import com.tim.nuspacker.utils.HashUtil;
import com.tim.nuspacker.utils.Utils;

public class TMD implements IHasData{
	private int signatureType = 0x00010004;  //0x000
	private byte[] signature = new byte[0x100];                            //0x004
	private byte[] padding0 = new byte[0x3C];                              //0x104
	private byte[] issuer = ByteBuffer.allocate(0x40).put(Utils.hexStringToByteArray("526F6F742D434130303030303030332D435030303030303030620000000000000000000000000000000000000000000000000000000000000000000000000000")).array();                                //0x140

	private byte version = 0x01;                                           //0x180
	private byte CACRLVersion = 0x00;                                      //0x181
	private byte signerCRLVersion = 0x00;                                  //0x182
	private byte padding1 = 0x00;                                          //0x183
	
	private long systemVersion = 0x000500101000400AL;                      //0x184
	//private long titleID = 0x0000000000000000L;                          //0x18C Info the from the ticket will be used
	private int	titleType = 0x000100;                                      //0x194
	private short groupID = 0x0000;                                        //0x198
	private int appType = 0x80000000;          //for updates 0x0800001B;   //0x19A
	private int random1 = 0;
	private int random2 = 0;//0x02FE6000; //something about the (encrypted) sizes?
	private byte[] reserved = new byte[50];                               
	private int accessRights = 0x0000;                                     //0x1D8
	private short titleVersion = 0x00;                                     //0x1DC
	private short contentCount = 0x00;                                     //0x1DE
	private short bootIndex = 0x00;                                        //0x1E0
	private byte[] padding3 = new byte[2];                                 //0x1E2
	private byte[] SHA2 = new byte[0x20];                                  //0x1E4
	
	private ContentInfos contentInfos =  null;
	private Contents contents = null;
	//private byte[] certs = Cert.getTMDCertAsData();                      //not needed
	
	private Ticket ticket;
	
	public TMD(AppXMLInfo appInfo, FST fst,Ticket ticket){
	    setGroupID(appInfo.getGroupID());
	    setSystemVersion(appInfo.getOSVersion());
	    setAppType(appInfo.getAppType());
	    setTitleVersion(appInfo.getTitleVersion());
	    setTicket(ticket);
	    setContents(fst.getContents());
	    contentInfos = new ContentInfos();
	}

    private void setContents(Contents contents){
	    if(contents != null){
    		this.contents = contents;
    		contentCount = contents.getContentCount();
	    }
	}
	
	public void update(){
	    updateContents();
	}
	
	public void updateContents(){
	    this.contentCount = (short) (contents.getContentCount());
	    
	    ContentInfo firstContentInfo = new ContentInfo(contents.getContentCount());
	    byte [] randomHash  = new byte[0x20];
        ThreadLocalRandom.current().nextBytes(randomHash);
        
        firstContentInfo.setSHA2Hash(HashUtil.hashSHA2(contents.getAsData()));
        getContentInfos().setContentInfo(0,firstContentInfo);
	}

    public void updateContentInfoHash() {
        this.SHA2 = HashUtil.hashSHA2(getContentInfos().getAsData());
    }

    @Override
    public byte[] getAsData() {
        ByteBuffer buffer = ByteBuffer.allocate(getDataSize());
        buffer.putInt(signatureType);
        buffer.put(signature);
        buffer.put(padding0);
        buffer.put(issuer);
        
        buffer.put(version);
        buffer.put(CACRLVersion);
        buffer.put(signerCRLVersion);
        buffer.put(padding1);
       
        buffer.putLong(getSystemVersion());
        buffer.putLong(getTicket().getTitleID());
        buffer.putInt(titleType);
        buffer.putShort(getGroupID());
        buffer.putInt(getAppType());
        buffer.putInt(random1);
        buffer.putInt(random2);
        buffer.put(reserved);
        buffer.putInt(accessRights);
        buffer.putShort(getTitleVersion());
        buffer.putShort(contentCount);
        buffer.putShort(bootIndex);
        
        buffer.put(padding3);
        buffer.put(SHA2);
        
        buffer.put(getContentInfos().getAsData());
        buffer.put(getContents().getAsData());
        //buffer.put(certs); not needed
        return buffer.array();
    }

    @Override
    public int getDataSize() {
        int staticSize = 0x204;
        int contentInfoSize = contentInfos.getDataSize();
        int contentsSize = contents.getDataSize();
        //int certSize = certs.length;
        return staticSize + contentInfoSize + contentsSize;// + certSize;
    }

    public ContentInfos getContentInfos() {
        if(contentInfos == null){
            contentInfos = new ContentInfos();
        }
        return contentInfos;
    }

    public void setContentInfos(ContentInfos contentInfos) {
        this.contentInfos = contentInfos;
    }

    public Contents getContents() {
        if(contents == null){
            contents = new Contents();
        }
        return contents;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
    
    public Encryption getEncryption() {
        ByteBuffer iv = ByteBuffer.allocate(0x10);
        iv.putLong(getTicket().getTitleID());
        Key key = getTicket().getDecryptedKey();        
        return  new Encryption(key,new IV(iv.array()));
    }

    public long getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(long systemVersion) {
        this.systemVersion = systemVersion;
    }

    public short getGroupID() {
        return groupID;
    }

    public void setGroupID(short groupID) {
        this.groupID = groupID;
    }

    public int getAppType() {
        return appType;
    }

    public void setAppType(int appType) {
        this.appType = appType;
    }

    public short getTitleVersion() {
        return titleVersion;
    }

    public void setTitleVersion(short titleVersion) {
        this.titleVersion = titleVersion;
    }
}
