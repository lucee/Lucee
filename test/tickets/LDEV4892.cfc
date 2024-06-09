component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4892 - Invalid struct shorthand syntax", function() {
			it( title="check syntax", body=function( currentSpec ) {
				// https://github.com/coldbox-modules/cbproxies/blob/development/models/Optional.cfc#L252
				function getNativeType( results ){
					if ( isNull( arguments.results ) ) {
						return;
					}
			
					var className  = arguments.results.getClass().getName();
					var isEntrySet = isInstanceOf( arguments.results, "java.util.Map$Entry" ) OR isInstanceOf(
						arguments.results,
						"java.util.HashMap$Node"
					);
			
					if ( isEntrySet ) {
						return { "#arguments.results.getKey()#" : arguments.results.getValue() };
					}
			
					return arguments.results;
				}
			});
		}); 
	}

}
