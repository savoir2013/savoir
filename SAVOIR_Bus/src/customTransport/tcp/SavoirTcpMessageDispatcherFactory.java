// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package customTransport.tcp;
import org.mule.api.MuleException;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.transport.tcp.TcpMessageDispatcherFactory;
import org.mule.api.transport.MessageDispatcher;

public class SavoirTcpMessageDispatcherFactory extends
		TcpMessageDispatcherFactory {
	public MessageDispatcher create(OutboundEndpoint endpoint) throws MuleException
    {
        return new SavoirTcpMessageDispatcher(endpoint);
    }

}
