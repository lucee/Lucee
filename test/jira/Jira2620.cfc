<!--- 
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
	<cfset variables.basedir="ram://Jira2620/">
	
	<cffunction name="testFileAppendNoAttr" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cftry>
			<cffile action="append" output="append" file="#variables.basedir#append_none/test.txt">
			<cfset fail("must throw: parent directory for [#variables.basedir#append_none/test.txt] doesn't exist ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testFileAppendFalse" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cftry>
			<cffile action="append" output="append" file="#variables.basedir#append_false/test.txt" createPath="false">
			<cfset fail("must throw: parent directory for [#variables.basedir#append_false/test.txt] doesn't exist ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testFileAppendTrue" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="append" output="append" file="#variables.basedir#append_true/test.txt" createPath="true">
		<cffile action="read" file="#variables.basedir#append_true/test.txt" variable="local.c">
		<cfset assertEquals("append",trim(c))>
	</cffunction>
	
	
	<cffunction name="testFileWriteNoAttr" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cftry>
			<cffile action="write" output="write" file="#variables.basedir#write_none/test.txt">
			<cfset fail("must throw: parent directory for [#variables.basedir#write_none/test.txt] doesn't exist ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testFileWriteFalse" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cftry>
			<cffile action="write" output="write" file="#variables.basedir#write_false/test.txt" createPath="false">
			<cfset fail("must throw: parent directory for [#variables.basedir#write_false/test.txt] doesn't exist ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testFileWriteTrue" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#write_true/test.txt" createPath="true">
		<cffile action="read" file="#variables.basedir#write_true/test.txt" variable="local.c">
		<cfset assertEquals("write",trim(c))>
	</cffunction>
	
	
	
	
	<cffunction name="testFileTouchNoAttr" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cftry>
			<cffile action="touch" output="touch" file="#variables.basedir#touch_none/test.txt">
			<cfset fail("must throw: parent directory for [#variables.basedir#touch_none/test.txt] doesn't exist ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testFileTouchFalse" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cftry>
			<cffile action="touch" output="touch" file="#variables.basedir#touch_false/test.txt" createPath="false">
			<cfset fail("must throw: parent directory for [#variables.basedir#touch_false/test.txt] doesn't exist ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testFileTouchTrue" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="touch" file="#variables.basedir#touch_true/test.txt" createPath="true">
		<cffile action="read" file="#variables.basedir#touch_true/test.txt" variable="local.c">
		<cfset assertEquals("",trim(c))>
	</cffunction>
	
	<cffunction name="testDirectoryCreateNoAttr" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cfdirectory action="create" directory="#variables.basedir#create/none">
		<cfset assertEquals(true,directoryExists("#variables.basedir#create/none"))>
	</cffunction>
	
	<cffunction name="testDirectoryCreateFalse" access="public">
		<cfset deleteDirectory(variables.basedir)>
		
		<cftry>
			<cfdirectory action="create" directory="#variables.basedir#create/none" createPath="false">
			<cfset fail("must throw: can't create file [ram:///Jira2620/create/none], missing parent directory ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testDirectoryCreateTrue" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cfdirectory action="create" directory="#variables.basedir#create/none" createPath="true">
		<cfset assertEquals(true,directoryExists("#variables.basedir#create/none"))>
	</cffunction>
	
	<cffunction name="testDirectoryCopyNoAttr" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
		
		<cfdirectory action="copy" directory="#variables.basedir#source/" destination="#variables.basedir#copy/none">
		<cfset assertEquals(true,directoryExists("#variables.basedir#copy/none"))>
	</cffunction>
	
	<cffunction name="testDirectoryCopyFalse" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
			
		<cftry>
			<cfdirectory action="copy" directory="#variables.basedir#source/" destination="#variables.basedir#copy/false" createPath="false">
			<cfset fail("must throw: can't create file [ram:///Jira2620/create/none], missing parent directory ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testDirectoryCopyTrue" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
		
		<cfdirectory action="copy" directory="#variables.basedir#source/" destination="#variables.basedir#copy/true" createPath="true">
		<cfset assertEquals(true,directoryExists("#variables.basedir#copy/true"))>
	</cffunction>
	
	<cffunction name="testDirectoryRenameNoAttr" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
		
		<cfdirectory action="rename" directory="#variables.basedir#source/" newDirectory="#variables.basedir#rename/none">
		<cfset assertEquals(true,directoryExists("#variables.basedir#rename/none"))>
	</cffunction>
	
	<cffunction name="testDirectoryRenameFalse" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
		
		<cftry>
			<cfdirectory action="rename" directory="#variables.basedir#source/" newDirectory="#variables.basedir#rename/false"  createPath="false">
			<cfset fail("must throw: can't create file [ram:///Jira2620/create/none], missing parent directory ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testDirectoryRenameTrue" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
		
		<cfdirectory action="rename" directory="#variables.basedir#source/" newDirectory="#variables.basedir#rename/false"  createPath="true">
		<cfset assertEquals(true,directoryExists("#variables.basedir#rename/false"))>
	</cffunction>
	
	<cffunction name="testBIFDirectoryRenameNoArg" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
		
		<cfset directoryRename("#variables.basedir#source/","#variables.basedir#rename/none")>
		<cfset assertEquals(true,directoryExists("#variables.basedir#rename/none"))>
	</cffunction>
	
	<cffunction name="testBIFDirectoryRenameFalse" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
		
		<cftry>
			<cfset directoryRename("#variables.basedir#source/","#variables.basedir#rename/none",false)>
			<cfset fail("must throw: can't create file [ram:///Jira2620/create/none], missing parent directory ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testBIFDirectoryRenameTrue" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
		
		<cfset directoryRename("#variables.basedir#source/","#variables.basedir#rename/true",true)>
		<cfset assertEquals(true,directoryExists("#variables.basedir#rename/true"))>
	</cffunction>
	
	<cffunction name="testBIFDirectoryCopyNoArg" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
		<cfset directoryCopy("#variables.basedir#source/", "#variables.basedir#copy/none",true, "")>
		<cfset assertEquals(true,directoryExists("#variables.basedir#copy/none"))>
	</cffunction>
	
	<cffunction name="testBIFDirectoryCopyFalse" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
			
		<cftry>
			<cfset directoryCopy("#variables.basedir#source/", "#variables.basedir#copy/false",true, "",false)>
			<cfset fail("must throw: can't create file [ram:///Jira2620/create/none], missing parent directory ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testBIFDirectoryCopyTrue" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cffile action="write" output="write" file="#variables.basedir#source/test.txt" createPath="true">
		
		<cfset directoryCopy("#variables.basedir#source/", "#variables.basedir#copy/true",true, "",true)>
		<cfset assertEquals(true,directoryExists("#variables.basedir#copy/true"))>
	</cffunction>
	
	<cffunction name="testBIFDirectoryCreateNoArg" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cfset DirectoryCreate("#variables.basedir#create/none")>
		<cfset assertEquals(true,directoryExists("#variables.basedir#create/none"))>
	</cffunction>
	
	<cffunction name="testBIFDirectoryCreateFalse" access="public">
		<cfset deleteDirectory(variables.basedir)>
		
		<cftry>
			<cfset DirectoryCreate("#variables.basedir#create/false")>
			<cfset fail("must throw: can't create file [ram:///Jira2620/create/false], missing parent directory ")>
			<cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	<cffunction name="testBIFDirectoryCreateTrue" access="public">
		<cfset deleteDirectory(variables.basedir)>
		<cfset DirectoryCreate("#variables.basedir#create/none",true)>
		<cfset assertEquals(true,directoryExists("#variables.basedir#create/none"))>
	</cffunction>
	
<cfscript>
	public function beforeTests(){
		variables.filePath=createFile("source.txt","Susi Sorglos");
	}
	public function afterTests(){
		deleteDirectory(variables.basedir);
	}

	public function setUp(){
	}

	/**
	* creates a file in the ram resource and returnthe absoulte path to this file
	* @filename name of the file, for example "test.txt"
	* @content string content for the file
	*/
	private string function createFile(required string filename, required string content) {
		local.path="ram:///"&filename;
		file action="write" file="#path#" output="#content#";
		return path;
	}

	/**
	* creates a file in the ram resource and returnthe absoulte path to this file
	* @filename name of the file, for example "test.txt"
	* @content string content for the file
	*/
	private void function deleteDirectory(required string dir) {
		if(directoryexists(dir))directory action="delete" directory="#dir#" recurse="true";
	}
 
</cfscript>
</cfcomponent>