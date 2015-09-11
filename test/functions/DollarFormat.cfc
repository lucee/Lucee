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
	<cffunction name="testDollarFormat" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#DollarFormat("")#", right="$0.00")>
<cfset valueEquals(left="#DollarFormat("1")#", right="$1.00")>
<cfset valueEquals(left="#DollarFormat("1.3333333")#", right="$1.33")>
<cfset valueEquals(left="#DollarFormat("123.46")#", right="$123.46")>
<cfset valueEquals(left="#DollarFormat("1.999999")#", right="$2.00")>
<cfset valueEquals(left="#DollarFormat("1.774")#", right="$1.77")>
<cfset valueEquals(left="#DollarFormat("1.776")#", right="$1.78")>
<cftry>
	<cfset valueEquals(left="#DollarFormat("one Dollar")#", right="$1.00")>
	<cfset fail("must throw:invalid call of the function dollarFormat, first Argument (number) is invalid, Cant cast String [one Dollar] to a number")>
	<cfcatch></cfcatch>
</cftry>

<cfset org=GetLocale()>
<cfset valueEquals(left="#DollarFormat(200000)#", right="$200,000.00")>
<cfset setLocale('english (us)')>
<cfset valueEquals(left="#DollarFormat(200000)#", right="$200,000.00")>
<cfset setLocale('english (uk)')>
<cfset valueEquals(left="#DollarFormat(200000)#", right="$200,000.00")>
<cfset setLocale('german (swiss)')>
<cfset valueEquals(left="#DollarFormat(200000)#", right="$200,000.00")>

<cfset setLocale(org)>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>