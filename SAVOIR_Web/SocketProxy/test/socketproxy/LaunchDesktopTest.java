// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.TestCase;
import org.junit.Test;


/**
 * LaunchDesktopTest
 *  Launch a Desktop-based application
 * @author Bryan Copeland 09/09/09
 * @version 2.0
 */

public class LaunchDesktopTest extends TestCase
{
    public LaunchDesktopTest (String input)
    {
        super(input);
    }

    /**
     * Test of launchDesktop method, of class Launch.
     */
    @Test
    public void testLaunchDesktop()
    {
        System.out.println("launchDesktop");
        String launchPath = "C:/HSVO_RSV/RSV_Viewer/RSV_Native/Windows/InfoTools/RSV.exe";
        Launch instance = new Launch();
        String[] commands = {"C:/HSVO_RSV/RSV_Viewer/RSV_Native/Windows/InfoTools/rsv.RSVSvr"};
        instance.launchDesktop(launchPath,commands);
        File installPath = new File(launchPath);
        boolean exists = installPath.exists();
        assertTrue(exists);
    }

    public static void main(String[] args) throws Exception
    {
        String inputLine = "";

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
