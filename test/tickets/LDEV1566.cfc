component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	variables.dir = getTempDirectory() & "ldev1566-" & createUUID() & "/";

	function beforeAll() {
		directoryCreate( variables.dir );
		fileWrite( variables.dir & "a.txt", "a" );
		fileWrite( variables.dir & "b.txt", "bb" );
		fileWrite( variables.dir & "c.log", "ccc" );
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1566 returntype array|struct for all tags and functions returning a query", function() {

			describe( "directoryList", function() {

				it( title="default is a query", body=function( currentSpec ) {
					var res = directoryList( path=variables.dir, listInfo="query" );
					expect( isQuery( res ) ).toBeTrue();
					expect( res.recordCount ).toBe( 3 );
				});

				it( title="returntype=query", body=function( currentSpec ) {
					var res = directoryList( variables.dir, false, "query", "", "name", "", "query" );
					expect( isQuery( res ) ).toBeTrue();
				});

				it( title="returntype=array returns an array of structs", body=function( currentSpec ) {
					var res = directoryList( path=variables.dir, listInfo="query", sort="name", returntype="array" );
					expect( isArray( res ) ).toBeTrue();
					expect( arrayLen( res ) ).toBe( 3 );
					expect( res[ 1 ].name ).toBe( "a.txt" );
					expect( res[ 3 ].name ).toBe( "c.log" );
					expect( res[ 2 ].size ).toBe( 2 );
				});

				it( title="returntype=struct with columnKey", body=function( currentSpec ) {
					var res = directoryList( path=variables.dir, listInfo="query", returntype="struct", columnKey="name" );
					expect( isStruct( res ) ).toBeTrue();
					expect( structCount( res ) ).toBe( 3 );
					expect( res[ "b.txt" ].size ).toBe( 2 );
					expect( res[ "b.txt" ].name ).toBe( "b.txt" );
				});

				it( title="returntype=struct without columnKey and more than one row throws", body=function( currentSpec ) {
					expect( function() {
						directoryList( path=variables.dir, listInfo="query", returntype="struct" );
					}).toThrow( type="application", regex="columnKey" );
				});

				it( title="returntype=struct with a single row and no columnKey returns the row", body=function( currentSpec ) {
					var res = directoryList( path=variables.dir, listInfo="query", filter="c.log", returntype="struct" );
					expect( isStruct( res ) ).toBeTrue();
					expect( res.name ).toBe( "c.log" );
				});

				it( title="unknown columnKey column throws", body=function( currentSpec ) {
					expect( function() {
						directoryList( path=variables.dir, listInfo="query", returntype="struct", columnKey="susi" );
					}).toThrow();
				});

				it( title="invalid returntype throws", body=function( currentSpec ) {
					expect( function() {
						directoryList( path=variables.dir, listInfo="query", returntype="xml" );
					}).toThrow( type="application", regex="returntype" );
				});

				it( title="returntype array/struct with listInfo=name throws", body=function( currentSpec ) {
					expect( function() {
						directoryList( path=variables.dir, listInfo="name", returntype="array" );
					}).toThrow( regex="listInfo" );
				});

			});

			describe( "cfdirectory", function() {

				it( title="default is a query", body=function( currentSpec ) {
					directory action="list" directory=variables.dir name="local.res";
					expect( isQuery( res ) ).toBeTrue();
				});

				it( title="returntype=array", body=function( currentSpec ) {
					directory action="list" directory=variables.dir name="local.res" sort="name" returntype="array";
					expect( isArray( res ) ).toBeTrue();
					expect( arrayLen( res ) ).toBe( 3 );
					expect( res[ 1 ].name ).toBe( "a.txt" );
				});

				it( title="returntype=struct", body=function( currentSpec ) {
					directory action="list" directory=variables.dir name="local.res" returntype="struct" columnKey="name";
					expect( isStruct( res ) ).toBeTrue();
					expect( res[ "c.log" ].size ).toBe( 3 );
				});

				it( title="invalid returntype throws", body=function( currentSpec ) {
					expect( function() {
						directory action="list" directory=variables.dir name="local.res" returntype="nope";
					}).toThrow( type="application", regex="returntype" );
				});

			});

			describe( "extensionList", function() {

				it( title="default is a query", body=function( currentSpec ) {
					expect( isQuery( extensionList() ) ).toBeTrue();
				});

				it( title="returntype=array", body=function( currentSpec ) {
					var q = extensionList();
					var res = extensionList( false, "array" );
					expect( isArray( res ) ).toBeTrue();
					expect( arrayLen( res ) ).toBe( q.recordCount );
					if ( q.recordCount ) expect( res[ 1 ] ).toHaveKey( "id" );
				});

				it( title="returntype=struct with columnKey", body=function( currentSpec ) {
					var q = extensionList();
					var res = extensionList( false, "struct", "id" );
					expect( isStruct( res ) ).toBeTrue();
					expect( structCount( res ) ).toBe( q.recordCount );
				});

				it( title="invalid returntype throws", body=function( currentSpec ) {
					expect( function() {
						extensionList( false, "xml" );
					}).toThrow( type="application", regex="returntype" );
				});

			});

			describe( "getMemoryUsage", function() {

				it( title="default is a query", body=function( currentSpec ) {
					expect( isQuery( getMemoryUsage() ) ).toBeTrue();
				});

				it( title="returntype=array", body=function( currentSpec ) {
					var res = getMemoryUsage( "", "array" );
					expect( isArray( res ) ).toBeTrue();
					expect( arrayLen( res ) ).toBeGT( 0 );
					expect( res[ 1 ] ).toHaveKey( "name" );
				});

				it( title="returntype=struct with columnKey", body=function( currentSpec ) {
					var q = getMemoryUsage();
					var res = getMemoryUsage( "", "struct", "name" );
					expect( isStruct( res ) ).toBeTrue();
					expect( res ).toHaveKey( q.name[ 1 ] );
				});

			});

			describe( "sslCertificateList", function() {

				it( title="default is a query", body=function( currentSpec ) {
					expect( isQuery( sslCertificateList() ) ).toBeTrue();
				});

				it( title="returntype=array", body=function( currentSpec ) {
					var q = sslCertificateList();
					var res = sslCertificateList( "", 443, "array" );
					expect( isArray( res ) ).toBeTrue();
					expect( arrayLen( res ) ).toBe( q.recordCount );
				});

				it( title="invalid returntype throws", body=function( currentSpec ) {
					expect( function() {
						sslCertificateList( "", 443, "xml" );
					}).toThrow( type="application", regex="returntype" );
				});

			});

			describe( "aiGetConnections", function() {

				it( title="default is a query", body=function( currentSpec ) {
					expect( isQuery( aiGetConnections() ) ).toBeTrue();
				});

				it( title="returntype=array", body=function( currentSpec ) {
					var q = aiGetConnections();
					var res = aiGetConnections( "array" );
					expect( isArray( res ) ).toBeTrue();
					expect( arrayLen( res ) ).toBe( q.recordCount );
				});

				it( title="returntype=struct with columnKey", body=function( currentSpec ) {
					var q = aiGetConnections();
					var res = aiGetConnections( "struct", "name" );
					expect( isStruct( res ) ).toBeTrue();
					expect( structCount( res ) ).toBe( q.recordCount );
				});

			});

			describe( "aiGetModels, mavenInfo and mavenImport", function() {

				it( title="mavenInfo with an invalid returntype throws", body=function( currentSpec ) {
					expect( function() {
						mavenInfo( "org.example", "nothing", "1.0", [], true, false, "xml" );
					}).toThrow( type="application", regex="returntype" );
				});

				it( title="mavenImport with an invalid returntype throws", body=function( currentSpec ) {
					expect( function() {
						mavenImport( variables.dir & "pom.xml", false, false, "xml" );
					}).toThrow( type="application", regex="returntype" );
				});

			});

			describe( "cfschedule list", function() {

				it( title="returntype=array", body=function( currentSpec ) {
					schedule action="list" result="local.res" returntype="array";
					expect( isArray( res ) ).toBeTrue();
				});

				it( title="default is a query", body=function( currentSpec ) {
					schedule action="list" result="local.res";
					expect( isQuery( res ) ).toBeTrue();
				});

			});

			describe( "cfdbinfo", function() {

				it( title="returntype=array and struct", skip=isNotSupported(), body=function( currentSpec ) {
					var ds = h2();
					queryExecute( "create table if not exists t1566 ( id int primary key, name varchar(10) )", {}, { datasource: ds } );
					dbinfo datasource=ds type="columns" table="T1566" name="local.q";
					dbinfo datasource=ds type="columns" table="T1566" name="local.arr" returntype="array";
					dbinfo datasource=ds type="columns" table="T1566" name="local.sct" returntype="struct" columnKey="COLUMN_NAME";
					expect( isQuery( q ) ).toBeTrue();
					expect( isArray( arr ) ).toBeTrue();
					expect( arrayLen( arr ) ).toBe( q.recordCount );
					expect( isStruct( sct ) ).toBeTrue();
					expect( sct ).toHaveKey( "ID" );
					expect( sct.NAME.COLUMN_NAME ).toBe( "NAME" );
				});

			});

		});
	}

	private boolean function isNotSupported() {
		try {
			h2();
			return false;
		}
		catch ( any e ) {
			return true;
		}
	}

	private struct function h2() {
		var ds = {
			class: "org.h2.Driver",
			bundleName: "org.lucee.h2",
			connectionString: "jdbc:h2:mem:ldev1566;MODE=MySQL"
		};
		queryExecute( "select 1", {}, { datasource: ds } );
		return ds;
	}

}
