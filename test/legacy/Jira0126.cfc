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
		<cfimport prefix="t" taglib="./Jira0126"> 
		
<cfsavecontent variable="content">
<t:_asso_tree>
	<t:_asso_node level="1">
		<t:_asso_node level="2">
			<t:_asso_node level="3">
				<t:_asso_node level="4" />
			</t:_asso_node>
		</t:_asso_node>
	</t:_asso_node>
</t:_asso_tree>
</cfsavecontent>


<cfset assertEquals("tree: {node: {level: 1,node: {level: 2,node: {level: 3,node: {level: 4}}}}}",trim(content))>
	
	</cffunction>
</cfcomponent>