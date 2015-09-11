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
	<cffunction name="testDecimalFormat" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="x#toString(DecimalFormat (123))#", right="x123.00")> 
<cfset valueEquals(left="x#toString(DecimalFormat (123.00000000002))#", right="x123.00")> 
<cfset valueEquals(left="x#toString(DecimalFormat (123456789.00))#", right="x123,456,789.00")> 
<cfset valueEquals(left="x#toString(DecimalFormat (123456.00))#", right="x123,456.00")> 

<cfset valueEquals(left="x#toString(DecimalFormat (-123))#", right="x-123.00")> 
<cfset valueEquals(left="x#toString(DecimalFormat (-1234))#", right="x-1,234.00")> 
<cfset valueEquals(left="x#toString(DecimalFormat (-123.00000000002))#", right="x-123.00")> 
<cfset valueEquals(left="x#toString(DecimalFormat (-123456789.00))#", right="x-123,456,789.00")> 
<cfset valueEquals(left="x#toString(DecimalFormat (-123456.00))#", right="x-123,456.00")>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>