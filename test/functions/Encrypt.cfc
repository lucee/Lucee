component extends="org.lucee.cfml.test.LuceeTestCase" labels="encrypt"{
	function run( testResults , testBox ) {
		describe( "test case for Encrypt()", function() {
			var ex = {};
			ex.key = '56RgnfAaMGCf4Ba4+XifQg=+';
			ex.password='testPassword';
			it(title = "Checking with Encrypt()", body = function( currentSpec ) {
				assertEquals("hallo welt",trim(decrypt("hallo welt".encrypt("stringkey"),"stringkey")));
			});

			it(title = "Checking with Encrypt", body = function( currentSpec ) {

				assertEquals("hallo welt","#trim(decrypt(encrypt("hallo welt","stringkey"),"stringkey"))#");
					
				key=generateSecretKey("AES");
				assertEquals("hallo welt","#trim(decrypt(encrypt("hallo welt",key,"AES"),key,"AES"))#");

				key=generateSecretKey("BLOWFISH");
				assertEquals("hallo welt","#trim(decrypt(encrypt("hallo welt",key,"BLOWFISH"),key,"BLOWFISH"))#");

				key=generateSecretKey("DES");
				assertEquals("hallo welt","#trim(decrypt(encrypt("hallo welt",key,"DES"),key,"DES"))#");

				key=generateSecretKey("DESEDE");
				assertEquals("hallo welt","#trim(decrypt(encrypt("hallo welt",key,"DESEDE"),key,"DESEDE"))#");
					
				try{
					key="susi";
					assertEquals("hallo welt","#trim(decrypt(encrypt("hallo welt",key,"AES"),key,"AES"))#");
					fail("must throw:The key specified is not a valid key for this encryption: Invalid AES key length: 24.");
				}
				catch(any e){}
			});	
			

			it( title="checking encrypt() with precise=false and validkey", body=function( currentSpec ) {
				ex.encrypted_password = encrypt(ex.password, ex.key, 'AES', 'Hex',"",1,"false");
				expect(trim(ex.password)).toBe("testPassword");
				expect(trim(ex.KEY)).toBe("56RgnfAaMGCf4Ba4+XifQg=+");
				expect(trim(ex.encrypted_password)).toBe("EB952E18E5736006D9F12137C87EB43D");
			});
			
			it( title="checking encrypt() with precise=true", body=function( currentSpec ) {
				try {
					ex.encrypted_password = encrypt(ex.password, ex.key, 'AES', 'Hex',"",1,"true");
				}
				catch(any e) {
					var result = e.message;
				}
				expect(trim(ex.password)).toBe("testPassword");
				expect(trim(result)).toBe("invalid character [=] in base64 string at position [23]");
			});

			it( title="checking encrypt() with precise=true and validkey", body=function( currentSpec ) {
				ex.secretKey = "mV8fvwNFU3fUk2MlgWiUxg==";
				expect(trim(ex.password)).toBe("testPassword");
				ex.encryptedPassword = encrypt(ex.password, ex.secretKey, 'AES', 'Hex',"",1,"true");
				expect(trim(ex.encryptedPassword)).toBe("A3ECB851A6259B3FFE6E81B129E89BE0");
			});

			it( title="checking encrypt() with algorithm=RC4", body=function( currentSpec ) {
				var algo = "RC4";
				var value = "554122";
				var key = GenerateSecretKey(algo);
				var enc = Encrypt(value, key, algo);
				var dec = Decrypt(enc, key, algo);
				assertEquals(value,dec);
			});

			it( title="checking encrypt() with algorithm=RC4", body=function( currentSpec ) {
				var algo = "RC4";
				var value = "554122";
				var key = "test";
				var enc = Encrypt(value, key, algo);
				var dec = Decrypt(enc, key, algo);
				assertEquals(value,dec);
			})
		});	
	}
}