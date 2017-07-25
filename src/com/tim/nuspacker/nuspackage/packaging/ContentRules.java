package com.tim.nuspacker.nuspackage.packaging;

import java.util.ArrayList;
import java.util.List;

import com.tim.nuspacker.Settings;
import com.tim.nuspacker.nuspackage.interfaces.IContentRule;

/**
 * Set of ContentRule objects which contains the rules for assigning file to specific content files.
 * @author timogus
 *
 */
public class ContentRules {
    public List<ContentRule> rules = new ArrayList<>();
    
    /*
     * Creates an object with an empty list
     */
    public ContentRules(){
        
    }
    
    /**
     * Returns the list of all ContentRule objects
     * @return list of content rules
     */
    public List<ContentRule> getRules(){
        return rules;
    }
    
    /**
     * Add a rule. If the rule is already in the list, this command will be ignrored
     * @param the content rule that should be added
     * @return the added rule will be returned.
     */
    public ContentRule addRule(ContentRule rule){
        if(!rules.contains(rule)){
            rules.add(rule);
        }
        return rule;
    }
    
    /**
     * TODO:
     * @param pattern
     * @param details
     * @param contentPerMatch
     * @return
     */
    public ContentRule createNewRule(String pattern,ContentDetails details,boolean contentPerMatch){
        ContentRule newRule = new ContentRule(pattern, details,contentPerMatch);
        rules.add(newRule);
        return newRule;
    }
    
    /**
     * TODO:
     * @param pattern
     * @param details
     * @return
     */

    public ContentRule createNewRule(String pattern, ContentDetails details) {
        return createNewRule(pattern, details, false);
    }
    
    /**
     * Implementation of the Content Rule. Private class so it can only be created from within the ContentRules class. 
     * See the interface for proper documentation
     * @author timogus
     *
     */
	private class ContentRule implements IContentRule{
		/**
		 * 
		 */
	    private String pattern = "";
	    /**
	     * 
	     */
	    private ContentDetails details = null;
	    /**
	     * 
	     */
	    private boolean contentPerMatch =  false;
	    
	    public ContentRule(String pattern,ContentDetails details,boolean contentPerMatch){
	        setPattern(pattern);
	        setDetails(details);
	        setContentPerMatch(contentPerMatch);
	    }
	
	    public String getPattern() {
	        return pattern;
	    }
	
	    public void setPattern(String pattern) {
	        this.pattern = pattern;
	    }
	
	    public ContentDetails getDetails() {
	        return details;
	    }
	
	    public void setDetails(ContentDetails details) {
	        this.details = details;
	    }
	
	    public boolean isContentPerMatch() {
	        return contentPerMatch;
	    }
	
	    public void setContentPerMatch(boolean contentPerMatch) {
	        this.contentPerMatch = contentPerMatch;
	    }
	}

    public static ContentRules getCommonRules(short contentGroup,long titleID) {
        ContentRules rules =  new ContentRules();
        //I'm not sure of the order of the content. Maybe you can arrange it in the way we want. But this is working =)
        
        //At first we have the code .xml's
        ContentDetails common_details_code =  new ContentDetails(false, Settings.GROUPID_CODE, 0x0L, Settings.FSTFLAGS_CODE); // not hashed, groupID empty, parentid empty, fstentry flags
        /*00000001*/ rules.createNewRule("/code/app.xml",common_details_code);
        /*00000002*/ rules.createNewRule("/code/cos.xml",common_details_code);
        
        //Then the meta.xml
        ContentDetails common_details_meta =  new ContentDetails(true, Settings.GROUPID_META, 0x0L, Settings.FSTFLAGS_META);// // hashed, groupID 0x400, parentid empty, fstentry flags   
        /*00000003*/ rules.createNewRule("/meta/meta.xml", common_details_meta);
        
        //Then the rest of the meta folder except the meta.xml
        common_details_meta =  new ContentDetails(true, Settings.GROUPID_META, 0x0L, Settings.FSTFLAGS_META);// // hashed, groupID 0x400, parentid empty, fstentry flags   
        /*00000004*/ rules.createNewRule("/meta/.*[^.xml)]+", common_details_meta);
        
        //But lets move the bootMovie + Logo in own files.
        common_details_meta =  new ContentDetails(true, Settings.GROUPID_META, 0x0L,Settings.FSTFLAGS_META);// // hashed, groupID 0x400, parentid empty, fstentry flags  
        /*00000005*/ rules.createNewRule("/meta/bootMovie.h264", common_details_meta);
        /*00000006*/ rules.createNewRule("/meta/bootLogoTex.tga", common_details_meta);
        
        //... and the manual 
        ContentDetails common_details_meta_manual =  new ContentDetails(true, Settings.GROUPID_META, 0x0L,Settings.FSTFLAGS_META);// // hashed, groupID 0x400, parentid empty, fstentry flags   
        /*00000007*/ rules.createNewRule("/meta/Manual.bfma", common_details_meta_manual);
        
        //... and the images
        ContentDetails common_details_meta_images =  new ContentDetails(true, Settings.GROUPID_META, 0x0L, Settings.FSTFLAGS_META);// // hashed, groupID 0x400, parentid empty, fstentry flags   
        /*00000008*/ rules.createNewRule("/meta/.*.jpg", common_details_meta_images);
        
        //Now we can assign the rpx and rpls. each gets it own content. just to be sure.
        /*00000009*/ rules.createNewRule("/code/.*(.rpx|.rpl)",common_details_code,true); // Each file has it own content file
        
        //Don't forget the preload.txt
        ContentDetails common_details_preload =  new ContentDetails(true, Settings.GROUPID_CODE, 0x0L, Settings.FSTFLAGS_CODE);// // hashed, groupID 0x400, parentid empty, fstentry flags
        /*000000??*/ rules.createNewRule("/code/preload.txt",common_details_preload); // Each file has it own content file

        /*000000??*/ rules.createNewRule("/code/fw.img",common_details_code);
        /*000000??*/ rules.createNewRule("/code/fw.tmd",common_details_code);
        /*000000??*/ rules.createNewRule("/code/htk.bin",common_details_code);
        ///*000000??*/ rules.createNewRule("/code/nn_hai_user.rpl",common_details_code);
        /*000000??*/ rules.createNewRule("/code/rvlt.tik",common_details_code);
        /*000000??*/ rules.createNewRule("/code/rvlt.tmd",common_details_code);
        
        //And finally the content
        ContentDetails common_details_content =  new ContentDetails(true, contentGroup, titleID, Settings.FSTFLAGS_CONTENT);// // hashed, groupID part of titleid, parentid own titleid, fstentry flags
        /*000000??*/ rules.createNewRule("/content/.*", common_details_content);
        return rules;
    }

}
