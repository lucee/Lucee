component extends="org.lucee.cfml.test.LuceeTestCase" labels="static" {
	function beforeAll(){
		variables.path = replace( "#getDirectoryFromPath(getCurrenttemplatepath())#LDEV1431\", "/\", "/", "all" );
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV-1431", function() {
			it(title="checking getCurrentTemplatePath() in static function, calling via create Object", body = function( currentSpec ) {
				var obj = createObject("component", 'LDEV1431.test');
				expect( replace( obj.testInstance() , "/\", "/", "all" ) ).toBe("#path#test.cfc");
			});

			it(title="checking getCurrentTemplatePath() in static function,  calling static function directly", body = function( currentSpec ) {
				expect(  replace( LDEV1431.Test::testStatic() , "/\", "/", "all" ) ).toBe("#path#test.cfc");
			});

			it(title="checking getCurrentTemplatePath() in static function, calling static function via another component", body = function( currentSpec ) {
				var obj2 = new LDEV1431.test2();
				expect( replace( obj2.testInstance(), "/\", "/", "all" ) ).toBe("#path#test.cfc");
			});
		});
	}
}