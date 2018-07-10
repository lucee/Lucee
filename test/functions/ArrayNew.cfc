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
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testArrayNew" localMode="modern">

<!--- begin old test code --->
<cfset arr=arrayNew(1)>
<cfset arr=arrayNew(2)>
<cfset arr=arrayNew(3)>

<cftry>
	<cfset arr=arrayNew(4)>
	<cfset fail("must throw:Array dimension 4 must be between 1 and 3. ")>
	<cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset arr=arrayNew(0)>
	<cfset fail("must throw:Array dimension 4 must be between 1 and 3. ")>
	<cfcatch></cfcatch>
</cftry>

<cfset arr=arrayNew(2)>
<cfset x=arr[1]>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>



	<cffunction name="testArraySync" localMode="modern">
		<cfset arr=arrayNew(1,true)>
		<cfset arr=arrayNew(1,false)>
	</cffunction>
	
</cfcomponent>