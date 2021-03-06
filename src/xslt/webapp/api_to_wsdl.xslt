<?xml version="1.0" encoding="UTF-8" ?>

<!--
 XSLT that generates the WSDL file from the API.

 $Id: api_to_wsdl.xslt,v 1.36 2010/09/29 17:21:48 agoubard Exp $

 See the COPYRIGHT file for redistribution and use restrictions.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
								xmlns="http://schemas.xmlsoap.org/wsdl/"
								xmlns:xsd="http://www.w3.org/2001/XMLSchema"
								xmlns:soapbind="http://schemas.xmlsoap.org/wsdl/soap/"
								version="1.0">

	<xsl:include href="../types.xslt" />

	<xsl:param name="project_home" />
	<xsl:param name="project_file" />
	<xsl:param name="specsdir"     />
	<xsl:param name="endpoint"     />
	<xsl:param name="xins_version" />
	<xsl:param name="timestamp"    />

	<xsl:output method="xml" indent="yes" />

	<xsl:variable name="project_node" select="document($project_file)/project" />
	<xsl:variable name="return">
		<xsl:text>
</xsl:text>
	</xsl:variable>
	<xsl:variable name="tab"><xsl:text>	</xsl:text></xsl:variable>
	<xsl:variable name="tab4"><xsl:text>				</xsl:text></xsl:variable>

	<xsl:template match="api">

		<xsl:variable name="apiname" select="@name" />
		<xsl:variable name="location">
			<xsl:choose>
				<xsl:when test="string-length($endpoint) > 0">
					<xsl:value-of select="$endpoint" />
				</xsl:when>
				<xsl:when test="$project_node/api[@name=$apiname]/environments">
					<xsl:variable name="env_file" select="concat($project_home, '/apis/', $apiname, '/environments.xml')" />
					<xsl:value-of select="document($env_file)/environments/environment[1]/@url" />
					<xsl:text>/?_convention=_xins-soap</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>http://localhost:8080/</xsl:text>
					<xsl:value-of select="$apiname" />
					<xsl:text>/?_convention=_xins-soap</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<definitions name="{$apiname}"
			targetNamespace="urn:{$apiname}"
			xmlns="http://schemas.xmlsoap.org/wsdl/"
			xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
			xmlns:soapbind="http://schemas.xmlsoap.org/wsdl/soap/"
			xmlns:http="http://schemas.xmlsoap.org/wsdl/http/"
			xmlns:xsd="http://www.w3.org/2001/XMLSchema"
			xmlns:tns="urn:apiname">
			<xsl:comment>
				<xsl:text> WSDL generated by XINS </xsl:text>
				<xsl:value-of select="$xins_version" />
				<xsl:text> on </xsl:text>
				<xsl:value-of select="$timestamp" />
				<xsl:text>. </xsl:text>
			</xsl:comment>
			<xsl:value-of select="concat($return, $tab)" />
			<types>
				<xsl:value-of select="concat($return, $tab, $tab)" />
				<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
										targetNamespace="urn:{$apiname}">

					<!-- Write the elements -->
					<xsl:apply-templates select="function" mode="elements">
						<xsl:with-param name="project_node" select="$project_node" />
						<xsl:with-param name="specsdir"     select="$specsdir"     />
						<xsl:with-param name="api"          select="$apiname"      />
					</xsl:apply-templates>

					<!-- Write the fault elements -->
					<xsl:apply-templates select="resultcode" mode="elements">
						<xsl:with-param name="project_node" select="$project_node" />
						<xsl:with-param name="specsdir"     select="$specsdir"     />
						<xsl:with-param name="api"          select="$apiname"      />
					</xsl:apply-templates>

					<!-- Write the defined types -->
					<xsl:apply-templates select="type" mode="types">
						<xsl:with-param name="project_node" select="$project_node" />
						<xsl:with-param name="specsdir"     select="$specsdir"     />
						<xsl:with-param name="api"          select="$apiname"      />
					</xsl:apply-templates>
				<xsl:value-of select="concat($return, $tab, $tab)" />
				</xsd:schema>
			<xsl:value-of select="concat($return, $tab)" />
			</types>

			<!-- Write the messages -->
			<xsl:apply-templates select="function" mode="messages" />
			<xsl:apply-templates select="resultcode" mode="messages" />

			<!-- Write the port types -->
			<xsl:value-of select="concat($return, $tab)" />
			<portType name="{$apiname}PortType">
				<xsl:apply-templates select="function" mode="porttypes" />
			<xsl:value-of select="concat($return, $tab)" />
			</portType>

			<!-- Write the bindings -->
			<xsl:value-of select="concat($return, $tab)" />
			<binding name="{$apiname}SOAPBinding" type="tns:{$apiname}PortType">
				<xsl:value-of select="concat($return, $tab, $tab)" />
				<documentation>
					<xsl:value-of select="description" />
				</documentation>
				<xsl:value-of select="concat($return, $tab, $tab)" />
				<soapbind:binding style="document" transport="http://schemas.xmlsoap.org/soap/http" />
				<xsl:apply-templates select="function" mode="bindings">
					<xsl:with-param name="location" select="$location" />
					<xsl:with-param name="apiname" select="$apiname" />
				</xsl:apply-templates>
				<xsl:value-of select="concat($return, $tab)" />
			</binding>

			<!-- Write the services -->
			<xsl:value-of select="concat($return, $tab)" />
			<service name="{$apiname}Service">
				<xsl:value-of select="concat($return, $tab, $tab)" />
				<port name="{$apiname}Port" binding="tns:{$apiname}SOAPBinding">
					<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
					<soapbind:address location="{$location}" />
				<xsl:value-of select="concat($return, $tab, $tab)" />
				</port>
			<xsl:value-of select="concat($return, $tab)" />
			</service>
		<xsl:value-of select="$return" />
		</definitions>
	</xsl:template>

	<xsl:template match="function" mode="elements">
		<xsl:param name="project_node" />
		<xsl:param name="specsdir"     />
		<xsl:param name="api"          />

		<xsl:variable name="function_file" select="concat($specsdir, '/', @name, '.fnc')" />
		<xsl:variable name="function_node" select="document($function_file)/function" />

		<xsl:if test="not($function_node/input)">
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			<xsd:element name="{concat(@name, 'Request')}" />
		</xsl:if>
		<xsl:apply-templates select="$function_node/input" mode="elements">
			<xsl:with-param name="project_node" select="$project_node" />
			<xsl:with-param name="specsdir"     select="$specsdir"     />
			<xsl:with-param name="api"          select="$api"          />
			<xsl:with-param name="elementname"  select="concat(@name, 'Request')" />
		</xsl:apply-templates>

		<xsl:if test="not($function_node/output)">
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			<xsd:element name="{concat(@name, 'Response')}" />
		</xsl:if>
		<xsl:apply-templates select="$function_node/output" mode="elements">
			<xsl:with-param name="project_node" select="$project_node" />
			<xsl:with-param name="specsdir"     select="$specsdir"     />
			<xsl:with-param name="api"          select="$api"          />
			<xsl:with-param name="elementname"  select="concat(@name, 'Response')" />
		</xsl:apply-templates>
	</xsl:template>


	<xsl:template match="resultcode" mode="elements">
		<xsl:param name="project_node" />
		<xsl:param name="specsdir"     />
		<xsl:param name="api"          />

		<xsl:variable name="resultcode_file">
			<xsl:choose>
				<xsl:when test="contains(@name, '/')">
					<xsl:value-of select="concat($project_home, '/apis/', substring-before(@name, '/'), '/spec/', substring-after(@name, '/'), '.rcd')" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat($specsdir, '/', @name, '.rcd')" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="resultcode_node" select="document($resultcode_file)/resultcode" />

		<xsl:if test="not($resultcode_node/output)">
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			<xsd:element name="{concat($resultcode_node/@name, 'Fault')}" nillable="true" />
		</xsl:if>
		<xsl:apply-templates select="$resultcode_node/output" mode="elements">
			<xsl:with-param name="project_node" select="$project_node" />
			<xsl:with-param name="specsdir"     select="$specsdir"     />
			<xsl:with-param name="api"          select="$api"          />
			<xsl:with-param name="elementname"  select="concat($resultcode_node/@name, 'Fault')" />
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="input | output" mode="elements">

		<xsl:param name="project_node" />
		<xsl:param name="specsdir"     />
		<xsl:param name="api"          />
		<xsl:param name="elementname"  />

		<!-- The input or output parameters of the function -->
		<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
		<xsd:element name="{$elementname}">
			<xsl:value-of select="concat($return, $tab4)" />
			<xsd:complexType>
				<xsl:value-of select="concat($return, $tab4, $tab)" />
				<xsd:sequence>
					<xsl:for-each select="param">
						<xsl:variable name="elementtype">
							<xsl:call-template name="elementtype">
								<xsl:with-param name="project_node" select="$project_node" />
								<xsl:with-param name="specsdir"     select="$specsdir" />
								<xsl:with-param name="api"          select="$api" />
								<xsl:with-param name="type"         select="@type" />
							</xsl:call-template>
						</xsl:variable>
						<xsl:variable name="minoccurs">
							<xsl:choose>
								<xsl:when test="@required = 'true'">1</xsl:when>
								<xsl:otherwise>0</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="paramname" select="@name" />

						<xsl:value-of select="concat($return, $tab4, $tab, $tab)" />
						<xsd:element name="{$paramname}" type="{$elementtype}" minOccurs="{$minoccurs}">
							<xsl:if test="@default and @type != '_date' and @type != '_timestamp'">
								<xsl:attribute name="default">
									<xsl:value-of select="@default" />
								</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="concat($return, $tab4, $tab, $tab, $tab)" />
							<xsd:annotation>
								<xsl:value-of select="concat($return, $tab4, $tab4)" />
								<xsd:documentation>
									<xsl:value-of select="description/text()" />
								</xsd:documentation>
							<xsl:value-of select="concat($return, $tab4, $tab, $tab, $tab)" />
							</xsd:annotation>
						<xsl:value-of select="concat($return, $tab4, $tab, $tab)" />
						</xsd:element>
					</xsl:for-each>
					<xsl:if test="data">
						<xsl:value-of select="concat($return, $tab4, $tab, $tab)" />
						<xsd:element name="data" minOccurs="0">
							<xsl:value-of select="concat($return, $tab4, $tab, $tab, $tab)" />
							<xsd:complexType>
								<xsl:value-of select="concat($return, $tab4, $tab4)" />
								<xsd:sequence>
									<xsl:if test="data/@contains">
										<xsl:variable name="contained_element" select="data/@contains" />
										<xsl:apply-templates select="data/element[@name=$contained_element]" mode="datasection">
											<xsl:with-param name="project_node" select="$project_node" />
											<xsl:with-param name="specsdir"     select="$specsdir" />
											<xsl:with-param name="api"          select="$api" />
										</xsl:apply-templates>
									</xsl:if>
									<xsl:for-each select="data/contains/contained">
										<xsl:variable name="contained_element" select="@element" />
										<xsl:apply-templates select="../../element[@name=$contained_element]" mode="datasection">
											<xsl:with-param name="project_node" select="$project_node" />
											<xsl:with-param name="specsdir"     select="$specsdir" />
											<xsl:with-param name="api"          select="$api" />
										</xsl:apply-templates>
									</xsl:for-each>
								<xsl:value-of select="concat($return, $tab4, $tab4)" />
								</xsd:sequence>
							<xsl:value-of select="concat($return, $tab4, $tab, $tab, $tab)" />
							</xsd:complexType>
						<xsl:value-of select="concat($return, $tab4, $tab, $tab)" />
						</xsd:element>
					</xsl:if>
				<xsl:value-of select="concat($return, $tab4, $tab)" />
				</xsd:sequence>
			<xsl:value-of select="concat($return, $tab4)" />
			</xsd:complexType>
		<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
		</xsd:element>
	</xsl:template>

	<xsl:template match="element" mode="datasection">

		<xsl:param name="project_node" />
		<xsl:param name="specsdir"     />
		<xsl:param name="api"          />

		<xsl:value-of select="concat($return, $tab4, $tab4, $tab)" />
		<xsd:element name="{@name}" minOccurs="0" maxOccurs="unbounded">
			<xsl:value-of select="concat($return, $tab4, $tab4, $tab, $tab)" />
			<xsd:annotation>
				<xsl:value-of select="concat($return, $tab4, $tab4, $tab, $tab, $tab)" />
				<xsd:documentation>
					<xsl:value-of select="description/text()" />
				</xsd:documentation>
			<xsl:value-of select="concat($return, $tab4, $tab4, $tab, $tab)" />
			</xsd:annotation>
			<xsl:value-of select="concat($return, $tab4, $tab4, $tab, $tab)" />
			<xsd:complexType>
				<xsl:if test="contains/contained">
					<xsl:value-of select="concat($return, $tab4, $tab4, $tab, $tab, $tab)" />
					<xsd:sequence>
						<xsl:for-each select="contains/contained">
							<xsl:variable name="contained_element" select="@element" />
							<xsl:apply-templates select="../../../element[@name=$contained_element]" mode="datasection">
								<xsl:with-param name="project_node" select="$project_node" />
								<xsl:with-param name="specsdir"     select="$specsdir" />
								<xsl:with-param name="api"          select="$api" />
							</xsl:apply-templates>
						</xsl:for-each>
					<xsl:value-of select="concat($return, $tab4, $tab4, $tab, $tab, $tab)" />
					</xsd:sequence>
				</xsl:if>
				<xsl:if test="contains/pcdata">
					<xsl:text disable-output-escaping="yes">
											&lt;xsd:simpleContent>
												&lt;xsd:extension base="xsd:string"></xsl:text>
				</xsl:if>
				<xsl:for-each select="attribute">
					<xsl:variable name="elementtype">
						<xsl:call-template name="elementtype">
							<xsl:with-param name="project_node" select="$project_node" />
							<xsl:with-param name="specsdir"     select="$specsdir" />
							<xsl:with-param name="api"          select="$api" />
							<xsl:with-param name="type"         select="@type" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="use">
						<xsl:choose>
							<xsl:when test="@required = 'true'">required</xsl:when>
							<xsl:otherwise>optional</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:variable name="attributename" select="@name" />

					<xsl:value-of select="concat($return, $tab4, $tab4, $tab4, $tab)" />
					<xsd:attribute name="{$attributename}" type="{$elementtype}" use="{$use}">
						<xsl:if test="@default and @type != '_date' and @type != '_timestamp'">
							<xsl:attribute name="default">
								<xsl:choose>
									<xsl:when test="@type != '_date'">
										<xsl:value-of select="concat(substring(@default,0,4), '-', substring(@default,4,2), '-', substring(@default,6,2))" />
									</xsl:when>
									<xsl:when test="@type != '_timestamp'">
										<xsl:value-of select="concat(substring(@default,0,4), '-', substring(@default,4,2), '-', substring(@default,6,2))" />
										<xsl:value-of select="concat('T', substring(@default,8,2), ':', substring(@default,10,2), ':', substring(@default,12,2))" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="@default" />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="concat($return, $tab4, $tab4, $tab4, $tab, $tab)" />
						<xsd:annotation>
							<xsl:value-of select="concat($return, $tab4, $tab4, $tab4, $tab, $tab, $tab)" />
							<xsd:documentation>
								<xsl:value-of select="description/text()" />
							</xsd:documentation>
						<xsl:value-of select="concat($return, $tab4, $tab4, $tab4, $tab, $tab)" />
						</xsd:annotation>
					<xsl:value-of select="concat($return, $tab4, $tab4, $tab4, $tab)" />
					</xsd:attribute>
				</xsl:for-each>
				<xsl:if test="contains/pcdata">
					<xsl:text disable-output-escaping="yes">
												&lt;/xsd:extension>
										&lt;/xsd:simpleContent></xsl:text>
				</xsl:if>
			<xsl:value-of select="concat($return, $tab4, $tab4, $tab, $tab)" />
			</xsd:complexType>
		<xsl:value-of select="concat($return, $tab4, $tab4, $tab)" />
		</xsd:element>
	</xsl:template>

	<xsl:template match="function" mode="messages">
		<xsl:variable name="functionname" select="@name" />

		<xsl:value-of select="concat($return, $tab)" />
		<message name="{$functionname}Input">
			<xsl:value-of select="concat($return, $tab, $tab)" />
			<part name="parameters" element="tns:{$functionname}Request" />
		<xsl:value-of select="concat($return, $tab)" />
		</message>
		<xsl:value-of select="concat($return, $tab)" />
		<message name="{$functionname}Output">
			<xsl:value-of select="concat($return, $tab, $tab)" />
			<part name="parameters" element="tns:{$functionname}Response" />
		<xsl:value-of select="concat($return, $tab)" />
		</message>
	</xsl:template>

	<xsl:template match="resultcode" mode="messages">
		<xsl:variable name="resultcodename">
			<xsl:choose>
				<xsl:when test="contains(@name, '/')">
					<xsl:value-of select="substring-after(@name, '/')" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@name" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:value-of select="concat($return, $tab)" />
		<message name="{$resultcodename}FaultMessage">
			<xsl:value-of select="concat($return, $tab, $tab)" />
			<part name="fault" element="tns:{$resultcodename}Fault" />
		<xsl:value-of select="concat($return, $tab)" />
		</message>
	</xsl:template>

	<xsl:template match="function" mode="porttypes">

		<xsl:variable name="functionname" select="@name" />
		<xsl:variable name="function_file" select="concat($specsdir, '/', @name, '.fnc')" />
		<xsl:variable name="function_node" select="document($function_file)/function" />

		<xsl:value-of select="concat($return, $tab, $tab)" />
		<operation name="{$functionname}">
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			<documentation>
				<xsl:value-of select="$function_node/description" />
			</documentation>
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			<input name="{$functionname}Input" message="tns:{$functionname}Input" />
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			<output name="{$functionname}Output" message="tns:{$functionname}Output" />
			<xsl:for-each select="$function_node/output/resultcode-ref">
				<xsl:variable name="rcd_file">
					<xsl:choose>
						<xsl:when test="contains(@name, '/')">
							<xsl:value-of select="concat($project_home, '/apis/', substring-before(@name, '/'), '/spec/', substring-after(@name, '/'), '.rcd')" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="concat($specsdir, '/', @name, '.rcd')" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="rcd_node" select="document($rcd_file)/resultcode" />
				<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
				<fault name="{$rcd_node/@name}" message="tns:{$rcd_node/@name}FaultMessage">
					<xsl:value-of select="concat($return, $tab4)" />
					<documentation>
						<xsl:value-of select="$rcd_node/description" />
					</documentation>
				<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
				</fault>
			</xsl:for-each>
		<xsl:value-of select="concat($return, $tab, $tab)" />
		</operation>
	</xsl:template>

	<xsl:template match="function" mode="bindings">

		<xsl:param name="location" />
		<xsl:param name="apiname" />

		<xsl:variable name="functionname" select="@name" />
		<xsl:variable name="function_file" select="concat($specsdir, '/', @name, '.fnc')" />
		<xsl:variable name="function_node" select="document($function_file)/function" />

		<xsl:value-of select="concat($return, $tab, $tab)" />
		<operation name="{$functionname}">
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			<documentation>
				<xsl:value-of select="$function_node/description" />
			</documentation>
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			<!--soapbind:operation soapAction="{$location}/{$functionname}" /-->
			<soapbind:operation soapAction="" />
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			<input name="{$functionname}Input">
				<xsl:value-of select="concat($return, $tab4)" />
				<soapbind:body use="literal" />
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			</input>
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			<output name="{$functionname}Output">
				<xsl:value-of select="concat($return, $tab4)" />
				<soapbind:body use="literal" />
			<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
			</output>
			<xsl:for-each select="$function_node/output/resultcode-ref">
				<xsl:variable name="rcd_file">
					<xsl:choose>
						<xsl:when test="contains(@name, '/')">
							<xsl:value-of select="concat($project_home, '/apis/', substring-before(@name, '/'), '/spec/', substring-after(@name, '/'), '.rcd')" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="concat($specsdir, '/', @name, '.rcd')" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="rcd_node" select="document($rcd_file)/resultcode" />
				<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
				<fault name="{$rcd_node/@name}">
					<xsl:value-of select="concat($return, $tab4)" />
					<documentation>
						<xsl:value-of select="$rcd_node/description" />
					</documentation>
					<xsl:value-of select="concat($return, $tab4)" />
					<soapbind:fault name="{$rcd_node/@name}" use="literal"/>
				<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
				</fault>
			</xsl:for-each>
		<xsl:value-of select="concat($return, $tab, $tab)" />
		</operation>
	</xsl:template>

	<xsl:template match="type" mode="types">

		<xsl:param name="project_node" />
		<xsl:param name="specsdir"     />
		<xsl:param name="api"          />

		<xsl:variable name="type_file">
			<xsl:call-template name="file_for_type">
				<xsl:with-param name="specsdir" select="$specsdir" />
				<xsl:with-param name="type" select="@name" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="type_node" select="document($type_file)/type" />
		<xsl:variable name="base_type">
			<xsl:choose>
				<xsl:when test="$type_node/pattern or $type_node/enum or $type_node/list or $type_node/set">
					<xsl:text>string</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<!-- It's the same as for the basic type -->
					<xsl:call-template name="soaptype_for_type">
						<xsl:with-param name="project_node" select="$project_node" />
						<xsl:with-param name="specsdir"     select="$specsdir" />
						<xsl:with-param name="api"          select="$api" />
						<xsl:with-param name="type"         select="concat('_', local-name($type_node/*[2]))" />
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="type_name">
			<xsl:choose>
				<xsl:when test="contains(@name, '/')">
					<xsl:value-of select="substring-after(@name, '/')" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@name" />
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>Type</xsl:text>
		</xsl:variable>

		<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
		<xsd:simpleType name="{$type_name}">
			<xsl:value-of select="concat($return, $tab4)" />
			<xsd:annotation>
				<xsl:value-of select="concat($return, $tab4, $tab)" />
				<xsd:documentation>
					<xsl:value-of select="$type_node/description/text()" />
				</xsd:documentation>
			<xsl:value-of select="concat($return, $tab4)" />
			</xsd:annotation>
			<xsl:value-of select="concat($return, $tab4)" />
			<xsd:restriction base="xsd:{$base_type}">
				<xsl:if test="$type_node/pattern">
					<xsl:variable name="pattern" select="$type_node/pattern/text()" />
					<xsl:value-of select="concat($return, $tab4, $tab)" />
					<xsd:pattern value="{$pattern}" />
				</xsl:if>
				<xsl:if test="$type_node/enum">
					<xsl:for-each select="$type_node/enum/item">
						<xsl:variable name="enumeration_value" select="@value" />
						<xsl:value-of select="concat($return, $tab4, $tab)" />
						<xsd:enumeration value="{$enumeration_value}" />
					</xsl:for-each>
				</xsl:if>
				<xsl:if test="$type_node/int8 or $type_node/int16 or $type_node/int32 or $type_node/int64 or $type_node/float32 or $type_node/float64">
					<xsl:if test="$type_node/*[2]/@min">
						<xsl:value-of select="concat($return, $tab4, $tab)" />
						<xsd:minInclusive value="{$type_node/*[2]/@min}" />
					</xsl:if>
					<xsl:if test="$type_node/*[2]/@max">
						<xsl:value-of select="concat($return, $tab4, $tab)" />
						<xsd:maxInclusive value="{$type_node/*[2]/@max}" />
					</xsl:if>
				</xsl:if>
				<xsl:if test="$type_node/base64">
					<xsl:if test="$type_node/base64/@min">
						<xsl:value-of select="concat($return, $tab4, $tab)" />
						<xsd:minLength value="{$type_node/base64/@min}" />
					</xsl:if>
					<xsl:if test="$type_node/base64/@max">
						<xsl:value-of select="concat($return, $tab4, $tab)" />
						<xsd:maxLength value="{$type_node/base64/@max}" />
					</xsl:if>
				</xsl:if>
			<xsl:value-of select="concat($return, $tab4)" />
			</xsd:restriction>
		<xsl:value-of select="concat($return, $tab, $tab, $tab)" />
		</xsd:simpleType>
	</xsl:template>

	<xsl:template name="elementtype">

		<xsl:param name="project_node" />
		<xsl:param name="specsdir"     />
		<xsl:param name="api"          />
		<xsl:param name="type"         />

		<xsl:choose>
			<xsl:when test="starts-with($type, '_') or string-length($type) = 0">
				<xsl:variable name="soaptype">
					<xsl:call-template name="soaptype_for_type">
						<xsl:with-param name="project_node" select="$project_node" />
						<xsl:with-param name="specsdir"     select="$specsdir" />
						<xsl:with-param name="api"          select="$api" />
						<xsl:with-param name="type"         select="@type" />
					</xsl:call-template>
				</xsl:variable>
				<xsl:text>xsd:</xsl:text>
				<xsl:value-of select="$soaptype" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>tns:</xsl:text>
				<xsl:choose>
					<xsl:when test="contains($type, '/')">
						<xsl:value-of select="substring-after($type, '/')" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$type" />
					</xsl:otherwise>
				</xsl:choose>
				<xsl:text>Type</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
