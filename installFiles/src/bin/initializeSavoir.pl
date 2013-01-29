#! /usr/bin/perl

# Licensed under Apache 2.0
# Copyright 2011, National Research Council of Canada

#############################################################################
#              National Research Council -- IIT - Fredericton
#
# PROJECT:      SAVOIR
# AUTHOR(S):    Justin Hickey
# PURPOSE:      To check several settings and install several resources
#				before SAVOIR is deployed.
# CMD LINE ARG: none
# OUTPUT:       Messages to user
# DATE CREATED: Sep 09 2011
# LAST MOD: 	$Date:$
# LAST MOD BY:	$Author: hickeyj $
# USAGE:    	For internal NRC use and distribution with SAVOIR only
# COMMENTS: 	none
#
#############################################################################

# ****************************** Main Script ********************************

# Declare variables
my($envErr);	    	# Flag for checking existance of environment variables
my($sessionLogPath);	# Path to directory for session log files
my($webDocRoot);		# Path to the web server document root
my($command);	    	# System command to run

# Use any pragmas
use strict;
use English;
use Env qw(CATALINA_HOME MULE_HOME ANT_HOME);
use File::Path qw(make_path);
use File::Copy qw(copy);

# Set the environment error flag to false
$envErr = 0;

# Set the umask so that created directories have the proper group permissions
umask(0002);

# Check if the home envirionment variables exist - this does not verify if
# they are valid values - simply check if they start with a slash (/) 
if (!($CATALINA_HOME =~ /^\//))
{
	$envErr = 1;
	print "ERROR: The environment variable CATALINA_HOME is not defined.\n";
}

if (!($MULE_HOME =~ /^\//))
{
	$envErr = 1;
	print "ERROR: The environment variable MULE_HOME is not defined.\n";
}

if (!($ANT_HOME =~ /^\//))
{
	$envErr = 1;
	print "ERROR: The environment variable ANT_HOME is not defined.\n";
}

if ($envErr)
{
	print "Please define any environment variables mentioned above to\n";
	die "appropriate values and then run this script again.\n";
}

# Obtain the document root of the web server
# First open the httpd config file
if (-e "/etc/httpd/conf/httpd.conf")
{
	open(HTTPD_CONF, "/etc/httpd/conf/httpd.conf") ||
		die "Failed to open the httpd config file: $OS_ERROR\n";
}
else
{
	print "The httpd config file /etc/httpd/conf/httpd.conf does not exist.\n";
	print "Please install Apache or edit this script to open the correct\n";
	die "httpd config file\n";
}

# Start reading in the httpd config file
while (<HTTPD_CONF>)
{
	# Find the document root
	if ($ARG =~ /^DocumentRoot\s+/)
	{
		$webDocRoot = $POSTMATCH;
		chomp $webDocRoot;
		
		# Strip off any double quotes and trailing slashes
		$webDocRoot =~ s/\"//g;
		$webDocRoot =~ s/\/*$//;
		
		last;
	}
}

# Close the httpd config file
close (HTTPD_CONF);

# Make the directory for the session logs
$sessionLogPath = "$CATALINA_HOME" . '/logs/runSessions';

if (!(-e $sessionLogPath))
{
	make_path ("$sessionLogPath") ||
		die "Failed to create session log directory: $OS_ERROR\n";
}

# Make sure the six perl scripts in the code are executable
chmod (0755, "../src/SAVOIR_Bus/setActivemqSrvUrl.pl",
	"../src/SAVOIR_MgmtServices/setMuleSrvUrl.pl",
	"../src/SAVOIR_SampleDevices/EKGTutorial/setConfigData.pl",
	"../src/SAVOIR_SampleDevices/VaderWebTutorial/setConfigData.pl",
	"../src/SAVOIR_SampleDevices/VaderDesktop/setConfigData.pl",
	"../src/SAVOIR_Web/SocketProxy/setJnlpData.pl") ||
	die "Failed to set perl scripts to be executable: $OS_ERROR\n";

# Copy the activemq-all jar file to mule
copy("../resources/activemq/activemq-all-5.3.0.jar", "$MULE_HOME/lib/opt") ||
	die "Failed to copy activemq-all: $OS_ERROR\n";

# Copy the resource web files to the web server directory
CopyResources("../resources", $webDocRoot);

# Inform users of the computer's IP address and hostname
print "\nThe following is the hostname and IP address for this computer.\n";
print "Please use one of these values (preferably the IP address) when\n";
print "editing appropriate properties that define this host in the\n";
print "localDefault.properties file.\n\n";

$command = "$ANT_HOME" . "/bin/ant ";
$command .= "-f ../src/SAVOIR_MasterBuild/build.xml check-host";
system ("$command") && die "Failed to print host info: $OS_ERROR\n";
	
print "\nSavoir initialization complete.\n";

# Exit the script
exit (0);

# ************************* Internal Procedures *****************************

#############################################################################
#
# PROC NAME:    CopyResources
# AUTHOR(S):    Justin Hickey
# PURPOSE:      To recursively copy the SAVOIR resources to the web server
#				directory.
# INPUT VARS:   $dir
# RETURN VARS:  none
# DATE CREATED: Sep 12 2011
#
#############################################################################

sub CopyResources
{
    # Declare local variables
    my($dir) = shift;	    # Directory to copy
	my($webRoot) = shift;	# Document root of web server
    my(@files);     	    # List of files in directory
	my($path);				# Path of new web site directory
    my($file);	    	    # Individual file in directory
	my($destDir);			# Destination directory
    
    # Obtain the destination directory
	$destDir = $webRoot;
	
	if (!($dir =~ /resources/))
	{
		$destDir = $destDir . '/' . $dir;
	}
			
	# Open the directory and read in the files - skip . and ..
    opendir(DIR, "$dir") || die "Could not open directory $dir: $OS_ERROR\n";
    @files = grep(!/^\.\.?$/, readdir(DIR));
    closedir(DIR);
    
    # Change to the directory
    chdir($dir) || die "Could not change to directory $dir: $OS_ERROR\n";
    
    # Go through the files
    foreach $file (@files)
    {
		# Skip any files that are not web server files
		if ($file eq "savoirdb.sql")
		{
			next;
		}
		
		# Check if the entry is a directory
		if (-d $file)
		{
			# Skip the activemq directory since it is not a web server directory
			if ($file eq "activemq")
			{
				next;
			}
		
			# Create the path and then search it
			$path = $webRoot . '/' . $file;
			
			if (!(-e $path))
			{
				make_path ("$path") ||
					die "Failed to create directory $path: $OS_ERROR\n";
			}
			
   			CopyResources($file, $webRoot);
	    	chdir("..") || die "Could not change to directory .. from
		    	$file: $OS_ERROR\n";
		}
		else
		{
			# Copy the file
	    	copy($file, $destDir) || die "Failed to copy $file: $OS_ERROR\n";
		}
    }
}

__END__
