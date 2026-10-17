component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2122", function() {
			it(title = "IsValid('email', 'email@example.com,') inconsistency with ACF", body = function( currentSpec ) {
				// Checking with valid emails
				expect(isValid( 'email', 'example@example.in' )).toBe(True);
				expect(isValid( 'email', 'test@test.com' )).toBe(True);
				expect(isValid( 'email', 'result@result.in' )).toBe(True);
				expect(isValid( 'email', 'sample@mail.edu' )).toBe(True);
				expect(isValid( 'email', 'testcase@13245.org' )).toBe(True);
				expect(isValid( 'email', 'testcase@yahoo.co.testingdomains' )).toBe(True);
				// checking with inValid emails
				expect(isValid( 'email', 'samp,le@mail.com' )).toBe(false);
				expect(isValid( 'email', 'test@sample@mail.co.in' )).toBe(false);
				expect(isValid( 'email', 'example@example.in/' )).toBe(false);
				expect(isValid( 'email', 'test@test.com,' )).toBe(false);
				expect(isValid( 'email', 'result@result.in&*(' )).toBe(false);
				expect(isValid( 'email', 'testcase@yahoo.com3>12' )).toBe(false);
				expect(isValid( 'email', 'testcase@yahoo.co.in@' )).toBe(false);
			});

			it(title = "isValid('email') checks a single address: list separators and surrounding whitespace make it invalid (as ACF)", body = function( currentSpec ) {
				var wrong = [];
				for ( var address in [
					"example@example.com,", "myname@yahoo.co,", "example@example.com;", ",example@example.com", "example@example.com, ",
					"a@example.com,b@example.com", "a@example.com;b@example.com", " example@example.com", "example@example.com ",
					"example@example.com" & chr( 9 ), "example@example.com" & chr( 10 )
				] ) {
					if ( isValid( "email", address ) ) arrayAppend( wrong, address );
				}
				expect( wrong ).toBeEmpty( "accepted but invalid: [" & arrayToList( wrong, "] [" ) & "]" );
				expect( isValid( "email", "example@example.com" ) ).toBeTrue();
			});
		});
	}
}
