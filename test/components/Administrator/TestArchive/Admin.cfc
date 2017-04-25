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
 ---><cfcomponent>
	<cffunction access="remote" name="invoke" output="false">
		<cfargument name="type" required="yes" type="string">
		<cfargument name="password" required="yes" type="string">
		<cfargument name="attributeCollection" required="yes" type="struct">
		<cfargument name="callerId" required="no" type="string" default="undefined">
		
		<cfset var result="">
		<cfset var id=getLuceeId()[arguments.type].id>
		<cfset var sec=getLuceeId()[arguments.type].securityKey>
		<cfif not listFind(arguments.callerId,id)>
			<cfadmin 
				type="#arguments.type#"
				password="#Decrypt(arguments.password,sec)#"
				attributeCollection="#arguments.attributeCollection#"
				providedCallerIds="#arguments.callerId#"
				returnVariable="result">
			<cfif isDefined('result')><cfreturn result></cfif>
		</cfif>
	</cffunction>
    
    <cffunction access="remote" name="plugin" output="false" returntype="struct">
		<cfargument name="type" required="yes" type="string">
		<cfargument name="password" required="yes" type="string">
		<cfargument name="plugin" required="yes" type="string">
		<cfargument name="action" required="yes" type="string">
		<cfargument name="urlCollection" required="no" type="struct" default="#{}#">
	
		<cfset var sec=getLuceeId()[arguments.type].securityKey>
		<cfset pw=Decrypt(arguments.password,sec,'cfmx_compat','hex')>
		
        <cfadmin 
            action="connect"
            type="#arguments.type#"
            password="#pw#">
            
        <cfadmin 
            action="getPluginDirectory"
            type="#arguments.type#"
            password="#pw#"
            returnVariable="local.pluginDir">	
		<cfset mappings['/lucee_plugin_directory/']=pluginDir>
        <cfapplication action="update" mappings="#mappings#" sessionmanagement="yes" 
	clientmanagement="no" 
	setclientcookies="yes" 
	setdomaincookies="no">
    
    	<cfset url.action="plugin">
    	<cfset url.plugin=arguments.plugin>
    	<cfset url.PluginAction=arguments.action>
        
        <cfloop collection="#urlCollection#" item="key">
        	<cfset url[key]=urlCollection[key]>
        </cfloop>
        
        
        <cfset request.disableFrame=true>
        <cfset request.setCFApplication=false>
        <cfset request.adminType=arguments.type>
        <cfset session["password"&arguments.type]=pw>
        <cfset request.return={}>
        <cfsilent>
        <cfinclude template="admin/#arguments.type#.cfm">
        </cfsilent>
        <cfreturn request.return>
	</cffunction>

</cfcomponent>