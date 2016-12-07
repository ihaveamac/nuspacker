package com.tim.nuspacker.nuspackage;

import java.nio.ByteBuffer;

import com.tim.nuspacker.utils.Utils;

public class Cert {

    // Currently resulting in a "Failed to match cached cert"-error
    public static byte[] getCertAsData(){
        ByteBuffer buffer = ByteBuffer.allocate(0xA00);
        buffer.putInt(0x000    ,0x010003);
        buffer.putInt(0x400    ,0x010004);
        buffer.putInt(0x700    ,0x010004);
        
        buffer.position(0x240);
        buffer.put(Utils.hexStringToByteArray("526F6F74000000000000000000000000"));
        buffer.position(0x280);
        buffer.put(Utils.hexStringToByteArray("00000001434130303030303030330000"));    
        
        
        buffer.position(0x540);
        buffer.put(Utils.hexStringToByteArray("526F6F742D4341303030303030303300"));
        buffer.position(0x580);
        buffer.put(Utils.hexStringToByteArray("00000001435030303030303030620000"));  
        
        buffer.position(0x840);
        buffer.put(Utils.hexStringToByteArray("526F6F742D4341303030303030303300"));
        buffer.position(0x880);
        buffer.put(Utils.hexStringToByteArray("00000001585330303030303030630000"));  
       
       
        return buffer.array();
    }
    
    public static byte[] getTMDCertAsData(){
        ByteBuffer buffer = ByteBuffer.allocate(0x700);
        buffer.putInt(0x000    ,0x010004);
        buffer.putInt(0x300    ,0x010003);
        
        buffer.position(0x140);
        buffer.put(Utils.hexStringToByteArray("526F6F742D4341303030303030303300"));
        buffer.position(0x180);
        buffer.put(Utils.hexStringToByteArray("00000001435030303030303030620000"));  
        
        
        buffer.position(0x540);
        buffer.put(Utils.hexStringToByteArray("526F6F74000000000000000000000000"));
        buffer.position(0x580);
        buffer.put(Utils.hexStringToByteArray("00000001434130303030303030330000"));    
            
       
        return buffer.array();
    }
}
