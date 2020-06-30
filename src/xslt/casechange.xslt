<?xml version="1.0" encoding="US-ASCII"?>
<!--
 Utility XSLT that provides templates to convert a text to lower text or to upper case.

 $Id: casechange.xslt,v 1.10 2010/09/29 17:21:48 agoubard Exp $

 See the COPYRIGHT file for redistribution and use restrictions.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template name="toupper">
		<xsl:param name="text" />
		<xsl:value-of select="translate($text,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
	</xsl:template>

	<xsl:template name="tolower">
		<xsl:param name="text" />
		<xsl:value-of select="translate($text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
	</xsl:template>

</xsl:stylesheet>
