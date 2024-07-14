component extends="org.lucee.cfml.test.LuceeTestCase" {

	
	function run( testResults , testBox ) {
		describe( "test case for LDEV-4920", function() {
			it( title="multi line: test with no comment", body=function( currentSpec ) {
				str='{ "b": ""}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("");
			});

			it( title="multi line: test with comment before name", body=function( currentSpec ) {
				str='{ /*dd*/"b": ""}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("");
			});

			it( title="multi line: test with comment after name", body=function( currentSpec ) {
				str='{ "b"/*dd*/: ""}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("");
			});

			it( title="multi line: test with comment before number value", body=function( currentSpec ) {
				str='{ "b": /*dd*/1}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe(1);
			});

			it( title="multi line: test with comment after number value", body=function( currentSpec ) {
				str='{ "b": 1/*dd*/}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe(1);
			});

			it( title="multi line: test with comment before boolean value", body=function( currentSpec ) {
				str='{ "b": /*dd*/true}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe(true);
			});

			it( title="multi line: test with comment after boolean value", body=function( currentSpec ) {
				str='{ "b": true/*dd*/}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe(true);
			});

			it( title="multi line: test with comment before string value", body=function( currentSpec ) {
				str='{ "b": /*dd*/"susi"}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("susi");
			});

			it( title="multi line: test with comment after string value", body=function( currentSpec ) {
				str='{ "b": "susi"/*dd*/}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("susi");
			});

			it( title="multi line: test with comment before string (single quote) value", body=function( currentSpec ) {
				str='{ "b": /*dd*/''susi''}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("susi");
			});

			it( title="multi line: test with comment after string (single quote) value", body=function( currentSpec ) {
				str='{ "b": ''susi''/*dd*/}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("susi");
			});

			it( title="multi line: test with comment before struct value", body=function( currentSpec ) {
				str='{ "b": /*dd*/{"a":1}}';    
				var result=deserializeJSON(str);
				expect( result.b.a ).toBe(1);
			});

			it( title="multi line: test with comment after struct value", body=function( currentSpec ) {
				str='{ "b": {"a":1}/*dd*/}';    
				var result=deserializeJSON(str);
				expect( result.b.a ).toBe(1);
			});

			it( title="multi line: test with comment before array value", body=function( currentSpec ) {
				str='{ "b": /*dd*/[1,2,3]}';    
				var result=deserializeJSON(str);
				expect( result.b[1] ).toBe(1);
			});

			it( title="multi line: test with comment after array value", body=function( currentSpec ) {
				str='{ "b": [1,2,3]/*dd*/}';    
				var result=deserializeJSON(str);
				expect( result.b[1] ).toBe(1);
			});







			it( title="single line: test with comment before name", body=function( currentSpec ) {
				str='{ 
				//dd
				"b": ""}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("");
			});

			it( title="single line: test with comment after name", body=function( currentSpec ) {
				str='{ "b"//dd
				: ""}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("");
			});

			it( title="single line: test with comment before number value", body=function( currentSpec ) {
				str='{ "b": //dd
				1}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe(1);
			});

			it( title="single line: test with comment after number value", body=function( currentSpec ) {
				str='{ "b": 1//dd
				}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe(1);
			});

			it( title="single line: test with comment before boolean value", body=function( currentSpec ) {
				str='{ "b": //
				true}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe(true);
			});

			it( title="single line: test with comment after boolean value", body=function( currentSpec ) {
				str='{ "b": true//dd
				}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe(true);
			});

			it( title="single line: test with comment before string value", body=function( currentSpec ) {
				str='{ "b": //dd
				"susi"}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("susi");
			});

			it( title="single line: test with comment after string value", body=function( currentSpec ) {
				str='{ "b": "susi"//dd*
				}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("susi");
			});

			it( title="single line: test with comment before string (single quote) value", body=function( currentSpec ) {
				str='{ "b": //dd
				''susi''}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("susi");
			});

			it( title="single line: test with comment after string (single quote) value", body=function( currentSpec ) {
				str='{ "b": ''susi''//dd
				}';    
				var result=deserializeJSON(str);
				expect( result.b ).toBe("susi");
			});

			it( title="single line: test with comment before struct value", body=function( currentSpec ) {
				str='{ "b": //dd
				{"a":1}}';    
				var result=deserializeJSON(str);
				expect( result.b.a ).toBe(1);
			});

			it( title="single line: test with comment after struct value", body=function( currentSpec ) {
				str='{ "b": {"a":1}//dd
				}';    
				var result=deserializeJSON(str);
				expect( result.b.a ).toBe(1);
			});

			it( title="single line: test with comment before array value", body=function( currentSpec ) {
				str='{ "b": //dd
				[1,2,3]}';    
				var result=deserializeJSON(str);
				expect( result.b[1] ).toBe(1);
			});

			it( title="single line: test with comment after array value", body=function( currentSpec ) {
				str='{ "b": [1,2,3]//dd
				}';    
				var result=deserializeJSON(str);
				expect( result.b[1] ).toBe(1);
			});

		});
	}
}



