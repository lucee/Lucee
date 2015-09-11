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
	
	
	<cffunction name="testFileCopy" localMode="modern">

<!--- begin old test code --->
<cfset srcDir=dir&"src/">
<cfset trgDir=dir&"trg/">
<cfdirectory directory="#srcDir#" action="create" mode="777">
<cfdirectory directory="#trgDir#" action="create" mode="777">
<cfscript>
// define paths
src=srcDir&"test.txt";
dest1=trgDir&"testx.txt";
dest3=trgDir&'test.txt';
fileWrite(src,"Susi");



valueEquals(FileExists(dest1),false);
valueEquals(FileExists(dest3),false);


// copy with destination file
fileCopy(src,dest1);

// copy with destination dir
fileCopy(src,trgDir);



valueEquals(FileExists(dest1),true);
valueEquals(FileExists(dest3),true);



//fileWrite(_file,"ABC");
//fileAppend(_file,"DEF","UTF-8");



</cfscript>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>