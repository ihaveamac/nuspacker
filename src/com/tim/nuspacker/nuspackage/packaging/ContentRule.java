package com.tim.nuspacker.nuspackage.packaging;

public class ContentRule {
    private String pattern = "";
    private ContentDetails details = null;
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
