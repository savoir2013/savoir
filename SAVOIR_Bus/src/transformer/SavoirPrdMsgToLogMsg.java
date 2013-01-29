// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package transformer;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.emory.mathcs.backport.java.util.Arrays;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class SavoirPrdMsgToLogMsg extends AbstractMessageAwareTransformer {
	
	public SavoirPrdMsgToLogMsg(){
		super();
		this.registerSourceType(String.class);
		this.setReturnClass(String.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object transform(MuleMessage message, String outputEncoding)
			throws TransformerException {
		// TODO Auto-generated method stub
		String msg = "";
		try {
			msg = message.getPayloadAsString();
			this.logger.info("Savoir Prd Inbound Log message is: " + msg);
			//System.out.println(msg);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(msg)));
			doc.getDocumentElement().normalize();
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//message/@sessionID");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			
			NodeList nodes = (NodeList) result;
			String router = "";
			if (nodes.getLength() == 1) {
				router = "jms://topic:savoirLog" + nodes.item(0).getFirstChild().getNodeValue().trim();
				List<String> addressList = new ArrayList<String>();
				addressList.add(router);
				message.setProperty("savoirPrdLogRouting", addressList);
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
		
		return msg;
	}
	
	

}
