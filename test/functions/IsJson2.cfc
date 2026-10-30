component extends = "org.lucee.cfml.test.LuceeTestCase" label="json" {

	function run( testResults, testBox ){
		describe( "json5 testing", function(){


			// we allowing, because other parser do as well (some)
			it( "isJson shouldn't allow json5, block comment by default (lenient=false)", function(){
				var str = '{
					"name" : "lucee"
					/*
						block comment
					*/
				}';
				expect( isJson( str ) ).toBeFalse();
				expect( isJson( var=str, lenient=true ) ).toBeTrue();
			});

			it( "isJson should allow json5, block comment when format is set to [json5]", function(){
				var str = '{
					"name" : "lucee"
					/*
						block comment
					*/
				}';
				expect( isJson( str,"json5" ) ).toBeTrue();
			});

			it( "isJson shouldn't allow json5, block comment when format is set to json", function(){
				var str = '{
					"name" : "lucee"
					/*
						block comment
					*/
				}';
				expect( isJson( str,"json" ) ).toBeFalse();
			});

			it( "isJson shouldn't allow json5 inline by default (lenient=false)", function(){
				var str = '{
					"name" : "lucee" // inline comment
				}';
				expect( isJson( str ) ).toBeFalse();
				expect( isJson( var=str, lenient=true ) ).toBeTrue();
			});

			it( "isJson should allow json5 inline when format is set to [json5]", function(){
				var str = '{
					"name" : "lucee" // inline comment
				}';
				expect( isJson( str,"json5" ) ).toBeTrue();
			});

			it( "isJson shouldn't allow json5 inline when format is set to [json]", function(){
				var str = '{
					"name" : "lucee" // inline comment
				}';
				expect( isJson( str,"json" ) ).toBeFalse();
			});


		} );
	}

}