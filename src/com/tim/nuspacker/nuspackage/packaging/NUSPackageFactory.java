package com.tim.nuspacker.nuspackage.packaging;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.tim.nuspacker.nuspackage.FST;
import com.tim.nuspacker.nuspackage.TMD;
import com.tim.nuspacker.nuspackage.Ticket;
import com.tim.nuspacker.nuspackage.contents.Content;
import com.tim.nuspacker.nuspackage.contents.Contents;
import com.tim.nuspacker.nuspackage.fst.FSTEntries;
import com.tim.nuspacker.nuspackage.fst.FSTEntry;

public class NUSPackageFactory {
    private static Map<Content,NUSPackage> contentDictionary = new HashMap<>();
    private static Map<FST,NUSPackage> FSTDictionary = new HashMap<>();
    private static Map<TMD,NUSPackage> TMDDictionary = new HashMap<>();
    private static Map<FSTEntries,NUSPackage> FSTEntriesDictionary = new HashMap<>();
    private static Map<Contents,NUSPackage> contentsDictionary = new HashMap<>();
    
    
    public static NUSPackage createNewPackage(NusPackageConfiguration config){
        NUSPackage nusPackage = new NUSPackage();
        
        Contents contents = new Contents();
        FST fst = new FST(contents);
        addFSTDictonary(fst,nusPackage);
        FSTEntries entries =  fst.getFSTEntries();
        addFSTEntriesDictonary(fst.getFSTEntries(),nusPackage);  
        
        FSTEntry root =  entries.getRootEntry();
        root.setContent(contents.getFSTContent());
        
        //Create FSTEntries for the given directory.
        File dir_read = new File(config.getDir());
        readFiles(dir_read.listFiles(),root);
        
        /*
        if(config.getFullGameDir() != null && !config.getFullGameDir().equals("")){
            FSTEntry rootFullGame =  new FSTEntry(true);
            rootFullGame.setContent(contents.getFSTContent());
            File dir_read_fullgame = new File(config.getFullGameDir());
            readFiles(dir_read_fullgame.listFiles(),rootFullGame,true);
            mergeFSTEntries(root,rootFullGame);
        }*/
      
        System.out.println("Files read. Set it to content files.");
      
        ContentRulesService.applyRules(root,contents,config.getRules());

        addContentsDictonary(contents,nusPackage);
        addContentDictonary(contents,nusPackage);
        
        System.out.println("Generating the FST.");
        fst.update();        
        
        System.out.println("Generating the Ticket.");
        
        // titleid, key used for encryption, key used for encrypting the key.
        Ticket ticket = new Ticket(config.getAppInfo().getTitleID(),config.getEncryptionKey(), config.getEncryptKeyWith());
        
        System.out.println("Creating the TMD.");
        TMD tmd = new TMD(config.getAppInfo(),fst,ticket);       
        tmd.update();
       
        addTMDDictonary(tmd,nusPackage);
      
        nusPackage.setFST(fst);
        nusPackage.setTicket(ticket);
        nusPackage.setTMD(tmd);
        
        //System.out.println(root);
        //root.printRecursive(0);
        return nusPackage;
    }

    private static void addContentsDictonary(Contents contents, NUSPackage nusPackage) {
        contentsDictionary.put(contents, nusPackage);
    }
    private static void addContentDictonary(Contents contents, NUSPackage nusPackage) {
        for(Content c: contents.getContents()){
            if(!contentDictionary.containsKey(c)){
                contentDictionary.put(c, nusPackage);
            }
        }
    }
    
    private static void addTMDDictonary(TMD tmd, NUSPackage nusPackage) {       
        TMDDictionary.put(tmd, nusPackage);        
    }

    private static void addFSTDictonary(FST fst, NUSPackage nusPackage) {     
        FSTDictionary.put(fst, nusPackage);        
    }
    
    private static void addFSTEntriesDictonary(FSTEntries fstEntries, NUSPackage nusPackage) {     
        FSTEntriesDictionary.put(fstEntries, nusPackage);        
    }

    public static NUSPackage getPackageByContent(Content content){
        if(contentDictionary.containsKey(content)){
            return contentDictionary.get(content);
        }
        return null;
    }
    
    public static NUSPackage getPackageByFST(FST fst){
        if(FSTDictionary.containsKey(fst)){
            return FSTDictionary.get(fst);
        }
        return null;
    }
    
    public static NUSPackage getPackageByTMD(TMD tmd){
        if(TMDDictionary.containsKey(tmd)){
            return TMDDictionary.get(tmd);
        }
        return null;
    }
    
    public static NUSPackage getPackageByContents(Contents contents) {
        if(contentsDictionary.containsKey(contents)){
            return contentsDictionary.get(contents);
        }
        return null;
    }

    public static NUSPackage getPackageByFSTEntires(FSTEntries fstEntries) {
        if(FSTEntriesDictionary.containsKey(fstEntries)){
            return FSTEntriesDictionary.get(fstEntries);
        }
        return null;
    }
    
    public static void readFiles(File[] list,FSTEntry parent){
        readFiles(list, parent,false);
    }
    
    public static void readFiles(File[] list,FSTEntry parent,boolean notInNUSPackage){
        for(File f : list){
            if(!f.isDirectory()){                
                parent.addChildren(new FSTEntry(f,notInNUSPackage));    
            }
        }  
        for(File f : list){
            if(f.isDirectory()){
                FSTEntry newdir = new FSTEntry(f,notInNUSPackage);
                parent.addChildren(newdir);
                readFiles(f.listFiles(),newdir,notInNUSPackage);
            }
        }
    }
    
    /*
    private static void mergeFSTEntries(FSTEntry root, FSTEntry fullGameEntry) {
        //We iterate through all full games files to add missing one into update fst.
        for(FSTEntry curFullGameEntry : fullGameEntry.getChildren()){   
            //Check if the file will be overwritten (aka. file not in update folder)
            FSTEntry mainEntry = root.getEntryByName(curFullGameEntry.getFilename());
            
            //Not in update folder. We need to copy it from the full games.
            if(mainEntry == null){
                root.addChildren(curFullGameEntry.clone());
                //TODO: check clone function.
                //FSTEntry addedEntry = root.getEntryByName(curFullGameEntry.getFilename());
                //addedEntry.getChildren().clear();
            }
            //Now it should be available!
            mainEntry = root.getEntryByName(curFullGameEntry.getFilename());
            
            if(mainEntry != null){
                mergeFSTEntries(mainEntry, curFullGameEntry);
            }else{
               //If not, something went wrong.
               System.out.println("mergeFSTEntries: WTF");
               System.exit(-1);
            }
        }        
    }*/
}
