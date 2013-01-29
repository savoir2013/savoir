// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.model.profile;


/**
 * Represents the widget specification contained within a device registration 
 * message. This implementation adds the capability to send the user choices 
 * from the profile message as part of the same object.
 * 
 * @author Aaron Moss
 */
public class Widget {

	/**
	 * Supported file types for images.
	 */
	public static enum ImgType {
		JPG("jpg"),
		GIF("gif"),
		BMP("bmp"),
		PNG("png");
		
		private String xml;
		
		private ImgType(String xml) {
			this.xml = xml;
		}
		
		public String toString() {
			return this.xml;
		}
	}
	
	/** Widget title */
	private String title;
	/** Widget description */
	private String description;
	/** Base name of widget icon file */
	private String iconName;
	/** Filetype of widget icon */
	private ImgType iconType;
	/** User-selectable widget options */
	private Choices choices;
	
	
	public Widget() {}
	
	
	//Java bean API
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getIconName() {
		return iconName;
	}
	
	public ImgType getIconType() {
		return iconType;
	}
	
	public Choices getChoices() {
		return choices;
	}
	
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setIconName(String iconName) {
		this.iconName = iconName;
	}
	
	public void setIconType(ImgType iconType) {
		this.iconType = iconType;
	}
	
	public void setChoices(Choices choices) {
		this.choices = choices;
	}
	
	
	//Fluent API
	public Widget withTitle(String title) {
		this.title = title;
		return this;
	}
	
	public Widget withDescription(String description) {
		this.description = description;
		return this;
	}
	
	public Widget withIconName(String iconName) {
		this.iconName = iconName;
		return this;
	}
	
	public Widget withIconType(ImgType iconType) {
		this.iconType = iconType;
		return this;
	}
	
	public Widget withChoices(Choices choices) {
		this.choices = choices;
		return this;
	}
}
