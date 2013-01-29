// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package socketproxy;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.TestCase;

/**
 *
 * @author copelandb
 */
public class LaunchTest extends TestCase
{

    public LaunchTest(String input)
    {
        super(input);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of launchBrowser method, of class Launch.
     */
    @Test
    public void testLaunchBrowser()
    {
        System.out.println("launchBrowser");
        String theLink = "http://hsvo.ca";
        Launch instance = new Launch();
        instance.launchBrowser(theLink);
        try
        {
            URI webPath = new URI(theLink);
            assertNotNull(webPath.getPath());
        }
        catch(URISyntaxException uriSyntaxEx)
        {
            uriSyntaxEx.printStackTrace();
        }
    }

    /**
     * Test of launchDesktop method, of class Launch.
     */
    @Test
    public void testLaunchDesktop()
    {
        System.out.println("launchDesktop");
        String theProgram = "C:/HSVO_RSV/RSV_Viewer/RSV_Native/Windows/InfoTools/RSV.exe";
        String[] commands = {"C:/HSVO_RSV/RSV_Viewer/RSV_Native/Windows/InfoTools/rsv.RSVSvr"};
        Launch instance = new Launch();
        instance.launchDesktop(theProgram, commands);
        File installPath = new File(theProgram);
        boolean exists = installPath.exists();
        assertTrue(exists);
    }


    public static void main(String[] args) throws Exception
    {
        String inputLine = "";

        if (args.length < 1) { inputLine = "calc"; }
        else { inputLine = args[1]; }

        System.out.println(inputLine); //print the input program name before opening it

        if (inputLine.equalsIgnoreCase("savoir"))
        {
            String link = "http://198.164.40.210:8080/savoir2/";
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

        inputLine = "";

        if (args.length < 1) { inputLine = "volseg"; }
        else { inputLine = args[1]; }

        System.out.println(inputLine); //print the input program name before opening it

        if (inputLine.equalsIgnoreCase("notepad"))
        {
            //Runtime.getRuntime().exec("notepad.exe");
            String program = "C:/WINDOWS/system32/notepad.exe";
            String[] progArgs = {""};
            Launch launcher = new Launch();
            launcher.launchDesktop(program, progArgs);
        }
        else if (inputLine.equalsIgnoreCase("paint"))
        {
            //Runtime.getRuntime().exec("mspaint.exe");
            String program = "C:/WINDOWS/system32/mspaint.exe";
            String[] progArgs = {"SUPPORTED_OS: Windows XP"};
            Launch launcher = new Launch();
            launcher.launchDesktop(program, progArgs);
        }
        else if (inputLine.equalsIgnoreCase("calculator") || inputLine.equalsIgnoreCase("calc"))
        {
            //Runtime.getRuntime().exec("mspaint.exe");
            String program = "C:/WINDOWS/system32/calc.exe";
            String[] progArgs = {""};
            Launch launcher = new Launch();
            launcher.launchDesktop(program, progArgs);
        }
        else if (inputLine.equalsIgnoreCase("rsv"))
        {
            String program = "C:/HSVO_RSV/RSV_Viewer/RSV_Native/Windows/InfoTools/RSV.exe";
            String[] progArgs = {"C:/HSVO_RSV/RSV_Viewer/RSV_Native/Windows/InfoTools/rsv.RSVSvr"};
            Launch launcher = new Launch();
            launcher.launchDesktop(program, progArgs);
        }
        else if (inputLine.equalsIgnoreCase("volseg"))
        {
            String program = "C:/InfoTools/PC_VRD.exe";
            String[] progArgs = {"C:/InfoTools/Vrd_Server_Proxy.VrdSvr"};
            Launch launcher = new Launch();
            launcher.launchDesktop(program, progArgs);
        }
        else if (inputLine.equalsIgnoreCase("simman"))
        {
            try
            {
                String program = "C:/Prorgram Files/Laerdal Medical/ri/ri.exe";
                String[] progArgs = {"C:/HSVO/Healthy patient.sce SUPPORTED_OS: Windows XP | Mac"};
                Launch launcher = new Launch();
                launcher.launchDesktop(program, progArgs);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("Usage:  java TestDesktop file://path/desktopApp launchArgs");
	    System.err.println("\nLaunching sample...");
        }
    }
}
