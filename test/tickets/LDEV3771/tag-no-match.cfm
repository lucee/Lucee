<cftry>
	<cftry>
		<cfthrow type="d" message="boom">
		<cfcatch type="a" | "b" | "c" name="ex">
			<cfoutput>wrong:#ex.type#</cfoutput>
		</cfcatch>
	</cftry>
	<cfcatch type="any" name="outer">
		<cfoutput>propagated:#outer.type#</cfoutput>
	</cfcatch>
</cftry>
