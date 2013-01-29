// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;

import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author copelandb
 */
public class ClientInfoTest extends TestCase
{

  private static ClientInfo ci = new ClientInfo();

  @Test
  public void testGetOSName()
  {
     assertEquals(ci.getOSName(), System.getProperty("os.name"));
  }

  @Test
  public void testGetOSType()
  {
     assertEquals(ci.getOSType(), System.getProperty("os.arch"));
  }

  @Test
  public void testGetOSVersion()
  {
     assertEquals(ci.getOSVersion(), System.getProperty("os.version"));
  }

  @Test
  public void testGetOSJavaVersion()
  {
     assertEquals(ci.getOSJavaVersion(), System.getProperty("java.version"));
  }

  @Test
  public void testGetOSJavaEnvironment()
  {
     assertEquals(ci.getOSJavaEnvironment(), System.getenv("JAVA_HOME"));
  }

  @Test
  public void testGetOSNumberOfProcessors()
  {
     assertEquals(Integer.parseInt(ci.getOSNumberOfProcessors().toString()), Runtime.getRuntime().availableProcessors());
  }

  @Test
  public void testGetUserAccountName()
  {
     assertEquals(ci.getUserAccountName(), System.getProperty("user.name"));
  }

  @Test
  public void testGetUserCountry()
  {
     assertEquals(ci.getUserCountry(), System.getProperty("user.country"));
  }

  @Test
  public void testGetUserLocale()
  {
     assertEquals(ci.getUserLocale(), System.getProperty("user.language"));
  }

  @Test
  public void testGetUserTimeZoneName()
  {
     assertEquals(ci.getUserTimeZoneName(), Calendar.getInstance().getTimeZone().getDisplayName());
  }

  @Test
  public void testGetUserTimeZoneID()
  {
     assertEquals(ci.getUserTimeZoneID(), Calendar.getInstance().getTimeZone().getID());
  }

  @Test
  public void testGetNetworkHostname()
  {
      try
      {
        assertEquals(ci.getNetworkHostname(), InetAddress.getLocalHost().getHostName());
      }
      catch (UnknownHostException uhEx)
      {
          uhEx.printStackTrace();
      }
  }

  @Test
  public void testGetNetworkInfo()
  {

        for (NetworkInterface netint : Collections.list(ci.getNetworks()))
        {
            System.out.println("\nDisplay name : " + netint.getDisplayName());

            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses))
            {
              System.out.println("IP Address : " + inetAddress.toString());
            }
        }
  }

  @Test
  public void testGetMaxMemorySize()
  {
     assertEquals(ci.getMaxMemorySize(),  Runtime.getRuntime().maxMemory());
  }

  @Test
  public void testGetHeapMemorySize()
  {
     assertEquals(ci.getHeapMemorySize(),  Runtime.getRuntime().totalMemory());
  }

  @Test
  public void testGetFreeMemorySize()
  {
     assertEquals(ci.getFreeMemorySize(),  Runtime.getRuntime().freeMemory());
  }

  public static void main(String args[])
  {
        ////////////////////////////////////////////////////////////////////////////
        // Operating System info
          System.out.println("\n\nOperating System");
          System.out.println("OS name: "+ ci.getOSName());
          System.out.println("OS type: "+ ci.getOSType());

          System.out.println("OS version: "+ci.getOSVersion());
          System.out.print("JAVA (runtime) version: "+ci.getOSJavaVersion());

          System.out.print("; JAVA_HOME="+ci.getOSJavaEnvironment());

          System.out.println("\nProcessors available to JVM: " + ci.getOSNumberOfProcessors());

           ////////////////////////////////////////////////////////////////////////////
           //User info
            System.out.println("\n\nUser");
            System.out.println("Account name: "+ci.getUserAccountName());

            System.out.println("Country: "+ci.getUserCountry());

            System.out.println("Locale: "+ci.getUserLocale());

            System.out.println("TimeZone: "+ci.getUserTimeZoneName());
            System.out.println("ID: "+ci.getUserTimeZoneID());

        ////////////////////////////////////////////////////////////////////////////
        // Network info
          System.out.println("\n\nNetwork");
            // Get the HOSTNAME
                System.out.println("hostname="+ci.getNetworkHostname());


            for (NetworkInterface netint : Collections.list(ci.getNetworks()))
            {
                System.out.println("\nDisplay name : " + netint.getDisplayName());

                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses))
                {
                  System.out.println("IP Address : " + inetAddress.toString());
                }
            }

        ////////////////////////////////////////////////////////////////////////////
        // Application
          System.out.println("\n\nApplication");
            System.out.println("Max Memory: "+ci.getMaxMemorySize());
            System.out.println("Heap Size: "+ci.getHeapMemorySize());
            System.out.println("Free Memory: "+ci.getFreeMemorySize());
  }
}
