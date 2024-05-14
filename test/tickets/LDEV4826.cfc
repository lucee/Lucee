component extends = "org.lucee.cfml.test.LuceeTestCase" {


	function run( testResults, testBox ){
		describe( "Test case for LDEV-4826", function(){
			
			it( title = "struct method call for existing value",  body = function( currentSpec ){
				var sct = {};
				sct["key"]="Susi";
				expect( sct.get( "key" ) ?: "EMPTY" ).toBe( "Susi" );
			});

			it( title = "struct method call for existing value containg null",  body = function( currentSpec ){
				var sct = {};
				sct["nulls"]=nullValue();
				expect( sct.get( "nulls" ) ?: "EMPTY" ).toBe( "EMPTY" );
			});

			it( title = "struct method call for NOT existing value ",  body = function( currentSpec ){
				var sct = {};
				sct["nulls"]=nullValue();
				expect( sct.get( "nulls" ) ?: "EMPTY" ).toBe( "EMPTY" );
			});



			it( title = "ConcurrentHashMap method call for existing value",  body = function( currentSpec ){
				var chm = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();
				chm.put("key","Susi");
				expect( chm.get( "key" ) ?: "EMPTY" ).toBe( "Susi" );
			});

			it( title = "ConcurrentHashMap method call for NOT existing value ",  body = function( currentSpec ){
				var chm = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();
				expect( chm.get( "undefined" ) ?: "EMPTY" ).toBe( "EMPTY" );
			});



			it( title = "struct get existing value",  body = function( currentSpec ) {
				var sct = {};
				sct["key"]="Susi";
				expect( sct["key"] ?: "EMPTY" ).toBe( "Susi" );
				expect( sct.key ?: "EMPTY" ).toBe( "Susi" );
			});

			it( title = "struct get NOT existing value",  body = function( currentSpec ) {
				var sct = {};
				expect( sct["undefined"] ?: "EMPTY" ).toBe( "EMPTY" );
				expect( sct.undefined ?: "EMPTY" ).toBe( "EMPTY" );
			});

			it( title = "function calls",  body = function( currentSpec ) {
				expect( susi().sorglos() ?: "EMPTY" ).toBe( "EMPTY" );
			});

		});
	}
}