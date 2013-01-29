// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package customTransport.tcp;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.retry.RetryContext;
import org.mule.api.transformer.TransformerException;
import org.mule.transport.AbstractMessageDispatcher;
import org.mule.transport.tcp.TcpConnector;
import org.mule.transport.tcp.TcpInputStream;
import org.mule.transport.tcp.TcpMessageDispatcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SavoirTcpMessageDispatcher extends TcpMessageDispatcher {

	private final TcpConnector savoirConnector;
	//private boolean orginalkeepSendSocketOpen = true;

    public SavoirTcpMessageDispatcher(OutboundEndpoint endpoint)
    {
        super(endpoint);
        savoirConnector = (TcpConnector) endpoint.getConnector();
        //orginalkeepSendSocketOpen = savoirConnector.isKeepSendSocketOpen();
    }

    protected synchronized void doDispatch(MuleEvent event) throws Exception
    {
    	try{
    		//logger.info("The orginal keepSocketOpen:" + orginalkeepSendSocketOpen);
    		//savoirConnector.setKeepSendSocketOpen(orginalkeepSendSocketOpen);
    		logger.info("Savoir dispatcher normal!!!");
    		super.doDispatch(event);
    		
    	}catch(IOException ex){
    		//Socket socket = savoirConnector.getSocket(event.getEndpoint());
    		//socket.close();
    		//TcpConnector connector = (TcpConnector)event.getEndpoint().getConnector()
    		//orginalkeepSendSocketOpen = savoirConnector.isKeepSendSocketOpen();
    		//if (orginalkeepSendSocketOpen) {
			//	savoirConnector.setKeepSendSocketOpen(false);
			//}
    		logger.info("Savoir dispatcher exception!!!");
    		logger.info("Savoir dispatcher exception is:" + ex.getMessage());
    		ex.printStackTrace();
    		savoirConnector.getOutputStream(event.getEndpoint(), event.getMessage()).close();
    		super.doDispatch(event);
    		//savoirConnector.setKeepSendSocketOpen(orginalkeepSendSocketOpen);
    		//logger.info("The After keepSocketOpen:" + savoirConnector.isKeepSendSocketOpen());
//    		if (((TcpConnector) endpoint.getConnector()).getOutputStream(event.getEndpoint(), event.getMessage()) != null) {
//				((TcpConnector) endpoint.getConnector()).setKeepSendSocketOpen(false);
//				((TcpConnector) endpoint.getConnector()).getOutputStream(event.getEndpoint(), event.getMessage()).close();
//				//event.getOutputStream().close();
//				((TcpConnector) endpoint.getConnector()).setKeepSendSocketOpen(true);
//			}
    		
    		//((TcpConnector) endpoint.getConnector()).getOutputStream(endpoint, message);
    		logger.info("Socket exception! And Socket is closed!");
    		
    	}
        
    }

    
 
}
