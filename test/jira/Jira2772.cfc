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

	<cffunction name="beforeTests">
		<cfset variables.name=ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".")>
		<cfset variables.dir=getDirectoryFromPath(getCurrentTemplatePath())&name&"/">
		<cfif directoryExists(dir)>
			<cfset afterTests()>
		</cfif>
		<cfdirectory directory="#dir#" action="create" mode="777">
	</cffunction>
	
	<cffunction name="afterTests">
		<cfset directorydelete(dir,true)>
	</cffunction>
	
	<cffunction name="testTagFile" localMode="modern">
		<cfset local.file=dir&"1.txt">
		
		<cffile action="write" file="#file#" output="Hello World" mode="644" addnewline="No">
		<cffile action="read" file="#file#" variable="local.content" mode="644">
		<cfset assertEquals("Hello World",content)>
		
		<cffile action="write" file="#file#" output="Hello" mode="644" addnewline="No">
		<cffile action="read" file="#file#" variable="local.content" mode="644">
		<cfset assertEquals("Hello",content)>
		
		<cffile action="write" file="#file#" output="" mode="644" addnewline="No">
		<cffile action="read" file="#file#" variable="local.content" mode="644">
		<cfset assertEquals("",content)>
	</cffunction>
	
	
	<cffunction name="testFunctionFileWrite" localMode="modern">
		<cfset local.file=dir&"1.txt">
		<cfset fileWrite(file,"Hello World")>
		<cffile action="read" file="#file#" variable="local.content" mode="644">
		<cfset assertEquals("Hello World",content)>
		
		<cfset fileWrite(file,"Hello")>
		<cffile action="read" file="#file#" variable="local.content" mode="644">
		<cfset assertEquals("Hello",content)>
		
		<cfset fileWrite(file,"")>
		<cffile action="read" file="#file#" variable="local.content" mode="644">
		<cfset assertEquals("",content)>
	</cffunction>
	
</cfcomponent>