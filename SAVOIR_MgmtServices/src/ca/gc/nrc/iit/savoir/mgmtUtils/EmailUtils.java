// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.mgmtUtils;


import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

/**
 * Simplifies MgmtServices sending of emails.
 * 
 * @author Aaron Moss
 */
public class EmailUtils {

	private static ResourceBundle properties = 
		ResourceBundle.getBundle("mgmtservices", Locale.getDefault());
	
	/**
	 * Creates a new email to send from the management services.
	 * 
	 * @return	a new email object set to send from the management services
	 * 
	 * @throws EmailException 
	 */
	public static MultiPartEmail email() throws EmailException {
		MultiPartEmail email = new MultiPartEmail();
		setMgmtServicesParams(email);
		return email;
	}
	
	/**
	 * Creates a new email with the given addressee, subject, message, and 
	 * attachments.
	 * 
	 * @param toAddr		The address to send the email to
	 * @param toName		The name of the addressee (optional)
	 * @param subject		The subject of the email
	 * @param msg			The message of the email
	 * @param attachments	The attachments to the email
	 * 
	 * @return a new email object with these parameters
	 * 
	 * @throws EmailException 
	 */
	public static MultiPartEmail email(String toAddr, String toName, 
			String subject, String msg, EmailAttachment... attachments) 
			throws EmailException {
	
		MultiPartEmail email = new MultiPartEmail();
		
		if (toName == null) email.addTo(toAddr);
		else email.addTo(toAddr, toName);
		email.setSubject(subject);
		email.setMsg(msg);
		if (attachments != null) for (EmailAttachment toAttach : attachments) {
			email.attach(toAttach);
		}
		setMgmtServicesParams(email);
		
		return email;		
	}
	
	/**
	 * Creates a new email attachment with the given name and description from 
	 * a file in local storage.
	 *  
	 * @param name				The name of the attachment
	 * @param description		The description of the attachment (optional)
	 * @param filename			The filename of the attachment
	 * 
	 * @return a new email attachment with these parameters
	 */
	public static EmailAttachment attachment(String name, 
			String description, String filename) {
	
		EmailAttachment attachment = new EmailAttachment();
		
		attachment.setName(name);
		if (description != null) attachment.setDescription(description);
		attachment.setPath(filename);
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		
		return attachment;
	}
	
