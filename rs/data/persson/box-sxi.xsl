<?xml version="1.0"?>
<xsl:stylesheet
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
   xmlns:style="http://openoffice.org/2000/style"
   xmlns:fo="http://www.w3.org/1999/XSL/Format"
   version="1.0">
  <xsl:output method="text"/>
  <!-- find the name of the master page layout (default is PM1) -->
  <xsl:variable name="master">
    <xsl:value-of select="//style:master-page[@style:name='Default']/@style:page-master-name"/>
  </xsl:variable>
  <!-- get parameters from the master page layout -->
  <xsl:template match="style:page-master">
    <xsl:if test="@style:name = $master">
      <xsl:value-of select="substring-before(style:properties/@fo:page-width, 'cm')"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="substring-before(style:properties/@fo:page-height, 'cm')"/>
      <xsl:text> </xsl:text>
    </xsl:if>
  </xsl:template>
  <!-- drop text elements from output -->
  <xsl:template match="text()"/>
</xsl:stylesheet>
