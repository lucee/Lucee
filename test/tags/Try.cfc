<!---
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
 --->
<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	
	<cffunction name="testCFCatchLiveTime1" localmode=true>
		<cfset exists="">
		<cfset assertTrue(isNull(cfcatch))>
		<cftry>
			<cfthrow type="TestException" message="test testCfCatch">
			<cfcatch>
				<cfset exists=!isNull(cfcatch.message)>
			</cfcatch>
		</cftry>
		<cfset assertTrue(isNull(cfcatch))>
		<cfset assertTrue(exists)>
	</cffunction>

	<cffunction name="testCFCatchLiveTime2" localmode=true>
		<cftry>
			<cftry>
				<cfthrow type="application" message="1">
				<cfcatch type="application">
					<cfset assertEquals('1',cfcatch.message)>
					<cfthrow type="TestException" message="2">
				</cfcatch>
			</cftry>
			<cfcatch>
				<cfset assertEquals('2',cfcatch.message)>
			</cfcatch>
		</cftry>
		<!--- now it should no longer exist --->
		<cfset assertTrue(isNull(cfcatch))>
	</cffunction>


	<cffunction name="testCFCatchLiveTime3" localmode=true>
		<cfscript>
		try{
			try { 
				_test();
			}
			catch( Any e ){
				rethrow;
				
			}
			finally {
				
			}
		}
		catch(ee) {

		}
		assertTrue(isNull(cfcatch));
		</cfscript>
	</cffunction>
	<cffunction name="_test" access="private">
		<cfthrow message="abc">
	</cffunction>

	<!--- <cffunction access="public" name="testCfCatchName" localmode=true>
		<cfset exists="">
		<cfset exists2="">
		
		<cftry>
			<cfthrow type="TestException" message="test testCfCatchName">
			<cfcatch name="local.etrytest">
				<cfset exists=!isNull(etrytest.message)>
				<cfset exists2=!isNull(cfcatch.message)>
			</cfcatch>
		</cftry>

		<cfset assertTrue(exists)>
		<cfset assertFalse(exists2)>
		<cfset assertTrue(isNull(local.etrytest))>
	</cffunction> --->
</cfcomponent>
