component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function beforeAll(){
		errorString = "can't decode the the base64 input string";
	}

	public function run( testResults , testBox ) {
		describe( "Test suite for LDEV-245 ( ACF 11 compatibility )", function() {
			it("Checking encrypt() with BLOWFISH algorithm with key of length 1", function( currentSpec ){
				msg = "";
				try {
					key="1";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
					// writeDump(e);
				}
				expect(msg).toBe(errorString);
			});

			it("Checking encrypt() with BLOWFISH algorithm with key of length = 4", function( currentSpec ){
				msg = "";
				try {
					key="1zas";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
				}
				expect(msg).toBe("true");
			});

			it("Checking encrypt() with BLOWFISH algorithm with key of length = 5( not divisible by 4 )", function( currentSpec ){
				msg = "";
				try {
					key="1zasa";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
				}
				expect(msg).toBe(errorString);
			});

			it("Checking encrypt() with BLOWFISH algorithm with key of length = 20 ( max length )", function( currentSpec ){
				msg = "";
				try {
					key="12345678123456781234";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
				}
				expect(msg).toBe("true");
			});

			it("Checking encrypt() with BLOWFISH algorithm with key of length = 24 ( exceeding max length & divisible by 4 )", function( currentSpec ){
				msg = "";
				try {
					key="12345678123456781234";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
				}
				expect(msg).toBe(errorString);
			});
		});

		describe( "Test suite for LDEV-245 ( Railo 4.2.1.008 compatibility )", function() {
			it("Checking encrypt() with BLOWFISH algorithm with key of length 1", function( currentSpec ){
				msg = "";
				try {
					key="1";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
				}
				expect(msg).toBe(errorString);
			});

			it("Checking encrypt() with BLOWFISH algorithm with key of length 2", function( currentSpec ){
				msg = "";
				try {
					key = "1x";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
				}
				expect(msg).toBe("true");
			});

			it("Checking encrypt() with BLOWFISH algorithm with key of length = 4 ( divisible by 4 )", function( currentSpec ){
				msg = "";
				try {
					key="1zas";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
				}
				expect(msg).toBe("true");
			});

			it("Checking encrypt() with BLOWFISH algorithm with key of length = 5( not divisible by 4 )", function( currentSpec ){
				msg = "";
				try {
					key="1zasa";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
				}
				expect(msg).toBe("true");
			});

			it("Checking encrypt() with BLOWFISH algorithm with key of length = 22 ( max length )", function( currentSpec ){
				msg = "";
				try {
					key="1234567812345678123456";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
				}
				expect(msg).toBe("true");
			});

			it("Checking encrypt() with BLOWFISH algorithm with key of length = 23 ( exceeding max length & divisible by 4 )", function( currentSpec ){
				msg = "";
				try {
					key="123456781234567812345678";
					msg = BlowFishEncrypt( key );
				} catch( any e ) {
					msg = left(e.Message, 40);
				}
				expect(msg).toBe(errorString);
			});
		});
	}

	// Private functions
	private string function BlowFishEncrypt(key){
		data = "Susi Sorglos foehnte ihr Haar";
		sVars.cData = encrypt(data, key, "BLOWFISH");
		sVars.sData = decrypt(sVars.cData, key, "BLOWFISH");

		return toString(sVars.sData EQ data);
	}
}