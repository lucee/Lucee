component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4101", body=function() {
			it( title="Encrypt base64 test", body=function( currentSpec ) {
				var ex={};
                ex.algo="AES";
                ex.key='56RgnfAaMGCf4Ba4+XifQg=+';
                ex.password='testPassword';
                var result = "";
                try {
                    ex.encrypted_password = encrypt( ex.password, ex.key, 'AES', 'Hex' ); 
                } catch ( e ) {
                    // throws invalid character [=] in base64 string at position [23]
                    result = e.message;
                }
                expect( result ).toBe( "" ); // shouldn't throw
			});
		});
	}
}
