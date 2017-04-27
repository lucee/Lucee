<cfparam name="form.scene" default="1">

<cfif form.scene EQ 1>
	<cfset result = isDDX( "test.pdf" )>
<cfelseif form.scene EQ 2>
	<cfset result = isPdfFile( "test.pdf" )>
<cfelseif form.scene EQ 3>
	<cfset result = ajaxLink( 'http://www.google.com' )>
<cfelseif form.scene EQ 4>
	<cfscript>
		function test(){
			result = "true";
		}
	</cfscript>
	<cfset ajaxOnLoad( test() )>
<cfelseif form.scene EQ 5>
	<cfdiv bind="url:verifyClient.cfm">
	<!--- Error Occurs on AJAX binding loader --->
	<cfset result = true>
<cfelseif form.scene EQ 6>
	<cfset result=DotNetToCFType('true') />
<cfelseif form.scene EQ 7>
	<cfset printerList = listToArray(getPrinterList())>
	<cfset result = isStruct(getPrinterInfo(printerList[1]))>
</cfif>

<cfoutput>
	<cfif form.scene NEQ 5>
		#result#
	</cfif>
</cfoutput>