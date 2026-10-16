component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		cfapplication(action='update', cgiReadOnly="true");
	}

	function afterAll(){
		cfapplication(action='update', cgiReadOnly="true");
	}

	function run( testResults, testBox ) {

		describe("Testcase for LDEV-3841 - cfapplication / cgiReadOnly", function() {
			it( title="cfapplication cgiReadOnly=false", body=function( currentSpec ) {
				expect( cgiReadOnlyTest( 1 ) ).toBe( "writable:1" );
			});
			it( title="cfapplication without setting cgiReadOnly", body=function( currentSpec ) {
				expect( cgiReadOnlyTest( 2 ) ).toBe( "readOnly:2" );
			});
			it( title="cfapplication cgiReadOnly=true", body=function( currentSpec ) {
				expect( cgiReadOnlyTest( 3 ) ).toBe( "ReadOnly:3" );
			});
		});

		describe("Testcase for LDEV-3841 - application.cfm / cgiReadOnly", function() {
			it(title = "checking via application.cfc, cgiReadOnly=true", body = function( currentSpec ) {
				expect(function(){
					var result = _InternalRequest(
						template : "#createURI("LDEV3841")#/cfm/index.cfm",
						url: {
							cgiReadonly: true
						}
					);
				}).toThrow();
			});

			it(title = "checking via application.cfm, cgiReadOnly=false", body = function( currentSpec ) {
				var result = _InternalRequest(
					template : "#createURI("LDEV3841")#/cfm/index.cfm",
					url: {
						cgiReadonly: false
					}
				);
				var _cgi = deserializeJson( result.filecontent );
				expect( _cgi ).toHaveKey( "Readonly" );
				expect( _cgi.readOnly ).toBe( "false" );
			});
		});

		describe("Testcase for LDEV-3841 - application.cfc / cgiReadOnly", function() {
			it(title = "checking via application.cfc, cgiReadOnly=true", body = function( currentSpec ) {
				expect(function(){
					var result = _InternalRequest(
						template : "#createURI("LDEV3841")#/cfc/index.cfm",
						url: {
							cgiReadonly: true
						}
					);
				}).toThrow();
			});

			it(title = "checking via application.cfc, cgiReadOnly=false", body = function( currentSpec ) {
				var result = _InternalRequest(
					template : "#createURI("LDEV3841")#/cfc/index.cfm",
					url: {
						cgiReadonly: false
					}
				);
				var _cgi = deserializeJson( result.filecontent );
				expect( _cgi ).toHaveKey( "Readonly" );
				expect( _cgi.readOnly ).toBe( "false" );
			});

			// only works when set in CFconfig.json
			it(title = "checking via application.cfc, cgiReadOnly=false constructor", skip=true, body = function( currentSpec ) {
				var result = _InternalRequest(
					template : "#createURI("LDEV3841")#/cfc/index.cfm",
					url: {
						cgiReadonly: false,
						constructor: true
					}
				);
				var _cgi = deserializeJson( result.filecontent );
				expect( _cgi ).toHaveKey( "constructor" );
				expect( _cgi.constructor ).toBe( "true" );
				expect( _cgi ).toHaveKey( "Readonly" );
				expect( _cgi.readOnly ).toBe( "false" );
			});
		});
	}

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}

	private string function cgiReadOnlyTest( required numeric scene ) {
		try {
			if (scene == 1) cfapplication(name="LDEV-3841", action='update', cgiReadOnly="false");
			if (scene == 2) cfapplication(action='update');
			if (scene == 3) cfapplication(action='update', cgiReadOnly="true");
			CGI.foo = "writable:#scene#";
			return CGI.foo;
		}
		catch(any e) {
			// systemOutput(e.message, true);
			return "readonly:#scene#";
		}
	}
}
