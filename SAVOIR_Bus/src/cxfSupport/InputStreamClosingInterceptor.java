// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package cxfSupport;
import java.io.IOException;
import java.io.InputStream;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
public class InputStreamClosingInterceptor extends AbstractPhaseInterceptor<Message> 
{
	
	public InputStreamClosingInterceptor(){
		super(Phase.PRE_LOGICAL);
	}
	
	public void handleMessage(final Message message) throws Fault
	{
		final InputStream is = message.getContent(InputStream.class);
		try{
			is.close();
		}catch(IOException ex){
			throw new Fault(ex);
	
		}
	}

}
