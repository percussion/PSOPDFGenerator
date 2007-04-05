<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:saxon="http://icl.com/saxon/fop" extension-element-prefixes="saxon">
<xsl:output method="saxon:fop" indent="yes"/>
	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<!-- layout information -->
				<fo:simple-page-master page-master-name="simple" page-height="29.7cm" page-width="21cm" margin-top="1cm" margin-bottom="2cm" margin-left="2.5cm" margin-right="2.5cm">
					<fo:region-body margin-top="3cm"/>
					<fo:region-before extent="3cm"/>
					<fo:region-after extent="1.5cm"/>
				</fo:simple-page-master>
			</fo:layout-master-set>
			<!-- end: defines page layout -->
			<!-- start page-sequence-->
			<fo:page-sequence master-reference="simple" initial-page-number="1">
				<fo:static-content flow-name="xsl-region-after">
					<fo:block text-align="end" font-size="10pt" font-family="serif" line-height="14pt">
						<xsl:value-of select="RXS_CT_SHAREDSet/RXS_CT_SHARED/DISPLAYTITLE"/> - p.
					<fo:page-number/>
					</fo:block>
				</fo:static-content>

				<!-- start fo:flow -->
				<fo:flow flow-name="xsl-region-body">
					<!-- this defines a title -->
					<fo:block font-size="18pt" font-family="sans-serif" line-height="24pt" space-after.optimum="15pt" background-color="blue" color="white" text-align="center" padding-top="3pt">
						<xsl:value-of select="RXS_CT_SHAREDSet/RXS_CT_SHARED/DISPLAYTITLE"/>
					</fo:block>
					<!-- this defines normal text -->
					<fo:block text-indent="5mm"
					 font-size="12pt" font-family="sans-serif" line-height="15pt" space-after.optimum="15pt" text-align="justify">
						<xsl:apply-templates select="RXS_CT_SHAREDSet/RXS_CT_SHARED/CALLOUT" mode="format-bold"/>
						<!-- <xsl:value-of select="RXS_CT_SHAREDSet/RXS_CT_SHARED/CALLOUT"/> -->
					</fo:block>
					<!-- this defines normal text -->
					<fo:block text-indent="5mm" font-size="12pt" font-family="sans-serif" line-height="15pt" space-after.optimum="3pt" text-align="justify">
						<xsl:apply-templates select="RXS_CT_SHAREDSet/RXS_CT_SHARED/BODY" mode="format-paragraph"/>
						<!-- <xsl:value-of select="RXS_CT_SHAREDSet/RXS_CT_SHARED/BODY"/> -->
					</fo:block>
				</fo:flow>
				<!-- closes the flow element-->
			</fo:page-sequence>
			<!-- closes the page-sequence -->
		</fo:root>
	</xsl:template>
	<xsl:template match="p" mode="format-paragraph">
    	<fo:block
        	text-indent="1em"
        	font-family="sans-serif" font-size="12pt"
        	space-before.minimum="2pt"
        	space-before.maximum="6pt"
        	space-before.optimum="4pt"
        	space-after.minimum="2pt"
        	space-after.maximum="6pt"
        	space-after.optimum="4pt">
    	<xsl:apply-templates mode="format-italic"/>
    	</fo:block>
	</xsl:template>
	
	<xsl:template match="b" mode="format-bold">
    		<fo:inline font-weight="bold"><xsl:apply-templates/></fo:inline>
		</xsl:template>

		<!-- <xsl:template match="u" mode="format">
    		<fo:inline text-decoration="underline"><xsl:apply-templates/></fo:inline>
		</xsl:template> -->

		<xsl:template match="i" mode="format-italic">
    		<fo:inline font-style="italic" color="blue"><xsl:apply-templates mode="format-bold"/></fo:inline>	
		</xsl:template>
</xsl:stylesheet>
