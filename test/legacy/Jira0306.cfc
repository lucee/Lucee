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

		<cfset defineDatasource()>

		<!--- create table User if necessary --->
		<cftry>
			<cfquery>
		        select * from User306
		    </cfquery>
		    <cfcatch>
		    <cfquery>
		        CREATE TABLE User306 (
		            id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, 
		            data VARCHAR(100) 
		        );
		    </cfquery>
		    </cfcatch>
		</cftry>
		<!--- create table Order if necessary --->
		<cftry>
			<cfquery>
		        select * from Order306
		    </cfquery>
		    <cfcatch>
		    <cfquery>
		        CREATE TABLE Order306 (
		            id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, 
		            data VARCHAR(100) , 
		            User_id INT , 
		            
		            Foreign Key (User_id) references User306(id)
		        );
		    </cfquery>
		    </cfcatch>
		</cftry>
		
	</cffunction>
	<cffunction name="afterTests">
		<!--- drop table User --->
		<cftry>
			<cfquery>
		        drop table User306
		    </cfquery>
		    <cfcatch></cfcatch>
		</cftry>
		
		<!--- drop table Order --->
		<cftry>
			<cfquery>
		        drop table Order306
		    </cfquery>
		    <cfcatch></cfcatch>
		</cftry>
	</cffunction>
	
	
	<cffunction name="testOrder_User_id">
		<cfdbinfo type="columns" name="local.data" table="Order306" pattern="User_id">
		<cfset assertEquals("User_id",data.COLUMN_NAME)>
		<cfset assertEquals(10,data.COLUMN_SIZE)>
		<cfset assertEquals(0,data.DECIMAL_DIGITS)>
		<cfset assertEquals(true,_boolean(data.IS_FOREIGNKEY))>
		<cfset assertEquals(true,_boolean(data.IS_NULLABLE))>
		<cfset assertEquals(false,_boolean(data.IS_PRIMARYKEY))>
		<cfset assertEquals(3,data.ORDINAL_POSITION)>
		<cfset assertEquals("id",data.REFERENCED_PRIMARYKEY&"")>
		<cfset assertEquals("User306",data.REFERENCED_PRIMARYKEY_TABLE&"")>
		<cfset assertEquals("",data.REMARKS)>
		<cfset assertEquals("INT",left(data.TYPE_NAME,3))>
	</cffunction>
	
	<cffunction name="testOrder_id">
		<cfdbinfo type="columns" name="local.data" table="Order306" pattern="id">
		<cfset assertEquals("id",data.COLUMN_NAME)>
		<cfset assertEquals(10,data.COLUMN_SIZE)>
		<cfset assertEquals(0,data.DECIMAL_DIGITS)>
		<cfset assertEquals(false,_boolean(data.IS_FOREIGNKEY))>
		<cfset assertEquals(false,_boolean(data.IS_NULLABLE))>
		<cfset assertEquals(true,_boolean(data.IS_PRIMARYKEY))>
		<cfset assertEquals(1,data.ORDINAL_POSITION)>
		<cfset assertEquals("N/A",data.REFERENCED_PRIMARYKEY&"")>
		<cfset assertEquals("N/A",data.REFERENCED_PRIMARYKEY_TABLE&"")>
		<cfset assertEquals("",data.REMARKS)>
		<cfset assertEquals("INT",left(data.TYPE_NAME,3))>
	</cffunction>
	
	<cffunction name="testOrder_data">
		<cfdbinfo type="columns" name="local.data" table="Order306" pattern="data">
		<cfset assertEquals("data",data.COLUMN_NAME)>
		<cfset assertEquals(100,data.COLUMN_SIZE)>
		<cfset assertEquals(0,data.DECIMAL_DIGITS)>
		<cfset assertEquals(false,_boolean(data.IS_FOREIGNKEY))>
		<cfset assertEquals(true,_boolean(data.IS_NULLABLE))>
		<cfset assertEquals(false,_boolean(data.IS_PRIMARYKEY))>
		<cfset assertEquals(2,data.ORDINAL_POSITION)>
		<cfset assertEquals("N/A",data.REFERENCED_PRIMARYKEY&"")>
		<cfset assertEquals("N/A",data.REFERENCED_PRIMARYKEY_TABLE&"")>
		<cfset assertEquals("",data.REMARKS)>
		<cfset assertEquals("VARCHAR",data.TYPE_NAME)>
	</cffunction>
	
	
	<cffunction name="testUser_id">
		<cfdbinfo type="columns" name="local.data" table="User306" pattern="id">
		<cfset assertEquals("id",data.COLUMN_NAME)>
		<cfset assertEquals(10,data.COLUMN_SIZE)>
		<cfset assertEquals(0,data.DECIMAL_DIGITS)>
		<cfset assertEquals(false,_boolean(data.IS_FOREIGNKEY))>
		<cfset assertEquals(false,_boolean(data.IS_NULLABLE))>
		<cfset assertEquals(true,_boolean(data.IS_PRIMARYKEY))>
		<cfset assertEquals(1,data.ORDINAL_POSITION)>
		<cfset assertEquals("N/A",data.REFERENCED_PRIMARYKEY&"")>
		<cfset assertEquals("N/A",data.REFERENCED_PRIMARYKEY_TABLE&"")>
		<cfset assertEquals("",data.REMARKS)>
		<cfset assertEquals("INT",left(data.TYPE_NAME,3))>
	</cffunction>
	
	
	<cffunction name="testUser_data">
		<cfdbinfo type="columns" name="local.data" table="User306" pattern="data">
		<cfset assertEquals("data",data.COLUMN_NAME)>
		<cfset assertEquals(100,data.COLUMN_SIZE)>
		<cfset assertEquals(0,data.DECIMAL_DIGITS)>
		<cfset assertEquals(false,_boolean(data.IS_FOREIGNKEY))>
		<cfset assertEquals(true,_boolean(data.IS_NULLABLE))>
		<cfset assertEquals(false,_boolean(data.IS_PRIMARYKEY))>
		<cfset assertEquals(2,data.ORDINAL_POSITION)>
		<cfset assertEquals("N/A",data.REFERENCED_PRIMARYKEY&"")>
		<cfset assertEquals("N/A",data.REFERENCED_PRIMARYKEY_TABLE&"")>
		<cfset assertEquals("",data.REMARKS)>
		<cfset assertEquals("VARCHAR",data.TYPE_NAME)>
	</cffunction>
	
	<cffunction access="private" name="_boolean">
		<cfargument name="b" type="boolean" required="yes">
		<cfreturn arguments.b==true>
	</cffunction>


<cfscript>
	private string function defineDatasource(){
		application action="update" 
			datasource="#{
	  		class: 'org.h2.Driver'
	  		, bundleName: 'org.h2'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/jira306;MODE=MySQL'
		}#";
	}

	public function afterTests() {
		var javaIoFile=createObject("java","java.io.File");
		loop array=DirectoryList(
			path=getDirectoryFromPath(getCurrentTemplatePath()), 
			recurse=true, filter="*.db") item="local.path"  {
			fileDeleteOnExit(javaIoFile,path);
		}
	}

	private function fileDeleteOnExit(required javaIoFile, required string path) {
		var file=javaIoFile.init(arguments.path);
		if(!file.isFile())file=javaIoFile.init(expandPath(arguments.path));
		if(file.isFile()) file.deleteOnExit();
	}
</cfscript>


</cfcomponent>