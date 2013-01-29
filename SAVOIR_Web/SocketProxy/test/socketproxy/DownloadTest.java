// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.io.File;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DownloadTest
 *  Download required software from the web
 * @author Bryan Copeland 09/09/09
 * @version 2.0
 */
public class DownloadTest extends TestCase
{
    String downloadURL = "";     // URL of a file to Download
    String[] supportedOS = {""};        // CSV string of Supported Operating Systems

    static Download dl = new Download();    // Download object

    public DownloadTest(String url)
    {
       super(url);
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
     * Test of downloadToDesktop method, of class Download.
     */
    @Test
    public void testDownloadToDesktop() {
        System.out.println("downloadToDesktop");
        String downloadURL = "http://www.laerdal.com/binaries/SimMan3.3.1.zip";
        String[] supportedOS = null;
        Download instance = new Download();
        instance.downloadToDesktop(downloadURL, supportedOS);
        File installPath=new File("C:\\Program Files\\Laerdal Medical\\SimMan\\ri\\ri.exe");
        boolean exists = installPath.exists();
        assertTrue(exists);
    }

    public static void main(String[] args) throws Exception
    {
        String downloadURL = "";     //Web Service URL
        String[] supportedOS = {""};        //XML (File) to send

        if (args.length == 2)
        {
            downloadURL = args[0];
            supportedOS = args;
        }
        else
        {
            System.err.println("Usage:  java TestDownload http://downloadURL {supportedOS}");
	    System.err.println("\nSample call:");
            //try to get user to download SimMan software if on Windows
            //        Info link:  http://www.laerdal.com/document.asp?docid=1022609
            //    Download link:  http://www.laerdal.com/binaries/SimMan3.3.1.zip
            downloadURL = "http://www.laerdal.com/binaries/SimMan3.3.1.zip";
            supportedOS[0] = "Windows XP";
        }
        dl.downloadToDesktop(downloadURL, supportedOS); //download the application to the desktop
    }
   

}
