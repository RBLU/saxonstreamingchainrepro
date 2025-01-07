<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  exclude-result-prefixes="xs math"
  version="3.0">
  
  <xsl:mode name="burststream" streamable="yes" on-no-match="shallow-copy" />
  <xsl:mode streamable="no" on-no-match="shallow-copy" />
  
  <xsl:template match="ExportContent" mode="#all">
    <xsl:message>staring  trafo xslt1</xsl:message>
    <ExpCont>
      <xsl:apply-templates select="copy-of(Document)" mode="#default"/>
    </ExpCont>
  </xsl:template>
  
  <xsl:template match="Document">
    <TransformedDoc>
      <xsl:apply-templates select="*"/>
    </TransformedDoc>
  </xsl:template>
</xsl:stylesheet>