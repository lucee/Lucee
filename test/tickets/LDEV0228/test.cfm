<cfparam name="FORM.Scene" default="1">
<cfif FORM.Scene EQ 1>
	<cfxml variable="xTest">
	<Some>
		<Sec>
			<Comments template="textarea" required="true" text="My Comments" bold="true"></Comments>
		</Sec>
	</Some>
	</cfxml>

	<cfsavecontent variable="sxsltTest"><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

		<xsl:include href="templates.xsl" />

		<xsl:template match="Some">
			<xsl:for-each select="Sec/*">
				<xsl:call-template name="createElement">
					<xsl:with-param name="node" select="." />
				</xsl:call-template>
			</xsl:for-each>
		</xsl:template>
	</xsl:stylesheet></cfsavecontent>
<cfelse>
	<cfxml variable="xTest">
	<Some>
		<Sec>
			<Comments template="textarea" required="true" text="My Comments" bold="true"></Comments>
		</Sec>
	</Some>
	</cfxml>

	<cfsavecontent variable="sxsltTest"><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

		<xsl:template name="createElement">
			<xsl:param name="node" />
			<xsl:param name="rows" select="'5'" />
			<xsl:param name="cols" select="'80'" />
			<xsl:choose>
				<xsl:when test="$node/@template='textarea'">
					<xsl:element name="label">
						<xsl:attribute name="for"><xsl:value-of select="name($node)" /></xsl:attribute>
						<xsl:attribute name="style">
							<xsl:choose>
								<xsl:when test="$node/@bold='true'">
									<xsl:value-of select="'font-weight:bold;'" />
								</xsl:when>
							</xsl:choose>
						</xsl:attribute>
						<xsl:value-of select="$node/@text" />
					</xsl:element>
					<xsl:element name="textarea"><xsl:attribute name="name"><xsl:value-of select="name($node)" /></xsl:attribute><xsl:attribute name="id"><xsl:value-of select="name($node)" /></xsl:attribute><xsl:if test="$node/@required='true'"><xsl:attribute name="data-rule-required"><xsl:value-of select="$node/@required" /></xsl:attribute></xsl:if><xsl:if test="$node/@maxlength!=''"><xsl:attribute name="data-rule-maxlength"><xsl:value-of select="$node/@maxlength" /></xsl:attribute></xsl:if><xsl:attribute name="rows"><xsl:value-of select="$rows" /></xsl:attribute><xsl:attribute name="cols"><xsl:value-of select="$cols" /></xsl:attribute><xsl:value-of select="$node" disable-output-escaping="yes" />Test</xsl:element>
				</xsl:when>

			</xsl:choose>
		</xsl:template>

		<xsl:template match="Some">
			<xsl:for-each select="Sec/*">
				<xsl:call-template name="createElement">
					<xsl:with-param name="node" select="." />
				</xsl:call-template>
			</xsl:for-each>
		</xsl:template>
	</xsl:stylesheet></cfsavecontent>
</cfif>
<cfset xslt = XMLTransform( xTest, sxsltTest )>
