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



	<cffunction name="testEncodeForStatic" localmode="true">
		<cfset str="<">
		<cfsavecontent variable="c"><cfoutput encodeFor="url">lit:#str#</cfoutput></cfsavecontent>
		<cfset assertEquals('lit:%3C',c)>
	</cffunction>

	<cffunction name="testEncodeForDynamic" localmode="true">
		<cfset str="<">
		<cfset et="url">
		<cfsavecontent variable="c"><cfoutput encodeFor="#et#">lit:#str#</cfoutput></cfsavecontent>
		<cfset assertEquals('lit:%3C',c)>
	</cffunction>

	<cffunction name="testEncodeForIgnoreExistingEncode" localmode="true">
		<cfset str="<">
		<cfsavecontent variable="c"><cfoutput encodeFor="url">#encodeForCSS(str)#:#str#</cfoutput></cfsavecontent>
		<cfset assertEquals('\3c :%3C',c)>
	</cffunction>


	<cffunction name="testEncodeForInvalid" localmode="true">
		<cfset str="<">
		<cfset et="susi">
		<cfset failed=false>
		<cftry>
			<cfoutput encodeFor="#et#">lit:#str#</cfoutput>
			<cfcatch><cfset failed=true></cfcatch>
		</cftry>
		<cfif !failed><cfset fail("this must fail because the type is invalid")></cfif>
	</cffunction>


</cfcomponent>