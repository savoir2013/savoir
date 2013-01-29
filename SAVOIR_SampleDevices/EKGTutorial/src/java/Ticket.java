// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University




import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Ticket
 *  Reads and processes an XML Ticket following the SAVOIR Network Enabled
 *  Platform specification
 *  (NOTE: once Ticket system is in place, use this to populate all Constants)
 * @author copelandb
 */
public class Ticket {

    private static String TICKET_XML = "WEB-INF/businterface/template/ticket.xml";

    public String getAttribute(String elementName, String attributeName) {
        try {
            String serviceAttribute = "";
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(TICKET_XML));

            // normalize text representation
            doc.getDocumentElement().normalize();

            NodeList serviceNode = doc.getElementsByTagName(elementName);
            for(int s=0; s<serviceNode.getLength(); s++) {
                Node firstServiceNode = serviceNode.item(s);
                if(firstServiceNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element serviceElement = (Element) firstServiceNode;
                    serviceAttribute = serviceElement.getAttribute(attributeName);
                }
            }
            return serviceAttribute;
        }
        catch (SAXException saxEx) { saxEx.printStackTrace(); }        
        catch (IOException ioEx) { ioEx.printStackTrace(); }
        catch (ParserConfigurationException pcEx) { pcEx.printStackTrace(); }
        return null;
    }


    public static void run() {
    try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(TICKET_XML));

            // normalize text representation
            doc.getDocumentElement().normalize();
            System.out.println ("Root element of the doc is:  " + doc.getDocumentElement().getNodeName() + "");
            NodeList serviceNode = doc.getElementsByTagName("service");
            System.out.println ("---service---");
            for(int s=0; s<serviceNode.getLength(); s++) {
                Node firstServiceNode = serviceNode.item(s);
                if(firstServiceNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element serviceElement = (Element) firstServiceNode;
                    String serviceID = serviceElement.getAttribute("ID");
                    System.out.println(serviceID);
                    String serviceName = serviceElement.getAttribute("name");
                    System.out.println(serviceName);
                    String serviceScheduleType = serviceElement.getAttribute("scheduleType");
                    System.out.println(serviceScheduleType);
                    String serviceAuthenticationType = serviceElement.getAttribute("authenticationType");
                    System.out.println(serviceAuthenticationType);
                    String serviceDeviceEndpoint = serviceElement.getAttribute("deviceEndpoint");
                    System.out.println(serviceDeviceEndpoint);
                    String serviceBusInterfaceEndpoint = serviceElement.getAttribute("businterfaceEndpoint");
                    System.out.println(serviceBusInterfaceEndpoint);
                }
            }

            NodeList savoirNode = doc.getElementsByTagName("SAVOIR");
            System.out.println ("------SAVOIR------");
            for(int s=0; s<savoirNode.getLength(); s++) {
                Node firstSavoirNode = savoirNode.item(s);
                if(firstSavoirNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element savoirElement = (Element) firstSavoirNode;
                    String protocol = savoirElement.getAttribute("protocol");                    
                    String ipAddress = savoirElement.getAttribute("ipAddress");                    
                    String portNumber = savoirElement.getAttribute("portNumber");
                    System.out.println(protocol + "://" + ipAddress + ":" + portNumber);
                    String savoirTopic = savoirElement.getAttribute("SAVOIRTopic");
                    System.out.println(savoirTopic);
                    String serviceTopic = savoirElement.getAttribute("serviceTopic");
                    System.out.println(serviceTopic);
                }
            }
        }
        catch (SAXParseException err) {
            System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
            System.out.println(" " + err.getMessage ());
        }
        catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
        System.exit (0);
    }

    public static void main (String argv []) {
        Ticket.run();
    }
}
