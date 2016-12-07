package com.tim.nuspacker.nuspackage.crypto;

public class IV {
	private static int LENGTH = 0x10;
	private byte[] IV = new byte[LENGTH];
	
	public IV(){
	}
	
	public IV(byte[] array) {
        setIV(array);
    }

    public byte[] getIV() {
		return IV;
	}

	public void setIV(byte[] IV) {
		if(IV != null && IV.length == getIV().length){
			this.IV = IV;
		}
	}
	
	public int getLength(){
		return LENGTH;
	}
}
