<cfsetting enablecfoutputonly="true">
<cfset refreshT1StateInterval=100>

<!--- A thread running long enough (t1ProcessingDuration) to be joined by T2 --->
<cfset t1ProcessingDuration=2000>
<cfthread name="T1" action="run">
	<cfset sleep(t1ProcessingDuration)>
</cfthread>

<!--- A second thread doing some processing (t2ProcessingBeforeJoin) before joining with T1 --->
<cfset t2ProcessingBeforeJoin=500>
<cfset t2JoinDuration=t1ProcessingDuration+500>
<cfthread name="T2" action="run">
	<cftry>
		<!--- One can remove this sleep to check thread status only for action="join" --->
		<!--- Status of thread T1 should be "RUNNING" --->
		<cfset thread.T1Status1 = T1.Status>
		<cfset thread.T2Status1 = thread.Status>
		<cfif t2ProcessingBeforeJoin GT 0>
			<cfset sleep(t2ProcessingBeforeJoin)>
		</cfif>
		<!--- Status of thread T1 should be "WAITING" --->
		<cfset thread.T1Status2 = T1.Status>
		<cfset thread.T2Status2 = thread.Status>
		<!--- Making thread T2 to wait for T1 to complete --->
		<cfthread action="join" name="T1" timeout="#t2JoinDuration#"/>
		<!--- Status of thread T1 should be "Completed" --->
		<cfset thread.T1Status3 = T1.Status>
		<cfset thread.T2Status3 = thread.Status>
		<cflog text="#T2.T1Status1#|#T2.T1Status2#|#T2.T1Status3#">
		<cfcatch type="Any">
			<cflog type="error" text=">>> T2 error: #cfcatch.message#">
		</cfcatch>
	</cftry>
</cfthread>
<!--- waiting for T2 to complete to avoid errors --->
<cfthread action="join" name="T2"/>
<cftry>
	<cfoutput>#T2.T1Status1#|#T2.T1Status2#|#T2.T1Status3#|#T2.T2Status2#</cfoutput>
	<cfcatch type="any">
		<cfoutput>#cfcatch.Message#</cfoutput>
	</cfcatch>
</cftry>
