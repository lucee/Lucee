<cfoutput>
<cfset passwordPdf = "lucee-test">
<cfdocument filename="testLucee.pdf" userpassword="#passwordPdf#" encryption="128-bit" format="pdf" overwrite="yes">
	Lucee PDF
</cfdocument>
<cfset temp=structnew()>
<cfset strDocumentWithPath = "testLucee.pdf">

<cfset temp.pwFiacWord="#passwordPdf#">
<cfset temp.outfile = replacenocase(strDocumentWithPath,".pdf","down.pdf")>

<cfsavecontent variable="temp.myddx">
	<?xml version="1.0" encoding="UTF-8"?>
	<DDX xmlns="http://ns.adobe.com/DDX/1.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://ns.adobe.com/DDX/1.0/ coldfusion_ddx.xsd">
	<PDF result="OUTpdf">
	<PDF source="INpdf" access="fiacPDF"/></PDF>
	<PasswordAccessProfile name="fiacPDF">
	<Password>#temp.pwFiacWord#</Password>
	</PasswordAccessProfile>
	</DDX>
</cfsavecontent>

<cfset temp.myddx = trim(temp.myddx)>

<cfset temp.inputStruct = {INpdf="#strDocumentWithPath#"}>
<cfset temp.outputStruct = {OUTpdf="#temp.outfile#"}>
<cfpdf action="processddx" ddxfile="#temp.myddx#" inputfiles="#temp.inputStruct#" outputfiles="#temp.outputStruct#" name="temp.ddxVar">

#temp.ddxVar.OUTPDF#
</cfoutput>