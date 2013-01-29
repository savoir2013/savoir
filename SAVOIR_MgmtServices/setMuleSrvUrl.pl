#! /usr/bin/perl

# Licensed under Apache 2.0
# Copyright 2011, National Research Council of Canada
# Property of Lakehead University


#############################################################################
#              National Research Council -- IIT - Fredericton
#
# PROJECT:      SAVOIR
# AUTHOR(S):    Justin Hickey
# PURPOSE:      To set the mule server URL for the file
#				WebContent/WEB-INF/beans.xml. This should eventually be
#				replaced with an Ant task that can edit the XML file properly.
#				For now, we use this script since it was quicker to write.
# CMD LINE ARG: none
# OUTPUT:       none
# DATE CREATED: Feb 28 2011
# LAST MOD: 	$Date:$
# LAST MOD BY:	$Author: hickeyj $
# USAGE:    	For internal NRC use only
# COMMENTS: 	none
#
#############################################################################

# ****************************** Main Script ********************************

# Declare variables
my(@xml);		# Contents of the XML file
my($line);  	# Single line from one of the files
my($url);		# URL of the mule server

# Use any pragmas
use strict;
use English;

# Open the build.properties file
open(PROP, "build.properties") || die "Failed to open build.properties: $!\n";

# Start reading the properties file
while ($line = <PROP>)
{ 
	# Check if this is the mule server URL property
    if ($line =~ /^savoir\.mgmt\.mule\.server\.url=/)
    {
		# Save the url
		$url = $POSTMATCH;
		chomp($url);
		
		# Stop reading the file once we have the data
		last;
    }
}

# Close the property file
close(PROP);

# Read in the XML file
open(XML, "WebContent/WEB-INF/beans.xml") || die "Failed to open XML: $!\n";
@xml = <XML>;
close(XML);

# Open the XML file for output
open(OUT, ">WebContent/WEB-INF/beans.xml") || die "Failed to open XML: $!\n";

# Go through the file and replace the URL
foreach $line (@xml)
{
	if (!($line =~ /localhost/))
	{
		if ($line =~ /services\/EdgeServicesPrototype/)
		{
			$line =~ s/http:\/\/[^\/]*\//$url/;
			$line =~ s/\\//g;
		}
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
