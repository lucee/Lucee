<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction access="private" name="test">
		<cftry> 
			<cfthrow type="TestException" message="test exception"> 
			<cfcatch name="local.e">
				<cfset local.cfcatchExists=isNull(cfcatch)>
				<cfset local.eExists=isNull(e)>
			</cfcatch> 
		</cftry>

		<cfset assertFalse(cfcatchExists)>
		<cfset assertTrue(eExists)>
	</cffunction>
</cfcomponent>