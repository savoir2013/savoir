#! /usr/bin/perl

# Licensed under Apache 2.0
# Copyright 2011, National Research Council of Canada
# Property of Lakehead University


#############################################################################
#              National Research Council -- IIT - Fredericton
#
# PROJECT:      SAVOIR
# AUTHOR(S):    Justin Hickey
# PURPOSE:      To set the port number for the tomcat server in the
#				services-config.src.xml file. The Flex BlazeDS mechanism
#				used for communicating between the EKG tuorial system and
#				the EKG web tutorial system, bypasses the modjk system that
#				would redirect http requests to the tomcat server. Thus, we
#				need to set the port specifically to ensure that the request
#				is passed to tomcat.
#
#				Also this script will set properties for the EKG Desktop
#				application inside the EKG Desktop .xml file.
#
#				This should eventually be replaced with an Ant task that can
#				edit the XML file properly. For now, we use this script since
#				it was quicker to write.
# CMD LINE ARG: none
# OUTPUT:       none
# DATE CREATED: Dec 02 2011
# LAST MOD: 	$Date:$
# LAST MOD BY:	$Author: hickeyj $
# USAGE:    	For internal NRC use only
# COMMENTS: 	none
#
#############################################################################

# ****************************** Main Script ********************************

# Declare variables
my($xmlFile);		# File name of source XML file
my(@xml);			# Contents of the XML file
my($line);  		# Single line from one of the files
my($port);			# Port of the tomcat server
my($host);			# Host address of the web server
my($contactName);	# Contact name for the device
my($contactEmail);	# Contact email for the device

# Use any pragmas
use strict;
use English;

# Open the build.properties file
open(PROP, "build.properties") || die "Failed to open build.properties: $!\n";

# Start reading the properties file
while ($line = <PROP>)
{ 
	# Check if this is the contact name property
    if ($line =~ /^savoir\.ekgt\.deploy\.contact\.name=/)
    {
		# Save the property value
		$contactName = $POSTMATCH;
		chomp($contactName);
		
		# Get rid of any back slashes
		$contactName =~ s/\\//g;
		
		# Get rid of any trailing slashes
		$contactName =~ s/\/*$//;
    }

	# Check if this is the contact email property
    if ($line =~ /^savoir\.ekgt\.deploy\.contact\.email=/)
    {
		# Save the property value
		$contactEmail = $POSTMATCH;
		chomp($contactEmail);
		
		# Get rid of any back slashes
		$contactEmail =~ s/\\//g;
		
		# Get rid of any trailing slashes
		$contactEmail =~ s/\/*$//;
    }

	# Check if this is the EKG tutorial deploy host property
    if ($line =~ /^savoir\.ekgt\.deploy\.host=/)
    {
		# Save the property value
		$host = $POSTMATCH;
		chomp($host);
		
		# Get rid of any back slashes
		$host =~ s/\\//g;
		
		# Get rid of any trailing slashes
		$host =~ s/\/*$//;
    }

	# Check if this is the EKG tutorial deploy port property
    if ($line =~ /^savoir\.ekgt\.deploy\.port=/)
    {
		# Save the port
		$port = $POSTMATCH;
		chomp($port);
		
		# Get rid of any back slashes
		$port =~ s/\\//g;
		
		# Get rid of any trailing slashes
		$port =~ s/\/*$//;
    }
}

# Close the property file
close(PROP);

# Read in the xml file
open(XML, "resources/168_EKGDesktop_NRC.src.xml") ||
	die "Failed to open profile xml source file: $OS_ERROR\n";
@xml = <XML>;
close(XML);

# Open the xml file for output
open(OUT, ">resources/168_EKGDesktop_NRC.xml") ||
	die "Failed to open profile xml output file: $OS_ERROR\n";

# Go through the file and replace the properties
foreach $line (@xml)
{
	if ($line =~ /contactName="someone"/)
	{
		$line =~ s/someone/$contactName/;
		$line =~ s/\\//g;
	}
	
	if ($line =~ /contactEmail="somebody\@some.com"/)
	{
		$line =~ s/somebody\@some.com/$contactEmail/;
		$line =~ s/\\//g;
	}
	
	if ($line =~ /http:\/\/0\.0\.0\.0/)
	{
		$line =~ s/0\.0\.0\.0/$host/g;
		$line =~ s/\\//g;
	}
	
	print OUT $line;
}

# Close the XML file
close(OUT);

# Read in the XML file
open(XML, "web/WEB-INF/flex/services-config.src.xml") ||
	die "Failed to open XML file for input: $!\n";
@xml = <XML>;
close(XML);

# Open the XML file for output
open(OUT, ">web/WEB-INF/flex/services-config.xml") ||
	die "Failed to open XML file for output: $!\n";

# Go through the file and replace the data
foreach $line (@xml)
{
	if ($line =~ /server\.port/)
	{
		$line =~ s/{server\.port}/$port/;
		$line =~ s/\\//g;
	}

	print OUT $line;
}

# Close the XML file
close(OUT);

# Exit the script
exit (0);

# ************************* Internal Procedures *****************************

# none

__END__
