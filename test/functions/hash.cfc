component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for hash()", function() {

			it(title = "Checking with hash()", body = function( currentSpec ) {

				assertEquals('B5AD53C085F0D402334689101351D842',"#'some string to hash'.hash()#");	
				assertEquals('B5AD53C085F0D402334689101351D842',"#hash('some string to hash')#");
			});		
		});	
	}
}