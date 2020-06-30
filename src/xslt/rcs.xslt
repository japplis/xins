<?xml version="1.0" encoding="US-ASCII" ?>
<!--
 XSLT template that extracts the revision date from the revision argument.

 $Id: rcs.xslt,v 1.10 2010/09/29 17:21:48 agoubard Exp $

 See the COPYRIGHT file for redistribution and use restrictions.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template name="revision2string">
		<xsl:param name="revision" />

    	<xsl:choose>
			<xsl:when test="string-length($revision) &lt; 16">
				<xsl:text>?.?</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="substring($revision, 12, string-length($revision) - 13)" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
