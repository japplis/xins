<?xml version="1.0"?>
<!--
 XSLT that generates the header of the generated java files.

 $Id: java.xslt,v 1.10 2010/09/29 17:21:48 agoubard Exp $

 See the COPYRIGHT file for redistribution and use restrictions.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template name="java-header">
		<xsl:text>// This is a generated file. Please do not edit.&#10;&#10;</xsl:text>
	</xsl:template>
</xsl:stylesheet>
