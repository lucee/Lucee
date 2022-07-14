/*
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
 */
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	variables.suffix="QueryExecute";

	public function beforeTests(){
		defineDatasource();

		try{
			query {
				echo("drop TABLE T"&suffix);
			}
		}
		catch(local.e){}
		
		
		query  {
			echo("CREATE TABLE T"&suffix&" (");
			echo("id int NOT NULL,");
			echo("i int,");		
			echo("dec DECIMAL");		
			echo(") ");
		}
	}

	private string function defineDatasource(){
		application action="update" 
			datasource="#{
	  		class: 'org.h2.Driver'
	  		,bundleName:'org.h2'
	  		,bundleVersion:'1.3.172'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/update;MODE=MySQL'
		}#";
	}


	public void function testArg1() localmode="true" {
		qry=queryExecute("select 1 as one");
		assertEquals(1,qry.recordcount);
		assertEquals(1,qry.one);
		assertEquals("qry",qry.getName());
	} 

	public void function testArg2() localmode="true" {
		qry1=queryExecute("select ? as one",["eins"]);
		assertEquals(1,qry1.recordcount);
		assertEquals('eins',qry1.one);
		assertEquals("qry1",qry1.getName());

		qry2=queryExecute("select :uno as one",{uno:"eis"});
		assertEquals(1,qry2.recordcount);
		assertEquals('eis',qry2.one);
		assertEquals("qry2",qry2.getName());
	}

	public void function testArg3() localmode="true" {
		qry1=queryExecute("select ? as one",["eins"],{psq:true});
		assertEquals(1,qry1.recordcount);
		assertEquals('eins',qry1.one);
		assertEquals("qry1",qry1.getName());

	} 


	public void function testNamedArg1() localmode="true" {
		qry=queryExecute(sql:"select 1 as one");
		assertEquals(1,qry.recordcount);
		assertEquals(1,qry.one);
		assertEquals("qry",qry.getName());
	} 

	public void function testNamedArg2() localmode="true" {
		qry1=queryExecute(sql:"select ? as one",params:["eins"]);
		assertEquals(1,qry1.recordcount);
		assertEquals('eins',qry1.one);
		assertEquals("qry1",qry1.getName());
	}

	public void function testNamedArg3() localmode="true" {
		qry1=queryExecute(sql:"select ? as one",params:["eins"],options:{psq:true});
		assertEquals(1,qry1.recordcount);
		assertEquals('eins',qry1.one);
		assertEquals("qry1",qry1.getName());

	} 
	function afterTests() {
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
} 