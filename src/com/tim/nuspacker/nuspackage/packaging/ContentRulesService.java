package com.tim.nuspacker.nuspackage.packaging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tim.nuspacker.nuspackage.contents.Content;
import com.tim.nuspacker.nuspackage.contents.Contents;
import com.tim.nuspacker.nuspackage.fst.FSTEntry;
import com.tim.nuspacker.nuspackage.interfaces.IContentRule;



public class ContentRulesService {
    public static long MAX_CONTENT_LENGTH = (long) (0xBFFFFFFFL*0.975); //Hashes take about 2 - 2.5%. Depending on the number of files the alignment/padding is added too. Needs further checks at packing
   
    public static long cur_content_size = 0L;
    
    public static Content cur_content = null;
    public static Content cur_content_first = null;
    public static void applyRules(FSTEntry root,final Contents targetContents, ContentRules rules) {
        System.out.println("-----");
        for(IContentRule rule :rules.getRules()){
            System.out.println("Apply rule " + rule.getPattern());
            if(rule.isContentPerMatch()){
                setNewContentRecursiveRule("",rule.getPattern(),root, targetContents,rule);
            }else{
                cur_content = targetContents.getNewContent(rule.getDetails());
                cur_content_first = cur_content;
                cur_content_size = 0L;
                boolean result = setContentRecursiveRule("",rule.getPattern(), root,targetContents,rule.getDetails());
                if(!result){
                    System.out.println("No file matched the rule. Lets delete the content again");
                    targetContents.deleteContent(cur_content);
                }
                cur_content_first = null;
            }
            System.out.println("-----");
        }
    }
    
    private static Content setNewContentRecursiveRule(String path, final String pattern, FSTEntry cur_entry,final Contents targetContents, IContentRule rule) {
        path += cur_entry.getFilename() + "/";
        Pattern p = Pattern.compile(pattern);
        Content result = null;
                
        if(cur_entry.getChildren().size() == 0){
            String filePath = path;
            Matcher m = p.matcher(filePath);
            //System.out.println("Trying " + pattern + "to" + filePath);
            if(m.matches()){
                Content result_content = targetContents.getNewContent(rule.getDetails());                
                //System.out.println("Set content to " + String.format("%08X", cur_content.getID()) + " for: " + filePath);
                //child.setContent(cur_content);
                result = result_content;
            }
        }
        for(FSTEntry child: cur_entry.getChildren()){
            if(child.isDir()){
                Content child_result = setNewContentRecursiveRule(path,pattern,child,targetContents,rule);
                if(child_result != null){
                    result = child_result;
                }
            }else{
                String filePath = path + child.getFilename();
                Matcher m = p.matcher(filePath);
                if(m.matches()){    
                    Content result_content = targetContents.getNewContent(rule.getDetails());
                    if(!child.isNotInPackage()) System.out.println("Set content to " + String.format("%08X", result_content.getID()) + " for: " + filePath);
                    child.setContent(result_content);
                    result = result_content;
                }
            }           
        }
        if(result != null){            
            cur_entry.setContent(result);
        }
        return result;
        
    }

    private static boolean setContentRecursiveRule(String path,final String pattern,FSTEntry cur_entry, Contents targetContents, ContentDetails contentDetails){
        path += cur_entry.getFilename() + "/";
        Pattern p = Pattern.compile(pattern);
        boolean result = false;
        if(cur_entry.getChildren().size() == 0){
            String filePath = path;
            Matcher m = p.matcher(filePath);
            //System.out.println("Trying " + pattern + "to" + filePath);
            if(m.matches()){
                if(!cur_entry.isNotInPackage()) System.out.println("Set content to " + String.format("%08X (%08X,%08X)", cur_content.getID(),cur_content_size,cur_entry.getFilesize()) + " for: " + filePath);

                if(cur_entry.getChildren().isEmpty()/* && cur_entry.getFilename().equals("content")*/){  //TODO: may could cause problems. Current solution only apply to content folder.
                    cur_entry.setContent(cur_content);
                }
               
                return true;
            }else{
                return false;
            }
        }
        for(FSTEntry child: cur_entry.getChildren()){
            if(child.isDir()){
                
                boolean child_result = setContentRecursiveRule(path,pattern,child,targetContents,contentDetails);
                if(child_result){
                    cur_entry.setContent(cur_content_first);
                    result = true;
                }
            }else{
                String filePath = path + child.getFilename();
                Matcher m = p.matcher(filePath);
                //System.out.println("Trying " + pattern + "to" + filePath);
                if(m.matches()){
                    //System.out.println(child.getFilename());
                    if(cur_content_size > 0 && (cur_content_size + child.getFilesize()) > MAX_CONTENT_LENGTH){
                        System.out.println("Info: Target content size is bigger than " + MAX_CONTENT_LENGTH+ " bytes. Content will be splitted in mutitple files. Don't worry, I'll automatically take care of everything!");
                        cur_content = targetContents.getNewContent(contentDetails);
                        cur_content_size = 0;
                    }
                    cur_content_size += child.getFilesize();
                    
                    if(!child.isNotInPackage()) System.out.println("Set content to " + String.format("%08X (%08X,%08X)", cur_content.getID(),cur_content_size,child.getFilesize()) + " for: " + filePath);
                    //System.out.println(child.getFilename());
                    child.setContent(cur_content);
                    result = true;
                }
            }           
        }
        if(result){
            cur_entry.setContent(cur_content_first);
        }
        return result;
    }

}
