component extends="org.lucee.cfml.test.LuceeTestCase" labels="decrypt"{
	
	function run( testResults, testBox ) {
		describe("Testcase for decrypt()", function() {
			var ex = {};
			ex.key = '56RgnfAaMGCf4Ba4+XifQg=+';
			ex.password = 'testPassword';
			it( title="checking decrypt() with precise=false and validkey", body=function( currentSpec ) {
				ex.secret_Key = "mV8fvwNFU3fUk2MlgWiUxg==";
				ex.encrypted__password = encrypt(ex.password, ex.secret_Key, 'AES', 'Hex',"",1,"false");
				ex.decrypted__password = decrypt(ex.encrypted__password, ex.secret_Key, 'AES', 'Hex',"",1,"false");

				expect(trim(ex.secret_Key)).toBe("mV8fvwNFU3fUk2MlgWiUxg==");
				expect(trim(ex.password)).toBe("testPassword");
				expect(trim(ex.decrypted__password)).toBe("testPassword");
			});
			
			it( title="checking decrypt() with precise=false and invalidkey", body=function( currentSpec ) {
				ex.encrypted_password = encrypt(ex.password, ex.key, 'AES', 'Hex',"",1,"false");
				ex.decrypted_password = decrypt(ex.encrypted_password, ex.key, 'AES', 'Hex',"",1,"false");

				expect(trim(ex.KEY)).toBe("56RgnfAaMGCf4Ba4+XifQg=+");
				expect(trim(ex.password)).toBe("testPassword");
				expect(trim(ex.decrypted_password)).toBe("testPassword");
			});

			it( title="checking decrypt() with precise=true and validkey", body=function( currentSpec ) {
				ex.secretKey = "mV8fvwNFU3fUk2MlgWiUxg==";
				ex.encryptedPassword = encrypt(ex.password, ex.secretKey, 'AES', 'Hex',"",1,"false");
				ex.decryptedPassword = decrypt(ex.encryptedPassword, ex.secretKey, 'AES', 'Hex',"",1,"true");

				expect(trim(ex.password)).toBe("testPassword");
				expect(trim(ex.decryptedPassword)).toBe("testPassword");
			});
			
			it( title="checking decrypt() with precise=true and invalidkey", body=function( currentSpec ) {
				try {
					ex.encrypted_password = encrypt(ex.password, ex.key, 'AES', 'Hex',"",1,"false");
					ex.decrypted_password = decrypt(ex.encrypted_password, ex.key, 'AES', 'Hex',"",1,"true");
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