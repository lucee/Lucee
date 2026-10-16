/**
 * LDEV-3046: isValid( "email", "test <example@exaaple.com> aaa" ) returned true.
 * isValid( "email" ) checks a bare address (RFC 5322 addr-spec), not a mailbox with a display name or trailing text.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function run( testResults , testBox ) {
		describe( title='LDEV-3046: isValid( "email" ) with display names and trailing text', body=function(){

			it( title='returns false for "test <example@exaaple.com> aaa"', body=function() {
				expect( isValid( "email", "test <example@exaaple.com> aaa" ) ).toBeFalse();
			});

			it( title="returns false for an address followed by other text", body=function() {
				assertInvalid( [ "example@exaaple.com aaa", "<example@exaaple.com> aaa", "example@exaaple.com (comment)" ] );
			});

			it( title="returns false for a display name or angle brackets", body=function() {
				assertInvalid( [ "test <example@exaaple.com>", '"test" <example@exaaple.com>', "<example@exaaple.com>" ] );
			});

			it( title="returns true for the bare address", body=function() {
				expect( isValid( "email", "example@exaaple.com" ) ).toBeTrue();
			});

		});
	}

	private function assertInvalid( required array addresses ) {
		var wrong = [];
		for ( var address in arguments.addresses ) {
			if ( isValid( "email", address ) ) arrayAppend( wrong, address );
		}
		expect( wrong ).toBeEmpty( "accepted but invalid: [" & arrayToList( wrong, "] [" ) & "]" );
	}

}
