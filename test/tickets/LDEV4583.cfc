component extends = "org.lucee.cfml.test.LuceeTestCase" label="json" {

	function run( testResults, testBox ){
		describe( "json5 testing", function(){

			it( "isJson allows json", function(){
				var str = '{
					"name" : "lucee"
				}';
				expect( isJson( str ) ).toBeTrue();
				expect( structKeyExists( deserializeJson( str ), "name" ) ).toBeTrue();
			});

			it( "isJson should allow json5, block comment", function(){
				var str = '{
					"name" : "lucee"
					/*
						block comment
					*/
				}';
				expect( isJson( str ) ).toBeTrue();
				structKeyExists( deserializeJson( str ), "name" ) ;
			});

			it( "isJson should allow json5 inline", function(){
				var str = '{
					"name" : "lucee" // inline comment
				}';
				expect( isJson( str ) ).toBeTrue();
				structKeyExists( deserializeJson( str ), "name" );
			});

		} );
	}

}