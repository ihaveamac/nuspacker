package com.tim.nuspacker.nuspackage.crypto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.tim.nuspacker.nuspackage.FST;
import com.tim.nuspacker.nuspackage.contents.Content;
import com.tim.nuspacker.utils.ByteArrayBuffer;
import com.tim.nuspacker.utils.Utils;

public class Encryption {
	private Key key = new Key();
	private IV IV = new IV();
	
	private Cipher cipher = null;
	
	public Encryption(Key key,IV IV){
		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		init(key,IV);
	}
	
	public void init(IV IV){
		init(getKey(),IV);
	}
	
	public void init(Key key){
		init(key,new IV());
	}
	public void init(Key key,IV IV){
		setKey(key);
		setIV(IV);
		SecretKeySpec secretKeySpec = new SecretKeySpec(getKey().getKey(), "AES");
		try {
		    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(getIV().getIV()));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
	
	//TODO: remove hardcoded output dir
	public void encryptFileWithPadding(FST fst, String output_filename,short contentID, int BLOCKSIZE) throws IOException {
	    InputStream in = new ByteArrayInputStream(fst.getAsData());
        FileOutputStream out = new FileOutputStream(output_filename);
        IV iv = new IV(ByteBuffer.allocate(0x10).putShort(contentID).array());
        encryptSingleFile(in, out,fst.getDataSize(),iv,BLOCKSIZE);
    }
	
	public void encryptFileWithPadding(File file,Content content, String output_filename, int BLOCKSIZE) throws IOException {
		InputStream in = new FileInputStream(file);
		FileOutputStream out = new FileOutputStream(output_filename);
		IV iv = new IV(ByteBuffer.allocate(0x10).putShort((short) content.getID()).array());
		encryptSingleFile(in, out,file.length(),iv,BLOCKSIZE);
	}
	
	public void encryptSingleFile(InputStream in,OutputStream out,long length,IV iv,int BLOCKSIZE) throws IOException {
	    
		long inputSize = length;
		long targetSize = Utils.align(inputSize, BLOCKSIZE);
		
		byte[] blockBuffer = new byte[BLOCKSIZE];
		int inBlockBufferRead; 
		
		long cur_position = 0;
		ByteArrayBuffer overflow = new ByteArrayBuffer(BLOCKSIZE);
		boolean first = true;
		do{
			if(first){				//After first chunk we need to reset the IV
				first = false;
			}else{
			    iv = null;
			}
			
			if(cur_position + BLOCKSIZE > inputSize){
				long expectedSize = (inputSize - cur_position);
				ByteBuffer buffer = ByteBuffer.allocate(BLOCKSIZE);
				
				inBlockBufferRead = Utils.getChunkFromStream(in,blockBuffer,overflow,expectedSize);
				buffer.put(Arrays.copyOfRange(blockBuffer, 0, inBlockBufferRead));
				blockBuffer = buffer.array();
				inBlockBufferRead = BLOCKSIZE;
			}else{
				int expectedSize = BLOCKSIZE;
				inBlockBufferRead = Utils.getChunkFromStream(in,blockBuffer,overflow,expectedSize);
			}
			byte[] output =  encryptChunk(blockBuffer,inBlockBufferRead,iv);
			
	        setIV(new IV(Arrays.copyOfRange(output,BLOCKSIZE-16, BLOCKSIZE)));
	        
			cur_position += inBlockBufferRead;
			out.write(output, 0, inBlockBufferRead);			
		}while(cur_position < targetSize && (inBlockBufferRead == BLOCKSIZE));
	}
	
	public void encryptFileHashed(File file,Content content, String output_filename,ContentHashes hashes) throws IOException {
	    File input = file;
	    String output = output_filename;
	    
        InputStream in = new FileInputStream(file);
        OutputStream out = new FileOutputStream(output);

        encryptFileHashed(in, out,input.length(),content,hashes);
        content.setEncryptedFileSize((int) new File(output).length());
    }

	private void encryptFileHashed(InputStream in, OutputStream out, long length, Content content,ContentHashes hashes) throws IOException {
	    int BLOCKSIZE = 0x10000;
        int HASHBLOCKSIZE = 0xFC00;
   
        int buffer_size = HASHBLOCKSIZE;
        byte[] buffer = new byte[buffer_size];
        ByteArrayBuffer overflowbuffer =  new ByteArrayBuffer(buffer_size);
        int read;
        int block = 0;
        
        int totalblocks = (int) (length / HASHBLOCKSIZE);
        
        do{
            read = Utils.getChunkFromStream(in, buffer, overflowbuffer, buffer_size);
            if(read != buffer_size){
                ByteBuffer new_buffer =  ByteBuffer.allocate(buffer_size);
                new_buffer.put(buffer);
                buffer = new_buffer.array();
            }
            byte[] output = encryptChunkHashed(buffer,block,hashes,content.getID());
            if(output.length !=  BLOCKSIZE)System.out.println("WTF?");
            out.write(output);
            
            block++;
            int progress = (int)((block *1.0/ totalblocks*1.0) *100);
            if((block % 100) == 0){
                System.out.print("\rEncryption: " + progress + "%");
            }
        }while(read == buffer_size);
        System.out.println("\rEncryption: 100%");
        in.close();
        out.close();
    }
	
	private byte[] encryptChunkHashed(byte[] buffer, int block,ContentHashes hashes, int content_id) {
        IV iv = new IV(ByteBuffer.allocate(16).putShort((short) content_id).array());
        byte[] decryptedHashes = null;
        try {
            decryptedHashes = hashes.getHashForBlock(block);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        decryptedHashes[1] ^= (byte)content_id;
            
        byte[] encryptedhashes = encryptChunk(decryptedHashes, 0x0400, iv);
        decryptedHashes[1] ^= (byte)content_id;
        int iv_start = (block % 16) * 20;
        iv = new IV(Arrays.copyOfRange(decryptedHashes,iv_start,iv_start + 16)); 
      
        
       
        byte[] encryptedContent =  encryptChunk(buffer,0xFC00,iv);
        
        ByteBuffer output = ByteBuffer.allocate(0x10000);
        
        output.put(encryptedhashes);
        output.put(encryptedContent);
        
        return output.array();	    
    }


    public byte[] encryptChunk(byte[] blockBuffer, int BLOCKSIZE, IV IV) {
		return  encryptChunk(blockBuffer,0,BLOCKSIZE, IV);
	}
		
	public byte[] encryptChunk(byte[] blockBuffer, int offset, int BLOCKSIZE,IV IV) {		
		if(IV != null) setIV(IV);
		init(getIV());
     	byte[] output = encrypt(blockBuffer,offset,BLOCKSIZE);
		return output;
	}
	
	public byte[] encrypt(byte[] input){		
		return encrypt(input,input.length);	
	}
	
	public byte[] encrypt(byte[] input,int len){
		return encrypt(input,0,len);
	}
	
	public byte[] encrypt(byte[] input,int offset,int len){
		try {			
			return cipher.doFinal(input, offset, len);			
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			System.exit(2);
		}
		return input;	
}       
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public IV getIV() {
		return IV;
	}

	public void setIV(IV iv) {
		IV = iv;
	}
}
