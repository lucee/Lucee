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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	//processingdirective pageencoding="UTF-8";
	variables.suffix="Query";

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

		try{
			query {
				echo("drop TABLE TX"&suffix);
			}
		}
		catch(local.e){}
		
		
		query  {
			echo("CREATE TABLE TX"&suffix&" (");
			echo("str varchar(10)");		
			echo(") ");
		}
	}

	private string function defineDatasource(){
		application action="update" 
			datasource={
	  		class: 'org.h2.Driver'
	  		, bundleName: 'org.h2'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/update;MODE=MySQL'
		};
	}

	public void function testCachedWithinColumns() {
		query name="local.qry" cachedwithin="#createTimespan(0,0,0,1)#" {
			echo("select * from T"&suffix);
		} 
		var columnList1=qry.columnlist;
		queryAddColumn(qry,"susi");

		query name="local.qry" cachedwithin="#createTimespan(0,0,0,1)#" {
			echo("select * from T"&suffix);
		} 
		var columnList2=qry.columnlist;
		assertEquals(columnList1,columnList2);
		queryAddColumn(qry,"susi2");

	}

	public void function testQueryParamCharset() {
		// when NO charset is defined, it checks char length
		query name="local.qry" {
			echo("insert into TX"&suffix&" (str) values(");
			queryparam maxlength=10 value="ĀĀĀĀĀĀĀĀĀĀ";
			echo(")");
		}

		// when charset is defined, it checks for byte length
		var fail=false;
		try{
			query name="local.qry" {
				echo("insert into TX"&suffix&" (str) values(");
				queryparam maxlength=10 charset="UTF-8" value="ĀĀĀĀĀĀĀĀĀĀ";
				echo(")");
			}
		}
		catch(e) {
			fail=true;
		}
		if(!fail) throw "it must fail when doing byte length compare!"
		
	}


	public void function testParamsCharset() {
		// when NO charset is defined, it checks char length
		query name="local.qry" params=[{value:"ĀĀĀĀĀĀĀĀĀĀ",maxlength:10}] {
			echo("insert into TX"&suffix&" (str) values(?)");
		}
		
		var fail=false;
		try{
			query name="local.qry" params=[{value:"ĀĀĀĀĀĀĀĀĀĀ",charset:"UTF-8",maxlength:10}] {
				echo("insert into TX"&suffix&" (str) values(?)");
			}
		}
		catch(e) {
			fail=true;
		}
		if(!fail) throw "it must fail when doing byte length compare!"
	}

	public void function testCachedAfter() {

		application action="update" cachedAfter=createTimespan(0,0,0,1);
		try{
			try{
				query  {
					echo("CREATE TABLE tcQuery ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, v VARCHAR(100) )");
				}
			}
			catch(e){}

			query {
				echo(" insert into tcQuery(v) values(#now()#) ");
			}

			var n=now();
			var ca=dateAdd("n",-1,n);
			
			query {
				echo("update tcQuery set v=#now()#");
			}
			query name="local.q" cachedAfter=ca {
				echo("select * from tcQuery");
			}
			var first=q.v;

			sleep(500);

			query {
				echo("update tcQuery set v=#now()#");
			}
			query name="local.q" cachedAfter=ca {
				echo("select * from tcQuery");
			}
			var second=q.v;

			sleep(600);


			query {
				echo("update tcQuery set v=#now()#");
			}
			query name="local.q" cachedAfter=ca {
				echo("select * from tcQuery");
			}
			var third=q.v;

			assertEquals(first,second);
			assertNotEquals(first,third);
		}
		finally {
			application action="update" cachedAfter=createTimespan(0,0,0,0);
			query  {
				echo("drop TABLE tcQuery");
			}
		}
	}



	public void function testListenerComponent() {
		query name="local.qry"  {
			echo("delete from T"&suffix);
		} 
		query name="local.qry" listener=new query.QueryListener() {
			echo("select id from T"&suffix);
		} 
		assertEquals(3,qry.recordcount);
		assertEquals("abc",qry.columnList);
	}


	public void function testListenerBefore() {
		query name="local.qry"  {
			echo("delete from T"&suffix);
		} 
		query name="local.qry" listener={
			before=function (caller,args) {
		        args.sql="SELECT TABLE_NAME as abc FROM INFORMATION_SCHEMA.TABLES"; // change SQL
		        args.maxrows=2;
		        return arguments;
		    }
		} {

			echo("select id from T"&suffix);
		} 
		assertTrue(qry.recordcount<=2);
		assertEquals("abc",qry.columnList);
	}


	public void function testAsynSerialisation() {
		var ds=getPageContext().getApplicationContext().getDefDataSource();
		var cd=ds.getClassDefinition();
		var s=objectSave(cd);
		var o=ObjectLoad(s);
		var s=objectSave(ds);
		var o=ObjectLoad(s);
	}

	public void function testAsynCFC() {
		var tbl="QueryTestAsync";
		testAsyn(new query.QueryListener2(tbl),tbl,1);
	}


	public void function testAsynUDF() {
		var udf=function (caller,args,result,meta) {
				arguments.args.sql="insert into QueryTestAsync(id,i,dec) values('6',1,1.0)"; // change SQL
		        application.query_testAsynUDF=true;
				return arguments;
		};
		var tbl="QueryTestAsync";
		application.query_testAsynUDF=false;
		testAsyn(udf,tbl,0);
		assertTrue(application.query_testAsynUDF);
	}

	public void function testAsynStructUDF() {
		var sct={
			before=function (caller,args) {
				arguments.args.sql="insert into QueryTestAsync(id,i,dec) values('2',1,1.0)"; // change SQL
		        return arguments;
		    }
		    ,after=function (caller,args) {
				return arguments;
		    }
		};
		var tbl="QueryTestAsync";
		testAsyn(sct,tbl,2);
	}

	public void function testAsynStructUDFInclude() {

		include "query/inc.cfm";
		var tbl="QueryTestAsync";
		testAsyn(sctListener,tbl,3);
	}

	private void function testAsyn(listener,tbl, res) {
		createTable(tbl);
		try {
			query name="local.qry"  {
				echo("delete from "&tbl);
			} 

			query async=true name="local.qry" listener=listener {
				echo("insert into QueryTestAsync(id,i,dec) values('0',1,1.0)");
			} 

			sleep(500);
			query name="local.qry" {
				echo("select * from "&tbl);
			}
			assertTrue(qry.recordcount==1);
			assertEquals(res,qry.id);
		}
		finally {
			dropTable(tbl);
			end();
		}
	}

	public void function testListenerAfter() {
		query name="local.qry"  {
			echo("delete from T"&suffix);
		} 
		query name="local.qry" listener={
			after=function (caller,args,result,meta) {
		        var row=queryAddRow(result);
		        querySetCell(qry,"id","1234",row);
		    }
		} {

			echo("select id from T"&suffix);
		} 
		assertEquals(1,qry.recordcount);
		assertEquals("1234",qry.id);


		query name="local.qry" listener={
			after=function (caller,args,result,meta) {
				result=query(a:[1,2,3]);
				return arguments;
		    }
		} {

			echo("select id from T"&suffix);
		} 
		assertEquals(3,qry.recordcount);
		assertEquals("A",qry.columnlist);
		
	}

	public void function testListenerInsert() {
		query name="local.qry"  {
			echo("delete from T"&suffix);
		} 
		query name="local.qry" listener={
				before=function (caller,args) {
			        //dump(label:"before",var:arguments);
			        return arguments;
		    	},
				after=function (caller,args) {
			        //dump(label:"after",var:arguments);
			        arguments.result=query(a:[1,2,3]);
			        return arguments;
		    	}
			}

		 {
			echo("insert into T"&suffix&"(id,i,dec) values('2',1,1.0)");
		} 
		assertEquals(3,qry.recordcount);
		assertEquals("A",qry.columnlist);
		
	}

	private function createTable(name) {
		//dropTable(name);
		query  {
			echo("CREATE TABLE "&name&" (");
			echo("id int NOT NULL,");
			echo("i int,");		
			echo("dec DECIMAL");		
			echo(") ");
		}
	}

	private function dropTable(name) {
		try{
			query {
				echo("drop TABLE "&name);
			}
		}
		catch(local.e){}
	}

	function end() {
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
</cfscript>
