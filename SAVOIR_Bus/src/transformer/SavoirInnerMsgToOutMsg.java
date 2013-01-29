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

public class SavoirInnerMsgToOutMsg extends AbstractMessageAwareTransformer {
	
	public SavoirInnerMsgToOutMsg(){
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
		String outMsg = "";
		try {
			msg = message.getPayloadAsString();
			this.logger.info("Out Mule message is " + msg);
			//System.out.println(msg);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(msg)));
			doc.getDocumentElement().normalize();
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//innermessage/router");
			//XPathExpression exprSID = xpath.compile("//message/@sessionID");

			//Object result = expr.evaluate(doc, XPathConstants.NODESET);
			Object result = expr.evaluate(doc, XPathConstants.STRING);
			//NodeList nodes = (NodeList) result;
			String router = (String)result;
			String[] addresses = router.split(",");
			//if (nodes.getLength() == 1) {
			
				//router = nodes.item(0).getFirstChild().getNodeValue().trim();
				List<String> addressList = new ArrayList<String>();
				//addressList.add(router);
				addressList.addAll((List<String>)(Arrays.asList(addresses)));
				message.setProperty("savoir_routing", addressList);
				outMsg = getOutMessage(doc);
			//}
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
		return outMsg;
		//return null;
	}
	
	private String getOutMessage(Document savoirDoc){
		String outMsg = "";
//		NodeList routerNodes = savoirDoc.getElementsByTagName("router");
//		savoirDoc.removeChild(routerNodes.item(0));
		NodeList msgNodes = savoirDoc.getElementsByTagName("message");
		 // set up a transformer
		try{
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        // create string from xml tree
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(msgNodes.item(0));
        trans.transform(source, result);
        outMsg = sw.toString();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//System.out.println(outMsg);
		return outMsg;
	}

}
