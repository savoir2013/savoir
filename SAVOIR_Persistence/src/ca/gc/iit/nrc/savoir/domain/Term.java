// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/**
 *TERM
 *
 *A term is a common name used for bridging terminology (parameter names) of two or more Edge Devices
 * which accept equivalent inputs with different names/titles.
 *  @ Project : SAVOIR
 *  @ File Name : Term.java
 *  @ Date : 21/09/2009
 *  @ Author : Bryan Copeland
 *
 */
package ca.gc.iit.nrc.savoir.domain;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="Term")
public class Term {	
	
	/** The unique identifier for the TERM **/
	private String termID;
	/** The Title used for indexing and searching the term in the Term Dictionary **/
	private String title;
	/** The image (preview/logo) of the term for ensuring that the right terms are used to map the correct parameters **/
	private String image;	
	/** The description of the term for ensuring that the right terms are used to map the correct parameters **/
	private String description;
	/** The i18n language and l10n locale of the term  **/
	private String termLanguageLocale;	
	
	
	public Term(){}
	
	public String getTermID() {
		return termID;
	}
	public String getTermTitle() {
		return title;
	}
	public String getTermImage() {
		return image;
	}
	public String getTermDesc() {
		return description;
	}
	public String getTermLanguageLocale() {
		return termLanguageLocale;
	}
	public void setTermID(String termID) {
		this.termID = termID;
	}
	public void setTermTitle(String title) {
		this.title = title;
	}
	public void setTermImage(String image) {
		this.image = image;
	}	
	public void setTermDesc(String description) {
		this.description = description;
	}
	public void setTermLanguageLocale(String languageLocale) {
		termLanguageLocale = languageLocale;
	}		
}
