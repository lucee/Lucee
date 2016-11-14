component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "binary encode with less than 88 character", function() {
			it("simple binary decode with length less than 88 without '=' ", function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc3N2";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("can't decode the the base64 input string [aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2 ... truncated], because the input string has an invalid length");
			});

			it("binary decode with length less than 88 having '=' at last", function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc3N2=";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc3Nw==");
			});

			it('binary decode with length less than 88 n number of "=" at end', function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc====";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc=");
			});

			it("binary decode with length less than 88 having '=' in between", function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hB=ThONzJHMTc3Nj";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("can't decode the the base64 input string [aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2 ... truncated], because the input string has an invalid length");
			});

			it("binary decode with length less than 88 having n mumber of '=' in between", function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmlu====N0hBMThONzJHMTc3N2";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("can't decode the the base64 input string [aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2 ... truncated], because the input string has an invalid length");
			});
		});
		describe( "binary encode with 88 character", function() {
			it("simple binary decode with length 88 without '=' ", function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc3Njg2";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe(encodedString);
			});

			it("binary decode with length 88 having '=' at last", function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc3Njg=";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc3Njg=");
			});

			it('binary decode with length 88 n number of "=" at end', function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc3====";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc3");
			});

			it("binary decode with length 88 having '=' in between", function( currentSpec ) {
				hasError = false;
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDIm=dmluPTNEN0hBMThONzJHMTc3Njg";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					hasError = true;
				}
				expect(hasError).toBeTrue();
			});

			it("binary decode with length 88 having n mumber of '=' in between", function( currentSpec ) {
				hasError = false;
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDIm===luPTNEN0hBMThONzJHMTc3Njg";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					hasError = true;
				}
				expect(hasError).toBeTrue();
			});
		});
		describe( "binary encode with greater than 88 character", function() {
			it("simple binary decode with length greater than 88 without '=' ", function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBMThONzJHMTc3Njg2tk";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("can't decode the the base64 input string [aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2 ... truncated], because the input string has an invalid length");
			});

			it("binary decode with length greater than 88 having '=' at last", function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBThONzJHMTc3Nj2tkga=";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBThONzJHMTc3Nj2tkgQ==");
			});

			it('binary decode with length greater than 88 n number of "=" at end', function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBThONzJHMTc3Nj2tkga====";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBThONzJHMTc3Nj2tkgQ==");
			});

			it("binary decode with length greater than 88 having '=' in between", function( currentSpec ) {
				hasError = false;
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hB=ThONzJHMTc3Nj2skasd";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					hasError = true;
				}
				expect(hasError).toBeTrue();
			});

			it("binary decode with length greater than 88 having n mumber of '=' in between", function( currentSpec ) {
				try{
					encodedString = "aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hB======ThONzJHMTc3Nj2sk";
					var decode = BinaryDecode(encodedString, "base64");
					var encodeValue = binaryEncode(decode, "base64");
				} catch ( any e ){
					var encodeValue = e.message;
				}
				expect(encodeValue).toBe("aWQ9QkEtMzU3OSZ5PTIwMDEmbT1EQUtPVEEmeW1zPVBQJnVyZ2lkPU5ZMDImdmluPTNEN0hBThONzJHMTc3Nj2sk");
			});
		});
	}
}
