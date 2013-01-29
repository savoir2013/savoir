// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Launch
 *  Launches a "desktop" or "webapp" based application
 * @author copelandb
 */

public class Launch
{
    public boolean canLaunchBrowser;
    public boolean canLaunchDesktop;
    
    private static Desktop desktop;
    private static Desktop.Action action;


    //constructor within which we find out what actions the user's OS supports
    public Launch()
    {
        canLaunchDesktop = Desktop.isDesktopSupported() ? true : false;
        if (canLaunchDesktop)
        {
           desktop = Desktop.getDesktop();
        }
        canLaunchBrowser = desktop.isSupported(action.BROWSE) ? true : false;
    }



    /*
     *launchBrowser
     *  Launch a Web-based Application with private Desktop desktop;
     *       private Desktop.Action action;
     *       private Properties appProps;in a browser (arguments can be appended to the actual URL as per a regular HTTP Request)
     *@param theProgram String
     *@param commands String array representing a list of commands
     */
    public void launchBrowser(String theLink)
    {
        if (canLaunchBrowser)
        {
            try
            {
                URI uri = URI.create(theLink);
                desktop = Desktop.getDesktop();
                desktop.browse(uri);
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
        else
        {
            System.out.println("Desktop Application not supported on this desktop");
        }
    }




    /**
     *launchDesktop
     *  Launch a Desktop Application with a specified set of commands (arguments)
     *@param theProgram String
     *@param commands String array representing a list of commands
     *    command args should contain (supportedOS and Download info) somewhere, preferably appended at the end:
     *    " SUPPORTED_OS: a | b | c , DOWNLOAD_URL: http://site.com/download"
     */
    public void launchDesktop(String theProgram, String[] commands)
    {
        if (canLaunchDesktop)
        {
            try
            {
                File installPath=new File(theProgram);
                boolean exists = installPath.exists();
                if (!exists) // user doesn't have the required application
                {
                    Download app = new Download();  // try to download the application
                    app.downloadToDesktop(theProgram, commands);
                }
                else    //application exists so we'll try to launch it with the supplied parameters (launchargs)
                {
                    desktop = Desktop.getDesktop();
                    try
                    {
                        List<String> command = new ArrayList<String>();
                        command.add(theProgram);
                        for ( int i = 0; i < commands.length; i++ )    //process the list of parameters
                        {
                            if (!commands[i].contains("SUPPORTED_OS"))
                                command.add(commands[i]);
                        }
                        ProcessBuilder builder = new ProcessBuilder(command);
                        final Process process = builder.start();
                        InputStream is = process.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String line;
                        while ((line = br.readLine()) != null)          //listen for output from the launched program
                        {
                            System.out.println(line);
                        }
                    }
                    catch(IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            System.out.println("Desktop Application not supported on this desktop!");
        }
    }

}
