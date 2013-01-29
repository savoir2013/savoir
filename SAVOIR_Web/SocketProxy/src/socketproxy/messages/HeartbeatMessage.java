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
public class HeartbeatMessage extends Message {

    public static final String HEARTBEAT = "heartbeat";
    public String message;

    public HeartbeatMessage() {
        super();
    }

    public HeartbeatMessage(String type) {
        super(type);
    }
}
