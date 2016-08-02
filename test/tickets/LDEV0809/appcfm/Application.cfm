<cfif not isDefined('url.original')>
<cfapplication 
	name="cfm809" 
	sessionmanagement="Yes" 
	sessiontimeout="#createTimeSpan(0,0,3,0)#" 
	scriptprotect="all" 
	sessioncookie="#{httponly=false, timeout=createTimeSpan(0, 0, 0, 10), secure=true,domain=".domain.com"}#" 
	authcookie="#{timeout=createTimeSpan(0, 0, 0, 10)}#">

</cfif>