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
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	
	<cffunction name="setUp"></cffunction>
	<cffunction name="test">
		
		<cfsavecontent variable="content"><cfset createObject("component","jira0236._argColl").ino(a:1,b:2)></cfsavecontent>
		<cfset assertEquals("A:1;B:2;A:1;B:37;",trim(content))>
		
		<cfsavecontent variable="content"><cfset createObject("component","jira0236._argColl").ino(1,2)></cfsavecontent>
		<cfset assertEquals("A:1;B:2;2:2;A:1;B:37;",trim(content))>
		
	</cffunction>
</cfcomponent>