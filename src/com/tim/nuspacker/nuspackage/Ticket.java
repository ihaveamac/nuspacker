package com.tim.nuspacker.nuspackage;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

import com.tim.nuspacker.nuspackage.crypto.Encryption;
import com.tim.nuspacker.nuspackage.crypto.IV;
import com.tim.nuspacker.nuspackage.crypto.Key;
import com.tim.nuspacker.utils.Utils;
/**
 * Reprents a ticket
 * @author timogus
 *
 */
public class Ticket {    
    private long titleID;
    private Key decryptedKey = new Key();
    private Key encryptWith = new Key();
    
    public Ticket(long titleID,Key decryptedKey, Key encryptWith){
        setTitleID(titleID);
        setDecryptedKey(decryptedKey);
        setEncryptWith(encryptWith);
    }
    
    public byte[] getAsData(){
        ByteBuffer buffer = ByteBuffer.allocate(0x350);
        buffer.put(Utils.hexStringToByteArray("00010004"));
        byte[] randomData = new byte[0x100];
        ThreadLocalRandom.current().nextBytes(randomData);
        buffer.put(randomData);
        buffer.put(new byte[0x3C]);
        buffer.put(Utils.hexStringToByteArray("526F6F742D434130303030303030332D58533030303030303063000000000000"));
        buffer.put(new byte[0x5C]);
        buffer.put(Utils.hexStringToByteArray("010000"));
        buffer.put(getEncryptedKey().getKey());
        buffer.put(Utils.hexStringToByteArray("000005"));
        randomData = new byte[0x06];
        ThreadLocalRandom.current().nextBytes(randomData);
        buffer.put(randomData);
        buffer.put(new byte[0x04]);
        buffer.putLong(getTitleID());
        buffer.put(Utils.hexStringToByteArray("00000011000000000000000000000005"));
        buffer.put(new byte[0xB0]);
        buffer.put(Utils.hexStringToByteArray("00010014000000AC000000140001001400000000000000280000000100000084000000840003000000000000FFFFFF01"));
        buffer.put(new byte[0x7C]);
        return buffer.array();
    }
    
    public Key getEncryptedKey() {
        ByteBuffer iv = ByteBuffer.allocate(0x10);
        iv.putLong(getTitleID());
        Encryption encrypt = new Encryption(getEncryptWith(), new IV(iv.array()));
        return new Key(encrypt.encrypt(getDecryptedKey().getKey()));
    }

    public long getTitleID() {
        return titleID;
    }
    
    public void setTitleID(long titleID) {
        this.titleID = titleID;
    }
    
    public Key getDecryptedKey() {
        return decryptedKey;
    }
    
    public void setDecryptedKey(Key decryptedKey) {
        this.decryptedKey = decryptedKey;
    }

    public Key getEncryptWith() {
        return encryptWith;
    }

    public void setEncryptWith(Key encryptWith) {
        this.encryptWith = encryptWith;
    }
}
