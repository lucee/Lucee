<cfscript>
	 try {
		```
		<cfset CountVar = 1>
		<cfloop condition = "CountVar LESS THAN OR EQUAL TO 1">
			<cfset CountVar = CountVar + 1>
			<cfoutput>#CountVar#</cfoutput>
		</cfloop>
		```
	} catch (any e) {
		writeOutput(e.message);
	}
</cfscript>