<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<!-- simple output that can be captured in an environment variable and passed as parameter -->
	<xsl:output method="text"/>

	<!-- don't let the formatting of the input file propagate to the output -->
	<xsl:strip-space elements="classpath"/>

	<!-- set this variable to the path that should be prepended to all entries; include slash! -->
	<xsl:param name="root"/>

	<!-- put each item that is defined by this set to the output -->
	<xsl:variable name="nodeset" select="/classpath/classpathentry[@kind='lib' or @kind='output']"/>

	<!-- main driver; this matches the entire input file once -->
	<xsl:template match="/">

		<!-- use a regular loop so that position works as expected -->
		<xsl:for-each select="$nodeset">

			<!-- don't include a comma before the first item -->
			<xsl:if test="position()!=1">:</xsl:if>

			<!-- prepend a root path if necessary -->
			<xsl:value-of select="$root"/>

			<!-- output itself -->
			<xsl:value-of select="@path"/>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
