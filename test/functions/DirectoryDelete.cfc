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
		<cfset variables.parent=getDirectoryFromPath(getCurrentTemplatePath())&name&"/">
		
	</cffunction>
	<cffunction name="afterTests">
		<cfset directorydelete(parent,true)>
	</cffunction>
	<cffunction name="testDirectoryDelete" localMode="modern">

<!--- begin old test code --->
<cflock name="testdirectoryDelete" timeout="1" throwontimeout="no" type="exclusive">
<cfset dir=parent&createUUID()>

<cfset directoryCreate(dir)>
<cfset directorydelete(dir)>

<cftry>
	<cfset directorydelete(dir)>
	<cfset fail("must throw:does not exist")>
	<cfcatch></cfcatch>
</cftry>
   

<cfset dir2=dir&"/a/b/c/">
<cfset directoryCreate(dir2)>
<cftry>
	<cfset directorydelete(dir)>
	<cfset fail("must throw:The specified directory ... could not be deleted.")>
	<cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset directorydelete(dir,false)>
	<cfset fail("must throw:The specified directory ... could not be deleted.")>
	<cfcatch></cfcatch>
</cftry>
<cfset directorydelete(dir,true)>

</cflock>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>