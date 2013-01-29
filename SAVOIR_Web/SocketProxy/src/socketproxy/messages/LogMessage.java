// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package socketproxy.messages;
import merapi.messages.Message;
/**
 *
 * @author youy
 */
public class LogMessage extends Message{
 public static final String LOGINFO = "logInfo";

 public String logMessage;

 public LogMessage(){
     super();
 }

 public LogMessage(String type){
     super(type);
 }
}
