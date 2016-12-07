package com.tim.nuspacker.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Utils {
    
    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.deleteOnExit();
    }
    
    public static long align(long input, int alignment){
        long newSize = (input/alignment);
        if(newSize * alignment != input){
            newSize++;
        }
        newSize = newSize * alignment;        
        return newSize;        
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    public static long HexStringToLong(String s) {
        try{
            BigInteger bi = new BigInteger(s, 16);          
            return bi.longValue();
        }catch(NumberFormatException e){
            //System.out.println("HexStringToLong failed");
            return 0L;
        }
    }
    
    public static String ByteArrayToString(byte[] ba)
    {
      if(ba == null) return null;
      StringBuilder hex = new StringBuilder(ba.length * 2);
      for(byte b : ba){
        hex.append(String.format("%02X", b));
      }
      return hex.toString();
    }
    
    public static int getChunkFromStream(InputStream inputStream,byte[] output, ByteArrayBuffer overflowbuffer,long excptedSize) throws IOException {
		int bytesRead = -1;
    	int inBlockBuffer = 0;
    	do{
    		bytesRead = inputStream.read(overflowbuffer.buffer,overflowbuffer.getLengthOfDataInBuffer(),overflowbuffer.getSpaceLeft());    		
    		if(bytesRead <= 0) break;

    		overflowbuffer.addLengthOfDataInBuffer(bytesRead);
	    	
	    	if(inBlockBuffer + overflowbuffer.getLengthOfDataInBuffer() > excptedSize){
	    		long tooMuch = (inBlockBuffer + bytesRead) - excptedSize;
	    		long toRead = excptedSize - inBlockBuffer;
	    		
	    		System.arraycopy(overflowbuffer.buffer, 0, output, inBlockBuffer, (int) toRead);
	    		inBlockBuffer += toRead;
	    		
	    		System.arraycopy(overflowbuffer.buffer, (int) toRead, overflowbuffer.buffer, 0,(int)  tooMuch);
	    		overflowbuffer.setLengthOfDataInBuffer((int) tooMuch);
	    	}else{     
	    		System.arraycopy(overflowbuffer.buffer, 0, output, inBlockBuffer, overflowbuffer.getLengthOfDataInBuffer()); 
	    		inBlockBuffer +=overflowbuffer.getLengthOfDataInBuffer();
	    		overflowbuffer.resetLengthOfDataInBuffer();
	    	}
    	}while(inBlockBuffer != excptedSize);
    	return inBlockBuffer;
    }
    
    public static long getUnsingedIntFromBytes(byte[] input,int offset){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.position(4);
        buffer.put(Arrays.copyOfRange(input,offset, offset+4));
        
        return buffer.getLong(0);
    }
    public static int getIntFromBytes(byte[] input,int offset){     
        return ByteBuffer.wrap(Arrays.copyOfRange(input,offset, offset+4)).getInt();
    }
    
    public static long copyFileInto(File file, OutputStream out) throws IOException {
        return copyFileInto(file, out, null);
    }
    public static long copyFileInto(File file, OutputStream out,String output) throws IOException {
        if(output !=  null){
            System.out.print(output);
        }
        FileInputStream in =  new FileInputStream(file);
        long written = 0;
        long filesize = file.length();
        int buffer_size = 0x10000;
        byte[] buffer = new byte[buffer_size];
        long cycle = 0;
        do{
            int read = in.read(buffer);
            if(read <= 0) break;
            out.write(buffer,0,read);
            written += read;
            if((cycle % 10) == 0 && output != null){
                int progress = (int)((written *1.0/ filesize*1.0) *100);
                System.out.print("\r" + output + ": "+ progress + "%");
            }
        }while(written < filesize);
        if(output != null){
            System.out.println("\r" + output + ": 100%");
        }
        in.close();
        return written;
    }

}
