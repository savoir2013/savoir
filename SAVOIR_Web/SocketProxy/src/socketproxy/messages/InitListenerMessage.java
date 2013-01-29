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
public class InitListenerMessage extends Message {
    public static final String INITLISTENER = "initListener";

    public String server;
    
    public int sessionID;

    public int userSessionID;

    public String url;

    public Boolean isListenLogInfo;

    public Boolean success;

    public InitListenerMessage(){
        super();
    }

    public InitListenerMessage(String type){
     super(type);
 }
}
