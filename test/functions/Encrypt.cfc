component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for EncryptMember", function() {
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
	
			public void function testRC4(){
				var algo="RC4";
				var value="554122";
				var key=GenerateSecretKey(algo);
				var enc=Encrypt(value, key, algo);
				var dec=Decrypt(enc, key, algo);
				assertEquals(value,dec);
			}

			public void function testRC42(){
				var algo="RC4";
				var value="554122";
				var key="test";
				var enc=Encrypt(value, key, algo);
				var dec=Decrypt(enc, key, algo);
				assertEquals(value,dec);
			}
		});	
	}
}