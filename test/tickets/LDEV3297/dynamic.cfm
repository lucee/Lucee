<cfimport prefix="map" taglib="/ldev3297/">
<cftry>
	<cfif url.via eq "module">
		<cfmodule template="/ldev3297/Dynamic.cfm"></cfmodule>
	<cfelse>
		<map:Dynamic></map:Dynamic>
	</cfif>
	<cfcatch>MISSING</cfcatch>
</cftry>
