<!--- 
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><cfcomponent hint="Plugin">
	<cffunction name="load" output="no" returntype="any"
		hint="load persistent data from admin">
		<cfset var data=struct()>
		<cftry>
			<cfadmin 
				action="storageGet"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				key="#url.plugin#"
				returnVariable="data">
			<cfcatch>
				<cfset data="">
			</cfcatch>
		</cftry>
		<cfreturn data>
	</cffunction>
	
	<cffunction name="save" returntype="void"
		hint="save persistent data from admin">
		<cfargument name="data" type="any">
		<cfadmin 
			action="storageSet"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			key="#url.plugin#"
			value="#data#">
	</cffunction>
	
	<cffunction name="init"
		hint="this function will be called to initialize">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
	</cffunction>
	
	<cffunction name="overview" output="no"
		hint="this is the main display action">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
		<cfargument name="req" type="struct">
	</cffunction>
	
	
<cffunction name="action" output="false">
	<cfargument name="action" type="string" required="yes">
	<cfargument name="qs" type="string" required="no" default="">
	<cfreturn request.self&"?action="&url.action&"&plugin="&url.plugin&"&pluginAction="&arguments.action&"&"&arguments.qs>
</cffunction>
	
	<cffunction name="_action">
		<cfargument name="action" type="string">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
		<cfargument name="req" type="struct">
		<cfset var lang=lang>
		<cfset var app=app>
		<cfset var req=req>
		<cfreturn this[arguments.action](arguments.lang,arguments.app,arguments.req)>
	</cffunction>
	
	<cffunction name="_display">
		<cfargument name="template" type="string">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
		<cfargument name="req" type="struct">
		<cfinclude template="#arguments.template#">
	</cffunction>
	
</cfcomponent>