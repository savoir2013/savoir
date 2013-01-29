// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import org.junit.Test;
import junit.framework.TestCase;


/**
 * RESTClientTest.
 *  Invoke a RESTful Web Service
 * @author Bryan Copeland 09/09/09
 * @version 2.0
 */
public class RESTClientTest extends TestCase
{
    String serviceURL = "";     //Web Service URL
    String urlParams = "";        //XML (File) to send

    /*
     * RESTClientTest constructor
     * @param   serviceURL   URL of RESTful Endpoint to send request to
     * @param   urlParams    String of URL parameters to append to the endpoint to complete the RESTful service call
     */
    public RESTClientTest (String url)
    {
        super(url);
    }

    public static void main(String[] args) throws Exception
    {
        String serviceURL = "";     //Web Service URL
        String urlParams = "";        //XML (File) to send

        if (args.length == 2)
        {
            serviceURL = args[0];
            urlParams = args[1];
        }
        else
        {
            System.err.println("Usage:  java RESTClient http://restURL restParams");
	    System.err.println("\nSample call:");
            //Do a RESTful search query for "SAVOIR" search term
            serviceURL = "http://www.flickr.com/services/rest/";
            urlParams = "?method=flickr.photos.search&text=SAVOIR&format=rest&foo=bar&api_key=57d30be449caf9032b1027d468885a40";
        }
        CallService cs = new CallService();
        cs.callREST(serviceURL, urlParams); //call RESTful Web Service
    }

  @Test
  public void testCallREST()
  {
     serviceURL = "http://www.flickr.com/services/rest/";
     urlParams = "?method=flickr.test.echo&format=rest&foo=bar&api_key=57d30be449caf9032b1027d468885a40";
     String expResult = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<rsp stat=\"ok\">\n<method>flickr.test.echo</method>\n<format>rest</format>\n<foo>bar</foo>\n<api_key>57d30be449caf9032b1027d468885a40</api_key>\n<launchData>?method=flickr.test.echo&amp;format=rest&amp;foo=bar&amp;api_key=57d30be449caf9032b1027d468885a40</launchData>\n</rsp>\n";

     CallService cs = new CallService();
     assertEquals(expResult, cs.callREST(serviceURL, urlParams));
  }
}
