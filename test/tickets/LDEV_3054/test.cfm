<cfset result = "">
<cfset nullSupport = "true,false">
<cfloop list="#nullSupport#" index="i">
	<cfapplication name="test" enableNullSupport="#i#">
	<cfscript>
		try {
			function test(){
			}
			t = test();
			result = listappend(result,isEmpty(t));
		}
		catch (any e) {
			result = listappend(result, e.message);
		}
	</cfscript>
</cfloop>
<cfoutput>#result#</cfoutput>