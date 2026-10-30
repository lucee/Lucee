component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function run( testResults, testBox ) {

		describe( title="LDEV-3657 transaction savepoints with ORM code", skip=( noOrm() || preLDEV3657() ), body=function() {

			it( title="transactionSetSavepoint() after ORM code", body=function( currentSpec ) {
				var r = scenario( "unnamed" );
				expect( r.names ).toBe( "John" );
			});

			it( title="transactionSetSavepoint( name ) after ORM code", body=function( currentSpec ) {
				var r = scenario( "named" );
				expect( r.names ).toBe( "John" );
			});

			it( title="savepoint after ORM read only code", body=function( currentSpec ) {
				var r = scenario( "loadOnly" );
				expect( r.names ).toBe( "" );
			});

			it( title="save A, savepoint, save B, rollback to savepoint, commit only keeps A", body=function( currentSpec ) {
				var r = scenario( "rollbackToSavepoint" );
				expect( r.names ).toBe( "A" );
			});

			it( title="rollback to savepoint undoes ORM changes that were already flushed", body=function( currentSpec ) {
				var r = scenario( "rollbackToSavepointFlushed" );
				expect( r.insideBeforeRollback ).toBe( "A,B" );
				expect( r.insideAfterRollback ).toBe( "A" );
				expect( r.names ).toBe( "A" );
			});

			it( title="savepoint set before ORM code, rollback to it, ORM save afterwards is still committed", body=function( currentSpec ) {
				var r = scenario( "savepointBeforeOrm" );
				expect( r.names ).toBe( "after,keep" );
			});

			it( title="ORM saves after a rollback to a savepoint survive another savepoint rollback", body=function( currentSpec ) {
				var r = scenario( "twoRollbacks" );
				expect( r.names ).toBe( "A,C,E" );
			});

			it( title="full rollback after a savepoint rolls back all ORM changes", body=function( currentSpec ) {
				var r = scenario( "fullRollbackAfterSavepoint" );
				expect( r.names ).toBe( "" );
			});

			it( title="entities become detached after a rollback to a savepoint", body=function( currentSpec ) {
				var r = scenario( "heldEntityDetached" );
				expect( r.insideBeforeRollback ).toBe( "modified" );
				expect( r.insideAfterRollback ).toBe( "original" );
				expect( r.contained ).toBeFalse();
				expect( r.names ).toBe( "original" );
			});

			it( title="rollback to an unknown savepoint still throws", body=function( currentSpec ) {
				var r = scenario( "unknownSavepoint" );
				expect( r.error ).toInclude( "doesNotExist" );
				expect( r.names ).toBe( "A" );
			});

			it( title="an error while flushing for the savepoint is not swallowed", body=function( currentSpec ) {
				var r = scenario( "flushFailure" );
				expect( r.error ).toInclude( "NAME" );
			});

		});

		describe( title="LDEV-3657 transaction savepoints with ORM code and a named datasource", skip=( noOrm() || preLDEV3657() ), body=function() {

			it( title="save A, savepoint, save B, rollback to savepoint, commit only keeps A", body=function( currentSpec ) {
				var r = scenario( "rollbackToSavepoint", true );
				expect( r.names ).toBe( "A" );
			});

			it( title="entities become detached after a rollback to a savepoint", body=function( currentSpec ) {
				var r = scenario( "heldEntityDetached", true );
				expect( r.insideAfterRollback ).toBe( "original" );
				expect( r.contained ).toBeFalse();
				expect( r.names ).toBe( "original" );
			});

		});
	}

	private struct function scenario( required string name, boolean named=false ) {
		var uri = createURI( "LDEV3657/index.cfm" );
		var result = _InternalRequest( template: uri, url: { scenario: arguments.name, datasource: arguments.named ? "named" : "default" } );
		if ( result.status != 200 ) fail( result.fileContent );
		return deserializeJSON( trim( result.fileContent ) );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

	private boolean function noOrm() {
		return ( structCount( server.getTestService( "orm" ) ) eq 0 );
	}

	// savepoint support with ORM was added to core in 7.1 (LDEV-3657)
	private boolean function preLDEV3657() {
		return getVersion().minorFloat < 7.1;
	}

	private struct function getVersion() {
		var arr = listToArray( server.lucee.version, "." );
		return { major: arr[ 1 ], minor: arr[ 2 ], minorFloat: ( arr[ 1 ] & "." & arr[ 2 ] ) + 0 };
	}

}
