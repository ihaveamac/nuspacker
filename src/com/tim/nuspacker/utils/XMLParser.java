package com.tim.nuspacker.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {
    private Document document;
    
    public void loadDocument(File doc) throws ParserConfigurationException, SAXException, IOException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(doc);
        this.document = document;
    }
    public AppXMLInfo getAppXMLInfo(){
        AppXMLInfo appxmlinfo = new AppXMLInfo();
        appxmlinfo.setOSVersion(getValueOfElementAsLongHex("os_version", 0));
        appxmlinfo.setTitleID(getValueOfElementAsLongHex("title_id", 0));
        appxmlinfo.setTitleVersion((short) getValueOfElementAsLongHex("title_version", 0));
        appxmlinfo.setSDKVersion((int) getValueOfElementAsInt("sdk_version", 0));
        appxmlinfo.setAppType((int) getValueOfElementAsLongHex("app_type", 0));
        appxmlinfo.setGroupID((short) getValueOfElementAsLongHex("group_id", 0));
        appxmlinfo.setOSMask(getValueOfElementAsByteArray("os_mask", 0));
        appxmlinfo.setCommon_id(getValueOfElementAsLongHex("common_id", 0));
        return appxmlinfo;
    }
    
    public long getValueOfElementAsInt(String element,int index){
        return Integer.parseInt(getValueOfElement(element,index));
    }
    
    public long getValueOfElementAsLong(String element,int index){
        return Long.parseLong(getValueOfElement(element,index));
    }
    
    public long getValueOfElementAsLongHex(String element,int index){
        return Utils.HexStringToLong(getValueOfElement(element,index));
    }
    
    public byte[] getValueOfElementAsByteArray(String element,int index){
        return Utils.hexStringToByteArray(getValueOfElement(element,index));
    }
    
    public String getValueOfElement(String element){
        return getValueOfElement(element,0);
    }
    
    public String getValueOfElement(String element,int index){
        if(document == null){
            System.out.println("Please load the document first.");
        }
        NodeList list = document.getElementsByTagName(element);        
        if(list == null){
            //System.out.println("NodeList is null");
            return "";
        }
        Node node = list.item(index);
        if(node == null){
            //System.out.println("Node is null");
            return "";
        }
        return node.getTextContent().toString(); 
    }
}
