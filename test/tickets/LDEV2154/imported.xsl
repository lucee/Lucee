<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:import href="sub/included.xsl"/>
	<xsl:output method="text"/>
	<xsl:template match="/">
		<xsl:call-template name="greeting"/>
	</xsl:template>
</xsl:stylesheet>
