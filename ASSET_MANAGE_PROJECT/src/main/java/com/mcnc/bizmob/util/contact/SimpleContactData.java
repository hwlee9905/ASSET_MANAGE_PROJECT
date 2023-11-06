package com.mcnc.bizmob.util.contact;

public class SimpleContactData {
	private String contactId;
	private String displayName;
	
	public SimpleContactData(String contactId, String displayName) {
		this.contactId = contactId;
		this.displayName = displayName;
	}
	
	public String getContactId() {
		return contactId;
	}
	public void setContactId(String contactId) {
		this.contactId = contactId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
}
