<?xml version="1.0" encoding="US-ASCII"?>
<!--
 Utility XSLT that displays a warning message to the console.

 $Id: warning.xslt,v 1.7 2010/09/29 17:21:48 agoubard Exp $

 See the COPYRIGHT file for redistribution and use restrictions.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template name="warn">
		<xsl:param name="message" />
		<xsl:message terminate="no">
			<xsl:text>
 *** WARNING: </xsl:text>
			<xsl:value-of select="$message" />
			<xsl:text> ***</xsl:text>
		</xsl:message>
	</xsl:template>
</xsl:stylesheet>
