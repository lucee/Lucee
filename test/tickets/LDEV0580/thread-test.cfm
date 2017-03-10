<cfoutput>
	<!--- <cfdump var="#Session.CFID#" /></br> --->
	<cfthread name="threadput" action="run">
		<cfset thread.session_CFID = Session.CFID>
	</cfthread>
	<cfthread name="threadput" action="join" />
	<!--- <cfdump var="#CFThread["threadput"].session_CFID#" /> --->
	#compare(Session.CFID, CFThread["threadput"].session_CFID)#
</cfoutput>
