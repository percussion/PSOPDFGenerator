<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:svg="http://www.w3.org/2000/svg" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:variable name="related" select="/*/sys_AssemblerInfo/RelatedContent"/>
	<!--	<xsl:template match="div[@class='rxbodyfield']" mode="currentpage" xmlns:html="http://www.w3.org/TR/REC-html40">
		<xsl:for-each select="p">
			<fo:block font-size="12pt" font-family="sans-serif" line-height="15pt" space-after.optimum="15pt" text-align="left">
				<xsl:apply-templates select="." mode="rxbodyfield"/>
			</fo:block>
		</xsl:for-each>
	</xsl:template>
-->
	<xsl:template match="div[@class='rxbodyfield'] | html:div[@class='rxbodyfield']" mode="currentpage" xmlns:html="http://www.w3.org/TR/REC-html40">
		<xsl:apply-templates select=".//p | .//html:p | .//html:table | .//table" mode="currentpage"/>
		<!-- xsl:for-each select=".//p | .//html:p">
			
		</xsl:for-each -->
	</xsl:template>
	<xsl:template match="p" mode="currentpage">
		<fo:block font-size="12pt" font-family="sans-serif" line-height="15pt" space-after.optimum="15pt">
			<xsl:apply-templates select="." mode="rxbodyfield"/>
		</fo:block>
	</xsl:template>
	<xsl:template match="img" mode="rxbodyfield">
		<fo:block font-size="12pt" font-family="sans-serif" line-height="15pt" space-after.optimum="15pt">
			<fo:external-graphic>
				<xsl:attribute name="src"><xsl:value-of select="./@src"/></xsl:attribute>
			</fo:external-graphic>
		</fo:block>
	</xsl:template>
	<xsl:template match="table" mode="currentpage">
		<fo:table width="15cm" table-layout="fixed">
			<fo:table-column column-width="100pt" column-number="1"/>
			<fo:table-column column-width="100pt" column-number="2"/>
			<fo:table-column column-width="100pt" column-number="3"/>
			<fo:table-column column-width="100pt" column-number="4"/>
			<fo:table-column column-width="100pt" column-number="5"/>
			<fo:table-column column-width="100pt" column-number="6"/>
			<fo:table-column column-width="100pt" column-number="7"/>
			<fo:table-body>
				<xsl:for-each select="tr">
					<fo:table-row>
						<xsl:for-each select="td">
							<fo:table-cell border-color="black" border-style="solid" border-width="0.2mm">
								<fo:block>
									<xsl:value-of select="."/>
								</fo:block>
							</fo:table-cell>
						</xsl:for-each>
					</fo:table-row>
				</xsl:for-each>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	<xsl:template match="/" xml:space="default">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:svg="http://www.w3.org/2000/svg">
			<fo:layout-master-set>
				<!-- layout information -->
				<fo:simple-page-master master-name="simple" page-height="29.7cm" page-width="21cm" margin-top="0mm" margin-bottom="0cm" margin-left="0cm" margin-right="0cm">
					<fo:region-body margin-top="5mm" margin-bottom="2cm" margin-left="5mm" margin-right="5mm"/>
					<fo:region-before extent="3cm" background-color="#10264b" margin-top="4mm" margin-bottom="1mm" margin-left="1mm" margin-right="15mm"/>
					<fo:region-after extent="5mm" background-color="#10264b"/>
					<fo:region-start extent="5mm" background-color="#10264b"/>
					<fo:region-end extent="5mm" background-color="#10264b"/>
				</fo:simple-page-master>
			</fo:layout-master-set>
			<!-- end: defines page layout -->
			<!-- start page-sequence-->
			<fo:page-sequence initial-page-number="1" master-reference="simple">
				<fo:static-content flow-name="xsl-region-after">
					<fo:block text-align="end" font-size="10pt" font-family="sans-serif" line-height="14pt" color="white">
						<xsl:value-of select="/*/RXS_CT_SHARED/DISPLAYTITLE"/> - p.
               <fo:page-number/>
					</fo:block>
				</fo:static-content>
				<fo:static-content flow-name="xsl-region-start">
					<fo:block/>
				</fo:static-content>
				<fo:static-content flow-name="xsl-region-end">
					<fo:block/>
				</fo:static-content>
				<fo:static-content flow-name="xsl-region-before">
					<fo:block>
					</fo:block>
				</fo:static-content>
				<!-- start fo:flow -->
				<fo:flow flow-name="xsl-region-body">
					<!-- SET THE INTERNAL MARGINS SO THIS TEXT LOOKS CLEAR -->
					<fo:block margin-top="10mm" margin-bottom="10mm" margin-left="10mm" margin-right="10mm" text-align="left">
						<!-- this defines a title -->
						<fo:block font-size="24pt" font-family="sans-serif" font-style="italic" line-height="24pt" space-after.optimum="50pt" color="white" text-align="right">
							<xsl:value-of select="/*/RXS_CT_SHARED/DISPLAYTITLE"/>
						</fo:block>
						<!-- this defines the Abstract Area -->
						<fo:block margin-top="20mm" font-size="14pt" font-family="sans-serif" line-height="15pt" space-after.minimum="20pt">
							<fo:inline text-decoration="underline" font-weight="bold">Summary3</fo:inline>
						</fo:block>
						<fo:block font-size="12pt" font-family="sans-serif" line-height="15pt" space-after.optimum="50pt" text-align="justify">
							<xsl:value-of select="/*/RXS_CT_SHARED/DESCRIPTION"/>
						</fo:block>
						<!-- this defines the Body Area -->
						<fo:block font-size="14pt" font-family="sans-serif" line-height="15pt" space-after.optimum="20pt">
							<fo:inline text-decoration="underline" font-weight="bold">Description</fo:inline>
						</fo:block>
						<fo:block font-size="12pt" font-family="sans-serif" line-height="15pt" space-after.optimum="30pt" text-align="justify">
							<xsl:apply-templates select="/*/RXS_CT_SHARED/BODY/*" mode="currentpage"/>
						</fo:block>
					</fo:block>
				</fo:flow>
				<!-- closes the flow element-->
			</fo:page-sequence>
			<!-- closes the page-sequence -->
		</fo:root>
	</xsl:template>
</xsl:stylesheet>
