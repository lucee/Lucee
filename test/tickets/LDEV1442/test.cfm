<cfthread name="myThread" action="run">
   <cfset myThread.foo="bar">
</cfthread>
<cfthread name="myThread" action="join" />

<cfoutput>#myThread.foo# </cfoutput>
