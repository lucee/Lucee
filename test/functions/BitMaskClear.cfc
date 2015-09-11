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
	<cffunction name="testBitMaskClear" localMode="modern">
 
<!--- begin old test code --->
<cfset valueEquals(left="#BitMaskClear(3,1,1)#", right="1")>
<cfset valueEquals(left="#BitMaskClear(3,2,1)#", right="3")>
<cfset valueEquals(left="#BitMaskClear(31,2,4)#", right="3")>
<cftry>
	<cfset valueEquals(left="#BitMaskClear(31,32,4)#", right="3")>
	<cfset fail("must throw:Invalid argument for function BitMaskClear.")>
	<cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset valueEquals(left="#BitMaskClear(31,-1,4)#", right="3")>
	<cfset fail("must throw:Invalid argument for function BitMaskClear.")>
	<cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset valueEquals(left="#BitMaskClear(31,1,32)#", right="3")>
	<cfset fail("must throw:Invalid argument for function BitMaskClear.")>
	<cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset valueEquals(left="#BitMaskClear(31,1,-1)#", right="3")>
	<cfset fail("must throw:Invalid argument for function BitMaskClear.")>
	<cfcatch></cfcatch>
</cftry>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>