<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="oracle"	{
	// ZAC is that version correct? i simply took what comes from 6.0
	
	//public function afterTests(){}
	
	
	variables.PROCEDURE="testOraclePro";
	variables.TABLE="testOracleTbl";

	public function setUp(){
		variables.has=defineDatasource();
	}

	private boolean function defineDatasource(){
		var orcl = server.getDatasource("oracle");
		if(orcl.count()==0) return false;

		// otherwise we get the following on travis ORA-00604: error occurred at recursive SQL level 1 / ORA-01882: timezone region not found
		var tz=getTimeZone();
		//var d1=tz.getDefault();
		tz.setDefault(tz);
		//throw d1&":"&tz.getDefault();

		application action="update" datasource="#orcl#";
	
		return true;
	}

	private boolean function defineDatasource(){
		var orcl = server.getDatasource("oracle");
		if(orcl.count()==0) return false;

		// otherwise we get the following on travis ORA-00604: error occurred at recursive SQL level 1 / ORA-01882: timezone region not found
		var tz=getTimeZone();
		//var d1=tz.getDefault();
		tz.setDefault(tz);
		//throw d1&":"&tz.getDefault();

		application action="update" datasource="#orcl#";
	
		return true;
	}


	public void function testStoredProcIn(){
		if(!variables.has) return;
		echo(now()&"start:testStoredProcIn
			");
		query name="qry" {
			echo("
CREATE OR REPLACE PROCEDURE procOneINParameter(param1 IN VARCHAR2)
IS
BEGIN
  DBMS_OUTPUT.PUT_LINE('in:' || param1);
END;
			");
		}
		echo(now()&"----testStoredProcIn
			");
		storedproc procedure="procOneINParameter" {
			procparam type="in" value="input1" cfsqltype="cf_sql_varchar";
		}
		
		echo(now()&"end:testStoredProcIn
			");
	}

	public void function testStoredProcOut(){
		if(!variables.has) return;
		
		query name="qry" {
			echo("
CREATE OR REPLACE PROCEDURE procOneOUTParameter(outParam1 OUT VARCHAR2)
IS
BEGIN
  outParam1 := 'out';
END;
			");
		}

		storedproc procedure="procOneOUTParameter" {
			procparam type="out" variable="res" cfsqltype="cf_sql_varchar";
		}

		assertEquals('out',res);
		
	}

	public void function testStoredProcInOut(){
		if(!variables.has) return;
		
		query name="qry" {
			echo("
CREATE OR REPLACE PROCEDURE procOneINOUTParameter(genericParam IN OUT VARCHAR2)
IS
BEGIN
  genericParam := 'out:' || genericParam;
END;
			");
		}
		storedproc procedure="procOneINOUTParameter" {
			procparam type="inout" variable="res" value="in" cfsqltype="cf_sql_varchar";
		}
		assertEquals('out:in',res);
		
	}

	// LDEV-2543
	public void function testStoredProcXmlType() localmode=true skip=true {
		if(!variables.has) return;

		```
		<cfquery name="qry">
			CREATE OR REPLACE PROCEDURE SP_XMLTEST_BUG(
				cur_XMLOut OUT sys_refcursor
			) AS
				BEGIN
				OPEN cur_XMLOut FOR
					SELECT XMLSerialize(DOCUMENT XMLELEMENT("SystemDate",to_char(sysdate,'YYYYMMDD'))) AS xml
					FROM DUAL;
				END;
		</cfquery>
		```
		storedproc procedure="SP_XMLTEST_BUG" {
			procresult name="qTest";
		}
		expect ( qTest.xml ).toInclude( "<SystemDate>" ); // works due to XMLSerialize
		
		```
		<cfquery name="qry">
			CREATE OR REPLACE PROCEDURE SP_XMLTEST_BUG( cur_XMLOut OUT sys_refcursor) AS
				BEGIN
				  OPEN cur_XMLOut FOR
					SELECT XMLELEMENT("SystemDate",to_char(sysdate,'YYYYMMDD')) AS xml
					FROM DUAL;
				END;
		</cfquery>
		```
		storedproc procedure="SP_XMLTEST_BUG" {
			procresult name="qTest";
		}
		//systemOutput( "-----222-----[" & qTest.toString() & "]", true );
		expect ( qTest ).toInclude( "<SystemDate>" ); // fails, an empty string is returned
	}

	public void function testConnection(){
		if(!variables.has) return;
		
		echo(now()&"start:testConnection
			");
		query name="local.qry" {
			echo("SELECT table_name FROM user_tables where table_name like 'MAP_%'");
		}
		echo(now()&"endpublic:testConnection
			");
		//assertEquals("AA",qry.a);
		
	}

	private function cleanupInsertTable() localmode=true{
		query name="exists" params={id: "INSERT_TEST"}{
			echo("SELECT table_name FROM user_tables WHERE table_name = :id");
		}
		//systemoutput(exists, true);
		if ( exists.recordcount eq 1 ){
			//systemoutput("dropping insert_test", true);
			query {
				echo( "DROP TABLE insert_test" );
			}
		}

		query name="exists" params={id: "INSERT_SEQ" } {
			echo( "SELECT sequence_name FROM user_sequences WHERE sequence_name = :id" );
		}
		//systemoutput(exists, true);
		if ( exists.recordcount eq 1 ){
			//systemoutput("dropping insert_seq", true);
			query {
				echo ( "DROP SEQUENCE insert_seq" );
			}
		}
	}

	public void function testInsertTable() localmode=true{
		if ( !variables.has ) return;

		//systemOutput("", true); // just a new line
		cleanupInsertTable();
		query {
			echo ( "CREATE TABLE insert_test ( id number(10) )" );
		}
		query {
			echo ( "CREATE SEQUENCE insert_seq START WITH 3" ); // start with 3, so the test is a little more testing!
		}
	
		query result="result" {
			echo("INSERT INTO insert_test ( id ) VALUES ( insert_seq.nextval )");
		}; // TODO this needs to have a column list passed so it returns the ID value

		query name="q" {
			echo( "SELECT id FROM insert_test" );
		};
		//systemOutput( q, true );
		//systemOutput( result, true );

		query name="seq" {
			echo ( "SELECT insert_seq.currval AS id FROM dual" );
		}
		expect( seq.id ).toBe( 3 );
		expect( seq.id ).toBe( q.id );
		// expect( result.generatedKey ).toBe( seq.id );  // TODO returns oracle.sql.ROWID instead

		// systemOutput( seq, true );

		cleanupInsertTable();
	}
} 
</cfscript>