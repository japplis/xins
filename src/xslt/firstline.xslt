<?xml version="1.0" encoding="US-ASCII" ?>
<!--
 Utility XSLT that provides a template that returns the first line of a given text.

 $Id: firstline.xslt,v 1.10 2010/09/29 17:21:48 agoubard Exp $

 See the COPYRIGHT file for redistribution and use restrictions.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template name="firstline">
		<xsl:param name="text" />

		<xsl:choose>
			<xsl:when test="not(boolean($text))">
				<xsl:value-of select="''" />
			</xsl:when>
			<xsl:when test="contains($text, '. ')">
				<xsl:value-of select="substring-before($text, '. ')" />
			</xsl:when>
			<xsl:when test="substring($text, string-length($text)) = '.'">
				<xsl:value-of select="substring($text, 1, string-length($text) - 1)" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
