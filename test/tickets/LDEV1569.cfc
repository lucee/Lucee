component extends="org.lucee.cfml.test.LuceeTestCase"  labels="orm" {
	function run(  testResults , testBox ) {
		describe( title="Test suite for LDEV-1569",  skip=(noOrm() || checkMySqlEnvVarsAvailable()), body=function() {
			it(title="checking SerializeJSON() mysql query with NULL value",
					skip=(noOrm() || checkMySqlEnvVarsAvailable()),
					body = function( currentSpec ) {
				var uri=createURI("LDEV1569/query.cfm");
				var result = _InternalRequest(
					template:uri,
					url:{
						nullSupport: false
					}
				);
				expect(result.filecontent.trim()).toBe('[{"ID":1,"Name":""}]'); // nulls are an empty string, unless fullnullsupport
			});

			it(title="checking SerializeJSON() mysql query with NULL value, nullSupport=true",
					skip=(noOrm() || checkMySqlEnvVarsAvailable()),
					body = function( currentSpec ) {
				var uri=createURI("LDEV1569/query.cfm");
				var result = _InternalRequest(
					template:uri,
					url:{
						nullSupport: true
					}
				);
				expect(result.filecontent.trim()).toBe('[{"ID":1,"Name":null}]');
			});

			it(title="checking SerializeJSON() for QoQ with NULL value", body = function( currentSpec ) {
				var uri=createURI("LDEV1569/qoq.cfm");
				var result = _InternalRequest(
					template:uri,
					url:{
						nullSupport: false
					}
				);
				expect(result.filecontent.trim()).toBe('[{"ID":3,"Name":""}]');  // nulls are an empty string, unless fullnullsupport
			});

			it(title="checking SerializeJSON() for QoQ with NULL value, nullSupport=true", body = function( currentSpec ) {
				var uri=createURI("LDEV1569/qoq.cfm");
				var result = _InternalRequest(
					template:uri,
					url:{
						nullSupport: true
					}
				);
				expect(result.filecontent.trim()).toBe('[{"ID":3,"Name":null}]');
			});
		});

		describe( title="Test suite for LDEV-1569 (ORM entity on H2)", skip=noOrm(), body=function() {

			it( title="serializeJSON( entityNew ) includes null persistent property with full null support", body=function( currentSpec ) {
				expect( ormRequest( "entityNew", true ) ).toBe( '{"id":2,"name":null}' );
			});

			it( title="serializeJSON( entityLoadByPK ) includes null persistent property with full null support", body=function( currentSpec ) {
				expect( ormRequest( "entityLoad", true ) ).toBe( '{"id":3,"name":null}' );
			});

			it( title="serializeJSON( entityNew ) omits null persistent property without full null support (unchanged)", body=function( currentSpec ) {
				expect( ormRequest( "entityNew", false ) ).toBe( '{"id":2}' );
			});

			it( title="serializeJSON( entityLoadByPK ) omits null persistent property without full null support (unchanged)", body=function( currentSpec ) {
				expect( ormRequest( "entityLoad", false ) ).toBe( '{"id":3}' );
			});

			it( title="serializeJSON() of a non persistent component still omits null properties with full null support", body=function( currentSpec ) {
				expect( ormRequest( "plain", true ) ).toBe( '{"id":4}' );
			});

			it( title="serializeJSON() of a non persistent component still omits null properties without full null support", body=function( currentSpec ) {
				expect( ormRequest( "plain", false ) ).toBe( '{"id":4}' );
			});
		});
	}

	private string function ormRequest( required string scene, required boolean nullSupport ) {
		var result = _InternalRequest(
			template: createURI( "LDEV1569/ormh2/orm.cfm" ),
			url: { scene: arguments.scene, nullSupport: arguments.nullSupport }
		);
		return trim( result.filecontent );
	}

	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function checkMySqlEnvVarsAvailable() {
		// getting the credentials from the environment variables
		var mySQL = server.getDatasource("mysql");
		return structIsEmpty(mySQL);
	}

	private function noOrm() {
		return ( structCount( server.getTestService("orm") ) eq 0 );
	}
}
