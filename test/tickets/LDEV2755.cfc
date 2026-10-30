component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, testBox ) {

		describe( "LDEV-2755 QoQ HSQLDB fallback with reserved words as column names", function() {

			beforeEach( function() {
				// a column list full of SQL reserved words
				variables.t1 = queryNew( "id,unique,select,and,order,by,table,type,distinct,name" );
				queryAddRow( t1, 2 );
				querySetCell( t1, "id", 1, 1 );
				querySetCell( t1, "unique", "u1", 1 );
				querySetCell( t1, "select", "s1", 1 );
				querySetCell( t1, "name", "first", 1 );
				querySetCell( t1, "id", 2, 2 );
				querySetCell( t1, "unique", "u2", 2 );
				querySetCell( t1, "select", "s2", 2 );
				querySetCell( t1, "name", "second", 2 );
				variables.t2 = queryNew( "id,label", "integer,varchar" );
				queryAddRow( t2, 2 );
				querySetCell( t2, "id", 1, 1 );
				querySetCell( t2, "label", "one", 1 );
				querySetCell( t2, "id", 2, 2 );
				querySetCell( t2, "label", "two", 2 );
			} );

			it( "joins a query that has reserved words as column names, without selecting them", function() {
				// a join can't be handled by the native engine, so this falls back to HSQLDB
				var q = queryExecute(
					sql = "SELECT t1.id, t2.label FROM t1, t2 WHERE t1.id = t2.id ORDER BY t1.id",
					options = { dbtype: "query" }
				);
				expect( q.recordcount ).toBe( 2 );
				expect( q.label[ 1 ] ).toBe( "one" );
				expect( q.label[ 2 ] ).toBe( "two" );
			} );

			it( "joins a query that has reserved words as column names, run twice to cover the column usage cache", function() {
				loop times=2 {
					var q = queryExecute(
						sql = "SELECT t1.name, t2.label FROM t1, t2 WHERE t1.id = t2.id ORDER BY t1.id",
						options = { dbtype: "query" }
					);
					expect( q.recordcount ).toBe( 2 );
					expect( q.name[ 2 ] ).toBe( "second" );
				}
			} );

			it( "joins with a query that has a unique column (the cfschedule case)", function() {
				var tasks = queryNew( "task,unique", "varchar,boolean", [ [ "a", false ], [ "b", true ] ] );
				var x = queryNew( "task", "varchar", [ [ "b" ] ] );
				var q = queryExecute(
					sql = "SELECT task FROM tasks, x WHERE tasks.task = x.task",
					options = { dbtype: "query" }
				);
				expect( q.recordcount ).toBe( 1 );
				expect( q.task ).toBe( "b" );
			} );

			it( "selects all columns of a table with reserved word columns in a join", function() {
				var q = queryExecute(
					sql = "SELECT t1.* FROM t1, t2 WHERE t1.id = t2.id ORDER BY t1.id",
					options = { dbtype: "query" }
				);
				expect( q.recordcount ).toBe( 2 );
				expect( q.unique[ 1 ] ).toBe( "u1" );
				expect( q.select[ 2 ] ).toBe( "s2" );
			} );

			it( "selects a column that is a reserved word in a join", function() {
				var q = queryExecute(
					sql = "SELECT t1.unique, t1.select FROM t1, t2 WHERE t1.id = t2.id ORDER BY t1.id",
					options = { dbtype: "query" }
				);
				expect( q.recordcount ).toBe( 2 );
				expect( q.unique[ 1 ] ).toBe( "u1" );
				expect( q.select[ 2 ] ).toBe( "s2" );
			} );

			it( "selects bracketed reserved word columns in a join", function() {
				var q = queryExecute(
					sql = "SELECT t1.[unique], t1.[select] FROM t1, t2 WHERE t1.id = t2.id ORDER BY t1.id",
					options = { dbtype: "query" }
				);
				expect( q.recordcount ).toBe( 2 );
				expect( q.unique[ 2 ] ).toBe( "u2" );
				expect( q.select[ 1 ] ).toBe( "s1" );
			} );

			it( "filters on a reserved word column in a join", function() {
				var q = queryExecute(
					sql = "SELECT t2.label FROM t1, t2 WHERE t1.id = t2.id AND t1.unique = 'u2'",
					options = { dbtype: "query" }
				);
				expect( q.recordcount ).toBe( 1 );
				expect( q.label ).toBe( "two" );
			} );

			it( "references reserved word columns case insensitive in a join", function() {
				var q = queryExecute(
					sql = "SELECT T1.Unique, t1.SELECT FROM t1, t2 WHERE T1.ID = t2.Id ORDER BY t1.id",
					options = { dbtype: "query" }
				);
				expect( q.recordcount ).toBe( 2 );
				expect( q.unique[ 1 ] ).toBe( "u1" );
				expect( q.select[ 2 ] ).toBe( "s2" );
			} );

			it( "joins a query that is named like a reserved word", function() {
				var unique = t1;
				var q = queryExecute(
					sql = "SELECT unique.id, unique.name, t2.label FROM unique, t2 WHERE unique.id = t2.id ORDER BY unique.id",
					options = { dbtype: "query" }
				);
				expect( q.recordcount ).toBe( 2 );
				expect( q.name[ 1 ] ).toBe( "first" );
				expect( q.label[ 2 ] ).toBe( "two" );
			} );

			it( "joins mixed case column names with a column that is a reserved word", function() {
				var mixed = queryNew( "Id,Unique,MixedCase", "integer,varchar,varchar", [ [ 1, "u", "m" ] ] );
				var q = queryExecute(
					sql = "SELECT mixed.MixedCase, mixed.unique, t2.label FROM mixed, t2 WHERE mixed.id = t2.id",
					options = { dbtype: "query" }
				);
				expect( q.recordcount ).toBe( 1 );
				expect( q.mixedcase ).toBe( "m" );
				expect( q.unique ).toBe( "u" );
				expect( q.label ).toBe( "one" );
			} );

		} );

	}

}
