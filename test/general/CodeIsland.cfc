<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 --->
<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	

<cfscript>
	public void function testContent() {
		saveContent variable="local.c" {
		```abc```
		}
		assertEquals("abc",c);
	}
	public void function testTestContentAndVar() {
		local.x=1;
		saveContent variable="local.c" {
		```abc<cfset x=2>```
		}
		assertEquals("abc",c);
		assertEquals(2,x);
	}
	public void function testMultipleTags() {
		local.x="x";
		saveContent variable="local.c" {

```
<cfoutput>#x#<!---
---><cfloop from=1 to=3 index="i">#i#</cfloop></cfoutput>
```
		}

		assertEquals("x123",c.trim());
	}

	public void function testEscapeNoOutput() {
		local.x=1;
		saveContent variable="local.c" {
		```abc``````def```
		}
		assertEquals("abc```def",c);
	}
	public void function testEscapeNoOutputBeginning() {
		local.x=1;
		saveContent variable="local.c" {
		`````````def```
		}
		assertEquals("```def",c);
	}

	public void function testEscapeNoOutputEnd() {
		local.x=1;
		saveContent variable="local.c" {
		```abc`````````
		}
		assertEquals("abc```",c);
	}

	public void function testEscapeOutput() {
		local.x=1;
		saveContent variable="local.c" {
		```<cfoutput>abc``````def</cfoutput>```
		}
		assertEquals("abc```def",c);
	}


</cfscript>
	
	<cffunction name="testOutsideOutput">
		<cfset local.x="testOutsideOutput">
		<cfsavecontent variable="local.c">
		<cfoutput>
			<cfscript>
				```#x#```
			</cfscript>
		</cfoutput>
		</cfsavecontent>
		<cfset assertEquals("testOutsideOutput",c.trim())>
	</cffunction>

	<cffunction name="testMatrjoschka">
		<cfset local.x="b">
		<cfset local.y="e">
		<cfset local.z="f">
		<cfsavecontent variable="local.c">
		<cfoutput>
			<cfscript>
				```a#x#<cfscript>echo('c');```d#y#<cfscript>echo(z);</cfscript>#y#d```echo('c');</cfscript>#x#a```
			</cfscript>
		</cfoutput>
		</cfsavecontent>
		

		<cfset assertEquals("abcdefedcba",c.trim())>
	</cffunction>
	
	<cffunction name="testEscapeOutputBeginning">
		<cfset local.x="testOutsideOutput">
		<cfsavecontent variable="local.c">
		<cfoutput>
			<cfscript>
				`````````def```
			</cfscript>
		</cfoutput>
		</cfsavecontent>
		<cfset assertEquals("```def",c.trim())>
	</cffunction>

	<cffunction name="testEscapeOutputEnd">
		<cfset local.x="testOutsideOutput">
		<cfsavecontent variable="local.c">
		<cfoutput>
			<cfscript>
				```abc`````````
			</cfscript>
		</cfoutput>
		</cfsavecontent>
		<cfset assertEquals("abc```",c.trim())>
	</cffunction>

	<cffunction name="testEscapeOutputBeginningEnd">
		<cfsavecontent variable="local.c"><cfoutput><cfscript>
				````````````
		</cfscript></cfoutput></cfsavecontent>
		<cfset assertEquals("```",c.trim())>
	</cffunction>

	<cffunction name="testEscapeOutputBeginningEndDouble">
		<cfsavecontent variable="local.c"><cfoutput><cfscript>
				``````````````````
		</cfscript></cfoutput></cfsavecontent>
		<cfset assertEquals("``````",c.trim())>
	</cffunction>
</cfcomponent>