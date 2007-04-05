<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
     <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    
     <xsl:template match="/">
		<xsl:text disable-output-escaping="yes">&lt;!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN"
"http://www.springframework.org/dtd/spring-beans.dtd"&gt;</xsl:text>
        	<xsl:apply-templates select="node()" mode="copy"/>
     </xsl:template>
     
    <xsl:template match="beans" mode="copy">
        <xsl:copy>
        <xsl:apply-templates select="@*" mode="copy"/>
        <xsl:if test="not(//import[@resource='dispatch/fop-beans.xml'])">
            <import resource="dispatch/fop-beans.xml"/>
        </xsl:if>
        <xsl:copy-of select="node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="node()" mode="copy">
       <xsl:copy>       
           <xsl:apply-templates select="node()" />
       </xsl:copy>   
    </xsl:template>
     
     <xsl:template match="@*" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="copy"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
