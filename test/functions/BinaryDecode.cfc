component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BinaryDecode()", body=function() {
			it(title="invalid characters", body = function( currentSpec ) {
				
				var failed=false;
				try{
					binaryDecode("@@@@", "base64");
					
					
					fail("must throw:1.7 must be within range: ( -1 : 1 )");
				}
				catch(local.exp){
					failed=true;
				}
				assertEquals(failed,true);

			});

			it(title="valid characters, invalid length precise mode", body = function( currentSpec ) {
				var failed=false;
				try{
					binaryDecode("String", "base64",true);
					
					
					fail("must throw:1.7 must be within range: ( -1 : 1 )");
				}
				catch(local.exp){
					failed=true;
				}
				assertEquals(failed,true);

			});


			it(title="valid characters, invalid length NOT precise mode", body = function( currentSpec ) {
				assertEquals(binaryDecode("String", "base64")[1],74);
				assertEquals(binaryDecode("String", "base64",false)[1],74);
				
			});

			it(title="valid characters, valid length", body = function( currentSpec ) {
				assertEquals(binaryDecode("String==", "base64")[1],74);
				assertEquals(binaryDecode("String==", "base64",false)[1],74);
				assertEquals(binaryDecode("String==", "base64",true)[1],74);
			});

			it(title="invalid characters with precise mode", body = function( currentSpec ) {
				expect( function (){ binaryDecode("--------", "base64", true) }).toThrow();
				expect( function (){ binaryDecode("@@@@@@@@", "base64", true) }).toThrow();
				expect( function (){ binaryDecode("string_with_characters==", "base64", true) }).toThrow();
				expect( function (){ binaryDecode("@@@@test", "base64", true) }).toThrow();
			});
			
		});
	}
}

