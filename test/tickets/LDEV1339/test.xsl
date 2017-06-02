<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 
<xsl:output method="html" doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN" /> 
<xsl:template match="/"> 
<html> 
<body> 
<table border="2" bgcolor="yellow"> 
<tr> 
<th>Names</th> 
<th>Price</th> 
</tr> 
<xsl:for-each select="breakfast_menu/food"> 
<tr> 
<td> 
<xsl:value-of select="names"/> 
</td> 
<td> 
<xsl:value-of select="price"/> 
</td> 
</tr> 
</xsl:for-each> 
<tr>
<td>5.1</td>
<td>2.1</td>
</tr>
</table> 
</body> 
</html> 
</xsl:template> 
</xsl:stylesheet>