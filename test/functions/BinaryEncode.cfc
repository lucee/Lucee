component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Testcase for binaryEncode()", body=function() {
			it(title="checking binaryEncode() function", body = function( currentSpec ) {
				var binary_data = ToBinary(ToBase64("I am a string"));
				assertEquals("4920616D206120737472696E67", "#binaryEncode(binary_data, "hex")#");
				assertEquals("-22!A;2!A('-T<FEN9P", "#trim(binaryEncode(binary_data, "UU"))#");
				assertEquals("SSBhbSBhIHN0cmluZw==", "#binaryEncode(binary_data, "base64")#");
			});
		});
	}
}