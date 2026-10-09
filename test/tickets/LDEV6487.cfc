/**
 * LDEV-6487: on 7.1 / 8.0 isValid( "email" ) used a placeholder regex after the mail code moved to the
 * mail extension. It rejected valid addresses (apostrophe, unicode, quoted local part, IP literal) and
 * accepted invalid ones (dots at the wrong place, no TLD). The expectations below are what 6.2 / 7.0
 * (MailUtil.isValidEmail) return, except the last describe block, where the new check is deliberately stricter,
 * and the quoted "a..b" and IPv6 literal cases, which LDEV-3095 made valid (RFC 5321).
 */
component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title='LDEV-6487: isValid( "email" ) must accept apostrophes and reject invalid dot placement in the local part', body=function(){

			it( title='returns true for an email address containing an apostrophe in the local part (o''reilly@example.com)', body=function() {

				expect( isValid( "email", "o'reilly@example.com" ) ).toBeTrue();

			});

			it( title='returns false for an email address whose local part starts with a dot (.foo@example.com)', body=function() {

				expect( isValid( "email", ".foo@example.com" ) ).toBeFalse();

			});

			it( title='returns false for an email address whose local part ends with a dot (user.@example.com)', body=function() {

				expect( isValid( "email", "user.@example.com" ) ).toBeFalse();

			});

			it( title='returns false for an email address with consecutive dots in the local part (a..b@example.com)', body=function() {

				expect( isValid( "email", "a..b@example.com" ) ).toBeFalse();

			});

		});

		describe( title='LDEV-6487: isValid( "email" ) behaves like 6.2 / 7.0', body=function(){

			it( title="accepts common addresses", body=function() {
				assertValid( [
					"a@gmail.com", "user+tag@gmail.com", "user+@gmail.com", "user.name+tag+sorting@example.com", "AZ.+-_az09@gmail.com",
					"first.last@sub.example.co.uk", "USER@EXAMPLE.COM", "foo@gm.ail.com", "user-@example.org", "1@1.com", "a@a-b.com",
					"testcase@13245.org", "testcase@yahoo.co.testingdomains", "user@example.museum", "test@xn--mller-kva.de", "a@sub.xn--p1ai"
				] );
			});

			it( title="accepts special characters allowed in the local part (apostrophe, &, /, =, {}, !, ##, %)", body=function() {
				assertValid( [
					"o'reilly@example.com", "user&foo@gmail.com", "user/foo@gmail.com", "user=foo@gmail.com", "user{x}@gmail.com",
					"user!x@gmail.com", "user##@gmail.com", "user%example.com@example.org"
				] );
			});

			it( title="accepts internationalized local parts and domains", body=function() {
				assertValid( [ "test@müller.de", "test@müller.çöm", "jürgen@example.com", "somthingçöm@gmail.com", "somthingçöm@çöm.com", "user@例え.テスト" ] );
			});

			it( title="rejects invalid local parts", body=function() {
				assertInvalid( [
					".foo@example.com", "user.@example.com", "a..b@example.com", "a@b@example.com", "a b@example.com", "samp,le@mail.com",
					"user(x)@gmail.com", "a-b.c@a@bc-_fgdg.dfgd.dj", "test@sample@mail.co.in"
				] );
			});

			it( title="rejects invalid domains (consecutive dots, no TLD, trailing dot, one letter TLD, underscore)", body=function() {
				assertInvalid( [
					"foo@gmail..com", "somthingçöm@gmail..com", "somthing@gmail..çöm", "user@localhost", "user@example", "a@b", "dddd@abc.com.",
					"user@.example.com", "user@example.c", "user@ex_ample.com", "a@abc-_fgdg.dfgd.dj", "dddd@abc-_fgdg.dfgd.", "@abc-_fgdg.dfgd.dj"
				] );
			});

			it( title="rejects malformed input", body=function() {
				assertInvalid( [
					"", "@", "@example.com", "user@", "testcase@yahoo.co.in@", "a@example.com, c@example.com", "example@example.in/",
					"result@result.in&*(", "testcase@yahoo.com3>12", "error@domain.com 😄", "foo@bar" & chr( 8207 ), "foo@bar.com" & chr( 8207 )
				] );
			});

			it( title="accepts quoted local parts", body=function() {
				assertValid( [
					'"john doe"@example.com', '"john"@example.com', '"a\"b"@example.com', '"a\\b"@example.com', '"a@b"@example.com', '"a.b"@example.com',
					'".a"@example.com', '""@example.com', '"jürgen"@example.com', '"x"@müller.de', '"' & repeatString( "a", 62 ) & '"@example.com',
					'"a..b"@example.com' // LDEV-3095: consecutive dots are allowed inside quotes (6.2 / 7.0 rejected them)
				] );
			});

			it( title="rejects malformed quoted local parts", body=function() {
				assertInvalid( [
					'"a"b"@example.com', '"unterminated@example.com', '"a' & chr( 10 ) & 'b"@example.com',
					'"' & repeatString( "a", 63 ) & '"@example.com'
				] );
			});

			it( title="accepts IPv4 address literals", body=function() {
				assertValid( [ "user@[192.168.0.1]", "user@[0.0.0.0]", "user@[255.255.255.255]", '"john doe"@[192.168.0.1]' ] );
				// LDEV-3095: IPv6 address literals are valid too (6.2 / 7.0 rejected them)
				assertValid( [ "user@[IPv6:2001:db8::1]" ] );
			});

			it( title="rejects malformed address literals", body=function() {
				assertInvalid( [ "user@[192.168.0.1", "user@[192.168.0.1].com", "user@[ 192.168.0.1 ]" ] );
			});

			it( title="applies the length limits (local part 64, domain label 63 and domain 255 as punycode)", body=function() {
				assertValid( [
					repeatString( "a", 64 ) & "@example.com",
					"a@" & repeatString( "b", 63 ) & ".com",
					"a@" & repeatString( repeatString( "b", 62 ) & ".", 4 ) & "com", // domain: 255
					"a@" & repeatString( "ü", 50 ) & ".com" // punycode label: 59
				] );
				assertInvalid( [
					repeatString( "a", 65 ) & "@example.com",
					"a@" & repeatString( repeatString( "b", 63 ) & ".", 4 ) & "com", // domain: 259
					"a@" & repeatString( "ü", 63 ) & ".com" // 63 characters, but the punycode label is longer than 63
				] );
			});

			it( title='cfparam type="email" uses the same check', body=function() {
				cfparam( name="local.ok", type="email", default="o'reilly@example.com" );
				expect( local.ok ).toBe( "o'reilly@example.com" );
				expect( function() {
					cfparam( name="local.bad", type="email", default="a..b@example.com" );
				} ).toThrow();
			});

		});

		describe( title='LDEV-6487: isValid( "email" ) is stricter than 6.2 / 7.0 (agreed in the review of PR 2830)', body=function(){

			it( title="rejects numeric or alphanumeric TLDs", body=function() {
				assertInvalid( [ "user@example.123", "user@123.456.789.012", "a@b.c0m", "user@example.co1" ] );
			});

			it( title="rejects domain labels that start or end with a hyphen or are longer than 63 characters", body=function() {
				assertInvalid( [ "user@-example.com", "user@example-.com", "a@" & repeatString( "b", 64 ) & ".com" ] );
			});

			it( title="rejects whitespace, display names and trailing characters (LDEV-2122)", body=function() {
				assertInvalid( [ " a@example.com", "a@example.com ", "John <john@example.com>", "test@test.com,", 'test" "test@gmail.com' ] );
			});

			it( title="rejects IPv4 literals that are out of range or not IPv4, and unbalanced brackets", body=function() {
				assertInvalid( [ "user@[256.1.1.1]", "user@[1.2.3]", "user@[1.2.3.4.5]", "user@[example.com]", "user@192.168.0.1]" ] );
			});

			it( title="rejects mixed quoted and unquoted local parts (obsolete RFC 5322 syntax, not allowed by RFC 5321)", body=function() {
				assertInvalid( [ '"john".doe@example.com', 'john."doe"@example.com' ] );
			});

		});
	}

	private function assertValid( required array addresses ) {
		var wrong = [];
		for ( var address in arguments.addresses ) {
			if ( !isValid( "email", address ) ) arrayAppend( wrong, address );
		}
		expect( wrong ).toBeEmpty( "rejected but valid: [" & arrayToList( wrong, "] [" ) & "]" );
	}

	private function assertInvalid( required array addresses ) {
		var wrong = [];
		for ( var address in arguments.addresses ) {
			if ( isValid( "email", address ) ) arrayAppend( wrong, address );
		}
		expect( wrong ).toBeEmpty( "accepted but invalid: [" & arrayToList( wrong, "] [" ) & "]" );
	}

}
