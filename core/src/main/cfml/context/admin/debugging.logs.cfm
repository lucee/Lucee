<cfparam name="session.debugFilter.path" default="">
<cfparam name="session.debugFilter.starttime" default="">
<cfparam name="session.debugFilter.query" default="">
<cfparam name="session.debugFilter.app" default="">
<cfparam name="session.debugFilter.total" default="">
<cfparam name="session.debugFilter.scope" default="">


<cffunction name="doFilter" returntype="string" output="false">
	<cfargument name="filter" required="yes" type="string">
	<cfargument name="value" required="yes" type="string">
	<cfargument name="exact" required="no" type="boolean" default="false">
	
	<cfset arguments.filter=replace(arguments.filter,'*','',"all")>
    <cfset arguments.filter=trim(arguments.filter)>
	<cfif not len(arguments.filter)>
		<cfreturn true>
	</cfif>
	<cfif exact>
		<cfreturn arguments.filter EQ arguments.value>
	<cfelse>
		<cfreturn FindNoCase(arguments.filter, arguments.value)>
	</cfif>
</cffunction>


<cffunction name="doFilterMin" returntype="string" output="false">
	<cfargument name="filter" required="yes" type="string">
	<cfargument name="value" required="yes" type="string">
	
    <cfset arguments.filter=trim(arguments.filter)>
	<cfif not isNumeric(arguments.filter) or arguments.filter LTE 0>
		<cfreturn true>
	</cfif>
	<cfreturn arguments.filter*1000000 LTE arguments.value>
</cffunction>

<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfset isWeb=request.admintype EQ "web">

<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access"
	secType="debugging">
    
<cfif isWeb>
	<cfadmin 
		action="getLoggedDebugData"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="logs">
	<cfadmin 
		action="getDebugEntry"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="entries">
</cfif>    
<cfadmin 
	action="getDebugSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="setting">
    

<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Update#">
			<cfadmin 
				action="updateDebugSetting"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
                maxLogs="#form.maxLogs#"
				remoteClients="#request.getRemoteClients()#">
			
		</cfcase>
	<!--- reset to server setting --->
		<cfcase value="#stText.Buttons.resetServerAdmin#">
			<cfadmin 
				action="updateDebugSetting"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
                maxLogs=""
				remoteClients="#request.getRemoteClients()#">
			
		</cfcase>
	<!--- set filter --->
		<cfcase value="#stText.Debug.filter#">
        	<cfset session.debugFilter.path=form.path>
            <cftry>
            		<cfset session.debugFilter.starttime=ParseDateTime(form.starttime)>
                    <cfcatch>
                     	<cftry>
								<cfset session.debugFilter.starttime=lsParseDateTime(form.starttime)>
                                <cfcatch>
                                	<cfset session.debugFilter.starttime="">
                                </cfcatch>
                        </cftry>
                    </cfcatch>
            </cftry>
            <cfif isNumeric(trim(form.query))><cfset session.debugFilter.query=form.query><cfelse><cfset session.debugFilter.query=""></cfif>
            <cfif isNumeric(trim(form.app))><cfset session.debugFilter.app=form.app><cfelse><cfset session.debugFilter.app=""></cfif>
            <cfif isNumeric(trim(form.total))><cfset session.debugFilter.total=form.total><cfelse><cfset session.debugFilter.total=""></cfif>
            <cfif isNumeric(trim(form.scope))><cfset session.debugFilter.scope=form.scope><cfelse><cfset session.debugFilter.scope=""></cfif>
		</cfcase>
        
        #stText.Debug.filter#
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>
<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<cffunction name="formatUnit" output="no" returntype="string">
	<cfargument name="time" type="numeric" required="yes">
    
    <cfif arguments.time GTE 100000000><!--- 1000ms --->
    	<cfreturn int(arguments.time/1000000)&" ms">
    <cfelseif arguments.time GTE 10000000><!--- 100ms --->
    	<cfreturn (int(arguments.time/100000)/10)&" ms">
    <cfelseif arguments.time GTE 1000000><!--- 10ms --->
    	<cfreturn (int(arguments.time/10000)/100)&" ms">
    <cfelse><!--- 0ms --->
    	<cfreturn (int(arguments.time/1000)/1000)&" ms">
    </cfif>
    
    
    <cfreturn (arguments.time/1000000)&" ms">
</cffunction> 
    


<cfparam name="url.action2" default="list">

<cfif url.action2 EQ "list">
	<cfinclude template="debugging.logs.list.cfm">
<cfelse>
	<cfinclude template="debugging.logs.detail.cfm">
</cfif>
