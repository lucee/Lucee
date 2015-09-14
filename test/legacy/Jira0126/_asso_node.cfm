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
 ---><cfsetting enablecfoutputonly="true">
<cfif thisTag.executionMode eq "start">
	<cfset parentTag = ListGetAt(getBaseTagList(), 2)>
	<cfset thisTag.items = ArrayNew(1)>
	<cfset attributes._out = "">
<cfelseif thisTag.executionMode eq "end">
	<cfsavecontent variable="attributes._out"><cfoutput>node: {level: #attributes.level#<cfif arrayLen(thisTag.items)>,#thisTag.items[1]._out#</cfif>}</cfoutput></cfsavecontent>
	<cfassociate basetag="#parentTag#" datacollection="items">
</cfif>
<cfsetting enablecfoutputonly="false">