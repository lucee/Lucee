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
 ---><cfcomponent hint="Note" extends="lucee.admin.plugin.Plugin">
	
	<cffunction name="init"
		hint="this function will be called to initalize">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
		<cfset app.note=load()>
		
	</cffunction>

	<cffunction name="overview" output="yes"
		hint="load data for a single note">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
		<cfargument name="req" type="struct">
		<cfset req.note=app.note>
	</cffunction>
	
	<cffunction name="update" output="no"
		hint="update note">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
		<cfargument name="req" type="struct">
		<cfset app.note=req.note>
		<cfset save(app.note)>
		
		<cfreturn "redirect:overview">
	</cffunction>
	
	<cffunction name="_display">
		<cfargument name="template" type="string">
		<cfargument name="lang" type="struct">
		<cfargument name="app" type="struct">
		<cfargument name="req" type="struct">
		
		<cfimport prefix="admin" taglib="../../">
		<cfset tabs=structNew("linked")>
		<cfset tabs.susi    = "susi">
		<cfset tabs.peter    = "peter">
		
		<admin:tabbedPane name="note" tabs="#tabs#" default="peter">
			<admin:tab name="susi">
				<cfinclude template="#template#">
			</admin:tab>
			<admin:tab name="peter">
				<cfinclude template="#template#">
			</admin:tab>
		</admin:tabbedPane>
		
		
	</cffunction>
	
</cfcomponent>