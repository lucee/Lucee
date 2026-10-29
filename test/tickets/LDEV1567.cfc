component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	variables.dir = getTempDirectory() & "ldev1567-" & createUUID() & "/";

	function beforeAll() {
		directoryCreate( variables.dir );
		directoryCreate( variables.dir & "sub" );
		fileWrite( variables.dir & "a.txt", "a" );
		fileWrite( variables.dir & "b.txt", "bb" );
		fileWrite( variables.dir & "sub/a.txt", "ccc" );
		variables.qry = queryNew( "country,city,name", "varchar,varchar,varchar", [
			[ "CH", "Bern", "a" ],
			[ "CH", "Zurich", "b" ],
			[ "DE", "Berlin", "c" ],
			[ "CH", "Bern", "d" ]
		] );
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1567 columnKey accepts a comma separated list and creates nested structs", function() {

			it( title="QoQ, single key is unchanged (last row wins)", body=function( currentSpec ) {
				var q = variables.qry;
				var res = queryExecute( "SELECT * FROM q", {}, { dbtype: "query", returntype: "struct", columnKey: "country" } );
				expect( structCount( res ) ).toBe( 2 );
				expect( res.CH.name ).toBe( "d" );
				expect( res.DE.name ).toBe( "c" );
			});

			it( title="QoQ, two keys create two levels", body=function( currentSpec ) {
				var q = variables.qry;
				var res = queryExecute( "SELECT * FROM q", {}, { dbtype: "query", returntype: "struct", columnKey: "country,city" } );
				expect( isStruct( res ) ).toBeTrue();
				expect( structCount( res ) ).toBe( 2 );
				expect( structCount( res.CH ) ).toBe( 2 );
				expect( res.CH.Zurich.name ).toBe( "b" );
				expect( res.DE.Berlin.name ).toBe( "c" );
				expect( res.CH.Zurich.country ).toBe( "CH" );
			});

			it( title="QoQ, duplicate key combination: the last row wins", body=function( currentSpec ) {
				var q = variables.qry;
				var res = queryExecute( "SELECT * FROM q", {}, { dbtype: "query", returntype: "struct", columnKey: "country,city" } );
				expect( res.CH.Bern.name ).toBe( "d" );
			});

			it( title="QoQ, three keys and whitespace around the names", body=function( currentSpec ) {
				var q = variables.qry;
				var res = queryExecute( "SELECT * FROM q", {}, { dbtype: "query", returntype: "struct", columnKey: " country , city , name " } );
				expect( res.CH.Bern.d.name ).toBe( "d" );
				expect( res.CH.Bern.a.name ).toBe( "a" );
				expect( res.DE.Berlin.c.country ).toBe( "DE" );
			});

			it( title="QoQ, a missing key column throws", body=function( currentSpec ) {
				var q = variables.qry;
				expect( function() {
					queryExecute( "SELECT * FROM q", {}, { dbtype: "query", returntype: "struct", columnKey: "country,nope" } );
				}).toThrow();
			});

			it( title="the order of the structs follows the order of the rows", body=function( currentSpec ) {
				var q = variables.qry;
				var res = queryExecute( "SELECT * FROM q", {}, { dbtype: "query", returntype: "struct", columnKey: "country,city" } );
				expect( structKeyList( res ) ).toBe( "CH,DE" );
				expect( structKeyList( res.CH ) ).toBe( "Bern,Zurich" );
			});

			it( title="cfquery tag with dbtype query", body=function( currentSpec ) {
				var q = variables.qry;
				query name="local.res" dbtype="query" returntype="struct" columnKey="country,city" {
					echo( "SELECT * FROM q" );
				}
				expect( res.CH.Bern.name ).toBe( "d" );
				expect( res.CH.Zurich.name ).toBe( "b" );
			});

			it( title="datasource", body=function( currentSpec ) {
				var ds = server.getDatasource( "h2", server._getTempDir( "LDEV1567" ) );
				queryExecute( "CREATE TABLE t1567 ( country varchar(10), city varchar(10), name varchar(10) )", {}, { datasource: ds } );
				try {
					queryExecute( "INSERT INTO t1567 VALUES ( 'CH', 'Bern', 'a' ), ( 'CH', 'Zurich', 'b' ), ( 'DE', 'Berlin', 'c' ), ( 'CH', 'Bern', 'd' )", {}, { datasource: ds } );
					var res = queryExecute( "SELECT * FROM t1567 ORDER BY name", {}, { datasource: ds, returntype: "struct", columnKey: "country,city" } );
					expect( structCount( res ) ).toBe( 2 );
					expect( res.CH.Bern.name ).toBe( "d" );
					expect( res.CH.Zurich.name ).toBe( "b" );
					expect( res.DE.Berlin.name ).toBe( "c" );

					// single key is unchanged
					res = queryExecute( "SELECT * FROM t1567 ORDER BY name", {}, { datasource: ds, returntype: "struct", columnKey: "name" } );
					expect( structCount( res ) ).toBe( 4 );
					expect( res.d.city ).toBe( "Bern" );

					// missing column
					expect( function() {
						queryExecute( "SELECT * FROM t1567", {}, { datasource: ds, returntype: "struct", columnKey: "country,nope" } );
					}).toThrow();

					// queryLazy
					var collected = {};
					queryLazy( sql="SELECT * FROM t1567 ORDER BY name", listener=function( rows ) {
						structAppend( collected, rows );
					}, options={ datasource: ds, returntype: "struct", columnKey: "country,city", blockfactor: 10 } );
					expect( collected.CH.Bern.name ).toBe( "d" );
					expect( collected.DE.Berlin.name ).toBe( "c" );
				}
				finally {
					queryExecute( "DROP TABLE t1567", {}, { datasource: ds } );
				}
			});

			it( title="directoryList", body=function( currentSpec ) {
				var res = directoryList( path=variables.dir, recurse=true, listInfo="query", returntype="struct", columnKey="directory,name" );
				expect( isStruct( res ) ).toBeTrue();
				expect( structCount( res ) ).toBe( 2 );
				var found = 0;
				for ( var dirKey in res ) {
					if ( structKeyExists( res[ dirKey ], "a.txt" ) ) found++;
				}
				expect( found ).toBe( 2 );
			});

			it( title="cfdirectory", body=function( currentSpec ) {
				directory action="list" directory=variables.dir recurse=true name="local.res" returntype="struct" columnKey="type,name";
				expect( structCount( res ) ).toBe( 2 );
				expect( res.File[ "b.txt" ].size ).toBe( 2 );
				expect( res.Dir.sub.name ).toBe( "sub" );
			});
		});
	}
}
