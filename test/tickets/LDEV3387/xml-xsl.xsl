<?xml version="1.0" encoding="UTF-8"?>
<html xsl:version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <body>
        <xsl:for-each select="notes/note">
            From: <xsl:value-of select="from"/>
            To: <xsl:value-of select="to"/>
        </xsl:for-each>
    </body>
</html>