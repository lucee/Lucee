<cfset passwordPdf = "lucee-test">
<cfset testPDF = getTempFile(getTempDirectory(), "LDEV1850", "pdf")>

<cfdocument filename="#testPDF#" userpassword="#passwordPdf#" encryption="128-bit" format="pdf" overwrite="yes">
	Lucee PDF
</cfdocument>
<cfset temp=structnew()>
<cfset outputFile = getTempFile(getTempDirectory(), "LDEV1850-output", "pdf")>

<cfset temp.pwFiacWord="#passwordPdf#">
<cfset temp.outfile = outputFile>

<cfsavecontent variable="temp.ddx">
	<?xml version="1.0" encoding="UTF-8"?>
	<DDX xmlns="http://ns.adobe.com/DDX/1.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://ns.adobe.com/DDX/1.0/ coldfusion_ddx.xsd">
	<PDF result="OUTpdf">
	<PDF source="INpdf" access="fiacPDF"/></PDF>
	<PasswordAccessProfile name="fiacPDF">
	<Password>#temp.pwFiacWord#</Password>
	</PasswordAccessProfile>
	</DDX>
</cfsavecontent>

<cfset temp.ddx = trim(temp.ddx)>

<cfset temp.inputStruct = {INpdf="#testPDF#"}>
<cfset temp.outputStruct = {OUTpdf="#temp.outfile#"}>
<cfpdf action="processddx" ddxfile="#temp.ddx#" inputfiles="#temp.inputStruct#" outputfiles="#temp.outputStruct#" name="temp.ddxResult">

<cfoutput>
#temp.ddxResult.OUTPDF#
</cfoutput>