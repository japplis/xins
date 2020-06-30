<?xml version="1.0" encoding="US-ASCII"?>
<!--
 XSLT that generates the package.html which is used by the javadoc.

 $Id: api_to_packagehtml.xslt,v 1.14 2010/09/29 17:21:48 agoubard Exp $

 See the COPYRIGHT file for redistribution and use restrictions.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Define parameters -->
	<xsl:param name="api"          />

	<!-- Output is text/plain -->
	<xsl:output method="html" />

	<!-- ***************************************************************** -->
	<!-- Match the root element: api                                       -->
	<!-- ***************************************************************** -->

	<xsl:template match="api">

		<html>
			<body>
				<xsl:text>Client-side calling interface (CAPI) for the </xsl:text>
				<em>
					<xsl:value-of select="$api" />
				</em>
				<xsl:text> API.</xsl:text>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
