// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package transformer;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;

public class MalIncomeMsgToRightMsg extends AbstractMessageAwareTransformer {

	public MalIncomeMsgToRightMsg(){
		super();
		this.registerSourceType(String.class);
		this.setReturnClass(String.class);
	}
	@Override
	public Object transform(MuleMessage message, String outputEncoding)
			throws TransformerException {
		// TODO Auto-generated method stub
		String msg = "";
		try {
			msg = message.getPayloadAsString();
			System.out.println("The checked msg is:" + msg);
			int startTagBeginIndex = msg.indexOf("<");
			int startTagEndIndex = msg.indexOf(">");
			String startTag = "";
			if((startTagBeginIndex != -1) && (startTagEndIndex != -1)){
				startTag = msg.substring(startTagBeginIndex + 1, startTagEndIndex).trim();
				int firstSpaceIndex = startTag.indexOf(" ");
				if( firstSpaceIndex != -1){
					startTag = startTag.substring(0, firstSpaceIndex);
				}
				int endTagBeginIndex = msg.indexOf("</" + startTag);
				int endTagEndIndex = msg.indexOf(">", endTagBeginIndex);
				msg = msg.substring(startTagBeginIndex, endTagEndIndex + 1);
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msg;
	}

}
