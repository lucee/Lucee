component extends="org.lucee.cfml.test.LuceeTestCase" labels="argon2" {

    function run( testResults , testBox ) {
        describe( title = "Testcase for Argon2CheckHash function", body = function() {
            it( title = "checking Argon2CheckHash function", body = function( currentSpec ) {
                secret = createUUID();
                generateHash = generateArgon2Hash(secret);
                expect( argon2checkhash(secret, generateHash)).toBeTrue();
                expect( argon2checkhash(123, generateHash)).toBeFalse();
            });
        });
    }
	private function getJavaVersion() {
	    var raw=server.java.version;
	    var arr=listToArray(raw,'.');
	    if(arr[1]==1) // version 1-9
	        return arr[2];
	    return arr[1];
	}
}