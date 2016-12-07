package com.tim.nuspacker.nuspackage.packaging;

import com.tim.nuspacker.nuspackage.crypto.Key;
import com.tim.nuspacker.utils.AppXMLInfo;

public class NusPackageConfiguration {
    private String dir;
    private AppXMLInfo appInfo;
    private Key encryptionKey;
    private Key encryptKeyWith;
    private ContentRules rules;
    private String fullGameDir = null;
    
    public NusPackageConfiguration(String dir, AppXMLInfo appInfo, Key encryptionKey, Key encryptKeyWith,ContentRules rules) {
        super();
        setDir(dir);
        setAppInfo(appInfo);
        setEncryptionKey(encryptionKey);
        setEncryptKeyWith(encryptKeyWith);
        setRules(rules);
    }
    
    public String getDir() {
        return dir;
    }
    
    public void setDir(String dir) {
        this.dir = dir;
    }
    
    public AppXMLInfo getAppInfo() {
        return appInfo;
    }
    
    public void setAppInfo(AppXMLInfo appInfo) {
        this.appInfo = appInfo;
    }
    
    public Key getEncryptionKey() {
        return encryptionKey;
    }
    
    public void setEncryptionKey(Key encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
    
    public Key getEncryptKeyWith() {
        return encryptKeyWith;
    }
    
    public void setEncryptKeyWith(Key encryptKeyWith) {
        this.encryptKeyWith = encryptKeyWith;
    }
    
    public ContentRules getRules() {
        return rules;
    }
    
    public void setRules(ContentRules rules) {
        this.rules = rules;
    }
    
    public String getFullGameDir() {
        return fullGameDir;
    }
    
    public void setFullGameDir(String fullGameDir) {
        this.fullGameDir = fullGameDir;
    }
}
