<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"  xmlns:saxon="http://saxon.sf.net/" 
  exclude-result-prefixes="xs math saxon"
  version="3.0">
  
  <xsl:mode name="burststream" streamable="yes" on-no-match="shallow-copy" use-accumulators="#all"/>
  <xsl:mode streamable="no" on-no-match="shallow-copy" use-accumulators="#all"/>
  
  <xsl:accumulator name="ExportHeader" initial-value="()" streamable="yes" as="element(ExportHeader)*">
    <xsl:accumulator-rule phase="end" saxon:capture="yes" match="ExportHeader" select="."/>
  </xsl:accumulator>
  
  <xsl:accumulator name="DocCount" initial-value="0" streamable="yes" as="xs:integer">
    <xsl:accumulator-rule match="TransformedDoc" select="$value + 1"/>
  </xsl:accumulator>  
  
  
  <xsl:template match="ExpCont" mode="#all">
    <xsl:message>starting second xslt2</xsl:message>
    <ExpCont>
      <xsl:apply-templates select="copy-of(TransformedDoc)" mode="#default"/>
    </ExpCont>
    <xsl:message select="concat('transformed Doc:', accumulator-after('DocCount'))"/>
  </xsl:template>
  
  <xsl:template match="TransformedDoc">
    <Doc>
      <xsl:attribute name="PrintDate" select="accumulator-before('ExportHeader')/PrintDate"/>
      <xsl:apply-templates select="*"/>
    </Doc>
    <xsl:message select="concat('done: Doc: ',accumulator-after('DocCount'))"/>
  </xsl:template>
</xsl:stylesheet>