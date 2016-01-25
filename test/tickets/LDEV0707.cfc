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

	<cffunction name="testLoopCondition1" localmode="true">
		<cfset a = "This is a string">
		<cfset b = 0>
		<cfset alen = LEN(a)>

		<cfloop condition="(alen GT 3) AND (b EQ 0)">
			<cfset a = Left(a, Len(a)-1)>
			<cfset alen = LEN(a)>
		</cfloop>
	</cffunction>

	<cffunction name="testLoopCondition2" localmode="true">
		<cfset a = "This is a string">
		<cfset b = 0>

		<cfloop condition="(LEN(a) GT 3) AND (b EQ 0)">
			<cfset a = Left(a, Len(a)-1)>
		</cfloop>
	</cffunction>
</cfcomponent>