<?xml version="1.0"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:svg="http://www.w3.org/2000/svg"
    version="1.0">
  <xsl:output method="text"/>
  
  <xsl:template match="svg:svg">
  	<xsl:value-of select="@width"/>
  	<xsl:text> </xsl:text>
  	<xsl:value-of select="@height"/>
  	<xsl:text>&#10;</xsl:text>
  </xsl:template>
  
  <!-- drop text elements from output -->  
  <xsl:template match="text()"/>
</xsl:stylesheet>
