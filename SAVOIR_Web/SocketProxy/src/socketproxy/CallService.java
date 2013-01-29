// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.io.*;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * CallService
 *
 * @author copelandb
 */

public class CallService
{
    /*
     * CallService
     *  Call a Web Service with a specified set of arguments. Reasons for doing this include:
     *    -separate business logic into specific modules or service components (i.e. performing repetitive or specialized computations)
     *@param serviceType String
     *@param endpoint String
     *@param query String array representing a list of commands
     */
    public String CallService(String serviceType, String endpoint, String query)
    {
        if(serviceType == null || serviceType.length() == 0)
        {
           System.out.println("Call failed (unknown service type)");
           return null;
        }
        else if (serviceType.equalsIgnoreCase("SOAP") || serviceType.equalsIgnoreCase("SOAP1.2"))
        {
           return callSOAP(endpoint, query);
        }
        else if (serviceType.equalsIgnoreCase("SOAP-file") || serviceType.equalsIgnoreCase("SOAP1.1"))
        {
           return callSOAPFromFile(endpoint, query);
        }
        else if (serviceType.equalsIgnoreCase("REST"))
        {
            return callREST(endpoint, query);
        }
        else
        {
           System.out.println("Call failed (unknown service type)");
           return null;
        }
    }


    /*
     * callSOAP
     *  Launch a Desktop Application with a specified set of commands (arguments)
     *  the goal of the callSOAP method is to cache requests and subsequent responses
     *  to guarantee we have required Web Service data 
     *  before building running an application which requires the Web Service's data for execution
     * NOTE:
     *       This is an old way of calling SOAP, used because it is not protocol (SOAP 1.1/1.2) specific and
     *       does not require a full schema/data model or separate Java classes to create the call.
     * NOTE2:
     *       Requires permission for our application to cache XML requests/responses on the deployment server
     *@param theProgram String
     *@param commands String array representing a list of commands
     *@param SOAPAction optional String representing the type of SOAP call
     */
    public String callSOAP(String endpoint, String query)
    {
        String soapURL =  endpoint;
        String soapXML=query;
        String xmlResponse = "";
        String SOAPAction = ""; //set SOAPAction to "" meaning the intent of the SOAP message is provided by the HTTP Request-URI

        // Create the connection where we're going to send the file.
        try
        {
            URL url = new URL(soapURL);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) connection;

            httpConn.setRequestProperty( "Content-Length", soapXML );
            httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
                httpConn.setRequestProperty("SOAPAction",SOAPAction);
            httpConn.setRequestMethod( "POST" );
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);

            // Everything's set up; send the XML that was read in to b.
            OutputStream out = httpConn.getOutputStream();
            out.write( soapXML.getBytes() );
            out.close();

          // Read the response and write it to standard out.
            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
            BufferedReader in = new BufferedReader(isr);
            String inputLine;
            while ((inputLine = in.readLine()) != null)
            {
                System.out.println(inputLine);
                xmlResponse += inputLine; //set the response object to the contents of the SOAP call response
            }
            in.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return xmlResponse;
    }


    /*
     * callSOAPFromFile
     *    Launch a Desktop Application with a specified set of commands (arguments)
     *    the goal of the callSOAP method is to cache requests and subsequent responses
     *    to guarantee we have required Web Service data 
     *    before building running an application which requires the Web Service's data for execution
     * NOTE: This is an old way of calling SOAP, used because it is not protocol (SOAP 1.1/1.2) specific and
     *       does not require a full schema/data model or separate Java classes to create the call.
     * NOTE2: requires permission for application to cache XML requests/responses on the deployment machine
     *@param endpoint String representing the SOAP Web Service URL
     *@param file String representing the full path of a SOAP formatted XML (request) file
     *@param SOAPAction optional String representing the type of SOAP call
     */
    public String callSOAPFromFile(String endpoint, String file)
    {
        String SOAPUrl =  endpoint;
        String xmlResponse = "";
        String SOAPAction = ""; //set SOAPAction to "" meaning the intent of the SOAP message is provided by the HTTP Request-URI

        // Create the connection where we're going to send the file.
        try
        {
            URL url = new URL(SOAPUrl);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) connection;

            FileInputStream fin = new FileInputStream(file);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            // Copy the SOAP file to the open connection.
            copy(fin,bout);
            fin.close();

            byte[] b = bout.toByteArray();

            // Set the appropriate HTTP parameters for SOAP 1.1/1.2.
            httpConn.setRequestProperty( "Content-Length",
                                         String.valueOf( b.length ) );
            httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
                      httpConn.setRequestProperty("SOAPAction",SOAPAction);
            httpConn.setRequestMethod( "POST" );
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);

            // Everything's set up; send the XML that was read in to b.
            OutputStream out = httpConn.getOutputStream();
            out.write( b );
            out.close();

          // Read the response and write it to standard out.
            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
            BufferedReader in = new BufferedReader(isr);
            String inputLine;
            while ((inputLine = in.readLine()) != null)
            {
                System.out.println(inputLine);
                xmlResponse += inputLine; //set the response object to the contents of the SOAP call response
            }
            in.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return xmlResponse;
    }


    /*
     * callREST
     *   Launch a Desktop Application with a specified set of commands (arguments)
     *   From info at:
     *       http://xml.nig.ac.jp/tutorial/rest/index.html
     *@param theProgram String
     *@param commands String array representing a list of commands
     */
    public String callREST(String endpoint, String query)
    {
        if (query == null) { query = ""; } 
        String params = query; //set URL query parameters
        String response = "";  //placeholder for HTTP response
        String requestURL = endpoint+params;
        
        try
        {
            URL url = new URL(requestURL);

            //make connection, use post mode, and send query
            URLConnection urlc = url.openConnection();  
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            String encodedPost = URLEncoder.encode(params,DEFAULT_ENCODING);
            PrintStream request = new PrintStream(urlc.getOutputStream());
            request.print("launchData="+encodedPost);
            request.close();

            //retrieve results of the RESTful HTTP request
            BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String representation;
            StringBuffer sb = new StringBuffer();
            while ((representation = br.readLine()) != null)
            {
                sb.append(representation);
                sb.append("\n");
            }
            br.close();
            response = sb.toString();            

            if ( response == null )
            {
                System.out.println(request + "\n REST Web Service returned nothing");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println(requestURL + "\n Failed REST service call. Exception="+e);
            response = null;
        }
        System.out.println(response);
        return response;
    }


  /*
   * copy
   *   based on method from From E.R. Harold's book "Java I/O"
   * @param InputStream in
   * @param OutputStream out
   */
  public static void copy(InputStream in, OutputStream out)  throws IOException
  {
    // do not allow other threads to read from the input or write to the output while copying is taking place
    synchronized (in)
    {
      synchronized (out)
      {
        byte[] buffer = new byte[256];
        while (true)
        {
          int bytesRead = in.read(buffer);
          if (bytesRead == -1) break;
          out.write(buffer, 0, bytesRead);
        }
      }
    }
  }
   private final String DEFAULT_ENCODING = "UTF-8";
}
