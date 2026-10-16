<cfparam name="url.scene" default="archive">

<cfswitch expression="#url.scene#">
	<cfcase value="archive">
		<cfinclude template="/hybridLib/hello.cfm">
	</cfcase>
	<cfcase value="fallback">
		<cfinclude template="/hybridLib/fallback.cfm">
	</cfcase>
</cfswitch>