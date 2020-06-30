<?xml version="1.0" encoding="US-ASCII"?>
<!--
 XSLT that generated the APIImpl.java class.

 $Id: api_to_java.xslt,v 1.39 2012/03/10 15:10:34 agoubard Exp $

 See the COPYRIGHT file for redistribution and use restrictions.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Define parameters -->
	<xsl:param name="xins_home"    />
	<xsl:param name="project_home" />
	<xsl:param name="project_file" />
	<xsl:param name="specsdir"     />
	<xsl:param name="impl_file"    />
	<xsl:param name="package"      />

	<!-- Perform includes -->
	<xsl:include href="../casechange.xslt" />
	<xsl:include href="../hungarian.xslt" />
	<xsl:include href="../java.xslt" />
	<xsl:include href="../rcs.xslt"  />

	<xsl:output method="text" />

	<!-- Determine name of API -->
	<xsl:variable name="api" select="/api/@name" />

	<xsl:variable name="project_node" select="document($project_file)/project" />

	<xsl:template match="api">
		<xsl:apply-templates select="document($impl_file)/impl">
			<xsl:with-param name="api_node" select="." />
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="impl-java | impl">
		<xsl:param name="api_node" />

		<xsl:call-template name="java-header" />
		<xsl:text>package </xsl:text>
		<xsl:value-of select="$package" />
		<xsl:text>;</xsl:text>
		<xsl:text><![CDATA[

import org.xins.server.API;

/**
 * Implementation of <code>]]></xsl:text>
		<xsl:value-of select="$api" />
		<xsl:text><![CDATA[</code> API.
 */
public class APIImpl extends API {

   private final RuntimeProperties _runtimeProperties;]]></xsl:text>

		<xsl:for-each select="instance">
			<xsl:text>

   private final </xsl:text>
			<xsl:value-of select="@class" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="@name" />
			<xsl:text>;</xsl:text>
		</xsl:for-each>

		<xsl:text><![CDATA[

   /**
    * Constructs a new <code>APIImpl</code> instance.
    */
   public APIImpl() {
      super("]]></xsl:text>
		<xsl:value-of select="$api" />
		<xsl:text>");
      _runtimeProperties = new RuntimeProperties();</xsl:text>
		<xsl:if test="instance">
			<xsl:text>

      // Creation of the shared instances</xsl:text>
		</xsl:if>
		<xsl:for-each select="instance">
			<xsl:text>
      </xsl:text>
			<xsl:value-of select="@name" />
			<xsl:text> = new </xsl:text>
			<xsl:value-of select="@class" />
			<xsl:text>(this);</xsl:text>
		</xsl:for-each>
		<xsl:text>

      // Creation of the functions</xsl:text>
		<xsl:for-each select="$api_node/function">

					<xsl:text>
      new </xsl:text>
			<xsl:value-of select="@name" />
			<xsl:text>Impl(this);</xsl:text>
		</xsl:for-each>
		<xsl:text>
   }</xsl:text>

		<xsl:text><![CDATA[

   /**
    * Gets the class used to access the defined runtime properties
    *
    * @return
    *    the runtime properties, never <code>null</code>code>.
    */
   public RuntimeProperties getProperties() {
      return _runtimeProperties;
   }

   /**
    * Triggers re-initialization of this API.
    */
   void reinitialize() {
      super.reinitializeImpl();
   }
]]></xsl:text>
	<xsl:if test="instance">
		<xsl:text><![CDATA[
   protected void bootstrapImpl2(java.util.Map<String, String> properties)
   throws org.xins.common.collections.MissingRequiredPropertyException,
          org.xins.common.collections.InvalidPropertyValueException,
          org.xins.common.manageable.BootstrapException {]]></xsl:text>
		<xsl:for-each select="instance">
			<xsl:text>
      add(</xsl:text>
			<xsl:value-of select="@name" />
			<xsl:text>);</xsl:text>
		</xsl:for-each>
		<xsl:text>
   }</xsl:text>
	</xsl:if>

		<xsl:for-each select="instance">
			<xsl:text>

   public </xsl:text>
			<xsl:value-of select="@class" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="@getter" />
			<xsl:text>() {
      return </xsl:text>
			<xsl:value-of select="@name" />
			<xsl:text>;
   }</xsl:text>
		</xsl:for-each>
		<xsl:text>
}</xsl:text>
	</xsl:template>
</xsl:stylesheet>
