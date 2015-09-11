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
		
	</cffunction>
	<cffunction name="afterTests">
		<cfset directorydelete(dir,true)>
	</cffunction>
	
	<cffunction name="testdirectoryCopy" localMode="modern">

		<!--- inital create --->
		<cfdirectory directory=	"#dir#inc" action="create" mode="777">
		<cffile file=			"#dir#/inc/test1.txt" action="write">hello1</cffile>
		<cffile file=			"#dir#/inc/abra.txt" action="write">hello1</cffile>

		<cfdirectory directory=	"#dir#inc/empty" action="create" mode="777">
		<cfdirectory directory=	"#dir#inc/testempty" action="create" mode="777">
		<cfdirectory directory=	"#dir#inc/sub" action="create" mode="777">
		<cffile file=			"#dir#inc/sub/test3.txt" action="write">hello2</cffile>
		<cfscript>

		// copy not recursive
		directoryCopy("#dir#inc","#dir#inc2");
		directory directory="#dir#inc2" action="list" name="qry" recurse="yes";
		assertEquals("abra.txt,test1.txt",listSort(valueList(qry.name),'textnocase'));

		// copy not recursive with filter "test*"
		directoryCopy("#dir#inc","#dir#inc4",false,"test*");
		directory directory="#dir#inc4" action="list" name="qry" recurse="yes";
		assertEquals("test1.txt",listSort(valueList(qry.name),'textnocase'));

		//  copy recursive
		directoryCopy("#dir#inc","#dir#inc3",true);
		directory directory="#dir#inc3" action="list" name="qry" recurse="yes";
		assertEquals("abra.txt,empty,sub,test1.txt,test3.txt,testempty",listSort(valueList(qry.name),'textnocase'));

		// copy not recursive with filter "test*"
		directoryCopy("#dir#inc","#dir#inc5",true,"test*"); 
		directory directory="#dir#inc5" action="list" name="qry" recurse="yes";
		assertEquals("sub,test1.txt,test3.txt,testempty",listSort(valueList(qry.name),'textnocase'));
		// test1.txt,testempty
		</cfscript>
	</cffunction>
	
</cfcomponent>