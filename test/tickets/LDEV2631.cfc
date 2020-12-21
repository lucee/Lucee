component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2631", function() {
			
			it(title = "test convert Hashset to collection", body = function( currentSpec ) {
				var hs = createObject( 'java', 'java.util.HashSet' ).init( ['foo','bar'] )
				var res="";
				for( item in hs ) {
					res&=","&item;
				}
				expect(res==",foo,bar" || res==",bar,foo").toBe(true);
			});
		});
	}
} 