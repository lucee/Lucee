<cfparam name="session.debugFilter.path" default="">
<cfparam name="session.debugFilter.starttime" default="">
<cfparam name="session.debugFilter.query" default="">
<cfparam name="session.debugFilter.app" default="">
<cfparam name="session.debugFilter.total" default="">


    

<cfset stText.debug.path="Path">
<cfset stText.debug.reqTime="Request Time">
<cfset stText.debug.exeTime="Execution Timespan (ms)">
<cfset stText.debug.exeTimeQuery="Query">
<cfset stText.debug.exeTimeTotal="Total">
<cfset stText.debug.exeTimeApp="App">
<cfset stText.debug.maxLogs="Maximum Logged Requests">
<cfset stText.debug.minExeTime="Minimal Execution Time (ms)">
<cfset stText.debug.minExeTimeDesc="Minimal Execution Time that Lucee outputs the debugger information of a request.">
<cfset stText.debug.pathRestriction="Path Restriction">
<cfset stText.debug.pathRestrictionDesc="Path that should not be outputted, sperated path by line break.">

<cfset stText.debug.settingTitle="Settings">
<cfset stText.debug.settingDesc="Define how Lucee Log the debugging information.">

<cfset stText.debug.outputTitle="Output">
<cfset stText.debug.outputDesc="Debugging information logged by Lucee.">



<cfset stText.debug.filter="Filter">
<cfset stText.debug.filterTitle="Output Filters">
<cfset stText.debug.filterPath="Only Outputs records where the path match the following pattern.">


<cfset stText.debug.detailTitle="Detail">
<cfset stText.debug.detailDesc="Detailed information about a single Request">

<cffunction name="doFilter" returntype="string" output="false">
	<cfargument name="filter" required="yes" type="string">
	<cfargument name="value" required="yes" type="string">
	<cfargument name="exact" required="no" type="boolean" default="false">
	
	<cfset arguments.filter=replace(arguments.filter,'*','',"all")>
    <cfset filter=trim(filter)>
	<cfif not len(filter)>
		<cfreturn true>
	</cfif>
	<cfif exact>
		<cfreturn filter EQ value>
	<cfelse>
		<cfreturn FindNoCase(filter,value)>
	</cfif>
</cffunction>


<cffunction name="doFilterMin" returntype="string" output="false">
	<cfargument name="filter" required="yes" type="string">
	<cfargument name="value" required="yes" type="string">
	
    <cfset filter=trim(filter)>
	<cfif not isNumeric(filter) or filter LTE 0>
		<cfreturn true>
	</cfif>
	<cfreturn filter*1000000 LTE value>
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
    
    <cfif time GTE 100000000><!--- 1000ms --->
    	<cfreturn int(time/1000000)&" ms">
    <cfelseif time GTE 10000000><!--- 100ms --->
    	<cfreturn (int(time/100000)/10)&" ms">
    <cfelseif time GTE 1000000><!--- 10ms --->
    	<cfreturn (int(time/10000)/100)&" ms">
    <cfelse><!--- 0ms --->
    	<cfreturn (int(time/1000)/1000)&" ms">
    </cfif>
    
    
    <cfreturn (time/1000000)&" ms">
</cffunction> 
    


<cfparam name="url.action2" default="list">

<cfif url.action2 EQ "list">
	<cfinclude template="debugging.logs.list.cfm">
<cfelse>
	<cfinclude template="debugging.logs.detail.cfm">
</cfif>
