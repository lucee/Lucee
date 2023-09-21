component extends="org.lucee.cfml.test.LuceeTestCase" {

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
}