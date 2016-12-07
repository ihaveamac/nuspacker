package com.tim.nuspacker.nuspackage.interfaces;

import com.tim.nuspacker.nuspackage.packaging.ContentDetails;

public interface IContentRule {	
	public String getPattern();

	public void setPattern(String pattern);
	
	public ContentDetails getDetails() ;
	
	public void setDetails(ContentDetails details);
	public boolean isContentPerMatch();
	
	public void setContentPerMatch(boolean contentPerMatch);
}
