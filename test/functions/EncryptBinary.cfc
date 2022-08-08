component extends="org.lucee.cfml.test.LuceeTestCase" labels="encryptBinary"{
	
	function run( testResults, testBox ) {
		describe("Testcase for encryptBinary()", function() {
			var ex = {};
			ex.key = '56RgnfAaMGCf4Ba4+XifQg=+';
			ex.password = 'testPassword';
		
			it( title="checking encryptBinary() with precise=false and validkey", body=function( currentSpec ) {
				ex.secret_Key = "mV8fvwNFU3fUk2MlgWiUxg==";
				ex.encrypted__password = encryptBinary(ex.password, ex.secret_Key, 'AES', 'Hex', 1, "false");
				ex.base64__Encoded = binaryEncode(ex.encrypted__password, "base64");

				expect(trim(isbinary(ex.encrypted__password))).toBeTrue();
				expect(ex.base64__Encoded).tobe("4ejLf/alRPPecBYTZdIR9w==");
				expect(trim(ex.password)).toBe("testPassword");
				expect(trim(ex.secret_Key)).toBe("mV8fvwNFU3fUk2MlgWiUxg==");
			});

			it( title="checking encryptBinary() with precise=false and invalidkey", body=function( currentSpec ) {
				ex.encrypted_password = encryptBinary(ex.password, ex.key, 'AES', 'Hex', 1, "false");
				ex.base64_Encoded = binaryEncode(ex.encrypted_password, "base64");

				expect(trim(isbinary(ex.encrypted_password))).toBeTrue();
				expect(ex.base64_Encoded).tobe("26EI4JyCRW/8FLmQNpaBug==");
				expect(trim(ex.password)).toBe("testPassword");
				expect(trim(ex.KEY)).toBe("56RgnfAaMGCf4Ba4+XifQg=+");
			});

			it( title="checking encryptBinary() with precise=true and validkey", body=function( currentSpec ) {
				ex.secretKey = "mV8fvwNFU3fUk2MlgWiUxg==";
				ex.encryptedPassword = encryptBinary(ex.password, ex.secretKey, 'AES', 'Hex', 1, "true");
				ex.base64Encoded = binaryEncode(ex.encryptedPassword, "base64");

				expect(trim(isbinary(ex.encryptedPassword))).toBeTrue();
				expect(ex.base64Encoded).tobe("4ejLf/alRPPecBYTZdIR9w==");
				expect(trim(ex.password)).toBe("testPassword");
				expect(trim(ex.secretKey)).toBe("mV8fvwNFU3fUk2MlgWiUxg==");
			});
			
			it( title="checking encryptBinary() with precise=true and invalidkey", body=function( currentSpec ) {
				try {
					ex.encrypted_password = encryptBinary(ex.password, ex.key, 'AES', 'Hex', 1, "true");
				}
				catch(any e) {
					var result = e.message;
				}
				expect(trim(ex.password)).toBe("testPassword");
				expect(trim(result)).toBe("invalid character [=] in base64 string at position [23]");
			});
			
		});
	}
}