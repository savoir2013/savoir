// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.TestCase;
import org.junit.Test;


/**
 * TestLaunchDesktop
 *  Launch a Web-based application
 * @author Bryan Copeland 09/09/09
 * @version 2.0
 */
public class LaunchBrowserTest extends TestCase
{
    String serviceURL = "";     //Web Service URL
    String urlParams = "";        //XML (File) to send

    public LaunchBrowserTest (String input)
    {
        super(input);
    }

    /**
     * Test of launchBrowser method, of class Launch.
     */
    @Test
    public void testLaunchBrowser()
    {
        System.out.println("launchBrowser");
        String launchURL = "http://hsvo.ca";
        Launch instance = new Launch();
        instance.launchBrowser(launchURL);
        try
        {
            URI webPath = new URI(launchURL);
            assertNotNull(webPath.getPath());
        }
        catch(URISyntaxException uriSyntaxEx)
        {
            uriSyntaxEx.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {
        String inputLine = "";

        if (args.length < 1) { inputLine = "calc"; }
        else { inputLine = args[1]; }

        System.out.println(inputLine); //print the input program name before opening it

        if (inputLine.equalsIgnoreCase("savoir"))
        {
            String link = "http://198.164.40.210:8080/savoir2/test/calculator.html";
            Launch launcher = new Launch();
            launcher.launchBrowser(link);
        }
        else if (inputLine.equalsIgnoreCase("opensavoir"))
        {
            String link = "http://opensavoir.ca";
            Launch launcher = new Launch();
            launcher.launchBrowser(link);
        }
        else if (inputLine.equalsIgnoreCase("notepad"))
        {
            String link = "http://yanobs.com/notepad/";
            Launch launcher = new Launch();
            launcher.launchBrowser(link);
        }
        else if (inputLine.equalsIgnoreCase("paint"))
        {
            String link = "http://canvaspaint.org/";
            Launch launcher = new Launch();
            launcher.launchBrowser(link);
        }
        else if (inputLine.equalsIgnoreCase("calculator") || inputLine.equalsIgnoreCase("calc"))
        {
            String link = "http://198.164.40.210:8080/savoir2/test/calculator.html";
            Launch launcher = new Launch();
            launcher.launchBrowser(link);
        }
        else if (inputLine.equalsIgnoreCase("hsvo"))
        {
            try
            {
                String link = "http://hsvo.ca";
                Launch launcher = new Launch();
                launcher.launchBrowser(link);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("Usage:  java LaunchBrowser http://URL");
	    System.err.println("\nLaunching sample...");
        }
    }
}
