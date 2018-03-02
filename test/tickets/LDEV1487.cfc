component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1487", body=function() {
			it(title="test UDF", body = function( currentSpec ) {
				
				var name = 'F' & createUniqueId() ;
		        var fileName = "LDEV1487/"&name & '.cfm';
		        fileWrite( fileName, '<cfscript> function abc() { return createObject("MyCFC"); } </cfscript>' );
		        include fileName;
		        fileDelete( fileName );
		        abc();

			});

			it(title="test UDF metadata", body = function( currentSpec ) {
				
				var name = 'F' & createUniqueId() ;
		        var fileName = "LDEV1487/"&name & '.cfm';
		        fileWrite( fileName, '<cfscript> function abc() { return createObject("MyCFC"); } </cfscript>' );
		        include fileName;
		        fileDelete( fileName );
		        getMetadata(abc);

			});

			it(title="test component", body = function( currentSpec ) {
				
				var name = 'C' & createUniqueId() ;
		        var fileName = "LDEV1487/"&name & '.cfc';
		        var cfcName = "LDEV1487."&name ;
		        fileWrite( fileName, 'component { function foo() { return createObject("MyCFC"); } }' );
		        test =  createObject(cfcName);
		        fileDelete( fileName );
		        test.foo();
			});

			it(title="test component metadata", body = function( currentSpec ) {
				
				var name = 'C' & createUniqueId() ;
		        var fileName = "LDEV1487/"&name & '.cfc';
		        var cfcName = "LDEV1487."&name ;
		        fileWrite( fileName, 'component { function foo() { return createObject("MyCFC"); } }' );
		        test =  createObject(cfcName);
		        fileDelete( fileName );
		        getMetadata(test);
			});
		});
	}
}
