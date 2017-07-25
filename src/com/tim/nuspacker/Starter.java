package com.tim.nuspacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.tim.nuspacker.nuspackage.crypto.Key;
import com.tim.nuspacker.nuspackage.packaging.ContentRules;
import com.tim.nuspacker.nuspackage.packaging.NUSPackage;
import com.tim.nuspacker.nuspackage.packaging.NUSPackageFactory;
import com.tim.nuspacker.nuspackage.packaging.NusPackageConfiguration;
import com.tim.nuspacker.utils.AppXMLInfo;
import com.tim.nuspacker.utils.Utils;
import com.tim.nuspacker.utils.XMLParser;

public class Starter {
    
    public static void main (String args[]){
        System.out.print("NUSPacker 0.3-i");
        new CompileDate().printDate();
        System.out.println();
        System.out.println();
        
   
        
        new File(Settings.tmpDir).mkdir();
        
        //String fullGameDir = "";
        
        String inputPath = "output";
        String outputPath = "output";
        new File(outputPath).mkdir();
        
        String encryptionKey = "";
        String encryptKeyWith = "";
        
        long titleID = 0x0L;
        long OSVersion = 0x000500101000400AL;
        int appType = 0x80000000;
        short titleVersion = 0;
        
        boolean skipXMLReading = false;
        
        if(args.length == 0){
            System.out.println("Provide at least the in and out parameter.");
            System.out.println();
            showHelp();
            System.exit(0);
        }
        
        for(int i = 0;i<args.length;i++){
            if(args[i].equals("-in")){
                if(args.length > i){
                    inputPath = args[i+1];
                    i++;
                }
            }else if(args[i].equals("-out")){
                if(args.length > i){
                    outputPath = args[i+1];
                    new File(outputPath).mkdir();
                    i++;
                }
            }else if(args[i].equals("-tID")){
                if(args.length > i){
                    titleID = Utils.HexStringToLong(args[i+1]);
                    i++;
                }
            }else if(args[i].equals("-OSVersion")){
                if(args.length > i){
                    OSVersion = Utils.HexStringToLong(args[i+1]);
                    i++;
                }
            }else if(args[i].equals("-appType")){
                if(args.length > i){
                    appType = (int) Utils.HexStringToLong(args[i+1]);
                    i++;
                }
            }else if(args[i].equals("-titleVersion")){
                if(args.length > i){
                    titleVersion = (short) Utils.HexStringToLong(args[i+1]);
                    i++;
                }
            }else if(args[i].equals("-encryptionKey")){
                if(args.length > i){
                    encryptionKey = args[i+1];
                    i++;
                }
            }else if(args[i].equals("-encryptKeyWith")){
                if(args.length > i){
                    encryptKeyWith = args[i+1];
                    i++;
                }
            }else if(args[i].equals("-skipXMLParsing")){
                skipXMLReading = true;
            }else if(args[i].equals("-help")){
                showHelp();
                System.exit(0);
            }/*else if(args[i].equals("-fullGameDir")){
                if(args.length > i){
                    fullGameDir = args[i+1];
                    i++;
                }
            }*/
        }        
        
        if(     !(new File(inputPath + "/code").exists()) ||
                !(new File(inputPath + "/content").exists()) ||
                !(new File(inputPath + "/meta").exists())){
            System.err.println("Invalid input dir ("+ new File(inputPath).getAbsolutePath()  + "): It's missing either the code, content or meta folder.");
            System.exit(0);
        }
        
        AppXMLInfo appInfo = new AppXMLInfo(); 
        //Set command line values in case the XML reading fails. 
        appInfo.setTitleID(titleID);
        appInfo.setGroupID((short) ((titleID >> 8) & 0xFFFF));
        appInfo.setAppType(appType);
        appInfo.setOSVersion(OSVersion);
        appInfo.setTitleVersion(titleVersion);
        
        if(encryptionKey == "" || encryptionKey.length() != 32){            
            encryptionKey = Settings.defaultEncryptionKey;
            System.out.println("Empty or invalid encryption provided. Will use " + encryptionKey  + " instead");
        }
        System.out.println();
        
        if(encryptKeyWith == "" || encryptKeyWith.length() != 32){
            System.out.println("Will try to load the encryptionWith key from the file \""+Settings.encryptWithFile+ "\"");
            encryptKeyWith = loadEncryptWithKey();
        }
        if(encryptKeyWith == "" || encryptKeyWith.length() != 32){
            encryptKeyWith = Settings.defaultEncryptWithKey;
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("WARNING:Empty or invalid encryptWith key provided. Will use " + encryptKeyWith  + " instead");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        
        System.out.println();
        
        File appxml = new File(inputPath + Settings.pathToAppXML);       
        
        if(!skipXMLReading){
            try {
                System.out.println("Parsing app.xml (values will be overwritten. Use the -skipXMLParsing argument to disable it)");
                XMLParser parser = new XMLParser();
                parser.loadDocument(appxml);
                appInfo = parser.getAppXMLInfo();
                
            } catch (ParserConfigurationException |SAXException |IOException  e ) {
                e.printStackTrace();
                System.err.println("Error while parsing the app.xml from path \"" + Settings.pathToAppXML + "\"");
            }
        }else{
            System.out.println("Skipped app.xml parsing");
        }
        
        /*if(appInfo.getAppType() == 0x0800001B){
            if(fullGameDir == null || fullGameDir.isEmpty()){
                System.err.println("The title you want to pack is an update. Please provide the path of the full game with the -fullGameDir paramter");
                System.exit(0);
            }else{
                System.out.println(fullGameDir);
            }
        }*/
        
        short content_group = appInfo.getGroupID();
        titleID = appInfo.getTitleID();
        
        long parentID = titleID & ~0x0000000F00000000L;
        
        System.out.println();
        System.out.println("Configuration:");
        System.out.println("Input            : \"" + inputPath + "\"");
        System.out.println("Output           : \"" + outputPath + "\"");
        //System.out.println("FullGameDir      : \"" + fullGameDir + "\"");
        System.out.println("TitleID          : " + String.format("%016X",appInfo.getTitleID()));
        System.out.println("GroupID          : " + String.format("%04X",appInfo.getGroupID()));
        System.out.println("ParentID         : " + String.format("%016X",parentID));
        System.out.println("AppType          : " + String.format("%08X",appInfo.getAppType()));
        System.out.println("OSVersion        : " + String.format("%016X",appInfo.getOSVersion()));
        System.out.println("Encryption key   : " + encryptionKey);
        System.out.println("Encrypt key with : " + encryptKeyWith);
        System.out.println();
                
        System.out.println("---");
      
        ContentRules rules = ContentRules.getCommonRules(content_group,parentID);
        
        NusPackageConfiguration config = new NusPackageConfiguration(inputPath, appInfo, new Key(encryptionKey), new Key(encryptKeyWith), rules);
        //config.setFullGameDir(fullGameDir);
        //Create a new nuspackage! 
        NUSPackage nuspackage = NUSPackageFactory.createNewPackage(config);
        //And now to pack it to .app files
        nuspackage.packContents(outputPath);        
        nuspackage.printTicketInfos();
        
        //Clean up        
        Utils.deleteDir(new File(Settings.tmpDir));
    }
  

    private static void showHelp() {
        System.out.println("help:");
        System.out.println("-in             ; is the dir where you have your decrypted data. Make this pointing to the root folder with the folder code,content and meta.");
        System.out.println("-out            ; Where the installable package will be saves");
        System.out.println("");
        System.out.println("(optional! will be parsed from app.xml if missing)");
        System.out.println("-tID            ; titleId of this package. Will be saved in the TMD and provided as 00050000XXXXXXXX");
        System.out.println("-OSVersion      ; target OS version");
        System.out.println("-appType        ; app type");
        System.out.println("-skipXMLParsing ; disables the app.xml parsing");
        System.out.println("");
        System.out.println("(optional! defaults values will be used if missing (or loaded from external file))");
        System.out.println("-encryptionKey  ; the key that is used to encrypt the package");
        System.out.println("-encryptKeyWith ; the key that is used to encrypt the encryption key");
        System.out.println("");
        //System.out.println("(optional! Only needed when building an update");
        //System.out.println("-fullGameDir    ; path to the full game to this update");
    }

    //TODO: do it in a clean way
    public static String loadEncryptWithKey(){
        File file = new File(Settings.encryptWithFile);
        if(!file.exists()) return "";
        String key = "";
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            key = in.readLine();
        } catch (IOException e) {
            System.out.println("Failed to read \""+Settings.encryptWithFile+ "\"");
        } finally{
            try {
                in.close();
            } catch (IOException e) {
            }
        }
       
        return key;
    }
}
