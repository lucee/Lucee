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
 ---><cfsetting showdebugoutput="no">

<cffunction name="testTagTF" output="false">
	<cftry>
		<cfreturn "tag:in try;">
		<cffinally>
			<cfreturn "tag:in finally;">
		</cffinally>
	</cftry>
</cffunction>

<cffunction name="testTagTCF" output="false">
	<cftry>
		<cfreturn "tag:in try">
		<cfcatch>
			<cfreturn "tag:in catch;">
		</cfcatch>
		<cffinally>
			<cfreturn "tag:in finally;">
		</cffinally>
	</cftry>
</cffunction>

<cfscript>
	function testScriptTF(){
		try {
			return "script:in try;";
		}
		finally {
			return "script:in finally;";
		}
	}

	function testScriptTCF(){
		try {
			return "script:in try;";
		}
		catch (any e){
			return "script:in catch;";
		}
		finally {
			return "script:in finally;";
		}
	}

	if(isdefined("url.tagtf"))echo(testTagTF());
	if(isdefined("url.tagtcf"))echo(testTagTCF());
	if(isdefined("url.scripttf"))echo(testScriptTF());
	if(isdefined("url.scripttcf"))echo(testScriptTCF());
</cfscript>