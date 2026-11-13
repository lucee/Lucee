component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function run( testResults, testBox ) {

		describe( "LDEV-6208 isWithinTransaction() with ORM", function() {

			it( title="returns false when ORM is enabled but no ORM session has been opened", skip=noOrm(), body=function( currentSpec ) {
				var r = request( "orm/noSession.cfm" );
				expect( r.before ).toBeFalse();
			} );

			it( title="returns true inside a transaction {} block and false after it, with ORM enabled", skip=noOrm(), body=function( currentSpec ) {
				var r = request( "orm/cftransaction.cfm" );
				expect( r.before ).toBeFalse();
				expect( r.inside ).toBeTrue();
				expect( r.insideAfterOrm ).toBeTrue();
				expect( r.after ).toBeFalse();
			} );

			it( title="returns true inside a native ormGetSession().beginTransaction() transaction", skip=noOrm(), body=function( currentSpec ) {
				var r = request( "orm/native.cfm" );
				expect( r.before ).toBeFalse();
				expect( r.insideCommit ).toBeTrue();
				expect( r.afterCommit ).toBeFalse();
				expect( r.insideRollback ).toBeTrue();
				expect( r.afterRollback ).toBeFalse();
			} );

		} );

		describe( "LDEV-6208 isWithinTransaction() without ORM", function() {

			it( title="behaves as before for transaction {} blocks", body=function( currentSpec ) {
				var r = request( "noorm/cftransaction.cfm" );
				expect( r.before ).toBeFalse();
				expect( r.inside ).toBeTrue();
				expect( r.insideAfterQuery ).toBeTrue();
				expect( r.after ).toBeFalse();
				expect( r.insideRollbackTx ).toBeTrue();
				expect( r.afterRollback ).toBeFalse();
			} );

		} );

	}

	private boolean function noOrm() {
		return ( structCount( server.getTestService( "orm" ) ) eq 0 );
	}

	private struct function request( required string template ) {
		var result = _InternalRequest( template: createURI( "LDEV6208/" & arguments.template ) );
		expect( result.status ).toBe( 200, result.filecontent );
		return deserializeJSON( trim( result.filecontent ) );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
