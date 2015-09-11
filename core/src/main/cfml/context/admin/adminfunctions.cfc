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
 ---><cfcomponent output="no">

	<cfset variables._dataCache = {} />
	
	
	<cffunction name="getfavorites" returntype="struct" output="no">
		<cfreturn getdata('favorites', {}) />
	</cffunction>
	
	
	<cffunction name="isfavorite" returntype="boolean" output="no">
		<cfargument name="action" type="string" required="yes" />
		<cfreturn structKeyExists(getfavorites(), arguments.action) />
	</cffunction>
	
	
	<cffunction name="addfavorite" returntype="void" output="no">
		<cfargument name="action" type="string" required="yes" />
		<cfset var data = getfavorites() />
		<cfset data[arguments.action] = "" />
		<cfset setdata('favorites', data) />
	</cffunction>
	
	
	<cffunction name="removefavorite" returntype="void" output="no">
		<cfargument name="action" type="string" required="yes" />
		<cfset var data = getfavorites() />
		<cfset structDelete(data, arguments.action, false) />
		<cfset setdata('favorites', data) />
	</cffunction>
	
	
	<cffunction name="getdata" returntype="any" output="no">
		<cfargument name="key" type="string" required="yes" />
		<cfargument name="defaultvalue" type="any" required="no" default="" />
		<cfset var data = loadData() />
		<cfif structKeyExists(data, arguments.key)>
			<cfreturn data[arguments.key] />
		</cfif>
		<cfreturn arguments.defaultvalue />
	</cffunction>
	
	
	<cffunction name="setdata" returntype="void" output="no">
		<cfargument name="key" type="string" required="yes" />
		<cfargument name="value" type="any" required="yes" />
		<cflock name="setdata_admin" timeout="1" throwontimeout="no">
			<cfset var data = loadData() />
			<cfset data[arguments.key] = arguments.value />
			<cfset writeData() />
		</cflock>
	</cffunction>

	<cffunction name="loadData" access="private" output="no" returntype="any">
		<cfset var dataKey = getDataStoreName() />
		<cfif not structKeyExists(variables._dataCache, dataKey)>
			<cfset var data = {} />
			<cfset var dataFile = getDataFilePath() />
			<cfif fileExists(dataFile)>
				<cfset data = evaluate(fileRead(dataFile)) />
			</cfif>
			<cfset variables._dataCache[dataKey] = data />
		</cfif>
		<cfreturn variables._dataCache[dataKey] />
	</cffunction>

	<cffunction name="writeData" access="private" output="no" returntype="void">
		<cfset fileWrite(getDataFilePath(), serialize(loadData())) />
	</cffunction>

	<cffunction name="getDataStoreName" access="private" output="no" returntype="string">
		<cfreturn "#request.admintype#-#getLuceeId()[request.admintype].id#" />
	</cffunction>

	<cffunction name="getDataFilePath" access="private" output="no" returntype="string">
		<cfif request.admintype eq "server">
			<cfset local.datadir = expandPath("{lucee-server}/userdata") & server.separator.file />
		<cfelse>
			<cfset local.datadir = "/lucee/admin/userdata/" />
		</cfif>
		<cfif not directoryExists(datadir)>
			<cfdirectory action="create" directory="#datadir#" />
		</cfif>
		<cfreturn "#datadir##getDataStoreName()#.cfm" />
	</cffunction>

</cfcomponent>