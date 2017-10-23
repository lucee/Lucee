component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path = "#getDirectoryFromPath(getCurrenttemplatepath())#LDEV1431\";
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV-1431", function() {
			it(title="checking getCurrentTemplatePath() in static function, calling via create Object", body = function( currentSpec ) {
				var obj = createObject("component", 'LDEV1431.test');
				expect(obj.testInstance()).toBe("#path#test.cfc");
			});

			it(title="checking getCurrentTemplatePath() in static function,  calling static function directly", body = function( currentSpec ) {
				expect(LDEV1431.Test::testStatic()).toBe("#path#test.cfc");
			});

			it(title="checking getCurrentTemplatePath() in static function, calling static function via another component", body = function( currentSpec ) {
				var obj2 = new LDEV1431.test2();
				expect(obj2.testInstance()).toBe("#path#test.cfc");
			});
		});
	}
}