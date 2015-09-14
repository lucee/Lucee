<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
 ---><cfif thisTag.executionMode EQ "Start" >

<cfparam name="Attributes.Label" type="string" />

<cfoutput>{Start:#Attributes.Label#}</cfoutput>

  <cfset base_tags = getBaseTagList() />
  <!--- check if we have a parent context --->
	
	<cfif ListValueCountNoCase ( base_tags, "cf_mytag" ) GT 1 >
		<!--- NOTE: the instance number works if CF_MYTAG are not immediate children --->
      	<cfset pt = getBaseTagData( "CF_MYTAG", 1 ) />
		
		
		<cfif attributes.label EQ "lvl 3">
			<cfset ppt = getBaseTagData( "CF_MYTAG", 2 ) />
      		<cfoutput>{label:#attributes.label#;parent-label:#pt.Attributes.label#;parent-parent-label:#ppt.Attributes.label#}</cfoutput>
		<cfelse>
			<cfoutput>{label:#attributes.label#;parent-label:#pt.Attributes.label#}</cfoutput>
		</cfif>
		<!---
		<cfset parent_tag = getBaseTagData( "CF_MYTAG", 2 ) />
      	<cfoutput>{2=#parent_tag.Attributes.label#}</cfoutput>
		<cfabort>
		--->
    </cfif>

<cfelse>

<cfoutput>{End:#Attributes.Label#}</cfoutput>

</cfif>