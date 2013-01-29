#! /usr/bin/perl

# Licensed under Apache 2.0
# Copyright 2011, National Research Council of Canada
# Property of Lakehead University


#############################################################################
#              National Research Council -- IIT - Fredericton
#
# PROJECT:      SAVOIR
# AUTHOR(S):    Justin Hickey
# PURPOSE:      To set the web URL and loaded jar files inside the socket
#				proxy .jnlp file. This should eventually be replaced with an
#				Ant task that can edit the .jnlp file properly. For now, we
#				use this script since it was quicker to write.
# CMD LINE ARG: none
# OUTPUT:       none
# DATE CREATED: Oct 19 2011
# LAST MOD: 	$Date:$
# LAST MOD BY:	$Author: hickeyj $
# USAGE:    	For internal NRC use only
# COMMENTS: 	none
#
#############################################################################

# ****************************** Main Script ********************************

# Declare variables
my($jnlpFile);		# File name of source jnlp file
my(@jnlp);			# Contents of the jnlp file
my($line);  		# Single line from one of the files
my($url);			# URL of the web server
my($activemqVer);	# Version of AvtiveMQ installed on server

# Use any pragmas
use strict;
use English;

# Open the build.properties file
open(PROP, "build.properties") || die "Failed to open build.properties: $!\n";

# Start reading the properties file
while ($line = <PROP>)
{ 
	# Check if this is the socket proxy web URL property
    if ($line =~ /^savoir\.skpx\.web\.url=/)
    {
		# Save the url
		$url = $POSTMATCH;
		chomp($url);
		
		# Get rid of any back slashes
		$url =~ s/\\//g;
		
		# Get rid of any trailing slashes
		$url =~ s/\/*$//;
    }
	
	# Check if this is the activemq version property
    if ($line =~ /^savoir\.skpx\.activemq\.version=/)
    {
		# Save the activemq version
		$activemqVer = $POSTMATCH;
		chomp($activemqVer);
		
		# Get rid of any back slashes
		$activemqVer =~ s/\\//g;
    }
}

# Close the property file
close(PROP);

# Read in the jnlp file
open(JNLP, "savoirServerBridge.src.jnlp") || die "Failed to open jnlp file: $!\n";
@jnlp = <JNLP>;
close(JNLP);

# Open the jnlp file for output
open(OUT, ">savoirServerBridge.jnlp") || die "Failed to open jnlp file: $!\n";

# Go through the file and replace the data
foreach $line (@jnlp)
{
	if ($line =~ /codebase="http:\/\/0\.0\.0\.0\/"/)
	{
		$line =~ s/http:\/\/0\.0\.0\.0\//$url/;
		$line =~ s/\\//g;
		print OUT $line;
	}
	elsif ($line =~ /^<resources>/)
	{
		print OUT $line;
		print OUT "\n";
		
		# Print out the jar lines for activemq 5.2.0
		if ($activemqVer eq "5.2.0")
		{
			print OUT '<jar href="SocketProxy.jar" />' . "\n";
			print OUT '<jar href="lib/Savoir_MsgBindings.jar" />' . "\n";
			print OUT '<jar href="lib/activemq-all-5.2.0.jar" />' . "\n";
			print OUT '<jar href="lib/activemq-optional-5.2.0.jar" />' . "\n";
			print OUT '<jar href="lib/activemq-xmpp-5.2.0.jar" />' . "\n";
			print OUT '<jar href="lib/commons-httpclient-2.0.1.jar" />' . "\n";
			print OUT '<jar href="lib/commons-logging.jar" />' . "\n";
			print OUT '<jar href="lib/flex-messaging-common.jar" />' . "\n";
			print OUT '<jar href="lib/flex-messaging-core.jar" />' . "\n";
			print OUT '<jar href="lib/log4j-1.2.15.jar" />' . "\n";
			print OUT '<jar href="lib/merapi-core-0.0.1-beta.jar" />' . "\n";
			print OUT '<jar href="lib/spring.jar" />' . "\n";
			print OUT '<jar href="lib/tools.jar" />' . "\n";
			print OUT '<jar href="lib/xmlpull-1.1.3.4d_b4_min.jar" />' . "\n";
			print OUT '<jar href="lib/xstream-1.3.jar" />' . "\n";
			print OUT "\n";
		}

		# Print out the jar lines for activemq 5.3.0
		if ($activemqVer eq "5.3.0")
		{
			print OUT '<jar href="SocketProxy.jar" />' . "\n";
			print OUT '<jar href="lib/Savoir_MsgBindings.jar" />' . "\n";
			print OUT '<jar href="lib/activemq-all-5.3.0.jar" />' . "\n";
			print OUT '<jar href="lib/activemq-optional-5.3.0.jar" />' . "\n";
			print OUT '<jar href="lib/activemq-xmpp-5.3.0.jar" />' . "\n";
			print OUT '<jar href="lib/commons-httpclient-3.1.jar" />' . "\n";
			print OUT '<jar href="lib/commons-logging.jar" />' . "\n";
			print OUT '<jar href="lib/flex-messaging-common.jar" />' . "\n";
			print OUT '<jar href="lib/flex-messaging-core.jar" />' . "\n";
			print OUT '<jar href="lib/log4j-1.2.15.jar" />' . "\n";
			print OUT '<jar href="lib/merapi-core-0.0.1-beta.jar" />' . "\n";
			print OUT '<jar href="lib/spring.jar" />' . "\n";
			print OUT '<jar href="lib/tools.jar" />' . "\n";
			print OUT '<jar href="lib/xmlpull-1.1.3.4d_b4_min.jar" />' . "\n";
			print OUT '<jar href="lib/xstream-1.3.1.jar" />' . "\n";
			print OUT "\n";
		}

		# Print out the jar lines for activemq 5.5.0
		if ($activemqVer eq "5.5.0")
		{
			print OUT '<jar href="SocketProxy.jar" />' . "\n";
			print OUT '<jar href="lib/Savoir_MsgBindings.jar" />' . "\n";
			print OUT '<jar href="lib/activemq-all-5.5.0.jar" />' . "\n";
			print OUT '<jar href="lib/activemq-optional-5.5.0.jar" />' . "\n";
			print OUT '<jar href="lib/activemq-xmpp-5.5.0.jar" />' . "\n";
			print OUT '<jar href="lib/commons-codec-1.2.jar" />' . "\n";
			print OUT '<jar href="lib/commons-httpclient-3.1.jar" />' . "\n";
			print OUT '<jar href="lib/commons-logging.jar" />' . "\n";
			print OUT '<jar href="lib/flex-messaging-common.jar" />' . "\n";
			print OUT '<jar href="lib/flex-messaging-core.jar" />' . "\n";
			print OUT '<jar href="lib/log4j-1.2.15.jar" />' . "\n";
			print OUT '<jar href="lib/merapi-core-0.0.1-beta.jar" />' . "\n";
			print OUT '<jar href="lib/slf4j-log4j12-1.5.11.jar" />' . "\n";
			print OUT '<jar href="lib/spring.jar" />' . "\n";
			print OUT '<jar href="lib/tools.jar" />' . "\n";
			print OUT '<jar href="lib/xpp3-1.1.4c.jar" />' . "\n";
			print OUT '<jar href="lib/xstream-1.3.1.jar" />' . "\n";
			print OUT "\n";
		}
	}
	else
	{
		print OUT $line;
	}
}

# Close the jnlp file
close(OUT);

# Exit the script
exit (0);

# ************************* Internal Procedures *****************************

# none

__END__
