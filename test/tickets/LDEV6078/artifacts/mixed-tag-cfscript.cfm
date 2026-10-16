<!--- Edge case 3: mixed tag and cfscript --->
<cfset greeting = "Hello">
<cfscript>
echo( greeting );
</cfscript>
<cfoutput>#greeting#</cfoutput>
