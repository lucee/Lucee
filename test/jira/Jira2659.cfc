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
	
	<cffunction name="testTagLoopTagBreak">
		<cfsavecontent variable="local.c" trim="true">
		<cfloop from="1" to="2" index="o" label="outer">outer<cfloop index="i" from="1" to="2" label="inner">inner<cfbreak "outer"></cfloop></cfloop>
		</cfsavecontent>
		<cfset assertEquals("outerinner",c)>
	</cffunction>
	
	<cffunction name="testTagLoopTagContinue">
		<cfsavecontent variable="local.c" trim="true">
		<cfloop index="o" from="1" to="4" label="outer">outer<cfloop index="i" from="1" to="4" label="inner">inner<cfcontinue "outer"></cfloop></cfloop>
		</cfsavecontent>
		<cfset assertEquals("outerinnerouterinnerouterinnerouterinner",c)>
	</cffunction> 
	
	
	<!------>
	<cffunction name="testTagLoopScriptBreak">
		<cfsavecontent variable="local.c" trim="true">
		<cfloop index="o" from="1" to="2" label="outer">outer<cfloop index="i" from="1" to="2" label="inner">inner<cfscript>
			break outer;
		</cfscript></cfloop></cfloop>
		</cfsavecontent>
		<cfset assertEquals("outerinner",c)>
	</cffunction>
	
	<cffunction name="testTagLoopScriptContinue" access="public">
		<cfsavecontent variable="local.c" trim="true">
		<cfloop index="o" from="1" to="4" label="outer">outer<cfloop  index="i" from="1" to="4" label="inner">inner<cfscript>
			continue outer;
		</cfscript></cfloop></cfloop>
		</cfsavecontent>
		<cfset assertEquals("outerinnerouterinnerouterinnerouterinner",c)>
	</cffunction> 

	
	<cffunction name="testScriptWhileScriptBreak">
		<cfscript>
		var res="";
		whileLabel:while(true){
			res&="o;";
			loop index="x" from="1" to="5"  {
				res&="i;";
				break whileLabel;
			}
		}
		assertEquals("o;i;",res);
		</cfscript>
	</cffunction>
	
	
	<cffunction name="testScriptWhileScriptContinue">
		<cfscript>
		var res="";
		count=0;
		whileLabel:while(count++<2){
			res&="o;";
			loop index="x" from="1" to="5"  {
				res&="i;";
				continue whileLabel;
			}
		}
		assertEquals("o;i;o;i;",res);
		</cfscript>
	</cffunction>
	
	<cffunction name="testScriptLoopScriptBreak">
		<cfscript>
		var res="";
		loop index="x" from="1" to="5" label="susi" {
			res&="o;";
			loop index="x" from="1" to="5"  {
				res&="i;";
				break susi;
			}
		}
		assertEquals("o;i;",res);
		</cfscript>
	</cffunction>
	<cffunction name="testScriptLoopScriptContinue">
		<cfscript>
		var res="";
		loop index="x" from="1" to="2" label="susi" {
			res&="o;";
			loop index="x" from="1" to="2"  {
				res&="i;";
				continue susi;
			}
		}
		assertEquals("o;i;o;i;",res);
		</cfscript>
	</cffunction>
	
</cfcomponent>