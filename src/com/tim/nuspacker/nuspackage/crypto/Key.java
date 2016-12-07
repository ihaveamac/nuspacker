package com.tim.nuspacker.nuspackage.crypto;

import com.tim.nuspacker.utils.Utils;

public class Key {
	private static int LENGTH = 0x10;
	private byte[] key = new byte[LENGTH];
	
	public Key(){
	}
	
	public Key(byte[] key){
		setKey(key);
	}

	public Key(String string) {
        this(Utils.hexStringToByteArray(string));
    }

    public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		if(key != null && key.length == getKey().length){
			this.key = key;
		}
	}
	
	public int getLength(){
		return LENGTH;
	}
	
	@Override
	public String toString(){
	    return Utils.ByteArrayToString(key);
	}
}
