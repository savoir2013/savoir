// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import javax.swing.JOptionPane;

/**
 * Download
 *   Enables the downloading of desktop software for an Edge Device, if it is
 *    not currently installed on the user's system, but required by SAVOIR in an
 *    authored session, or if the user is attempting to use it in on their own
 *    in a MySAVOIR adhoc session.
 *@author copelandb
 */
public class Download
{

    public String download;
    public String[] osList;
    
    //empty constructor 
    public Download() { }
    
    /**
     * Download constructor
     * @param downloadURL String representing the URL where this device (application) can be downloaded
     * @param supportedOS String (optional) list of supported Operating Systems (OS's)
     */
    public Download (String url, String[] os)
    {
        download=url;
        osList=os;
    }

    /**
     * downloadToDesktop
     *   Downloads an application to the user's Desktop if it was not found when trying to load
     * SAMPLE CALL
     *   String[] os = {"Windows","Mac","Linux"};
     *   downloadToDesktop("http://host.domain/file.zip", os );
     * @param downloadURL String representing the URL where this device (application) can be downloaded
     * @param supportedOS String (optional) list of supported Operating Systems (OS's)
     * @return null
     */
    public void downloadToDesktop(String downloadURL, String[] supportedOS)
    {
        download = downloadURL;
        if (supportedOS == null || supportedOS.length == 0)
        {
            osList = DEFAULT_OS_LIST; //use default Windows,Mac,Linux list
        }
        else
        {
            osList = supportedOS;
        }
        
        for (int i = 0; i < osList.length; i++)
        {
            if (osList[i].contains(OS_NAME))
            {
                Object[] options = { "Download Now", "I have it already" };
                int selectedValue = JOptionPane.showOptionDialog(null, "In order to continue, some missing third-party software must be installed on your system.\n\nWould you like to download and install now? \n(Note, some systems may not be supported yet)", "Edge Device Software Missing", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (selectedValue == JOptionPane.YES_OPTION)
                {
                    try
                    {
                        URI uri = URI.create(download);
                        desktop = Desktop.getDesktop();                        
                        desktop.browse(uri);  //launch the local default browser to download and install the application from the web
                    }
                    catch(IOException ioEx)
                    {
                        ioEx.printStackTrace();
                    }
                }
                else if (selectedValue == JOptionPane.NO_OPTION)
                {
                    JOptionPane.showMessageDialog(null,"Please re-locate the files/folder to the default directory\n");
                    return;
                }
                else
                {
                    JOptionPane.showMessageDialog(null,"Unable to access software download.\n\nTry to download from: \n" + download);
                    return;
                }
            }
            else
            {
                JOptionPane.showMessageDialog(null, "Sorry, " + OS_NAME + " is not currently supported.");
                return;
            }
        }
    }
    
    static String PROGRAMFILES = System.getenv("programfiles");
    static String FILE_SEPARATOR   = System.getProperty("file.separator");
    static String OS_NAME = System.getProperty("os.name");
    static String[] DEFAULT_OS_LIST = {"Windows XP","Windows Vista","Windows","Mac","Linux"};
    private Desktop desktop;
}
