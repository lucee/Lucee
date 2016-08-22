<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
		<cfset variables.parent=getDirectoryFromPath(getCurrentTemplatePath()) & name & Server.separator.file>
	</cffunction>

	<cffunction name="afterTests">
		<cfset directorydelete(parent,true)>
	</cffunction>

	<cffunction name="testDirectoryList" localMode="modern">
		<cfset var SEP = Server.separator.file>
		<cflock name="testdirectoryList" timeout="1" throwontimeout="no" type="exclusive">
			<cfset path=parent&createUUID()>
			<cfset path2=path&"#SEP#a">
			<cfset directoryCreate(path2)>
			<cffile action="write" addnewline="yes" file="#path##SEP#b.txt" output="aaa" fixnewline="no">
			<cffile action="write" addnewline="yes" file="#path2##SEP#c.txt" output="aaa" fixnewline="no">

			<!--- recursive false --->
			<cfset dir=directoryList(path)>
			<cfset assertEquals(2,arrayLen(dir))>
			<cfset assertEquals("#path##SEP#a,#path##SEP#b.txt",listSort(arrayToList(dir),'textnocase'))>

			<!--- recursive true --->
			<cfset dir=directoryList(path,true)>
			<cfset assertEquals(3,arrayLen(dir))>
			<cfset assertEquals("#path##SEP#a,#path##SEP#a#SEP#c.txt,#path##SEP#b.txt",listSort(arrayToList(dir),'textnocase'))>

			<!--- type:directory --->
			<cfset dir=directoryList(path:path,type:'directory')>
			<cfset assertEquals(1,arrayLen(dir))>
			<cfset assertEquals("#path##SEP#a",arrayToList(dir))>
			
			<!--- type:file --->
			<cfset dir=directoryList(path:path,type:'file')>
			<cfset assertEquals(1,arrayLen(dir))>
			<cfset assertEquals("#path##SEP#b.txt",arrayToList(dir))>


			<!--- list info --->
			<cfset dir=directoryList(path,true,"name")>
			<cfset assertEquals(3,arrayLen(dir))>
			<cfset assertEquals("a,b.txt,c.txt",listSort(arrayToList(dir),'textnocase'))>

			<cfset dir=directoryList(path,true,"path")>
			<cfset assertEquals(3,arrayLen(dir))>
			<cfset assertEquals("#path##SEP#a,#path##SEP#a#SEP#c.txt,#path##SEP#b.txt",listSort(arrayToList(dir),'textnocase'))>

			<cfset dir=directoryList(path,true,"query")>
			<cfset directoryDelete(path,true)>
		</cflock>

	</cffunction>
</cfcomponent>