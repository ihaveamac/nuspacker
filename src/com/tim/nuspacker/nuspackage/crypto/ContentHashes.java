package com.tim.nuspacker.nuspackage.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

import com.tim.nuspacker.nuspackage.contents.Content;
import com.tim.nuspacker.utils.ByteArrayBuffer;
import com.tim.nuspacker.utils.HashUtil;
import com.tim.nuspacker.utils.Utils;

public class ContentHashes {
    Map<Integer,byte[]> h0hashes = new TreeMap<>();
    Map<Integer,byte[]> h1hashes = new TreeMap<>();
    Map<Integer,byte[]> h2hashes = new TreeMap<>();
    Map<Integer,byte[]> h3hashes = new TreeMap<>();
    
    /*
     * Hash for the the TMD
     */
    byte[] TMDHash = new byte[0x14];
    
    private int blockCount = 0;
    public ContentHashes(File file, boolean hashed) {
        if(hashed){
        try {
               
                calculateH0Hashes(file);
                calculateOtherHashes(1,h0hashes,h1hashes);
                calculateOtherHashes(2,h1hashes,h2hashes);
                calculateOtherHashes(3,h2hashes,h3hashes);
                setTMDHash(HashUtil.hashSHA1(getH3Hashes()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            setTMDHash(HashUtil.hashSHA1(file,Content.CONTENT_FILE_PADDING));
        }
    }
      
    private void calculateOtherHashes(int hash_level,Map<Integer,byte[]> in_hashes,Map<Integer,byte[]> out_hashes) throws Exception {    
        int hash_level_pow = (int) Math.pow(16, hash_level);
        
        int hashescount = (blockCount/(hash_level_pow)) + 1;
        int new_blocks = 0;
        for(int j = 0;j<hashescount;j++){
            byte[] cur_hashes = new byte[16*20];
            for(int i = j*16;i<((j*16)+16);i++){
                if(in_hashes.containsKey(i)){
                    byte[] cur_hash = in_hashes.get(i);
                    System.arraycopy(cur_hash, 0, cur_hashes, (i%16)*20, 20);
                }else{
                    System.arraycopy(new byte[20], 0, cur_hashes, (i%16)*20, 20);
                }
            }
            out_hashes.put(new_blocks, HashUtil.hashSHA1(cur_hashes));
            new_blocks++;
            
            int progress = (int)((new_blocks *1.0/ hashescount*1.0) *100);
            if(new_blocks % 100 == 0){
                System.out.print("\rcalculating h" + hash_level + ": " + progress + "%");
            }
        }
        System.out.println("\rcalculating h" + hash_level + ": done");
    }

    private void calculateH0Hashes(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        int buffer_size = 0xFC00;
        byte[] buffer = new byte[buffer_size];
        ByteArrayBuffer overflowbuffer =  new ByteArrayBuffer(buffer_size);
        int read;
        int block = 0;
      
        int total_blocks = (int) (file.length() / buffer_size)+1;

        do{
            read = Utils.getChunkFromStream(in, buffer, overflowbuffer, buffer_size);
            if(read != buffer_size){
                ByteBuffer new_buffer =  ByteBuffer.allocate(buffer_size);
                new_buffer.put(buffer);
                buffer = new_buffer.array();
            }
            h0hashes.put(block, HashUtil.hashSHA1(buffer));
            
            block++;
            int progress = (int)((block *1.0/ total_blocks*1.0) *100);
            if(block % 100 == 0){
                System.out.print("\rcalculating h0: " + progress + "%");
            }
               
        }while(read == buffer_size);
        System.out.println("\rcalculating h0: done");
        setBlockCount(block);
    }   
    
    public byte[] getHashForBlock(int block) throws Exception{
        if(block > blockCount){
            throw new Exception("fofof");
        }
        ByteBuffer hashes = ByteBuffer.allocate(0x400);
        int h0_hash_start = (block/16)*16;
        
        for(int i = 0; i<16;i++){
            int index = h0_hash_start + i;
            if(h0hashes.containsKey(index)){
                hashes.put(h0hashes.get(index));
            }else{
                hashes.put(new byte[20]);
            }
        }
        
        int h1_hash_start = ((block/256))*16;
        for(int i = 0; i<16;i++){
            int index = h1_hash_start + i;
            if(h1hashes.containsKey(index)){
                hashes.put(h1hashes.get(index));
            }else{
                hashes.put(new byte[20]);
            }
            
        }
        
        int h2_hash_start = (block/4096)*16;
        for(int i = 0; i<16;i++){
            int index = h2_hash_start + i;
            if(h2hashes.containsKey(index)){
                hashes.put(h2hashes.get(index));
            }else{
                hashes.put(new byte[20]);
            }
        }
        return hashes.array();
    }

    public int getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

    public byte[] getH3Hashes() {
        ByteBuffer buffer =  ByteBuffer.allocate(h3hashes.size() * 0x14);
        for(int i = 0;i<h3hashes.size();i++){
            buffer.put(h3hashes.get(i));
        }
        return buffer.array();
    }

    public byte[] getTMDHash() {
        return TMDHash;
    }

    public void setTMDHash(byte[] TMDHash) {
        this.TMDHash = TMDHash;
    }

    public void saveH3ToFile(String h3_path) throws IOException {
        if(!h3hashes.isEmpty()){
            FileOutputStream fos = null;
            try{
                fos = new FileOutputStream(h3_path);
                fos.write(getH3Hashes());
            }finally{
                fos.close();
            }
        }
    }
}
