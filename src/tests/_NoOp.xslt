<?xml version="1.0" encoding="UTF-8" ?>

<!--
 XSLT that transform the _NoOp XML to an HTML page.

 $Id: _NoOp.xslt,v 1.1 2013/01/18 14:21:24 agoubard Exp $

 See the COPYRIGHT file for redistribution and use restrictions.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" />

    <xsl:template match="/">
        <html>
            <head>
                <title>_NoOp.xslt</title>
            </head>
            <body>
							<xsl:if test="/result[@name='errorcode']">
								Call failed :
								<xsl:value-of select="/result[@name='errorcode']" />
							</xsl:if>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
