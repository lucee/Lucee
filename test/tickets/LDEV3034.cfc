component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testbox ){
		describe( "Testcase for LDEV-3034", function(){
            var base64Valid = "dmFsaWQ=";
            var base64Invalid = "@@@@";
            it(title="binaryDecode() with valid base64 data", body=function( currentSpec ){
                res = (toString(binaryDecode(base64Valid, "base64")));
                expect(res).toBe("valid");
            });
            it(title="binaryEncode with valid binary data",body=function( currentSpec ){
                res = binaryEncode(toBinary(base64Valid), "base64");
                expect(res).toBe("dmFsaWQ=");
            });
            it(title="toBinary() with invalid base64 data",body=function( currentSpec ){
                try{
                    res = toBinary(base64Invalid);
                    hasError = false;      
                }
                catch(any e){
                    hasError = true;
                }
                expect(hasError).toBe(true);
            });
            it(title="binaryEncode() with String", body=function( currentSpec ){
                try{      
                    res = binaryEncode("String instead of binary Object", "base64");
                    hasError = false;      
                }
                catch(any e){
                    hasError = true;
                }
                expect(hasError).toBe(true);
            });
            it(title="binaryDecode() with invalid base64 data", body=function( currentSpec ){
                try{
                    res = binaryDecode(base64Invalid, "base64");  
                    hasError = false;
                }
                catch(any e){
                    hasError = true;
                }
                expect(hasError).toBe(true);
            });
        });
    }
}