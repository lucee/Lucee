<cfparam name="url.scene" default="physical">

<cfswitch expression="#url.scene#">
	<cfcase value="physical">
		<cfinclude template="/hybridPFLib/hello.cfm">
	</cfcase>
	<cfcase value="fallback">
		<cfinclude template="/hybridPFLib/archiveOnly.cfm">
	</cfcase>
</cfswitch>