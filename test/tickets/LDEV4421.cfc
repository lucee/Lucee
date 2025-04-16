component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "testcase for LDEV-4421", function() {
			it(title = "check null value with component metadata keys in getcomponentmetadata()", body = function( currentSpec ) {
				expect( function () { 
					var metaData = getComponentMetadata("Administrator");
					for ( var thisKey in metaData ) {
						variables[ thisKey ] =  metaData[thisKey] ;
					}
				}).notToThrow();
			});	
		});
	}

}