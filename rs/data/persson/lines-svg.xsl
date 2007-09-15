<?xml version="1.0"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:svg="http://www.w3.org/2000/svg"
    version="1.0">
  <xsl:output method="text"/>
  
  <!-- simple line -->
  <xsl:template match="svg:line">
  	<xsl:value-of select="@x1"/>
  	<xsl:text> </xsl:text>
  	<xsl:value-of select="@y1"/>
  	<xsl:text> </xsl:text>
  	<xsl:value-of select="@x2"/>
  	<xsl:text> </xsl:text>
  	<xsl:value-of select="@y2"/>
  	<xsl:text>&#10;</xsl:text>
  </xsl:template>
  
  <!-- composite line -->
  <xsl:template match="svg:polyline">
    <xsl:variable name="points" select="normalize-space(@points)"/>
    <xsl:call-template name="expand_poly">
      <xsl:with-param name="points" select="$points"/>
    </xsl:call-template>
  </xsl:template>
   
  <!-- expand a polyline into individual lines -->
  <xsl:template name="expand_poly">
    <xsl:param name="points"/>
    <xsl:choose>
      <!-- there must be at least two points in the list; these will be separated by a space -->
      <xsl:when test="contains($points, ' ')">
        <!-- head is the first point whereas tail is the remainder of the list. we need to do
             the same thing once more for the tail (split in into next and rest) to find the
             second point. there must be a second point (otherwise we won't be here), but not
             necessarily a third, which is why we need to figure out how much to remove for the
             rest of the tail -->
        <xsl:variable name="head" select="substring-before($points, ' ')"/>
        <xsl:variable name="tail" select="substring-after($points, ' ')"/>
        <xsl:variable name="rest" select="substring-after($tail, ' ')"/>
        <xsl:variable name="next" select="substring($tail, 1, string-length($tail)-string-length($rest))"/>

        <!-- first coordinate is everything before the comma, second coordinate is after -->        
        <xsl:value-of select="substring-before($head, ',')"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="substring-after($head, ',')"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="substring-before($next, ',')"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="substring-after($next, ',')"/>
        <xsl:text>&#10;</xsl:text>

        <!-- recursively process the remainder of the list (next iteration) -->
        <xsl:call-template name="expand_poly">
          <xsl:with-param name="points" select="$tail"/>
        </xsl:call-template>
        
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <!-- drop text elements from output -->  
  <xsl:template match="text()"/>
</xsl:stylesheet>
