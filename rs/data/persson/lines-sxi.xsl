<?xml version="1.0"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:svg="http://www.w3.org/2000/svg"
	xmlns:draw="http://openoffice.org/2000/drawing"
	version="1.0">
	<xsl:output method="text"/>
	<xsl:template match="draw:line">
		      <xsl:value-of select="substring-before(@svg:x1,'cm')"/>
		      <xsl:text> </xsl:text>
		      <xsl:value-of select="substring-before(@svg:y1,'cm')"/>
		      <xsl:text> </xsl:text>
		      <xsl:value-of select="substring-before(@svg:x2,'cm')"/>
		      <xsl:text> </xsl:text>
		      <xsl:value-of select="substring-before(@svg:y2,'cm')"/>
		      <xsl:text>&#xa;</xsl:text>
        </xsl:template>
</xsl:stylesheet>