	/**
	 * Sets the management services specific parameters on an email message
	 * 
	 * @param email			The email message to set
	 * @throws EmailException 
	 */
	private static void setMgmtServicesParams(Email email) 
			throws EmailException {
		
		String hostname = null;
		int smtpPort = 587;
		String emailFrom = null;
		String emailReplyTo = null;
		String username = null;
		String password = null;
		boolean useSSL = false;
		int sslPort = 587;
		boolean useTLS = false;
		
		//required properties:
		try { 
			hostname = properties.getString("mgmtservices.email.host");
			emailFrom = properties.getString("mgmtservices.email.from");
		} catch (MissingResourceException e) {
			e.printStackTrace();
			return;
		}
		
		//optional properties
		try {
			smtpPort = Integer.parseInt(
					properties.getString("mgmtservices.email.port"));
		} catch (MissingResourceException e) {
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		try {
			emailReplyTo = properties.getString("mgmtservices.email.replyto");
		} catch (MissingResourceException e) {
			emailReplyTo = emailFrom;
		}
		
		try {
			username = properties.getString("mgmtservices.email.username");
			password = properties.getString("mgmtservices.email.password");
		} catch (MissingResourceException e) {}
		
		try {
			if ("true".equalsIgnoreCase(
					properties.getString("mgmtservices.email.useSSL"))) {
				useSSL = true;
			}
		} catch (MissingResourceException e) {}
		
		try {
			sslPort = Integer.parseInt(
					properties.getString("mgmtservices.email.sslPort"));
		} catch (MissingResourceException e) {
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		try {
			if ("true".equalsIgnoreCase(
					properties.getString("mgmtservices.email.useTLS"))) {
				useTLS = true;
			}
		} catch (MissingResourceException e) {}
		
		//set properties
		email.setHostName(hostname);
		email.setSmtpPort(smtpPort);
		email.setFrom(emailFrom, "SAVOIR Management Services");
		email.addReplyTo(emailReplyTo);
		email.setBounceAddress(emailReplyTo);
		if (username != null && password != null)
			email.setAuthentication(username, password);
		email.setSSL(useSSL);
		email.setSslSmtpPort(Integer.toString(sslPort));
		email.setTLS(useTLS);
	}
	
	/**
	 * Generates a message from a template and a set of named variables. The 
	 * template will be parsed, and all variable references 
	 * ("<code>${variableName}</code>") will be replaced with the value of 
	 * {@code variableName} from the set of variables. If there is no mapping 
	 * for {@code variableName} in the variable map (or that mapping returns 
	 * {@code null}), the reference will be left as 
	 * "<code>${variableName}</code>".
	 * <br>
	 * Variable references cannot be nested, and the character sequence 
	 * <code>${</code> is not allowed in variable names. So, for instance, the 
	 * template string <code>"${foo${bar}}"</code> with a variable map of 
	 * <code>"bar" = "baz"; "foobaz" = "quux"</code> will result in the message 
	 * <code>"${foobaz}"</code>, not <code>"quux"</code>. 
	 * 
	 * @param template		The string template for the message
	 * @param variables		The set of variables. Keys are variable names, 
	 * 						values are variable values.
	 * 
	 * @return The completed message, with variable substitutions made as 
	 * 			above, null if {@code template} is null.
	 */
	public static String message(
			String template, Map<String, String> variables) {
		
		if (template == null) return null;
		if (variables == null) variables = Collections.emptyMap();
		
		StringBuilder sb = new StringBuilder();
		
		char[] orig = template.toCharArray();
		int i = 0;
		
		outerLoop: while (i < orig.length) {
			
			if (orig[i] == '$' && i+1 < orig.length && orig[i+1] == '{') {
				//the start of an escape sequence.
				
				//search for closing bracket
				int varStart = i+2;
				int j = varStart;
				innerLoop: while (j < orig.length) {
					if (orig[j] == '}') {
						//handle variable
						String varName = 
							new String(orig, varStart, j - varStart);
						String varValue = variables.get(varName);
						
						if (varValue == null) {
							//no replacement for this variable
							//add variable reference verbatim
							sb.append("${").append(varName).append('}');
						} else {
							//variable replacement found
							//add replacement
							sb.append(varValue);
						}
						
						//continue parsing from after variable reference
						i = j+1;
						continue outerLoop;
					
					} else if (orig[j] == '$' 
						&& j+1 < orig.length && orig[j+1] == '{') {
						//found new start of variable reference
						//add bits before new start, and continue searching
						
						varStart -= 2; //roll back the start to include the "${"
						//append bits before new variable reference start
						sb.append(new String(orig, varStart, j - varStart));
						//set new variable start and continue looking
						varStart = j+2;
						j = varStart;
						continue innerLoop;
						
					} else {
						//normal character, continue looking
						j++;
						continue innerLoop;
					}
				}
				
				if (j == orig.length) {
					//No closing brace found.
					//In this case, we can guarantee there are no more 
					//variables in the string (or their closing brace would 
					//have been found). Since there are no more variables in 
					//the string, we append the remainder, and stop looking.
					
					varStart -= 2; //roll back the start to include the "${"
					//append rest of string
					sb.append(
							new String(orig, varStart, orig.length - varStart));
					//finish
					break outerLoop;
				}
				
			} else {
				//no escape sequence. Add current character, and continue
				sb.append(orig[i]);
				i++;
				continue outerLoop;
			}
			
		}
		
		return sb.toString();
	}
}
