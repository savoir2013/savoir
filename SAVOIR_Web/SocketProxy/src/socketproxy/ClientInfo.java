// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.util.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author copelandb
 */
public class ClientInfo
{
    public void ClientInfo()
    {
    }

    public final TimeZone tz = Calendar.getInstance().getTimeZone();

    ////////////////////////////////////////////////////////////////////////////
    // Operating System info  
    public String getOSName()
    {
        String osName = System.getProperty("os.name");
        return osName;
    }
    
    public String getOSType()
    {
        String osType= System.getProperty("os.arch");
        return osType;
    }
    
    
    public String getOSVersion()
    {
        String osVersion = System.getProperty("os.version");
        return osVersion;
    }    
    
    public String getOSJavaVersion()
    {
        String javaVersion = System.getProperty("java.version");
        return javaVersion;
    }    
    
    public String getOSJavaEnvironment()
    {
        String environmentVariable = System.getenv("JAVA_HOME");
        return environmentVariable;
    }    
    
    public Integer getOSNumberOfProcessors()
    {
        Runtime runtime = Runtime.getRuntime();
        int numberOfProcessors = runtime.availableProcessors();
        return numberOfProcessors;
    }    
    ////////////////////////////////////////////////////////////////////////////
    //User info
    public String getUserAccountName()
    {
        String userName = System.getProperty("user.name");
        return userName;
    }    
    
    public String getUserCountry()
    {
        String userCountry = System.getProperty("user.country");
        return userCountry;
    }    
    
    public String getUserLocale()
    {
       String userLocale = System.getProperty("user.language");
       return userLocale;
    }    
        
    public String getUserTimeZoneName()
    {
        String timeZoneName = tz.getDisplayName();
        return timeZoneName;
    }    
    
    public String getUserTimeZoneID()
    {
        String timeZoneID = tz.getID();
        return timeZoneID;
    }    

    ////////////////////////////////////////////////////////////////////////////
    // Application
    public long getMaxMemorySize()
    {
        long maxMemorySize = Runtime.getRuntime().maxMemory();
        return maxMemorySize;
    }
    public long getHeapMemorySize()
    {
        long heapSize = Runtime.getRuntime().totalMemory();
        return heapSize;
    }
    public long getFreeMemorySize()
    {
        long free = Runtime.getRuntime().freeMemory();
        return free;
    }



    ////////////////////////////////////////////////////////////////////////////
    // Network info
    public String getNetworkIP()
    {
        String ipAddr = "";
        // Get the HOSTNAME and IP
        try
        {
            InetAddress addr = InetAddress.getLocalHost();
            ipAddr = addr.getHostAddress();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        return ipAddr;
    }

    // Get the HOSTNAME
    public String getNetworkHostname()
    {
        String hostname = "";
        try
        {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        return hostname;
    }
    
    // Get the IP address(es)
    public Enumeration<NetworkInterface> getNetworks()
    {
        Enumeration<NetworkInterface> nets = null;
        try 
        {           
            nets = NetworkInterface.getNetworkInterfaces();
            return nets;
        }
        catch (SocketException sockEx)
        {
            sockEx.printStackTrace();
        }
        return nets;
    }
}
