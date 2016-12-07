package com.tim.nuspacker.nuspackage.packaging;

import java.io.FileOutputStream;
import java.io.IOException;

import com.tim.nuspacker.nuspackage.Cert;
import com.tim.nuspacker.nuspackage.FST;
import com.tim.nuspacker.nuspackage.TMD;
import com.tim.nuspacker.nuspackage.Ticket;
import com.tim.nuspacker.nuspackage.contents.Content;
import com.tim.nuspacker.nuspackage.contents.ContentInfo;
import com.tim.nuspacker.nuspackage.contents.ContentInfos;
import com.tim.nuspacker.nuspackage.contents.Contents;
import com.tim.nuspacker.nuspackage.crypto.Encryption;
import com.tim.nuspacker.utils.HashUtil;

public class NUSPackage {
    private Ticket ticket;
    private TMD tmd;
    private FST fst;
    
    private String outputdir = "output";

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getOutputdir() {
        return outputdir;
    }

    public void setOutputdir(String outputdir) {
        this.outputdir = outputdir;
    }

    public TMD getTMD() {
        return tmd;
    }

    public void setTMD(TMD tmd) {
        this.tmd = tmd;
    }

    public FST getFST() {
        return fst;
    }

    public void setFST(FST fst) {
        this.fst = fst;
    }
    
    public Contents getContents(){
       return getFST().getContents();
    }

    public ContentInfos getContentInfos(){
        return getTMD().getContentInfos();
    }
    
    public void packContents(String outputDir) {
        if(outputDir != null && !outputDir.isEmpty()){
            setOutputdir(outputDir);
        }
        System.out.println("Packing contents.");
        //Do this before creating the title.tmd.
        try {
            getFST().getContents().packContents(outputDir);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } 
        
        //Set the correct FST hash and size.
        Content fstContent = getContents().getFSTContent();
        fstContent.setHash(HashUtil.hashSHA1(getFST().getAsData()));
        fstContent.setEncryptedFileSize(getFST().getAsData().length);
        
        //Update the grouphash
        ContentInfo contentInfo = getContentInfos().getContentInfo(0);
        contentInfo.setSHA2Hash(HashUtil.hashSHA2(getContents().getAsData()));
        //And the tmd contentinfo hash
        getTMD().updateContentInfoHash();
        
        try {
            /*
            FileOutputStream fos = new FileOutputStream("fst.bin");
            fos.write(fst.getAsData());
            fos.close();*/
    
            FileOutputStream fos = new FileOutputStream(getOutputdir() + "/title.tmd");
            fos.write(tmd.getAsData());
            fos.close();       
            System.out.println("TMD saved to    "+ getOutputdir() + "/title.tmd");
            
            fos = new FileOutputStream(getOutputdir() + "/title.cert");
            fos.write(Cert.getCertAsData());
            fos.close();         
            System.out.println("Cert saved to   "+ getOutputdir() + "/title.cert");
                
            fos = new FileOutputStream(getOutputdir() + "/title.tik");
            fos.write(ticket.getAsData());
            fos.close(); 
            System.out.println("Ticket saved to " + getOutputdir() + "/title.tik");
            System.out.println();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printTicketInfos(){
        System.out.println("Encrypted with this key           : " + getTicket().getDecryptedKey());
        System.out.println("Key encrypted with this key       : " + getTicket().getEncryptWith());
        System.out.println();
        System.out.println("Encrypted key                     : " + getTicket().getEncryptedKey());
    }

    public Encryption getEncryption() {
        return getTMD().getEncryption();
    }
}
