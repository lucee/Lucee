component extends="org.lucee.cfml.test.LuceeTestCase" labels="decryptBinary"{
	
	function run( testResults, testBox ) {
		describe("Testcase for decryptBinary()", function() {
			var ex = {};
			ex.key = '56RgnfAaMGCf4Ba4+XifQg=+';
			ex.password = 'testPassword';

			it( title="checking decryptBinary() with precise=false and validkey", body=function( currentSpec ) {
				ex.secret_Key = "mV8fvwNFU3fUk2MlgWiUxg==";
				ex.encrypted__password = encryptBinary(ex.password, ex.secret_Key, 'AES', 'Hex', 1, "false");
				ex.decrypted__password = decryptBinary(ex.encrypted__password, ex.secret_Key, 'AES', 'Hex', 1, "false");
				ex.base64__Encoded = binaryEncode(ex.decrypted__password, "base64");

				expect(trim(isbinary(ex.decrypted__password))).toBeTrue();
				expect(ex.base64__Encoded).tobe("testpassword")
				expect(trim(ex.secret_Key)).toBe("mV8fvwNFU3fUk2MlgWiUxg==");
				expect(trim(ex.password)).tobe("testpassword");
			});

			it( title="checking decryptBinary() with precise=false and invalidkey", body=function( currentSpec ) {
				ex.encrypted_password = encryptBinary(ex.password, ex.key, 'AES', 'Hex', 1, "false");
				ex.decrypted_password = decryptBinary(ex.encrypted_password, ex.key, 'AES', 'Hex', 1, "false");
				ex.base64_Encoded = binaryEncode(ex.decrypted_password, "base64");

				expect(trim(isbinary(ex.decrypted_password))).toBeTrue();
				expect(ex.base64_Encoded).tobe("testpassword")
				expect(trim(ex.KEY)).toBe("56RgnfAaMGCf4Ba4+XifQg=+");
				expect(trim(ex.password)).tobe("testpassword");
			});

			 it( title="checking decryptBinary() with precise=true and validkey", body=function( currentSpec ) {
				ex.secretKey = "mV8fvwNFU3fUk2MlgWiUxg==";
				ex.encryptedPassword = encryptBinary(ex.password, ex.secretKey, 'AES', 'Hex', 1, "true");
				ex.decryptedPassword = decryptBinary(ex.encryptedPassword, ex.secretKey, 'AES', 'Hex', 1, "true");
				ex.base64Encoded = binaryEncode(ex.decryptedPassword, "base64");

				expect(trim(isbinary(ex.decryptedPassword))).toBeTrue();
				expect(ex.base64Encoded).tobe("testpassword")
				expect(trim(ex.secretKey)).toBe("mV8fvwNFU3fUk2MlgWiUxg==");
				expect(trim(ex.password)).tobe("testpassword");
			});

			it( title="checking decryptBinary() with precise=true and invalidkey", body=function( currentSpec ) {
				try {
					ex.encrypted_password = encryptBinary(ex.password, ex.key, 'AES', 'Hex', 1, "false");
					ex.decrypted_password = decryptBinary(ex.encrypted_password, ex.key, 'AES', 'Hex', 1, "true");
				}
				catch(any e) {
					var result = e.message;
				}
				expect(trim(ex.password)).tobe("testpassword");
				expect(trim(result)).toBe("invalid character [=] in base64 string at position [23]");
			});
		});
	}
}